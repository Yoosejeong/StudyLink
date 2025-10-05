package com.studycrew.studyBoard.service.user;

import com.studycrew.studyBoard.apiPayload.code.status.ErrorStatus;
import com.studycrew.studyBoard.apiPayload.exception.handler.UserHandler;
import com.studycrew.studyBoard.entity.User;
import com.studycrew.studyBoard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.studycrew.studyBoard.dto.UserDTO.UserRequestDTO.UserSignUpRequestDTO;
import static com.studycrew.studyBoard.dto.UserDTO.UserRequestDTO.updateNicknameRequest;

@Service
@Transactional
@RequiredArgsConstructor
public class UserCommandServiceImpl implements UserCommandService {

    private final UserRepository userRepository;

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    public void joinProcess(UserSignUpRequestDTO joinRequestDTO) {

        if (userRepository.existsByEmail(joinRequestDTO.getEmail())) {
             throw new UserHandler(ErrorStatus._EMAIL_DUPLICATED);
        }

        if (userRepository.existsByNickname(joinRequestDTO.getNickname())) {
            throw new UserHandler(ErrorStatus._NICKNAME_DUPLICATED);
        }

        User user = User.builder()
                .email(joinRequestDTO.getEmail())
                .password(bCryptPasswordEncoder.encode(joinRequestDTO.getPassword()))
                .username(joinRequestDTO.getUsername())
                .nickname(joinRequestDTO.getNickname())
                .role("ROLE_USER")
                .build();

        userRepository.save(user);
    }

    @Override
    public User updateNickname(User user,updateNicknameRequest requestDTO) {
        String nickname = requestDTO.getNickname();
        if (userRepository.existsByNickname(nickname)) {
            throw new UserHandler(ErrorStatus._NICKNAME_DUPLICATED);
        }
        user.changeNickname(nickname);
        return user;
    }
}
