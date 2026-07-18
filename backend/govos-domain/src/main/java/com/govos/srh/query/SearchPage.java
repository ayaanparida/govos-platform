package com.govos.srh.query;

public record SearchPage(
        int page,
        int size
) {

    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_SIZE = 20;
    public static final int MAX_SIZE = 100;

    public static SearchPage defaults() {
        return new SearchPage(DEFAULT_PAGE, DEFAULT_SIZE);
    }

    public int offset() {
        return page * size;
    }
}
