import axios, {AxiosInstance, AxiosResponse} from 'axios';
import {Post, User, AuthData} from '../types';

export const createAPI = (): AxiosInstance => {
    const newApi = axios.create({baseURL: process.env.REACT_APP_API_URL});

    newApi.interceptors.request.use((req) =>{
        const profile = localStorage.getItem('profile');
        if(profile && req.headers){
            req.headers.Authorization = `Bearer ${JSON.parse(profile).token}`;
        }
        return req;
    });
    return newApi;
}
//Posts

export const fetchPost = (id: string): Promise<AxiosResponse <({post: Post})>> => createAPI().get(`/posts/${id}`);

export const fetchPosts = (page: number, id: string | null, profileId: string | null = null): Promise<AxiosResponse <{data: Post[], currentPage: number, numberPages: number}>> => {
    const api = createAPI();
    let url = `posts?page=${page}`;
    if(id) url += `&id=${id}`;
    if(profileId) url += `&profileId=${profileId}`;
    return api.get(url);    
}

export const createPost = (newPost: Partial<Post>): Promise<AxiosResponse <Post>> => createAPI().post('/posts', newPost);

export const LikePost = (id: string): Promise<AxiosResponse <Post>> => createAPI().patch(`/posts/${id}/likePost`);

export const commentPost = (value: string, id: string): Promise<AxiosResponse<{post: Post}>> => createAPI().post(`/posts/${id}/commentPost`, {value});

export const updatePost = (id: string, updatedPost: Partial<Post>): Promise<AxiosResponse <{data: Post}>> => createAPI().patch(`/posts/${id}`, updatedPost);

export const deletePost = (id: string): Promise<AxiosResponse <{message: string}>> => createAPI().delete(`/posts/${id}`);

export const fetchPostUsersBySearch = (searchQuery: {searchData: string}): Promise<AxiosResponse<{data: {user: User[], posts: Post[]}}>> => {
    const api = createAPI();
    return api.get(`/posts/search?searchQuery=${searchQuery.searchData}`);
}

// User
export const signIn = (formData: any): Promise<AxiosResponse<AuthData>> => {
    const api = createAPI();
    return api.post('user/signin', formData);
}

export const signUp = (formData: any): Promise<AxiosResponse<AuthData>> => {
    const api = createAPI();
    return api.post('user/signup', formData);
}

export const fechUserProfile = (id: string): Promise<AxiosResponse<{user: User, posts: Post[], postsCount?: number}>> => {
    const api = createAPI();
    return api.get(`user/getUser/${id}`);
}

export const getSugUser = (id: string): Promise<AxiosResponse<{users:User[]}>> => {
    const api = createAPI();
    return api.get(`user/getSug?id=${id}`);
}

export const updateUser = (userData: User): Promise<AxiosResponse<{user:User, posts: Post[]}>> => {
    const api = createAPI();
    return api.patch(`user/Update/${userData._id}`, userData);
}

export const following = (id: string): Promise<AxiosResponse<User>> => {
    const api = createAPI();
    return api.patch(`user/${id}/following`);
}
//Chat
export const sendMessage = (msg: {content: string, sender: string, recever: string}): Promise<AxiosResponse<any>> => {
    const api = createAPI();
    return api.post('chat/sendmessage', msg);
} 
export const getUnreadMsgNum =(id: string): Promise<AxiosResponse<{total: number, messages: any[]}>> => {
    const api = createAPI();
    return api.get(`chat/get-user-unreadedmsg?userid=${id}`);
}

export const getMsgsBetweenTwoUsersByNum =(beforeId: string | null, firstuid:string, seconduid: string): Promise<AxiosResponse<{msgs: any[], hasMore:boolean}>> => {
    const api = createAPI();
    return api.get(`chat/getmsgsbynums?beforeId=${beforeId || ''}&firstuid=${firstuid}&seconduid=${seconduid}`);
} 

export const markMsgAsReaded = (mainuid: string, otheruid: string): Promise<AxiosResponse<any>> => {
    const api = createAPI();
    return api.get(`chat/mark-msg-asreaded?mainuid=${mainuid}&otheruid=${otheruid}`);
} 

//Notification
export const getNotificationForUser = (id: string): Promise<AxiosResponse<{notifications: any[]}>> => {
    const api = createAPI();
    return api.get(`notification/${id}`);
}

export const markNotificationAsReaded = (id: string): Promise<AxiosResponse<any>> => {
    const api = createAPI();
    return api.get(`notification/mark-notification-asreaded?id=${id}`);
}