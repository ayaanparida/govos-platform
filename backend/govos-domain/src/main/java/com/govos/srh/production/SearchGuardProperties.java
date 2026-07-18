package com.govos.srh.production;

public class SearchGuardProperties {

    private int maxResultWindow = 10000;
    private int maxDeepPaginationOffset = 10000;

    public int getMaxResultWindow() {
        return maxResultWindow;
    }

    public void setMaxResultWindow(int maxResultWindow) {
        this.maxResultWindow = maxResultWindow;
    }

    public int getMaxDeepPaginationOffset() {
        return maxDeepPaginationOffset;
    }

    public void setMaxDeepPaginationOffset(int maxDeepPaginationOffset) {
        this.maxDeepPaginationOffset = maxDeepPaginationOffset;
    }
}
