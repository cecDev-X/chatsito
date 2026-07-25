import { GET_NOTIFICATIONS_FOR_USER, MARK_NOTIFICATIONS_AS_READED } from "../constants/actionTypes";
import * as api from "../../api/index";

const getUserId = () => {
    const user = JSON.parse(localStorage.getItem("profile") || "{}");
    return user?.result?._id || null;
}

export const getNotifyForUser = () => async (dispatch: any) => {
    try{
        const userId = getUserId();
        if(!userId) return;
        const { data} = await api.getNotificationForUser(userId);
        dispatch({type: GET_NOTIFICATIONS_FOR_USER, payload:data});
    }catch(error){
        console.log(error);
    }
}

export const markNotifyAsReaded =() => async(dispatch: any) => {
    try{
        const userId = getUserId();
        if(!userId) return;
        await api.markNotificationAsReaded(userId);
        dispatch({type: MARK_NOTIFICATIONS_AS_READED})
    }catch(error){
        console.log(error);
    }
}
