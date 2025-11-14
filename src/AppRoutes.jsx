import React from "react";
import { Routes, Route } from 'react-router-dom'
import RedirectionPage from "./pages/RedirectionPage";
import StartPage from "./pages/StartPage"
import LoginPage from "./pages/LoginPage";
import SignInPage from "./pages/SignInPage";
import HomePage from "./pages/HomePage";
import Agendamento from "./pages/Agendamento";


function AppRoutes() {
  return (
    <Routes>
      <Route path="/" element={<StartPage />} />
      <Route path="/redirection" element={<RedirectionPage />} />
      <Route path="/login" element= {<LoginPage/>}/>
      <Route path="/SignIn" element={<SignInPage/>}/>
      <Route path="/homepage" element={<HomePage/>}/>
      <Route path="/agendamento" element={<Agendamento/>}/>
    </Routes>
  )
}

export default AppRoutes