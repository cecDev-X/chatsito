class WebSocketService {
    constructor() {
        if(!WebSocketService.instance) {
            this._ws = null;
            this._listeners = [];
            this._reconnectTimer = null;
            this._reconnectAttempts = 0;
            this._maxReconnectAttempts = 10;
            WebSocketService.instance = this;
        }
        return WebSocketService.instance;
    }

    _connect(){
        try {
            const configuredUri = process.env.REACT_APP_RealTimeUrl;
            const uri = configuredUri?.startsWith('/')
                ? `${window.location.protocol === 'https:' ? 'wss:' : 'ws:'}//${window.location.host}${configuredUri}`
                : configuredUri;
            const profile = JSON.parse(localStorage.getItem("profile"));
            const userId = profile?.result?.id;

            if (!userId || !uri) return null;

            console.log("Establishing new WebSocket chat connection...", userId);
            const ws = new WebSocket(`${uri}${userId}`);

            ws.addEventListener("message", (event) => {
                this._listeners.forEach((fn) =>{
                    try { fn(event);} catch (e) {console.error("WebSocket listener error:", e)};
                });
            });

            ws.addEventListener("close", () => {
                console.log("WebSocket chat closed, reconnecting...");
                this._ws = null;
                this._scheduleReconnect();
            });

            ws.addEventListener("open", () => {
                this._reconnectAttempts = 0;
            });

            this._ws = ws;
            return ws;
        } catch (error) {
            console.error("WebSocket connection error:", error);
            return null;
        }
    }

    _scheduleReconnect() {
        if (this._reconnectTimer) return;
        if (this._reconnectAttempts >= this._maxReconnectAttempts) {
            console.error("Max WebSocket reconnect attempts reached");
            return;
        }
        this._reconnectAttempts++;
        const delay = Math.min(1000 * Math.pow(2, this._reconnectAttempts - 1), 30000);
        console.log(`Reconnecting WebSocket in ${delay}ms (attempt ${this._reconnectAttempts})`);
        this._reconnectTimer = setTimeout(() => {
            this._reconnectTimer = null;
            this._connect();
        }, delay);
    }

    getConnection(){
        if (!this._ws || this._ws.readyState === WebSocket.CLOSED || this._ws.readyState === WebSocket.CLOSING) {
            return this._connect();
        }
        return this._ws;
    }

     addMessagelistener(fn) {
        if (!this._listeners.includes(fn)) {
            this._listeners.push(fn);
        }
        this.getConnection();
        return () => this.removeMessageListener(fn);
    }

    removeMessageListener(fn) {
        this._listeners = this._listeners.filter((listener) => listener !== fn);
    }

    sendJson(payload) {
        const ws = this.getConnection();
        if (ws && ws.readyState === WebSocket.OPEN) {
            ws.send(JSON.stringify(payload));
            return true;
        }
        return false;
    }

    closeConnection() {
        if (this._reconnectTimer) {
            clearTimeout(this._reconnectTimer);
            this._reconnectTimer = null;
        }
        if (this._ws){
            this._ws.close();
            this._ws = null;
        }
        this._listeners = [];
        this._reconnectAttempts = 0;
    }

}

const instance = new WebSocketService();
export default instance;
