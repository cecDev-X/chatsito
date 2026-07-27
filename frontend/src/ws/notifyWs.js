class WebSocketServiceNotify {
    constructor(){
        if(!WebSocketServiceNotify.instance){
            this._ws = null;
            WebSocketServiceNotify.instance = this;
        }
        return WebSocketServiceNotify.instance;
    }

    createConnection(){
        try{
            const url= process.env.REACT_APP_RealTimeNotificationUrl;
            const profile = JSON.parse(localStorage.getItem("profile"));
            const userId = profile?.result?._id;

            if(!userId || !url) return null;
            if(!this._ws || (this.ws.readyState !== WebSocket.OPEN && this._ws.readyState !== WebSocket.CONNECTING)){
                console.log("Conexion web socket establecida");
                this._ws = new WebSocket(`${url}${userId}`);
            }

        }catch(error){
            console.error("Web", error);
            return null;
        }
    }


    getConnection(){
        return this.createConnection();
    }

    closeConnection(){
        if(this._ws){
            if(this._ws.readyState === WebSocket.OPEN || this._ws.readyState === WebSocket.CONNECTING){
                this._ws.close();

            }
            this._ws = null;
        }

    }
}

const instance = new WebSocketServiceNotify();
export default instance;