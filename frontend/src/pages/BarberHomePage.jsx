import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';
import { logoutUser } from '../services/authService'; // 1. Importar a função de logout
import ManageServices from '../components/BarberPage/ManageServices';
import ManageMySkills from '../components/BarberPage/ManageMySkills';

function BarberHomePage() {
  const [barber, setBarber] = useState(null);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    const userId = localStorage.getItem('userId');
    if(!userId) { navigate('/identificacao'); return; }

    api.get(`/barbers/${userId}`)
      .then(response => {
        setBarber(response.data);
        setLoading(false);
      })
      .catch(err => {
          console.error(err);
          setLoading(false);
          if(err.response?.status === 403) navigate('/login');
      });
  }, [navigate]);

  const handleCreateShop = () => navigate('/create-barbershop');
  
  const handleJoinShop = () => {
      const cnpj = prompt("Digite o CNPJ da barbearia:");
      if(cnpj) {
          api.post('/barbershops/join-request', { cnpj })
             .then(() => alert("Pedido enviado! Aguarde o dono aceitar."))
             .catch(() => alert("Erro. Verifique o CNPJ."));
      }
  };

  // 2. Função simples para chamar o logout
  const handleLogout = () => {
      if(window.confirm("Tem certeza que deseja sair?")) {
          logoutUser();
      }
  };

  if (loading) return <div style={{color:'white', textAlign:'center', padding:'50px'}}>Carregando...</div>;

  return (
    <div style={{ backgroundColor: '#1E1E1E', minHeight: '100vh', color: 'white', padding: '20px' }}>
        <div style={{ maxWidth: '900px', margin: '0 auto' }}>
            
            {/* 3. Header Flexível para alinhar o botão à direita */}
            <header style={{ 
                display: 'flex', 
                justifyContent: 'space-between', 
                alignItems: 'flex-start', // Alinha ao topo
                marginBottom: '30px', 
                borderBottom: '1px solid #333', 
                paddingBottom: '20px' 
            }}>
                {/* Div vazia para equilibrar (opcional) ou apenas título */}
                <div style={{ flex: 1 }}></div> 

                <div style={{ textAlign: 'center', flex: 2 }}>
                    <h1 style={{ margin: 0 }}>Painel do Profissional</h1>
                    <p style={{ margin: '10px 0' }}>Bem-vindo, {barber?.name}</p>
                    {barber?.barbershopName && <span style={{color:'#D4AF37', fontWeight:'bold'}}>@{barber.barbershopName}</span>}
                </div>

                {/* Botão de Sair no canto direito */}
                <div style={{ flex: 1, textAlign: 'right' }}>
                    <button 
                        onClick={handleLogout}
                        style={{ 
                            background: 'transparent', 
                            border: '1px solid #F44336', 
                            color: '#F44336', 
                            padding: '8px 15px', 
                            borderRadius: '5px', 
                            cursor: 'pointer',
                            fontSize: '0.9rem'
                        }}
                    >
                        Sair ➜
                    </button>
                </div>
            </header>

            {!barber?.barbershopId ? (
                /* TELA SEM LOJA */
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
                            🤝 Entrar em Barbearia
                        </button>
                    </div>
                </div>
            ) : (
                /* TELA COM LOJA */
                <div>
                    <button 
                        onClick={() => navigate('/meus-agendamentos')}
                        style={{ width:'100%', padding: '15px', backgroundColor: '#D4AF37', border: 'none', borderRadius: '5px', cursor: 'pointer', fontWeight: 'bold', fontSize: '1.1rem', marginBottom: '20px' }}
                    >
                        Ver Minha Agenda
                    </button>

                    <ManageMySkills shopId={barber.barbershopId} />

                    {/* Painel de Serviços (Só Dono) */}
                    {barber.owner && (
                        <ManageServices />
                    )}
                </div>
            )}

        </div>
    </div>
  );
}

export default BarberHomePage;