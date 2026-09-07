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
                Toast.makeText(requireContext(), "Chat refreshed", Toast.LENGTH_SHORT).show();
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
        String userCollege = UserPreferences.getSelectedCollege(getContext());

        // Context-aware queries based on user's selected college
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

        // General queries
        if (query.contains("cive") || query.contains("informatics") || query.contains("computer")) {
            return "💻 College of Informatics and Virtual Education (CIVE)\n\n"
                    + "• Location: North-East campus zone.\n"
                    + "• Facilities: Lecture Rooms A & B, Auditorium, Blocks 1–6, Computer Laboratories.\n"
                    + "• Programs: Computer Science, Software Engineering, Telecommunications, Information Systems, Cyber Security.\n"
                    + (userCollege.equalsIgnoreCase("CIVE") ? "\n⭐ Note: This is your currently selected college!" : "");
        }

        if (query.contains("cobe") || query.contains("business") || query.contains("economics") || query.contains("accounting")) {
            return "🏛️ College of Business and Economics (CoBE)\n\n"
                    + "• Location: Central campus near administration block.\n"
                    + "• Facilities: CoBE Auditoriums, lecture halls, and departmental library.\n"
                    + "• Programs: Accounting, Finance, Marketing, Economics, Human Resources, Procurement.\n"
                    + (userCollege.equalsIgnoreCase("CoBE") ? "\n⭐ Note: This is your currently selected college!" : "");
        }

        if (query.contains("coed") || query.contains("education") || query.contains("teacher")) {
            return "🎓 College of Education (CoED)\n\n"
                    + "• Location: Central academic wing.\n"
                    + "• Focus: Science and Arts teacher education, pedagogy, and educational administration.\n"
                    + (userCollege.equalsIgnoreCase("CoED") ? "\n⭐ Note: This is your currently selected college!" : "");
        }

        if (query.contains("chss") || query.contains("humanities") || query.contains("social")) {
            return "📚 College of Humanities and Social Sciences (CHSS)\n\n"
                    + "• Location: West campus area.\n"
                    + "• Departments: Sociology, Political Science, Languages, History, Geography.\n"
                    + (userCollege.equalsIgnoreCase("CHSS") ? "\n⭐ Note: This is your currently selected college!" : "");
        }

        if (query.contains("cnms") || query.contains("natural") || query.contains("math") || query.contains("science") || query.contains("physics") || query.contains("chemistry")) {
            return "🔬 College of Natural and Mathematical Sciences (CNMS)\n\n"
                    + "• Location: Science complex area.\n"
                    + "• Facilities: Mathematics research rooms, advanced physics, chemistry, biology laboratories.\n"
                    + (userCollege.equalsIgnoreCase("CNMS") ? "\n⭐ Note: This is your currently selected college!" : "");
        }

        if (query.contains("coese") || query.contains("coet") || query.contains("engineering") || query.contains("mining") || query.contains("geology")) {
            return "🏗️ College of Earth Sciences and Engineering (CoESE)\n\n"
                    + "• Location: Engineering campus wing.\n"
                    + "• Programs: Mining Engineering, Petroleum Geology, Environmental Engineering, Renewable Energy.\n"
                    + (userCollege.equalsIgnoreCase("CoESE") ? "\n⭐ Note: This is your currently selected college!" : "");
        }

        if (query.contains("law") || query.contains("sol")) {
            return "⚖️ School of Law (SoL)\n\n"
                    + "• Offers Bachelor of Laws (LL.B) and postgraduate legal programs with a dedicated campus moot court.";
        }

        if (query.contains("somd") || query.contains("medicine") || query.contains("doctor") || query.contains("nursing") || query.contains("sonph")) {
            return "🩺 School of Medicine & Nursing (SoMD / SoNPH)\n\n"
                    + "• Location: Health Sciences campus near Benjamin Mkapa Hospital.\n"
                    + "• Programs: Doctor of Medicine (MD), Pharmacy, Nursing, and Public Health.";
        }

        if (query.contains("colleges") || query.contains("faculty") || query.contains("faculties") || query.contains("schools")) {
            return "🏛️ UDOM Colleges & Schools:\n\n"
                    + "• CIVE – Informatics & Virtual Education\n"
                    + "• CoBE – Business & Economics\n"
                    + "• CoED – Education\n"
                    + "• CHSS – Humanities & Social Sciences\n"
                    + "• CNMS – Natural & Mathematical Sciences\n"
                    + "• CoESE – Earth Sciences & Engineering\n"
                    + "• School of Law (SoL)\n"
                    + "• School of Medicine (SoMD)\n"
                    + "• School of Nursing (SoNPH)\n\n"
                    + "💡 You can select any of these on the HOME screen to customize your profile.";
        }

        if (query.contains("hostel") || query.contains("accommodation") || query.contains("room") || query.contains("dorm") || query.contains("sleep")) {
            return "🏠 UDOM Accommodation & Hostels\n\n"
                    + "• On-Campus: Rooms are organized per college blocks. You apply via your SR2 portal.\n"
                    + "• For " + userCollege + " students, hostel blocks are designated near your academic complex.\n"
                    + "• Off-Campus: Popular areas include Mkonze, Kisasa, Medeli, and Chimwaga.";
        }

        if (query.contains("library") || query.contains("book") || query.contains("reading") || query.contains("study")) {
            return "📚 UDOM Central Library\n\n"
                    + "• Location: Central campus.\n"
                    + "• Opening Hours:\n"
                    + "   - Mon – Fri: 08:00 AM – 10:00 PM\n"
                    + "   - Sat: 08:00 AM – 04:00 PM\n"
                    + "   - Sun: 02:00 PM – 08:00 PM\n"
                    + "• Carry your Student ID card for entry and book borrowing.";
        }

        if (query.contains("cafeteria") || query.contains("food") || query.contains("eat") || query.contains("lunch") || query.contains("breakfast") || query.contains("dinner") || query.contains("canteen")) {
            return getCollegeCafeteriaInfo(userCollege);
        }

        if (query.contains("fee") || query.contains("control number") || query.contains("gepg") || query.contains("payment") || query.contains("pay") || query.contains("tuition")) {
            return "💳 Fee Payments & GePG Control Numbers\n\n"
                    + "1. Sign in to your SR2 account (sr2.udom.ac.tz).\n"
                    + "2. Go to 'Payment Invoices' and generate a GePG Control Number.\n"
                    + "3. Pay via Mobile Money (M-Pesa, Airtel, Tigo Pesa, HaloPesa) or CRDB / NMB bank.\n"
                    + "4. Retain the SMS confirmation receipt.";
        }

        if (query.contains("sr2") || query.contains("srmis") || query.contains("register") || query.contains("registration") || query.contains("result") || query.contains("portal")) {
            return "📋 SR2 Student Portal (sr2.udom.ac.tz)\n\n"
                    + "• Official portal for " + userCollege + " course registration, GPA/results, control numbers, and hostel allocation.\n"
                    + "• Log in using your Registration Number and password.";
        }

        if (query.contains("transport") || query.contains("bus") || query.contains("daladala") || query.contains("bajaji") || query.contains("how to reach") || query.contains("shuttle")) {
            return "🚌 Campus Transport\n\n"
                    + "• Campus shuttles connect the Roundabout to " + userCollege + " and surrounding colleges.\n"
                    + "• Daladala city buses run directly between Dodoma Town (Posta) and UDOM.\n"
                    + "• Bajajis operate at all college gates.";
        }

        if (query.contains("health") || query.contains("hospital") || query.contains("dispensary") || query.contains("clinic") || query.contains("sick")) {
            return "🏥 UDOM Health Center\n\n"
                    + "• Located centrally on campus for primary consultations and outpatient treatment.\n"
                    + "• Benjamin Mkapa Hospital is nearby for specialized clinical care.";
        }

        if (query.contains("hello") || query.contains("hi") || query.contains("mambo") || query.contains("habari") || query.contains("hey")) {
            return "👋 Hello! I recognize you as a " + userCollege + " student.\n\n"
                    + "How can I help you today? Ask me about your classes, nearest cafeteria, library, or fees!";
        }

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