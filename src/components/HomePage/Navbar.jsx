// src/components/HomePage/Navbar.jsx
import React from "react";
import { useNavigate } from "react-router-dom";
import Styles from "./CSS/Navbar.module.css";
import { logoutUser } from "../../services/authService"; // Importação da função de logout

function Navbar() {
  const navigate = useNavigate();

  const handleLogout = () => {
    if (window.confirm("Tem certeza que deseja sair?")) {
      logoutUser(); // Limpa o token
      navigate("/identificacao"); // Redireciona para o login imediatamente
    }
  };

  return (
    <div className={Styles.navbar_container}>
      <div className={Styles.navbar_content}>
        <h3>CortaAI</h3>
        
        {/* Botão "Sair" estilizado */}
        <button 
            className={Styles.logout_button} 
            onClick={handleLogout}
        >
            Sair ➜
        </button>
      </div>
    </div>
  );
}

export default Navbar;