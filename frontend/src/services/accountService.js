
import api from "../api";

export const getAllAccounts = async () => {
  const response = await api.get("/api/accounts");
  return response.data;
};

export const createAccount = async (userId) => {
  const response = await api.post(`/api/accounts/${userId}`);
  return response.data;
};

export const getAccountsByUserId = async (userId) => {
  const response = await api.get(`/api/accounts/user/${userId}`);
  return response.data;
};
