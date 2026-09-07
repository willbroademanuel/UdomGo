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
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ChatFragment extends Fragment {

    private EditText chatInput;
    private ImageButton sendButton;
    private ImageButton clearChatButton;
    private LinearLayout chatContainer;
    private ScrollView chatScrollView;

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

        // Clear Chat button
        if (clearChatButton != null) {
            clearChatButton.setOnClickListener(v -> {
                chatContainer.removeAllViews();
                showWelcomeMessage();
                Toast.makeText(requireContext(), "Chat cleared", Toast.LENGTH_SHORT).show();
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

        // Add Initial Welcome message
        if (chatContainer.getChildCount() == 0) {
            showWelcomeMessage();
        }
    }

    private void setupSuggestionChips(View root) {
        setChipListener(root, R.id.chipColleges, "Tell me about UDOM colleges");
        setChipListener(root, R.id.chipCive, "Tell me about CIVE");
        setChipListener(root, R.id.chipHostels, "How does hostel accommodation work?");
        setChipListener(root, R.id.chipLibrary, "What are the library hours and services?");
        setChipListener(root, R.id.chipCafeteria, "Where are the cafeterias and food spots?");
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

    private void showWelcomeMessage() {
        String welcome = "👋 Hello! Welcome to UDOMGo Assistant.\n\n"
                + "I can help you with campus information, including:\n"
                + "• Colleges & Schools (CIVE, CoBE, CoED, etc.)\n"
                + "• Hostels & Student Accommodation\n"
                + "• Central Library Services & Hours\n"
                + "• Cafeterias & Campus Dining\n"
                + "• Fee Payments & SR2 Registration\n"
                + "• Campus Transport & Health Center\n\n"
                + "Tap any quick topic above or type your question below!";

        addMessage(welcome, false);
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
        // Add User message
        addMessage(question, true);

        // Hide keyboard smoothly
        hideKeyboard();

        // Simulate thinking briefly for natural chat experience
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (!isAdded()) return;
            String answer = getAssistantResponse(question);
            addMessage(answer, false);
        }, 350);
    }

    private void addMessage(String message, boolean isUser) {
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
        timeView.setText(timeFormat.format(new Date()));
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

        if (query.contains("cive") || query.contains("informatics") || query.contains("computer")) {
            return "💻 College of Informatics and Virtual Education (CIVE)\n\n"
                    + "• Location: North-East campus.\n"
                    + "• Key Buildings: Lecture Rooms A & B, Auditorium, Blocks 1–6, Specialized Computing Labs.\n"
                    + "• Programs: Computer Science, Software Engineering, Telecommunications, Information Systems, Cyber Security.\n\n"
                    + "💡 Tip: You can view exact CIVE lecture rooms on the MAP tab!";
        }

        if (query.contains("cobe") || query.contains("business") || query.contains("economics") || query.contains("accounting")) {
            return "🏛️ College of Business and Economics (CoBE)\n\n"
                    + "• Location: Near the main university administration complex.\n"
                    + "• Programs: Accounting, Finance, Economics, Marketing, HR, Procurement.\n"
                    + "• Facilities: Modern lecture auditoriums, departmental offices, and student cafeteria.";
        }

        if (query.contains("coed") || query.contains("education") || query.contains("teacher")) {
            return "🎓 College of Education (CoED)\n\n"
                    + "• Location: Central campus zone.\n"
                    + "• Focus: Training secondary school educators, educational managers, and curriculum developers in Science and Arts.";
        }

        if (query.contains("chss") || query.contains("humanities") || query.contains("social")) {
            return "📚 College of Humanities and Social Sciences (CHSS)\n\n"
                    + "• Location: West campus wing.\n"
                    + "• Departments: Sociology, Political Science, Languages, History, and Geography.";
        }

        if (query.contains("cnms") || query.contains("natural") || query.contains("math") || query.contains("science") || query.contains("physics") || query.contains("chemistry")) {
            return "🔬 College of Natural and Mathematical Sciences (CNMS)\n\n"
                    + "• Location: Central science zone.\n"
                    + "• Facilities: Advanced physics, chemistry, biology laboratories, and mathematics research rooms.";
        }

        if (query.contains("coese") || query.contains("coet") || query.contains("engineering") || query.contains("mining") || query.contains("geology")) {
            return "🏗️ College of Earth Sciences and Engineering (CoESE)\n\n"
                    + "• Focus: Mining Engineering, Petroleum Geology, Environmental Engineering, and Renewable Energy studies.";
        }

        if (query.contains("law") || query.contains("sol")) {
            return "⚖️ School of Law (SoL)\n\n"
                    + "• Offers Bachelor of Laws (LL.B) and postgraduate legal programs with a dedicated moot courtroom.";
        }

        if (query.contains("somd") || query.contains("medicine") || query.contains("doctor") || query.contains("nursing") || query.contains("sonph")) {
            return "🩺 School of Medicine & Nursing (SoMD / SoNPH)\n\n"
                    + "• Location: Health Sciences campus near Benjamin Mkapa Hospital.\n"
                    + "• Programs: Doctor of Medicine (MD), Pharmacy, Nursing, and Public Health.";
        }

        if (query.contains("colleges") || query.contains("faculty") || query.contains("faculties") || query.contains("schools")) {
            return "🏛️ UDOM Colleges & Schools:\n\n"
                    + "1. CIVE – Informatics & Virtual Education\n"
                    + "2. CoBE – Business & Economics\n"
                    + "3. CoED – Education\n"
                    + "4. CHSS – Humanities & Social Sciences\n"
                    + "5. CNMS – Natural & Mathematical Sciences\n"
                    + "6. CoESE – Earth Sciences & Engineering\n"
                    + "7. School of Law (SoL)\n"
                    + "8. School of Medicine & Health Sciences";
        }

        if (query.contains("hostel") || query.contains("accommodation") || query.contains("room") || query.contains("dorm") || query.contains("sleep")) {
            return "🏠 UDOM Accommodation & Hostels\n\n"
                    + "• On-Campus: Hostels are organized into college blocks. Applications are made through the SR2 portal.\n"
                    + "• Off-Campus: Popular student areas include Mkonze, Kisasa, Chimwaga, and Medeli.\n"
                    + "• Tip: Apply early on SR2 at semester registration to secure on-campus rooms.";
        }

        if (query.contains("library") || query.contains("book") || query.contains("reading") || query.contains("study")) {
            return "📚 UDOM Central Library\n\n"
                    + "• Services: Printed book collections, e-resource computer labs, discussion rooms.\n"
                    + "• Opening Hours:\n"
                    + "   - Mon – Fri: 08:00 AM – 10:00 PM\n"
                    + "   - Sat: 08:00 AM – 04:00 PM\n"
                    + "   - Sun: 02:00 PM – 08:00 PM\n"
                    + "• Note: Always carry your Student ID card for entry and book borrowing.";
        }

        if (query.contains("cafeteria") || query.contains("food") || query.contains("eat") || query.contains("lunch") || query.contains("breakfast") || query.contains("dinner") || query.contains("canteen")) {
            return "🍽️ Campus Dining & Cafeterias\n\n"
                    + "• Every college features a dedicated student cafeteria.\n"
                    + "• Meals: Breakfast (tea, snacks), Lunch & Dinner (rice, ugali, beef, chicken, fish, beans, greens).\n"
                    + "• Price range: TZS 1,500 – 3,500 per meal.";
        }

        if (query.contains("fee") || query.contains("control number") || query.contains("gepg") || query.contains("payment") || query.contains("pay") || query.contains("tuition")) {
            return "💳 Fee Payments & GePG Control Numbers\n\n"
                    + "1. Sign in to your SR2 account (sr2.udom.ac.tz).\n"
                    + "2. Go to 'Payment Invoices' and generate a GePG Control Number.\n"
                    + "3. Pay via Mobile Money (M-Pesa, Airtel, Tigo Pesa, HaloPesa) or CRDB / NMB bank.\n"
                    + "4. Payment is verified automatically within a few minutes.";
        }

        if (query.contains("sr2") || query.contains("srmis") || query.contains("register") || query.contains("registration") || query.contains("result") || query.contains("portal")) {
            return "📋 SR2 Student Portal (sr2.udom.ac.tz)\n\n"
                    + "• Use SR2 for:\n"
                    + "  - Semester course registration\n"
                    + "  - Viewing examination results & GPA\n"
                    + "  - Generating fee control numbers\n"
                    + "  - Hostel application\n"
                    + "• Default login uses your Registration Number and chosen password.";
        }

        if (query.contains("transport") || query.contains("bus") || query.contains("daladala") || query.contains("bajaji") || query.contains("how to reach") || query.contains("shuttle")) {
            return "🚌 Transport to & Around UDOM\n\n"
                    + "• From Town: Take a daladala or bus from Dodoma town center (Posta / Machinga Complex) to UDOM.\n"
                    + "• On Campus: Campus shuttles and Bajajis run continuously connecting Roundabout, CIVE, CoBE, and CoED.\n"
                    + "• Check the MAP tab to see walking routes and building directions!";
        }

        if (query.contains("health") || query.contains("hospital") || query.contains("dispensary") || query.contains("clinic") || query.contains("sick")) {
            return "🏥 UDOM Health Center\n\n"
                    + "• Located centrally on campus for primary healthcare, consultations, and emergency first aid.\n"
                    + "• For specialized hospital care, Benjamin Mkapa Hospital is located adjacent to the campus.";
        }

        if (query.contains("hello") || query.contains("hi") || query.contains("mambo") || query.contains("habari") || query.contains("hey")) {
            return "👋 Hello there! How can I assist you with UDOM today?\n\n"
                    + "Feel free to ask about colleges, hostels, library, food, or fee payments!";
        }

        if (query.contains("help") || query.contains("what can you do") || query.contains("options")) {
            return "💡 Here are questions you can ask me:\n"
                    + "• 'Where is CIVE?'\n"
                    + "• 'How does hostel accommodation work?'\n"
                    + "• 'What are library hours?'\n"
                    + "• 'How to pay tuition fees?'\n"
                    + "• 'How to register on SR2?'\n"
                    + "• 'Tell me about campus transport'";
        }

        return "🤖 Thank you for your question!\n\n"
                + "To help you best, here are common topics you can ask me about:\n"
                + "• Colleges (CIVE, CoBE, CoED, CHSS, CNMS, Law, Medicine)\n"
                + "• Hostels & Accommodation\n"
                + "• Library hours & services\n"
                + "• Cafeteria & food on campus\n"
                + "• Tuition fees & GePG payment\n"
                + "• Campus transport & health center";
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