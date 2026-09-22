package com.example.udomgo;

import android.content.Context;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import android.widget.HorizontalScrollView;
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

import io.noties.markwon.Markwon;
import io.noties.markwon.ext.tables.TablePlugin;
import io.noties.markwon.ext.tables.TableTheme;

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
    private AiService aiService;
    private View currentThinkingBubble;
    private Markwon markwon;

    // Follow-up suggestion pills state
    private LinearLayout verticalFollowUpsContainer;
    private HorizontalScrollView quickSuggestionsScroll;
    private LinearLayout currentPendingBotWrapper = null;
    private final java.util.List<String> currentPendingFollowUps = new java.util.ArrayList<>();

    // Deduplication of asked or clicked queries in the current chat window
    private final java.util.Set<String> askedQueries = new java.util.HashSet<>();

    private final SimpleDateFormat timeFormat =
            new SimpleDateFormat("HH:mm", Locale.getDefault());

    public ChatFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

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

        verticalFollowUpsContainer = view.findViewById(R.id.verticalFollowUpsContainer);
        quickSuggestionsScroll = view.findViewById(R.id.quickSuggestionsScroll);

        // Setup Quick Suggestion Chips
        setupSuggestionChips(view);

        // Send button
        sendButton.setOnClickListener(v -> sendMessage());

        // Drawer Layout
        chatDrawerLayout = view.findViewById(R.id.chatDrawerLayout);

        // Initialize AI Service & Markdown Parser with TablePlugin and custom theme
        aiService = new AiService(requireContext());
        TableTheme tableTheme = TableTheme.buildWithDefaults(requireContext())
                .tableBorderColor(0xFFCBD5E1)
                .tableBorderWidth(dpToPx(1))
                .tableCellPadding(dpToPx(8))
                .tableHeaderRowBackgroundColor(0xFFF1F5F9)
                .tableOddRowBackgroundColor(0xFFF8FAFC)
                .tableEvenRowBackgroundColor(0xFFFFFFFF)
                .build();
        markwon = Markwon.builder(requireContext())
                .usePlugin(TablePlugin.create(tableTheme))
                .build();



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
                askedQueries.clear();
                finalizePreviousBotFollowUps();
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

        finalizePreviousBotFollowUps();
        chatContainer.removeAllViews();
        String userEmail = UserPreferences.getCurrentUserEmail(context);
        if (TextUtils.isEmpty(userEmail)) {
            userEmail = UserPreferences.DEMO_EMAIL;
        }

        List<DatabaseHelper.ChatMessageItem> history =
                DatabaseHelper.getInstance(context).getChatHistory(userEmail);

        // Pre-populate asked queries from history so already asked questions are deduplicated
        askedQueries.clear();
        for (DatabaseHelper.ChatMessageItem item : history) {
            if ("user".equalsIgnoreCase(item.sender) && !TextUtils.isEmpty(item.message)) {
                recordAskedQuery(item.message);
            }
        }

        if (history.isEmpty()) {
            showWelcomeMessage();
        } else {
            showDefaultQuickChips();
            for (int i = 0; i < history.size(); i++) {
                DatabaseHelper.ChatMessageItem item = history.get(i);
                boolean isLast = (i == history.size() - 1);
                boolean isUser = "user".equalsIgnoreCase(item.sender);
                boolean isGemini = "gemini".equalsIgnoreCase(item.sender);
                String time = item.timestamp != null ? item.timestamp : timeFormat.format(new Date());
                if (time.length() >= 16 && time.contains(" ")) {
                    try {
                        time = time.substring(11, 16);
                    } catch (Exception ignored) {}
                }
                renderMessageBubble(item.message, isUser, isGemini, time, isLast && !isUser);
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
        askedQueries.clear();
        finalizePreviousBotFollowUps();
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
                .setMessage("Are you sure you want to permanently clear all conversation history?")
                .setPositiveButton("Clear All", (dialog, which) -> {
                    DatabaseHelper.getInstance(context).clearChatHistory(finalEmail);
                    askedQueries.clear();
                    chatContainer.removeAllViews();
                    showWelcomeMessage();
                    populateSidebarHistory();
                    Toast.makeText(context, "All chat history cleared", Toast.LENGTH_SHORT).show();
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
            statusText.setText("● Saved Sessions • " + history.size() + " messages");
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
        finalizePreviousBotFollowUps();
        showDefaultQuickChips();
        Context context = getContext();
        String myCollege = UserPreferences.getSelectedCollege(context);
        String myCollegeName = UserPreferences.getSelectedCollegeFullName(context);

        String welcome = "👋 Hello! Welcome to UDOMGo Assistant.\n\n"
                + "🎯 Identified Profile: " + myCollege + " (" + myCollegeName + ")\n\n"
                + "Responses are personalized for your college! You can ask me:\n"
                + "• Lectures & Software Engineering timetables\n"
                + "• Professor offices & consultation hours\n"
                + "• University Examination (UE) dates\n"
                + "• Student leaders, CRs & warden contacts\n"
                + "• GePG fees & SR2 portal guide\n\n"
                + "Tap a quick topic above or type your question below!";

        addMessage(welcome, false, false, false);
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
        // Record as asked immediately so it is never suggested again in pills
        recordAskedQuery(question);

        // Move previous bot follow-ups into its bubble as horizontal scrollable pills
        finalizePreviousBotFollowUps();

        // Add User message and save to SQLite
        addMessage(question, true, true, false);

        // Hide keyboard smoothly
        hideKeyboard();

        Context context = getContext();
        String userCollege = UserPreferences.getSelectedCollege(context);

        if (isNetworkAvailable() && AiService.isApiKeyConfigured()) {
            showThinkingIndicator();
            String userEmail = UserPreferences.getCurrentUserEmail(context);
            if (TextUtils.isEmpty(userEmail)) {
                userEmail = UserPreferences.DEMO_EMAIL;
            }
            List<DatabaseHelper.ChatMessageItem> recentHistory =
                    DatabaseHelper.getInstance(context).getChatHistory(userEmail);

            aiService.askAssistant(question, userCollege, recentHistory, new AiService.AiCallback() {
                @Override
                public void onSuccess(String responseText) {
                    if (!isAdded()) return;
                    hideThinkingIndicator();
                    addMessage(responseText, false, true, true, true);
                }

                @Override
                public void onError(String errorMessage) {
                    if (!isAdded()) return;
                    hideThinkingIndicator();
                    // Fall back gracefully to local SQLite knowledge engine
                    String offlineAnswer = getAssistantResponse(question);
                    addMessage(offlineAnswer, false, false, true, true);
                }
            });
        } else {
            // Local offline simulation
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (!isAdded()) return;
                String answer = getAssistantResponse(question);
                addMessage(answer, false, false, true, true);
            }, 300);
        }
    }

    private void showThinkingIndicator() {
        hideThinkingIndicator();
        currentThinkingBubble = renderMessageBubble("⏳ Thinking & searching UDOM database...", false, true, "", false);
    }

    private void hideThinkingIndicator() {
        if (currentThinkingBubble != null && chatContainer != null) {
            chatContainer.removeView(currentThinkingBubble);
            currentThinkingBubble = null;
        }
    }

    private boolean isNetworkAvailable() {
        Context context = getContext();
        if (context == null) return false;
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnected();
        }
        return false;
    }

    private void addMessage(String message, boolean isUser) {
        addMessage(message, isUser, false, true, true);
    }

    private void addMessage(String message, boolean isUser, boolean isGemini) {
        addMessage(message, isUser, isGemini, true, true);
    }

    private void addMessage(String message, boolean isUser, boolean saveToDb, boolean isLatest) {
        addMessage(message, isUser, false, saveToDb, isLatest);
    }

    private void addMessage(String message, boolean isUser, boolean isGemini, boolean saveToDb, boolean isLatest) {
        Context context = getContext();
        if (context == null) return;

        if (saveToDb) {
            String userEmail = UserPreferences.getCurrentUserEmail(context);
            if (TextUtils.isEmpty(userEmail)) {
                userEmail = UserPreferences.DEMO_EMAIL;
            }
            DatabaseHelper.getInstance(context).insertChatMessage(
                    userEmail,
                    isUser ? "user" : (isGemini ? "gemini" : "local"),
                    message
            );
        }

        renderMessageBubble(message, isUser, isGemini, timeFormat.format(new Date()), isLatest);
    }

    private View renderMessageBubble(String message, boolean isUser, String formattedTime) {
        return renderMessageBubble(message, isUser, false, formattedTime, true);
    }

    private View renderMessageBubble(String message, boolean isUser, String formattedTime, boolean isLatest) {
        return renderMessageBubble(message, isUser, false, formattedTime, isLatest);
    }

    private View renderMessageBubble(String message, boolean isUser, boolean isGemini, String formattedTime, boolean isLatest) {
        Context context = getContext();
        if (context == null) return null;

        LinearLayout messageWrapper = new LinearLayout(context);
        messageWrapper.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams wrapperParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        wrapperParams.setMargins(0, dpToPx(4), 0, dpToPx(6));
        messageWrapper.setLayoutParams(wrapperParams);

        // Extract follow-up suggestions (lines starting with '>>')
        java.util.List<String> followUps = new java.util.ArrayList<>();
        String displayMessage = message != null ? message : "";
        if (!isUser && message != null && message.contains(">>")) {
            StringBuilder cleanSb = new StringBuilder();
            String[] lines = message.split("\n");
            for (String line : lines) {
                String trimmed = line.trim();
                if (trimmed.startsWith(">>")) {
                    String pill = trimmed.substring(2).replace("✨", "").trim();
                    if (!pill.isEmpty()) {
                        followUps.add(pill);
                    }
                } else {
                    cleanSb.append(line).append("\n");
                }
            }
            displayMessage = cleanSb.toString().trim();
        }

        // Card / Bubble Layout
        LinearLayout bubbleLayout = new LinearLayout(context);
        bubbleLayout.setOrientation(LinearLayout.VERTICAL);

        boolean hasTable = !isUser && displayMessage.contains("|") && displayMessage.contains("---");

        int padH = hasTable ? dpToPx(10) : dpToPx(14);
        int padV = dpToPx(10);
        bubbleLayout.setPadding(padH, padV, padH, padV);

        LinearLayout.LayoutParams bubbleParams = new LinearLayout.LayoutParams(
                hasTable ? LinearLayout.LayoutParams.MATCH_PARENT : LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        // Calculate max width (~94% for tables, 82% for standard text)
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int maxWidth = hasTable ? (int) (screenWidth * 0.94) : (int) (screenWidth * 0.82);

        // Sender label for bot: colored circular status dot replacing the bot emoji
        if (!isUser) {
            LinearLayout headerRow = new LinearLayout(context);
            headerRow.setOrientation(LinearLayout.HORIZONTAL);
            headerRow.setGravity(Gravity.CENTER_VERTICAL);
            headerRow.setPadding(0, 0, 0, dpToPx(3));

            // Status dot: Green for Gemini, Orange for Local
            View statusDot = new View(context);
            int dotSize = dpToPx(7);
            LinearLayout.LayoutParams dotLp = new LinearLayout.LayoutParams(dotSize, dotSize);
            dotLp.setMargins(0, 0, dpToPx(5), 0);
            statusDot.setLayoutParams(dotLp);

            android.graphics.drawable.GradientDrawable dotDrawable = new android.graphics.drawable.GradientDrawable();
            dotDrawable.setShape(android.graphics.drawable.GradientDrawable.OVAL);
            dotDrawable.setColor(isGemini ? 0xFF10B981 : 0xFFF97316); // Green = Gemini, Orange = Local
            statusDot.setBackground(dotDrawable);
            headerRow.addView(statusDot);

            TextView senderLabel = new TextView(context);
            senderLabel.setText("UDOMGo");
            senderLabel.setTextSize(11);
            senderLabel.setTextColor(0xFF0284C7); // Sky blue accent
            senderLabel.setTypeface(null, android.graphics.Typeface.BOLD);
            headerRow.addView(senderLabel);

            bubbleLayout.addView(headerRow);
        }

        // Timestamp
        TextView timeView = new TextView(context);
        timeView.setText(formattedTime);
        timeView.setTextSize(10);
        timeView.setPadding(0, dpToPx(4), 0, 0);

        if (isUser) {
            messageWrapper.setGravity(Gravity.END);
            bubbleParams.gravity = Gravity.END;
            bubbleLayout.setBackgroundResource(R.drawable.chat_user_bubble);
            timeView.setTextColor(0xFFCBD5E1);
            timeView.setGravity(Gravity.END);
        } else {
            messageWrapper.setGravity(Gravity.START);
            bubbleParams.gravity = Gravity.START;
            bubbleLayout.setBackgroundResource(R.drawable.chat_bot_bubble);
            timeView.setTextColor(0xFF94A3B8);
            timeView.setGravity(Gravity.START);
            bubbleLayout.setElevation(dpToPx(2));
        }

        if (!hasTable) {
            // Standard text message rendering
            TextView textContent = new TextView(context);
            textContent.setTextSize(14);
            textContent.setLineSpacing(dpToPx(2), 1.15f);
            textContent.setMaxWidth(maxWidth);
            textContent.setTextColor(isUser ? 0xFFFFFFFF : 0xFF0F172A);

            if (markwon != null && !displayMessage.isEmpty()) {
                markwon.setMarkdown(textContent, displayMessage);
            } else {
                textContent.setText(displayMessage);
            }
            bubbleLayout.addView(textContent);
        } else {
            // Split into intro text, horizontal scrollable table, and outro text
            renderTableMessageBlocks(bubbleLayout, displayMessage, maxWidth, isUser);
        }

        bubbleLayout.setLayoutParams(bubbleParams);
        bubbleLayout.addView(timeView);
        messageWrapper.addView(bubbleLayout);

        // Follow-up Pills Handling:
        java.util.List<String> cleanFollowUps = !isUser ? filterAndEnrichFollowUps(followUps) : new java.util.ArrayList<>();
        if (!isUser && !cleanFollowUps.isEmpty()) {
            if (isLatest) {
                // For latest response: DO NOT show horizontal pills under bubble (avoids duplicate).
                // Instead, store this bot message and display the follow-ups stacked vertically on top of the text area!
                currentPendingBotWrapper = messageWrapper;
                currentPendingFollowUps.clear();
                currentPendingFollowUps.addAll(cleanFollowUps);
                showVerticalFollowUps(cleanFollowUps);
            } else {
                // For past bot responses: attach horizontal scrollable row underneath its bubble
                attachHorizontalFollowUps(messageWrapper, cleanFollowUps);
            }
        }

        chatContainer.addView(messageWrapper);

        // Smoothly scroll down
        chatScrollView.post(() -> chatScrollView.fullScroll(View.FOCUS_DOWN));
        return messageWrapper;
    }

    private static class MessageBlock {
        final boolean isTable;
        final String content;
        final int columnCount;

        MessageBlock(boolean isTable, String content, int columnCount) {
            this.isTable = isTable;
            this.content = content;
            this.columnCount = columnCount;
        }
    }

    private void renderTableMessageBlocks(LinearLayout bubbleLayout, String message, int maxWidth, boolean isUser) {
        Context context = getContext();
        if (context == null) return;

        List<MessageBlock> blocks = splitMessageIntoBlocks(message);
        for (MessageBlock block : blocks) {
            if (block.isTable) {
                // Swipe Hint Header
                TextView scrollHint = new TextView(context);
                scrollHint.setText("↔ Swipe horizontally to view full table");
                scrollHint.setTextSize(11);
                scrollHint.setTextColor(0xFF0284C7); // Sky blue accent
                scrollHint.setTypeface(null, android.graphics.Typeface.BOLD);
                scrollHint.setPadding(0, dpToPx(6), 0, dpToPx(2));
                bubbleLayout.addView(scrollHint);

                // HorizontalScrollView wrapper
                HorizontalScrollView tableScroll = new HorizontalScrollView(context);
                tableScroll.setHorizontalScrollBarEnabled(true);
                tableScroll.setScrollBarSize(dpToPx(4));
                tableScroll.setOverScrollMode(View.OVER_SCROLL_IF_CONTENT_SCROLLS);
                tableScroll.setScrollbarFadingEnabled(false);

                LinearLayout.LayoutParams scrollParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                scrollParams.setMargins(0, dpToPx(2), 0, dpToPx(6));
                tableScroll.setLayoutParams(scrollParams);

                // Generous column width: at least 130dp per column so words never break awkwardly
                int colWidthPx = dpToPx(130);
                int calculatedWidth = Math.max(maxWidth, block.columnCount * colWidthPx);

                TextView tableTextView = new TextView(context);
                tableTextView.setTextSize(13);
                tableTextView.setTextColor(0xFF0F172A);
                tableTextView.setLineSpacing(dpToPx(2), 1.15f);

                android.widget.FrameLayout.LayoutParams tvParams = new android.widget.FrameLayout.LayoutParams(
                        calculatedWidth,
                        android.widget.FrameLayout.LayoutParams.WRAP_CONTENT
                );
                tableTextView.setLayoutParams(tvParams);
                tableTextView.setMinimumWidth(calculatedWidth);

                if (markwon != null) {
                    markwon.setMarkdown(tableTextView, block.content);
                } else {
                    tableTextView.setText(block.content);
                }

                tableScroll.addView(tableTextView);
                bubbleLayout.addView(tableScroll);
            } else {
                if (block.content.trim().isEmpty()) continue;
                TextView textChunk = new TextView(context);
                textChunk.setTextSize(14);
                textChunk.setLineSpacing(dpToPx(2), 1.15f);
                textChunk.setMaxWidth(maxWidth);
                textChunk.setTextColor(isUser ? 0xFFFFFFFF : 0xFF0F172A);
                textChunk.setPadding(0, dpToPx(2), 0, dpToPx(2));

                if (markwon != null) {
                    markwon.setMarkdown(textChunk, block.content);
                } else {
                    textChunk.setText(block.content);
                }
                bubbleLayout.addView(textChunk);
            }
        }
    }

    private List<MessageBlock> splitMessageIntoBlocks(String message) {
        List<MessageBlock> blocks = new java.util.ArrayList<>();
        if (message == null || message.isEmpty()) return blocks;

        if (!message.contains("|") || !message.contains("---")) {
            blocks.add(new MessageBlock(false, message, 0));
            return blocks;
        }

        String[] lines = message.split("\n");
        StringBuilder currentText = new StringBuilder();
        List<String> currentTable = new java.util.ArrayList<>();
        boolean inTable = false;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            String trimmed = line.trim();
            boolean isTableLine = trimmed.contains("|");

            if (isTableLine) {
                if (!inTable) {
                    boolean hasSep = false;
                    for (int j = i; j < Math.min(lines.length, i + 3); j++) {
                        if (lines[j].contains("---")) {
                            hasSep = true;
                            break;
                        }
                    }
                    if (hasSep) {
                        inTable = true;
                        if (currentText.length() > 0) {
                            blocks.add(new MessageBlock(false, currentText.toString().trim(), 0));
                            currentText.setLength(0);
                        }
                        currentTable.add(line);
                    } else {
                        currentText.append(line).append("\n");
                    }
                } else {
                    currentTable.add(line);
                }
            } else {
                if (inTable) {
                    inTable = false;
                    int cols = calculateTableColumns(currentTable);
                    blocks.add(new MessageBlock(true, TextUtils.join("\n", currentTable).trim(), cols));
                    currentTable.clear();
                    if (!trimmed.isEmpty()) {
                        currentText.append(line).append("\n");
                    }
                } else {
                    currentText.append(line).append("\n");
                }
            }
        }

        if (inTable && !currentTable.isEmpty()) {
            int cols = calculateTableColumns(currentTable);
            blocks.add(new MessageBlock(true, TextUtils.join("\n", currentTable).trim(), cols));
        } else if (currentText.length() > 0) {
            blocks.add(new MessageBlock(false, currentText.toString().trim(), 0));
        }

        return blocks;
    }

    private int calculateTableColumns(List<String> tableLines) {
        int maxPipes = 0;
        for (String line : tableLines) {
            int pipes = 0;
            for (int i = 0; i < line.length(); i++) {
                if (line.charAt(i) == '|') pipes++;
            }
            if (pipes > maxPipes) maxPipes = pipes;
        }
        return Math.max(3, maxPipes - 1);
    }

    private void showVerticalFollowUps(java.util.List<String> followUps) {
        if (verticalFollowUpsContainer == null || quickSuggestionsScroll == null) return;
        if (followUps == null || followUps.isEmpty()) {
            showDefaultQuickChips();
            return;
        }

        verticalFollowUpsContainer.removeAllViews();

        for (String suggestion : followUps) {
            String cleanSuggestion = suggestion.replace("✨", "").trim();
            TextView pill = new TextView(requireContext());
            pill.setText(cleanSuggestion);
            pill.setTextSize(13);
            pill.setTextColor(0xFF0369A1); // Deep sky blue
            pill.setTypeface(null, android.graphics.Typeface.BOLD);
            pill.setBackgroundResource(R.drawable.followup_pill_bg);
            pill.setPadding(dpToPx(14), dpToPx(8), dpToPx(14), dpToPx(8));
            pill.setClickable(true);
            pill.setFocusable(true);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            lp.setMargins(0, dpToPx(3), 0, dpToPx(3));
            pill.setLayoutParams(lp);

            pill.setOnClickListener(v -> handleUserQuery(cleanSuggestion));
            verticalFollowUpsContainer.addView(pill);
        }

        quickSuggestionsScroll.setVisibility(View.GONE);
        verticalFollowUpsContainer.setVisibility(View.VISIBLE);
    }

    private void showDefaultQuickChips() {
        if (verticalFollowUpsContainer != null) {
            verticalFollowUpsContainer.removeAllViews();
            verticalFollowUpsContainer.setVisibility(View.GONE);
        }
        if (quickSuggestionsScroll != null) {
            quickSuggestionsScroll.setVisibility(View.VISIBLE);
        }
    }

    private void finalizePreviousBotFollowUps() {
        if (currentPendingBotWrapper != null && !currentPendingFollowUps.isEmpty()) {
            java.util.List<String> remaining = new java.util.ArrayList<>();
            for (String f : currentPendingFollowUps) {
                if (!isQueryAlreadyAsked(f)) {
                    remaining.add(f);
                }
            }
            if (!remaining.isEmpty()) {
                attachHorizontalFollowUps(currentPendingBotWrapper, remaining);
            }
            currentPendingBotWrapper = null;
            currentPendingFollowUps.clear();
        }
        showDefaultQuickChips();
    }

    private void recordAskedQuery(String query) {
        if (query == null) return;
        String norm = normalizeQuery(query);
        if (!norm.isEmpty()) {
            askedQueries.add(norm);
        }
    }

    private String normalizeQuery(String q) {
        if (q == null) return "";
        return q.toLowerCase(Locale.ROOT)
                .replace(">>", "")
                .replace("✨", "")
                .replaceAll("[^a-z0-9 ]", " ")
                .trim()
                .replaceAll("\\s+", " ");
    }

    private boolean isQueryAlreadyAsked(String candidate) {
        if (candidate == null) return false;
        String normCand = normalizeQuery(candidate);
        if (normCand.isEmpty()) return true;

        if (askedQueries.contains(normCand)) return true;

        for (String asked : askedQueries) {
            if (asked.equals(normCand)) return true;
            if (normCand.length() >= 10 && asked.contains(normCand)) return true;
            if (asked.length() >= 10 && normCand.contains(asked)) return true;
        }

        // Semantic / key-phrase overlap check
        String[] candWords = normCand.split(" ");
        java.util.Set<String> candSet = new java.util.HashSet<>();
        for (String w : candWords) {
            if (w.length() > 3 && !isStopWord(w)) candSet.add(w);
        }
        if (!candSet.isEmpty()) {
            for (String asked : askedQueries) {
                String[] askedWords = asked.split(" ");
                int matchCount = 0;
                for (String aw : askedWords) {
                    if (candSet.contains(aw)) matchCount++;
                }
                if ((double) matchCount / candSet.size() >= 0.70) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isStopWord(String w) {
        return "what".equals(w) || "when".equals(w) || "where".equals(w) || "which".equals(w)
                || "show".equals(w) || "tell".equals(w) || "about".equals(w) || "does".equals(w)
                || "with".equals(w) || "from".equals(w) || "your".equals(w) || "this".equals(w)
                || "that".equals(w) || "have".equals(w) || "will".equals(w) || "udom".equals(w);
    }

    private static final String[] GLOBAL_RECOMMENDATIONS = new String[]{
            "When are University Examinations (UE)?",
            "Show Software Engineering timetable",
            "Where is Dr. Mwamba's office?",
            "How do I pay fees with GePG control number?",
            "How do HESLB loans work at UDOM?",
            "What is the grading system and pass mark at UDOM?",
            "How do I connect to UDOM campus Wi-Fi?",
            "What are the Central Library hours?",
            "Where is my nearest cafeteria?",
            "Where is the UDOM health center?",
            "How does hostel accommodation work?",
            "Who is the Class Representative (CR)?",
            "What are the steps for first-year registration?",
            "How do I use the SR2 student portal?",
            "How do I get around campus?",
            "What is the Campus Dispensary emergency phone number?",
            "What are the IPT requirements?",
            "Show upcoming 2026/2027 exam dates"
    };

    private java.util.List<String> filterAndEnrichFollowUps(java.util.List<String> rawFollowUps) {
        java.util.List<String> result = new java.util.ArrayList<>();
        if (rawFollowUps != null) {
            for (String s : rawFollowUps) {
                if (s == null) continue;
                String clean = s.replace(">>", "").replace("✨", "").trim();
                if (clean.isEmpty()) continue;
                if (!isQueryAlreadyAsked(clean) && !result.contains(clean)) {
                    result.add(clean);
                }
            }
        }

        // If filtered list has fewer than 2 items, backfill from recommendations
        if (result.size() < 2) {
            for (String rec : GLOBAL_RECOMMENDATIONS) {
                if (!isQueryAlreadyAsked(rec) && !result.contains(rec)) {
                    result.add(rec);
                    if (result.size() >= 3) break;
                }
            }
        }

        return result;
    }

    private String getCategoryFollowUps(String category) {
        if (category == null) category = "general";
        switch (category.toLowerCase(Locale.ROOT)) {
            case "freshers":
                return ">> How do I pay fees with GePG control number?\n>> How do I use the SR2 portal?\n>> How do HESLB loans work at UDOM?";
            case "heslb":
                return ">> How do I contact the UDOSO Minister of Loans?\n>> What are the steps for first-year registration?\n>> How do I pay fees with GePG control number?";
            case "grades":
                return ">> When are University Examinations (UE)?\n>> When is Continuous Assessment Test 1?\n>> What is the dress code policy at UDOM?";
            case "wifi":
                return ">> How do I use the SR2 portal?\n>> What are the Central Library hours?\n>> How do I contact ICT Helpdesk?";
            case "rules":
                return ">> What are the Central Library hours?\n>> When are University Examinations (UE)?\n>> Who is the Class Representative (CR)?";
            case "banking":
                return ">> How do I pay fees with GePG control number?\n>> Where is my nearest cafeteria?\n>> How does campus transport operate?";
            case "hostels":
                return ">> How does campus transport operate?\n>> How do I contact the hostel warden?\n>> How do I pay fees with GePG control number?";
            case "library":
                return ">> How do I connect to UDOM campus Wi-Fi?\n>> What is the dress code policy at UDOM?\n>> Where is Dr. Mwamba's office?";
            case "cafeteria":
                return ">> Where are my lectures or classes?\n>> Show Software Engineering timetable\n>> Where is the UDOM health center?";
            case "fees":
                return ">> How do I use the SR2 portal?\n>> How do HESLB loans work at UDOM?\n>> What are the steps for first-year registration?";
            case "sr2":
                return ">> How do I pay fees with GePG control number?\n>> What is the grading system and pass mark at UDOM?\n>> What are the steps for first-year registration?";
            case "transport":
                return ">> How does hostel accommodation work?\n>> Where is the UDOM health center?\n>> Where are my lectures or classes?";
            case "health":
                return ">> What is the Campus Dispensary emergency phone number?\n>> Where is Benjamin Mkapa Hospital (BMH)?\n>> How do I contact campus security?";
            case "cive":
            case "cobe":
            case "coed":
            case "chss":
            case "cnms":
            case "coese":
            case "sol":
            case "somd":
            case "colleges":
            default:
                return ">> Show Software Engineering timetable\n>> Where is Dr. Mwamba's office?\n>> When are University Examinations (UE)?";
        }
    }

    private void attachHorizontalFollowUps(LinearLayout messageWrapper, java.util.List<String> followUps) {
        Context context = getContext();
        if (context == null || messageWrapper == null || followUps == null || followUps.isEmpty()) return;

        android.widget.HorizontalScrollView pillScrollView = new android.widget.HorizontalScrollView(context);
        pillScrollView.setHorizontalScrollBarEnabled(false);
        pillScrollView.setOverScrollMode(View.OVER_SCROLL_NEVER);

        LinearLayout.LayoutParams scrollParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        scrollParams.setMargins(0, dpToPx(6), 0, dpToPx(2));
        pillScrollView.setLayoutParams(scrollParams);

        LinearLayout pillContainer = new LinearLayout(context);
        pillContainer.setOrientation(LinearLayout.HORIZONTAL);
        pillContainer.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);

        for (String suggestion : followUps) {
            String cleanSuggestion = suggestion.replace("✨", "").trim();
            TextView pill = new TextView(context);
            pill.setText(cleanSuggestion);
            pill.setTextSize(12);
            pill.setTextColor(0xFF0369A1); // Sky blue
            pill.setTypeface(null, android.graphics.Typeface.BOLD);
            pill.setBackgroundResource(R.drawable.followup_pill_bg);
            pill.setPadding(dpToPx(12), dpToPx(7), dpToPx(12), dpToPx(7));
            pill.setClickable(true);
            pill.setFocusable(true);

            LinearLayout.LayoutParams pillParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            pillParams.setMargins(0, 0, dpToPx(8), 0);
            pill.setLayoutParams(pillParams);

            pill.setOnClickListener(v -> handleUserQuery(cleanSuggestion));
            pillContainer.addView(pill);
        }

        pillScrollView.addView(pillContainer);
        messageWrapper.addView(pillScrollView);
    }

    private String getAssistantResponse(String rawQuery) {
        String query = rawQuery.toLowerCase(Locale.ROOT).trim();
        Context context = getContext();
        String userCollege = UserPreferences.getSelectedCollege(context);
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(context);

        // 1. Timetable Queries (Software Engineering)
        if (query.contains("timetable") || query.contains("schedule") || query.contains("lecture") || query.contains("class")
                || query.contains("monday") || query.contains("tuesday") || query.contains("wednesday") || query.contains("thursday") || query.contains("friday")
                || query.contains("networking") || query.contains("database") || query.contains("security") || query.contains("wearable")
                || query.contains("cp 121") || query.contains("cn 121") || query.contains("ia 124") || query.contains("cg 121") || query.contains("cs 123")) {
            String day = "ALL";
            if (query.contains("monday") && query.contains("tuesday")) day = "Monday to Tuesday";
            else if (query.contains("monday")) day = "Monday";
            else if (query.contains("tuesday")) day = "Tuesday";
            else if (query.contains("wednesday")) day = "Wednesday";
            else if (query.contains("thursday")) day = "Thursday";
            else if (query.contains("friday")) day = "Friday";

            int semester = 2; // Default to Semester 2 (current active semester)
            if (query.contains("sem 1") || query.contains("semester 1") || query.contains("first semester")) {
                semester = 1;
            } else if (query.contains("sem 2") || query.contains("semester 2") || query.contains("second semester")) {
                semester = 2;
            }

            if (dbHelper != null) {
                List<DatabaseHelper.TimetableItem> timetable = dbHelper.getTimetable("Software Engineering", day, 1, semester);
                if (timetable.isEmpty() && semester != 0) {
                    timetable = dbHelper.getTimetable("Software Engineering", day, 1, 0);
                }
                if (!timetable.isEmpty()) {
                    StringBuilder sb = new StringBuilder("📅 **BSc Software Engineering Timetable (Semester " + semester + ")**");
                    if (!"ALL".equalsIgnoreCase(day)) sb.append(" - ").append(day);
                    sb.append(":\n\n");
                    sb.append("| Day | Time | Type | Course | Venue | Lecturer |\n");
                    sb.append("|---|---|---|---|---|---|\n");
                    for (DatabaseHelper.TimetableItem item : timetable) {
                        sb.append("| ").append(item.dayOfWeek)
                                .append(" | ").append(item.startTime).append(" - ").append(item.endTime)
                                .append(" | ").append(item.sessionType)
                                .append(" | ").append(item.courseCode).append(" (").append(item.courseName).append(")")
                                .append(" | ").append(item.venue)
                                .append(" | ").append(item.lecturerName)
                                .append(" |\n");
                    }
                    if (semester == 2) {
                        sb.append("\n>> Show Semester 1 timetable\n");
                        sb.append(">> Where is CIVE-LRB 103?\n");
                        sb.append(">> Where is NET_LAB?\n");
                    } else {
                        sb.append("\n>> Show Semester 2 timetable\n");
                        sb.append(">> Where is CIVE Lecture Room A?\n");
                        sb.append(">> Where is Dr. Mwamba's office?\n");
                    }
                    return sb.toString().trim();
                }
            }
        }

        // 2. Professor & Academic Staff Queries
        if (query.contains("prof") || query.contains("dr.") || query.contains("dr ") || query.contains("lecturer")
                || query.contains("mwamba") || query.contains("ndunguru") || query.contains("haule") || query.contains("mushi")
                || query.contains("temba") || query.contains("mmari") || query.contains("feruzi") || query.contains("mjahid")
                || query.contains("manyilizu") || query.contains("mluba") || query.contains("fereji") || query.contains("nixon")
                || query.contains("office") || query.contains("consultation")) {
            if (dbHelper != null) {
                List<DatabaseHelper.ProfessorItem> profs = dbHelper.searchProfessors(query, userCollege);
                if (!profs.isEmpty()) {
                    StringBuilder sb = new StringBuilder("👨‍🏫 **UDOM Academic Staff & Office Directory**:\n\n");
                    for (DatabaseHelper.ProfessorItem p : profs) {
                        sb.append("• **").append(p.name).append("** (").append(p.department).append(")\n")
                                .append("   🏢 Office: ").append(p.officeBlock).append(", ").append(p.officeRoom).append("\n")
                                .append("   📞 Phone: ").append(p.phone).append(" | ✉️ ").append(p.email).append("\n")
                                .append("   📚 Subjects: ").append(p.subjectsTaught).append("\n")
                                .append("   ⏰ Consultation: ").append(p.consultationHours).append("\n\n");
                    }
                    sb.append(">> Where is Dr. Mwamba's office?\n");
                    sb.append(">> Show Software Engineering timetable\n");
                    sb.append(">> Who is the Class Representative (CR)?\n");
                    return sb.toString().trim();
                }
            }
        }

        // 2b. Curriculum Handbook & Degree Requirements (BSc SE 4-Year Catalog)
        if (query.contains("curriculum") || query.contains("handbook") || query.contains("credit") || query.contains("ipt")
                || query.contains("fyp") || query.contains("core") || query.contains("elective")
                || (query.contains("course") && (query.contains("year") || query.contains("sem") || query.contains("software") || query.contains("se")))) {
            if (dbHelper != null) {
                int targetYear = 0;
                if (query.contains("year 1") || query.contains("1st year") || query.contains("first year")) targetYear = 1;
                else if (query.contains("year 2") || query.contains("2nd year") || query.contains("second year")) targetYear = 2;
                else if (query.contains("year 3") || query.contains("3rd year") || query.contains("third year")) targetYear = 3;
                else if (query.contains("year 4") || query.contains("4th year") || query.contains("fourth year")) targetYear = 4;

                int targetSem = 0;
                if (query.contains("sem 1") || query.contains("semester 1") || query.contains("first semester")) targetSem = 1;
                else if (query.contains("sem 2") || query.contains("semester 2") || query.contains("second semester")) targetSem = 2;

                List<DatabaseHelper.CourseItem> courses;
                if (query.contains("ipt")) {
                    courses = dbHelper.searchCourses("IPT", "BSc Software Engineering");
                } else if (query.contains("fyp")) {
                    courses = dbHelper.searchCourses("FYP", "BSc Software Engineering");
                } else if (targetYear > 0) {
                    courses = dbHelper.getCourses("BSc Software Engineering", targetYear, targetSem);
                } else {
                    courses = dbHelper.searchCourses(query, "BSc Software Engineering");
                    if (courses.isEmpty()) {
                        courses = dbHelper.getCourses("BSc Software Engineering", 1, 2);
                        targetYear = 1;
                        targetSem = 2;
                    }
                }

                if (!courses.isEmpty()) {
                    StringBuilder sb = new StringBuilder("📚 **BSc Software Engineering Curriculum Handbook**");
                    if (targetYear > 0) sb.append(" - Year ").append(targetYear);
                    if (targetSem > 0) sb.append(" (Semester ").append(targetSem).append(")");
                    sb.append(":\n\n");
                    sb.append("| Code | Course Title | Credits | Status | Category | Yr | Sem |\n");
                    sb.append("|---|---|---|---|---|---|---|\n");
                    double totalCredits = 0.0;
                    for (DatabaseHelper.CourseItem c : courses) {
                        sb.append("| ").append(c.courseCode)
                                .append(" | ").append(c.courseTitle)
                                .append(" | ").append(c.credits)
                                .append(" | ").append(c.courseStatus)
                                .append(" | ").append(c.category)
                                .append(" | Yr ").append(c.yearOfStudy)
                                .append(" | Sem ").append(c.semester)
                                .append(" |\n");
                        totalCredits += c.credits;
                    }
                    sb.append("\n📊 **Total Credits:** ").append(String.format(java.util.Locale.US, "%.1f", totalCredits)).append("\n\n");
                    sb.append(">> What courses are in Year 2 Semester 1?\n");
                    sb.append(">> What are the IPT requirements?\n");
                    sb.append(">> Show Year 4 FYP courses\n");
                    return sb.toString().trim();
                }
            }
        }

        // 3. Academic Calendar & Exam Dates
        if (query.contains("ue") || query.contains("exam") || query.contains("test") || query.contains("calendar")
                || query.contains("holiday") || query.contains("break") || query.contains("sports") || query.contains("baraza")
                || query.contains("orientation") || query.contains("suppl") || query.contains("graduation")) {
            if (dbHelper != null) {
                boolean askForPast = query.contains("past") || query.contains("previous") || query.contains("last year")
                        || query.contains("history") || query.contains("old") || query.contains("2024");

                String targetYear = "2025/2026";
                if (askForPast) {
                    targetYear = "2024/2025";
                } else if (query.contains("2026/2027") || query.contains("2027") || query.contains("next year")) {
                    targetYear = "2026/2027";
                }

                String searchTerm = query;
                if (query.contains("suppl")) {
                    searchTerm = "Supplementary";
                } else if (query.contains("graduation")) {
                    searchTerm = "Graduation";
                } else if (query.contains("orientation") || query.contains("freshers")) {
                    searchTerm = "Orientation";
                } else if (query.contains("ipt")) {
                    searchTerm = "IPT";
                } else if (query.contains("test") || query.contains("ca ")) {
                    searchTerm = "Continuous Assessment";
                } else if (query.contains("ue") || query.contains("final exam")) {
                    searchTerm = "University Examinations";
                }

                List<DatabaseHelper.CalendarEventItem> events = dbHelper.searchCalendarEvents(searchTerm, targetYear, askForPast);
                if (events.isEmpty() && !askForPast) {
                    events = dbHelper.searchCalendarEvents(searchTerm, null, false);
                }

                if (!events.isEmpty()) {
                    // For specific single events like Supplementary exams, provide a concise direct answer
                    if (query.contains("suppl")) {
                        DatabaseHelper.CalendarEventItem ev = events.get(0);
                        String dates = DatabaseHelper.formatFriendlyDateRange(ev.startDate, ev.endDate);
                        return "📅 **" + ev.eventName + " (" + targetYear + ")**\n\n"
                                + "• Dates: **" + dates + "**\n"
                                + "• Details: " + ev.description + "\n\n"
                                + ">> When are University Examinations (UE)?\n"
                                + ">> What is the grading system and pass mark at UDOM?";
                    }

                    StringBuilder sb = new StringBuilder();
                    if (askForPast) {
                        sb.append("📅 **Past Academic Calendar & Examinations (").append(targetYear).append(" - Completed)**:\n\n");
                    } else if (query.contains("ue") || query.contains("exam")) {
                        sb.append("📅 **Upcoming University Examinations (").append(targetYear).append(")**:\n\n");
                    } else {
                        sb.append("📅 **Upcoming Academic Calendar (").append(targetYear).append(")**:\n\n");
                    }

                    sb.append("| Semester | Event / Examination | Dates | Key Details |\n");
                    sb.append("|---|---|---|---|\n");
                    for (DatabaseHelper.CalendarEventItem ev : events) {
                        String dates = DatabaseHelper.formatFriendlyDateRange(ev.startDate, ev.endDate);
                        sb.append("| ").append(ev.semester)
                                .append(" | ").append(ev.eventName)
                                .append(" | ").append(dates)
                                .append(" | ").append(ev.description)
                                .append(" |\n");
                    }

                    if (!askForPast) {
                        if (query.contains("ue") || query.contains("exam")) {
                            sb.append("\n💡 **Next Upcoming Exam**: **Semester 1 UE (09 Mar – 20 Mar 2026)** across all colleges. Minimum 16/40 in CA required.\n\n");
                            sb.append("Need a different session or academic year?\n");
                            sb.append(">> Show upcoming 2026/2027 exam dates\n");
                            sb.append(">> When is Continuous Assessment Test 1?\n");
                            sb.append(">> View past 2024/2025 exam dates\n");
                        } else {
                            sb.append("\n>> When are University Examinations (UE)?\n");
                            sb.append(">> When is Continuous Assessment Test 1?\n");
                            sb.append(">> Show BSc Software Engineering timetable\n");
                        }
                    } else {
                        sb.append("\n>> Show upcoming 2025/2026 exam dates\n");
                        sb.append(">> When is the next exam?\n");
                    }
                    return sb.toString().trim();
                }
            }
        }

        // 4. Student Leaders, Prefects & CRs
        if (query.contains("cr") || query.contains("prefect") || query.contains("leader") || query.contains("president")
                || query.contains("warden") || query.contains("udoso") || query.contains("loan minister")) {
            if (dbHelper != null) {
                List<DatabaseHelper.StudentLeaderItem> leaders = dbHelper.searchStudentLeaders(query, userCollege);
                if (!leaders.isEmpty()) {
                    StringBuilder sb = new StringBuilder("👥 **Student Leaders & Class Representatives**:\n\n");
                    for (DatabaseHelper.StudentLeaderItem l : leaders) {
                        sb.append("• **").append(l.name).append("** – ").append(l.role).append("\n")
                                .append("   🏠 Room: ").append(l.officeOrHostel).append(", ").append(l.roomNumber).append("\n")
                                .append("   📞 Phone: ").append(l.phone).append(" | ✉️ ").append(l.email).append("\n\n");
                    }
                    sb.append(">> Who is the Software Engineering Class Representative (CR)?\n");
                    sb.append(">> Where is the UDOSO President's office?\n");
                    sb.append(">> How do I contact the hostel warden?\n");
                    return sb.toString().trim();
                }
            }
        }

        // 4b. Campus Locations & Buildings
        if (query.contains("where is") || query.contains("location") || query.contains("building") || query.contains("block")
                || query.contains("room") || query.contains("auditorium") || query.contains("hall") || query.contains("gate")
                || query.contains("direction")) {
            if (dbHelper != null) {
                List<DatabaseHelper.LocationItem> locs = dbHelper.searchLocations(query, userCollege);
                if (!locs.isEmpty()) {
                    StringBuilder sb = new StringBuilder("📍 **UDOM Campus Locations & Buildings**:\n\n");
                    for (DatabaseHelper.LocationItem l : locs) {
                        sb.append("• **").append(l.name).append("** (").append(l.category).append(")\n")
                                .append("   🏫 College: ").append(l.college).append("\n")
                                .append("   ℹ️ ").append(l.description).append("\n\n");
                    }
                    sb.append("💡 You can find these locations on the **MAP** tab for live navigation.\n\n");
                    sb.append(">> Show Software Engineering timetable\n");
                    sb.append(">> Where is Dr. Mwamba's office?\n");
                    sb.append(">> How do I get around campus?\n");
                    return sb.toString().trim();
                }
            }
        }

        // 5. Campus Emergency & Department Contacts
        if (query.contains("contact") || query.contains("phone") || query.contains("emergency") || query.contains("ambulance")
                || query.contains("police") || query.contains("security") || query.contains("helpdesk") || query.contains("dispensary")) {
            if (dbHelper != null) {
                List<DatabaseHelper.CampusContactItem> contacts = dbHelper.searchCampusContacts(query, userCollege);
                if (!contacts.isEmpty()) {
                    StringBuilder sb = new StringBuilder("📞 **UDOM Verified Campus Directory**:\n\n");
                    for (DatabaseHelper.CampusContactItem c : contacts) {
                        sb.append("• **").append(c.departmentOrOffice).append("** (").append(c.category).append(")\n")
                                .append("   📍 Location: ").append(c.officeLocation).append("\n")
                                .append("   📞 Phone: ").append(c.phoneNumber).append(" | ✉️ ").append(c.email).append("\n")
                                .append("   ℹ️ ").append(c.description).append("\n\n");
                    }
                    sb.append(">> What is the Campus Dispensary emergency phone number?\n");
                    sb.append(">> Where is the CIVE Dean's office?\n");
                    sb.append(">> How do I contact ICT Helpdesk?\n");
                    return sb.toString().trim();
                }
            }
        }

        // 6. Context-aware queries based on user's selected college
        if (query.contains("my class") || query.contains("where do i study") || query.contains("my room")) {
            return getCollegeLocationInfo(userCollege)
                    + "\n\n>> Show Software Engineering timetable\n>> Where is my nearest cafeteria?";
        }

        if (query.contains("my cafeteria") || query.contains("nearest cafeteria") || query.contains("nearest food") || query.contains("where to eat")) {
            return getCollegeCafeteriaInfo(userCollege)
                    + "\n\n>> Where are my lectures or classes?\n>> Show Software Engineering timetable";
        }

        if (query.contains("my college") || query.contains("my profile") || query.contains("who am i")) {
            String name = UserPreferences.getSelectedCollegeFullName(getContext());
            return "🎯 Your current student profile is identified as: " + userCollege + " (" + name + ").\n\n"
                    + getCollegeLocationInfo(userCollege) + "\n\n"
                    + "💡 You can switch your college identity at any time on the HOME screen."
                    + "\n\n>> Show Software Engineering timetable\n>> Where is Dr. Mwamba's office?\n>> When is University Examination (UE)?";
        }

        if (query.contains("hello") || query.contains("hi") || query.contains("mambo") || query.contains("habari") || query.contains("hey")) {
            return "👋 Hello! I recognize you as a " + userCollege + " student.\n\n"
                    + "How can I help you today? Ask me about your classes, Software Engineering timetable, professors, UE exam dates, CRs, or cafeteria!"
                    + "\n\n>> Show Software Engineering timetable\n>> Where is Dr. Mwamba's office?\n>> When are University Examinations (UE)?";
        }

        // 7. Verified UDOM knowledge base matching from SQLite
        if (dbHelper != null) {
            DatabaseHelper.FaqItem item = dbHelper.findFaqItem(query, userCollege);
            if (item != null && item.answer != null && !item.answer.isEmpty()) {
                StringBuilder sb = new StringBuilder(item.answer);
                if (query.contains(userCollege.toLowerCase(Locale.ROOT))) {
                    sb.append("\n\n⭐ Note: This is your currently selected college!");
                }
                sb.append("\n\n").append(getCategoryFollowUps(item.category));
                return sb.toString();
            }
        }

        // 8. Helpful fallback
        return "🤖 Thank you for your question!\n\n"
                + "Since you are identified as a " + userCollege + " student, you can ask me or explore any suggestion below:\n"
                + "• Lectures & Courses timetables\n"
                + "• Professor offices & consultation hours\n"
                + "• UE examination dates & tests\n"
                + "• Student leaders, CRs & hostel wardens\n"
                + "• HESLB loan disbursements & GePG fees\n\n"
                + ">> Show Software Engineering timetable\n"
                + ">> Where is Dr. Mwamba's office?\n"
                + ">> When are University Examinations (UE)?";
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