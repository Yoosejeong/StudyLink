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
    _USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER4040", "사용자가 없습니다."),
    _EMAIL_DUPLICATED(HttpStatus.CONFLICT, "USER4090", "이미 사용중인 이메일입니다."),
    _NICKNAME_DUPLICATED(HttpStatus.CONFLICT, "USER4091", "이미 사용중인 닉네임입니다."),
    _USER_LOGIN_INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "USER4010", "이메일 또는 비밀번호가 올바르지 않습니다."),

    //토큰 관련 응답
    _INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH4010", "유효하지 않은 Refresh 토큰입니다."),
    _INVALID_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "TOKEN4012", "유효하지 않은 Access 토큰입니다."),
    _ACCESS_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "TOKEN4011", "Access 토큰이 만료되었습니다."),

    //스터디글 관련 응답
    _STUDY_POST_NOT_FOUND(HttpStatus.NOT_FOUND, "POST4040", "스터디글이 없습니다."),
    _STUDY_POST_FORBIDDEN(HttpStatus.FORBIDDEN, "POST4030" , "스터디글에 권한이 없습니다." ),
    _STUDY_POST_ALREADY_CLOSED(HttpStatus.CONFLICT, "POST4090", "이미 모집이 종료된 스터디글입니다."),
    _STUDY_POST_MAX_CAPACITY_EXCEEDED(HttpStatus.BAD_REQUEST, "POST4000", "스터디 정원이 초과되었습니다."),

    //스터디지원 관련 응답
    _STUDY_APPLICATION_ALREADY_EXISTS(HttpStatus.CONFLICT, "APPLICATION4090", "이미 해당 스터디에 지원했습니다."),
    _STUDY_APPLICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "APPLICATION4040", "스터디 지원이 없습니다."),
    _STUDY_APPLICATION_ALREADY_PROCESSED(HttpStatus.CONFLICT, "APPLICATION4091", "이미 처리된 지원입니다."),
    _STUDY_APPLICATION_FORBIDDEN(HttpStatus.FORBIDDEN, "APPLICATION4030", "스터디 지원에 권한이 없습니다."),
    _SELF_APPLICATION_NOT_ALLOWED(HttpStatus.FORBIDDEN, "APPLICATION4031", "자신의 글에는 지원할 수 없습니다." );

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
