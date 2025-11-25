import { Routes, Route } from 'react-router-dom'
import RedirectionPage from "./pages/RedirectionPage";
import StartPage from "./pages/StartPage"
import LoginPage from "./pages/LoginPage";
import SignInPage from "./pages/SignInPage";
import HomePage from "./pages/HomePage";
import Agendamento from "./pages/Agendamento";
import BarberHomePage from './pages/BarberHomePage';
import AgendamentoPage from './pages/AgendamentoPage';


function AppRoutes() {
  return (
    <Routes>
      <Route path="/" element={<StartPage />} />
      <Route path="/identificacao" element={<RedirectionPage/>} />
      <Route path="/login" element= {<LoginPage/>}/>
      <Route path="/SignIn" element={<SignInPage/>}/>
      <Route path="/homepage" element={<HomePage/>}/>
      <Route path="/agendamento" element={<Agendamento/>}/>
      <Route path='/agendamentoPage' element={<AgendamentoPage/>}/>
      <Route path='/barberHome' element={<BarberHomePage/>}/>
    </Routes>
  )
}

export default AppRoutes