package com.studycrew.studyBoard.apiPayload.exception.handler;

import com.studycrew.studyBoard.apiPayload.code.BaseErrorCode;
import com.studycrew.studyBoard.apiPayload.exception.GeneralException;

public class StudyApplicationHandler extends GeneralException {
    public StudyApplicationHandler(BaseErrorCode errorCode) {
        super(errorCode);
    }
}