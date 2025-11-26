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

export const createBarbershop = async (shopData, imageFile) => {
    const formData = new FormData();

    // 1. LIMPEZA: Remove pontos, barras e traços, deixando apenas números
    const cleanCnpj = shopData.cnpj.replace(/\D/g, ''); 

    // 2. Monta o objeto JSON com o CNPJ limpo
    const shopObject = {
        name: shopData.name,
        cnpj: cleanCnpj, // Agora envia "48719131000150" (14 dígitos)
        address: shopData.address
    };

    // 3. Cria o Blob JSON
    const shopJsonString = JSON.stringify(shopObject);
    const jsonBlob = new Blob([shopJsonString], {
        type: 'application/json'
    });

    // 4. Adiciona ao FormData
    formData.append('shop', jsonBlob);

    // 5. Adiciona imagem se existir
    if (imageFile) {
        formData.append('file', imageFile);
    }

    const response = await api.post('/barbershops/register-my-shop', formData);
    
    return response.data;
};