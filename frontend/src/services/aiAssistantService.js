
import api from "../api";

export const askAiAssistant = async (question) => {
    const response = await api.post("/api/ai/ask", {
        question,
    });

    return response.data;
};
