package com.chatsito.api.chat.grpc;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import io.grpc.Server;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

@Component
public class ChatGrpcServer implements SmartLifecycle {
    private final int port;
    private final ChatGrpcService chatGrpcService;
    private volatile Server server;
    private volatile boolean running;

    public ChatGrpcServer(
            @Value("${legacy.chat-grpc.port:5001}") int port,
            ChatGrpcService chatGrpcService) {
        this.port = port;
        this.chatGrpcService = chatGrpcService;
    }

    @Override
    public synchronized void start() {
        if (running) {
            return;
        }
        try {
            server = NettyServerBuilder.forPort(port)
                    .addService(chatGrpcService)
                    .build()
                    .start();
            running = true;
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to start chat gRPC server on port " + port, exception);
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
