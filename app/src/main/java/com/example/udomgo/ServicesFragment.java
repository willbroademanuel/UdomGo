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