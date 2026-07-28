// @ts-nocheck
import {
    CLEAR_UN_READED_MSG,
    GET_UNREADED_MESSAGE,
    GET_MSG_BETWEEN_TWO_USERS_BY_NUM,
    GET_CHAT_LIST_USERS,
    UPDATE_ONLINE_USERS,
    SEND_MESSAGE,
    MARK_MSG_AS_READED
}from '../constants/actionTypes'

import * as api from '../../api/index';
import {v4 as uuidv4} from 'uuid';

const getUserId = () => {
    const user = JSON.parse(localStorage.getItem('profile') || null);
    return user?.result?.id || null;
}

export const sendNewMessage = (content, sender, recever) => async (dispatch: any) => {
    try{
        var Message = {"content": content, "sender": sender, "recever": recever};
        const data = await api.sendMessage(Message);
        Message = {"_id": uuidv4(), "content": content, "sender": sender, "recever": recever};
        dispatch({type: SEND_MESSAGE, payload: {data, Message}})
    }catch(error){
        console.log(error);
    }
}


export const getUnReadMessage =() => async (dispatch: any) => {
    try{
        const userId = getUserId();
        
        if(!userId) return;

        const {data} = await api.getUnreadMsgNum(userId);
        dispatch({type: GET_UNREADED_MESSAGE, payload: data});
    }catch(error){
        console.log(error);
    }
}


export const getMsgBetweenTwoUsers = (page, fuserid, suserid) => async (dispatch: any) => {
    try{
        const { data } = await api.getMsgsBetweenTwoUsersByNum(page, fuserid, suserid);
        dispatch({type: GET_MSG_BETWEEN_TWO_USERS_BY_NUM, payload: data});
    }catch(error){
        console.log(error);
    }
}


// GETChatUserListData
export const getChatUserListData = () => async (dispatch: any) => {
    try{
        const userId= getUserId();
        if(!userId) return;

        const {data} = await api.fechUserProfile(userId);

        var userList= [];
        var finalUserList=[];
        var followers = data.user.followers;
        var following = data.user.following;

        var userListIDES = [...new Set([...followers, ...following])];

        const promises = userListIDES.map(async(id) =>{
            const {data} = await api.fechUserProfile(id);
            return data.user;
        });
        userList = await Promise.all(promises);
        
        const msg = await api.getUnreadMsgNum(userId);
        var msgs = msg.data.messages;


        for(let i=0; i< userList.length; i++){
            const el = userList[i];
            var final = {
                "_id": el._id,
                "name": el.name,
                "imageUrl": el.imageUrl,
                "unreadMessages": 0
            }
            finalUserList.push(final)
            
        }
        finalUserList.forEach(main => {
            msgs.forEach((x) => {
                if(x.otherUseid === main._id){ //no recuerdo si es otherUserId o otherUserid
                    main.unreadMessages= x.numOfUnreadedMessages;

                }
            });
        });

        dispatch({type: GET_CHAT_LIST_USERS, payload: finalUserList});
    }catch(error){
        console.log(error);
    }
}
//markMSGAsReaded
export const markMSGAsReaded = (mainuid, otheruid) => async (dispatch: any) => {
    try{
        const {data} = await api.markMsgAsReaded(mainuid, otheruid);
        dispatch({type: MARK_MSG_AS_READED, payload: {mainuid, otheruid}});

    }catch(error){
        console.log(error);
    }
}
//clearUnreadedMsgBetweenUs
export const clearUnreadedMsgBetweenUs = () => async (dispatch) => {
    try{
        dispatch({type: CLEAR_UN_READED_MSG})
    }catch(error){
        console.log(error);
    }
}
//UpdateOnlineList
export const updateOnlineList =(onlineUsers) => async (dispatch) => {
    try{
        dispatch({type: UPDATE_ONLINE_USERS, payload: onlineUsers});
    }catch(error){
        console.log(error);
    }
}

//UpdateMessagesBetweenTwoUs
export const updateMessagesBetweenTwoUs = (message, isReceived = true) => async(dispatch) => {
    try{
        const Message = {"_id": uuidv4(), "content": message.content, "sender": message.sender, "recever": message.recever};
        dispatch({type: SEND_MESSAGE, payload: {Message, IsRecevedMessage: isReceived}});
    }catch(error){
        console.log(error);
    }
}