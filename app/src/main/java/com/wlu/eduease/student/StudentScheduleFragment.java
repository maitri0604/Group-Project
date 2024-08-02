package com.wlu.eduease.student;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.cardview.widget.CardView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.wlu.eduease.R;

import java.util.Locale;

public class StudentScheduleFragment extends Fragment {

    private CalendarView calendarView;
    private CardView scheduleCardView;
    private TextView facultyNameTextView;
    private TextView subjectTextView;
    private TextView timeTextView;
    private TextView roomTextView;

    private DatabaseReference databaseReference;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_student_schedule, container, false);

        calendarView = view.findViewById(R.id.calendarView);
        scheduleCardView = view.findViewById(R.id.scheduleCardView);
        facultyNameTextView = view.findViewById(R.id.facultyNameTextView);
        subjectTextView = view.findViewById(R.id.subjectTextView);
        timeTextView = view.findViewById(R.id.timeTextView);
        roomTextView = view.findViewById(R.id.roomTextView);

        databaseReference = FirebaseDatabase.getInstance().getReference("class_schedules");

        calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
            String selectedDate = String.format(Locale.getDefault(), "%d/%d/%d", dayOfMonth, month + 1, year);
            fetchClassSchedule(selectedDate);
        });

        return view;
    }

    private void fetchClassSchedule(String date) {
        databaseReference.orderByChild("day").equalTo(date).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot scheduleSnapshot : snapshot.getChildren()) {
                        String facultyName = scheduleSnapshot.child("faculty_name").getValue(String.class);
                        String subjectName = scheduleSnapshot.child("subject").getValue(String.class);
                        String time = scheduleSnapshot.child("time").getValue(String.class);
                        String room = scheduleSnapshot.child("room").getValue(String.class);

                        facultyNameTextView.setText(facultyName);
                        subjectTextView.setText(subjectName);
                        timeTextView.setText(time);
                        roomTextView.setText(room);
                    }
                } else {
                    facultyNameTextView.setText("No Data");
                    subjectTextView.setText("No Data");
                    timeTextView.setText("No Data");
                    roomTextView.setText("No Data");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle possible errors
            }
        });
    }
}
