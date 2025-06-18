package com.studycrew.studyBoard.apiPayload.code.status;

import com.studycrew.studyBoard.apiPayload.code.BaseCode;
import com.studycrew.studyBoard.apiPayload.code.ReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum SuccessStatus implements BaseCode {

    // 일반적인 응답
    _OK(HttpStatus.OK, "COMMON200", "성공입니다."),

    //멤버 관련 응답
    _USER_CREATED(HttpStatus.CREATED, "USER201", "회원가입을 성공했습니다."),

    //토큰 관련 응답
    _USER_REISSUED(HttpStatus.OK, "AUTH200", "Access 토큰이 재발급되었습니다."),

    //스터디글 관련 응답
    _STUDY_POST_CREATED(HttpStatus.CREATED, "POST201", "스터디글 등록을 성공했습니다."),
    _STUDY_POST_DELETED(HttpStatus.OK, "POST204", "스터디글을 성공적으로 삭제했습니다."),
    _STUDY_POST_UPDATE(HttpStatus.OK, "POST211", "스터디글을 성공적으로 수정했습니다."),
    _STUDY_POST_RETRIEVED(HttpStatus.OK, "POST210", "스터디글 조회를 성공했습니다."),
    _STUDY_POST_LIST_RETRIEVED(HttpStatus.OK, "POST212", "스터디글 목록 조회를 성공했습니다."),
    _STUDY_POST_CLOSED(HttpStatus.OK, "POST213", "스터디글 모집을 종료했습니다."),

    //스터디 지원 관련 응답
    _STUDY_APPLICATION_CREATED(HttpStatus.CREATED, "APPLICATION201", "스터디 지원을 성공했습니다."),
    _STUDY_APPLICANT_LIST_RETRIEVED(HttpStatus.OK, "APPLICATION200", "스터디 지원자 목록을 성공적으로 조회했습니다."),
    _STUDY_APPLICATION_MY_LIST_RETRIEVED(HttpStatus.OK, "APPLICATION202", "회원이 지원한 스터디 목록을 성공적으로 조회했습니다."),
    _STUDY_APPLICATION_APPROVED(HttpStatus.OK, "APPLICATION203", "스터디 신청이 승인되었습니다."),
    _STUDY_APPLICATION_REJECT(HttpStatus.OK,"APPLICATION204","스터디 신청이 거절되었습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public ReasonDTO getReason() {
        return ReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(true)
                .build();
    }

    @Override
    public ReasonDTO getReasonHttpStatus() {
        return ReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(true)
                .httpStatus(httpStatus)
                .build();
    }

}
