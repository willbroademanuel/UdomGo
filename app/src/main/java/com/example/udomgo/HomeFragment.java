package com.example.udomgo;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.HashMap;
import java.util.Map;

public class HomeFragment extends Fragment {

    private final Map<String, Button> collegeButtons = new HashMap<>();
    private final Map<String, String> collegeFullNames = new HashMap<>();

    public HomeFragment() {
        // Required empty constructor
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        return inflater.inflate(
                R.layout.fragment_home,
                container,
                false
        );
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        // Register all colleges, institutes and schools
        registerCollege(view, R.id.civeButton, "CIVE", "College of Informatics & Virtual Education");
        registerCollege(view, R.id.cobeButton, "CoBE", "College of Business & Economics");
        registerCollege(view, R.id.coeseButton, "CoESE", "College of Earth Sciences & Engineering");
        registerCollege(view, R.id.coedButton, "CoED", "College of Education");
        registerCollege(view, R.id.chssButton, "CHSS", "College of Humanities & Social Sciences");
        registerCollege(view, R.id.cnmsButton, "CNMS", "College of Natural & Mathematical Sciences");
        registerCollege(view, R.id.ciButton, "CI", "Confucius Institute");
        registerCollege(view, R.id.idsButton, "IDS", "Institute of Development Studies");
        registerCollege(view, R.id.solButton, "SoL", "School of Law");
        registerCollege(view, R.id.somdButton, "SoMD", "School of Medicine");
        registerCollege(view, R.id.sonphButton, "SoNPH", "School of Nursing & Public Health");

        // Load and apply saved user college
        Context context = getContext();
        String currentCollege = UserPreferences.getSelectedCollege(context);
        updateActiveSelection(currentCollege);
    }

    private void registerCollege(View root, int buttonId, String code, String fullName) {
        Button button = root.findViewById(buttonId);
        if (button != null) {
            collegeButtons.put(code, button);
            collegeFullNames.put(code, fullName);

            button.setOnClickListener(v -> selectCollege(code));
        }
    }

    private void selectCollege(String code) {
        Context context = getContext();
        if (context == null) return;

        String fullName = collegeFullNames.get(code);
        if (fullName == null) fullName = code;

        // Persist user selection silently
        UserPreferences.setSelectedCollege(context, code, fullName);

        // Update UI button highlights
        updateActiveSelection(code);

        // Toast feedback
        Toast.makeText(
                context,
                code + " selected",
                Toast.LENGTH_SHORT
        ).show();

        // Immediately land on Chat tab
        if (getActivity() instanceof HomeActivity) {
            ((HomeActivity) getActivity()).navigateToTab(HomeActivity.TAB_CHAT);
        }
    }

    private void updateActiveSelection(String activeCode) {
        // Highlight selected button, unhighlight others
        for (Map.Entry<String, Button> entry : collegeButtons.entrySet()) {
            String code = entry.getKey();
            Button btn = entry.getValue();

            if (code.equalsIgnoreCase(activeCode)) {
                btn.setBackgroundResource(R.drawable.college_btn_active);
                btn.setTextColor(0xFFFFFFFF);
            } else {
                btn.setBackgroundResource(R.drawable.college_btn_inactive);
                btn.setTextColor(0xFF102A43);
            }
        }
    }
}