import api from "./api"

export const getAllBarbershops = async () => {
    try {
        const response = await api.get("/barbershops");
        return response.data;
    } catch (error) {
        console.log("Erro de API - API AllBarbershops");
        return[];
    };
}

export const getBarbershopById = async (id) => {
    try {
        const response = await api.get("/barbershops/")
    } catch (error) {
         console.log("Erro de API - API GetBarbershopById");
        return[];
    }
}

