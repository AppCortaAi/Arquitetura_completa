// src/services/api.js
import axios from 'axios';

const api = axios.create({
    baseURL: '/api',
});

// Interceptador para adicionar o Token JWT em toda requisição
api.interceptors.request.use(async (config) => {
    const token = localStorage.getItem('token');
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

export default api;
