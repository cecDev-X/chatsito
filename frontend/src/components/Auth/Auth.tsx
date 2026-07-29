// @ts-nocheck
import { useCallback, useEffect, useRef, useState } from "react";
import SendIcon from '@mui/icons-material/Send';
import { useDispatch, useSelector } from "react-redux";
import { useLocation, useNavigate } from "react-router-dom";
import { googleSignIn, signin, signup } from '../../store/actions/auth';
import { RootState } from '../../store/reducers';

import { Box, Button, Card, Stack, TextField, Typography } from '@mui/material';

import { AuthCard, AuthMainBox, authSx } from '../MainStyles';

const initialSignInState = {
  email: "",
  password: ""
};

const initialSignUpState = {
  firstName: "",
  lastName: "",
  email: "",
  password: ""
};

const getEmailError = (email) => {
  if (!email.trim()) return "El correo es obligatorio.";
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/.test(email)) {
    return "Ingresa un correo válido.";
  }
  return "";
};

const getNameError = (name, label) => {
  if (!name.trim()) return `El ${label} es obligatorio.`;
  if (!/^\p{L}+$/u.test(name)) return `El ${label} solo puede contener letras.`;
  return "";
};

const getPasswordError = (password, isSignUp) => {
  if (!password) return "La contraseña es obligatoria.";
  if (password.length < 8) return "Debe tener al menos 8 caracteres.";
  if (!/[A-Z]/.test(password)) return "Debe incluir al menos una mayúscula.";
  if (!/[a-z]/.test(password)) return "Debe incluir al menos una minúscula.";
  if (isSignUp && !/[^A-Za-z0-9]/.test(password)) {
    return "Debe incluir al menos un símbolo.";
  }
  return "";
};

