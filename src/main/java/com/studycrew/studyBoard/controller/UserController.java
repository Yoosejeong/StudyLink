package com.studycrew.studyBoard.controller;

import com.studycrew.studyBoard.dto.UserDTO.UserSignUpRequestDTO;
import com.studycrew.studyBoard.service.UserCommandServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserCommandServiceImpl userCommandService;

    @PostMapping("/api/signUp")
    public ResponseEntity<String> SignUp(@RequestBody UserSignUpRequestDTO userSignUpRequestDTO) {

        boolean result = userCommandService.joinProcess(userSignUpRequestDTO);
        if (result) {
            return ResponseEntity.status(HttpStatus.CREATED).body("회원가입 성공");
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("이미 존재하는 이메일입니다");
        }
    }

}
