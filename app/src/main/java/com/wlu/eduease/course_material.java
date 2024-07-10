package com.wlu.eduease;

import static androidx.fragment.app.FragmentManager.TAG;

import android.os.Bundle;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class course_material extends Fragment {

    private EditText etCourseMaterial;
    private Button btnSaveCourseMaterial;

    private DatabaseReference facultyDataRef;
    private FirebaseUser currentUser; // Firebase user object to get current user

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_course_matrial, container, false);

        // Initialize Firebase references
        facultyDataRef = FirebaseDatabase.getInstance().getReference().child("faculty_data");

        etCourseMaterial = view.findViewById(R.id.etCourseMaterial);
        btnSaveCourseMaterial = view.findViewById(R.id.btnSaveCourseMaterial);

        // Get current user from Firebase Authentication
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        btnSaveCourseMaterial.setOnClickListener(v -> {
            String courseMaterial = etCourseMaterial.getText().toString().trim();
            if (!courseMaterial.isEmpty()) {
                saveCourseMaterial(courseMaterial);
            } else {
                Toast.makeText(getActivity(), "Please enter course material", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void saveCourseMaterial(String courseMaterial) {
        if (currentUser == null) {
            Toast.makeText(getActivity(), "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = currentUser.getUid();

        DatabaseReference userFacultyDataRef = facultyDataRef.child(uid).child("name");
        userFacultyDataRef.setValue(courseMaterial)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getActivity(), "Course material saved", Toast.LENGTH_SHORT).show();
                    etCourseMaterial.setText("");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getActivity(), "Failed to save course material: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Failed to save course material", e);
                });
    }

}
