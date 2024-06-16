package com.wlu.eduease;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class register_teacher extends AppCompatActivity {

    // Firebase database reference
    DatabaseReference databaseReference;

    private Spinner spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_teacher);

        // Initialize Firebase database reference
        databaseReference = FirebaseDatabase.getInstance().getReference().child("teachers");

        final EditText fullname = findViewById(R.id.fullname);
        final EditText phone = findViewById(R.id.phone);
        final EditText email = findViewById(R.id.email);
        final EditText password = findViewById(R.id.password);
        final EditText conPassword = findViewById(R.id.conPassword);

        final Button btn_register = findViewById(R.id.btn_register);
        final TextView loginNow = findViewById(R.id.loginNow);



        spinner = findViewById(R.id.spinner);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.spinner_items,
                android.R.layout.simple_spinner_item
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = parent.getItemAtPosition(position).toString();
                Toast.makeText(register_teacher.this,"Selected:"+selectedItem,Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Toast.makeText(register_teacher.this,"Please Select An Item!",Toast.LENGTH_SHORT).show();
            }
        });

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String fullnameTxt = fullname.getText().toString();
                final String phoneTxt = phone.getText().toString();
                final String emailTxt = email.getText().toString();
                final String passwordTxt = password.getText().toString();
                final String conPasswordTxt = conPassword.getText().toString();
                final String selectedRole = spinner.getSelectedItem().toString();

                if (fullnameTxt.isEmpty() || phoneTxt.isEmpty() || emailTxt.isEmpty() || passwordTxt.isEmpty()) {
                    Toast.makeText(register_teacher.this, "Please fill all the fields", Toast.LENGTH_SHORT).show();
                } else if (!passwordTxt.equals(conPasswordTxt)) {
                    Toast.makeText(register_teacher.this, "Password and Confirm Password do not match", Toast.LENGTH_SHORT).show();
                } else {
                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(emailTxt, passwordTxt)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                                        if (firebaseUser != null) {
                                            String userId = firebaseUser.getUid();

                                            // Save additional user data to Firebase Realtime Database
                                            DatabaseReference userRef = databaseReference.child(userId);
                                            userRef.child("fullname").setValue(fullnameTxt);
                                            userRef.child("email").setValue(emailTxt);
                                            userRef.child("phone").setValue(phoneTxt);
                                            userRef.child("role").setValue(selectedRole);
                                            userRef.child("password").setValue(passwordTxt);

                                            Toast.makeText(register_teacher.this, "Registration Successful", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(getApplicationContext(), user_login.class);
                                            startActivity(intent);
                                            finish(); // Finish the activity after successful registration
                                        } else {
                                            Toast.makeText(register_teacher.this, "Failed to authenticate user", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        Toast.makeText(register_teacher.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });

        loginNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), user_login.class);
                startActivity(intent);
                finish();
            }
        });

    }
}
