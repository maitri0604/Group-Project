package com.wlu.eduease;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AssignmentAddFragment extends Fragment {

    private static final String TAG = "AssignmentAddFragment"; // For logging
    private Spinner spinnerSubject;
    private EditText editTextTitle, editTextDate, editTextPdfUrl;
    private Button buttonSaveAssignment;
    private TableLayout tableLayoutAssignments;
    private DatabaseReference facultyDataRef, assignmentsRef;
    private List<String> subjects = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private String selectedSubject;

    public AssignmentAddFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_assignment_add, container, false);

        // Initialize Firebase Database references
        facultyDataRef = FirebaseDatabase.getInstance().getReference("faculty_data");
        assignmentsRef = FirebaseDatabase.getInstance().getReference("assignments");

        // Initialize UI components
        spinnerSubject = view.findViewById(R.id.spinnerSubject);
        editTextTitle = view.findViewById(R.id.editTextTitle);
        editTextDate = view.findViewById(R.id.editTextDate);
        editTextPdfUrl = view.findViewById(R.id.editTextPdfUrl);
        buttonSaveAssignment = view.findViewById(R.id.buttonSaveAssignment);
        tableLayoutAssignments = view.findViewById(R.id.tableLayoutAssignments);

        // Initialize Spinner and Adapter
        adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, subjects);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSubject.setAdapter(adapter);

        // Load subjects into Spinner
        loadAllSubjects();

        // Spinner item selection listener
        spinnerSubject.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedSubject = subjects.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedSubject = null;
            }
        });

        // Set OnClickListener for the date EditText
        editTextDate.setOnClickListener(v -> showDatePickerDialog());

        // Save assignment button click listener
        buttonSaveAssignment.setOnClickListener(v -> saveAssignment());

        // Load assignments into TableLayout
        loadAssignments();

        return view;
    }

    private void loadAllSubjects() {
        facultyDataRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                subjects.clear();
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot courseSnapshot : userSnapshot.child("name").getChildren()) {
                        String courseName = courseSnapshot.getValue(String.class);
                        subjects.add(courseName);
                    }
                }
                // Notify the adapter that the data has changed
                adapter.notifyDataSetChanged();
                Log.d(TAG, "Number of subjects loaded: " + subjects.size());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), "Failed to load subjects: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Failed to load subjects: " + databaseError.getMessage());
            }
        });
    }

    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // Format month as MM
                    String monthStr = String.format("%02d", selectedMonth + 1);
                    // Format day as DD
                    String dayStr = String.format("%02d", selectedDay);
                    // Set date in YYYY-MM-DD format
                    String date = selectedYear + "-" + monthStr + "-" + dayStr;
                    editTextDate.setText(date);
                }, year, month, day);
        datePickerDialog.show();
    }

    private void saveAssignment() {
        String title = editTextTitle.getText().toString().trim();
        String date = editTextDate.getText().toString().trim();
        String pdfUrl = editTextPdfUrl.getText().toString().trim();

        if (selectedSubject == null || title.isEmpty() || date.isEmpty() || pdfUrl.isEmpty()) {
            Toast.makeText(getContext(), "Please fill all fields and provide a PDF URL", Toast.LENGTH_SHORT).show();
            return;
        }

        assignmentsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Find the next available ID
                int nextId = getNextAvailableId(dataSnapshot);

                // Create and add the new assignment with the next ID
                addAssignment(nextId, selectedSubject, title, date, pdfUrl);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to read data: " + databaseError.getMessage());
                Toast.makeText(getContext(), "Failed to save assignment", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private int getNextAvailableId(DataSnapshot dataSnapshot) {
        int maxId = 0;

        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
            String idStr = snapshot.getKey();
            try {
                int id = Integer.parseInt(idStr);
                if (id > maxId) {
                    maxId = id;
                }
            } catch (NumberFormatException e) {
                Log.w(TAG, "Skipping invalid ID: " + idStr);
            }
        }

        return maxId + 1;
    }

    private void addAssignment(int id, String subject, String title, String date, String pdfUrl) {
        String idStr = new DecimalFormat("00").format(id); // Format ID with leading zeroes
        Assignment assignment = new Assignment(subject, title, date, pdfUrl);
        assignmentsRef.child(idStr).setValue(assignment).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(getContext(), "Assignment added successfully with ID: " + idStr, Toast.LENGTH_SHORT).show();
                clearInputFields();
                loadAssignments(); // Reload assignments after saving
            } else {
                Log.e(TAG, "Failed to save assignment: " + task.getException().getMessage());
                Toast.makeText(getContext(), "Failed to save assignment", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clearInputFields() {
        editTextTitle.setText("");
        editTextDate.setText("");
        editTextPdfUrl.setText("");
    }

    private void loadAssignments() {
        assignmentsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                tableLayoutAssignments.removeAllViews(); // Clear existing rows
                // Add header row
                TableRow headerRow = new TableRow(getActivity());
                addTextViewToRow(headerRow, "Assignment ID");
                addTextViewToRow(headerRow, "Subject");
                addTextViewToRow(headerRow, "Title");
                addTextViewToRow(headerRow, "Date");
                addTextViewToRow(headerRow, "PDF URL");
                tableLayoutAssignments.addView(headerRow);

                // Add data rows
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String id = snapshot.getKey();
                    Assignment assignment = snapshot.getValue(Assignment.class);

                    TableRow row = new TableRow(getActivity());
                    addTextViewToRow(row, id);
                    addTextViewToRow(row, assignment.subject);
                    addTextViewToRow(row, assignment.title);
                    addTextViewToRow(row, assignment.date);
                    addTextViewToRow(row, assignment.pdfUrl);
                    tableLayoutAssignments.addView(row);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to load assignments: " + databaseError.getMessage());
            }
        });
    }

    private void addTextViewToRow(TableRow row, String text) {
        TextView textView = new TextView(getActivity());
        textView.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
        textView.setText(text);
        row.addView(textView);
    }

    // Inner class to represent Assignment data
    private static class Assignment {
        public String subject;
        public String title;
        public String date;
        public String pdfUrl;

        public Assignment() {
            // Default constructor required for calls to DataSnapshot.getValue(Assignment.class)
        }

        public Assignment(String subject, String title, String date, String pdfUrl) {
            this.subject = subject;
            this.title = title;
            this.date = date;
            this.pdfUrl = pdfUrl;
        }
    }
}