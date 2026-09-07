package com.example.udomgo;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

public class HomeActivity extends AppCompatActivity {

    public static final int TAB_HOME = 0;
    public static final int TAB_CHAT = 1;
    public static final int TAB_MAP = 2;
    public static final int TAB_GUIDE = 3;
    public static final int TAB_SERVICES = 4;

    private ImageView homeIcon, chatIcon, mapIcon, guideIcon, servicesIcon;
    private TextView homeText, chatText, mapText, guideText, servicesText;

    private static final int COLOR_ACTIVE = 0xFF102A43;
    private static final int COLOR_INACTIVE = 0xFF64748B;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);

        // Find Navigation containers
        LinearLayout homeNav = findViewById(R.id.homeNav);
        LinearLayout chatNav = findViewById(R.id.chatNav);
        LinearLayout mapNav = findViewById(R.id.mapNav);
        LinearLayout guideNav = findViewById(R.id.guideNav);
        LinearLayout servicesNav = findViewById(R.id.servicesNav);

        // Find Navigation icons & labels
        homeIcon = findViewById(R.id.homeIcon);
        chatIcon = findViewById(R.id.chatIcon);
        mapIcon = findViewById(R.id.mapIcon);
        guideIcon = findViewById(R.id.guideIcon);
        servicesIcon = findViewById(R.id.servicesIcon);

        homeText = findViewById(R.id.homeText);
        chatText = findViewById(R.id.chatText);
        mapText = findViewById(R.id.mapText);
        guideText = findViewById(R.id.guideText);
        servicesText = findViewById(R.id.servicesText);

        // Set Tab Click Listeners
        homeNav.setOnClickListener(v -> navigateToTab(TAB_HOME));
        chatNav.setOnClickListener(v -> navigateToTab(TAB_CHAT));
        mapNav.setOnClickListener(v -> navigateToTab(TAB_MAP));
        guideNav.setOnClickListener(v -> navigateToTab(TAB_GUIDE));
        servicesNav.setOnClickListener(v -> navigateToTab(TAB_SERVICES));

        // Open Home by default
        if (savedInstanceState == null) {
            navigateToTab(TAB_HOME);
        }
    }

    public void navigateToTab(int tabIndex) {
        Fragment fragment;
        switch (tabIndex) {
            case TAB_CHAT:
                fragment = new ChatFragment();
                break;
            case TAB_MAP:
                fragment = new MapFragment();
                break;
            case TAB_GUIDE:
                fragment = new GuideFragment();
                break;
            case TAB_SERVICES:
                fragment = new ServicesFragment();
                break;
            case TAB_HOME:
            default:
                fragment = new HomeFragment();
                tabIndex = TAB_HOME;
                break;
        }

        updateTabStyles(tabIndex);
        openFragment(fragment);
    }

    private void updateTabStyles(int activeTab) {
        setNavStyle(homeIcon, homeText, activeTab == TAB_HOME);
        setNavStyle(chatIcon, chatText, activeTab == TAB_CHAT);
        setNavStyle(mapIcon, mapText, activeTab == TAB_MAP);
        setNavStyle(guideIcon, guideText, activeTab == TAB_GUIDE);
        setNavStyle(servicesIcon, servicesText, activeTab == TAB_SERVICES);
    }

    private void setNavStyle(ImageView icon, TextView text, boolean isActive) {
        int color = isActive ? COLOR_ACTIVE : COLOR_INACTIVE;
        if (icon != null) {
            icon.setImageTintList(ColorStateList.valueOf(color));
        }
        if (text != null) {
            text.setTextColor(color);
        }
    }

    private void openFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }
}