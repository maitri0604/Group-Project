package com.wlu.eduease;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;

public class tests_marks_add extends Fragment {

    private Spinner spinnerTests;
    private LinearLayout studentMarksContainer;
    private DatabaseReference studentDataRef, testsRef;
    private ArrayList<String> studentNames;
    private ArrayList<String> testNames;
    private ArrayAdapter<String> testsAdapter;
    private String selectedTest;

    public tests_marks_add() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_tests_marks_add, container, false);

        // Initialize UI components
        spinnerTests = view.findViewById(R.id.spinnerTests);
        studentMarksContainer = view.findViewById(R.id.llStudentMarksContainer);
        Button btnSaveMarks = view.findViewById(R.id.btnSaveMarks);

        // Initialize Firebase Database references
        studentDataRef = FirebaseDatabase.getInstance().getReference().child("studentTuples");
        testsRef = FirebaseDatabase.getInstance().getReference().child("quizzes");

        // Initialize student names and test names lists
        studentNames = new ArrayList<>();
        testNames = new ArrayList<>();

        // Initialize Spinner and Adapter
        testsAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, testNames);
        testsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTests.setAdapter(testsAdapter);

        // Load data
        loadAllStudentNames();
        loadAllTests();

        // Spinner item selection listener
        spinnerTests.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < testNames.size()) {
                    selectedTest = testNames.get(position);
                } else {
                    selectedTest = null;
                }
                Log.d("tests_marks_add", "Selected test: " + selectedTest);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedTest = null;
            }
        });

        // Save marks button click listener
        btnSaveMarks.setOnClickListener(v -> saveMarks());

        return view;
    }

    private void loadAllStudentNames() {
        studentDataRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                studentMarksContainer.removeAllViews();
                studentNames.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String studentName = snapshot.child("Name").getValue(String.class);
                    if (studentName != null) {
                        studentNames.add(studentName);
                        addStudentView(studentName);
                    }
                }
                Log.d("tests_marks_add", "Student names loaded: " + studentNames.size());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), "Failed to load students: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("tests_marks_add", "Failed to load students: " + databaseError.getMessage());
            }
        });
    }

    private void loadAllTests() {
        testsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                testNames.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String testName = snapshot.child("title").getValue(String.class);
                    Log.d("tests_marks_add", "Fetched test name: " + testName); // Log fetched test names
                    if (testName != null) {
                        testNames.add(testName);
                    }
                }
                Log.d("tests_marks_add", "Test names loaded: " + testNames.size());
                testsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), "Failed to load tests: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("tests_marks_add", "Failed to load tests: " + databaseError.getMessage());
            }
        });
    }


    private void addStudentView(String studentName) {
        // Create a horizontal LinearLayout
        LinearLayout studentLayout = new LinearLayout(getActivity());
        studentLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        studentLayout.setOrientation(LinearLayout.HORIZONTAL);

        // Create a TextView for student name
        TextView nameTextView = new TextView(getActivity());
        nameTextView.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f));
        nameTextView.setText(studentName);
        nameTextView.setPadding(8, 8, 8, 8);

        // Create an EditText for marks
        EditText marksEditText = new EditText(getActivity());
        marksEditText.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f));
        marksEditText.setHint("Marks");
        marksEditText.setPadding(8, 8, 8, 8);

        // Add views to horizontal LinearLayout
        studentLayout.addView(nameTextView);
        studentLayout.addView(marksEditText);

        // Add horizontal LinearLayout to container
        studentMarksContainer.addView(studentLayout);
    }

    private void saveMarks() {
        if (selectedTest == null) {
            Toast.makeText(getActivity(), "Please select a test", Toast.LENGTH_SHORT).show();
            return;
        }

        // Implement the logic to save marks for students
        Toast.makeText(getActivity(), "Marks saved for test: " + selectedTest, Toast.LENGTH_SHORT).show();
    }
}
