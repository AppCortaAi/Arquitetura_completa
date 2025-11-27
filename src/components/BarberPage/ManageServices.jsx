import React, { useEffect, useState } from 'react';
import { getMyServices, createService, deleteService } from '../../services/barbershopService';

const ManageServices = () => {
    const [services, setServices] = useState([]);
    const [loading, setLoading] = useState(true);
    
    // Estados do Formulário
    const [name, setName] = useState("");
    const [price, setPrice] = useState("");
    const [duration, setDuration] = useState("30");

    const loadServices = async () => {
        const data = await getMyServices();
        setServices(data);
        setLoading(false);
    };

    useEffect(() => {
        loadServices();
    }, []);

    const handleAdd = async (e) => {
        e.preventDefault();
        if (!name || !price || !duration) return;

        try {
            await createService({
                activityName: name,
                price: parseFloat(price),
                durationMinutes: parseInt(duration)
            });
            
            alert("Serviço adicionado!");
            setName("");
            setPrice("");
            loadServices(); // Recarrega a lista
        } catch (error) {
            alert("Erro ao criar serviço.");
        }
    };

    const handleDelete = async (id) => {
        if (window.confirm("Tem certeza que deseja excluir este serviço?")) {
            try {
                await deleteService(id);
                loadServices();
            } catch (error) {
                alert("Erro ao excluir.");
            }
        }
    };

    return (
        <div style={{ backgroundColor: '#2A2A2A', padding: '20px', borderRadius: '10px', marginTop: '20px' }}>
            <h2 style={{ color: '#D4AF37', borderBottom: '1px solid #444', paddingBottom: '10px' }}>Gerenciar Serviços</h2>

            {/* Lista de Serviços Existentes */}
            <div style={{ marginBottom: '30px' }}>
                {loading ? <p style={{color:'white'}}>Carregando...</p> : (
                    services.length > 0 ? (
                        <ul style={{ listStyle: 'none', padding: 0 }}>
                            {services.map(s => (
                                <li key={s.id} style={{ 
                                    display: 'flex', justifyContent: 'space-between', alignItems: 'center',
                                    borderBottom: '1px solid #333', padding: '10px 0', color: 'white'
                                }}>
                                    <div>
                                        <strong>{s.activityName}</strong>
                                        <span style={{ color: '#aaa', marginLeft: '10px', fontSize: '0.9rem' }}>
                                            ({s.durationMinutes} min)
                                        </span>
                                    </div>
                                    <div>
                                        <span style={{ marginRight: '15px', fontWeight: 'bold' }}>R$ {s.price.toFixed(2)}</span>
                                        <button 
                                            onClick={() => handleDelete(s.id)}
                                            style={{ background: '#F44336', border: 'none', color: 'white', borderRadius: '4px', padding: '5px 10px', cursor: 'pointer' }}
                                        >
                                            Excluir
                                        </button>
                                    </div>
                                </li>
                            ))}
                        </ul>
                    ) : (
                        <p style={{ color: '#aaa' }}>Nenhum serviço cadastrado. Adicione o primeiro abaixo!</p>
                    )
                )}
            </div>

            {/* Formulário de Adicionar */}
            <form onSubmit={handleAdd} style={{ display: 'flex', gap: '10px', flexWrap: 'wrap', alignItems: 'flex-end' }}>
                <div style={{ flex: 2 }}>
                    <label style={{ color: 'white', fontSize: '0.8rem' }}>Nome do Serviço</label>
                    <input 
                        type="text" 
                        placeholder="Ex: Corte Degradê"
                        value={name}
                        onChange={e => setName(e.target.value)}
                        style={{ width: '90%', padding: '8px', borderRadius: '4px', border: 'none' }}
                        required
                    />
                </div>
                
                <div style={{ flex: 1 }}>
                    <label style={{ color: 'white', fontSize: '0.8rem' }}>Preço (R$)</label>
                    <input 
                        type="number" 
                        placeholder="0.00"
                        step="0.01"
                        value={price}
                        onChange={e => setPrice(e.target.value)}
                        style={{ width: '90%', padding: '8px', borderRadius: '4px', border: 'none' }}
                        required
                    />
                </div>

                <div style={{ flex: 1 }}>
                    <label style={{ color: 'white', fontSize: '0.8rem' }}>Duração (min)</label>
                    <input 
                        type="number" 
                        placeholder="30"
                        value={duration}
                        onChange={e => setDuration(e.target.value)}
                        style={{ width: '90%', padding: '8px', borderRadius: '4px', border: 'none' }}
                        required
                    />
                </div>

                <button 
                    type="submit"
                    style={{ padding: '10px 20px', background: '#D4AF37', border: 'none', borderRadius: '4px', fontWeight: 'bold', cursor: 'pointer', height: '36px' }}
                >
                    + Adicionar
                </button>
            </form>
        </div>
    );
};

export default ManageServices;