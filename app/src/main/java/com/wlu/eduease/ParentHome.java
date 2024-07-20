package com.wlu.eduease;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import java.util.Locale;

public class ParentHome extends Fragment {

    private CalendarView calendarView;
    private CardView scheduleCardView;
    private TextView facultyNameTextView;
    private TextView subjectTextView;
    private TextView timeTextView;
    private TextView roomTextView;

    private DatabaseReference databaseReference;

    public ParentHome() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_parent, container, false);

        calendarView = view.findViewById(R.id.calendarView);
        scheduleCardView = view.findViewById(R.id.ptmScheduleCardView);
        facultyNameTextView = view.findViewById(R.id.facultyNameTextView);
        subjectTextView = view.findViewById(R.id.subjectTextView);
        timeTextView = view.findViewById(R.id.timeTextView);
        roomTextView = view.findViewById(R.id.roomTextView);

        databaseReference = FirebaseDatabase.getInstance().getReference("ptm_schedule");

        calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
            String selectedDate = String.format(Locale.getDefault(), "%d-%02d-%02d", year, month + 1, dayOfMonth);
            fetchPTMSchedule(selectedDate);
        });

        return view;
    }

    private void fetchPTMSchedule(String date) {
        databaseReference.orderByChild("date").equalTo(date).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot scheduleSnapshot : snapshot.getChildren()) {
                        String facultyName = scheduleSnapshot.child("faculty_name").getValue(String.class);
                        String subjectName = scheduleSnapshot.child("subject").getValue(String.class);
                        String time = scheduleSnapshot.child("time").getValue(String.class);
                        String room = scheduleSnapshot.child("room_number").getValue(String.class);

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
