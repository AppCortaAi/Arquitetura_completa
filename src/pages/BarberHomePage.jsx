import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';
// Você pode criar um CSS específico depois, por enquanto usaremos estilo inline ou reutilizaremos
import Styles from './CSS/HomePage.module.css'; 

function BarberHomePage() {
    const navigate = useNavigate();
    const [barberInfo, setBarberInfo] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        // Busca os dados do barbeiro logado para saber se ele tem loja
        const fetchMe = async () => {
            try {
                const response = await api.get('/barbers/me');
                setBarberInfo(response.data); // O DTO tem o campo 'barbershopId' ou similar
            } catch (error) {
                console.error("Erro ao carregar perfil", error);
            } finally {
                setLoading(false);
            }
        };
        fetchMe();
    }, []);

    const handleCreateShop = () => {
        // Aqui você levaria para um formulário de "Criar Barbearia"
        // Por enquanto, vamos usar um prompt só para testar a API
        alert("Funcionalidade de Criar Loja será implementada na próxima etapa!");
    };

    const handleJoinShop = () => {
        const cnpj = prompt("Digite o CNPJ da barbearia que deseja entrar:");
        if (cnpj) {
            api.post('/barbershops/join-request', { cnpj })
               .then(() => alert("Solicitação enviada! Aguarde o dono aceitar."))
               .catch(err => alert("Erro ao solicitar entrada. Verifique o CNPJ."));
        }
    };

    if (loading) return <div style={{color:'white', textAlign:'center', marginTop:'50px'}}>Carregando...</div>;

    return (
        <div style={{ backgroundColor: '#1E1E1E', minHeight: '100vh', color: 'white', padding: '20px' }}>
            <h1 style={{ textAlign: 'center' }}>Painel do Barbeiro</h1>
            
            <div style={{ maxWidth: '800px', margin: '0 auto', textAlign: 'center' }}>
                <h2>Bem-vindo, {barberInfo?.name}!</h2>
                
                {/* LÓGICA DO FLUXO: Se não tem barbearia vinculada, mostra opções */}
                {!barberInfo?.barbershopId ? (
                    <div style={{ marginTop: '50px', padding: '30px', border: '1px solid #D4AF37', borderRadius: '10px' }}>
                        <h3>Você ainda não está vinculado a uma Barbearia.</h3>
                        <p>O que deseja fazer?</p>
                        
                        <div style={{ display: 'flex', gap: '20px', justifyContent: 'center', marginTop: '30px' }}>
                            <button 
                                onClick={handleCreateShop}
                                style={{ padding: '15px 30px', background: '#D4AF37', border: 'none', borderRadius: '5px', cursor: 'pointer', fontWeight: 'bold' }}
                            >
                                Criar Minha Barbearia (Sou Dono)
                            </button>

                            <button 
                                onClick={handleJoinShop}
                                style={{ padding: '15px 30px', background: '#333', color: 'white', border: '1px solid white', borderRadius: '5px', cursor: 'pointer' }}
                            >
                                Entrar em uma Barbearia (Sou Funcionário)
                            </button>
                        </div>
                    </div>
                ) : (
                    /* Se JÁ TEM barbearia, mostra a agenda */
                    <div style={{ marginTop: '30px' }}>
                        <h3>Sua Agenda de Hoje</h3>
                        <p>Aqui entrará o componente de Meus Agendamentos (Barbeiro)</p>
                        {/* <MeusAgendamentos /> */}
                    </div>
                )}
            </div>
        </div>
    );
}

export default BarberHomePage;