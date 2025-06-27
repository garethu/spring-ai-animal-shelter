package com.yootiful.animal_shelter_assistant;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;



@SpringBootApplication
public class AnimalShelterAssistantApplication {

	public static void main(String[] args) {
		SpringApplication.run(AnimalShelterAssistantApplication.class, args);
	}

	@Controller
	@ResponseBody
	class AnimalShelterAssistantController {

		private final ChatClient ai;
		// each user has it's own memory with concurrent hashmap
		private final Map<String, PromptChatMemoryAdvisor> memory = new ConcurrentHashMap<>();

        AnimalShelterAssistantController(ChatClient.Builder ai) {
            this.ai = ai.build();
        }

        @GetMapping("/{user}/assistant")
		String assistant (@PathVariable String user, @RequestParam String question) {

			// add an advisor to allow the assistant to have memory, this acts as a filter to pre-process the request
			var advisor = PromptChatMemoryAdvisor
					.builder(
							MessageWindowChatMemory.builder().chatMemoryRepository(new InMemoryChatMemoryRepository()).build()
					)
					.build();
			var advisorForUser = this.memory.computeIfAbsent(user,  u -> advisor);
			return this.ai
					.prompt()
					.user(question)
					.advisors(advisorForUser)
					.call()
					.content();
		}
	}

}
