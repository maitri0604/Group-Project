package com.wlu.eduease.faculty;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.wlu.eduease.R;

import java.util.Map;

public class StudentDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_detail);

        String studentJson = getIntent().getStringExtra("student");
        Gson gson = new Gson();
        MyResponse.Student student = gson.fromJson(studentJson, MyResponse.Student.class);

        TextView studentIdView = findViewById(R.id.studentId);
        TextView avgAssignmentMarksView = findViewById(R.id.avgAssignmentMarks);
        TextView avgQuizMarksView = findViewById(R.id.avgQuizMarks);
        TextView assignmentsView = findViewById(R.id.assignments);
        TextView quizzesView = findViewById(R.id.quizzes);

        studentIdView.setText("Student ID: " + student.student_id);
        avgAssignmentMarksView.setText("Average Assignment Marks: " + student.avg_assignment_marks);
        avgQuizMarksView.setText("Average Quiz Marks: " + student.avg_quiz_marks);

        StringBuilder assignmentsInfo = new StringBuilder("Assignments:\n");
        if (student.assignments != null) {
            for (Map.Entry<String, MyResponse.Assignment> entry : student.assignments.entrySet()) {
                assignmentsInfo.append(entry.getKey()).append(": Subject: ")
                        .append(entry.getValue().subject).append(", Marks: ").append(entry.getValue().marks).append("\n");
            }
        }
        assignmentsView.setText(assignmentsInfo.toString());

        StringBuilder quizzesInfo = new StringBuilder("Quizzes:\n");
        if (student.quizzes != null) {
            for (Map.Entry<String, MyResponse.Quiz> entry : student.quizzes.entrySet()) {
                quizzesInfo.append(entry.getKey()).append(": Subject: ")
                        .append(entry.getValue().subject).append(", Marks: ").append(entry.getValue().marks).append("\n");
            }
        }
        quizzesView.setText(quizzesInfo.toString());
    }

    private static class MyResponse {
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
