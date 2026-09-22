package com.example.udomgo;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ServicesFragment extends Fragment {

    public ServicesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_services, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        android.widget.TextView profileNameText = view.findViewById(R.id.profileNameText);
        android.widget.TextView profileEmailText = view.findViewById(R.id.profileEmailText);
        android.widget.Button logoutButton = view.findViewById(R.id.logoutButton);
        android.widget.Button changePasswordButton = view.findViewById(R.id.changePasswordButton);

        if (getContext() != null) {
            String userName = UserPreferences.getCurrentUserName(requireContext());
            String userEmail = UserPreferences.getCurrentUserEmail(requireContext());

            if (profileNameText != null) {
                profileNameText.setText("👤 " + userName);
            }
            if (profileEmailText != null && !userEmail.isEmpty()) {
                profileEmailText.setText(userEmail);
            }
        }

        if (changePasswordButton != null) {
            changePasswordButton.setOnClickListener(v -> {
                android.widget.EditText input = new android.widget.EditText(requireContext());
                input.setHint("Enter new password");
                input.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
                android.widget.FrameLayout container_input = new android.widget.FrameLayout(requireContext());
                android.widget.FrameLayout.LayoutParams params = new  android.widget.FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.leftMargin = 50; params.rightMargin = 50;
                input.setLayoutParams(params);
                container_input.addView(input);

                new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                        .setTitle("Change Password")
                        .setView(container_input)
                        .setPositiveButton("Update", (dialog, which) -> {
                            String newPass = input.getText().toString().trim();
                            if (newPass.length() < 4) {
                                android.widget.Toast.makeText(requireContext(), "Password too short", android.widget.Toast.LENGTH_SHORT).show();
                                return;
                            }
                            String email = UserPreferences.getCurrentUserEmail(requireContext());
                            if (UserPreferences.updatePassword(requireContext(), email, newPass)) {
                                android.widget.Toast.makeText(requireContext(), "Password updated successfully", android.widget.Toast.LENGTH_SHORT).show();
                            } else {
                                android.widget.Toast.makeText(requireContext(), "Failed to update password", android.widget.Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });
        }

        if (logoutButton != null) {
            logoutButton.setOnClickListener(v -> {
                if (getContext() == null || getActivity() == null) return;

                new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                        .setTitle("Log Out")
                        .setMessage("Are you sure you want to log out?")
                        .setPositiveButton("Log Out", (dialog, which) -> {
                            UserPreferences.logout(requireContext());
                            android.widget.Toast.makeText(
                                    requireContext(),
                                    "Logged out successfully",
                                    android.widget.Toast.LENGTH_SHORT
                            ).show();

                            android.content.Intent intent =
                                    new android.content.Intent(requireActivity(), LoginActivity.class);
                            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            requireActivity().finish();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });
        }
    }
}