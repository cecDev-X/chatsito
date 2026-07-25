import { combineReducers } from "redux";
import auth from "./auth";
import posts from "./posts";
import chat from "./chat";
import notifications from "./notifications";

const rootReducer = combineReducers({
    auth,
    posts,
    chat,
    notifications
});

export type RootState = ReturnType<typeof rootReducer>;
export default rootReducer;