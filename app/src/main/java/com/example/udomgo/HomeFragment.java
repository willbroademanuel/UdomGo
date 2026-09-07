package com.example.udomgo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class HomeFragment extends Fragment {

    public HomeFragment() {
        // Required empty constructor
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
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

        // COLLEGES

        Button civeButton = view.findViewById(R.id.civeButton);
        Button cobeButton = view.findViewById(R.id.cobeButton);
        Button coeseButton = view.findViewById(R.id.coeseButton);
        Button coedButton = view.findViewById(R.id.coedButton);
        Button chssButton = view.findViewById(R.id.chssButton);
        Button cnmsButton = view.findViewById(R.id.cnmsButton);

        // INSTITUTES

        Button ciButton = view.findViewById(R.id.ciButton);
        Button idsButton = view.findViewById(R.id.idsButton);

        // SCHOOLS

        Button solButton = view.findViewById(R.id.solButton);
        Button somdButton = view.findViewById(R.id.somdButton);
        Button sonphButton = view.findViewById(R.id.sonphButton);


        civeButton.setOnClickListener(v ->
                showMessage("CIVE selected"));

        cobeButton.setOnClickListener(v ->
                showMessage("CoBE selected"));

        coeseButton.setOnClickListener(v ->
                showMessage("CoESE selected"));

        coedButton.setOnClickListener(v ->
                showMessage("CoED selected"));

        chssButton.setOnClickListener(v ->
                showMessage("CHSS selected"));

        cnmsButton.setOnClickListener(v ->
                showMessage("CNMS selected"));

        ciButton.setOnClickListener(v ->
                showMessage("CI selected"));

        idsButton.setOnClickListener(v ->
                showMessage("IDS selected"));

        solButton.setOnClickListener(v ->
                showMessage("SoL selected"));

        somdButton.setOnClickListener(v ->
                showMessage("SoMD selected"));

        sonphButton.setOnClickListener(v ->
                showMessage("SoNPH selected"));
    }

    private void showMessage(String message) {

        Toast.makeText(
                requireContext(),
                message,
                Toast.LENGTH_SHORT
        ).show();
    }
}