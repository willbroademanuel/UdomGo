package com.example.udomgo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * DatabaseHelper manages the SQLite database for UDOMGo.
 * Handles database creation, version upgrades, and CRUD operations
 * for registered users, persistent chat history, and campus FAQ knowledge base.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";

    // Database Information
    public static final String DATABASE_NAME = "udomgo.db";
    public static final int DATABASE_VERSION = 3;

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

    // Singleton instance
    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Table 1: Users Table
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_NAME + " TEXT NOT NULL, "
                + COLUMN_EMAIL + " TEXT UNIQUE NOT NULL, "
                + COLUMN_PASSWORD + " TEXT NOT NULL, "
                + COLUMN_COLLEGE + " TEXT DEFAULT 'CIVE', "
                + COLUMN_CREATED_AT + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                + ");";

        // Table 2: Chat History Table (Foreign Key -> users.email)
        String CREATE_CHAT_TABLE = "CREATE TABLE " + TABLE_CHAT_HISTORY + " ("
                + COLUMN_CHAT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_CHAT_USER_EMAIL + " TEXT NOT NULL, "
                + COLUMN_CHAT_SENDER + " TEXT NOT NULL, "
                + COLUMN_CHAT_MESSAGE + " TEXT NOT NULL, "
                + COLUMN_CHAT_TIMESTAMP + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                + "FOREIGN KEY(" + COLUMN_CHAT_USER_EMAIL + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_EMAIL + ") ON DELETE CASCADE"
                + ");";

        // Table 3: Campus FAQs Table (Knowledge Base)
        String CREATE_FAQS_TABLE = "CREATE TABLE " + TABLE_CAMPUS_FAQS + " ("
                + COLUMN_FAQ_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_FAQ_CATEGORY + " TEXT NOT NULL, "
                + COLUMN_FAQ_KEYWORDS + " TEXT NOT NULL, "
                + COLUMN_FAQ_QUESTION + " TEXT NOT NULL, "
                + COLUMN_FAQ_ANSWER + " TEXT NOT NULL, "
                + COLUMN_FAQ_COLLEGE + " TEXT DEFAULT 'ALL'"
                + ");";

        db.execSQL(CREATE_USERS_TABLE);
        db.execSQL(CREATE_CHAT_TABLE);
        db.execSQL(CREATE_FAQS_TABLE);
        Log.d(TAG, "All 3 tables created successfully.");

        // Pre-populate built-in demo account
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, "UDOM Student");
        values.put(COLUMN_EMAIL, "student@udom.ac.tz");
        values.put(COLUMN_PASSWORD, "password123");
        values.put(COLUMN_COLLEGE, "CIVE");
        db.insert(TABLE_USERS, null, values);

        // Pre-populate Campus Knowledge Base in SQLite
        seedCampusFaqs(db);
        Log.d(TAG, "Database seed data inserted successfully.");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);
        if (oldVersion < 2) {
            String CREATE_CHAT_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_CHAT_HISTORY + " ("
                    + COLUMN_CHAT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COLUMN_CHAT_USER_EMAIL + " TEXT NOT NULL, "
                    + COLUMN_CHAT_SENDER + " TEXT NOT NULL, "
                    + COLUMN_CHAT_MESSAGE + " TEXT NOT NULL, "
                    + COLUMN_CHAT_TIMESTAMP + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                    + ");";
            db.execSQL(CREATE_CHAT_TABLE);
        }
        if (oldVersion < 3) {
            String CREATE_FAQS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_CAMPUS_FAQS + " ("
                    + COLUMN_FAQ_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COLUMN_FAQ_CATEGORY + " TEXT NOT NULL, "
                    + COLUMN_FAQ_KEYWORDS + " TEXT NOT NULL, "
                    + COLUMN_FAQ_QUESTION + " TEXT NOT NULL, "
                    + COLUMN_FAQ_ANSWER + " TEXT NOT NULL, "
                    + COLUMN_FAQ_COLLEGE + " TEXT DEFAULT 'ALL'"
                    + ");";
            db.execSQL(CREATE_FAQS_TABLE);
            seedCampusFaqs(db);
        }
    }

    // ==========================================
    // CRUD OPERATIONS: USERS
    // ==========================================

    public boolean insertUser(String name, String email, String password, String college) {
        if (email == null || password == null) return false;

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name != null ? name.trim() : "Student");
        values.put(COLUMN_EMAIL, email.trim().toLowerCase());
        values.put(COLUMN_PASSWORD, password);
        values.put(COLUMN_COLLEGE, college != null ? college : "CIVE");

        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }

    public boolean checkEmailExists(String email) {
        if (email == null) return false;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(
                    TABLE_USERS,
                    new String[]{COLUMN_ID},
                    "LOWER(" + COLUMN_EMAIL + ") = ?",
                    new String[]{email.trim().toLowerCase()},
                    null, null, null
            );
            return cursor != null && cursor.getCount() > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error checking email existence: " + e.getMessage());
            return false;
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    public boolean checkUserCredentials(String email, String password) {
        if (email == null || password == null) return false;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(
                    TABLE_USERS,
                    new String[]{COLUMN_ID},
                    "LOWER(" + COLUMN_EMAIL + ") = ? AND " + COLUMN_PASSWORD + " = ?",
                    new String[]{email.trim().toLowerCase(), password},
                    null, null, null
            );
            return cursor != null && cursor.getCount() > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error validating user credentials: " + e.getMessage());
            return false;
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    public String getUserName(String email) {
        if (email == null) return "Student";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
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
            Log.e(TAG, "Error retrieving user name: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
        }
        return "Student";
    }

    public String getUserCollege(String email) {
        if (email == null) return "CIVE";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
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
            Log.e(TAG, "Error retrieving user college: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
        }
        return "CIVE";
    }

    public boolean updateUserCollege(String email, String college) {
        if (email == null || college == null) return false;

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
    }

    // ==========================================
    // CRUD OPERATIONS: CHAT HISTORY
    // ==========================================

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

    public boolean insertChatMessage(String userEmail, String sender, String message) {
        if (userEmail == null || message == null) return false;

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CHAT_USER_EMAIL, userEmail.trim().toLowerCase());
        values.put(COLUMN_CHAT_SENDER, sender != null ? sender : "user");
        values.put(COLUMN_CHAT_MESSAGE, message);

        long result = db.insert(TABLE_CHAT_HISTORY, null, values);
        return result != -1;
    }

    public List<ChatMessageItem> getChatHistory(String userEmail) {
        List<ChatMessageItem> messages = new ArrayList<>();
        if (userEmail == null) return messages;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(
                    TABLE_CHAT_HISTORY,
                    new String[]{COLUMN_CHAT_ID, COLUMN_CHAT_USER_EMAIL, COLUMN_CHAT_SENDER, COLUMN_CHAT_MESSAGE, COLUMN_CHAT_TIMESTAMP},
                    "LOWER(" + COLUMN_CHAT_USER_EMAIL + ") = ?",
                    new String[]{userEmail.trim().toLowerCase()},
                    null, null,
                    COLUMN_CHAT_ID + " ASC"
            );

            if (cursor != null && cursor.moveToFirst()) {
                int idIdx = cursor.getColumnIndex(COLUMN_CHAT_ID);
                int emailIdx = cursor.getColumnIndex(COLUMN_CHAT_USER_EMAIL);
                int senderIdx = cursor.getColumnIndex(COLUMN_CHAT_SENDER);
                int msgIdx = cursor.getColumnIndex(COLUMN_CHAT_MESSAGE);
                int timeIdx = cursor.getColumnIndex(COLUMN_CHAT_TIMESTAMP);

                do {
                    long id = cursor.getLong(idIdx);
                    String email = cursor.getString(emailIdx);
                    String sender = cursor.getString(senderIdx);
                    String msg = cursor.getString(msgIdx);
                    String time = cursor.getString(timeIdx);

                    messages.add(new ChatMessageItem(id, email, sender, msg, time));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error fetching chat history: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
        }
        return messages;
    }

    public boolean clearChatHistory(String userEmail) {
        if (userEmail == null) return false;

        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(
                TABLE_CHAT_HISTORY,
                "LOWER(" + COLUMN_CHAT_USER_EMAIL + ") = ?",
                new String[]{userEmail.trim().toLowerCase()}
        );
        return rows >= 0;
    }

    // ==========================================
    // CRUD OPERATIONS: CAMPUS FAQS (KNOWLEDGE BASE)
    // ==========================================

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

    /**
     * Inserts an FAQ record into the SQLite database.
     */
    public boolean insertFaq(SQLiteDatabase db, String category, String keywords, String question, String answer, String collegeSpecific) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_FAQ_CATEGORY, category);
        values.put(COLUMN_FAQ_KEYWORDS, keywords);
        values.put(COLUMN_FAQ_QUESTION, question);
        values.put(COLUMN_FAQ_ANSWER, answer);
        values.put(COLUMN_FAQ_COLLEGE, collegeSpecific != null ? collegeSpecific : "ALL");

        long result = db.insert(TABLE_CAMPUS_FAQS, null, values);
        return result != -1;
    }

    /**
     * Searches the SQLite knowledge base for an answer matching user's query and college.
     *
     * @param userQuery   Raw query typed by the student
     * @param userCollege Currently selected college (e.g. CIVE, CoBE)
     * @return Answer text retrieved from SQLite, or null if no match found
     */
    public String findFaqAnswer(String userQuery, String userCollege) {
        if (userQuery == null || userQuery.trim().isEmpty()) return null;

        String query = userQuery.toLowerCase().trim();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.query(
                    TABLE_CAMPUS_FAQS,
                    new String[]{COLUMN_FAQ_ID, COLUMN_FAQ_CATEGORY, COLUMN_FAQ_KEYWORDS, COLUMN_FAQ_ANSWER, COLUMN_FAQ_COLLEGE},
                    null, null, null, null,
                    COLUMN_FAQ_ID + " ASC"
            );

            if (cursor != null && cursor.moveToFirst()) {
                int keyIdx = cursor.getColumnIndex(COLUMN_FAQ_KEYWORDS);
                int ansIdx = cursor.getColumnIndex(COLUMN_FAQ_ANSWER);
                int colIdx = cursor.getColumnIndex(COLUMN_FAQ_COLLEGE);

                String bestMatchAnswer = null;

                do {
                    String keywordsStr = cursor.getString(keyIdx);
                    String answer = cursor.getString(ansIdx);
                    String college = cursor.getString(colIdx);

                    // Check college affinity if college-specific
                    boolean collegeMatches = "ALL".equalsIgnoreCase(college) ||
                            (userCollege != null && userCollege.equalsIgnoreCase(college));

                    if (keywordsStr != null) {
                        String[] keywords = keywordsStr.split(",");
                        for (String kw : keywords) {
                            String trimmedKw = kw.trim().toLowerCase();
                            if (!trimmedKw.isEmpty() && query.contains(trimmedKw)) {
                                if (collegeMatches) {
                                    return answer; // Exact specific match
                                } else if (bestMatchAnswer == null) {
                                    bestMatchAnswer = answer;
                                }
                            }
                        }
                    }
                } while (cursor.moveToNext());

                return bestMatchAnswer;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error finding FAQ answer in SQLite: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
        }

        return null;
    }

    /**
     * Seeds the initial campus knowledge base into SQLite.
     */
    private void seedCampusFaqs(SQLiteDatabase db) {
        // CIVE
        insertFaq(db, "cive", "cive,informatics,computer,software,programming,cyber",
                "What is CIVE?",
                "💻 College of Informatics and Virtual Education (CIVE)\n\n"
                        + "• Location: North-East campus zone.\n"
                        + "• Facilities: Lecture Rooms A & B, Auditorium, Blocks 1–6, Computer Laboratories.\n"
                        + "• Programs: Computer Science, Software Engineering, Telecommunications, Information Systems, Cyber Security.",
                "CIVE");

        // CoBE
        insertFaq(db, "cobe", "cobe,business,economics,accounting,finance,marketing,procurement",
                "What is CoBE?",
                "🏛️ College of Business and Economics (CoBE)\n\n"
                        + "• Location: Central campus near administration block.\n"
                        + "• Facilities: CoBE Auditoriums, lecture halls, and departmental library.\n"
                        + "• Programs: Accounting, Finance, Marketing, Economics, Human Resources, Procurement.",
                "CoBE");

        // CoED
        insertFaq(db, "coed", "coed,education,teacher,teaching,pedagogy",
                "What is CoED?",
                "🎓 College of Education (CoED)\n\n"
                        + "• Location: Central academic wing.\n"
                        + "• Focus: Science and Arts teacher education, pedagogy, and educational administration.",
                "CoED");

        // CHSS
        insertFaq(db, "chss", "chss,humanities,social,sociology,languages,history,geography",
                "What is CHSS?",
                "📚 College of Humanities and Social Sciences (CHSS)\n\n"
                        + "• Location: West campus area.\n"
                        + "• Departments: Sociology, Political Science, Languages, History, Geography.",
                "CHSS");

        // CNMS
        insertFaq(db, "cnms", "cnms,natural,math,science,physics,chemistry,biology",
                "What is CNMS?",
                "🔬 College of Natural and Mathematical Sciences (CNMS)\n\n"
                        + "• Location: Science complex area.\n"
                        + "• Facilities: Mathematics research rooms, advanced physics, chemistry, biology laboratories.",
                "CNMS");

        // CoESE
        insertFaq(db, "coese", "coese,engineering,mining,geology,earth,petroleum,energy",
                "What is CoESE?",
                "🏗️ College of Earth Sciences and Engineering (CoESE)\n\n"
                        + "• Location: Engineering campus wing.\n"
                        + "• Programs: Mining Engineering, Petroleum Geology, Environmental Engineering, Renewable Energy.",
                "CoESE");

        // School of Law
        insertFaq(db, "sol", "law,sol,llb,legal,court,lawyer",
                "What is School of Law?",
                "⚖️ School of Law (SoL)\n\n"
                        + "• Offers Bachelor of Laws (LL.B) and postgraduate legal programs with a dedicated campus moot court.",
                "SoL");

        // School of Medicine & Nursing
        insertFaq(db, "somd", "somd,medicine,doctor,nursing,sonph,health,pharmacy",
                "What is School of Medicine & Nursing?",
                "🩺 School of Medicine & Nursing (SoMD / SoNPH)\n\n"
                        + "• Location: Health Sciences campus near Benjamin Mkapa Hospital.\n"
                        + "• Programs: Doctor of Medicine (MD), Pharmacy, Nursing, and Public Health.",
                "SoMD");

        // All Colleges
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

        // Hostels & Accommodation
        insertFaq(db, "hostels", "hostel,hostels,accommodation,dorm,dorms,sleep,housing",
                "How do hostels work at UDOM?",
                "🏠 UDOM Accommodation & Hostels\n\n"
                        + "• On-Campus: Rooms are organized per college blocks. You apply via your SR2 portal.\n"
                        + "• Hostel blocks are designated within your college complex zone.\n"
                        + "• Off-Campus: Popular student areas include Mkonze, Kisasa, Medeli, and Chimwaga.",
                "ALL");

        // Library
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

        // Cafeteria & Food
        insertFaq(db, "cafeteria", "cafeteria,food,eat,lunch,breakfast,dinner,canteen,restaurant",
                "Where can I eat on campus?",
                "🍽️ Campus Cafeterias & Dining:\n\n"
                        + "• Operating Hours: 07:00 AM to 09:00 PM daily.\n"
                        + "• Breakfast (Chai & Vitafunio): 7:00 AM – 10:00 AM.\n"
                        + "• Lunch & Dinner: Meals (Wali, Ugali, Ndizi, Nyama, Maharage, Samaki).\n"
                        + "• Food kiosks and juice points are situated near every lecture complex.",
                "ALL");

        // Fees & GePG
        insertFaq(db, "fees", "fee,fees,control number,gepg,payment,pay,tuition,bank,crdb,nmb",
                "How do I pay fees with GePG?",
                "💳 Fee Payments & GePG Control Numbers\n\n"
                        + "1. Sign in to your SR2 account (sr2.udom.ac.tz).\n"
                        + "2. Go to 'Payment Invoices' and generate a GePG Control Number.\n"
                        + "3. Pay via Mobile Money (M-Pesa, Airtel, Tigo Pesa, HaloPesa) or CRDB / NMB bank.\n"
                        + "4. Retain the SMS confirmation receipt.",
                "ALL");

        // SR2 Portal
        insertFaq(db, "sr2", "sr2,srmis,portal,registration,register,result,results,gpa",
                "How do I use the SR2 student portal?",
                "📋 SR2 Student Portal (sr2.udom.ac.tz)\n\n"
                        + "• Official portal for semester course registration, viewing GPA and exam results, generating GePG control numbers, and hostel applications.\n"
                        + "• Log in using your student Registration Number and password.",
                "ALL");

        // Transport
        insertFaq(db, "transport", "transport,bus,daladala,bajaji,shuttle,travel,movement",
                "How does campus transport operate?",
                "🚌 Campus Transport\n\n"
                        + "• Campus shuttles connect the Roundabout to all university colleges.\n"
                        + "• Daladala city buses run directly between Dodoma Town (Posta) and UDOM.\n"
                        + "• Bajaji and Bodaboda drop-off points are stationed at every college entrance gate.",
                "ALL");

        // Health Center
        insertFaq(db, "health", "health,hospital,clinic,doctor,sick,medicine,emergency,treatment",
                "Where is the health center?",
                "🏥 UDOM Health Services\n\n"
                        + "• University Health Center is located near Central Campus.\n"
                        + "• Benjamin Mkapa Hospital (BMH) is situated right adjacent to the Health Sciences Campus for specialized treatments.\n"
                        + "• NHIF student cards are accepted for outpatient and emergency medical services.",
                "ALL");
    }
}
