package com.example.roomq_module;

import ohos.app.Context;
import ohos.data.DatabaseHelper;
import ohos.data.preferences.Preferences;

public class Token {
    private static final String FILE_NAME = "roomq";
    private static final String TOKEN_KEY = "token";

    public static String get(Context context) {
        DatabaseHelper dh = new DatabaseHelper(context);
        Preferences pf = dh.getPreferences(FILE_NAME);
        return pf.getString(TOKEN_KEY, null);
    }

    public static void set(Context context, String token) {
        DatabaseHelper dh = new DatabaseHelper(context);
        Preferences pf = dh.getPreferences(FILE_NAME);
        pf.putString(TOKEN_KEY, token);
    }
}
