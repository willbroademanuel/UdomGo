package com.example.udomgo;

import android.content.Context;
import android.content.SharedPreferences;

public class UserPreferences {

    private static final String PREF_NAME = "udomgo_user_prefs";
    private static final String KEY_COLLEGE_CODE = "selected_college_code";
    private static final String KEY_COLLEGE_NAME = "selected_college_name";

    // Authentication & Session Keys
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_CURRENT_USER_EMAIL = "current_user_email";
    private static final String KEY_CURRENT_USER_NAME = "current_user_name";

    // Default Demo Account for testing/presentation
    public static final String DEMO_EMAIL = "student@udom.ac.tz";
    public static final String DEMO_PASSWORD = "password123";
    public static final String DEMO_NAME = "UDOM Student";

    // ==========================================
    // COLLEGE SELECTION
    // ==========================================

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

    // ==========================================
    // LOCAL AUTHENTICATION & USER MANAGEMENT (SQLite)
    // ==========================================

    /**
     * Checks whether an account exists for the specified email in the SQLite database.
     */
    public static boolean hasAccount(Context context, String email) {
        if (context == null || email == null || email.trim().isEmpty()) return false;
        DatabaseHelper db = DatabaseHelper.getInstance(context);
        return db.checkEmailExists(email);
    }

    /**
     * Registers a new user into the SQLite database.
     */
    public static boolean registerUser(Context context, String name, String email, String password) {
        if (context == null || email == null || password == null) return false;
        DatabaseHelper db = DatabaseHelper.getInstance(context);
        return db.insertUser(name, email, password, "CIVE");
    }

    /**
     * Verifies user credentials against the SQLite database.
     */
    public static boolean authenticate(Context context, String email, String password) {
        if (context == null || email == null || password == null) return false;
        DatabaseHelper db = DatabaseHelper.getInstance(context);

        if (db.checkUserCredentials(email, password)) {
            String name = db.getUserName(email);
            setLoggedInSession(context, email, name);
            return true;
        }

        return false;
    }

    /**
     * Sets active logged in user session in SharedPreferences.
     */
    public static void setLoggedInSession(Context context, String email, String name) {
        if (context == null) return;
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putBoolean(KEY_IS_LOGGED_IN, true)
                .putString(KEY_CURRENT_USER_EMAIL, email != null ? email.trim() : "")
                .putString(KEY_CURRENT_USER_NAME, name != null ? name.trim() : "Student")
                .apply();
    }

    /**
     * Checks whether a user is currently logged in.
     */
    public static boolean isLoggedIn(Context context) {
        if (context == null) return false;
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    /**
     * Gets the logged-in user's display name.
     */
    public static String getCurrentUserName(Context context) {
        if (context == null) return "Student";
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_CURRENT_USER_NAME, "Student");
    }

    /**
     * Gets the logged-in user's email.
     */
    public static String getCurrentUserEmail(Context context) {
        if (context == null) return "";
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_CURRENT_USER_EMAIL, "");
    }

    /**
     * Logs the current user out.
     */
    public static void logout(Context context) {
        if (context == null) return;
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putBoolean(KEY_IS_LOGGED_IN, false)
                .remove(KEY_CURRENT_USER_EMAIL)
                .remove(KEY_CURRENT_USER_NAME)
                .apply();
    }
}
