package com.chatsito.realtime.notification;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import io.grpc.Server;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

@Component
public class NotificationGrpcServer implements SmartLifecycle {
    private final int port;
    private final NotificationGrpcService notificationGrpcService;
    private volatile Server server;
    private volatile boolean running;

    public NotificationGrpcServer(
            @Value("${legacy.notification-grpc.port:8090}") int port,
            NotificationGrpcService notificationGrpcService) {
        this.port = port;
        this.notificationGrpcService = notificationGrpcService;
    }

    @Override
    public synchronized void start() {
        if (running) {
            return;
        }
        try {
            server = NettyServerBuilder.forPort(port)
                    .addService(notificationGrpcService)
                    .build()
                    .start();
            running = true;
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Unable to start notification gRPC server on port " + port, exception);
        }
    }

    @Override
    public synchronized void stop() {
        var activeServer = server;
        server = null;
        running = false;
        if (activeServer == null) {
            return;
        }

        activeServer.shutdown();
        try {
            if (!activeServer.awaitTermination(5, TimeUnit.SECONDS)) {
                activeServer.shutdownNow();
            }
        } catch (InterruptedException exception) {
            activeServer.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    int boundPort() {
        var activeServer = server;
        return activeServer == null ? -1 : activeServer.getPort();
    }
}
