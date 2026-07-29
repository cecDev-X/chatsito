


import {jwtDecode} from 'jwt-decode';
import * as actionType from '../../store/constants/actionTypes';
import React, { useEffect, useRef, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { useLocation, useNavigate } from 'react-router-dom';
import { AppBar, Avatar, Badge, Box, Button, IconButton, Menu, MenuItem, Stack, Toolbar, useMediaQuery, useTheme } from '@mui/material';
import SearchIcon from '@mui/icons-material/Search';
import { RootState } from '../../store/reducers';
import { getUnReadMessage, updateOnlineList } from '../../store/actions/chat';
import { getNotifyForUser } from '../../store/actions/notifications';
import WebSocketServiceNotyfy from '../../ws/notifyWs.js'
import WebSocketService from '../../ws/RealTimeWs';
import { Icons, Search, SearchIconWrapper, StyledInputBase } from '../MainStyles';
import { Chat, Login, Notifications } from '@mui/icons-material';
import logo from '../../assets/logito.png';


const NavBar: React.FC<{ id?: string}> = ({id})=> {
 const dispatch = useDispatch();
 const navigate = useNavigate();
 const location = useLocation();

 const theme = useTheme();


 const isMobile = useMediaQuery(theme.breakpoints.down('sm'));

 const authData = useSelector((state: RootState)=> state.auth.authData);
 const { UnReadedNotificationNumbers } = useSelector((state: RootState) => state.notifications);
 const { unReadedMessage } = useSelector((state: RootState) => state.chat)

  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const [search, setSearch] = useState('');
  const [user, setUser] = useState<any>(authData || JSON.parse(localStorage.getItem('profile') || 'null'));
  const chatHandlerRef = useRef<((event: MessageEvent) => void) | null>(null);
 const open = Boolean(anchorEl);

 useEffect(()=>{
    if(authData){
        setUser(authData);
    } else {
        setUser(JSON.parse(localStorage.getItem('profile') || 'null'))
    }
 }, [authData]);


 useEffect(()=>{
    const token = user?.token;
    if(token){
      try {
        const decodedToken: any = jwtDecode(token);
        const expiresAt = decodedToken.expires ?? decodedToken.exp;
        if(typeof expiresAt !== 'number' || expiresAt * 1000 < Date.now()){
            logout(true);
            return;
        }
      } catch (_) {
        logout(true);
        return;
      }
        
        // initilize realtime connctions 
        // todo 
        initializeRealTime();
    }

    // Refresh Counts 
    if(user?.result?.id){
        dispatch(getUnReadMessage() as any);
        dispatch(getNotifyForUser() as any);
    }
 }, [location, user?.result?.id]);

 const initializeRealTime = ()=> {
    if (!user?.result?.id) return;

    // notify ws 
    const notifyWs = WebSocketServiceNotyfy.getConnection();
    if(notifyWs){
        notifyWs.onmessage = () => {
            dispatch(getNotifyForUser() as any);
        }
    }

    const chatHandler = (event: MessageEvent) => {
        try {
            const message = JSON.parse(event.data);
            if(message.onlineFriends !== undefined) {
                const uniqueUsers = Array.from(new Set(message.onlineFriends));
                dispatch(updateOnlineList(uniqueUsers) as any);

            } else if (message.content){
                dispatch(getUnReadMessage() as any);
            }
        } catch (_) {}
    };

    if (chatHandlerRef.current) {
      WebSocketService.removeMessageListener(chatHandlerRef.current);
    }
    chatHandlerRef.current = chatHandler;

    WebSocketService.addMessagelistener(chatHandler);

 }

 const logout = (sessionExpired = false) => {
     dispatch({  type: actionType.LOGOUT });
     navigate('/auth', sessionExpired ? {state: {sessionExpired: true}} : undefined);
    setUser(null);
    setAnchorEl(null);
    WebSocketService.closeConnection();
    WebSocketServiceNotyfy.closeConnection();
 }

 const hanldeClick = (event: React.MouseEvent<HTMLElement>) => setAnchorEl(event.currentTarget);
 const handleClose = () => setAnchorEl(null);

 const handleKeySearch = (e: React.KeyboardEvent) =>{
    if(e.key === 'Enter' && search.trim()){
        navigate(`/Search?searchQuery=${search}`);
    }
 };

 return (
    <AppBar position='sticky'>
        <Toolbar>
            <Stack direction="row" spacing={2} sx={{ justifyContent:'space-between', alignItems:'center', width:'100%' }}>
            <Box onClick={()=> navigate('/', {replace: true})} sx={{display: {xs: 'none', sm:'flex'}, cursor:'pointer', height: '64px', alignItems: 'center', overflow: 'hidden'}}>
                <img src={logo} alt="logo" style={{height: 100, objectFit: 'contain'}} />
            </Box>
            <Search sx={{ flex: 1, maxWidth: {xs:'100%', sm:400}}}>
                <SearchIconWrapper sx={{cursor:'pointer'}}>
                  <SearchIcon />
                </SearchIconWrapper>
             <StyledInputBase
             placeholder='Buscar...'
             value={search}
             onChange={(e)=> setSearch(e.target.value)}
             onKeyDown={handleKeySearch}
             />
            </Search>
            {user?.result && (
                <Icons sx={{display: 'flex', gap: {xs: 1, sm: 2}, alignItems:'center'}}>
                    <IconButton color='inherit' onClick={()=> navigate('/Chat')} size={isMobile ? 'small': 'medium'}>
                        <Badge badgeContent={unReadedMessage} color='error'>
                            <Chat fontSize={isMobile ? 'small': 'medium'} />
                        </Badge>
                    </IconButton>
                    <IconButton color='inherit' onClick={()=> navigate('/Notification')} size={isMobile ? 'small': 'medium'}>
                        <Badge badgeContent={UnReadedNotificationNumbers} color='error'>
                            <Notifications fontSize={isMobile ? 'small': 'medium'} />
                        </Badge>
                    </IconButton>
                    <Avatar 
                     alt={user.result.name}
                     src={user.result.imageUrl || 'https://cdn:pixabay.com/photo/2015/10/05/22/37/blank-picture-973460__340.png'}
                     onClick={hanldeClick}
                     sx={{ width: {xs:30, sm:40}, height: {xs:30, sm: 40}, cursor:'pointer'}} 
                     />
                </Icons>
            )}

            {!user?.result && (
                <Button color="inherit" startIcon={<Login />} onClick={() => navigate('/auth')}>
                     Iniciar sesión
                </Button>
            )}

            {user?.result && (
                <Menu anchorEl={anchorEl} open={open} onClose={handleClose}>
                     <MenuItem onClick={()=> {handleClose(); navigate(`/Profile/${user.result.id}`)}}>
                     Perfil
                    </MenuItem>
                     <MenuItem onClick={() => logout()}>Cerrar sesión</MenuItem>
                </Menu>
            )}
            </Stack>
        </Toolbar>
    </AppBar>
 )
}

export default NavBar;
