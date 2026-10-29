package com.studycrew.studyBoard.enums;

public enum SearchTarget {
    TITLE("title"),
    TITLE_CONTENT("title, content");

    private final String fulltextColumns;

    SearchTarget(String fulltextColumns) {
        this.fulltextColumns = fulltextColumns;
    }

    public String getFulltextColumns() {
        return fulltextColumns;
    }
}
