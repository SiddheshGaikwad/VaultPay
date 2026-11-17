import api from "../api";

export const getReceiverAccounts = async () => {
    const response = await api.get("/api/accounts/receivers");
    return response.data;
};
