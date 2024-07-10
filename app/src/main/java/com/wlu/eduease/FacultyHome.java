package com.wlu.eduease;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class FacultyHome extends Fragment {

    private FirebaseAuth auth;
    private TextView textView;
    private FirebaseUser user;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_faculty, container, false);

        Button btnCourseMaterial = view.findViewById(R.id.btnCourseMaterial);

        btnCourseMaterial.setOnClickListener(v -> {
            // Create the new fragment
            course_material courseMaterialFragment = new course_material();

            // Replace the current fragment with the new fragment
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, courseMaterialFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });

        return view;
    }
}
