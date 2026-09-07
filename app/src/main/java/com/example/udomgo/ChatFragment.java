package com.example.udomgo;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatFragment extends Fragment {

    private EditText chatInput;
    private ImageButton sendButton;
    private ImageButton clearChatButton;
    private LinearLayout chatContainer;
    private ScrollView chatScrollView;
    private DrawerLayout chatDrawerLayout;

    private final SimpleDateFormat timeFormat =
            new SimpleDateFormat("HH:mm", Locale.getDefault());

    public ChatFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        return inflater.inflate(
                R.layout.fragment_chat,
                container,
                false
        );
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        chatInput = view.findViewById(R.id.chatInput);
        sendButton = view.findViewById(R.id.sendButton);
        clearChatButton = view.findViewById(R.id.clearChatButton);
        chatContainer = view.findViewById(R.id.chatContainer);
        chatScrollView = view.findViewById(R.id.chatScrollView);

        // Setup Quick Suggestion Chips
        setupSuggestionChips(view);

        // Send button
        sendButton.setOnClickListener(v -> sendMessage());

        // Drawer Layout
        chatDrawerLayout = view.findViewById(R.id.chatDrawerLayout);

        // View Chat History button (Opens ChatGPT-style sliding sidebar)
        ImageButton chatHistoryButton = view.findViewById(R.id.chatHistoryButton);
        if (chatHistoryButton != null) {
            chatHistoryButton.setOnClickListener(v -> openChatSidebar());
        }

        // Sidebar Close button
        View closeBtn = view.findViewById(R.id.chatDrawerCloseBtn);
        if (closeBtn != null) {
            closeBtn.setOnClickListener(v -> closeChatSidebar());
        }

        // Sidebar "+ New Chat" button
        View newChatBtn = view.findViewById(R.id.sidebarNewChatBtn);
        if (newChatBtn != null) {
            newChatBtn.setOnClickListener(v -> startNewChat());
        }

        // Sidebar "Clear Chat History" button
        View clearHistoryBtn = view.findViewById(R.id.sidebarClearHistoryBtn);
        if (clearHistoryBtn != null) {
            clearHistoryBtn.setOnClickListener(v -> confirmClearAllHistory());
        }

        // Clear Chat button (Deletes from SQLite)
        if (clearChatButton != null) {
            clearChatButton.setOnClickListener(v -> {
                Context context = getContext();
                if (context != null) {
                    String userEmail = UserPreferences.getCurrentUserEmail(context);
                    if (TextUtils.isEmpty(userEmail)) {
                        userEmail = UserPreferences.DEMO_EMAIL;
                    }
                    DatabaseHelper.getInstance(context).clearChatHistory(userEmail);
                }
                chatContainer.removeAllViews();
                showWelcomeMessage();
                Toast.makeText(requireContext(), "Chat history cleared", Toast.LENGTH_SHORT).show();
            });
        }

        // Send on keyboard Enter / Done
        chatInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND ||
                    (event != null &&
                            event.getKeyCode() == KeyEvent.KEYCODE_ENTER &&
                            event.getAction() == KeyEvent.ACTION_DOWN)) {
                sendMessage();
                return true;
            }
            return false;
        });

        // Ensure chat view scrolls up above the soft keyboard
        chatInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                chatScrollView.postDelayed(() -> chatScrollView.fullScroll(View.FOCUS_DOWN), 100);
                chatScrollView.postDelayed(() -> chatScrollView.fullScroll(View.FOCUS_DOWN), 300);
            }
        });
        chatInput.setOnClickListener(v -> {
            chatScrollView.postDelayed(() -> chatScrollView.fullScroll(View.FOCUS_DOWN), 100);
            chatScrollView.postDelayed(() -> chatScrollView.fullScroll(View.FOCUS_DOWN), 300);
        });

        // Automatically scroll to bottom whenever the layout compresses (e.g. keyboard rises)
        if (chatContainer != null) {
            chatContainer.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
                if (bottom < oldBottom) {
                    chatScrollView.post(() -> chatScrollView.fullScroll(View.FOCUS_DOWN));
                }
            });
        }

        // Load persisted conversation from SQLite
        loadChatHistory();
    }

    private void setupSuggestionChips(View root) {
        String myCollege = UserPreferences.getSelectedCollege(getContext());

        setChipListener(root, R.id.chipColleges, "Tell me about my college (" + myCollege + ")");
        setChipListener(root, R.id.chipCive, "Tell me about CIVE");
        setChipListener(root, R.id.chipHostels, "How does hostel accommodation work?");
        setChipListener(root, R.id.chipLibrary, "What are library hours and services?");
        setChipListener(root, R.id.chipCafeteria, "Where is my nearest cafeteria?");
        setChipListener(root, R.id.chipFees, "How do I pay fees with GePG control number?");
        setChipListener(root, R.id.chipSr2, "How do I use the SR2 portal?");
        setChipListener(root, R.id.chipTransport, "How do I get around campus?");
        setChipListener(root, R.id.chipHealth, "Where is the UDOM health center?");
    }

    private void setChipListener(View root, int chipId, String query) {
        TextView chip = root.findViewById(chipId);
        if (chip != null) {
            chip.setOnClickListener(v -> handleUserQuery(query));
        }
    }

    private void loadChatHistory() {
        Context context = getContext();
        if (context == null) return;

        chatContainer.removeAllViews();
        String userEmail = UserPreferences.getCurrentUserEmail(context);
        if (TextUtils.isEmpty(userEmail)) {
            userEmail = UserPreferences.DEMO_EMAIL;
        }

        List<DatabaseHelper.ChatMessageItem> history =
                DatabaseHelper.getInstance(context).getChatHistory(userEmail);

        if (history.isEmpty()) {
            showWelcomeMessage();
        } else {
            for (DatabaseHelper.ChatMessageItem item : history) {
                String time = item.timestamp != null ? item.timestamp : timeFormat.format(new Date());
                if (time.length() >= 16 && time.contains(" ")) {
                    try {
                        time = time.substring(11, 16);
                    } catch (Exception ignored) {}
                }
                renderMessageBubble(item.message, "user".equalsIgnoreCase(item.sender), time);
            }
            chatScrollView.post(() -> chatScrollView.fullScroll(View.FOCUS_DOWN));
        }
    }



    public void openChatSidebar() {
        if (chatDrawerLayout != null) {
            populateSidebarHistory();
            chatDrawerLayout.openDrawer(GravityCompat.START);
        }
    }

    public void closeChatSidebar() {
        if (chatDrawerLayout != null && chatDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            chatDrawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    private void startNewChat() {
        closeChatSidebar();
        chatContainer.removeAllViews();
        showWelcomeMessage();
        Toast.makeText(requireContext(), "Started a new chat session", Toast.LENGTH_SHORT).show();
    }

    private void confirmClearAllHistory() {
        Context context = getContext();
        if (context == null) return;

        String userEmail = UserPreferences.getCurrentUserEmail(context);
        if (TextUtils.isEmpty(userEmail)) {
            userEmail = UserPreferences.DEMO_EMAIL;
        }

        final String finalEmail = userEmail;
        new androidx.appcompat.app.AlertDialog.Builder(context)
                .setTitle("Clear Chat History")
                .setMessage("Are you sure you want to permanently clear all conversation history from the SQLite database?")
                .setPositiveButton("Clear All", (dialog, which) -> {
                    DatabaseHelper.getInstance(context).clearChatHistory(finalEmail);
                    chatContainer.removeAllViews();
                    showWelcomeMessage();
                    populateSidebarHistory();
                    Toast.makeText(context, "All chat history cleared from SQLite", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void populateSidebarHistory() {
        View root = getView();
        if (root == null || getContext() == null) return;

        LinearLayout container = root.findViewById(R.id.sidebarHistoryContainer);
        TextView emptyText = root.findViewById(R.id.sidebarEmptyText);
        TextView statusText = root.findViewById(R.id.sidebarDbStatusText);
        if (container == null) return;

        // Reset list but keep empty state view
        container.removeAllViews();
        if (emptyText != null) {
            container.addView(emptyText);
        }

        String userEmail = UserPreferences.getCurrentUserEmail(getContext());
        if (TextUtils.isEmpty(userEmail)) {
            userEmail = UserPreferences.DEMO_EMAIL;
        }

        List<DatabaseHelper.ChatMessageItem> history =
                DatabaseHelper.getInstance(getContext()).getChatHistory(userEmail);

        if (statusText != null) {
            statusText.setText("● SQLite DB • " + history.size() + " messages saved");
        }

        if (history.isEmpty()) {
            if (emptyText != null) emptyText.setVisibility(View.VISIBLE);
            return;
        }

        if (emptyText != null) emptyText.setVisibility(View.GONE);

        // Render user query items in reverse chronological order (newest first)
        for (int i = history.size() - 1; i >= 0; i--) {
            DatabaseHelper.ChatMessageItem item = history.get(i);
            if (!"user".equalsIgnoreCase(item.sender)) continue;

            LinearLayout card = new LinearLayout(getContext());
            card.setOrientation(LinearLayout.VERTICAL);
            card.setBackgroundResource(R.drawable.sidebar_item_bg);
            card.setPadding(dpToPx(12), dpToPx(10), dpToPx(12), dpToPx(10));
            card.setClickable(true);
            card.setFocusable(true);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            lp.setMargins(0, 0, 0, dpToPx(8));
            card.setLayoutParams(lp);

            // Title row (Icon + Question snippet)
            LinearLayout titleRow = new LinearLayout(getContext());
            titleRow.setOrientation(LinearLayout.HORIZONTAL);
            titleRow.setGravity(Gravity.CENTER_VERTICAL);

            TextView icon = new TextView(getContext());
            icon.setText("💬 ");
            icon.setTextSize(13);
            titleRow.addView(icon);

            TextView title = new TextView(getContext());
            title.setText(item.message);
            title.setMaxLines(2);
            title.setEllipsize(TextUtils.TruncateAt.END);
            title.setTextColor(0xFFE2E8F0);
            title.setTextSize(13);
            title.setTypeface(null, android.graphics.Typeface.BOLD);
            LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f
            );
            title.setLayoutParams(titleLp);
            titleRow.addView(title);

            card.addView(titleRow);

            // Timestamp row
            if (!TextUtils.isEmpty(item.timestamp)) {
                TextView timeText = new TextView(getContext());
                timeText.setText("⏰ " + item.timestamp);
                timeText.setTextColor(0xFF64748B);
                timeText.setTextSize(11);
                timeText.setPadding(dpToPx(20), dpToPx(3), 0, 0);
                card.addView(timeText);
            }

            // Click card to re-ask or inspect in chat and close sidebar
            final String clickedQuestion = item.message;
            card.setOnClickListener(v -> {
                closeChatSidebar();
                handleUserQuery(clickedQuestion);
            });

            container.addView(card);
        }
    }

    private void showWelcomeMessage() {
        Context context = getContext();
        String myCollege = UserPreferences.getSelectedCollege(context);
        String myCollegeName = UserPreferences.getSelectedCollegeFullName(context);

        String welcome = "👋 Hello! Welcome to UDOMGo Assistant.\n\n"
                + "🎯 Identified Profile: " + myCollege + " (" + myCollegeName + ")\n\n"
                + "Responses are personalized for your college! You can ask me:\n"
                + "• 'Where are my lectures or classes?'\n"
                + "• 'Where is my nearest cafeteria?'\n"
                + "• Hostels & Room Allocation\n"
                + "• Library Opening Hours & Services\n"
                + "• GePG Control Numbers & SR2 Registration\n\n"
                + "Tap a quick topic above or type your question below!";

        addMessage(welcome, false, false);
    }

    private void sendMessage() {
        String question = chatInput.getText().toString().trim();
        if (TextUtils.isEmpty(question)) {
            return;
        }

        chatInput.setText("");
        handleUserQuery(question);
    }

    private void handleUserQuery(String question) {
        // Add User message and save to SQLite
        addMessage(question, true, true);

        // Hide keyboard smoothly
        hideKeyboard();

        // Simulate thinking briefly for natural chat experience
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (!isAdded()) return;
            String answer = getAssistantResponse(question);
            // Add Assistant message and save to SQLite
            addMessage(answer, false, true);
        }, 350);
    }

    private void addMessage(String message, boolean isUser) {
        addMessage(message, isUser, true);
    }

    private void addMessage(String message, boolean isUser, boolean saveToDb) {
        Context context = getContext();
        if (context == null) return;

        if (saveToDb) {
            String userEmail = UserPreferences.getCurrentUserEmail(context);
            if (TextUtils.isEmpty(userEmail)) {
                userEmail = UserPreferences.DEMO_EMAIL;
            }
            DatabaseHelper.getInstance(context).insertChatMessage(
                    userEmail,
                    isUser ? "user" : "assistant",
                    message
            );
        }

        renderMessageBubble(message, isUser, timeFormat.format(new Date()));
    }

    private void renderMessageBubble(String message, boolean isUser, String formattedTime) {
        Context context = getContext();
        if (context == null) return;

        LinearLayout messageWrapper = new LinearLayout(context);
        messageWrapper.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams wrapperParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        wrapperParams.setMargins(0, dpToPx(4), 0, dpToPx(6));
        messageWrapper.setLayoutParams(wrapperParams);

        // Card / Bubble Layout
        LinearLayout bubbleLayout = new LinearLayout(context);
        bubbleLayout.setOrientation(LinearLayout.VERTICAL);

        int padH = dpToPx(14);
        int padV = dpToPx(10);
        bubbleLayout.setPadding(padH, padV, padH, padV);

        LinearLayout.LayoutParams bubbleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        // Calculate max width (~80% of screen)
        int maxWidth = (int) (getResources().getDisplayMetrics().widthPixels * 0.82);

        // Sender label for bot
        if (!isUser) {
            TextView senderLabel = new TextView(context);
            senderLabel.setText("🤖 UDOMGo");
            senderLabel.setTextSize(11);
            senderLabel.setTextColor(0xFF0284C7); // Sky blue accent
            senderLabel.setTypeface(null, android.graphics.Typeface.BOLD);
            senderLabel.setPadding(0, 0, 0, dpToPx(3));
            bubbleLayout.addView(senderLabel);
        }

        // Message text
        TextView textContent = new TextView(context);
        textContent.setText(message);
        textContent.setTextSize(14);
        textContent.setLineSpacing(dpToPx(2), 1.1f);
        textContent.setMaxWidth(maxWidth);

        // Timestamp
        TextView timeView = new TextView(context);
        timeView.setText(formattedTime);
        timeView.setTextSize(10);
        timeView.setPadding(0, dpToPx(4), 0, 0);

        if (isUser) {
            messageWrapper.setGravity(Gravity.END);
            bubbleParams.gravity = Gravity.END;
            bubbleLayout.setBackgroundResource(R.drawable.chat_user_bubble);
            textContent.setTextColor(0xFFFFFFFF);
            timeView.setTextColor(0xFFCBD5E1);
            timeView.setGravity(Gravity.END);
        } else {
            messageWrapper.setGravity(Gravity.START);
            bubbleParams.gravity = Gravity.START;
            bubbleLayout.setBackgroundResource(R.drawable.chat_bot_bubble);
            textContent.setTextColor(0xFF0F172A);
            timeView.setTextColor(0xFF94A3B8);
            timeView.setGravity(Gravity.START);
            bubbleLayout.setElevation(dpToPx(2));
        }

        bubbleLayout.setLayoutParams(bubbleParams);
        bubbleLayout.addView(textContent);
        bubbleLayout.addView(timeView);

        messageWrapper.addView(bubbleLayout);
        chatContainer.addView(messageWrapper);

        // Smoothly scroll down
        chatScrollView.post(() -> chatScrollView.fullScroll(View.FOCUS_DOWN));
    }

    private String getAssistantResponse(String rawQuery) {
        String query = rawQuery.toLowerCase(Locale.ROOT).trim();
        String userCollege = UserPreferences.getSelectedCollege(getContext());

        // 1. Context-aware queries based on user's selected college
        if (query.contains("my class") || query.contains("my lecture") || query.contains("where do i study") || query.contains("my room")) {
            return getCollegeLocationInfo(userCollege);
        }

        if (query.contains("my cafeteria") || query.contains("nearest cafeteria") || query.contains("nearest food") || query.contains("where to eat")) {
            return getCollegeCafeteriaInfo(userCollege);
        }

        if (query.contains("my college") || query.contains("my profile") || query.contains("who am i")) {
            String name = UserPreferences.getSelectedCollegeFullName(getContext());
            return "🎯 Your current student profile is identified as: " + userCollege + " (" + name + ").\n\n"
                    + getCollegeLocationInfo(userCollege) + "\n\n"
                    + "💡 You can switch your college identity at any time on the HOME screen.";
        }

        if (query.contains("hello") || query.contains("hi") || query.contains("mambo") || query.contains("habari") || query.contains("hey")) {
            return "👋 Hello! I recognize you as a " + userCollege + " student.\n\n"
                    + "How can I help you today? Ask me about your classes, nearest cafeteria, library, hostels, or fees!";
        }

        // 2. Query Knowledge Base directly from SQLite database (Table: campus_faqs)
        Context context = getContext();
        if (context != null) {
            String dbAnswer = DatabaseHelper.getInstance(context).findFaqAnswer(query, userCollege);
            if (dbAnswer != null && !dbAnswer.isEmpty()) {
                if (query.contains(userCollege.toLowerCase(Locale.ROOT))) {
                    dbAnswer += "\n\n⭐ Note: This is your currently selected college!";
                }
                return dbAnswer;
            }
        }

        // 3. Helpful fallback when no matching knowledge record in SQLite
        return "🤖 Thank you for your question!\n\n"
                + "Since you are identified as a " + userCollege + " student, you can ask me:\n"
                + "• 'Where are my classes or lectures?'\n"
                + "• 'Where is my nearest cafeteria?'\n"
                + "• 'Tell me about hostel accommodation'\n"
                + "• 'How do I pay fees with control number?'\n"
                + "• 'What are library opening hours?'";
    }

    private String getCollegeLocationInfo(String college) {
        if (college.equalsIgnoreCase("CIVE")) {
            return "📍 As a CIVE student, your lectures and labs are in Lecture Rooms A & B, CIVE Auditorium, and Blocks 1 to 6 in the North-East campus zone.";
        } else if (college.equalsIgnoreCase("CoBE")) {
            return "📍 As a CoBE student, your classes and seminars are held in the CoBE Lecture Theatres near the main administration zone.";
        } else if (college.equalsIgnoreCase("CoED")) {
            return "📍 As a CoED student, your lectures are conducted in the central College of Education academic blocks.";
        } else if (college.equalsIgnoreCase("CHSS")) {
            return "📍 As a CHSS student, your lectures take place in the College of Humanities and Social Sciences wing.";
        } else if (college.equalsIgnoreCase("CNMS")) {
            return "📍 As a CNMS student, your science classes and laboratory practicals are in the CNMS Science Complex.";
        } else if (college.equalsIgnoreCase("CoESE")) {
            return "📍 As a CoESE student, your engineering and geology lectures are in the Earth Sciences & Engineering block.";
        } else if (college.equalsIgnoreCase("SoL")) {
            return "📍 As a School of Law student, your lectures and moot sessions are in the Law Complex.";
        } else if (college.equalsIgnoreCase("SoMD") || college.equalsIgnoreCase("SoNPH")) {
            return "📍 As a Health Sciences student, your classes and clinical sessions are at the Medical Campus near Benjamin Mkapa Hospital.";
        } else {
            return "📍 You are registered under " + college + ". Use the MAP tab to view your college building and surroundings!";
        }
    }

    private String getCollegeCafeteriaInfo(String college) {
        return "🍽️ Nearest Dining for " + college + " Students:\n\n"
                + "• Your designated college cafeteria is located adjacent to the " + college + " main complex.\n"
                + "• Operating Hours: 7:00 AM – 9:00 PM.\n"
                + "• Serving breakfast, lunch, and dinner (typical price: TZS 1,500 – 3,500).";
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    private void hideKeyboard() {
        if (chatInput == null) return;
        InputMethodManager imm =
                (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(chatInput.getWindowToken(), 0);
        }
        chatInput.clearFocus();
    }
}