package com.example.adamastour2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private EditText email_login, passsword_login;
    private TextView signupRedirectText;
    private Button login_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = auth.getCurrentUser();
        if(currentUser != null){
            String email = currentUser.getEmail();
            String sanitizedEmail = email.replaceAll("[.#$\\[\\]]", "");
            Log.d("firebase", sanitizedEmail);

            updateGamificationPoints(sanitizedEmail);
            startActivity(new Intent(LoginActivity.this, MapActivity.class));
        }
        else {
            email_login = findViewById(R.id.email_login);
            passsword_login = findViewById(R.id.passsword_login);
            login_button = findViewById(R.id.login_button);
            signupRedirectText = findViewById(R.id.signupRedirectText);

            login_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String email = email_login.getText().toString().trim();
                    String pwd = passsword_login.getText().toString().trim();
                    String sanitizedEmail = email.replaceAll("[.#$\\[\\]]", "");

                    if (!email.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        if (!pwd.isEmpty()) {
                            auth.signInWithEmailAndPassword(email, pwd).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                @Override
                                public void onSuccess(AuthResult authResult) {
                                    Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                                    updateGamificationPoints(sanitizedEmail);
                                    startActivity(new Intent(LoginActivity.this, MapActivity.class));
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(LoginActivity.this, "Login Failed", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            passsword_login.setError("Password cannot be empty");
                        }
                    } else if (email.isEmpty()) {
                        email_login.setError("Email cannot be empty");
                    } else {
                        email_login.setError("Enter valid email, please.");
                    }
                }
            });

            signupRedirectText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
                }
            });
        }

    }

    public void updateGamificationPoints(String sanitizedEmail) {
        FirebaseDatabase.getInstance().getReference("gamification_points").child(sanitizedEmail).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                }
                else {
                    DataSnapshot dataSnapshot = task.getResult();
                    if (dataSnapshot.exists()) {
                        String key = FirebaseDatabase.getInstance().getReference("gamification_points").child(sanitizedEmail).push().getKey();
                        DataSnapshot childSnapshot = dataSnapshot.child("points");
                        if (childSnapshot.exists()) {
                            Map<String, Object> postValues = new HashMap<>();
                            // add 10 points each time the app is open
                            postValues.put("points", childSnapshot.getValue(Integer.class)+10);
                            Map<String, Object> childUpdates = new HashMap<>();
                            childUpdates.put("/gamification_points/" + sanitizedEmail, postValues);
                            FirebaseDatabase.getInstance().getReference().updateChildren((childUpdates));
                            //  Log.d("firebase", "Value: " + value);
                            Log.d("firebase", "Hello, updated user points");

                        } else {
                            Log.d("firebase", "Child does not exist");
                        }
                        // Data for the given user ID exists
                        // You can access the data using dataSnapshot.getValue() or iterate through its children
                        Log.d("firebase", String.valueOf(dataSnapshot.getValue()));
                    } else {
                        Log.d("firebase", "Error updating user points");
                    }

                }
            }
        });
    }


    }