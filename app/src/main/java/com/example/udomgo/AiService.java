package com.example.udomgo;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * AiService handles communication with Google Gemini API using Tool / Function Calling.
 * It connects the Gemini Generative AI model with local SQLite data in DatabaseHelper,
 * allowing the model to look up Software Engineering timetables, professors, office numbers,
 * UE examination dates, and student leaders dynamically with zero hallucination.
 * Includes complete error handling, timeouts, and UI thread callbacks.
 */
public class AiService {

    private static final String TAG = "AiService";
    private static final MediaType JSON_MEDIA = MediaType.parse("application/json; charset=utf-8");

    public static String getModelName() {
        String model = BuildConfig.AI_MODEL_NAME;
        if (model == null || model.trim().isEmpty()) {
            return "gemini-1.5-flash";
        }
        return model.trim();
    }

    private static String getApiUrl() {
        return "https://generativelanguage.googleapis.com/v1beta/models/" + getModelName() + ":generateContent?key=";
    }

    public interface AiCallback {


        void onSuccess(String responseText);
        void onError(String errorMessage);
    }

    private final Context context;
    private final OkHttpClient httpClient;
    private final Handler mainHandler;

    public AiService(@NonNull Context context) {
        this.context = context.getApplicationContext();
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .build();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * Checks whether an API key has been configured in local.properties / BuildConfig.
     */
    public static boolean isApiKeyConfigured() {
        return BuildConfig.AI_API_KEY != null && !BuildConfig.AI_API_KEY.trim().isEmpty();
    }

    /**
     * Sends the student's question to Gemini with function calling tools.
     */
    public void askAssistant(String userMessage, String userCollege, AiCallback callback) {
        askAssistant(userMessage, userCollege, null, callback);
    }

    /**
     * Sends the student's question to Gemini with multi-turn context and SQL database tool.
     */
    public void askAssistant(String userMessage, String userCollege,
                             List<DatabaseHelper.ChatMessageItem> recentHistory,
                             AiCallback callback) {
        if (callback == null) return;

        if (userMessage == null || userMessage.trim().isEmpty()) {
            postError(callback, "Query cannot be empty.");
            return;
        }

        String apiKey = BuildConfig.AI_API_KEY;
        if (apiKey == null || apiKey.trim().isEmpty()) {
            postError(callback, "NO_API_KEY");
            return;
        }

        try {
            JSONObject requestBody = buildInitialPayload(userMessage.trim(), userCollege, recentHistory);
            executeGeminiRequest(requestBody, userMessage, userCollege, callback);
        } catch (Exception e) {
            Log.e(TAG, "Error building Gemini payload: " + e.getMessage(), e);
            postError(callback, "Failed to initialize query: " + e.getMessage());
        }
    }

    private JSONObject buildInitialPayload(String userMessage, String userCollege,
                                          List<DatabaseHelper.ChatMessageItem> recentHistory) throws JSONException {
        JSONObject payload = new JSONObject();

        // System Instruction with UDOM database schema and guidelines
        JSONObject systemInstruction = new JSONObject();
        JSONArray sysParts = new JSONArray();
        JSONObject sysText = new JSONObject();

        StringBuilder sysPrompt = new StringBuilder();
        sysPrompt.append("You are UDOMGo Assistant, the official intelligent campus AI for the University of Dodoma (UDOM) in Tanzania.\n")
                .append("Active student profile: College of ").append(userCollege != null ? userCollege : "CIVE").append(".\n")
                .append("Context: Current active academic year is 2025/2026. Current active semester is Semester 2.\n\n")
                .append("DATABASE ACCESS VIA queryDatabase TOOL:\n")
                .append("You have direct access to the campus SQLite database via the queryDatabase tool. Write precise, efficient read-only SELECT SQL queries to look up information.\n\n")
                .append("FULL DATABASE SCHEMA INDEX & COLUMN SPECIFICATIONS:\n")
                .append("1. academic_calendar:\n")
                .append("   Columns: id (INTEGER), academic_year (TEXT), semester (TEXT), event_name (TEXT), category (TEXT), start_date (TEXT YYYY-MM-DD), end_date (TEXT YYYY-MM-DD), description (TEXT)\n")
                .append("   - academic_year values: '2024/2025', '2025/2026', '2026/2027'\n")
                .append("   - semester values: 'Semester I', 'Semester II', 'Break', 'ALL'\n")
                .append("   - category values: 'ACADEMIC', 'EXAM', 'TEST', 'ORIENTATION', 'CEREMONY', 'BREAK', 'IPT', 'SPORTS', 'MEETING', 'HOLIDAY'\n")
                .append("   - Query Tip: Search using LOWER(event_name) LIKE '%term%' OR LOWER(description) LIKE '%term%'. For exam dates, filter specifically for exam events (e.g., supplementary, UE, CAT).\n\n")
                .append("2. timetables:\n")
                .append("   Columns: id (INTEGER), programme (TEXT), year_of_study (INTEGER), semester (INTEGER), day_of_week (TEXT), start_time (TEXT), end_time (TEXT), course_code (TEXT), course_name (TEXT), session_type (TEXT), venue (TEXT), lecturer_name (TEXT)\n")
                .append("   - programme: Always use LIKE '%...%' (e.g. programme LIKE '%Software Engineering%').\n")
                .append("   - year_of_study: Integer 1, 2, 3, or 4.\n")
                .append("   - semester: Integer 1 or 2 (Current active semester is 2).\n")
                .append("   - day_of_week values: 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday'. IMPORTANT: For multiple days or ranges, use IN operator (e.g. day_of_week IN ('Monday', 'Tuesday')).\n")
                .append("   - session_type values: 'Theory', 'Practical', 'Tutorial', 'Clinic', 'Sports'\n\n")
                .append("3. professors:\n")
                .append("   Columns: id (INTEGER), name (TEXT), college (TEXT), department (TEXT), office_block (TEXT), office_room (TEXT), phone (TEXT), email (TEXT), subjects_taught (TEXT), target_years (TEXT), consultation_hours (TEXT)\n")
                .append("   - Query Tip: Search using LOWER(name) LIKE '%...%' OR LOWER(subjects_taught) LIKE '%...%' OR LOWER(department) LIKE '%...%'\n\n")
                .append("4. courses:\n")
                .append("   Columns: id (INTEGER), programme (TEXT), year_of_study (INTEGER), semester (INTEGER), course_code (TEXT), course_title (TEXT), credits (REAL), course_status (TEXT), category (TEXT)\n")
                .append("   - programme: 'BSc Software Engineering'\n")
                .append("   - course_status: 'Core', 'Elective'\n")
                .append("   - category: 'Foundational', 'Core Software', 'Math/Stat', 'Security', 'IPT', 'FYP', 'Elective'\n\n")
                .append("5. student_leaders:\n")
                .append("   Columns: id (INTEGER), name (TEXT), role (TEXT), college (TEXT), office_or_hostel (TEXT), room_number (TEXT), phone (TEXT), email (TEXT)\n")
                .append("   - Covers: CRs, UDOSO Ministers, College Presidents, Wardens.\n\n")
                .append("6. campus_contacts:\n")
                .append("   Columns: id (INTEGER), department_or_office (TEXT), category (TEXT), college (TEXT), phone_number (TEXT), email (TEXT), office_location (TEXT), description (TEXT), keywords (TEXT)\n")
                .append("   - category values: 'EMERGENCY', 'ACADEMIC', 'SUPPORT', 'HOSTEL', 'ADMIN', 'GENERAL'\n\n")
                .append("7. campus_locations:\n")
                .append("   Columns: id (INTEGER), name (TEXT), college (TEXT), latitude (REAL), longitude (REAL), category (TEXT), description (TEXT), keywords (TEXT)\n")
                .append("   - category values: 'ACADEMIC', 'FOOD', 'TRANSIT', 'COLLEGE', 'HEALTH', 'ADMIN', 'ACCOMMODATION'\n\n")
                .append("8. campus_faqs:\n")
                .append("   Columns: id (INTEGER), category (TEXT), keywords (TEXT), question (TEXT), answer (TEXT), college_specific (TEXT)\n")
                .append("   - category values: 'freshers', 'heslb', 'grades', 'wifi', 'rules', 'banking', 'hostels', 'library', 'cafeteria', 'fees', 'sr2', 'transport', 'health', 'cive', 'cobe', 'coed', 'chss', 'cnms', 'coese', 'sol', 'somd', 'colleges'\n\n")
                .append("RESPONSE & UX PRINCIPLES:\n")
                .append("1. TONE & STYLE: NATURAL, FORMAL & CHARMING:\n")
                .append("   - Maintain an articulate, elegant, polite, and charming university tone.\n")
                .append("   - NO STORIES OR FILLER: Deliver facts clearly, gracefully, and directly.\n")
                .append("   - Only use Markdown tables when the student requests a full timetable, schedule, course list, or multi-item comparison.\n")
                .append("   - Format dates pleasantly in natural speech (e.g. '09 March to 20 March 2026').\n")
                .append("2. ACCURACY & NATURAL RESPONSE FOR MISSING DATA:\n")
                .append("   - Base your answers strictly on the records returned by queryDatabase.\n")
                .append("   - When queryDatabase returns 0 rows (rowCount: 0 or empty rows), DO NOT output raw error notices or make up fake facts.\n")
                .append("   - Instead, in your own natural, charming AI voice, explain politely that the campus database currently does not have records for that specific request (e.g., 'We don't have that specific data recorded in our campus database yet...').\n")
                .append("   - Explain briefly what was searched and suggest related topics or data that ARE available in the database (e.g., Software Engineering timetables, exam dates, professor offices, or campus FAQs).\n")
                .append("3. ALWAYS conclude your final response with 2 to 3 helpful, clickable follow-up suggestions for the student, each on its own line prefixed with '>> '.\n")
                .append("4. Forgive typos in user questions (e.g. 'supplimentary' -> supplementary, 'daen' -> Dean, 'softwear' -> Software Engineering).\n")
                .append("5. STRICT PROHIBITION ON RAW DUMPS: NEVER print raw database column names, raw key-value pairs (e.g. 'name: ... | latitude: ...'), raw GPS coordinates, or search keyword tags. Synthesize all retrieved records into warm, natural, human conversational English appropriate for a student assistant.");

        sysText.put("text", sysPrompt.toString());
        sysParts.put(sysText);
        systemInstruction.put("parts", sysParts);
        payload.put("system_instruction", systemInstruction);

        // Contents (Multi-turn conversational history + current user message)
        JSONArray contents = new JSONArray();

        String lastRole = null;
        if (recentHistory != null && !recentHistory.isEmpty()) {
            // Exclude the current user message if it was already inserted into SQLite right before this call
            int historyEnd = recentHistory.size();
            DatabaseHelper.ChatMessageItem lastItem = recentHistory.get(historyEnd - 1);
            if ("user".equalsIgnoreCase(lastItem.sender) && userMessage.equalsIgnoreCase(lastItem.message)) {
                historyEnd--;
            }

            // Keep up to last 6 messages for short-term conversational context
            int maxRecent = 6;
            int startIdx = Math.max(0, historyEnd - maxRecent);

            for (int i = startIdx; i < historyEnd; i++) {
                DatabaseHelper.ChatMessageItem item = recentHistory.get(i);
                if (item.message == null || item.message.trim().isEmpty()) continue;

                String msgText = item.message.trim();
                // Exclude system welcome banners or thinking indicators
                if (msgText.startsWith("👋 Hello! Welcome") || msgText.startsWith("⏳ Thinking")) continue;

                boolean isUserMsg = "user".equalsIgnoreCase(item.sender);
                String role = isUserMsg ? "user" : "model";

                // Clean follow-up suggestion lines from past model turns to keep context tidy
                if (!isUserMsg && msgText.contains(">>")) {
                    StringBuilder cleanPast = new StringBuilder();
                    for (String line : msgText.split("\n")) {
                        if (!line.trim().startsWith(">>")) {
                            cleanPast.append(line).append("\n");
                        }
                    }
                    msgText = cleanPast.toString().trim();
                }

                if (msgText.isEmpty()) continue;

                // Ensure history starts with user
                if (contents.length() == 0 && !"user".equals(role)) {
                    continue;
                }

                if (role.equals(lastRole)) {
                    try {
                        JSONObject prevTurn = contents.getJSONObject(contents.length() - 1);
                        JSONArray partsArr = prevTurn.getJSONArray("parts");
                        JSONObject addPart = new JSONObject();
                        addPart.put("text", msgText);
                        partsArr.put(addPart);
                    } catch (Exception ignored) {}
                } else {
                    JSONObject turn = new JSONObject();
                    turn.put("role", role);
                    JSONArray parts = new JSONArray();
                    JSONObject part = new JSONObject();
                    part.put("text", msgText);
                    parts.put(part);
                    turn.put("parts", parts);
                    contents.put(turn);
                    lastRole = role;
                }
            }
        }

        // Append current user turn
        if (contents.length() > 0 && "user".equals(lastRole)) {
            JSONObject prevTurn = contents.getJSONObject(contents.length() - 1);
            JSONArray partsArr = prevTurn.getJSONArray("parts");
            JSONObject addPart = new JSONObject();
            addPart.put("text", userMessage);
            partsArr.put(addPart);
        } else {
            JSONObject currentTurn = new JSONObject();
            currentTurn.put("role", "user");
            JSONArray parts = new JSONArray();
            JSONObject part = new JSONObject();
            part.put("text", userMessage);
            parts.put(part);
            currentTurn.put("parts", parts);
            contents.put(currentTurn);
        }
        payload.put("contents", contents);

        // Tools / Function Declaration: queryDatabase
        JSONArray tools = new JSONArray();
        JSONObject toolsObj = new JSONObject();
        JSONArray funcDecls = new JSONArray();

        JSONObject tSql = new JSONObject();
        tSql.put("name", "queryDatabase");
        tSql.put("description", "Executes a read-only SELECT SQL query against the UDOM SQLite database to retrieve exact campus information across academic calendar, timetables, professors, courses, student leaders, campus contacts, and FAQs.");
        JSONObject pSql = new JSONObject();
        pSql.put("type", "OBJECT");
        JSONObject propsSql = new JSONObject();
        propsSql.put("sql", new JSONObject().put("type", "STRING").put("description", "A valid SQLite SELECT query. Only SELECT queries are permitted. Use LIKE with % for flexible text matching."));
        pSql.put("properties", propsSql);
        JSONArray reqList = new JSONArray();
        reqList.put("sql");
        pSql.put("required", reqList);
        tSql.put("parameters", pSql);
        funcDecls.put(tSql);

        toolsObj.put("function_declarations", funcDecls);
        tools.put(toolsObj);
        payload.put("tools", tools);

        return payload;
    }

    private void executeGeminiRequest(JSONObject payload, String originalUserMessage,
                                      String userCollege, AiCallback callback) {
        String url = getApiUrl() + BuildConfig.AI_API_KEY.trim();
        RequestBody body = RequestBody.create(payload.toString(), JSON_MEDIA);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Gemini network call failed: " + e.getMessage());
                postError(callback, "Network connection error: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "No error body";
                    Log.e(TAG, "Gemini API error (" + response.code() + "): " + errorBody);
                    postError(callback, "Gemini API returned error code " + response.code());
                    return;
                }

                try {
                    String responseString = response.body() != null ? response.body().string() : "";
                    JSONObject jsonResponse = new JSONObject(responseString);
                    handleGeminiResponse(jsonResponse, payload, originalUserMessage, userCollege, callback);
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing Gemini response: " + e.getMessage(), e);
                    postError(callback, "Failed to parse AI response: " + e.getMessage());
                }
            }
        });
    }

    private void handleGeminiResponse(JSONObject jsonResponse, JSONObject previousPayload,
                                      String originalUserMessage, String userCollege,
                                      AiCallback callback) throws JSONException {
        JSONArray candidates = jsonResponse.optJSONArray("candidates");
        if (candidates == null || candidates.length() == 0) {
            postError(callback, "No response generated by the model.");
            return;
        }

        JSONObject firstCandidate = candidates.getJSONObject(0);
        JSONObject content = firstCandidate.optJSONObject("content");
        if (content == null) {
            postError(callback, "Empty candidate content.");
            return;
        }

        JSONArray parts = content.optJSONArray("parts");
        if (parts == null || parts.length() == 0) {
            postError(callback, "Empty response parts.");
            return;
        }

        // Collect all function calls from the model turn (supports parallel tool calls)
        java.util.List<JSONObject> functionCalls = new java.util.ArrayList<>();
        for (int i = 0; i < parts.length(); i++) {
            JSONObject part = parts.getJSONObject(i);
            if (part.has("functionCall")) {
                functionCalls.add(part.getJSONObject("functionCall"));
            }
        }

        if (!functionCalls.isEmpty()) {
            Log.d(TAG, "AI requested " + functionCalls.size() + " tool execution(s)");
            submitAllFunctionResponsesToGemini(previousPayload, content, functionCalls, userCollege, callback);
            return;
        }

        // Direct natural text answer
        StringBuilder textBuilder = new StringBuilder();
        for (int i = 0; i < parts.length(); i++) {
            JSONObject part = parts.getJSONObject(i);
            if (part.has("text")) {
                textBuilder.append(part.getString("text"));
            }
        }

        if (textBuilder.length() > 0) {
            postSuccess(callback, textBuilder.toString());
            return;
        }

        postError(callback, "Received unrecognized response structure from AI.");
    }

    /**
     * Executes the requested tool locally against the SQLite database.
     */
    private String executeLocalTool(String functionName, JSONObject args, String userCollege) {
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(context);
        if (dbHelper == null) return "{\"error\": \"Database not available.\"}";

        if ("queryDatabase".equals(functionName)) {
            String sql = args.optString("sql", "");
            if (sql.trim().isEmpty()) {
                return "{\"error\": \"SQL query cannot be empty.\"}";
            }
            Log.d(TAG, "Executing AI SQL query: " + sql);
            JSONArray rows = dbHelper.executeReadOnlySql(sql);
            JSONObject resultObj = new JSONObject();
            try {
                resultObj.put("rowCount", rows.length());
                resultObj.put("rows", rows);
                if (rows.length() == 0) {
                    resultObj.put("message", "No matching records found in database.");
                }
            } catch (Exception ignored) {}
            return resultObj.toString();
        }

        return "{\"error\": \"Unknown tool: " + functionName + "\"}";
    }

    private String buildHumanFallback(String fnName, String jsonResult, JSONObject args) {
        try {
            JSONObject obj = new JSONObject(jsonResult);
            if (obj.has("error")) {
                return "I ran into a temporary issue accessing campus records: " + obj.getString("error")
                        + "\n\n>> Show BSc Software Engineering timetable\n>> When are University Examinations (UE)?\n>> Where is Dr. Mwamba's office?";
            }

            JSONArray rows = obj.optJSONArray("rows");
            if (rows == null || rows.length() == 0) {
                return "We don't have that specific record in our campus database currently.\n\n"
                        + "You can ask me about:\n"
                        + "• Software Engineering timetables & courses\n"
                        + "• Professor office numbers & consultation hours\n"
                        + "• UE examination dates & academic almanac\n"
                        + "• Student leaders, CRs, and campus emergency contacts\n\n"
                        + ">> Show BSc Software Engineering timetable\n>> When are University Examinations (UE)?\n>> Where is Dr. Mwamba's office?";
            }

            JSONObject firstRow = rows.getJSONObject(0);

            // 1. Campus FAQs / Knowledge Base
            if (firstRow.has("answer")) {
                return firstRow.optString("answer");
            }

            // 2. Academic Calendar / Almanac
            if (firstRow.has("event_name")) {
                StringBuilder sb = new StringBuilder("📅 **UDOM Academic Calendar & Examination Schedule**:\n\n");
                for (int i = 0; i < Math.min(rows.length(), 6); i++) {
                    JSONObject r = rows.getJSONObject(i);
                    String ev = r.optString("event_name", "Academic Event");
                    String start = r.optString("start_date", "");
                    String end = r.optString("end_date", "");
                    String desc = r.optString("description", "");
                    String sem = r.optString("semester", "Semester");

                    String friendlyDates = DatabaseHelper.formatFriendlyDateRange(start, end);

                    sb.append("• **").append(ev).append("** (").append(sem).append(")\n");
                    if (!friendlyDates.isEmpty()) {
                        sb.append("  🗓️ Dates: **").append(friendlyDates).append("**\n");
                    }
                    if (!desc.isEmpty()) {
                        sb.append("  ℹ️ ").append(desc).append("\n");
                    }
                    sb.append("\n");
                }
                sb.append(">> When are University Examinations (UE)?\n>> When is Continuous Assessment Test 1?\n>> Show BSc Software Engineering timetable");
                return sb.toString().trim();
            }

            // 3. Professors & Faculty
            if (firstRow.has("office_block") || firstRow.has("subjects_taught") || (firstRow.has("department") && firstRow.has("office_room"))) {
                StringBuilder sb = new StringBuilder("👨‍🏫 **UDOM Academic Faculty Directory**:\n\n");
                for (int i = 0; i < Math.min(rows.length(), 6); i++) {
                    JSONObject r = rows.getJSONObject(i);
                    String name = r.optString("name", "Faculty Member");
                    String dept = r.optString("department", "");
                    String block = r.optString("office_block", "");
                    String room = r.optString("office_room", "");
                    String phone = r.optString("phone", DatabaseHelper.DEFAULT_PHONE);
                    String email = r.optString("email", DatabaseHelper.DEFAULT_EMAIL);
                    String subjects = r.optString("subjects_taught", "");
                    String hours = r.optString("consultation_hours", "");

                    sb.append("• **").append(name).append("**");
                    if (!dept.isEmpty()) sb.append(" (").append(dept).append(")");
                    sb.append("\n");

                    if (!block.isEmpty() || !room.isEmpty()) {
                        sb.append("  🏢 Office: ").append(block).append(room.isEmpty() ? "" : ", " + room).append("\n");
                    }
                    sb.append("  📞 Phone: ").append(phone).append(" | ✉️ ").append(email).append("\n");
                    if (!subjects.isEmpty()) {
                        sb.append("  📚 Subjects: ").append(subjects).append("\n");
                    }
                    if (!hours.isEmpty()) {
                        sb.append("  ⏰ Consultation: ").append(hours).append("\n");
                    }
                    sb.append("\n");
                }
                sb.append(">> Where is Dr. Mwamba's office?\n>> Show Software Engineering timetable\n>> Who is the Class Representative (CR)?");
                return sb.toString().trim();
            }

            // 4. Timetable Entries
            if (firstRow.has("course_code") && (firstRow.has("session_type") || firstRow.has("start_time"))) {
                StringBuilder sb = new StringBuilder("📅 **BSc Software Engineering Timetable**:\n\n");
                for (int i = 0; i < Math.min(rows.length(), 8); i++) {
                    JSONObject r = rows.getJSONObject(i);
                    String code = r.optString("course_code", "");
                    String name = r.optString("course_name", "");
                    String day = r.optString("day_of_week", "Monday");
                    String start = r.optString("start_time", "");
                    String end = r.optString("end_time", "");
                    String session = r.optString("session_type", "Theory");
                    String venue = r.optString("venue", "");
                    String lecturer = r.optString("lecturer_name", "");

                    sb.append("• **").append(day).append("** (").append(start).append(" - ").append(end).append(")\n");
                    sb.append("  📚 ").append(code).append(!name.isEmpty() ? " - " + name : "").append(" [").append(session).append("]\n");
                    sb.append("  🏫 Venue: ").append(venue);
                    if (!lecturer.isEmpty()) {
                        sb.append(" | 👨‍🏫 Lecturer: ").append(lecturer);
                    }
                    sb.append("\n\n");
                }
                sb.append(">> Where is CIVE-LRB 103?\n>> Where is Dr. Mwamba's office?\n>> When are University Examinations (UE)?");
                return sb.toString().trim();
            }

            // 5. Courses / Curriculum Handbook
            if (firstRow.has("credits") && firstRow.has("course_status")) {
                StringBuilder sb = new StringBuilder("📚 **BSc Software Engineering Curriculum**:\n\n");
                for (int i = 0; i < Math.min(rows.length(), 8); i++) {
                    JSONObject r = rows.getJSONObject(i);
                    String code = r.optString("course_code", "");
                    String title = r.optString("course_title", "");
                    double credits = r.optDouble("credits", 0.0);
                    String status = r.optString("course_status", "Core");
                    String category = r.optString("category", "");
                    int yr = r.optInt("year_of_study", 1);
                    int sem = r.optInt("semester", 1);

                    sb.append("• **").append(code).append("**: ").append(title).append("\n");
                    sb.append("  📊 Credits: ").append(credits).append(" | Status: ").append(status).append(" (").append(category).append(")\n");
                    sb.append("  🎓 Year ").append(yr).append(" - Semester ").append(sem).append("\n\n");
                }
                sb.append(">> What courses are in Year 2 Semester 1?\n>> What are the IPT requirements?\n>> Show BSc Software Engineering timetable");
                return sb.toString().trim();
            }

            // 6. Student Leaders & CRs
            if (firstRow.has("role") && firstRow.has("office_or_hostel")) {
                StringBuilder sb = new StringBuilder("👥 **Student Leaders & Class Representatives**:\n\n");
                for (int i = 0; i < Math.min(rows.length(), 6); i++) {
                    JSONObject r = rows.getJSONObject(i);
                    String name = r.optString("name", "");
                    String role = r.optString("role", "");
                    String location = r.optString("office_or_hostel", "");
                    String room = r.optString("room_number", "");
                    String phone = r.optString("phone", DatabaseHelper.DEFAULT_PHONE);
                    String email = r.optString("email", DatabaseHelper.DEFAULT_EMAIL);

                    sb.append("• **").append(name).append("** – ").append(role).append("\n");
                    sb.append("  🏠 Location: ").append(location).append(!room.isEmpty() ? ", Room " + room : "").append("\n");
                    sb.append("  📞 Phone: ").append(phone).append(" | ✉️ ").append(email).append("\n\n");
                }
                sb.append(">> Who is the Class Representative (CR)?\n>> Where is the UDOSO President's office?\n>> How do I contact the hostel warden?");
                return sb.toString().trim();
            }

            // 7. Campus Contacts
            if (firstRow.has("department_or_office") && firstRow.has("phone_number")) {
                StringBuilder sb = new StringBuilder("📞 **UDOM Verified Campus Directory**:\n\n");
                for (int i = 0; i < Math.min(rows.length(), 6); i++) {
                    JSONObject r = rows.getJSONObject(i);
                    String dept = r.optString("department_or_office", "");
                    String cat = r.optString("category", "");
                    String phone = r.optString("phone_number", DatabaseHelper.DEFAULT_PHONE);
                    String email = r.optString("email", DatabaseHelper.DEFAULT_EMAIL);
                    String loc = r.optString("office_location", "");
                    String desc = r.optString("description", "");

                    sb.append("• **").append(dept).append("** (").append(cat).append(")\n");
                    if (!loc.isEmpty()) sb.append("  📍 Location: ").append(loc).append("\n");
                    sb.append("  📞 Phone: ").append(phone).append(" | ✉️ ").append(email).append("\n");
                    if (!desc.isEmpty()) sb.append("  ℹ️ ").append(desc).append("\n");
                    sb.append("\n");
                }
                sb.append(">> What is the Campus Dispensary emergency phone number?\n>> Where is the CIVE Dean's office?\n>> How do I contact ICT Helpdesk?");
                return sb.toString().trim();
            }

            // 8. Campus Buildings & Locations (This was what produced the screenshot!)
            if (firstRow.has("latitude") || firstRow.has("longitude") || (firstRow.has("name") && firstRow.has("college"))) {
                StringBuilder sb = new StringBuilder("📍 **UDOM Campus Buildings & Facilities**:\n\n");
                for (int i = 0; i < Math.min(rows.length(), 6); i++) {
                    JSONObject r = rows.getJSONObject(i);
                    String name = r.optString("name", "Campus Location");
                    String college = r.optString("college", "ALL");
                    String category = r.optString("category", "FACILITY");
                    String desc = r.optString("description", "");

                    sb.append("• **").append(name).append("** (").append(college).append(" - ").append(category).append(")\n");
                    if (!desc.isEmpty()) {
                        sb.append("  ℹ️ ").append(desc).append("\n");
                    }
                    sb.append("\n");
                }
                sb.append("💡 You can also search for these buildings on the **MAP** tab for turn-by-turn navigation.\n\n");
                sb.append(">> Show Software Engineering timetable\n>> Where is Dr. Mwamba's office?\n>> How do I get around campus?");
                return sb.toString().trim();
            }

            // 9. Generic Polished Fallback (NO raw key-values, NO lat/long, NO keywords!)
            StringBuilder sb = new StringBuilder("ℹ️ **Retrieved Campus Information**:\n\n");
            for (int i = 0; i < Math.min(rows.length(), 6); i++) {
                JSONObject r = rows.getJSONObject(i);
                String title = r.optString("name", r.optString("question", r.optString("department_or_office", "Record")));
                String details = r.optString("description", r.optString("answer", r.optString("details", "")));

                sb.append("• **").append(title).append("**");
                if (!details.isEmpty()) {
                    sb.append("\n  ").append(details);
                }
                sb.append("\n\n");
            }
            sb.append(">> Show Software Engineering timetable\n>> Where is Dr. Mwamba's office?\n>> When are University Examinations (UE)?");
            return sb.toString().trim();

        } catch (Exception e) {
            return "Here is the information retrieved from our campus records.\n\n>> Show Software Engineering timetable\n>> Where is Dr. Mwamba's office?\n>> When are University Examinations (UE)?";
        }
    }

    /**
     * Executes all requested tools locally against SQLite and sends their responses back to Gemini
     * using the official Gemini turn role "user" with functionResponse parts.
     */
    private void submitAllFunctionResponsesToGemini(JSONObject originalPayload, JSONObject modelToolCallContent,
                                                    java.util.List<JSONObject> functionCalls, String userCollege,
                                                    AiCallback callback) {
        try {
            JSONArray contents = originalPayload.getJSONArray("contents");

            // 1. Append model's tool call turn
            contents.put(modelToolCallContent);

            // 2. Append user turn with role "user" and functionResponse for EACH functionCall
            JSONObject functionResponseTurn = new JSONObject();
            functionResponseTurn.put("role", "user");
            JSONArray fnParts = new JSONArray();

            StringBuilder fallbackCombinedResult = new StringBuilder();

            for (JSONObject fnCall : functionCalls) {
                String fnName = fnCall.optString("name");
                JSONObject args = fnCall.optJSONObject("args");
                if (args == null) args = new JSONObject();

                Log.d(TAG, "Executing tool: " + fnName + " with args: " + args);
                String toolResultJson = executeLocalTool(fnName, args, userCollege);

                String fallbackText = buildHumanFallback(fnName, toolResultJson, args);
                if (fallbackCombinedResult.length() > 0) fallbackCombinedResult.append("\n\n");
                fallbackCombinedResult.append(fallbackText);

                JSONObject fnPart = new JSONObject();
                JSONObject fnResponseObj = new JSONObject();
                fnResponseObj.put("name", fnName);
                if (fnCall.has("id")) {
                    fnResponseObj.put("id", fnCall.getString("id"));
                }

                JSONObject responseData = new JSONObject();
                try {
                    if (toolResultJson.startsWith("{")) {
                        responseData = new JSONObject(toolResultJson);
                    } else if (toolResultJson.startsWith("[")) {
                        responseData.put("rows", new JSONArray(toolResultJson));
                    } else {
                        responseData.put("result", toolResultJson);
                    }
                } catch (Exception e) {
                    responseData.put("result", toolResultJson);
                }
                fnResponseObj.put("response", responseData);

                fnPart.put("functionResponse", fnResponseObj);
                fnParts.put(fnPart);
            }

            functionResponseTurn.put("parts", fnParts);
            contents.put(functionResponseTurn);

            final String finalFallback = fallbackCombinedResult.toString();

            // 3. Resend to Gemini for final natural language generation and table rendering
            String url = getApiUrl() + BuildConfig.AI_API_KEY.trim();
            RequestBody body = RequestBody.create(originalPayload.toString(), JSON_MEDIA);
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();

            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.e(TAG, "Follow-up Gemini tool response network failed: " + e.getMessage());
                    postSuccess(callback, finalFallback);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        String errorBody = response.body() != null ? response.body().string() : "";
                        Log.e(TAG, "Follow-up Gemini tool response HTTP error " + response.code() + ": " + errorBody);
                        postSuccess(callback, finalFallback);
                        return;
                    }

                    try {
                        String responseString = response.body() != null ? response.body().string() : "";
                        JSONObject json = new JSONObject(responseString);
                        JSONArray candidates = json.optJSONArray("candidates");
                        if (candidates != null && candidates.length() > 0) {
                            JSONObject cand = candidates.getJSONObject(0);
                            JSONObject candContent = cand.optJSONObject("content");
                            if (candContent != null) {
                                JSONArray parts = candContent.optJSONArray("parts");
                                if (parts != null && parts.length() > 0) {
                                    StringBuilder ans = new StringBuilder();
                                    for (int i = 0; i < parts.length(); i++) {
                                        String txt = parts.getJSONObject(i).optString("text", "");
                                        if (!txt.isEmpty()) ans.append(txt);
                                    }
                                    if (ans.length() > 0) {
                                        postSuccess(callback, ans.toString());
                                        return;
                                    }
                                }
                            }
                        }
                        postSuccess(callback, finalFallback);
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing follow-up response: " + e.getMessage(), e);
                        postSuccess(callback, finalFallback);
                    }
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Error packaging function responses: " + e.getMessage(), e);
            postError(callback, "Error communicating with AI: " + e.getMessage());
        }
    }

    private void postSuccess(AiCallback callback, String message) {
        mainHandler.post(() -> callback.onSuccess(message));
    }

    private void postError(AiCallback callback, String error) {
        mainHandler.post(() -> callback.onError(error));
    }
}
