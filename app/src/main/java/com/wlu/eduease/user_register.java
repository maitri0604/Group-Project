package com.wlu.eduease;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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

public class user_register extends AppCompatActivity {

    // Firebase database reference
    DatabaseReference databaseReference;

    private RadioGroup radioGroup;
    private RadioButton radioButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_register);

        // Initialize Firebase database reference
        databaseReference = FirebaseDatabase.getInstance().getReference().child("users");

        final EditText fullname = findViewById(R.id.fullname);
        final EditText phone = findViewById(R.id.phone);
        final EditText email = findViewById(R.id.email);
        final EditText password = findViewById(R.id.password);
        final EditText conPassword = findViewById(R.id.conPassword);

        final Button btn_register = findViewById(R.id.btn_register);
        final TextView loginNow = findViewById(R.id.loginNow);

        radioGroup = findViewById(R.id.radioGroup);

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String fullnameTxt = fullname.getText().toString();
                final String phoneTxt = phone.getText().toString();
                final String emailTxt = email.getText().toString();
                final String passwordTxt = password.getText().toString();
                final String conPasswordTxt = conPassword.getText().toString();
                final int selectedId = radioGroup.getCheckedRadioButtonId();
                radioButton = findViewById(selectedId);
                final String selectedRole = radioButton.getText().toString();

                if (fullnameTxt.isEmpty() || phoneTxt.isEmpty() || emailTxt.isEmpty() || passwordTxt.isEmpty()) {
                    Toast.makeText(user_register.this, "Please fill all the fields", Toast.LENGTH_SHORT).show();
                } else if (!passwordTxt.equals(conPasswordTxt)) {
                    Toast.makeText(user_register.this, "Password and Confirm Password do not match", Toast.LENGTH_SHORT).show();
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

                                            Toast.makeText(user_register.this, "Registration Successful", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(getApplicationContext(), user_login.class);
                                            startActivity(intent);
                                        } else {
                                            Toast.makeText(user_register.this, "Failed to authenticate user", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        Toast.makeText(user_register.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
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
