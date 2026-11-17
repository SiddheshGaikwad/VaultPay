package com.vaultpay.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class AiAssistantService {

	private final ChatClient chatClient;

	public AiAssistantService(ChatClient.Builder chatClientBuilder) {
		this.chatClient = chatClientBuilder.build();
	}

	public String askQuestion(String question) {
		return chatClient
				.prompt().system("You are VaultPay's banking assistant. "
						+ "Answer banking questions in simple English. " + "Never claim to perform transactions.")
				.user(question).call().content();
	}
}