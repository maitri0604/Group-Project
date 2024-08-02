package com.wlu.eduease;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private FirebaseAuth auth;
    private TextView textView;
    private FirebaseUser user;
    private DatabaseReference usersRef; // Reference to users node in Realtime Database
    private String userRole;
    private NavigationView navigationView;
    private DatabaseReference databaseReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        if (user == null) {
            Intent intent = new Intent(MainActivity.this, user_login.class);
            startActivity(intent);
            finish();
        } else {
            // Set user name in navigation drawer header
            navigationView = findViewById(R.id.nav_view);
            navigationView.setNavigationItemSelectedListener(this);
            textView = navigationView.getHeaderView(0).findViewById(R.id.user_details);

            // Initialize Firebase Database reference
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            usersRef = database.getReference("users").child(user.getUid());

            // Retrieve and set full name and user role from database
            usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    // Check if the user data exists in the database
                    if (dataSnapshot.exists()) {
                        String fullName = dataSnapshot.child("fullname").getValue(String.class);
                        userRole = dataSnapshot.child("role").getValue(String.class);
                        String welcomeMessage = getString(R.string.welcome_message, fullName);
                        textView.setText(welcomeMessage);

                        updateMenuVisibility(userRole);
                        loadDefaultFragment(savedInstanceState);
                    }
                }
                // Handle any errors
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(MainActivity.this, "Failed to load user data.", Toast.LENGTH_SHORT).show();
                }
            });
        }
        // Set up Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        // Set up ActionBarDrawerToggle for navigation drawer
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();



        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference();

        // Start migration
        startMigration();

    }

    // Load default fragment based on user role
    private void loadDefaultFragment(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            if ("student".equalsIgnoreCase(userRole)) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new StudentHome()).commit();
                navigationView.setCheckedItem(R.id.nav_settings);
            } else if ("parent".equalsIgnoreCase(userRole)) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ParentHome()).commit();
                navigationView.setCheckedItem(R.id.nav_home);
            } else if ("faculty".equalsIgnoreCase(userRole)) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new FacultyHome()).commit();
                navigationView.setCheckedItem(R.id.nav_home);
            }
        }
    }

    // Update menu visibility based on user role
    private void updateMenuVisibility(String role) {
        Menu menu = navigationView.getMenu();
        MenuItem studentHome = menu.findItem(R.id.nav_student_home);
        MenuItem facultyHome = menu.findItem(R.id.nav_faculty_home);
        MenuItem parentHome = menu.findItem(R.id.nav_parent_home);

        if ("student".equalsIgnoreCase(role)) {
            studentHome.setVisible(true);
            facultyHome.setVisible(false);
            parentHome.setVisible(false);
        } else if ("faculty".equalsIgnoreCase(role)) {
            studentHome.setVisible(false);
            facultyHome.setVisible(true);
            parentHome.setVisible(false);
        } else if ("parent".equalsIgnoreCase(role)) {
            studentHome.setVisible(false);
            facultyHome.setVisible(false);
            parentHome.setVisible(true);
        }
    }

    // Handle navigation item selection
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.nav_student_home) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new StudentHome()).commit();
        } else if (itemId == R.id.nav_faculty_home) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new FacultyHome()).commit();
        } else if (itemId == R.id.nav_parent_home) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ParentHome()).commit();
        } else if (itemId == R.id.nav_settings) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new SettingsFragment()).commit();
        } else if (itemId == R.id.nav_about) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new AboutFragment()).commit();
        } else if (itemId == R.id.nav_logout) {
            Toast.makeText(this, "Logout!", Toast.LENGTH_SHORT).show();
            // Perform logout actions here
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(MainActivity.this, user_login.class);
            startActivity(intent);
            finish();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    // save data in new tuple students ;

    private void startMigration() {
        migrateData();
    }

    private void migrateData() {
        DatabaseReference assignmentsRef = databaseReference.child("assignments");
        DatabaseReference quizzesRef = databaseReference.child("quizzes");

        // Read assignments data
        assignmentsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot assignmentSnapshot : dataSnapshot.getChildren()) {
                    String assignmentId = assignmentSnapshot.getKey();
                    String date = assignmentSnapshot.child("date").getValue(String.class);
                    String pdfUrl = assignmentSnapshot.child("pdfUrl").getValue(String.class);
                    String subject = assignmentSnapshot.child("subject").getValue(String.class);
                    String title = assignmentSnapshot.child("title").getValue(String.class);

                    DataSnapshot marksSnapshot = assignmentSnapshot.child("marks");
                    for (DataSnapshot studentSnapshot : marksSnapshot.getChildren()) {
                        String studentId = studentSnapshot.getKey();
                        int marks = studentSnapshot.getValue(Integer.class);
                        saveAssignment(studentId, assignmentId, date, marks, pdfUrl, subject, title);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle possible errors.
            }
        });

        // Read quizzes data
        quizzesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot quizSnapshot : dataSnapshot.getChildren()) {
                    String quizId = quizSnapshot.getKey();
                    String date = quizSnapshot.child("date").getValue(String.class);
                    String pdfUrl = quizSnapshot.child("pdfUrl").getValue(String.class);
                    String subject = quizSnapshot.child("subject").getValue(String.class);
                    String title = quizSnapshot.child("title").getValue(String.class);

                    DataSnapshot marksSnapshot = quizSnapshot.child("marks");
                    for (DataSnapshot studentSnapshot : marksSnapshot.getChildren()) {
                        String studentId = studentSnapshot.getKey();
                        int marks = studentSnapshot.getValue(Integer.class);
                        saveQuiz(studentId, quizId, date, marks, pdfUrl, subject, title);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle possible errors.
            }
        });
    }

    private void saveAssignment(String studentId, String assignmentId, String date, int marks, String pdfUrl, String subject, String title) {
        Map<String, Object> assignmentData = new HashMap<>();
        assignmentData.put("date", date);
        assignmentData.put("marks", marks);
        assignmentData.put("pdfUrl", pdfUrl);
        assignmentData.put("subject", subject);
        assignmentData.put("title", title);

        databaseReference.child("students").child(studentId).child("assignments").child(assignmentId).setValue(assignmentData);
    }

    private void saveQuiz(String studentId, String quizId, String date, int marks, String pdfUrl, String subject, String title) {
        Map<String, Object> quizData = new HashMap<>();
        quizData.put("date", date);
        quizData.put("marks", marks);
        quizData.put("pdfUrl", pdfUrl);
        quizData.put("subject", subject);
        quizData.put("title", title);

        databaseReference.child("students").child(studentId).child("quizzes").child(quizId).setValue(quizData);
    }
}
