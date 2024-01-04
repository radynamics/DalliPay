package com.radynamics.dallipay.util;

public enum Platform {
    WINDOWS("windows"),
    OSX("mac"),
    LINUX("unix"),
    UNKNOWN("");

    private static final Platform current = currentPlatform();
    private final String platformId;

    Platform(String platformId) {
        this.platformId = platformId;
    }

    public String platformId() {
        return platformId;
    }

    public static Platform current() {
        return current;
    }

    private static Platform currentPlatform() {
        var osName = System.getProperty("os.name");
        if (osName.startsWith("Windows")) return WINDOWS;
        if (osName.startsWith("Mac")) return OSX;
        if (osName.startsWith("Linux")) return LINUX;
        return UNKNOWN;
    }
}
