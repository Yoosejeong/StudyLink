package com.studycrew.studyBoard.util;

import com.studycrew.studyBoard.enums.StudyStatus;

import java.util.Locale;

public final class StudyPostCountKey {
    private StudyPostCountKey() {}

    public static String key(StudyStatus status) {
        return "total:" + (status == null ? "ALL" : status.name());
    }
}
