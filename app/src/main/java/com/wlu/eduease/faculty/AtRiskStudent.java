package com.wlu.eduease.faculty;

import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.wlu.eduease.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AtRiskStudent extends AppCompatActivity {

    private static final String TAG = "AtRiskStudent";
    private DatabaseReference mDatabase;
    private LinearLayout resultContainer;
    private OkHttpClient client;
    private Gson gson;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_at_risk_student);

        resultContainer = findViewById(R.id.result_container);

        client = new OkHttpClient();
        gson = new Gson();
        mDatabase = FirebaseDatabase.getInstance().getReference("students");

        fetchDataFromFirebase();
    }

    private void fetchDataFromFirebase() {
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Map<String, Object>> studentsList = new ArrayList<>();
                for (DataSnapshot studentSnapshot : dataSnapshot.getChildren()) {
                    Map<String, Object> studentData = (Map<String, Object>) studentSnapshot.getValue();
                    studentData.put("student_id", studentSnapshot.getKey());
                    studentsList.add(studentData);
                }
                Map<String, List<Map<String, Object>>> requestData = new HashMap<>();
                requestData.put("students", studentsList);
                String jsonData = gson.toJson(requestData);
                sendDataToFlaskServer(jsonData);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Failed to read data from Firebase", databaseError.toException());
            }
        });
    }

    private void sendDataToFlaskServer(String jsonData) {
        Log.d(TAG, "Sending JSON data to server: " + jsonData);  // Log the JSON data

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(jsonData, JSON);
        Request request = new Request.Builder()
                .url("http://192.168.0.153:5000/predict")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Failed to connect to server", e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.e(TAG, "Server error: " + response);
                    return;
                }

                String responseData = response.body().string();
                Log.d(TAG, "Server response: " + responseData);

                // Parse the response
                final MyResponse parsedResponse = gson.fromJson(responseData, MyResponse.class);

                // Update UI
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        displayResults(parsedResponse);
                    }
                });
            }
        });
    }

    private void displayResults(MyResponse response) {
        resultContainer.removeAllViews();

        // Check if the response or students list is null
        if (response == null || response.students == null) {
            Log.e(TAG, "Response or students list is null");
            return;
        }

        for (MyResponse.Student student : response.students) {
            // Check if student data is null
            if (student == null || !student.at_risk) {
                continue;
            }

            StringBuilder studentInfo = new StringBuilder();
            studentInfo.append("Student ID: ").append(student.student_id).append("\n");
//            studentInfo.append("Prediction: ").append(student.at_risk ? "Student is at risk" : "Student is not at risk").append("\n");
            studentInfo.append("Average Assignment Marks: ").append(student.avg_assignment_marks).append("\n");
            studentInfo.append("Average Quiz Marks: ").append(student.avg_quiz_marks).append("\n");

            studentInfo.append("Assignments:\n");
            if (student.assignments != null) {
                for (Map.Entry<String, MyResponse.Assignment> entry : student.assignments.entrySet()) {
                    studentInfo.append(entry.getKey()).append(": ")
                            .append("Subject: ").append(entry.getValue().subject)
                            .append(", Marks: ").append(entry.getValue().marks).append("\n");
                }
            }

            studentInfo.append("Quizzes:\n");
            if (student.quizzes != null) {
                for (Map.Entry<String, MyResponse.Quiz> entry : student.quizzes.entrySet()) {
                    studentInfo.append(entry.getKey()).append(": ")
                            .append("Subject: ").append(entry.getValue().subject)
                            .append(", Marks: ").append(entry.getValue().marks).append("\n");
                }
            }

//            CardView cardView = new CardView(this);
//            cardView.setCardElevation(4);
//            cardView.setContentPadding(16, 16, 16, 16);
//            cardView.setUseCompatPadding(true);

            TextView studentView = new TextView(this);
            studentView.setText(studentInfo.toString());

//            cardView.addView(studentView);
            resultContainer.addView(studentView);
        }
    }

    private static class MyResponse {
        List<Student> students;

        private static class Student {
            String student_id;
            boolean at_risk;
            double avg_assignment_marks;
            double avg_quiz_marks;
            Map<String, Assignment> assignments;
            Map<String, Quiz> quizzes;
        }

        private static class Assignment {
            int marks;
            String subject;
        }

        private static class Quiz {
            int marks;
            String subject;
        }
    }
}