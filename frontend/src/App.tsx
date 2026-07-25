import React, {useMemo, useState} from 'react';
import './App.css';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import {
  Box,
  createTheme,
  ThemeProvider,
  PaletteMode,
  CssBaseline,
  Container,
} from "@mui/material"

import Home from './components/Home/Home';
import Auth from './components/Auth/Auth';

const App: React.FC = () => {

  const [mode, setMode] = useState<PaletteMode>('light');

  const theme = useMemo(() => createTheme({
    palette: {
      mode,
      primary: { main: '#1976d2' },
      secondary: { main: '#dc004e' },
      background: {
        default: mode === 'light' ? '#f4f7f6' : '#121212',
        paper: mode === 'light' ? '#fff' : '#1e1e1e'
      },
    },
    shape: { borderRadius: 8 },
    components: {
      MuiButton: { styleOverrides: { root: { textTransform: 'none', borderRadius: 8 } } },
      MuiPaper: { styleOverrides: { root: { backgroundImage: 'none' } } },
    },
  }),
    [mode]);
    
return (
  <ThemeProvider theme={theme}>
    <CssBaseline />
    <BrowserRouter>
      <Box sx={{ minHeight: '100VH', display: 'flex', flexDirection: 'column', bgcolor: 'background.default', color: 'text.primary' }}>
        {/*Navbar*/}
        <Container maxWidth="xl" sx={{ flex: 1, py: 2 }}>
          <Routes>
            {/*Force redirect form root to home*/}
            <Route path="/" element={<Navigate replace to="/Home" />} />
            <Route path="/Home" element={<Home setMode={setMode} mode={mode}/>} />
          
            {/*Auth*/}
            <Route path="/auth" element={<Auth />} />
          
          </Routes>
        </Container>

      </Box>
    </BrowserRouter>
  </ThemeProvider>
)
}
export default App;