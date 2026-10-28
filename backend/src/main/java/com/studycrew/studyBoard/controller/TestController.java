package com.studycrew.studyBoard.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class TestController {


        private final ChatClient.Builder chatClientBuilder;

        @GetMapping("/test")
        public String test() {
            return chatClientBuilder.build()
                    .prompt("안녕! StudyLink AI 연결 테스트야.")
                    .call()
                    .content();
        }

}
