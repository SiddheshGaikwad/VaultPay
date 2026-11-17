import api from "../api";

export const getTransactionsByAccountId = async (accountId) => {
    const response = await api.get(
        `/api/transactions/account/${accountId}`
    );

    return response.data;
};