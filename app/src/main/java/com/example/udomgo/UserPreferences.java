package com.example.udomgo;

import android.content.Context;
import android.content.SharedPreferences;

public class UserPreferences {

    private static final String PREF_NAME = "udomgo_user_prefs";
    private static final String KEY_COLLEGE_CODE = "selected_college_code";
    private static final String KEY_COLLEGE_NAME = "selected_college_name";

    public static void setSelectedCollege(Context context, String code, String name) {
        if (context == null) return;
        SharedPreferences prefs =
                context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putString(KEY_COLLEGE_CODE, code)
                .putString(KEY_COLLEGE_NAME, name)
                .apply();
    }

    public static String getSelectedCollege(Context context) {
        if (context == null) return "CIVE";
        SharedPreferences prefs =
                context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_COLLEGE_CODE, "CIVE");
    }

    public static String getSelectedCollegeFullName(Context context) {
        if (context == null) return "College of Informatics and Virtual Education";
        SharedPreferences prefs =
                context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_COLLEGE_NAME, "College of Informatics and Virtual Education");
    }
}
