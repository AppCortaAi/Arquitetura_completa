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