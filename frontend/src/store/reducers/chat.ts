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

const initialState = {
    unReadedMessage:0,
    chatListUsers:[],
    chatListMessages: [],
    MessageList: [],
    MessagesBetweenTwoUsers: [],
    hasMoreMessages: true,
    onlineUserIds: [] as string[],
}

const chatReducer = (state= initialState, action: any) => {
    switch(action.type){
        case CLEAR_UN_READED_MSG:
            return {...state, MessagesBetweenTwoUsers: [], hasMoreMessages: true};

        case GET_UNREADED_MESSAGE:
            return {...state, unReadedMessage: action.payload.data.total,
                MessageList: action.payload.data.messages,
            }
        case GET_MSG_BETWEEN_TWO_USERS_BY_NUM:
            const incoming = action.payload.msgs || [];
            const hasMoreMessage = action.payload.hasMore !== false;

            if(state.MessagesBetweenTwoUsers.length >0){
                const existingIds = new Set(state.MessagesBetweenTwoUsers.map((m: any)=> m._id));
                const newMsgs = incoming.filter((m: any) => !existingIds.has(m._id));
                return {...state,
                    MessagesBetweenTwoUsers: [...newMsgs, ...state.MessagesBetweenTwoUsers],
                    hasMoreMessages: hasMoreMessage
                }
            }else{
                return {...state, hasMoreMessages: hasMoreMessage, MessagesBetweenTwoUsers: incoming};
            }
        case GET_CHAT_LIST_USERS: {
            const onlineSet = new Set(state.onlineUserIds);
            const mergedList = (action.payload ||[]).map((u: any) =>({
                ...u,
                online: onlineSet.has(u._id),
            }));
            return {...state, chatListUsers: mergedList};
        }
        case UPDATE_ONLINE_USERS: {
            const onlineUserIds:string[] = action.payload || [];
            const onlineSet = new Set(onlineUserIds);

            const chatListUsers = state.chatListUsers.map((el: any) => ({
                ...el,
                online: onlineSet.has(el._id),
            }));
            return {...state, onlineUserIds, chatListUsers};
        }
        case SEND_MESSAGE: {
            const newMsg= action.payload.Message;
            const alreadyExists= state.MessagesBetweenTwoUsers.some((m: any) => m._id === newMsg._id);
            if(alreadyExists) return state;

            if(action.payload.IsRecevedMessage){
                const n = state.unReadedMessage +1;
                const updatedChatListUsers = state.chatListUsers.map((el: any) => {
                    if(el._id === newMsg.sender){
                        return {...el, unReadedMessage:(el.unReadedMessage || 0) + 1};
                    }
                    return el;
                });
                return {...state,
                    unReadedMessage: n,
                    chatListUsers: updatedChatListUsers,
                    MessagesBetweenTwoUsers: [...state.MessagesBetweenTwoUsers, newMsg],
                } 
            } else{
                return {...state, MessagesBetweenTwoUsers: [...state.MessagesBetweenTwoUsers, newMsg]}
            };
        }
        case MARK_MSG_AS_READED: {
            const { otheruid} = action.payload;
            let rmNum =0;
            const updatedChatListUsers = state.chatListUsers.map((el: any) => {
                if(el._id === otheruid){
                    rmNum = el.unreadMessage || 0;
                    return {...el, unreadedMessages: 0};
                }
                return el;
            });
            return {...state, 
                unReadedMessage: Math.max(0, state.unReadedMessage - rmNum),
                chatListUsers: updatedChatListUsers}; 
        }
        default: 
            return state;
        }       
}
export default chatReducer;
