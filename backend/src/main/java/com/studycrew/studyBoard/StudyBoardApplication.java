package com.studycrew.studyBoard;

import org.springframework.ai.model.google.genai.autoconfigure.embedding.GoogleGenAiEmbeddingConnectionAutoConfiguration;
import org.springframework.ai.model.google.genai.autoconfigure.embedding.GoogleGenAiTextEmbeddingAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

// Gemini Embedding은 Vector 검색을 켠 환경에서만 VectorSearchConfig가 직접 생성
@SpringBootApplication(exclude = {
		GoogleGenAiEmbeddingConnectionAutoConfiguration.class,
		GoogleGenAiTextEmbeddingAutoConfiguration.class
})
@EnableJpaAuditing
public class StudyBoardApplication {

	public static void main(String[] args) {
		SpringApplication.run(StudyBoardApplication.class, args);
	}

}
