import './App.css'
import AppRoutes from './AppRoutes';
import { BrowserRouter as Router } from 'react-router-dom'; // O Link não é mais necessário aqui

function App() {
  return (
    // Agora o App.js apenas define o contexto de roteamento e renderiza as rotas
    <Router>
      <AppRoutes/>
    </Router>
  )
}

export default App
