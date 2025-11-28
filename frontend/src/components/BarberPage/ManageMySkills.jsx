import React, { useEffect, useState } from 'react';
import { getShopServices, getMyAssignedActivities, assignActivities } from '../../services/barbershopService';
import api from '../../services/api';

const ManageMySkills = ({ shopId }) => {
    const [shopServices, setShopServices] = useState([]); 
    const [myServicesIds, setMyServicesIds] = useState([]); 
    const [loading, setLoading] = useState(true);

    // Carrega dados
    useEffect(() => {
        const loadData = async () => {
            try {
               
                const [allServices, myActivities] = await Promise.all([
                    getShopServices(shopId),
                    getMyAssignedActivities()
                ]);

                setShopServices(allServices);
                
                setMyServicesIds(myActivities.map(a => a.id));
                setLoading(false);
            } catch (error) {
                console.error("Erro ao carregar habilidades:", error);
            }
        };
        
        if (shopId) loadData();
    }, [shopId]);

    const handleToggle = (serviceId) => {
        setMyServicesIds(prev => 
            prev.includes(serviceId)
                ? prev.filter(id => id !== serviceId) // Remove
                : [...prev, serviceId]                // Adiciona
        );
    };

    const handleSave = async () => {
        try {
            await assignActivities(myServicesIds);
            alert("Habilidades atualizadas com sucesso!");
        } catch (error) {
            alert("Erro ao salvar habilidades.");
        }
    };

    return (
        <div style={{ backgroundColor: '#2A2A2A', padding: '20px', borderRadius: '10px', marginTop: '20px' }}>
            <h2 style={{ color: '#D4AF37', borderBottom: '1px solid #444', paddingBottom: '10px' }}>Meus Serviços</h2>
            <p style={{ color: '#aaa', fontSize: '0.9rem' }}>Selecione quais serviços desta barbearia você realiza:</p>

            {loading ? <p style={{color:'white'}}>Carregando...</p> : (
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(200px, 1fr))', gap: '10px', margin: '20px 0' }}>
                    {shopServices.map(service => (
                        <div 
                            key={service.id}
                            onClick={() => handleToggle(service.id)}
                            style={{
                                padding: '10px',
                                borderRadius: '5px',
                                cursor: 'pointer',
                                border: myServicesIds.includes(service.id) ? '2px solid #D4AF37' : '1px solid #444',
                                backgroundColor: myServicesIds.includes(service.id) ? '#3A3A3A' : 'transparent',
                                color: 'white',
                                transition: 'all 0.2s'
                            }}
                        >
                            <div style={{ display:'flex', justifyContent:'space-between'}}>
                                <strong>{service.activityName}</strong>
                                {myServicesIds.includes(service.id) && <span style={{color:'#D4AF37'}}>✓</span>}
                            </div>
                            <span style={{ fontSize: '0.8rem', color: '#ccc' }}>R$ {service.price.toFixed(2)}</span>
                        </div>
                    ))}
                </div>
            )}

            <button 
                onClick={handleSave}
                style={{ width: '100%', padding: '12px', backgroundColor: '#D4AF37', border: 'none', borderRadius: '5px', fontWeight: 'bold', cursor: 'pointer' }}
            >
                Salvar Minhas Habilidades
            </button>
        </div>
    );
};

export default ManageMySkills;