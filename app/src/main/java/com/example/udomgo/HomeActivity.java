package com.example.udomgo;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

public class HomeActivity extends AppCompatActivity {

    public static final int TAB_HOME = 0;
    public static final int TAB_CHAT = 1;
    public static final int TAB_MAP = 2;
    public static final int TAB_GUIDE = 3;
    public static final int TAB_SERVICES = 4;

    private ImageView homeIcon, chatIcon, mapIcon, guideIcon, servicesIcon;
    private TextView homeText, chatText, mapText, guideText, servicesText;
    private LinearLayout bottomNavBar;
    private int currentTab = TAB_HOME;

    private boolean isKeyboardOpen = false;
    private int lastKeyboardHeight = 0;

    private static final int COLOR_ACTIVE = 0xFF102A43;
    private static final int COLOR_INACTIVE = 0xFF64748B;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);

        // Find Bottom Navigation Container
        bottomNavBar = findViewById(R.id.bottomNavBar);

        // Modern Android (API 30+ / Android 15 / targetSdk 36 edge-to-edge):
        // Listen to IME insets to detect keyboard height and rise accordingly
        View mainHomeLayout = findViewById(R.id.mainHomeLayout);
        if (mainHomeLayout != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainHomeLayout, (v, insets) -> {
                int imeBottom = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom;
                applyKeyboardAdjustment(imeBottom);
                return insets;
            });
        }

        // Global Layout fallback for all devices and emulators
        View rootView = findViewById(android.R.id.content);
        if (rootView != null) {
            rootView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
                android.graphics.Rect r = new android.graphics.Rect();
                rootView.getWindowVisibleDisplayFrame(r);
                int screenHeight = rootView.getRootView().getHeight();
                int keypadHeight = screenHeight - r.bottom;

                if (keypadHeight > screenHeight * 0.15) {
                    applyKeyboardAdjustment(keypadHeight);
                } else if (lastKeyboardHeight > 0 && keypadHeight < screenHeight * 0.10) {
                    applyKeyboardAdjustment(0);
                }
            });
        }

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

    private void applyKeyboardAdjustment(int keyboardHeight) {
        if (keyboardHeight < 0) keyboardHeight = 0;
        lastKeyboardHeight = keyboardHeight;
        isKeyboardOpen = keyboardHeight > 100;

        // Toggle bottom navigation bar
        if (bottomNavBar != null) {
            if (currentTab == TAB_HOME || isKeyboardOpen) {
                bottomNavBar.setVisibility(View.GONE);
            } else {
                bottomNavBar.setVisibility(View.VISIBLE);
            }
        }

        // Apply bottom padding if the window was NOT resized by the OS
        View mainHomeLayout = findViewById(R.id.mainHomeLayout);
        View rootView = findViewById(android.R.id.content);
        if (mainHomeLayout != null && rootView != null) {
            int paddingToApply = 0;
            if (isKeyboardOpen) {
                int contentHeight = rootView.getHeight();
                int totalHeight = rootView.getRootView().getHeight();
                boolean windowAlreadyResized = (totalHeight > 0 && contentHeight > 0
                        && (totalHeight - contentHeight) > keyboardHeight * 0.7);

                if (!windowAlreadyResized) {
                    paddingToApply = keyboardHeight;
                }
            }

            if (mainHomeLayout.getPaddingBottom() != paddingToApply) {
                mainHomeLayout.setPadding(
                        mainHomeLayout.getPaddingLeft(),
                        mainHomeLayout.getPaddingTop(),
                        mainHomeLayout.getPaddingRight(),
                        paddingToApply
                );
            }
        }
    }

    public void navigateToTab(int tabIndex) {
        currentTab = tabIndex;

        // Hide bottom navigation completely on TAB_HOME or when keyboard is open
        if (bottomNavBar != null) {
            if (tabIndex == TAB_HOME || isKeyboardOpen) {
                bottomNavBar.setVisibility(View.GONE);
            } else {
                bottomNavBar.setVisibility(View.VISIBLE);
            }
        }

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