package com.wlu.eduease;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
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
import java.util.List;

public class StudentMarksFragment extends Fragment {

    private RecyclerView recyclerView;
    private List<String[]> markItemList;
    private RecyclerView.Adapter adapter;
    private DatabaseReference quizzesRef;
    private DatabaseReference userRef;
    private String currentUserName;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_student_marks, container, false);
        recyclerView = view.findViewById(R.id.marksRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        markItemList = new ArrayList<>();
        adapter = new RecyclerView.Adapter<RecyclerView.ViewHolder>() {

            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_mark, parent, false);
                return new RecyclerView.ViewHolder(itemView) {};
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                String[] item = markItemList.get(position);
                TextView quizTitle = holder.itemView.findViewById(R.id.markQuizTitle);
                TextView studentName = holder.itemView.findViewById(R.id.markStudentName);
                TextView markValue = holder.itemView.findViewById(R.id.markValue);

                quizTitle.setText(item[0]);
                studentName.setText(item[1]);
                markValue.setText(item[2]);
            }

            @Override
            public int getItemCount() {
                return markItemList.size();
            }
        };

        recyclerView.setAdapter(adapter);

        quizzesRef = FirebaseDatabase.getInstance().getReference("quizzes");
        userRef = FirebaseDatabase.getInstance().getReference("users");

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            Log.d("StudentMarksFragment", "Current User ID: " + userId);

            userRef.child(userId).child("fullname").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    currentUserName = dataSnapshot.getValue(String.class);
                    if (currentUserName != null && !currentUserName.isEmpty()) {
                        Log.d("StudentMarksFragment", "Current User Name: " + currentUserName);
                        loadMarks();
                    } else {
                        Log.e("StudentMarksFragment", "Current user name is null or empty");
                        showNoDataMessage("User name not found.");
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e("StudentMarksFragment", "Failed to fetch user name: " + databaseError.getMessage());
                    showNoDataMessage("Failed to fetch user details.");
                }
            });
        } else {
            Log.e("StudentMarksFragment", "No user is logged in");
            showNoDataMessage("No user is logged in.");
        }

        return view;
    }

    private void loadMarks() {
        quizzesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                markItemList.clear();

                boolean dataFound = false;
                for (DataSnapshot quizSnapshot : dataSnapshot.getChildren()) {
                    String quizId = quizSnapshot.getKey();
                    String date = quizSnapshot.child("date").getValue(String.class);
                    String subject = quizSnapshot.child("subject").getValue(String.class);
                    String title = quizSnapshot.child("title").getValue(String.class);

                    Log.d("StudentMarksFragment", "Quiz ID: " + quizId);

                    DataSnapshot marksSnapshot = quizSnapshot.child("marks");
                    boolean foundMarksForQuiz = false;
                    for (DataSnapshot markSnapshot : marksSnapshot.getChildren()) {
                        String studentName = markSnapshot.getKey();
                        Integer mark = markSnapshot.getValue(Integer.class);

                        Log.d("StudentMarksFragment", "Student: " + studentName + ", Mark: " + mark);

                        if (studentName.equals(currentUserName)) {
                            markItemList.add(new String[] { title, studentName, String.valueOf(mark) });
                            foundMarksForQuiz = true;
                            dataFound = true;
                        }
                    }

                    if (foundMarksForQuiz) {
                        Log.d("StudentMarksFragment", "Marks List Updated for Quiz ID: " + quizId);
                    }
                }

                if (!dataFound) {
                    showNoDataMessage("No marks found for the current user.");
                } else {
                    adapter.notifyDataSetChanged();
                }

                Log.d("StudentMarksFragment", "Total Marks List Items: " + markItemList.size());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("StudentMarksFragment", "Failed to load quiz data: " + databaseError.getMessage());
                showNoDataMessage("Failed to load quiz data.");
            }
        });
    }

    private void showNoDataMessage(String message) {
        markItemList.clear();
        markItemList.add(new String[] { message, "", "" });
        adapter.notifyDataSetChanged();
    }
}