const Auth: React.FC = () => {
  const [signInForm, setSignInForm] = useState(initialSignInState);
  const [signUpForm, setSignUpForm] = useState(initialSignUpState);
  const [signInErrors, setSignInErrors] = useState({});
  const [signUpErrors, setSignUpErrors] = useState({});
  const [submittedType, setSubmittedType] = useState("");
  const [googleError, setGoogleError] = useState("");
  const googleButtonRef = useRef(null);
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const location = useLocation();
  const { errors } = useSelector((state: RootState) => state.auth);

  const handleGoogleCredential = useCallback((response) => {
    if (!response?.credential) {
      setGoogleError("No se pudo obtener la cuenta de Google.");
      return;
    }
    setGoogleError("");
    dispatch(googleSignIn(response.credential, navigate))
      .catch((error) => {
        setGoogleError(error.response?.data?.message || "No se pudo iniciar sesión con Google.");
      });
  }, [dispatch, navigate]);

  useEffect(() => {
    const clientId = process.env.REACT_APP_GOOGLE_CLIENT_ID;
    if (!clientId || !googleButtonRef.current) return;

    const renderGoogleButton = () => {
      if (!window.google || !googleButtonRef.current) return;
      window.google.accounts.id.initialize({
        client_id: clientId,
        callback: handleGoogleCredential
      });
      googleButtonRef.current.innerHTML = "";
      window.google.accounts.id.renderButton(googleButtonRef.current, {
        theme: "outline",
        size: "large",
        text: "continue_with",
        width: 240
      });
    };

    const googleScriptSrc = "https://accounts.google.com/gsi/client?hl=es";
    const existingScript = document.querySelector(`script[src="${googleScriptSrc}"]`);
    if (existingScript) {
      renderGoogleButton();
      return;
    }

    const script = document.createElement("script");
    script.src = googleScriptSrc;
    script.async = true;
    script.defer = true;
    script.onload = renderGoogleButton;
    document.head.appendChild(script);
  }, [handleGoogleCredential]);

  const validateForm = (type, form) => {
    const isSignUp = type === 'siginup';
    const nextErrors = {};

    if (isSignUp) {
      const firstNameError = getNameError(form.firstName, "nombre");
      const lastNameError = getNameError(form.lastName, "apellido");
      if (firstNameError) nextErrors.firstName = firstNameError;
      if (lastNameError) nextErrors.lastName = lastNameError;
    }

    const emailError = getEmailError(form.email);
    const passwordError = getPasswordError(form.password, isSignUp);
    if (emailError) nextErrors.email = emailError;
    if (passwordError) nextErrors.password = passwordError;

    return nextErrors;
  };

  const handleSubmit = (type, event) => {
    event?.preventDefault();
    setSubmittedType(type);

    const form = type === 'siginup' ? signUpForm : signInForm;
    const nextErrors = validateForm(type, form);

    if (type === 'siginup') {
      setSignUpErrors(nextErrors);
      if (Object.keys(nextErrors).length === 0) dispatch(signup(form, navigate));
    } else {
      setSignInErrors(nextErrors);
      if (Object.keys(nextErrors).length === 0) dispatch(signin(form, navigate));
    }
  };

  const handleChange = (type, event) => {
    const { name, value } = event.target;

    if (type === 'siginup') {
      setSignUpForm((previous) => ({ ...previous, [name]: value }));
      if (signUpErrors[name]) {
        const nextErrors = validateForm('siginup', { ...signUpForm, [name]: value });
        setSignUpErrors((previous) => ({ ...previous, [name]: nextErrors[name] || "" }));
      }
    } else {
      setSignInForm((previous) => ({ ...previous, [name]: value }));
      if (signInErrors[name]) {
        const nextErrors = validateForm('signin', { ...signInForm, [name]: value });
        setSignInErrors((previous) => ({ ...previous, [name]: nextErrors[name] || "" }));
      }
    }
  };

  const signInServerError = submittedType === 'signin' && errors ? errors : "";
  const signUpServerError = submittedType === 'siginup' && errors ? errors : "";

  return (
    <Stack direction="row" spacing={4} sx={{ marginTop: 2, justifyContent: "center" }}>
      <Box flex={2} />
      <Box flex={2} sx={{ marginTop: 2 }}>
        <AuthCard>
          <Typography variant="h4" component="div" sx={authSx.AuthType}>
            Iniciar sesión
          </Typography>
          <AuthMainBox
            component="form"
            noValidate
            autoComplete="off"
            onSubmit={(event) => handleSubmit('signin', event)}
          >
            <TextField
              id="outline-required1"
              label="Correo electrónico"
              type="email"
              name="email"
              value={signInForm.email}
              onChange={(event) => handleChange('signin', event)}
              error={Boolean(signInErrors.email)}
              helperText={signInErrors.email}
            />
            <TextField
              id="outline-password-input1"
              label="Contraseña"
              type="password"
              name="password"
              value={signInForm.password}
              autoComplete="current-password"
              onChange={(event) => handleChange('signin', event)}
              error={Boolean(signInErrors.password || signInServerError)}
              helperText={signInErrors.password || signInServerError || (location.state?.sessionExpired ? "Tu sesión expiró. Inicia sesión nuevamente." : "")}
            />
            <Box sx={{ m: 2 }}>
              <Button size="large" variant="outlined" type="submit" endIcon={<SendIcon />}>
                Iniciar sesión
              </Button>
            </Box>
            <Box sx={{ mt: 1, mb: 2, minHeight: 40, textAlign: "center" }}>
              {process.env.REACT_APP_GOOGLE_CLIENT_ID ? (
                <div ref={googleButtonRef} />
              ) : (
                <Button
                  size="small"
                  variant="outlined"
                  onClick={() => setGoogleError("Configura REACT_APP_GOOGLE_CLIENT_ID para usar Google.")}
                >
                  Continuar con Google
                </Button>
              )}
              {googleError && (
                <Typography variant="caption" color="error">
                  {googleError}
                </Typography>
              )}
            </Box>
          </AuthMainBox>
        </AuthCard>
      </Box>
      <Box flex={6} sx={{ marginTop: 2 }}>
        <Card>
            <Typography variant="h4" component="div" sx={authSx.AuthType}>
            Registrarse
          </Typography>
          <AuthMainBox
            component="form"
            noValidate
            autoComplete="off"
            onSubmit={(event) => handleSubmit('siginup', event)}
          >
            <TextField
              id="outline-required3"
              label="Nombre"
              type="text"
              name="firstName"
              value={signUpForm.firstName}
              onChange={(event) => handleChange('siginup', event)}
              error={Boolean(signUpErrors.firstName)}
              helperText={signUpErrors.firstName}
            />
            <TextField
              id="outline-required4"
              label="Apellido"
              type="text"
              name="lastName"
              value={signUpForm.lastName}
              onChange={(event) => handleChange('siginup', event)}
              error={Boolean(signUpErrors.lastName)}
              helperText={signUpErrors.lastName}
            />
            <TextField
              id="outline-required5"
              label="Correo electrónico"
              type="email"
              name="email"
              value={signUpForm.email}
              onChange={(event) => handleChange('siginup', event)}
              error={Boolean(signUpErrors.email)}
              helperText={signUpErrors.email}
            />
            <TextField
              id="outline-password-input6"
              label="Contraseña"
              type="password"
              name="password"
              value={signUpForm.password}
              autoComplete="new-password"
              onChange={(event) => handleChange('siginup', event)}
              error={Boolean(signUpErrors.password || signUpServerError)}
              helperText={signUpErrors.password || signUpServerError || ""}
            />
            <Box sx={{ m: 1 }}>
              <Button size="large" variant="outlined" type="submit" endIcon={<SendIcon />}>
                Registrarse
              </Button>
            </Box>
          </AuthMainBox>
        </Card>
      </Box>
    </Stack>
  );
};

export default Auth;
