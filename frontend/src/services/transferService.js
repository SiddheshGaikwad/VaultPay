import api from "../api";

export const transferMoney = async (transferData) => {
    const response = await api.post("/api/transfers", transferData);
    return response.data;
};