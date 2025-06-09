package com.studycrew.studyBoard.apiPayload.code.status;

import com.studycrew.studyBoard.apiPayload.code.BaseErrorCode;
import com.studycrew.studyBoard.apiPayload.code.ErrorReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorStatus implements BaseErrorCode {
    // 가장 일반적인 응답
    _INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON500", "서버 에러, 관리자에게 문의 바랍니다."),
    _BAD_REQUEST(HttpStatus.BAD_REQUEST,"COMMON400","잘못된 요청입니다."),
    _UNAUTHORIZED(HttpStatus.UNAUTHORIZED,"COMMON401","인증이 필요합니다."),
    _FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON403", "금지된 요청입니다."),

    //멤버 관련 응답
    _USER_NOT_FOUND(HttpStatus.BAD_REQUEST, "USER401", "사용자가 없습니다."),
    _EMAIL_DUPLICATED(HttpStatus.CONFLICT, "EMAIL409", "이미 사용중인 이메일입니다."),
    _NICKNAME_DUPLICATED(HttpStatus.CONFLICT, "NICKNAME409", "이미 사용중인 닉네임입니다."),

    //토큰 관련 응답
    _INVALID_REFRESH_TOKEN(HttpStatus.BAD_REQUEST, "AUTH400", "유효하지 않은 Refresh 토큰입니다."),

    //스터디글 관련 응답
    _STUDY_POST_NOT_FOUND(HttpStatus.NOT_FOUND, "POST404", "스터디글이 없습니다."),
    _STUDY_POST_FORBIDDEN(HttpStatus.FORBIDDEN, "POST400" , "스터디글에 권한이 없습니다." );

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public ErrorReasonDTO getReason() {
        return ErrorReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(false)
                .build();
    }

    @Override
    public ErrorReasonDTO getReasonHttpStatus() {
        return ErrorReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(false)
                .httpStatus(httpStatus)
                .build();
    }
}
