
package com.vaultpay.controller;

import com.vaultpay.service.AiAssistantService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiAssistantController {

	private final AiAssistantService aiAssistantService;

	public AiAssistantController(AiAssistantService aiAssistantService) {
		this.aiAssistantService = aiAssistantService;
	}

	@PostMapping("/ask")
	public Map<String, String> askQuestion(@RequestBody Map<String, String> request) {

		String question = request.get("question");

		if (question == null || question.isBlank()) {
			throw new IllegalArgumentException("Question cannot be empty");
		}

		String answer = aiAssistantService.askQuestion(question);

		return Map.of("question", question, "answer", answer);
	}
}
