package com.example.udomgo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * DatabaseHelper manages the SQLite database for UDOMGo.
 * Handles user authentication, chat history, campus FAQs,
 * academic timetables, professor directories, academic calendar (UE),
 * student leaders/CRs, and campus emergency contacts.
 * Includes complete error handling and defensive null checks across all operations.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";

    // Database Information
    public static final String DATABASE_NAME = "udomgo.db";
    public static final int DATABASE_VERSION = 9;

    // Table 1: Users
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_COLLEGE = "college";
    public static final String COLUMN_CREATED_AT = "created_at";

    // Table 2: Chat History
    public static final String TABLE_CHAT_HISTORY = "chat_history";
    public static final String COLUMN_CHAT_ID = "id";
    public static final String COLUMN_CHAT_USER_EMAIL = "user_email";
    public static final String COLUMN_CHAT_SENDER = "sender";
    public static final String COLUMN_CHAT_MESSAGE = "message";
    public static final String COLUMN_CHAT_TIMESTAMP = "timestamp";

    // Table 3: Campus FAQs (Knowledge Base)
    public static final String TABLE_CAMPUS_FAQS = "campus_faqs";
    public static final String COLUMN_FAQ_ID = "id";
    public static final String COLUMN_FAQ_CATEGORY = "category";
    public static final String COLUMN_FAQ_KEYWORDS = "keywords";
    public static final String COLUMN_FAQ_QUESTION = "question";
    public static final String COLUMN_FAQ_ANSWER = "answer";
    public static final String COLUMN_FAQ_COLLEGE = "college_specific";

    // Table 4: Professors & Academic Staff
    public static final String TABLE_PROFESSORS = "professors";
    public static final String COLUMN_PROF_ID = "id";
    public static final String COLUMN_PROF_NAME = "name";
    public static final String COLUMN_PROF_COLLEGE = "college";
    public static final String COLUMN_PROF_DEPARTMENT = "department";
    public static final String COLUMN_PROF_OFFICE_BLOCK = "office_block";
    public static final String COLUMN_PROF_OFFICE_ROOM = "office_room";
    public static final String COLUMN_PROF_PHONE = "phone";
    public static final String COLUMN_PROF_EMAIL = "email";
    public static final String COLUMN_PROF_SUBJECTS = "subjects_taught";
    public static final String COLUMN_PROF_YEARS = "target_years";
    public static final String COLUMN_PROF_CONSULTATION = "consultation_hours";

    // Table 5: Timetables (Software Engineering & all courses)
    public static final String TABLE_TIMETABLES = "timetables";
    public static final String COLUMN_TIME_ID = "id";
    public static final String COLUMN_TIME_PROGRAMME = "programme";
    public static final String COLUMN_TIME_YEAR = "year_of_study";
    public static final String COLUMN_TIME_SEMESTER = "semester";
    public static final String COLUMN_TIME_DAY = "day_of_week";
    public static final String COLUMN_TIME_START = "start_time";
    public static final String COLUMN_TIME_END = "end_time";
    public static final String COLUMN_TIME_CODE = "course_code";
    public static final String COLUMN_TIME_NAME = "course_name";
    public static final String COLUMN_TIME_SESSION = "session_type"; // 'Theory', 'Practical', 'Tutorial', 'Clinic'
    public static final String COLUMN_TIME_VENUE = "venue";
    public static final String COLUMN_TIME_LECTURER = "lecturer_name";

    // Table 6: Academic Calendar (Multi-Year Almanac 2024-2027)
    public static final String TABLE_ACADEMIC_CALENDAR = "academic_calendar";
    public static final String COLUMN_CAL_ID = "id";
    public static final String COLUMN_CAL_YEAR = "academic_year"; // '2024/2025', '2025/2026', '2026/2027'
    public static final String COLUMN_CAL_SEMESTER = "semester"; // 'Semester I', 'Semester II', 'Break', 'ALL'
    public static final String COLUMN_CAL_EVENT = "event_name";
    public static final String COLUMN_CAL_CATEGORY = "category";
    public static final String COLUMN_CAL_START = "start_date";
    public static final String COLUMN_CAL_END = "end_date";
    public static final String COLUMN_CAL_DESCRIPTION = "description";

    // Table 9: Courses (Curriculum Catalog & Degree Handbook)
    public static final String TABLE_COURSES = "courses";
    public static final String COLUMN_COURSE_ID = "id";
    public static final String COLUMN_COURSE_PROGRAMME = "programme";
    public static final String COLUMN_COURSE_YEAR = "year_of_study";
    public static final String COLUMN_COURSE_SEMESTER = "semester";
    public static final String COLUMN_COURSE_CODE = "course_code";
    public static final String COLUMN_COURSE_TITLE = "course_title";
    public static final String COLUMN_COURSE_CREDITS = "credits";
    public static final String COLUMN_COURSE_STATUS = "course_status"; // 'Core' or 'Elective'
    public static final String COLUMN_COURSE_CATEGORY = "category";

    // Table 7: Student Leaders & Prefects (CRs, UDOSO)
    public static final String TABLE_STUDENT_LEADERS = "student_leaders";
    public static final String COLUMN_LEAD_ID = "id";
    public static final String COLUMN_LEAD_NAME = "name";
    public static final String COLUMN_LEAD_ROLE = "role";
    public static final String COLUMN_LEAD_COLLEGE = "college";
    public static final String COLUMN_LEAD_LOCATION = "office_or_hostel";
    public static final String COLUMN_LEAD_ROOM = "room_number";
    public static final String COLUMN_LEAD_PHONE = "phone";
    public static final String COLUMN_LEAD_EMAIL = "email";

    // Table 8: Campus Emergency & Department Contacts
    public static final String TABLE_CAMPUS_CONTACTS = "campus_contacts";
    public static final String COLUMN_CON_ID = "id";
    public static final String COLUMN_CON_DEPARTMENT = "department_or_office";
    public static final String COLUMN_CON_CATEGORY = "category";
    public static final String COLUMN_CON_COLLEGE = "college";
    public static final String COLUMN_CON_PHONE = "phone_number";
    public static final String COLUMN_CON_EMAIL = "email";
    public static final String COLUMN_CON_LOCATION = "office_location";
    public static final String COLUMN_CON_DESCRIPTION = "description";
    public static final String COLUMN_CON_KEYWORDS = "keywords";

    // Table 10: Campus Buildings & Locations (Geo-spatial)
    public static final String TABLE_CAMPUS_LOCATIONS = "campus_locations";
    public static final String COLUMN_LOC_ID = "id";
    public static final String COLUMN_LOC_NAME = "name";
    public static final String COLUMN_LOC_COLLEGE = "college";
    public static final String COLUMN_LOC_LATITUDE = "latitude";
    public static final String COLUMN_LOC_LONGITUDE = "longitude";
    public static final String COLUMN_LOC_CATEGORY = "category";
    public static final String COLUMN_LOC_DESCRIPTION = "description";
    public static final String COLUMN_LOC_KEYWORDS = "keywords";

    // Shared default contact info per requirements
    public static final String DEFAULT_PHONE = "0788129212";
    public static final String DEFAULT_EMAIL = "staff@udomgo.com";

    // Singleton instance
    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null && context != null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            // Table 1: Users
            String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + " ("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COLUMN_NAME + " TEXT NOT NULL, "
                    + COLUMN_EMAIL + " TEXT UNIQUE NOT NULL, "
                    + COLUMN_PASSWORD + " TEXT NOT NULL, "
                    + COLUMN_COLLEGE + " TEXT DEFAULT 'CIVE', "
                    + COLUMN_CREATED_AT + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                    + ");";

            // Table 2: Chat History
            String CREATE_CHAT_TABLE = "CREATE TABLE " + TABLE_CHAT_HISTORY + " ("
                    + COLUMN_CHAT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COLUMN_CHAT_USER_EMAIL + " TEXT NOT NULL, "
                    + COLUMN_CHAT_SENDER + " TEXT NOT NULL, "
                    + COLUMN_CHAT_MESSAGE + " TEXT NOT NULL, "
                    + COLUMN_CHAT_TIMESTAMP + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                    + "FOREIGN KEY(" + COLUMN_CHAT_USER_EMAIL + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_EMAIL + ") ON DELETE CASCADE"
                    + ");";

            // Table 3: Campus FAQs
            String CREATE_FAQS_TABLE = "CREATE TABLE " + TABLE_CAMPUS_FAQS + " ("
                    + COLUMN_FAQ_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COLUMN_FAQ_CATEGORY + " TEXT NOT NULL, "
                    + COLUMN_FAQ_KEYWORDS + " TEXT NOT NULL, "
                    + COLUMN_FAQ_QUESTION + " TEXT NOT NULL, "
                    + COLUMN_FAQ_ANSWER + " TEXT NOT NULL, "
                    + COLUMN_FAQ_COLLEGE + " TEXT DEFAULT 'ALL'"
                    + ");";

            // Table 4: Professors
            String CREATE_PROFESSORS_TABLE = "CREATE TABLE " + TABLE_PROFESSORS + " ("
                    + COLUMN_PROF_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COLUMN_PROF_NAME + " TEXT NOT NULL, "
                    + COLUMN_PROF_COLLEGE + " TEXT NOT NULL, "
                    + COLUMN_PROF_DEPARTMENT + " TEXT NOT NULL, "
                    + COLUMN_PROF_OFFICE_BLOCK + " TEXT, "
                    + COLUMN_PROF_OFFICE_ROOM + " TEXT, "
                    + COLUMN_PROF_PHONE + " TEXT DEFAULT '" + DEFAULT_PHONE + "', "
                    + COLUMN_PROF_EMAIL + " TEXT DEFAULT '" + DEFAULT_EMAIL + "', "
                    + COLUMN_PROF_SUBJECTS + " TEXT, "
                    + COLUMN_PROF_YEARS + " TEXT, "
                    + COLUMN_PROF_CONSULTATION + " TEXT"
                    + ");";

            // Table 5: Timetables
            String CREATE_TIMETABLES_TABLE = "CREATE TABLE " + TABLE_TIMETABLES + " ("
                    + COLUMN_TIME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COLUMN_TIME_PROGRAMME + " TEXT NOT NULL, "
                    + COLUMN_TIME_YEAR + " INTEGER NOT NULL, "
                    + COLUMN_TIME_SEMESTER + " INTEGER DEFAULT 1, "
                    + COLUMN_TIME_DAY + " TEXT NOT NULL, "
                    + COLUMN_TIME_START + " TEXT NOT NULL, "
                    + COLUMN_TIME_END + " TEXT NOT NULL, "
                    + COLUMN_TIME_CODE + " TEXT NOT NULL, "
                    + COLUMN_TIME_NAME + " TEXT NOT NULL, "
                    + COLUMN_TIME_SESSION + " TEXT DEFAULT 'Theory', "
                    + COLUMN_TIME_VENUE + " TEXT NOT NULL, "
                    + COLUMN_TIME_LECTURER + " TEXT"
                    + ");";

            // Table 6: Academic Calendar
            String CREATE_CALENDAR_TABLE = "CREATE TABLE " + TABLE_ACADEMIC_CALENDAR + " ("
                    + COLUMN_CAL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COLUMN_CAL_YEAR + " TEXT DEFAULT '2025/2026', "
                    + COLUMN_CAL_SEMESTER + " TEXT DEFAULT 'Semester I', "
                    + COLUMN_CAL_EVENT + " TEXT NOT NULL, "
                    + COLUMN_CAL_CATEGORY + " TEXT NOT NULL, "
                    + COLUMN_CAL_START + " TEXT NOT NULL, "
                    + COLUMN_CAL_END + " TEXT, "
                    + COLUMN_CAL_DESCRIPTION + " TEXT"
                    + ");";

            // Table 9: Courses (Curriculum Handbook)
            String CREATE_COURSES_TABLE = "CREATE TABLE " + TABLE_COURSES + " ("
                    + COLUMN_COURSE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COLUMN_COURSE_PROGRAMME + " TEXT NOT NULL, "
                    + COLUMN_COURSE_YEAR + " INTEGER NOT NULL, "
                    + COLUMN_COURSE_SEMESTER + " INTEGER NOT NULL, "
                    + COLUMN_COURSE_CODE + " TEXT NOT NULL, "
                    + COLUMN_COURSE_TITLE + " TEXT NOT NULL, "
                    + COLUMN_COURSE_CREDITS + " REAL NOT NULL, "
                    + COLUMN_COURSE_STATUS + " TEXT DEFAULT 'Core', "
                    + COLUMN_COURSE_CATEGORY + " TEXT DEFAULT 'Core Software'"
                    + ");";

            // Table 7: Student Leaders
            String CREATE_LEADERS_TABLE = "CREATE TABLE " + TABLE_STUDENT_LEADERS + " ("
                    + COLUMN_LEAD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COLUMN_LEAD_NAME + " TEXT NOT NULL, "
                    + COLUMN_LEAD_ROLE + " TEXT NOT NULL, "
                    + COLUMN_LEAD_COLLEGE + " TEXT NOT NULL, "
                    + COLUMN_LEAD_LOCATION + " TEXT, "
                    + COLUMN_LEAD_ROOM + " TEXT, "
                    + COLUMN_LEAD_PHONE + " TEXT DEFAULT '" + DEFAULT_PHONE + "', "
                    + COLUMN_LEAD_EMAIL + " TEXT DEFAULT '" + DEFAULT_EMAIL + "'"
                    + ");";

            // Table 8: Campus Emergency & Department Contacts
            String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_CAMPUS_CONTACTS + " ("
                    + COLUMN_CON_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COLUMN_CON_DEPARTMENT + " TEXT NOT NULL, "
                    + COLUMN_CON_CATEGORY + " TEXT NOT NULL, "
                    + COLUMN_CON_COLLEGE + " TEXT DEFAULT 'ALL', "
                    + COLUMN_CON_PHONE + " TEXT DEFAULT '" + DEFAULT_PHONE + "', "
                    + COLUMN_CON_EMAIL + " TEXT DEFAULT '" + DEFAULT_EMAIL + "', "
                    + COLUMN_CON_LOCATION + " TEXT, "
                    + COLUMN_CON_DESCRIPTION + " TEXT, "
                    + COLUMN_CON_KEYWORDS + " TEXT"
                    + ");";

            // Table 10: Campus Buildings & Locations
            String CREATE_LOCATIONS_TABLE = "CREATE TABLE " + TABLE_CAMPUS_LOCATIONS + " ("
                    + COLUMN_LOC_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COLUMN_LOC_NAME + " TEXT NOT NULL, "
                    + COLUMN_LOC_COLLEGE + " TEXT NOT NULL, "
                    + COLUMN_LOC_LATITUDE + " REAL, "
                    + COLUMN_LOC_LONGITUDE + " REAL, "
                    + COLUMN_LOC_CATEGORY + " TEXT, "
                    + COLUMN_LOC_DESCRIPTION + " TEXT, "
                    + COLUMN_LOC_KEYWORDS + " TEXT"
                    + ");";

            db.execSQL(CREATE_USERS_TABLE);
            db.execSQL(CREATE_CHAT_TABLE);
            db.execSQL(CREATE_FAQS_TABLE);
            db.execSQL(CREATE_PROFESSORS_TABLE);
            db.execSQL(CREATE_TIMETABLES_TABLE);
            db.execSQL(CREATE_CALENDAR_TABLE);
            db.execSQL(CREATE_COURSES_TABLE);
            db.execSQL(CREATE_LEADERS_TABLE);
            db.execSQL(CREATE_CONTACTS_TABLE);
            db.execSQL(CREATE_LOCATIONS_TABLE);

            // Performance Indices
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_courses_prog_yr_sem ON " + TABLE_COURSES + " ("
                    + COLUMN_COURSE_PROGRAMME + ", " + COLUMN_COURSE_YEAR + ", " + COLUMN_COURSE_SEMESTER + ");");
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_timetables_prog_yr_sem ON " + TABLE_TIMETABLES + " ("
                    + COLUMN_TIME_PROGRAMME + ", " + COLUMN_TIME_YEAR + ", " + COLUMN_TIME_SEMESTER + ", " + COLUMN_TIME_DAY + ");");
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_academic_cal_year_sem ON " + TABLE_ACADEMIC_CALENDAR + " ("
                    + COLUMN_CAL_YEAR + ", " + COLUMN_CAL_SEMESTER + ");");

            Log.d(TAG, "All tables created successfully.");

            // Pre-populate built-in demo account
            ContentValues values = new ContentValues();
            values.put(COLUMN_NAME, "UDOM Student");
            values.put(COLUMN_EMAIL, "student@udom.ac.tz");
            values.put(COLUMN_PASSWORD, "password123");
            values.put(COLUMN_COLLEGE, "CIVE");
            db.insert(TABLE_USERS, null, values);

            // Populate initial data
            seedCampusFaqs(db);
            seedProfessors(db);
            seedCourses(db);
            seedTimetables(db);
            seedAcademicCalendar(db);
            seedStudentLeaders(db);
            seedCampusContacts(db);
            seedCampusLocations(db);

            Log.d(TAG, "All seed data inserted successfully.");
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate SQLite database: " + e.getMessage(), e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);
        try {
            if (oldVersion < 2) {
                db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_CHAT_HISTORY + " ("
                        + COLUMN_CHAT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + COLUMN_CHAT_USER_EMAIL + " TEXT NOT NULL, "
                        + COLUMN_CHAT_SENDER + " TEXT NOT NULL, "
                        + COLUMN_CHAT_MESSAGE + " TEXT NOT NULL, "
                        + COLUMN_CHAT_TIMESTAMP + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                        + ");");
            }
            if (oldVersion < 3) {
                db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_CAMPUS_FAQS + " ("
                        + COLUMN_FAQ_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + COLUMN_FAQ_CATEGORY + " TEXT NOT NULL, "
                        + COLUMN_FAQ_KEYWORDS + " TEXT NOT NULL, "
                        + COLUMN_FAQ_QUESTION + " TEXT NOT NULL, "
                        + COLUMN_FAQ_ANSWER + " TEXT NOT NULL, "
                        + COLUMN_FAQ_COLLEGE + " TEXT DEFAULT 'ALL'"
                        + ");");
                seedCampusFaqs(db);
            }
            if (oldVersion < 4) {
                // Table 4: Professors
                db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_PROFESSORS + " ("
                        + COLUMN_PROF_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + COLUMN_PROF_NAME + " TEXT NOT NULL, "
                        + COLUMN_PROF_COLLEGE + " TEXT NOT NULL, "
                        + COLUMN_PROF_DEPARTMENT + " TEXT NOT NULL, "
                        + COLUMN_PROF_OFFICE_BLOCK + " TEXT, "
                        + COLUMN_PROF_OFFICE_ROOM + " TEXT, "
                        + COLUMN_PROF_PHONE + " TEXT DEFAULT '" + DEFAULT_PHONE + "', "
                        + COLUMN_PROF_EMAIL + " TEXT DEFAULT '" + DEFAULT_EMAIL + "', "
                        + COLUMN_PROF_SUBJECTS + " TEXT, "
                        + COLUMN_PROF_YEARS + " TEXT, "
                        + COLUMN_PROF_CONSULTATION + " TEXT"
                        + ");");

                // Table 5: Timetables
                db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_TIMETABLES + " ("
                        + COLUMN_TIME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + COLUMN_TIME_PROGRAMME + " TEXT NOT NULL, "
                        + COLUMN_TIME_YEAR + " INTEGER NOT NULL, "
                        + COLUMN_TIME_DAY + " TEXT NOT NULL, "
                        + COLUMN_TIME_START + " TEXT NOT NULL, "
                        + COLUMN_TIME_END + " TEXT NOT NULL, "
                        + COLUMN_TIME_CODE + " TEXT NOT NULL, "
                        + COLUMN_TIME_NAME + " TEXT NOT NULL, "
                        + COLUMN_TIME_VENUE + " TEXT NOT NULL, "
                        + COLUMN_TIME_LECTURER + " TEXT"
                        + ");");

                // Table 6: Academic Calendar
                db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_ACADEMIC_CALENDAR + " ("
                        + COLUMN_CAL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + COLUMN_CAL_EVENT + " TEXT NOT NULL, "
                        + COLUMN_CAL_CATEGORY + " TEXT NOT NULL, "
                        + COLUMN_CAL_START + " TEXT NOT NULL, "
                        + COLUMN_CAL_END + " TEXT, "
                        + COLUMN_CAL_DESCRIPTION + " TEXT"
                        + ");");

                // Table 7: Student Leaders
                db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_STUDENT_LEADERS + " ("
                        + COLUMN_LEAD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + COLUMN_LEAD_NAME + " TEXT NOT NULL, "
                        + COLUMN_LEAD_ROLE + " TEXT NOT NULL, "
                        + COLUMN_LEAD_COLLEGE + " TEXT NOT NULL, "
                        + COLUMN_LEAD_LOCATION + " TEXT, "
                        + COLUMN_LEAD_ROOM + " TEXT, "
                        + COLUMN_LEAD_PHONE + " TEXT DEFAULT '" + DEFAULT_PHONE + "', "
                        + COLUMN_LEAD_EMAIL + " TEXT DEFAULT '" + DEFAULT_EMAIL + "'"
                        + ");");

                // Table 8: Campus Contacts
                db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_CAMPUS_CONTACTS + " ("
                        + COLUMN_CON_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + COLUMN_CON_DEPARTMENT + " TEXT NOT NULL, "
                        + COLUMN_CON_CATEGORY + " TEXT NOT NULL, "
                        + COLUMN_CON_COLLEGE + " TEXT DEFAULT 'ALL', "
                        + COLUMN_CON_PHONE + " TEXT DEFAULT '" + DEFAULT_PHONE + "', "
                        + COLUMN_CON_EMAIL + " TEXT DEFAULT '" + DEFAULT_EMAIL + "', "
                        + COLUMN_CON_LOCATION + " TEXT, "
                        + COLUMN_CON_DESCRIPTION + " TEXT, "
                        + COLUMN_CON_KEYWORDS + " TEXT"
                        + ");");

                seedProfessors(db);
                seedTimetables(db);
                seedAcademicCalendar(db);
                seedStudentLeaders(db);
                seedCampusContacts(db);
                seedEnhancedFirstYearFaqs(db);
            }
            if (oldVersion < 5) {
                db.execSQL("DELETE FROM " + TABLE_ACADEMIC_CALENDAR);
                seedAcademicCalendar(db);
            }
            if (oldVersion < 6) {
                db.execSQL("DELETE FROM " + TABLE_PROFESSORS);
                db.execSQL("DELETE FROM " + TABLE_TIMETABLES);
                db.execSQL("DELETE FROM " + TABLE_CAMPUS_CONTACTS);
                seedProfessors(db);
                seedTimetables(db);
                seedCampusContacts(db);
            }
            if (oldVersion < 7) {
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_TIMETABLES);
                String CREATE_TIMETABLES_TABLE = "CREATE TABLE " + TABLE_TIMETABLES + " ("
                        + COLUMN_TIME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + COLUMN_TIME_PROGRAMME + " TEXT NOT NULL, "
                        + COLUMN_TIME_YEAR + " INTEGER NOT NULL, "
                        + COLUMN_TIME_SEMESTER + " INTEGER DEFAULT 1, "
                        + COLUMN_TIME_DAY + " TEXT NOT NULL, "
                        + COLUMN_TIME_START + " TEXT NOT NULL, "
                        + COLUMN_TIME_END + " TEXT NOT NULL, "
                        + COLUMN_TIME_CODE + " TEXT NOT NULL, "
                        + COLUMN_TIME_NAME + " TEXT NOT NULL, "
                        + COLUMN_TIME_VENUE + " TEXT NOT NULL, "
                        + COLUMN_TIME_LECTURER + " TEXT"
                        + ");";
                db.execSQL(CREATE_TIMETABLES_TABLE);
                seedTimetables(db);
                db.execSQL("DELETE FROM " + TABLE_PROFESSORS);
                seedProfessors(db);
            }
            if (oldVersion < 8) {
                // Table 9: Courses
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_COURSES);
                String CREATE_COURSES_TABLE = "CREATE TABLE " + TABLE_COURSES + " ("
                        + COLUMN_COURSE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + COLUMN_COURSE_PROGRAMME + " TEXT NOT NULL, "
                        + COLUMN_COURSE_YEAR + " INTEGER NOT NULL, "
                        + COLUMN_COURSE_SEMESTER + " INTEGER NOT NULL, "
                        + COLUMN_COURSE_CODE + " TEXT NOT NULL, "
                        + COLUMN_COURSE_TITLE + " TEXT NOT NULL, "
                        + COLUMN_COURSE_CREDITS + " REAL NOT NULL, "
                        + COLUMN_COURSE_STATUS + " TEXT DEFAULT 'Core', "
                        + COLUMN_COURSE_CATEGORY + " TEXT DEFAULT 'Core Software'"
                        + ");";
                db.execSQL(CREATE_COURSES_TABLE);
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_courses_prog_yr_sem ON " + TABLE_COURSES + " ("
                        + COLUMN_COURSE_PROGRAMME + ", " + COLUMN_COURSE_YEAR + ", " + COLUMN_COURSE_SEMESTER + ");");

                // Table 5: Timetables (recreate with session_type)
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_TIMETABLES);
                String CREATE_TIMETABLES_TABLE = "CREATE TABLE " + TABLE_TIMETABLES + " ("
                        + COLUMN_TIME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + COLUMN_TIME_PROGRAMME + " TEXT NOT NULL, "
                        + COLUMN_TIME_YEAR + " INTEGER NOT NULL, "
                        + COLUMN_TIME_SEMESTER + " INTEGER DEFAULT 1, "
                        + COLUMN_TIME_DAY + " TEXT NOT NULL, "
                        + COLUMN_TIME_START + " TEXT NOT NULL, "
                        + COLUMN_TIME_END + " TEXT NOT NULL, "
                        + COLUMN_TIME_CODE + " TEXT NOT NULL, "
                        + COLUMN_TIME_NAME + " TEXT NOT NULL, "
                        + COLUMN_TIME_SESSION + " TEXT DEFAULT 'Theory', "
                        + COLUMN_TIME_VENUE + " TEXT NOT NULL, "
                        + COLUMN_TIME_LECTURER + " TEXT"
                        + ");";
                db.execSQL(CREATE_TIMETABLES_TABLE);
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_timetables_prog_yr_sem ON " + TABLE_TIMETABLES + " ("
                        + COLUMN_TIME_PROGRAMME + ", " + COLUMN_TIME_YEAR + ", " + COLUMN_TIME_SEMESTER + ", " + COLUMN_TIME_DAY + ");");

                // Table 6: Academic Calendar (recreate with academic_year, semester)
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACADEMIC_CALENDAR);
                String CREATE_CALENDAR_TABLE = "CREATE TABLE " + TABLE_ACADEMIC_CALENDAR + " ("
                        + COLUMN_CAL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + COLUMN_CAL_YEAR + " TEXT DEFAULT '2025/2026', "
                        + COLUMN_CAL_SEMESTER + " TEXT DEFAULT 'Semester I', "
                        + COLUMN_CAL_EVENT + " TEXT NOT NULL, "
                        + COLUMN_CAL_CATEGORY + " TEXT NOT NULL, "
                        + COLUMN_CAL_START + " TEXT NOT NULL, "
                        + COLUMN_CAL_END + " TEXT, "
                        + COLUMN_CAL_DESCRIPTION + " TEXT"
                        + ");";
                db.execSQL(CREATE_CALENDAR_TABLE);
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_academic_cal_year_sem ON " + TABLE_ACADEMIC_CALENDAR + " ("
                        + COLUMN_CAL_YEAR + ", " + COLUMN_CAL_SEMESTER + ");");

                seedCourses(db);
                seedTimetables(db);
                seedAcademicCalendar(db);
            }
            if (oldVersion < 9) {
                // Ensure Table 10 exists (it might have been created in onCreate for new v8 users, but not v7)
                db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_CAMPUS_LOCATIONS + " ("
                        + COLUMN_LOC_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + COLUMN_LOC_NAME + " TEXT NOT NULL, "
                        + COLUMN_LOC_COLLEGE + " TEXT NOT NULL, "
                        + COLUMN_LOC_LATITUDE + " REAL, "
                        + COLUMN_LOC_LONGITUDE + " REAL, "
                        + COLUMN_LOC_CATEGORY + " TEXT, "
                        + COLUMN_LOC_DESCRIPTION + " TEXT, "
                        + COLUMN_LOC_KEYWORDS + " TEXT"
                        + ");");
                db.execSQL("DELETE FROM " + TABLE_CAMPUS_LOCATIONS); // Clear if any
                seedCampusLocations(db);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error executing onUpgrade: " + e.getMessage(), e);
        }
    }

    // ==========================================
    // DATA MODELS
    // ==========================================

    public static class ProfessorItem {
        public long id;
        public String name;
        public String college;
        public String department;
        public String officeBlock;
        public String officeRoom;
        public String phone;
        public String email;
        public String subjectsTaught;
        public String targetYears;
        public String consultationHours;

        public ProfessorItem(long id, String name, String college, String department,
                             String officeBlock, String officeRoom, String phone, String email,
                             String subjectsTaught, String targetYears, String consultationHours) {
            this.id = id;
            this.name = name != null ? name : "";
            this.college = college != null ? college : "";
            this.department = department != null ? department : "";
            this.officeBlock = officeBlock != null ? officeBlock : "";
            this.officeRoom = officeRoom != null ? officeRoom : "";
            this.phone = phone != null ? phone : DEFAULT_PHONE;
            this.email = email != null ? email : DEFAULT_EMAIL;
            this.subjectsTaught = subjectsTaught != null ? subjectsTaught : "";
            this.targetYears = targetYears != null ? targetYears : "";
            this.consultationHours = consultationHours != null ? consultationHours : "";
        }
    }

    public static class CourseItem {
        public long id;
        public String programme;
        public int yearOfStudy;
        public int semester;
        public String courseCode;
        public String courseTitle;
        public double credits;
        public String courseStatus; // "Core", "Elective"
        public String category; // "Foundational", "Core Software", "Math/Stat", "Security", "IPT", "FYP", "Elective"

        public CourseItem(long id, String programme, int yearOfStudy, int semester,
                          String courseCode, String courseTitle, double credits,
                          String courseStatus, String category) {
            this.id = id;
            this.programme = programme != null ? programme : "BSc Software Engineering";
            this.yearOfStudy = yearOfStudy;
            this.semester = semester;
            this.courseCode = courseCode != null ? courseCode : "";
            this.courseTitle = courseTitle != null ? courseTitle : "";
            this.credits = credits;
            this.courseStatus = courseStatus != null ? courseStatus : "Core";
            this.category = category != null ? category : "Core Software";
        }
    }

    public static class TimetableItem {
        public long id;
        public String programme;
        public int yearOfStudy;
        public int semester;
        public String dayOfWeek;
        public String startTime;
        public String endTime;
        public String courseCode;
        public String courseName;
        public String sessionType; // "Theory", "Practical", "Tutorial", "Clinic"
        public String venue;
        public String lecturerName;

        public TimetableItem(long id, String programme, int yearOfStudy, int semester, String dayOfWeek,
                             String startTime, String endTime, String courseCode, String courseName,
                             String sessionType, String venue, String lecturerName) {
            this.id = id;
            this.programme = programme != null ? programme : "";
            this.yearOfStudy = yearOfStudy;
            this.semester = semester > 0 ? semester : 1;
            this.dayOfWeek = dayOfWeek != null ? dayOfWeek : "";
            this.startTime = startTime != null ? startTime : "";
            this.endTime = endTime != null ? endTime : "";
            this.courseCode = courseCode != null ? courseCode : "";
            this.courseName = courseName != null ? courseName : "";
            this.sessionType = sessionType != null ? sessionType : "Theory";
            this.venue = venue != null ? venue : "";
            this.lecturerName = lecturerName != null ? lecturerName : "";
        }

        // Backward compatibility constructor
        public TimetableItem(long id, String programme, int yearOfStudy, int semester, String dayOfWeek,
                             String startTime, String endTime, String courseCode, String courseName,
                             String venue, String lecturerName) {
            this(id, programme, yearOfStudy, semester, dayOfWeek, startTime, endTime, courseCode, courseName, "Theory", venue, lecturerName);
        }

        public TimetableItem(long id, String programme, int yearOfStudy, String dayOfWeek,
                             String startTime, String endTime, String courseCode, String courseName,
                             String venue, String lecturerName) {
            this(id, programme, yearOfStudy, 1, dayOfWeek, startTime, endTime, courseCode, courseName, "Theory", venue, lecturerName);
        }
    }

    public static class CalendarEventItem {
        public long id;
        public String academicYear; // "2024/2025", "2025/2026", "2026/2027"
        public String semester; // "Semester I", "Semester II", "Break", "ALL"
        public String eventName;
        public String category;
        public String startDate;
        public String endDate;
        public String description;

        public CalendarEventItem(long id, String academicYear, String semester,
                                 String eventName, String category,
                                 String startDate, String endDate, String description) {
            this.id = id;
            this.academicYear = academicYear != null ? academicYear : "2025/2026";
            this.semester = semester != null ? semester : "Semester I";
            this.eventName = eventName != null ? eventName : "";
            this.category = category != null ? category : "";
            this.startDate = startDate != null ? startDate : "";
            this.endDate = endDate != null ? endDate : "";
            this.description = description != null ? description : "";
        }

        // Backward compatibility constructor
        public CalendarEventItem(long id, String eventName, String category,
                                 String startDate, String endDate, String description) {
            this(id, "2025/2026", "Semester I", eventName, category, startDate, endDate, description);
        }
    }

    public static class StudentLeaderItem {
        public long id;
        public String name;
        public String role;
        public String college;
        public String officeOrHostel;
        public String roomNumber;
        public String phone;
        public String email;

        public StudentLeaderItem(long id, String name, String role, String college,
                                 String officeOrHostel, String roomNumber, String phone, String email) {
            this.id = id;
            this.name = name != null ? name : "";
            this.role = role != null ? role : "";
            this.college = college != null ? college : "";
            this.officeOrHostel = officeOrHostel != null ? officeOrHostel : "";
            this.roomNumber = roomNumber != null ? roomNumber : "";
            this.phone = phone != null ? phone : DEFAULT_PHONE;
            this.email = email != null ? email : DEFAULT_EMAIL;
        }
    }

    public static class CampusContactItem {
        public long id;
        public String departmentOrOffice;
        public String category;
        public String college;
        public String phoneNumber;
        public String email;
        public String officeLocation;
        public String description;
        public String keywords;

        public CampusContactItem(long id, String departmentOrOffice, String category,
                                 String college, String phoneNumber, String email,
                                 String officeLocation, String description, String keywords) {
            this.id = id;
            this.departmentOrOffice = departmentOrOffice != null ? departmentOrOffice : "";
            this.category = category != null ? category : "";
            this.college = college != null ? college : "ALL";
            this.phoneNumber = phoneNumber != null ? phoneNumber : DEFAULT_PHONE;
            this.email = email != null ? email : DEFAULT_EMAIL;
            this.officeLocation = officeLocation != null ? officeLocation : "";
            this.description = description != null ? description : "";
            this.keywords = keywords != null ? keywords : "";
        }
    }

    public static class LocationItem {
        public long id;
        public String name;
        public String college;
        public double latitude;
        public double longitude;
        public String category;
        public String description;
        public String keywords;

        public LocationItem(long id, String name, String college, double latitude, double longitude,
                            String category, String description, String keywords) {
            this.id = id;
            this.name = name != null ? name : "";
            this.college = college != null ? college : "ALL";
            this.latitude = latitude;
            this.longitude = longitude;
            this.category = category != null ? category : "";
            this.description = description != null ? description : "";
            this.keywords = keywords != null ? keywords : "";
        }
    }

    public static class ChatMessageItem {
        public long id;
        public String userEmail;
        public String sender;
        public String message;
        public String timestamp;

        public ChatMessageItem(long id, String userEmail, String sender, String message, String timestamp) {
            this.id = id;
            this.userEmail = userEmail;
            this.sender = sender;
            this.message = message;
            this.timestamp = timestamp;
        }
    }

    public static class FaqItem {
        public long id;
        public String category;
        public String keywords;
        public String question;
        public String answer;
        public String collegeSpecific;

        public FaqItem(long id, String category, String keywords, String question, String answer, String collegeSpecific) {
            this.id = id;
            this.category = category;
            this.keywords = keywords;
            this.question = question;
            this.answer = answer;
            this.collegeSpecific = collegeSpecific;
        }
    }

    // ==========================================
    // CRUD OPERATIONS: USERS
    // ==========================================

    public boolean insertUser(String name, String email, String password, String college) {
        if (email == null || password == null) return false;
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(COLUMN_NAME, name != null ? name.trim() : "Student");
            values.put(COLUMN_EMAIL, email.trim().toLowerCase());
            values.put(COLUMN_PASSWORD, password);
            values.put(COLUMN_COLLEGE, college != null ? college : "CIVE");

            long result = db.insert(TABLE_USERS, null, values);
            return result != -1;
        } catch (Exception e) {
            Log.e(TAG, "Error inserting user: " + e.getMessage(), e);
            return false;
        }
    }

    public boolean checkEmailExists(String email) {
        if (email == null) return false;
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = this.getReadableDatabase();
            cursor = db.query(
                    TABLE_USERS,
                    new String[]{COLUMN_ID},
                    "LOWER(" + COLUMN_EMAIL + ") = ?",
                    new String[]{email.trim().toLowerCase()},
                    null, null, null
            );
            return cursor != null && cursor.getCount() > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error checking email existence: " + e.getMessage(), e);
            return false;
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    public boolean checkUserCredentials(String email, String password) {
        if (email == null || password == null) return false;
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = this.getReadableDatabase();
            cursor = db.query(
                    TABLE_USERS,
                    new String[]{COLUMN_ID},
                    "LOWER(" + COLUMN_EMAIL + ") = ? AND " + COLUMN_PASSWORD + " = ?",
                    new String[]{email.trim().toLowerCase(), password},
                    null, null, null
            );
            return cursor != null && cursor.getCount() > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error validating user credentials: " + e.getMessage(), e);
            return false;
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    public String getUserName(String email) {
        if (email == null) return "Student";
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = this.getReadableDatabase();
            cursor = db.query(
                    TABLE_USERS,
                    new String[]{COLUMN_NAME},
                    "LOWER(" + COLUMN_EMAIL + ") = ?",
                    new String[]{email.trim().toLowerCase()},
                    null, null, null
            );
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(COLUMN_NAME);
                if (nameIndex != -1) {
                    return cursor.getString(nameIndex);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error retrieving user name: " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return "Student";
    }

    public String getUserCollege(String email) {
        if (email == null) return "CIVE";
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = this.getReadableDatabase();
            cursor = db.query(
                    TABLE_USERS,
                    new String[]{COLUMN_COLLEGE},
                    "LOWER(" + COLUMN_EMAIL + ") = ?",
                    new String[]{email.trim().toLowerCase()},
                    null, null, null
            );
            if (cursor != null && cursor.moveToFirst()) {
                int colIndex = cursor.getColumnIndex(COLUMN_COLLEGE);
                if (colIndex != -1) {
                    return cursor.getString(colIndex);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error retrieving user college: " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return "CIVE";
    }

    public boolean updateUserCollege(String email, String college) {
        if (email == null || college == null) return false;
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(COLUMN_COLLEGE, college);

            int rows = db.update(
                    TABLE_USERS,
                    values,
                    "LOWER(" + COLUMN_EMAIL + ") = ?",
                    new String[]{email.trim().toLowerCase()}
            );
            return rows > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error updating user college: " + e.getMessage(), e);
            return false;
        }
    }

    public boolean updatePassword(String email, String newPassword) {
        if (email == null || newPassword == null) return false;
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(COLUMN_PASSWORD, newPassword);

            int rows = db.update(
                    TABLE_USERS,
                    values,
                    "LOWER(" + COLUMN_EMAIL + ") = ?",
                    new String[]{email.trim().toLowerCase()}
            );
            return rows > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error updating password: " + e.getMessage(), e);
            return false;
        }
    }

    // ==========================================
    // CRUD OPERATIONS: CHAT HISTORY
    // ==========================================

    public boolean insertChatMessage(String userEmail, String sender, String message) {
        if (userEmail == null || message == null) return false;
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(COLUMN_CHAT_USER_EMAIL, userEmail.trim().toLowerCase());
            values.put(COLUMN_CHAT_SENDER, sender != null ? sender.trim().toLowerCase() : "user");
            values.put(COLUMN_CHAT_MESSAGE, message.trim());

            long result = db.insert(TABLE_CHAT_HISTORY, null, values);
            return result != -1;
        } catch (Exception e) {
            Log.e(TAG, "Error inserting chat message: " + e.getMessage(), e);
            return false;
        }
    }

    public List<ChatMessageItem> getChatHistory(String userEmail) {
        List<ChatMessageItem> history = new ArrayList<>();
        if (userEmail == null) return history;
        Cursor cursor = null;
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            cursor = db.query(
                    TABLE_CHAT_HISTORY,
                    new String[]{COLUMN_CHAT_ID, COLUMN_CHAT_USER_EMAIL, COLUMN_CHAT_SENDER, COLUMN_CHAT_MESSAGE, COLUMN_CHAT_TIMESTAMP},
                    "LOWER(" + COLUMN_CHAT_USER_EMAIL + ") = ?",
                    new String[]{userEmail.trim().toLowerCase()},
                    null, null,
                    COLUMN_CHAT_TIMESTAMP + " ASC"
            );

            if (cursor != null && cursor.moveToFirst()) {
                int idIdx = cursor.getColumnIndex(COLUMN_CHAT_ID);
                int emailIdx = cursor.getColumnIndex(COLUMN_CHAT_USER_EMAIL);
                int senderIdx = cursor.getColumnIndex(COLUMN_CHAT_SENDER);
                int msgIdx = cursor.getColumnIndex(COLUMN_CHAT_MESSAGE);
                int timeIdx = cursor.getColumnIndex(COLUMN_CHAT_TIMESTAMP);

                do {
                    long id = idIdx != -1 ? cursor.getLong(idIdx) : 0;
                    String email = emailIdx != -1 ? cursor.getString(emailIdx) : userEmail;
                    String sender = senderIdx != -1 ? cursor.getString(senderIdx) : "user";
                    String msg = msgIdx != -1 ? cursor.getString(msgIdx) : "";
                    String time = timeIdx != -1 ? cursor.getString(timeIdx) : "";

                    history.add(new ChatMessageItem(id, email, sender, msg, time));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error retrieving chat history: " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return history;
    }

    public boolean clearChatHistory(String userEmail) {
        if (userEmail == null) return false;
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            int rows = db.delete(
                    TABLE_CHAT_HISTORY,
                    "LOWER(" + COLUMN_CHAT_USER_EMAIL + ") = ?",
                    new String[]{userEmail.trim().toLowerCase()}
            );
            return rows >= 0;
        } catch (Exception e) {
            Log.e(TAG, "Error clearing chat history: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Safely executes a read-only SQL SELECT query against the SQLite database.
     * Used by the AI Text-to-SQL engine.
     * Enforces strict read-only restrictions, blocks access to private tables (users, chat_history),
     * prevents multi-statement injection, and caps row output to 50 rows.
     */
    public JSONArray executeReadOnlySql(String sql) {
        JSONArray results = new JSONArray();
        if (sql == null || sql.trim().isEmpty()) {
            JSONObject err = new JSONObject();
            try { err.put("error", "Empty query string provided."); } catch (Exception ignored) {}
            results.put(err);
            return results;
        }

        String trimmed = sql.trim();

        // 1. Enforce read-only prefix
        String lower = trimmed.toLowerCase(java.util.Locale.ROOT);
        if (!lower.startsWith("select") && !lower.startsWith("explain") && !lower.startsWith("with")) {
            JSONObject err = new JSONObject();
            try { err.put("error", "Security violation: Only SELECT queries are permitted."); } catch (Exception ignored) {}
            results.put(err);
            return results;
        }

        // 2. Reject forbidden mutation keywords
        String[] forbidden = {"insert ", "update ", "delete ", "drop ", "alter ", "create ", "pragma ", "attach ", "detach ", "truncate ", "replace "};
        for (String f : forbidden) {
            if (lower.contains(f)) {
                JSONObject err = new JSONObject();
                try { err.put("error", "Security violation: Query contains forbidden keyword '" + f.trim() + "'."); } catch (Exception ignored) {}
                results.put(err);
                return results;
            }
        }

        // 3. Block access to sensitive/private tables
        if (lower.contains("users") || lower.contains(TABLE_USERS) || lower.contains("chat_history") || lower.contains(TABLE_CHAT_HISTORY)) {
            JSONObject err = new JSONObject();
            try { err.put("error", "Access denied: Access to private user authentication or chat history tables is restricted."); } catch (Exception ignored) {}
            results.put(err);
            return results;
        }

        // 4. Prevent semicolon multi-statement injection
        int semiIdx = trimmed.indexOf(';');
        if (semiIdx != -1 && semiIdx < trimmed.length() - 1) {
            String remainder = trimmed.substring(semiIdx + 1).trim();
            if (!remainder.isEmpty()) {
                JSONObject err = new JSONObject();
                try { err.put("error", "Security violation: Multiple statements separated by semicolons are not allowed."); } catch (Exception ignored) {}
                results.put(err);
                return results;
            }
        }

        // Remove trailing semicolon if any
        if (trimmed.endsWith(";")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1).trim();
        }

        Cursor cursor = null;
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            cursor = db.rawQuery(trimmed, null);

            if (cursor != null && cursor.moveToFirst()) {
                String[] colNames = cursor.getColumnNames();
                int rowCount = 0;
                do {
                    JSONObject row = new JSONObject();
                    for (int i = 0; i < colNames.length; i++) {
                        String col = colNames[i];
                        if (cursor.isNull(i)) {
                            row.put(col, JSONObject.NULL);
                        } else {
                            switch (cursor.getType(i)) {
                                case Cursor.FIELD_TYPE_INTEGER:
                                    row.put(col, cursor.getLong(i));
                                    break;
                                case Cursor.FIELD_TYPE_FLOAT:
                                    row.put(col, cursor.getDouble(i));
                                    break;
                                case Cursor.FIELD_TYPE_STRING:
                                default:
                                    row.put(col, cursor.getString(i));
                                    break;
                            }
                        }
                    }
                    results.put(row);
                    rowCount++;
                    if (rowCount >= 50) {
                        break; // Cap at 50 rows to protect mobile memory
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error executing read-only SQL: " + e.getMessage(), e);
            JSONObject err = new JSONObject();
            try {
                err.put("error", "SQL error: " + e.getMessage());
            } catch (Exception ignored) {}
            results.put(err);
        } finally {
            if (cursor != null) cursor.close();
        }

        return results;
    }

    // ==========================================
    // CRUD: PROFESSORS
    // ==========================================

    public boolean insertProfessor(SQLiteDatabase db, String name, String college, String department,
                                   String officeBlock, String officeRoom, String phone, String email,
                                   String subjectsTaught, String targetYears, String consultationHours) {
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_PROF_NAME, name);
            values.put(COLUMN_PROF_COLLEGE, college != null ? college : "CIVE");
            values.put(COLUMN_PROF_DEPARTMENT, department != null ? department : "");
            values.put(COLUMN_PROF_OFFICE_BLOCK, officeBlock != null ? officeBlock : "");
            values.put(COLUMN_PROF_OFFICE_ROOM, officeRoom != null ? officeRoom : "");
            values.put(COLUMN_PROF_PHONE, phone != null ? phone : DEFAULT_PHONE);
            values.put(COLUMN_PROF_EMAIL, email != null ? email : DEFAULT_EMAIL);
            values.put(COLUMN_PROF_SUBJECTS, subjectsTaught != null ? subjectsTaught : "");
            values.put(COLUMN_PROF_YEARS, targetYears != null ? targetYears : "");
            values.put(COLUMN_PROF_CONSULTATION, consultationHours != null ? consultationHours : "");

            long result = db.insert(TABLE_PROFESSORS, null, values);
            return result != -1;
        } catch (Exception e) {
            Log.e(TAG, "Error inserting professor: " + e.getMessage(), e);
            return false;
        }
    }

    public List<ProfessorItem> searchProfessors(String query, String college) {
        List<ProfessorItem> list = new ArrayList<>();
        Cursor cursor = null;
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            String selection = null;
            List<String> argsList = new ArrayList<>();

            if (query != null && !query.trim().isEmpty()) {
                String cleanQ = "%" + query.trim() + "%";
                selection = "(" + COLUMN_PROF_NAME + " LIKE ? OR "
                        + COLUMN_PROF_SUBJECTS + " LIKE ? OR "
                        + COLUMN_PROF_DEPARTMENT + " LIKE ? OR "
                        + COLUMN_PROF_OFFICE_BLOCK + " LIKE ?)";
                argsList.add(cleanQ);
                argsList.add(cleanQ);
                argsList.add(cleanQ);
                argsList.add(cleanQ);
            }

            if (college != null && !college.trim().isEmpty() && !"ALL".equalsIgnoreCase(college)) {
                if (selection != null) {
                    selection += " AND (" + COLUMN_PROF_COLLEGE + " = ? OR " + COLUMN_PROF_COLLEGE + " = 'ALL')";
                } else {
                    selection = "(" + COLUMN_PROF_COLLEGE + " = ? OR " + COLUMN_PROF_COLLEGE + " = 'ALL')";
                }
                argsList.add(college.trim());
            }

            String[] args = argsList.isEmpty() ? null : argsList.toArray(new String[0]);
            cursor = db.query(TABLE_PROFESSORS, null, selection, args, null, null, COLUMN_PROF_NAME + " ASC");

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    list.add(new ProfessorItem(
                            cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_PROF_ID)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PROF_NAME)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PROF_COLLEGE)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PROF_DEPARTMENT)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PROF_OFFICE_BLOCK)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PROF_OFFICE_ROOM)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PROF_PHONE)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PROF_EMAIL)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PROF_SUBJECTS)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PROF_YEARS)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PROF_CONSULTATION))
                    ));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error searching professors: " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return list;
    }

    // ==========================================
    // CRUD: COURSES & CURRICULUM
    // ==========================================

    public boolean insertCourse(SQLiteDatabase db, String programme, int yearOfStudy, int semester,
                                String courseCode, String courseTitle, double credits,
                                String courseStatus, String category) {
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_COURSE_PROGRAMME, programme != null ? programme : "BSc Software Engineering");
            values.put(COLUMN_COURSE_YEAR, yearOfStudy);
            values.put(COLUMN_COURSE_SEMESTER, semester);
            values.put(COLUMN_COURSE_CODE, courseCode != null ? courseCode : "");
            values.put(COLUMN_COURSE_TITLE, courseTitle != null ? courseTitle : "");
            values.put(COLUMN_COURSE_CREDITS, credits);
            values.put(COLUMN_COURSE_STATUS, courseStatus != null ? courseStatus : "Core");
            values.put(COLUMN_COURSE_CATEGORY, category != null ? category : "Core Software");

            long result = db.insert(TABLE_COURSES, null, values);
            return result != -1;
        } catch (Exception e) {
            Log.e(TAG, "Error inserting course: " + e.getMessage(), e);
            return false;
        }
    }

    public List<CourseItem> getCourses(String programme, int yearOfStudy, int semester) {
        List<CourseItem> list = new ArrayList<>();
        Cursor cursor = null;
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            StringBuilder selection = new StringBuilder();
            List<String> argsList = new ArrayList<>();

            if (programme != null && !programme.trim().isEmpty()) {
                selection.append(COLUMN_COURSE_PROGRAMME).append(" LIKE ?");
                argsList.add("%" + programme.trim() + "%");
            }

            if (yearOfStudy > 0) {
                if (selection.length() > 0) selection.append(" AND ");
                selection.append(COLUMN_COURSE_YEAR).append(" = ?");
                argsList.add(String.valueOf(yearOfStudy));
            }

            if (semester > 0) {
                if (selection.length() > 0) selection.append(" AND ");
                selection.append(COLUMN_COURSE_SEMESTER).append(" = ?");
                argsList.add(String.valueOf(semester));
            }

            String where = selection.length() > 0 ? selection.toString() : null;
            String[] args = argsList.isEmpty() ? null : argsList.toArray(new String[0]);

            cursor = db.query(TABLE_COURSES, null, where, args, null, null,
                    COLUMN_COURSE_YEAR + " ASC, " + COLUMN_COURSE_SEMESTER + " ASC, " + COLUMN_COURSE_CODE + " ASC");

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    list.add(new CourseItem(
                            cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_COURSE_ID)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COURSE_PROGRAMME)),
                            cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_COURSE_YEAR)),
                            cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_COURSE_SEMESTER)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COURSE_CODE)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COURSE_TITLE)),
                            cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_COURSE_CREDITS)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COURSE_STATUS)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COURSE_CATEGORY))
                    ));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting courses: " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return list;
    }

    public List<CourseItem> searchCourses(String query, String programme) {
        List<CourseItem> list = new ArrayList<>();
        Cursor cursor = null;
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            StringBuilder selection = new StringBuilder();
            List<String> argsList = new ArrayList<>();

            if (query != null && !query.trim().isEmpty()) {
                String cleanQ = "%" + query.trim() + "%";
                selection.append("(")
                        .append(COLUMN_COURSE_CODE).append(" LIKE ? OR ")
                        .append(COLUMN_COURSE_TITLE).append(" LIKE ? OR ")
                        .append(COLUMN_COURSE_CATEGORY).append(" LIKE ? OR ")
                        .append(COLUMN_COURSE_STATUS).append(" LIKE ?)");
                argsList.add(cleanQ);
                argsList.add(cleanQ);
                argsList.add(cleanQ);
                argsList.add(cleanQ);
            }

            if (programme != null && !programme.trim().isEmpty()) {
                if (selection.length() > 0) selection.append(" AND ");
                selection.append(COLUMN_COURSE_PROGRAMME).append(" LIKE ?");
                argsList.add("%" + programme.trim() + "%");
            }

            String where = selection.length() > 0 ? selection.toString() : null;
            String[] args = argsList.isEmpty() ? null : argsList.toArray(new String[0]);

            cursor = db.query(TABLE_COURSES, null, where, args, null, null,
                    COLUMN_COURSE_YEAR + " ASC, " + COLUMN_COURSE_SEMESTER + " ASC, " + COLUMN_COURSE_CODE + " ASC");

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    list.add(new CourseItem(
                            cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_COURSE_ID)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COURSE_PROGRAMME)),
                            cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_COURSE_YEAR)),
                            cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_COURSE_SEMESTER)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COURSE_CODE)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COURSE_TITLE)),
                            cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_COURSE_CREDITS)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COURSE_STATUS)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COURSE_CATEGORY))
                    ));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error searching courses: " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return list;
    }

    // ==========================================
    // CRUD: TIMETABLES
    // ==========================================

    public boolean insertTimetableEntry(SQLiteDatabase db, String programme, int yearOfStudy, int semester,
                                        String dayOfWeek, String startTime, String endTime,
                                        String courseCode, String courseName, String sessionType,
                                        String venue, String lecturerName) {
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_TIME_PROGRAMME, programme != null ? programme : "BSc Software Engineering");
            values.put(COLUMN_TIME_YEAR, yearOfStudy);
            values.put(COLUMN_TIME_SEMESTER, semester > 0 ? semester : 1);
            values.put(COLUMN_TIME_DAY, dayOfWeek != null ? dayOfWeek : "Monday");
            values.put(COLUMN_TIME_START, startTime != null ? startTime : "08:00");
            values.put(COLUMN_TIME_END, endTime != null ? endTime : "10:00");
            values.put(COLUMN_TIME_CODE, courseCode != null ? courseCode : "");
            values.put(COLUMN_TIME_NAME, courseName != null ? courseName : "");
            values.put(COLUMN_TIME_SESSION, sessionType != null ? sessionType : "Theory");
            values.put(COLUMN_TIME_VENUE, venue != null ? venue : "");
            values.put(COLUMN_TIME_LECTURER, lecturerName != null ? lecturerName : "");

            long result = db.insert(TABLE_TIMETABLES, null, values);
            return result != -1;
        } catch (Exception e) {
            Log.e(TAG, "Error inserting timetable entry: " + e.getMessage(), e);
            return false;
        }
    }

    public boolean insertTimetableEntry(SQLiteDatabase db, String programme, int yearOfStudy, int semester,
                                        String dayOfWeek, String startTime, String endTime,
                                        String courseCode, String courseName, String venue, String lecturerName) {
        return insertTimetableEntry(db, programme, yearOfStudy, semester, dayOfWeek, startTime, endTime, courseCode, courseName, "Theory", venue, lecturerName);
    }

    public boolean insertTimetableEntry(SQLiteDatabase db, String programme, int yearOfStudy,
                                        String dayOfWeek, String startTime, String endTime,
                                        String courseCode, String courseName, String venue, String lecturerName) {
        return insertTimetableEntry(db, programme, yearOfStudy, 1, dayOfWeek, startTime, endTime, courseCode, courseName, "Theory", venue, lecturerName);
    }

    public List<TimetableItem> getTimetable(String programme, String dayOfWeek, int yearOfStudy) {
        return getTimetable(programme, dayOfWeek, yearOfStudy, 0);
    }

    public List<TimetableItem> getTimetable(String programme, String dayOfWeek, int yearOfStudy, int semester) {
        List<TimetableItem> list = new ArrayList<>();
        Cursor cursor = null;
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            StringBuilder selection = new StringBuilder();
            List<String> argsList = new ArrayList<>();

            if (programme != null && !programme.trim().isEmpty()) {
                selection.append(COLUMN_TIME_PROGRAMME).append(" LIKE ?");
                argsList.add("%" + programme.trim() + "%");
            }

            if (semester > 0) {
                if (selection.length() > 0) selection.append(" AND ");
                selection.append(COLUMN_TIME_SEMESTER).append(" = ?");
                argsList.add(String.valueOf(semester));
            }

            if (dayOfWeek != null && !dayOfWeek.trim().isEmpty() && !"ALL".equalsIgnoreCase(dayOfWeek)) {
                String[] allDays = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
                String lower = dayOfWeek.trim().toLowerCase(java.util.Locale.ROOT);
                List<String> matchedDays = new ArrayList<>();

                if (lower.contains(" to ") || lower.contains(" - ") || lower.contains(" through ")) {
                    String startDay = null;
                    String endDay = null;
                    for (String d : allDays) {
                        String dl = d.toLowerCase(java.util.Locale.ROOT);
                        if (lower.contains(dl + " to") || lower.contains(dl + " -") || lower.contains(dl + " through") || lower.startsWith(dl)) {
                            if (startDay == null) startDay = d;
                        }
                        if (lower.contains("to " + dl) || lower.contains("- " + dl) || lower.contains("through " + dl) || lower.endsWith(dl)) {
                            endDay = d;
                        }
                    }
                    if (startDay != null && endDay != null) {
                        int sIdx = -1, eIdx = -1;
                        for (int i = 0; i < allDays.length; i++) {
                            if (allDays[i].equalsIgnoreCase(startDay)) sIdx = i;
                            if (allDays[i].equalsIgnoreCase(endDay)) eIdx = i;
                        }
                        if (sIdx != -1 && eIdx != -1 && sIdx <= eIdx) {
                            for (int i = sIdx; i <= eIdx; i++) {
                                matchedDays.add(allDays[i]);
                            }
                        }
                    }
                }

                if (matchedDays.isEmpty()) {
                    for (String d : allDays) {
                        if (lower.contains(d.toLowerCase(java.util.Locale.ROOT))) {
                            matchedDays.add(d);
                        }
                    }
                }

                if (selection.length() > 0) selection.append(" AND ");
                if (!matchedDays.isEmpty()) {
                    selection.append(COLUMN_TIME_DAY).append(" IN (");
                    for (int i = 0; i < matchedDays.size(); i++) {
                        if (i > 0) selection.append(", ");
                        selection.append("?");
                        argsList.add(matchedDays.get(i));
                    }
                    selection.append(")");
                } else {
                    selection.append("LOWER(").append(COLUMN_TIME_DAY).append(") = ?");
                    argsList.add(lower);
                }
            }

            if (yearOfStudy > 0) {
                if (selection.length() > 0) selection.append(" AND ");
                selection.append(COLUMN_TIME_YEAR).append(" = ?");
                argsList.add(String.valueOf(yearOfStudy));
            }

            String where = selection.length() > 0 ? selection.toString() : null;
            String[] args = argsList.isEmpty() ? null : argsList.toArray(new String[0]);

            String orderBy = COLUMN_TIME_SEMESTER + " ASC, CASE LOWER(" + COLUMN_TIME_DAY + ") "
                    + "WHEN 'monday' THEN 1 "
                    + "WHEN 'tuesday' THEN 2 "
                    + "WHEN 'wednesday' THEN 3 "
                    + "WHEN 'thursday' THEN 4 "
                    + "WHEN 'friday' THEN 5 "
                    + "WHEN 'saturday' THEN 6 "
                    + "WHEN 'sunday' THEN 7 "
                    + "ELSE 8 END, " + COLUMN_TIME_START + " ASC";

            cursor = db.query(TABLE_TIMETABLES, null, where, args, null, null, orderBy);

            if (cursor != null && cursor.moveToFirst()) {
                int semCol = cursor.getColumnIndex(COLUMN_TIME_SEMESTER);
                int sessCol = cursor.getColumnIndex(COLUMN_TIME_SESSION);
                do {
                    int semVal = semCol != -1 ? cursor.getInt(semCol) : 1;
                    String sessionVal = (sessCol != -1 && !cursor.isNull(sessCol)) ? cursor.getString(sessCol) : "Theory";
                    list.add(new TimetableItem(
                            cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TIME_ID)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIME_PROGRAMME)),
                            cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TIME_YEAR)),
                            semVal,
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIME_DAY)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIME_START)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIME_END)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIME_CODE)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIME_NAME)),
                            sessionVal,
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIME_VENUE)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIME_LECTURER))
                    ));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting timetable: " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return list;
    }

    // ==========================================
    // CRUD: ACADEMIC CALENDAR
    // ==========================================

    public boolean insertCalendarEvent(SQLiteDatabase db, String academicYear, String semester,
                                       String eventName, String category,
                                       String startDate, String endDate, String description) {
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_CAL_YEAR, academicYear != null ? academicYear : "2025/2026");
            values.put(COLUMN_CAL_SEMESTER, semester != null ? semester : "Semester I");
            values.put(COLUMN_CAL_EVENT, eventName != null ? eventName : "");
            values.put(COLUMN_CAL_CATEGORY, category != null ? category : "ACADEMIC");
            values.put(COLUMN_CAL_START, startDate != null ? startDate : "");
            values.put(COLUMN_CAL_END, endDate != null ? endDate : "");
            values.put(COLUMN_CAL_DESCRIPTION, description != null ? description : "");

            long result = db.insert(TABLE_ACADEMIC_CALENDAR, null, values);
            return result != -1;
        } catch (Exception e) {
            Log.e(TAG, "Error inserting calendar event: " + e.getMessage(), e);
            return false;
        }
    }

    public boolean insertCalendarEvent(SQLiteDatabase db, String eventName, String category,
                                       String startDate, String endDate, String description) {
        return insertCalendarEvent(db, "2025/2026", "Semester I", eventName, category, startDate, endDate, description);
    }

    public List<CalendarEventItem> searchCalendarEvents(String query, String academicYear, boolean includePast) {
        List<CalendarEventItem> list = new ArrayList<>();
        Cursor cursor = null;
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            StringBuilder selection = new StringBuilder();
            List<String> argsList = new ArrayList<>();

            if (query != null && !query.trim().isEmpty()) {
                String cleanQ = "%" + query.trim() + "%";
                selection.append("(")
                        .append(COLUMN_CAL_EVENT).append(" LIKE ? OR ")
                        .append(COLUMN_CAL_CATEGORY).append(" LIKE ? OR ")
                        .append(COLUMN_CAL_DESCRIPTION).append(" LIKE ? OR ")
                        .append(COLUMN_CAL_SEMESTER).append(" LIKE ?)");
                argsList.add(cleanQ);
                argsList.add(cleanQ);
                argsList.add(cleanQ);
                argsList.add(cleanQ);
            }

            if (academicYear != null && !academicYear.trim().isEmpty() && !"ALL".equalsIgnoreCase(academicYear)) {
                if (selection.length() > 0) selection.append(" AND ");
                selection.append(COLUMN_CAL_YEAR).append(" = ?");
                argsList.add(academicYear.trim());
            } else if (!includePast) {
                // By default, exclude past academic years (e.g. 2024/2025)
                if (selection.length() > 0) selection.append(" AND ");
                selection.append(COLUMN_CAL_YEAR).append(" != '2024/2025'");
            }

            String where = selection.length() > 0 ? selection.toString() : null;
            String[] args = argsList.isEmpty() ? null : argsList.toArray(new String[0]);

            String orderBy = "CASE "
                    + "WHEN " + COLUMN_CAL_YEAR + " = '2025/2026' THEN 1 "
                    + "WHEN " + COLUMN_CAL_YEAR + " = '2026/2027' THEN 2 "
                    + "ELSE 3 END, " + COLUMN_CAL_START + " ASC";

            cursor = db.query(TABLE_ACADEMIC_CALENDAR, null, where, args, null, null, orderBy);

            if (cursor != null && cursor.moveToFirst()) {
                int yrCol = cursor.getColumnIndex(COLUMN_CAL_YEAR);
                int semCol = cursor.getColumnIndex(COLUMN_CAL_SEMESTER);
                do {
                    String yrVal = (yrCol != -1 && !cursor.isNull(yrCol)) ? cursor.getString(yrCol) : "2025/2026";
                    String semVal = (semCol != -1 && !cursor.isNull(semCol)) ? cursor.getString(semCol) : "Semester I";
                    list.add(new CalendarEventItem(
                            cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_CAL_ID)),
                            yrVal,
                            semVal,
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CAL_EVENT)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CAL_CATEGORY)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CAL_START)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CAL_END)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CAL_DESCRIPTION))
                    ));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error searching calendar events: " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return list;
    }

    public List<CalendarEventItem> searchCalendarEvents(String query, String academicYear) {
        boolean includePast = (academicYear != null && !"ALL".equalsIgnoreCase(academicYear))
                || (query != null && (query.toLowerCase().contains("past")
                || query.toLowerCase().contains("previous")
                || query.toLowerCase().contains("history")
                || query.toLowerCase().contains("last year")
                || query.contains("2024")));
        return searchCalendarEvents(query, academicYear, includePast);
    }

    public List<CalendarEventItem> searchCalendarEvents(String query) {
        return searchCalendarEvents(query, null);
    }

    /**
     * Converts ISO YYYY-MM-DD to a human-friendly format like "09 Mar 2026".
     */
    public static String formatFriendlyDate(String isoDate) {
        if (isoDate == null || isoDate.trim().isEmpty()) return "";
        isoDate = isoDate.trim();
        try {
            if (isoDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
                java.text.SimpleDateFormat inFmt = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US);
                java.text.SimpleDateFormat outFmt = new java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.US);
                java.util.Date d = inFmt.parse(isoDate);
                if (d != null) {
                    return outFmt.format(d);
                }
            }
        } catch (Exception ignored) {}
        return isoDate;
    }

    /**
     * Converts a start and end ISO date range to a readable format like:
     * "09 Mar – 20 Mar 2026" or "30 Jun – 11 Jul 2025" or "22 Dec 2025 – 04 Jan 2026".
     */
    public static String formatFriendlyDateRange(String startIso, String endIso) {
        if (startIso == null || startIso.trim().isEmpty()) {
            return formatFriendlyDate(endIso);
        }
        if (endIso == null || endIso.trim().isEmpty()) {
            return formatFriendlyDate(startIso);
        }
        startIso = startIso.trim();
        endIso = endIso.trim();
        if (startIso.equals(endIso)) {
            return formatFriendlyDate(startIso);
        }

        try {
            if (startIso.matches("\\d{4}-\\d{2}-\\d{2}") && endIso.matches("\\d{4}-\\d{2}-\\d{2}")) {
                java.text.SimpleDateFormat inFmt = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US);
                java.util.Date dStart = inFmt.parse(startIso);
                java.util.Date dEnd = inFmt.parse(endIso);
                if (dStart != null && dEnd != null) {
                    java.util.Calendar cStart = java.util.Calendar.getInstance();
                    cStart.setTime(dStart);
                    java.util.Calendar cEnd = java.util.Calendar.getInstance();
                    cEnd.setTime(dEnd);

                    java.text.SimpleDateFormat dayMonthFmt = new java.text.SimpleDateFormat("dd MMM", java.util.Locale.US);
                    java.text.SimpleDateFormat fullFmt = new java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.US);

                    if (cStart.get(java.util.Calendar.YEAR) == cEnd.get(java.util.Calendar.YEAR)) {
                        return dayMonthFmt.format(dStart) + " – " + fullFmt.format(dEnd);
                    } else {
                        return fullFmt.format(dStart) + " – " + fullFmt.format(dEnd);
                    }
                }
            }
        } catch (Exception ignored) {}

        String s = formatFriendlyDate(startIso);
        String e = formatFriendlyDate(endIso);
        return s + " – " + e;
    }

    // ==========================================
    // CRUD: STUDENT LEADERS & PREFECTS
    // ==========================================

    public boolean insertStudentLeader(SQLiteDatabase db, String name, String role, String college,
                                       String officeOrHostel, String roomNumber, String phone, String email) {
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_LEAD_NAME, name != null ? name : "");
            values.put(COLUMN_LEAD_ROLE, role != null ? role : "");
            values.put(COLUMN_LEAD_COLLEGE, college != null ? college : "ALL");
            values.put(COLUMN_LEAD_LOCATION, officeOrHostel != null ? officeOrHostel : "");
            values.put(COLUMN_LEAD_ROOM, roomNumber != null ? roomNumber : "");
            values.put(COLUMN_LEAD_PHONE, phone != null ? phone : DEFAULT_PHONE);
            values.put(COLUMN_LEAD_EMAIL, email != null ? email : DEFAULT_EMAIL);

            long result = db.insert(TABLE_STUDENT_LEADERS, null, values);
            return result != -1;
        } catch (Exception e) {
            Log.e(TAG, "Error inserting student leader: " + e.getMessage(), e);
            return false;
        }
    }

    public List<StudentLeaderItem> searchStudentLeaders(String query, String college) {
        List<StudentLeaderItem> list = new ArrayList<>();
        Cursor cursor = null;
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            StringBuilder selection = new StringBuilder();
            List<String> argsList = new ArrayList<>();

            if (query != null && !query.trim().isEmpty()) {
                String cleanQ = "%" + query.trim() + "%";
                selection.append("(")
                        .append(COLUMN_LEAD_NAME).append(" LIKE ? OR ")
                        .append(COLUMN_LEAD_ROLE).append(" LIKE ? OR ")
                        .append(COLUMN_LEAD_LOCATION).append(" LIKE ?)");
                argsList.add(cleanQ);
                argsList.add(cleanQ);
                argsList.add(cleanQ);
            }

            if (college != null && !college.trim().isEmpty() && !"ALL".equalsIgnoreCase(college)) {
                if (selection.length() > 0) selection.append(" AND ");
                selection.append("(")
                        .append(COLUMN_LEAD_COLLEGE).append(" = ? OR ")
                        .append(COLUMN_LEAD_COLLEGE).append(" = 'ALL')");
                argsList.add(college.trim());
            }

            String where = selection.length() > 0 ? selection.toString() : null;
            String[] args = argsList.isEmpty() ? null : argsList.toArray(new String[0]);

            cursor = db.query(TABLE_STUDENT_LEADERS, null, where, args, null, null, COLUMN_LEAD_ROLE + " ASC");

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    list.add(new StudentLeaderItem(
                            cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_LEAD_ID)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LEAD_NAME)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LEAD_ROLE)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LEAD_COLLEGE)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LEAD_LOCATION)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LEAD_ROOM)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LEAD_PHONE)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LEAD_EMAIL))
                    ));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error searching student leaders: " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return list;
    }

    // ==========================================
    // CRUD: CAMPUS CONTACTS & DIRECTORY
    // ==========================================

    public boolean insertCampusContact(SQLiteDatabase db, String dept, String category, String college,
                                       String phone, String email, String location, String desc, String keywords) {
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_CON_DEPARTMENT, dept != null ? dept : "");
            values.put(COLUMN_CON_CATEGORY, category != null ? category : "GENERAL");
            values.put(COLUMN_CON_COLLEGE, college != null ? college : "ALL");
            values.put(COLUMN_CON_PHONE, phone != null ? phone : DEFAULT_PHONE);
            values.put(COLUMN_CON_EMAIL, email != null ? email : DEFAULT_EMAIL);
            values.put(COLUMN_CON_LOCATION, location != null ? location : "");
            values.put(COLUMN_CON_DESCRIPTION, desc != null ? desc : "");
            values.put(COLUMN_CON_KEYWORDS, keywords != null ? keywords : "");

            long result = db.insert(TABLE_CAMPUS_CONTACTS, null, values);
            return result != -1;
        } catch (Exception e) {
            Log.e(TAG, "Error inserting campus contact: " + e.getMessage(), e);
            return false;
        }
    }

    public List<CampusContactItem> searchCampusContacts(String query, String college) {
        List<CampusContactItem> list = new ArrayList<>();
        Cursor cursor = null;
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            StringBuilder selection = new StringBuilder();
            List<String> argsList = new ArrayList<>();

            if (query != null && !query.trim().isEmpty()) {
                String cleanQ = "%" + query.trim() + "%";
                selection.append("(")
                        .append(COLUMN_CON_DEPARTMENT).append(" LIKE ? OR ")
                        .append(COLUMN_CON_CATEGORY).append(" LIKE ? OR ")
                        .append(COLUMN_CON_KEYWORDS).append(" LIKE ?)");
                argsList.add(cleanQ);
                argsList.add(cleanQ);
                argsList.add(cleanQ);
            }

            if (college != null && !college.trim().isEmpty() && !"ALL".equalsIgnoreCase(college)) {
                if (selection.length() > 0) selection.append(" AND ");
                selection.append("(")
                        .append(COLUMN_CON_COLLEGE).append(" = ? OR ")
                        .append(COLUMN_CON_COLLEGE).append(" = 'ALL')");
                argsList.add(college.trim());
            }

            String where = selection.length() > 0 ? selection.toString() : null;
            String[] args = argsList.isEmpty() ? null : argsList.toArray(new String[0]);

            cursor = db.query(TABLE_CAMPUS_CONTACTS, null, where, args, null, null, COLUMN_CON_CATEGORY + " ASC");

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    list.add(new CampusContactItem(
                            cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_CON_ID)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CON_DEPARTMENT)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CON_CATEGORY)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CON_COLLEGE)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CON_PHONE)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CON_EMAIL)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CON_LOCATION)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CON_DESCRIPTION)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CON_KEYWORDS))
                    ));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error searching campus contacts: " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return list;
    }

    // ==========================================
    // CRUD: CAMPUS LOCATIONS
    // ==========================================

    public boolean insertLocation(SQLiteDatabase db, String name, String college, double lat, double lng,
                                  String category, String desc, String keywords) {
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_LOC_NAME, name);
            values.put(COLUMN_LOC_COLLEGE, college != null ? college : "ALL");
            values.put(COLUMN_LOC_LATITUDE, lat);
            values.put(COLUMN_LOC_LONGITUDE, lng);
            values.put(COLUMN_LOC_CATEGORY, category);
            values.put(COLUMN_LOC_DESCRIPTION, desc);
            values.put(COLUMN_LOC_KEYWORDS, keywords);

            long result = db.insert(TABLE_CAMPUS_LOCATIONS, null, values);
            return result != -1;
        } catch (Exception e) {
            Log.e(TAG, "Error inserting location: " + e.getMessage(), e);
            return false;
        }
    }

    public List<LocationItem> searchLocations(String query, String college) {
        List<LocationItem> list = new ArrayList<>();
        Cursor cursor = null;
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            StringBuilder selection = new StringBuilder();
            List<String> argsList = new ArrayList<>();

            if (query != null && !query.trim().isEmpty()) {
                String cleanQ = "%" + query.trim() + "%";
                selection.append("(")
                        .append(COLUMN_LOC_NAME).append(" LIKE ? OR ")
                        .append(COLUMN_LOC_CATEGORY).append(" LIKE ? OR ")
                        .append(COLUMN_LOC_KEYWORDS).append(" LIKE ?)");
                argsList.add(cleanQ);
                argsList.add(cleanQ);
                argsList.add(cleanQ);
            }

            if (college != null && !college.trim().isEmpty() && !"ALL".equalsIgnoreCase(college)) {
                if (selection.length() > 0) selection.append(" AND ");
                selection.append("(")
                        .append(COLUMN_LOC_COLLEGE).append(" = ? OR ")
                        .append(COLUMN_LOC_COLLEGE).append(" = 'ALL')");
                argsList.add(college.trim());
            }

            String where = selection.length() > 0 ? selection.toString() : null;
            String[] args = argsList.isEmpty() ? null : argsList.toArray(new String[0]);

            cursor = db.query(TABLE_CAMPUS_LOCATIONS, null, where, args, null, null, COLUMN_LOC_NAME + " ASC");

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    list.add(new LocationItem(
                            cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_LOC_ID)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOC_NAME)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOC_COLLEGE)),
                            cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LOC_LATITUDE)),
                            cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LOC_LONGITUDE)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOC_CATEGORY)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOC_DESCRIPTION)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOC_KEYWORDS))
                    ));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error searching locations: " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return list;
    }

    // ==========================================
    // CRUD: CAMPUS FAQS (KNOWLEDGE BASE)
    // ==========================================

    public boolean insertFaq(SQLiteDatabase db, String category, String keywords, String question, String answer, String collegeSpecific) {
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_FAQ_CATEGORY, category != null ? category : "general");
            values.put(COLUMN_FAQ_KEYWORDS, keywords != null ? keywords : "");
            values.put(COLUMN_FAQ_QUESTION, question != null ? question : "");
            values.put(COLUMN_FAQ_ANSWER, answer != null ? answer : "");
            values.put(COLUMN_FAQ_COLLEGE, collegeSpecific != null ? collegeSpecific : "ALL");

            long result = db.insert(TABLE_CAMPUS_FAQS, null, values);
            return result != -1;
        } catch (Exception e) {
            Log.e(TAG, "Error inserting FAQ: " + e.getMessage(), e);
            return false;
        }
    }

    public FaqItem findFaqItem(String userQuery, String userCollege) {
        if (userQuery == null || userQuery.trim().isEmpty()) return null;

        String query = userQuery.toLowerCase().trim();
        Cursor cursor = null;

        try {
            SQLiteDatabase db = this.getReadableDatabase();
            cursor = db.query(
                    TABLE_CAMPUS_FAQS,
                    new String[]{COLUMN_FAQ_ID, COLUMN_FAQ_CATEGORY, COLUMN_FAQ_KEYWORDS, COLUMN_FAQ_QUESTION, COLUMN_FAQ_ANSWER, COLUMN_FAQ_COLLEGE},
                    null, null, null, null,
                    COLUMN_FAQ_ID + " ASC"
            );

            if (cursor != null && cursor.moveToFirst()) {
                int idIdx = cursor.getColumnIndex(COLUMN_FAQ_ID);
                int catIdx = cursor.getColumnIndex(COLUMN_FAQ_CATEGORY);
                int keyIdx = cursor.getColumnIndex(COLUMN_FAQ_KEYWORDS);
                int qIdx = cursor.getColumnIndex(COLUMN_FAQ_QUESTION);
                int ansIdx = cursor.getColumnIndex(COLUMN_FAQ_ANSWER);
                int colIdx = cursor.getColumnIndex(COLUMN_FAQ_COLLEGE);

                FaqItem bestMatch = null;

                do {
                    long id = idIdx != -1 ? cursor.getLong(idIdx) : 0;
                    String category = catIdx != -1 ? cursor.getString(catIdx) : "general";
                    String keywordsStr = keyIdx != -1 ? cursor.getString(keyIdx) : null;
                    String question = qIdx != -1 ? cursor.getString(qIdx) : "";
                    String answer = ansIdx != -1 ? cursor.getString(ansIdx) : null;
                    String college = colIdx != -1 ? cursor.getString(colIdx) : "ALL";

                    boolean collegeMatches = "ALL".equalsIgnoreCase(college) ||
                            (userCollege != null && userCollege.equalsIgnoreCase(college));

                    if (keywordsStr != null && answer != null) {
                        String[] keywords = keywordsStr.split(",");
                        for (String kw : keywords) {
                            String trimmedKw = kw.trim().toLowerCase();
                            if (!trimmedKw.isEmpty() && query.contains(trimmedKw)) {
                                FaqItem item = new FaqItem(id, category, keywordsStr, question, answer, college);
                                if (collegeMatches) {
                                    return item;
                                } else if (bestMatch == null) {
                                    bestMatch = item;
                                }
                            }
                        }
                    }
                } while (cursor.moveToNext());

                return bestMatch;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error finding FAQ item in SQLite: " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
        }

        return null;
    }

    public String findFaqAnswer(String userQuery, String userCollege) {
        FaqItem item = findFaqItem(userQuery, userCollege);
        return item != null ? item.answer : null;
    }

    // ==========================================
    // SEED DATA INITIALIZATION
    // ==========================================

    private void seedProfessors(SQLiteDatabase db) {
        try {
            // CIVE - Computer Science & Software Engineering
            insertProfessor(db, "Dr. J. Mwamba", "CIVE", "Computer Science & Engineering",
                    "CIVE Block 2", "Room 214", DEFAULT_PHONE, DEFAULT_EMAIL,
                    "CS 111 (Introduction to Programming) & CS 211 (Data Structures)", "Year 1 & Year 2",
                    "Mon & Wed: 10:00 AM - 12:00 PM");

            insertProfessor(db, "Dr. E. Ndunguru", "CIVE", "Computer Science & Engineering",
                    "CIVE Block 1", "Room 105", DEFAULT_PHONE, DEFAULT_EMAIL,
                    "SE 111 (Intro to Software Engineering) & SE 221 (Requirements Engineering)", "Year 1 & Year 2",
                    "Tue & Thu: 02:00 PM - 04:00 PM");

            insertProfessor(db, "Dr. S. Haule", "CIVE", "Computer Science & Engineering",
                    "CIVE Block 3", "Room 302", DEFAULT_PHONE, DEFAULT_EMAIL,
                    "CS 112 (Discrete Mathematics) & CS 311 (Algorithm Analysis)", "Year 1 & Year 3",
                    "Wed & Fri: 10:00 AM - 12:00 PM");

            insertProfessor(db, "Dr. R. Mushi", "CIVE", "Computer Science & Engineering",
                    "CIVE Block 2", "Room 220", DEFAULT_PHONE, DEFAULT_EMAIL,
                    "CS 113 (Computer Architecture & Org) & Information Security", "Year 1 & Year 4",
                    "Tue & Thu: 10:00 AM - 12:00 PM");

            insertProfessor(db, "Mr. K. Temba", "CIVE", "Computer Science & Engineering",
                    "CIVE Block 4", "Room 402", DEFAULT_PHONE, DEFAULT_EMAIL,
                    "CS 111 Practical Labs (C/Python Programming Clinic)", "Year 1",
                    "Mon to Fri: 02:00 PM - 04:00 PM");

            insertProfessor(db, "Dr. B. Mrema", "CIVE", "Information Systems",
                    "CIVE Block 1", "Room 112", DEFAULT_PHONE, DEFAULT_EMAIL,
                    "SE 111 Labs (Git/GitHub Version Control) & Web Technologies", "Year 1 & Year 2",
                    "Wed: 02:00 PM - 04:00 PM");

            // Multi-college foundational lecturers for 1st Years
            insertProfessor(db, "Dr. F. Mmari", "CNMS", "Department of Mathematics & Statistics",
                    "CNMS Science Complex", "Room 118", DEFAULT_PHONE, DEFAULT_EMAIL,
                    "MT 111 (Linear Algebra & Calculus for Computing)", "Year 1",
                    "Mon & Thu: 11:00 AM - 01:00 PM");

            insertProfessor(db, "Ms. A. Komba", "CHSS", "Department of Languages & Literature",
                    "CHSS Block B", "Room 204", DEFAULT_PHONE, DEFAULT_EMAIL,
                    "IS 111 (Communication Skills & Study Methods)", "Year 1",
                    "Tue: 02:00 PM - 04:00 PM");

            // Semester 2 Lecturers (From Official UDOM SE1 Timetable 2025/2026)
            insertProfessor(db, "Mr. Feruzi Hassan", "CIVE", "Computer Science & Engineering",
                    "CIVE Block 2", "Room 208", DEFAULT_PHONE, DEFAULT_EMAIL,
                    "CP 121 / CP 1201 (Introduction to Database Systems)", "Year 1",
                    "Mon: 11:30 AM - 01:30 PM");

            insertProfessor(db, "Dr. Mohamed Mjahid", "CIVE", "Computer Science & Engineering",
                    "CIVE Block 3", "Room 315", DEFAULT_PHONE, DEFAULT_EMAIL,
                    "CN 121 / CN 1201 (Introduction to Computer Networking) & Lab", "Year 1",
                    "Mon & Fri: 09:30 AM - 11:30 AM");

            insertProfessor(db, "Prof. Majuto Manyilizu", "CIVE", "Information Systems",
                    "CIVE Block 1", "Room 101", DEFAULT_PHONE, DEFAULT_EMAIL,
                    "CG 121 (Wearable Computing)", "Year 1",
                    "Tue: 09:30 AM - 11:30 AM");

            insertProfessor(db, "Mr. Ona Nixon", "CIVE", "Computer Science & Engineering",
                    "CIVE Block 4", "Room 410", DEFAULT_PHONE, DEFAULT_EMAIL,
                    "CP 123 / CP 1203 (Database Applications)", "Year 1",
                    "Tue: 09:30 AM - 11:30 AM");

            insertProfessor(db, "Dr. Hassan Mluba", "CIVE", "Information Systems",
                    "CIVE Block 2", "Room 215", DEFAULT_PHONE, DEFAULT_EMAIL,
                    "CG 121 (Wearable Computing)", "Year 1",
                    "Wed: 07:30 AM - 09:30 AM");

            insertProfessor(db, "Mr. Hamis Fereji", "CIVE", "Computer Science & Engineering",
                    "CIVE Block 1", "Room 118", DEFAULT_PHONE, DEFAULT_EMAIL,
                    "CS 123 (Introduction to Software Engineering) & IA 124 (IT Security)", "Year 1",
                    "Wed & Thu: 01:30 PM - 03:30 PM");

            Log.d(TAG, "Professors seeded successfully.");
        } catch (Exception e) {
            Log.e(TAG, "Error seeding professors: " + e.getMessage(), e);
        }
    }

    private void seedCourses(SQLiteDatabase db) {
        try {
            // =========================================================================
            // YEAR 1 - SEMESTER 1 (BSc Software Engineering)
            // =========================================================================
            insertCourse(db, "BSc Software Engineering", 1, 1, "LG 102", "Foundation English Language Skills", 7.5, "Core", "Foundational");
            insertCourse(db, "BSc Software Engineering", 1, 1, "CP 111", "Introduction to Computer Programming", 9.0, "Core", "Core Software");
            insertCourse(db, "BSc Software Engineering", 1, 1, "MT 1111", "Calculus and Linear Algebra", 7.5, "Core", "Math/Stat");
            insertCourse(db, "BSc Software Engineering", 1, 1, "DS 102", "Development Perspectives", 7.5, "Core", "Foundational");
            insertCourse(db, "BSc Software Engineering", 1, 1, "MT 1117", "Discrete Mathematics for Computing", 7.5, "Core", "Math/Stat");
            insertCourse(db, "BSc Software Engineering", 1, 1, "IT 111", "Information Technology Essentials", 7.5, "Core", "Foundational");
            insertCourse(db, "BSc Software Engineering", 1, 1, "MT 1112", "Mathematical Foundation for Computing", 7.5, "Core", "Math/Stat");
            insertCourse(db, "BSc Software Engineering", 1, 1, "IA 112", "Fundamentals of Networking and Cyber Security", 7.5, "Core", "Security");

            // =========================================================================
            // YEAR 1 - SEMESTER 2 (BSc Software Engineering)
            // =========================================================================
            insertCourse(db, "BSc Software Engineering", 1, 2, "CP 123", "Database Applications Development", 9.0, "Core", "Core Software");
            insertCourse(db, "BSc Software Engineering", 1, 2, "MT 1211", "Advanced Calculus", 7.5, "Core", "Math/Stat");
            insertCourse(db, "BSc Software Engineering", 1, 2, "CP 121", "Database Systems", 9.0, "Core", "Core Software");
            insertCourse(db, "BSc Software Engineering", 1, 2, "CN 121", "Computer Networking Essentials", 7.5, "Core", "Foundational");
            insertCourse(db, "BSc Software Engineering", 1, 2, "ST 1210", "Probability and Statistics for Computing", 7.5, "Core", "Math/Stat");
            insertCourse(db, "BSc Software Engineering", 1, 2, "CS 123", "Introduction to Software Engineering", 6.0, "Core", "Core Software");
            insertCourse(db, "BSc Software Engineering", 1, 2, "IA 124", "IT Security", 6.0, "Core", "Security");
            insertCourse(db, "BSc Software Engineering", 1, 2, "CS 131", "Industrial Practical Training I (IPT I)", 9.6, "Core", "IPT");

            // =========================================================================
            // YEAR 2 - SEMESTER 1 (BSc Software Engineering)
            // =========================================================================
            insertCourse(db, "BSc Software Engineering", 2, 1, "CP 215", "Object-Oriented Programming (Java/C++)", 9.0, "Core", "Core Software");
            insertCourse(db, "BSc Software Engineering", 2, 1, "CN 211", "Routing and Switching Essentials", 9.0, "Core", "Foundational");
            insertCourse(db, "BSc Software Engineering", 2, 1, "CP 211", "Data Structures and Algorithms", 9.0, "Core", "Core Software");
            insertCourse(db, "BSc Software Engineering", 2, 1, "CP 212", "Advanced Database Systems", 7.5, "Core", "Core Software");
            insertCourse(db, "BSc Software Engineering", 2, 1, "CP 213", "Computer Organization and Architecture", 10.5, "Core", "Core Software");
            insertCourse(db, "BSc Software Engineering", 2, 1, "CT 211", "Digital Logic Design", 9.0, "Core", "Foundational");
            insertCourse(db, "BSc Software Engineering", 2, 1, "CP 214", "Operating Systems", 7.5, "Core", "Core Software");

            // =========================================================================
            // YEAR 2 - SEMESTER 2 (BSc Software Engineering)
            // =========================================================================
            insertCourse(db, "BSc Software Engineering", 2, 2, "CP 226", "Software Requirements Engineering", 9.0, "Core", "Core Software");
            insertCourse(db, "BSc Software Engineering", 2, 2, "CP 221", "Web Technologies and Applications", 7.5, "Core", "Core Software");
            insertCourse(db, "BSc Software Engineering", 2, 2, "IS 221", "Human Computer Interaction", 7.5, "Core", "Core Software");
            insertCourse(db, "BSc Software Engineering", 2, 2, "CP 222", "Mobile Application Development", 7.5, "Core", "Core Software");
            insertCourse(db, "BSc Software Engineering", 2, 2, "CP 223", "Design and Analysis of Algorithms", 7.5, "Core", "Core Software");
            insertCourse(db, "BSc Software Engineering", 2, 2, "CP 224", "Software Architecture and Design", 7.5, "Core", "Core Software");
            insertCourse(db, "BSc Software Engineering", 2, 2, "CP 225", "Enterprise Systems Modeling", 7.5, "Core", "Core Software");
            insertCourse(db, "BSc Software Engineering", 2, 2, "CS 231", "Industrial Practical Training II (IPT II)", 9.6, "Core", "IPT");

            // =========================================================================
            // YEAR 3 - SEMESTER 1 (BSc Software Engineering)
            // =========================================================================
            insertCourse(db, "BSc Software Engineering", 3, 1, "CP 318", "Software Quality Assurance and Testing", 9.0, "Core", "Core Software");
            insertCourse(db, "BSc Software Engineering", 3, 1, "MT 3111", "Numerical Analysis", 7.5, "Core", "Math/Stat");
            insertCourse(db, "BSc Software Engineering", 3, 1, "CP 311", "Software Metrics and Quality Management", 9.0, "Core", "Core Software");
            insertCourse(db, "BSc Software Engineering", 3, 1, "CP 312", "Component-Based Software Engineering", 9.0, "Core", "Core Software");
            insertCourse(db, "BSc Software Engineering", 3, 1, "CP 313", "Cloud Computing and Distributed Systems", 9.0, "Core", "Core Software");
            insertCourse(db, "BSc Software Engineering", 3, 1, "EME 314", "Engineering Economics and Entrepreneurship", 7.5, "Core", "Foundational");
            insertCourse(db, "BSc Software Engineering", 3, 1, "EL 311", "Elective I - Artificial Intelligence or IoT", 9.0, "Elective", "Elective");

            // =========================================================================
            // YEAR 3 - SEMESTER 2 (BSc Software Engineering)
            // =========================================================================
            insertCourse(db, "BSc Software Engineering", 3, 2, "CS 321", "Formal Methods in Software Engineering", 9.0, "Core", "Core Software");
            insertCourse(db, "BSc Software Engineering", 3, 2, "CP 321", "Software Project Management", 9.0, "Core", "Core Software");
            insertCourse(db, "BSc Software Engineering", 3, 2, "IA 321", "Information Systems Auditing and Risk", 9.0, "Core", "Security");
            insertCourse(db, "BSc Software Engineering", 3, 2, "CP 322", "Agile Software Development Methodologies", 9.0, "Core", "Core Software");
            insertCourse(db, "BSc Software Engineering", 3, 2, "CP 323", "Service Oriented Architecture and Web Services", 9.0, "Core", "Core Software");
            insertCourse(db, "BSc Software Engineering", 3, 2, "CS 331", "Industrial Practical Training III (IPT III)", 9.6, "Core", "IPT");
            insertCourse(db, "BSc Software Engineering", 3, 2, "EL 321", "Elective II - DevOps and Continuous Delivery", 7.5, "Elective", "Elective");

            // =========================================================================
            // YEAR 4 - SEMESTER 1 (BSc Software Engineering)
            // =========================================================================
            insertCourse(db, "BSc Software Engineering", 4, 1, "SI 311", "Research Methodology", 7.5, "Core", "Foundational");
            insertCourse(db, "BSc Software Engineering", 4, 1, "CS 431", "Final Year Project I (FYP I)", 6.0, "Core", "FYP");
            insertCourse(db, "BSc Software Engineering", 4, 1, "CT 312", "Embedded Systems and Firmware Engineering", 9.0, "Core", "Core Software");
            insertCourse(db, "BSc Software Engineering", 4, 1, "IM 411", "Professional Ethics and Cyber Law", 7.5, "Core", "Foundational");
            insertCourse(db, "BSc Software Engineering", 4, 1, "CP 412", "Big Data Analytics and Data Engineering", 9.0, "Core", "Core Software");
            insertCourse(db, "BSc Software Engineering", 4, 1, "CD 312", "System Administration and Security", 7.5, "Core", "Security");
            insertCourse(db, "BSc Software Engineering", 4, 1, "BT 413", "Engineering Management and Safety", 6.0, "Core", "Foundational");
            insertCourse(db, "BSc Software Engineering", 4, 1, "EL 411", "Elective III - Machine Learning Applications", 7.5, "Elective", "Elective");

            // =========================================================================
            // YEAR 4 - SEMESTER 2 (BSc Software Engineering)
            // =========================================================================
            insertCourse(db, "BSc Software Engineering", 4, 2, "CP 421", "Software Maintenance and Evolution", 7.5, "Core", "Core Software");
            insertCourse(db, "BSc Software Engineering", 4, 2, "CS 421", "Compiler Design and Virtual Machines", 7.5, "Core", "Core Software");
            insertCourse(db, "BSc Software Engineering", 4, 2, "CS 432", "Final Year Project II (FYP II)", 9.0, "Core", "FYP");
            insertCourse(db, "BSc Software Engineering", 4, 2, "CP 422", "Secure Software Engineering", 9.0, "Core", "Security");
            insertCourse(db, "BSc Software Engineering", 4, 2, "CP 423", "Human Centered Systems Design", 9.0, "Core", "Core Software");
            insertCourse(db, "BSc Software Engineering", 4, 2, "CP 424", "Enterprise Application Integration", 9.0, "Core", "Core Software");
            insertCourse(db, "BSc Software Engineering", 4, 2, "EL 421", "Elective IV - Blockchain and Distributed Ledgers", 9.0, "Elective", "Elective");

            Log.d(TAG, "Courses seeded successfully: 59+ BSc Software Engineering curriculum handbook entries.");
        } catch (Exception e) {
            Log.e(TAG, "Error seeding courses: " + e.getMessage(), e);
        }
    }

    private void seedTimetables(SQLiteDatabase db) {
        try {
            // =========================================================================
            // SEMESTER 1: BSc Software Engineering (Year 1)
            // =========================================================================
            // Monday
            insertTimetableEntry(db, "BSc Software Engineering", 1, 1, "Monday", "08:00", "10:00",
                    "CS 111", "Introduction to Programming (C/Python)", "Theory", "CIVE Lecture Room A", "Dr. J. Mwamba");

            insertTimetableEntry(db, "BSc Software Engineering", 1, 1, "Monday", "10:00", "12:00",
                    "MT 111", "Linear Algebra and Calculus for Computing", "Theory", "CIVE Auditorium", "Dr. F. Mmari");

            insertTimetableEntry(db, "BSc Software Engineering", 1, 1, "Monday", "14:00", "17:00",
                    "CS 111-Lab", "Programming Practical Lab (C/Python)", "Practical", "CIVE Computer Lab 2", "Mr. K. Temba");

            // Tuesday
            insertTimetableEntry(db, "BSc Software Engineering", 1, 1, "Tuesday", "08:00", "10:00",
                    "SE 111", "Introduction to Software Engineering and Git", "Theory", "CIVE Lecture Room B", "Dr. E. Ndunguru");

            insertTimetableEntry(db, "BSc Software Engineering", 1, 1, "Tuesday", "11:00", "13:00",
                    "CS 112", "Discrete Mathematics for Computing", "Theory", "CIVE Lecture Room A", "Dr. S. Haule");

            insertTimetableEntry(db, "BSc Software Engineering", 1, 1, "Tuesday", "14:00", "16:00",
                    "IS 111", "Communication Skills and Study Methods", "Theory", "CIVE Lecture Room C", "Ms. A. Komba");

            // Wednesday
            insertTimetableEntry(db, "BSc Software Engineering", 1, 1, "Wednesday", "08:00", "10:00",
                    "CS 113", "Computer Architecture and Organization", "Theory", "CIVE Lecture Room A", "Dr. R. Mushi");

            insertTimetableEntry(db, "BSc Software Engineering", 1, 1, "Wednesday", "10:00", "12:00",
                    "SE 111-Lab", "Software Engineering Lab and Version Control", "Practical", "CIVE Computer Lab 1", "Dr. B. Mrema");

            insertTimetableEntry(db, "BSc Software Engineering", 1, 1, "Wednesday", "14:00", "18:00",
                    "SPORTS", "University Sports Day and Association Baraza (Off Classes)", "Sports", "UDOM Sports Grounds", "Dean of Students");

            // Thursday
            insertTimetableEntry(db, "BSc Software Engineering", 1, 1, "Thursday", "08:00", "10:00",
                    "CS 111", "Control Structures - Functions and Pointers", "Theory", "CIVE Lecture Room B", "Dr. J. Mwamba");

            insertTimetableEntry(db, "BSc Software Engineering", 1, 1, "Thursday", "10:00", "12:00",
                    "MT 111", "Calculus Problem Solving Tutorial", "Tutorial", "CIVE Lecture Room D", "Dr. F. Mmari");

            insertTimetableEntry(db, "BSc Software Engineering", 1, 1, "Thursday", "14:00", "16:00",
                    "CS 113-Lab", "Computer Architecture and Hardware Lab", "Practical", "CIVE Hardware Lab", "Dr. R. Mushi");

            // Friday
            insertTimetableEntry(db, "BSc Software Engineering", 1, 1, "Friday", "08:00", "10:00",
                    "CS 112", "Discrete Mathematics Tutorial and Quiz", "Tutorial", "CIVE Lecture Room A", "Dr. S. Haule");

            insertTimetableEntry(db, "BSc Software Engineering", 1, 1, "Friday", "10:00", "12:00",
                    "SE 111", "Software Lifecycle Models and Agile Workshop", "Theory", "CIVE Auditorium", "Dr. E. Ndunguru");

            insertTimetableEntry(db, "BSc Software Engineering", 1, 1, "Friday", "14:00", "16:00",
                    "CLINIC", "Open Programming Help Clinic", "Clinic", "CIVE Computer Lab 3", "Mr. K. Temba");

            // =========================================================================
            // SEMESTER 2: BSc Software Engineering (Year 1) [Official UDOM Timetable 2025/2026]
            // =========================================================================
            // Monday
            insertTimetableEntry(db, "BSc Software Engineering", 1, 2, "Monday", "11:30", "13:30",
                    "CP 121 / CP 1201 D", "Introduction to Database Systems", "Theory", "CIVE-LRB 103", "Mr. Feruzi Hassan");

            insertTimetableEntry(db, "BSc Software Engineering", 1, 2, "Monday", "16:00", "18:00",
                    "CN 121 / CN 1201 D", "Introduction to Computer Networking", "Practical", "NET_LAB", "Dr. Mohamed Mjahid");

            // Tuesday
            insertTimetableEntry(db, "BSc Software Engineering", 1, 2, "Tuesday", "09:30", "11:30",
                    "CG 121 B", "Wearable Computing", "Theory", "CIVE-LRB 004C", "Prof. Majuto Manyilizu");

            insertTimetableEntry(db, "BSc Software Engineering", 1, 2, "Tuesday", "09:30", "11:30",
                    "CP 123 / CP 1203 D", "Database Applications", "Theory", "CIVE-LRB 105", "Mr. Ona Nixon");

            // Wednesday
            insertTimetableEntry(db, "BSc Software Engineering", 1, 2, "Wednesday", "07:30", "09:30",
                    "CG 121 E", "Wearable Computing", "Theory", "CIVE-LRB 004C", "Dr. Hassan Mluba");

            insertTimetableEntry(db, "BSc Software Engineering", 1, 2, "Wednesday", "13:30", "15:30",
                    "CS 123 B", "Introduction to Software Engineering", "Theory", "CIVE-LRB 105", "Mr. Hamis Fereji");

            // Thursday
            insertTimetableEntry(db, "BSc Software Engineering", 1, 2, "Thursday", "13:30", "15:30",
                    "IA 124 A", "Introduction to IT Security", "Theory", "CIVE-LRB 105", "Mr. Hamis Fereji");

            // Friday
            insertTimetableEntry(db, "BSc Software Engineering", 1, 2, "Friday", "09:30", "11:30",
                    "CN 121 / CN 1201 D", "Introduction to Computer Networking", "Theory", "CIVE-LRB 103", "Dr. Mohamed Mjahid");

            // Year 2 Software Engineering
            insertTimetableEntry(db, "BSc Software Engineering", 2, 1, "Monday", "08:00", "10:00",
                    "CP 211", "Data Structures and Algorithms", "Theory", "CIVE Lecture Room B", "Dr. J. Mwamba");

            insertTimetableEntry(db, "BSc Software Engineering", 2, 1, "Wednesday", "10:00", "12:00",
                    "CP 215", "Object-Oriented Programming (Java/C++)", "Theory", "CIVE Lecture Room A", "Dr. E. Ndunguru");

            insertTimetableEntry(db, "BSc Software Engineering", 2, 2, "Tuesday", "10:00", "12:00",
                    "CP 226", "Software Requirements Engineering", "Theory", "CIVE Lecture Room B", "Dr. E. Ndunguru");

            insertTimetableEntry(db, "BSc Software Engineering", 2, 2, "Thursday", "14:00", "17:00",
                    "CP 222", "Mobile Application Development Lab", "Practical", "CIVE Computer Lab 1", "Dr. B. Mrema");

            Log.d(TAG, "Timetables seeded successfully.");
        } catch (Exception e) {
            Log.e(TAG, "Error seeding timetables: " + e.getMessage(), e);
        }
    }

    private void seedAcademicCalendar(SQLiteDatabase db) {
        try {
            // =========================================================================
            // 2024/2025 ACADEMIC YEAR ALMANAC
            // =========================================================================
            insertCalendarEvent(db, "2024/2025", "Semester I",
                    "Orientation Week for All Freshers 2024/2025", "ORIENTATION",
                    "2024-10-21", "2024-10-27",
                    "Freshers reporting - documents physical verification - SR2 portal activation - medical checkup - hostel check-in.");

            insertCalendarEvent(db, "2024/2025", "Semester I",
                    "Semester 1 Teaching Period (15 Weeks)", "ACADEMIC",
                    "2024-11-04", "2025-02-13",
                    "Lectures and continuous assessment (CA) period across all university colleges.");

            insertCalendarEvent(db, "2024/2025", "Semester I",
                    "Mwalimu Nyerere Memorial Day", "HOLIDAY",
                    "2024-10-14", "2024-10-14",
                    "National public holiday honoring Mwalimu Julius Kambarage Nyerere. Campus offices and lectures closed.");

            insertCalendarEvent(db, "2024/2025", "Semester I",
                    "Continuous Assessment Test 1 (CA)", "TEST",
                    "2024-12-09", "2024-12-20",
                    "First round of departmental continuous assessment tests carrying 20% of final coursework.");

            insertCalendarEvent(db, "2024/2025", "Semester I",
                    "Tanzania Independence Day", "HOLIDAY",
                    "2024-12-09", "2024-12-09",
                    "National public holiday celebrating Independence Day. No classes.");

            insertCalendarEvent(db, "2024/2025", "Semester I",
                    "End of Year Christmas and New Year Break", "HOLIDAY",
                    "2024-12-23", "2025-01-03",
                    "University festive recess for students and staff.");

            insertCalendarEvent(db, "2024/2025", "Semester I",
                    "Zanzibar Revolution Day", "HOLIDAY",
                    "2025-01-12", "2025-01-12",
                    "National public holiday commemorating the Zanzibar Revolution.");

            insertCalendarEvent(db, "2024/2025", "Semester I",
                    "University Examinations (UE) - Semester 1", "EXAM",
                    "2025-02-17", "2025-02-28",
                    "Official end-of-semester 1 University Examinations (UE) across all colleges. Minimum 16/40 in CA required.");

            insertCalendarEvent(db, "2024/2025", "Break",
                    "Semester 1 Inter-Semester Break", "BREAK",
                    "2025-03-03", "2025-03-14",
                    "Two-week student vacation and staff grading recess.");

            insertCalendarEvent(db, "2024/2025", "Semester II",
                    "Semester 2 Teaching Period (15 Weeks)", "ACADEMIC",
                    "2025-03-17", "2025-06-27",
                    "Commencement of Semester 2 lectures and practical training across all degree programs.");

            insertCalendarEvent(db, "2024/2025", "Semester II",
                    "Sheikh Abeid Amani Karume Day", "HOLIDAY",
                    "2025-04-07", "2025-04-07",
                    "National public holiday commemorating Sheikh Abeid Amani Karume.");

            insertCalendarEvent(db, "2024/2025", "Semester II",
                    "Tanzania Union Day", "HOLIDAY",
                    "2025-04-26", "2025-04-26",
                    "National public holiday commemorating the Union of Tanganyika and Zanzibar.");

            insertCalendarEvent(db, "2024/2025", "Semester II",
                    "International Workers Day (May Day)", "HOLIDAY",
                    "2025-05-01", "2025-05-01",
                    "National public holiday celebrating workers rights.");

            insertCalendarEvent(db, "2024/2025", "Semester II",
                    "Continuous Assessment Test 2 (CA)", "TEST",
                    "2025-05-12", "2025-05-23",
                    "Second round of continuous assessment tests for Semester 2.");

            insertCalendarEvent(db, "2024/2025", "Semester II",
                    "University Examinations (UE) - Semester 2", "EXAM",
                    "2025-06-30", "2025-07-11",
                    "Final degree University Examinations (UE) for Semester 2 and progression to subsequent academic year.");

            insertCalendarEvent(db, "2024/2025", "Semester II",
                    "Saba Saba National Trade Fair Day", "HOLIDAY",
                    "2025-07-07", "2025-07-07",
                    "National public holiday during examination period.");

            insertCalendarEvent(db, "2024/2025", "Semester II",
                    "Industrial Practical Training (IPT I - II - III)", "IPT",
                    "2025-07-14", "2025-09-04",
                    "Mandatory 8-week field training attachment for Engineering - Computer Science - and IT students (Cost: TZS 560000).");

            insertCalendarEvent(db, "2024/2025", "Break",
                    "Nane Nane Farmers Day", "HOLIDAY",
                    "2025-08-08", "2025-08-08",
                    "National public holiday honoring farmers and agricultural development.");

            // =========================================================================
            // 2025/2026 ACADEMIC YEAR ALMANAC
            // =========================================================================
            insertCalendarEvent(db, "2025/2026", "Semester I",
                    "Orientation Week for First Year Students (Freshers)", "ORIENTATION",
                    "2025-11-10", "2025-11-16",
                    "Comprehensive freshers orientation: certificate verification - medical exam at dispensary - SR2 activation - hostel check-in.");

            insertCalendarEvent(db, "2025/2026", "Semester I",
                    "16th UDOM Graduation Ceremonies", "CEREMONY",
                    "2025-11-19", "2025-11-21",
                    "Annual university convocation and conferment of degrees at Chimwaga Hall.");

            insertCalendarEvent(db, "2025/2026", "Semester I",
                    "Semester 1 Teaching Period (15 Weeks)", "ACADEMIC",
                    "2025-11-24", "2026-03-06",
                    "Official 15-week lecture teaching period for Semester 1 across all colleges.");

            insertCalendarEvent(db, "2025/2026", "Semester I",
                    "Continuous Assessment Test 1 (CA)", "TEST",
                    "2026-01-12", "2026-01-23",
                    "First departmental continuous assessment tests for Semester 1.");

            insertCalendarEvent(db, "2025/2026", "Semester I",
                    "University Examinations (UE) - Semester 1", "EXAM",
                    "2026-03-09", "2026-03-20",
                    "Official end-of-semester 1 University Examinations (UE) for all colleges.");

            insertCalendarEvent(db, "2025/2026", "Break",
                    "Semester 1 Inter-Semester Break", "BREAK",
                    "2026-03-23", "2026-04-03",
                    "Two-week inter-semester holiday for students and grading period for academic staff.");

            insertCalendarEvent(db, "2025/2026", "Semester II",
                    "Semester 2 Teaching Period (15 Weeks)", "ACADEMIC",
                    "2026-04-08", "2026-07-17",
                    "Commencement of Semester 2 lectures and practical training registration.");

            insertCalendarEvent(db, "2025/2026", "Semester II",
                    "Continuous Assessment Test 2 (CA)", "TEST",
                    "2026-05-18", "2026-05-29",
                    "Second round of continuous assessment tests for Semester 2.");

            insertCalendarEvent(db, "2025/2026", "Semester II",
                    "University Examinations (UE) - Semester 2", "EXAM",
                    "2026-07-20", "2026-07-31",
                    "Final degree University Examinations (UE) for Semester 2 and progression to subsequent academic year.");

            insertCalendarEvent(db, "2025/2026", "Semester II",
                    "Industrial Practical Training (IPT I - II - III)", "IPT",
                    "2026-08-03", "2026-09-25",
                    "Mandatory 8-week field training attachment for Engineering - Computer Science - and IT students (Cost: TZS 560000).");

            insertCalendarEvent(db, "2025/2026", "Break",
                    "Supplementary and Special University Examinations", "EXAM",
                    "2026-08-31", "2026-09-11",
                    "Annual supplementary and special exams for failed or deferred courses.");

            // =========================================================================
            // 2026/2027 ACADEMIC YEAR ALMANAC
            // =========================================================================
            insertCalendarEvent(db, "2026/2027", "Semester I",
                    "Orientation Week for First Year Students (Freshers)", "ORIENTATION",
                    "2026-11-09", "2026-11-15",
                    "Comprehensive freshers orientation: certificate verification - medical exam at dispensary - SR2 activation - hostel check-in.");

            insertCalendarEvent(db, "2026/2027", "Semester I",
                    "Semester 1 Teaching Period (15 Weeks)", "ACADEMIC",
                    "2026-11-16", "2027-02-26",
                    "Official start of 15-week lecture teaching period for Semester 1 across all colleges.");

            insertCalendarEvent(db, "2026/2027", "Semester I",
                    "17th UDOM Graduation Ceremonies", "CEREMONY",
                    "2026-12-02", "2026-12-04",
                    "Annual university graduation ceremonies and award of degrees at Chimwaga Hall.");

            insertCalendarEvent(db, "2026/2027", "Semester I",
                    "University Examinations (UE) - Semester 1", "EXAM",
                    "2027-03-01", "2027-03-12",
                    "Official end-of-semester 1 University Examinations (UE) for all colleges.");

            insertCalendarEvent(db, "2026/2027", "Break",
                    "Semester 1 Inter-Semester Break", "BREAK",
                    "2027-03-15", "2027-03-26",
                    "Two-week inter-semester holiday for students and grading period for academic staff.");

            insertCalendarEvent(db, "2026/2027", "Semester II",
                    "Semester 2 Teaching Period (15 Weeks)", "ACADEMIC",
                    "2027-03-29", "2027-07-09",
                    "Start of Semester 2 lectures and practical training registration.");

            insertCalendarEvent(db, "2026/2027", "Semester II",
                    "University Examinations (UE) - Semester 2", "EXAM",
                    "2027-07-12", "2027-07-23",
                    "Final degree University Examinations (UE) for Semester 2 and progression to subsequent academic year.");

            insertCalendarEvent(db, "2026/2027", "Break",
                    "Supplementary and Special University Examinations", "EXAM",
                    "2027-08-23", "2027-09-03",
                    "Supplementary and special examinations across all academic programs.");

            insertCalendarEvent(db, "2026/2027", "ALL",
                    "UDOSO Student General Assembly (Baraza)", "MEETING",
                    "2026-11-14", "2026-11-14",
                    "Annual general student dialogue with Vice Chancellor - DVCs - Dean of Students at Chimwaga Hall.");

            insertCalendarEvent(db, "2026/2027", "ALL",
                    "University Annual Sports and Culture Gala", "SPORTS",
                    "2026-11-25", "2026-11-28",
                    "Inter-college sports tournament: CIVE - CoBE - CoED - CHSS - CNMS - CoESE - Law - Medicine at UDOM Sports Complex.");

            Log.d(TAG, "Academic calendar seeded successfully: Multi-year Almanac (2024-2027).");
        } catch (Exception e) {
            Log.e(TAG, "Error seeding academic calendar: " + e.getMessage(), e);
        }
    }

    private void seedStudentLeaders(SQLiteDatabase db) {
        try {
            insertStudentLeader(db, "Emmanuel Mwita", "Class Representative (CR) - Software Eng Year 1",
                    "CIVE", "Hostel Block 4", "Room 112", DEFAULT_PHONE, DEFAULT_EMAIL);

            insertStudentLeader(db, "Neema Shirima", "Assistant Class Representative (ACR) - Software Eng Year 1",
                    "CIVE", "Hostel Block 2", "Room 204", DEFAULT_PHONE, DEFAULT_EMAIL);

            insertStudentLeader(db, "Joshua Kavishe", "Class Representative (CR) - Software Eng Year 2",
                    "CIVE", "Hostel Block 5", "Room 310", DEFAULT_PHONE, DEFAULT_EMAIL);

            insertStudentLeader(db, "Kelvin John", "UDOSO President (Student Union)",
                    "ALL", "Central Admin - Student Center", "Office 01", DEFAULT_PHONE, DEFAULT_EMAIL);

            insertStudentLeader(db, "Grace Masanja", "UDOSO Vice President",
                    "ALL", "Central Admin - Student Center", "Office 02", DEFAULT_PHONE, DEFAULT_EMAIL);

            insertStudentLeader(db, "Baraka Mdegella", "UDOSO Minister of Loans & HESLB Affairs",
                    "ALL", "Central Admin - Student Center", "Office 05", DEFAULT_PHONE, DEFAULT_EMAIL);

            insertStudentLeader(db, "Denis Mkumbo", "CIVE Student Representative (College President)",
                    "CIVE", "CIVE Block 1", "Room 102", DEFAULT_PHONE, DEFAULT_EMAIL);

            insertStudentLeader(db, "Madam R. Temu", "Chief Hostel Warden - CIVE Zone",
                    "CIVE", "CIVE Hostel Block 1 Ground Floor", "Warden Office", DEFAULT_PHONE, DEFAULT_EMAIL);

            Log.d(TAG, "Student leaders seeded successfully.");
        } catch (Exception e) {
            Log.e(TAG, "Error seeding student leaders: " + e.getMessage(), e);
        }
    }

    private void seedCampusContacts(SQLiteDatabase db) {
        try {
            insertCampusContact(db, "UDOM Health Center (Dispensary)", "EMERGENCY", "ALL",
                    DEFAULT_PHONE, DEFAULT_EMAIL, "Central Campus Near Administration",
                    "Provides 24/7 outpatient - emergency - pharmacy - and NHIF student treatment.",
                    "emergency ambulance sick health hospital dispensary doctor treatment nhif");

            insertCampusContact(db, "Campus Auxiliary Police & Security", "EMERGENCY", "ALL",
                    DEFAULT_PHONE, DEFAULT_EMAIL, "Main Gate Security HQ",
                    "Handles 24/7 campus security - incident reports - theft - and lost property.",
                    "security police incident theft emergency safety guard");

            insertCampusContact(db, "Benjamin Mkapa Hospital (BMH)", "EMERGENCY", "ALL",
                    DEFAULT_PHONE, DEFAULT_EMAIL, "Adjacent to Health Sciences Campus",
                    "National referral hospital for critical emergencies and specialized care.",
                    "bmh hospital referral doctor specialist emergency");

            insertCampusContact(db, "CIVE Dean Office", "ACADEMIC", "CIVE",
                    DEFAULT_PHONE, DEFAULT_EMAIL, "CIVE Administration Block - 1st Floor",
                    "Oversees academic programs - admissions - and student clearance for CIVE.",
                    "dean cive principal academic clearance inquiry");

            insertCampusContact(db, "SR2 & ICT Support Helpdesk", "SUPPORT", "ALL",
                    DEFAULT_PHONE, DEFAULT_EMAIL, "Central Administration - Ground Floor",
                    "Assists with SR2 portal passwords - course registration errors - and campus Wi-Fi.",
                    "sr2 ict portal password login wifi helpdesk support");

            insertCampusContact(db, "Hostel Accommodation Directorate", "HOSTEL", "ALL",
                    DEFAULT_PHONE, DEFAULT_EMAIL, "Central Administration - Block C",
                    "Manages hostel room allocations - room key clearance - and warden assignments.",
                    "hostel room accommodation warden housing key");

            insertCampusContact(db, "Dean of Students Office", "ADMIN", "ALL",
                    DEFAULT_PHONE, DEFAULT_EMAIL, "Student Center Building - 1st Floor",
                    "Oversees student welfare - loans - disciplinary matters - and student governance.",
                    "dean of students welfare discipline heslb loans help");

            insertCampusContact(db, "UDOM Central Library Helpdesk", "SUPPORT", "ALL",
                    DEFAULT_PHONE, DEFAULT_EMAIL, "UDOM Central Library Entrance",
                    "Book loans - reading cubicles - e-resources - and plagiarism check support.",
                    "library book borrow reading study research");

            Log.d(TAG, "Campus contacts seeded successfully.");
        } catch (Exception e) {
            Log.e(TAG, "Error seeding campus contacts: " + e.getMessage(), e);
        }
    }

    private void seedEnhancedFirstYearFaqs(SQLiteDatabase db) {
        try {
            // Freshers Registration Steps
            insertFaq(db, "freshers", "registration,freshers,first year,orientation,admissions,documents,form 4,form 6",
                    "What are the steps for first-year registration?",
                    "📋 UDOM First-Year Registration Steps:\n\n"
                            + "1. Report to your designated college assembly hall during Orientation Week.\n"
                            + "2. Present your original Form 4, Form 6 certificates, birth certificate, and admission letter for physical verification.\n"
                            + "3. Complete medical examination at UDOM Dispensary and submit the signed medical form.\n"
                            + "4. Pay university direct fees via GePG control number generated on SR2 portal.\n"
                            + "5. Capture your biometric photo for your Student ID Card.\n"
                            + "6. Finalize online course registration on sr2.udom.ac.tz before the semester deadline.",
                    "ALL");

            // HESLB Loans
            insertFaq(db, "heslb", "heslb,loan,loans,boom,allocation,appeal,signing",
                    "How do HESLB loans work at UDOM?",
                    "💰 HESLB Student Loans & Boom:\n\n"
                            + "• Loan Allocation: Check your allocation status on the OLAMS portal (olas.heslb.go.tz).\n"
                            + "• Loan Signing: Done physically at your college during orientation week. Bring your Student ID and admission letter.\n"
                            + "• Disbursement: Meals & Accommodation (Boom) allowance is credited directly to your registered bank account (CRDB/NMB/NBC).\n"
                            + "• Inquiries: Contact the UDOSO Minister of Loans at Student Center Office 05 or call " + DEFAULT_PHONE + ".",
                    "ALL");

            // Grading System & Pass Marks
            insertFaq(db, "grades", "gpa,grade,grading,pass mark,ca,supp,supplementary,carryover,fail",
                    "What is the grading system and pass mark at UDOM?",
                    "📊 UDOM Degree Grading & Examination Rules:\n\n"
                            + "• Continuous Assessment (CA): 40% (You MUST score at least 16/40 to be allowed to sit for UE).\n"
                            + "• University Examination (UE): 60% (Minimum pass is 24/60).\n"
                            + "• Overall Pass Mark: 50% (Grade C).\n\n"
                            + "📈 GPA Grade Scale (5.0 Points):\n"
                            + "• A  : 70% – 100% (5.0 Points - Excellent)\n"
                            + "• B+ : 60% – 69%  (4.0 Points - Very Good)\n"
                            + "• B  : 50% – 59%  (3.0 Points - Good)\n"
                            + "• C  : 40% – 49%  (2.0 Points - Marginal Fail / Pass for diploma)\n"
                            + "• D  : 35% – 39%  (1.0 Point - Fail / Supplementary)\n"
                            + "• E  : 0% – 34%   (0.0 Points - Discontinuation / Carryover)\n\n"
                            + "⚠️ Minimum GPA to avoid discontinuation: 2.0.",
                    "ALL");

            // Wi-Fi Access
            insertFaq(db, "wifi", "wifi,internet,network,connect,password,eduroam",
                    "How do I connect to UDOM campus Wi-Fi?",
                    "📶 Connecting to UDOM Campus Wi-Fi:\n\n"
                            + "1. Turn on Wi-Fi on your phone or laptop.\n"
                            + "2. Connect to the SSID named 'UDOM-Student'.\n"
                            + "3. A login captive portal will appear automatically (or visit wifi.udom.ac.tz).\n"
                            + "4. Enter your SR2 Registration Number as username and your SR2 portal password.\n"
                            + "5. Tap Login. Free high-speed internet is available across all academic blocks, hostels, and libraries.",
                    "ALL");

            // Dress Code
            insertFaq(db, "rules", "dress code,clothes,rules,regulations,behavior,discipline",
                    "What is the dress code policy at UDOM?",
                    "👔 UDOM Student Dress Code Policy:\n\n"
                            + "• Students must dress decently and professionally in all lecture halls, laboratories, offices, and library.\n"
                            + "• Prohibited: Miniskirts, ripped jeans, transparent clothes, bare-back tops, sagging trousers, and caps inside lecture rooms.\n"
                            + "• Compliance: Security officers and lecturers have the authority to deny entry to students violating the dress code.",
                    "ALL");

            // Banking & ATMs
            insertFaq(db, "banking", "atm,bank,crdb,nmb,nbc,money,cash,withdraw",
                    "Where are bank branches and ATMs on campus?",
                    "🏦 Campus Banking & ATM Locations:\n\n"
                            + "• Central Administration Zone: CRDB Bank Branch & 24/7 ATMs, NMB Bank ATMs, NBC ATMs.\n"
                            + "• CIVE Zone: CRDB and NMB Wakala kiosks near cafeteria.\n"
                            + "• Mobile Money: M-Pesa, Tigo Pesa, Airtel Money, and HaloPesa wakala agents are situated near every college cafeteria.",
                    "ALL");

            Log.d(TAG, "Enhanced first-year FAQs seeded successfully.");
        } catch (Exception e) {
            Log.e(TAG, "Error seeding first-year FAQs: " + e.getMessage(), e);
        }
    }

    private void seedCampusFaqs(SQLiteDatabase db) {
        try {
            // Seed base FAQs
            insertFaq(db, "cive", "cive,informatics,computer,software,programming,cyber",
                    "What is CIVE?",
                    "💻 College of Informatics and Virtual Education (CIVE)\n\n"
                            + "• Location: North-East campus zone.\n"
                            + "• Facilities: Lecture Rooms A, B, C, D, CIVE Auditorium, Blocks 1–6, Computer Laboratories 1 to 4, Hardware Lab.\n"
                            + "• Programs: Software Engineering, Computer Science, Telecommunications, Information Systems, Cyber Security.",
                    "CIVE");

            insertFaq(db, "cobe", "cobe,business,economics,accounting,finance,marketing,procurement",
                    "What is CoBE?",
                    "🏛️ College of Business and Economics (CoBE)\n\n"
                            + "• Location: Central campus near administration block.\n"
                            + "• Facilities: CoBE Auditoriums, lecture halls, and departmental library.\n"
                            + "• Programs: Accounting, Finance, Marketing, Economics, Human Resources, Procurement.",
                    "CoBE");

            insertFaq(db, "coed", "coed,education,teacher,teaching,pedagogy",
                    "What is CoED?",
                    "🎓 College of Education (CoED)\n\n"
                            + "• Location: Central academic wing.\n"
                            + "• Focus: Science and Arts teacher education, pedagogy, and educational administration.",
                    "CoED");

            insertFaq(db, "chss", "chss,humanities,social,sociology,languages,history,geography",
                    "What is CHSS?",
                    "📚 College of Humanities and Social Sciences (CHSS)\n\n"
                            + "• Location: West campus area.\n"
                            + "• Departments: Sociology, Political Science, Languages, History, Geography.",
                    "CHSS");

            insertFaq(db, "cnms", "cnms,natural,math,science,physics,chemistry,biology",
                    "What is CNMS?",
                    "🔬 College of Natural and Mathematical Sciences (CNMS)\n\n"
                            + "• Location: Science complex area.\n"
                            + "• Facilities: Mathematics research rooms, advanced physics, chemistry, biology laboratories.",
                    "CNMS");

            insertFaq(db, "coese", "coese,engineering,mining,geology,earth,petroleum,energy",
                    "What is CoESE?",
                    "🏗️ College of Earth Sciences and Engineering (CoESE)\n\n"
                            + "• Location: Engineering campus wing.\n"
                            + "• Programs: Mining Engineering, Petroleum Geology, Environmental Engineering, Renewable Energy.",
                    "CoESE");

            insertFaq(db, "sol", "law,sol,llb,legal,court,lawyer",
                    "What is School of Law?",
                    "⚖️ School of Law (SoL)\n\n"
                            + "• Offers Bachelor of Laws (LL.B) and postgraduate legal programs with a dedicated campus moot court.",
                    "SoL");

            insertFaq(db, "somd", "somd,medicine,doctor,nursing,sonph,health,pharmacy",
                    "What is School of Medicine & Nursing?",
                    "🩺 School of Medicine & Nursing (SoMD / SoNPH)\n\n"
                            + "• Location: Health Sciences campus near Benjamin Mkapa Hospital.\n"
                            + "• Programs: Doctor of Medicine (MD), Pharmacy, Nursing, and Public Health.",
                    "SoMD");

            insertFaq(db, "colleges", "colleges,faculty,faculties,schools,departments",
                    "What colleges are at UDOM?",
                    "🏛️ UDOM Colleges & Schools:\n\n"
                            + "• CIVE – Informatics & Virtual Education\n"
                            + "• CoBE – Business & Economics\n"
                            + "• CoED – Education\n"
                            + "• CHSS – Humanities & Social Sciences\n"
                            + "• CNMS – Natural & Mathematical Sciences\n"
                            + "• CoESE – Earth Sciences & Engineering\n"
                            + "• School of Law (SoL)\n"
                            + "• School of Medicine (SoMD)\n"
                            + "• School of Nursing (SoNPH)\n\n"
                            + "💡 You can select your college on the HOME screen to customize your profile.",
                    "ALL");

            insertFaq(db, "hostels", "hostel,hostels,accommodation,dorm,dorms,sleep,housing",
                    "How do hostels work at UDOM?",
                    "🏠 UDOM Accommodation & Hostels\n\n"
                            + "• On-Campus: Rooms are organized per college blocks. You apply via your SR2 portal.\n"
                            + "• Hostel blocks are designated within your college complex zone.\n"
                            + "• Provided: Single bed & mattress. Bring your own bedsheets, pillow, bucket, and mosquito net.\n"
                            + "• Off-Campus: Popular student areas include Mkonze, Kisasa, Medeli, and Chimwaga.",
                    "ALL");

            insertFaq(db, "library", "library,book,reading,study,borrow,borrowing",
                    "What are the library hours and rules?",
                    "📚 UDOM Central Library\n\n"
                            + "• Location: Central campus.\n"
                            + "• Opening Hours:\n"
                            + "   - Mon – Fri: 08:00 AM – 10:00 PM\n"
                            + "   - Sat: 08:00 AM – 04:00 PM\n"
                            + "   - Sun: 02:00 PM – 08:00 PM\n"
                            + "• Carry your Student ID card for entry and book borrowing.",
                    "ALL");

            insertFaq(db, "cafeteria", "cafeteria,food,eat,lunch,breakfast,dinner,canteen,restaurant",
                    "Where can I eat on campus?",
                    "🍽️ Campus Cafeterias & Dining:\n\n"
                            + "• Operating Hours: 07:00 AM to 09:00 PM daily.\n"
                            + "• Breakfast (Chai & Vitafunio): 7:00 AM – 10:00 AM.\n"
                            + "• Lunch & Dinner: Meals (Wali, Ugali, Ndizi, Nyama, Maharage, Samaki).\n"
                            + "• Price Range: Affordable student pricing (TZS 1,500 – 3,500).\n"
                            + "• Food kiosks and juice points are situated near every lecture complex.",
                    "ALL");

            insertFaq(db, "fees", "fee,fees,control number,gepg,payment,pay,tuition,bank,crdb,nmb",
                    "How do I pay fees with GePG?",
                    "💳 Fee Payments & GePG Control Numbers\n\n"
                            + "1. Sign in to your SR2 account (sr2.udom.ac.tz).\n"
                            + "2. Go to 'Payment Invoices' and generate a GePG Control Number.\n"
                            + "3. Pay via Mobile Money (M-Pesa, Airtel, Tigo Pesa, HaloPesa) or CRDB / NMB bank.\n"
                            + "4. Retain the SMS confirmation receipt.",
                    "ALL");

            insertFaq(db, "sr2", "sr2,srmis,portal,registration,register,result,results,gpa",
                    "How do I use the SR2 student portal?",
                    "📋 SR2 Student Portal (sr2.udom.ac.tz)\n\n"
                            + "• Official portal for semester course registration, viewing GPA and exam results, generating GePG control numbers, and hostel applications.\n"
                            + "• Log in using your student Registration Number and password.",
                    "ALL");

            insertFaq(db, "transport", "transport,bus,daladala,bajaji,shuttle,travel,movement",
                    "How does campus transport operate?",
                    "🚌 Campus Transport\n\n"
                            + "• Campus shuttles connect the Roundabout to all university colleges.\n"
                            + "• Daladala city buses run directly between Dodoma Town (Posta) and UDOM.\n"
                            + "• Bajaji and Bodaboda drop-off points are stationed at every college entrance gate.",
                    "ALL");

            insertFaq(db, "health", "health,hospital,clinic,doctor,sick,medicine,emergency,treatment",
                    "Where is the health center?",
                    "🏥 UDOM Health Services\n\n"
                            + "• University Health Center is located near Central Campus.\n"
                            + "• Benjamin Mkapa Hospital (BMH) is situated right adjacent to the Health Sciences Campus for specialized treatments.\n"
                            + "• NHIF student cards are accepted for outpatient and emergency medical services.",
                    "ALL");

            seedEnhancedFirstYearFaqs(db);

            Log.d(TAG, "Campus FAQs seeded successfully.");
        } catch (Exception e) {
            Log.e(TAG, "Error seeding campus FAQs: " + e.getMessage(), e);
        }
    }

    private void seedCampusLocations(SQLiteDatabase db) {
        try {
            // CIVE Buildings & Lecture Rooms
            insertLocation(db, "CIVE Lecture Room A", "CIVE", -6.21728, 35.80805, "ACADEMIC", "Main lecture hall in CIVE.", "lecture room a lra cive");
            insertLocation(db, "CIVE Lecture Room B", "CIVE", -6.21745, 35.80852, "ACADEMIC", "Secondary lecture hall in CIVE.", "lecture room b lrb cive");
            insertLocation(db, "CIVE Auditorium", "CIVE", -6.21693, 35.81109, "ACADEMIC", "Large auditorium for major events and classes at CIVE.", "auditorium cive assembly hall");
            insertLocation(db, "CIVE Block 1", "CIVE", -6.21650, 35.80705, "ACADEMIC", "Administration and faculty offices at CIVE.", "block 1 cive admin");
            insertLocation(db, "CIVE Block 2", "CIVE", -6.21672, 35.80755, "ACADEMIC", "Faculty offices and labs at CIVE.", "block 2 cive");
            insertLocation(db, "CIVE Block 3", "CIVE", -6.21685, 35.80812, "ACADEMIC", "Faculty offices and labs at CIVE.", "block 3 cive");
            insertLocation(db, "CIVE Block 4", "CIVE", -6.21705, 35.80860, "ACADEMIC", "Faculty offices and labs at CIVE.", "block 4 cive");
            insertLocation(db, "CIVE Block 5", "CIVE", -6.21720, 35.80915, "ACADEMIC", "Faculty offices and labs at CIVE.", "block 5 cive");
            insertLocation(db, "CIVE Block 6", "CIVE", -6.21740, 35.80965, "ACADEMIC", "Faculty offices and labs at CIVE.", "block 6 cive");
            insertLocation(db, "CIVE Cafeteria", "CIVE", -6.21690, 35.80830, "FOOD", "Primary dining hall for CIVE students.", "cafeteria cive food eat");
            insertLocation(db, "CIVE Main Gate", "CIVE", -6.21620, 35.80620, "TRANSIT", "Main entrance to the CIVE complex.", "gate entrance cive");

            // UDOM Colleges
            insertLocation(db, "CoBE – Business and Economics", "CoBE", -6.21405, 35.82405, "COLLEGE", "College of Business and Economics.", "cobe business economics");
            insertLocation(db, "CoED – College of Education", "CoED", -6.21050, 35.81810, "COLLEGE", "College of Education.", "coed education");
            insertLocation(db, "CHSS – Humanities & Social Sciences", "CHSS", -6.20810, 35.81220, "COLLEGE", "College of Humanities and Social Sciences.", "chss humanities social");
            insertLocation(db, "CNMS – Natural & Mathematical Sciences", "CNMS", -6.21350, 35.81520, "COLLEGE", "College of Natural and Mathematical Sciences.", "cnms science math natural");
            insertLocation(db, "CoESE – Earth Sciences & Engineering", "CoESE", -6.21900, 35.80510, "COLLEGE", "College of Earth Sciences and Engineering.", "coese engineering earth");

            // Central Facilities
            insertLocation(db, "UDOM Central Library", "ALL", -6.21250, 35.81950, "ACADEMIC", "The main university library.", "library central study");
            insertLocation(db, "UDOM Administration Block", "ALL", -6.21470, 35.82470, "ADMIN", "Central administration and management offices.", "admin administration management");
            insertLocation(db, "UDOM Health Centre", "ALL", -6.22050, 35.82510, "HEALTH", "University dispensary and health services.", "health hospital dispensary clinic");
            insertLocation(db, "UDOM Roundabout", "ALL", -6.21550, 35.81600, "TRANSIT", "Central transit hub for campus shuttles.", "roundabout transit bus daladala");

            Log.d(TAG, "Campus locations seeded successfully.");
        } catch (Exception e) {
            Log.e(TAG, "Error seeding campus locations: " + e.getMessage(), e);
        }
    }
}
