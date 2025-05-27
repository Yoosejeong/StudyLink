package com.studycrew.studyBoard.service;

import com.studycrew.studyBoard.dto.UserDTO.UserSignUpRequestDTO;
import com.studycrew.studyBoard.entity.User;
import com.studycrew.studyBoard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserCommandServiceImpl implements UserCommandService {

    private final UserRepository userRepository;

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public boolean joinProcess(UserSignUpRequestDTO joinRequestDTO) {

        String email = joinRequestDTO.getEmail();
        String password = joinRequestDTO.getPassword();

        Boolean isExist = userRepository.existsByEmail(email);

        if (isExist) {
            return false;
        }

        User user = User.builder()
                .email(email)
                .password(bCryptPasswordEncoder.encode(password))
                .role("ROLE_USER")
                .build();
        userRepository.save(user);
        return true;
    }

}
