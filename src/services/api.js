import axios from "axios";

const api = axios.create({
  baseURL: "http://localhost:8080/api", // O endereço do seu Back-end Spring Boot
});

// Interceptador: Antes de cada requisição, insere o token se ele existir
api.interceptors.request.use((config) => {
  const token = localStorage.getItem("token"); // Vamos salvar o token aqui no login
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export default api;