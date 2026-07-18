package com.govos.cmp.enums;

public enum ComplaintFeedbackRating {
    ONE(1),
    TWO(2),
    THREE(3),
    FOUR(4),
    FIVE(5);

    private final int value;

    ComplaintFeedbackRating(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
