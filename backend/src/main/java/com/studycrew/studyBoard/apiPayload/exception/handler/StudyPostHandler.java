package com.studycrew.studyBoard.apiPayload.exception.handler;

import com.studycrew.studyBoard.apiPayload.code.BaseErrorCode;
import com.studycrew.studyBoard.apiPayload.exception.GeneralException;

public class StudyPostHandler extends GeneralException {
    public StudyPostHandler(BaseErrorCode errorCode){
      super(errorCode);
      }
}
