package com.example.adamastour2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private EditText email_signup;
    private EditText password_signup;
    private Button signup_button;
    private TextView loginRedirectText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        auth = FirebaseAuth.getInstance();
        email_signup = findViewById((R.id.email_signup));
        password_signup = findViewById(R.id.passsword_signup);
        signup_button = findViewById(R.id.signup_button);
        loginRedirectText = findViewById(R.id.loginRedirectText);

        signup_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                String user = email_signup.getText().toString().trim();
                String pwd = password_signup.getText().toString().trim();

                if(user.isEmpty()){
                    email_signup.setError("Email cannot be empty");
                }
                if(pwd.isEmpty()){
                    password_signup.setError("Password cannot be empty");
                }
                else {
                    auth.createUserWithEmailAndPassword(user, pwd).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                //Toast.makeText(SignUpActivity.this, "Sign Up Successful", Toast.LENGTH_SHORT).show();
                                Map<String, Object> userMap = new HashMap<>();
                                String sanitizedUser = user.replaceAll("[.#$\\[\\]]", "");
                                userMap.put("points", 0);
                               // FirebaseDatabase.getInstance().getReference("gamification_points").child(String.valueOf(email_signup)).setValue(userMap);

                                FirebaseDatabase.getInstance().getReference("gamification_points").child(sanitizedUser)
                                        .setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task2) {
                                                if (task2.isSuccessful()) {
                                                    Toast.makeText(SignUpActivity.this, "Created User with 0 points", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(SignUpActivity.this, e.getMessage().toString(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
                            }
                            else {
                                Toast.makeText(SignUpActivity.this, "Sign Up Failed" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

        loginRedirectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
            }
        });


    }
}