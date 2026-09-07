package com.example.udomgo;

import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

public class ChatFragment extends Fragment {

    private EditText chatInput;
    private ImageButton sendButton;
    private LinearLayout chatContainer;
    private ScrollView chatScrollView;
    private LinearLayout chatInputBar;

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
        chatContainer = view.findViewById(R.id.chatContainer);
        chatScrollView = view.findViewById(R.id.chatScrollView);
        chatInputBar = view.findViewById(R.id.chatInputBar);

        /*
         * Move the input bar directly above the keyboard.
         *
         * We subtract the navigation/system-bar height because
         * the keyboard inset includes that area as well.
         */
        ViewCompat.setOnApplyWindowInsetsListener(
                view,
                (v, insets) -> {

                    androidx.core.graphics.Insets imeInsets =
                            insets.getInsets(
                                    WindowInsetsCompat.Type.ime()
                            );

                    androidx.core.graphics.Insets systemInsets =
                            insets.getInsets(
                                    WindowInsetsCompat.Type.systemBars()
                            );

                    int keyboardHeight =
                            Math.max(
                                    0,
                                    imeInsets.bottom - systemInsets.bottom
                            );

                    chatInputBar.setTranslationY(-Math.max(0, keyboardHeight -0));

                    return insets;
                }
        );

        ViewCompat.requestApplyInsets(view);

        sendButton.setOnClickListener(v -> sendMessage());

        chatInput.setOnEditorActionListener(
                (v, actionId, event) -> {

                    if (actionId == EditorInfo.IME_ACTION_SEND ||
                            (event != null &&
                                    event.getKeyCode()
                                            == KeyEvent.KEYCODE_ENTER &&
                                    event.getAction()
                                            == KeyEvent.ACTION_DOWN)) {

                        sendMessage();
                        return true;
                    }

                    return false;
                }
        );
    }

    private void sendMessage() {

        String question =
                chatInput.getText().toString().trim();

        if (question.isEmpty()) {
            return;
        }

        // User message → RIGHT
        addMessage(question, true);

        // Temporary response → LEFT
        addMessage(
                "Your question has been received. UDOMGo will provide the answer here.",
                false
        );

        chatInput.setText("");

        chatScrollView.post(() ->
                chatScrollView.fullScroll(View.FOCUS_DOWN)
        );
    }

    private void addMessage(
            String message,
            boolean userMessage) {

        TextView textView =
                new TextView(requireContext());

        textView.setText(message);

        textView.setTextSize(14);

        textView.setTextColor(
                userMessage
                        ? 0xFF000000
                        : 0xFF333333
        );

        textView.setPadding(
                16,
                12,
                16,
                12
        );

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        params.setMargins(
                0,
                6,
                0,
                6
        );

        if (userMessage) {

            params.gravity = Gravity.END;

            textView.setBackgroundResource(
                    R.drawable.chat_user_message
            );

        } else {

            params.gravity = Gravity.START;

            textView.setBackgroundResource(
                    R.drawable.chat_bot_message
            );
        }

        chatContainer.addView(
                textView,
                params
        );
    }
}