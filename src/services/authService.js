import api from './api';

// Login Genérico (aceita 'customer' ou 'barber')
export const loginUser = async (email, password, userType = 'customer') => {
    let url = '';
    
    // 1. Define a URL correta baseada no Back-end (CustomerController ou BarberController)
    if (userType === 'barber') {
        url = '/barbers/login'; 
    } else {
        url = '/customers/login'; 
    }

    const response = await api.post(url, { email, password });
    
    // 2. Salva os dados se o login der certo
    if (response.data.token) {
        localStorage.setItem('token', response.data.token);
        
        // 3. CORREÇÃO CRÍTICA: O Back-end não manda a 'role' explícita no DTO.
        // Precisamos definir manualmente para o appointmentService funcionar.
        const roleName = userType === 'barber' ? 'ROLE_BARBER' : 'ROLE_CUSTOMER';
        localStorage.setItem('role', roleName); 

        // O userData vem dentro de response.data
        if (response.data.userData) {
             localStorage.setItem('userName', response.data.userData.name);
             localStorage.setItem('userId', response.data.userData.id);
             // Salva o objeto completo caso precise depois
             localStorage.setItem('user', JSON.stringify(response.data.userData));
        }
    }
    
    // Retorna o objeto combinado para a tela usar
    return { ...response.data, role: localStorage.getItem('role') };
};

// Função de Cadastro (Cliente)
export const registerCustomer = async (userData) => {
    // 4. CORREÇÃO DE ROTA: O Controller espera /register no final
    const response = await api.post('/customers/register', userData);
    return response.data;
};

// Função de Logout
export const logoutUser = () => {
    localStorage.clear(); // Limpa tudo de uma vez
    window.location.href = '/login';
};

export const registerBarber = async (barberData) => {
    // Para enviar foto + dados, usamos FormData em vez de JSON
    const formData = new FormData();
    
    // O Back-end espera um objeto JSON stringificado no campo 'barber'
    // E o arquivo opcional no campo 'file'
    // Mas no seu BarberController, ele usa @RequestPart("barber") 
    
    // Como estamos enviando via axios sem arquivo por enquanto, 
    // podemos tentar enviar o JSON direto, MAS precisamos garantir
    // que o Content-Type seja application/json se for só dados.
    
    // PORÉM, se o Controller exige @RequestPart, a melhor forma é enviar FormData:
    
    // Monta o objeto com os dados
    const barberJson = JSON.stringify({
        name: barberData.name,
        tell: barberData.tell,
        email: barberData.email,
        documentCPF: barberData.documentCPF,
        password: barberData.password,
        workStartTime: barberData.workStartTime, // "09:00"
        workEndTime: barberData.workEndTime      // "18:00"
    });

    // Adiciona ao FormData. 
    // IMPORTANTE: O Content-Type desta parte deve ser application/json
    const jsonBlob = new Blob([barberJson], { type: 'application/json' });
    formData.append('barber', jsonBlob);

    // Se tiver foto (futuro): formData.append('file', file);

    const response = await api.post('/barbers/register', formData, {
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    });

    return response.data;
};