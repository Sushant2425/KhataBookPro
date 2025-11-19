package com.sandhyasofttechh.mykhatapro.model;

public class SettingsItem {
    private final String title;
    private final boolean isHeader;

    public SettingsItem(String title, boolean isHeader) {
        this.title = title;
        this.isHeader = isHeader;
    }

    public String getTitle() {
        return title;
    }

    public boolean isHeader() {
        return isHeader;
    }
}
