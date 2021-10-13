package com.example.roomq_module;

public class LogManager {
    private static LogManager instance = null;
    private boolean debugMode = true;

    private LogManager() {

    }

    public static LogManager getInstance() {
        if (instance == null) {
            instance = new LogManager();
        }
        return instance;
    }

    public void log(String message) {
        if (debugMode) {
            System.out.println("[NoQ]" + message);
        }
    }

    public void confirm(String message) {
        System.out.println("[NoQ][Confirm]" + message);
    }
}