package hk.noq.roomq;

import ohos.app.Context;
import ohos.data.DatabaseHelper;
import ohos.data.preferences.Preferences;

public class Token {
    private static final String FILE_NAME = "roomq";
    private static final String TOKEN_KEY = "token";

    static String get(Context context) {
        DatabaseHelper dh = new DatabaseHelper(context);
        Preferences pf = dh.getPreferences(FILE_NAME);
        return pf.getString(TOKEN_KEY, null);
    }

    static void set(Context context, String token) {
        DatabaseHelper dh = new DatabaseHelper(context);
        Preferences pf = dh.getPreferences(FILE_NAME);
        pf.putString(TOKEN_KEY, token);
    }
}
