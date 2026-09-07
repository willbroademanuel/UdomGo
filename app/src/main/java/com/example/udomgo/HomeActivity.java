package com.example.udomgo;

import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);

        // Bottom navigation

        LinearLayout homeNav = findViewById(R.id.homeNav);
        LinearLayout chatNav = findViewById(R.id.chatNav);
        LinearLayout mapNav = findViewById(R.id.mapNav);
        LinearLayout guideNav = findViewById(R.id.guideNav);
        LinearLayout servicesNav = findViewById(R.id.servicesNav);


        // Open Home by default

        if (savedInstanceState == null) {
            openFragment(new HomeFragment());
        }


        // HOME

        homeNav.setOnClickListener(v ->
                openFragment(new HomeFragment())
        );


        // CHAT

        chatNav.setOnClickListener(v ->
                openFragment(new ChatFragment())
        );


        // MAP

        mapNav.setOnClickListener(v ->
                openFragment(new MapFragment())
        );


        // GUIDE

        guideNav.setOnClickListener(v ->
                openFragment(new GuideFragment())
        );


        // SERVICES

        servicesNav.setOnClickListener(v ->
                openFragment(new ServicesFragment())
        );
    }


    private void openFragment(Fragment fragment) {

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }
}