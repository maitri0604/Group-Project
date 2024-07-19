package com.wlu.eduease;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ClassScheduleFragment extends Fragment {

    private EditText roomEditText;
    private Spinner subjectSpinner;
    private TextView facultyNameText, dayTextView, timeTextView;
    private Button saveButton;
    private RecyclerView scheduleRecyclerView;
    private DatabaseReference classSchedulesRef;
    private DatabaseReference userRef;
    private DatabaseReference facultyDataRef;
    private String currentUserName;
    private List<String> subjectsList;
    private List<Schedule> scheduleList;
    private ScheduleAdapter scheduleAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_class_schedule, container, false);

        facultyNameText = view.findViewById(R.id.facultyNameText);
        subjectSpinner = view.findViewById(R.id.subjectSpinner);
        dayTextView = view.findViewById(R.id.dayTextView);
        timeTextView = view.findViewById(R.id.timeTextView);
        roomEditText = view.findViewById(R.id.roomEditText);
        saveButton = view.findViewById(R.id.saveButton);
        scheduleRecyclerView = view.findViewById(R.id.scheduleRecyclerView);

        classSchedulesRef = FirebaseDatabase.getInstance().getReference("class_schedules");
        userRef = FirebaseDatabase.getInstance().getReference("users");
        facultyDataRef = FirebaseDatabase.getInstance().getReference("faculty_data");

        subjectsList = new ArrayList<>();
        scheduleList = new ArrayList<>();
        scheduleAdapter = new ScheduleAdapter(scheduleList);

        // Set up RecyclerView
        scheduleRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        scheduleRecyclerView.setAdapter(scheduleAdapter);

        // Set the faculty name and populate the spinner with subjects
        setFacultyName();
        populateSubjects();

        // Set click listeners for date and time TextViews
        dayTextView.setOnClickListener(v -> showDatePicker());
        timeTextView.setOnClickListener(v -> showTimePicker());

        saveButton.setOnClickListener(v -> saveClassSchedule());

        // Load class schedules
        loadClassSchedules();

        return view;
    }

    private void setFacultyName() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            userRef.child(userId).child("fullname").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    currentUserName = dataSnapshot.getValue(String.class);
                    if (currentUserName != null) {
                        facultyNameText.setText(currentUserName);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    databaseError.toException().printStackTrace();
                }
            });
        } else {
            facultyNameText.setText("Not Logged In");
        }
    }

    private void populateSubjects() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            facultyDataRef.child(userId).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    subjectsList.clear();
                    for (DataSnapshot subjectSnapshot : dataSnapshot.getChildren()) {
                        String subject = subjectSnapshot.getValue(String.class);
                        if (subject != null) {
                            subjectsList.add(subject);
                        }
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, subjectsList);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    subjectSpinner.setAdapter(adapter);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    databaseError.toException().printStackTrace();
                }
            });
        }
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), (view, selectedYear, selectedMonth, selectedDay) -> {
            String date = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
            dayTextView.setText(date);
        }, year, month, day);
        datePickerDialog.show();
    }

    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR); // Use HOUR for 12-hour format
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(),
                (view, selectedHour, selectedMinute) -> {
                    String amPm = (selectedHour >= 12) ? "PM" : "AM";
                    int displayHour = (selectedHour % 12 == 0) ? 12 : selectedHour % 12; // Convert to 12-hour format
                    String time = String.format("%02d:%02d %s", displayHour, selectedMinute, amPm);
                    timeTextView.setText(time);
                }, hour, minute, false); // false for 12-hour format
        timePickerDialog.show();
    }

    private void saveClassSchedule() {
        String facultyName = facultyNameText.getText().toString().trim();
        String subject = (String) subjectSpinner.getSelectedItem();
        String day = dayTextView.getText().toString().trim();
        String time = timeTextView.getText().toString().trim();
        String room = roomEditText.getText().toString().trim();

        if (!facultyName.isEmpty() && !subject.isEmpty() && !day.isEmpty() && !time.isEmpty() && !room.isEmpty()) {
            String scheduleId = UUID.randomUUID().toString(); // Generate a unique ID for the schedule
            Map<String, Object> schedule = new HashMap<>();
            schedule.put("faculty_name", facultyName);
            schedule.put("subject", subject);
            schedule.put("day", day);
            schedule.put("time", time);
            schedule.put("room", room);

            classSchedulesRef.child(scheduleId).setValue(schedule)
                    .addOnSuccessListener(aVoid -> {
                        // Successfully saved
                        clearFields();
                    })
                    .addOnFailureListener(e -> {
                        // Failed to save
                        e.printStackTrace();
                    });
        }
    }

    private void clearFields() {
        subjectSpinner.setSelection(0);
        dayTextView.setText("");
        timeTextView.setText("");
        roomEditText.setText("");
    }

    private void loadClassSchedules() {
        classSchedulesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                scheduleList.clear();
                for (DataSnapshot scheduleSnapshot : dataSnapshot.getChildren()) {
                    Schedule schedule = scheduleSnapshot.getValue(Schedule.class);
                    if (schedule != null) {
                        scheduleList.add(schedule);
                    }
                }
                scheduleAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                databaseError.toException().printStackTrace();
            }
        });
    }

    // Schedule Model Class
    public static class Schedule {
        private String faculty_name;
        private String subject;
        private String day;
        private String time;
        private String room;

        // Default constructor required for calls to DataSnapshot.getValue(Schedule.class)
        public Schedule() {}

        // Getters and setters
        public String getFaculty_name() {
            return faculty_name;
        }

        public void setFaculty_name(String faculty_name) {
            this.faculty_name = faculty_name;
        }

        public String getSubject() {
            return subject;
        }

        public void setSubject(String subject) {
            this.subject = subject;
        }

        public String getDay() {
            return day;
        }

        public void setDay(String day) {
            this.day = day;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public String getRoom() {
            return room;
        }

        public void setRoom(String room) {
            this.room = room;
        }
    }

    // Schedule Adapter Class
    public static class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ViewHolder> {

        private final List<Schedule> scheduleList;

        public ScheduleAdapter(List<Schedule> scheduleList) {
            this.scheduleList = scheduleList;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_schedule, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Schedule schedule = scheduleList.get(position);
            holder.facultyNameTextView.setText(schedule.getFaculty_name());
            holder.subjectTextView.setText(schedule.getSubject());
            holder.dayTextView.setText(schedule.getDay());
            holder.timeTextView.setText(schedule.getTime());
            holder.roomTextView.setText(schedule.getRoom());
        }

        @Override
        public int getItemCount() {
            return scheduleList.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView facultyNameTextView;
            TextView subjectTextView;
            TextView dayTextView;
            TextView timeTextView;
            TextView roomTextView;

            ViewHolder(View itemView) {
                super(itemView);
                facultyNameTextView = itemView.findViewById(R.id.facultyNameTextView);
                subjectTextView = itemView.findViewById(R.id.subjectTextView);
                dayTextView = itemView.findViewById(R.id.dayTextView);
                timeTextView = itemView.findViewById(R.id.timeTextView);
                roomTextView = itemView.findViewById(R.id.roomTextView);
            }
        }
    }
}
