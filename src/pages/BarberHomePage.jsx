import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom'; // Corrigido import
import api from '../services/api';

function BarberHomePage() {
  const [barber, setBarber] = useState(null);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    // 1. Recupera o ID do barbeiro que salvamos no login (authService.js)
    const userId = localStorage.getItem('userId');

    if (!userId) {
        alert("Erro: Usuário não identificado. Faça login novamente.");
        navigate('/login');
        return;
    }

    // 2. Usa a rota existente passando o ID correto
    // De: api.get('/barbers/me')  <-- O ERRO ESTAVA AQUI
    // Para:
    api.get(`/barbers/${userId}`) 
      .then(response => {
        setBarber(response.data);
        setLoading(false);
      })
      .catch(error => {
        console.error("Erro ao carregar perfil", error);
        setLoading(false);
        // Se der erro 403/401, pode redirecionar para login
        if (error.response && (error.response.status === 403 || error.response.status === 401)) {
            navigate('/login');
        }
      });
  }, [navigate]);

  const handleCreateShop = () => {
      alert("Em breve: Página de Criar Barbearia");
  };

  const handleJoinShop = () => {
      const cnpj = prompt("Digite o CNPJ da barbearia:");
      if(cnpj) {
          api.post('/barbershops/join-request', { cnpj })
             .then(() => alert("Pedido enviado! Aguarde o dono aceitar."))
             .catch(() => alert("Erro. Verifique o CNPJ."));
      }
  };

  if (loading) return <div style={{color:'white', textAlign:'center', padding:'50px'}}>Carregando...</div>;

  return (
    <div style={{ backgroundColor: '#1E1E1E', minHeight: '100vh', color: 'white', padding: '20px' }}>
        <div style={{ maxWidth: '900px', margin: '0 auto' }}>
            
            <header style={{ textAlign: 'center', marginBottom: '40px', borderBottom: '1px solid #333', paddingBottom: '20px' }}>
                <h1>Painel do Profissional</h1>
                <p>Bem-vindo, {barber?.name}</p>
            </header>

            {/* Se não tiver barbearia vinculada, mostra opções */}
            {!barber?.barbershopId ? (
                <div style={{ textAlign: 'center', padding: '40px', background: '#2A2A2A', borderRadius: '10px' }}>
                    <h2 style={{ color: '#D4AF37' }}>Você ainda não faz parte de uma Barbearia</h2>
                    <p style={{ marginBottom: '30px', color: '#ccc' }}>Escolha como deseja começar:</p>
                    
                    <div style={{ display: 'flex', gap: '20px', justifyContent: 'center', flexWrap: 'wrap' }}>
                        <button 
                            onClick={handleCreateShop}
                            style={{ padding: '15px 30px', fontSize: '1rem', cursor: 'pointer', backgroundColor: '#D4AF37', border: 'none', borderRadius: '5px', fontWeight: 'bold' }}
                        >
                            🏢 Criar Minha Barbearia
                        </button>
                        <button 
                            onClick={handleJoinShop}
                            style={{ padding: '15px 30px', fontSize: '1rem', cursor: 'pointer', backgroundColor: 'transparent', border: '2px solid #D4AF37', color: '#D4AF37', borderRadius: '5px', fontWeight: 'bold' }}
                        >
                            🤝 Entrar em Barbearia Existente
                        </button>
                    </div>
                </div>
            ) : (
                /* Painel Principal do Barbeiro com Loja */
                <div>
                    <h2>Sua Agenda</h2>
                    <p>Loja Atual: {barber.barbershopName || "Minha Barbearia"}</p>
                    {/* Componente de Agenda virá aqui */}
                </div>
            )}

        </div>
    </div>
  );
}

export default BarberHomePage;