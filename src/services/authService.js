import api from './api';


export const loginUser = async (email, password) => {
    const response = await api.post('/auth/login', { email, password });
    
    // Salva os dados se o login der certo
    if (response.data.token) {
        localStorage.setItem('token', response.data.token);
        localStorage.setItem('role', response.data.role); 
        localStorage.setItem('userName', response.data.name);
    }
    return response.data;
};

// Função de Cadastro (Cliente)
export const registerCustomer = async (userData) => {
    // userData deve ser um objeto igual ao CustomerDTO do Java
    const response = await api.post('/customers', userData);
    return response.data;
};

// Função de Logout
export const logoutUser = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('role');
    localStorage.removeItem('userName');
    localStorage.removeItem('userId');
};