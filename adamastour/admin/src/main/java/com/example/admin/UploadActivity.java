package com.example.admin;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;

public class UploadActivity extends AppCompatActivity {

    Button saveButton;
    EditText uploadname, uploadcity, uploadLat, uploadLang;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        uploadname = findViewById(R.id.uploadName);
        uploadcity = findViewById(R.id.uploadCity);
        uploadLat = findViewById(R.id.uploadLat);
        uploadLang = findViewById(R.id.uploadLong);
        saveButton = findViewById(R.id.saveButton);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadData();
            }
        });


    }

       /* ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if(result.getResultCode() == Activity.RESULT_OK){
                            Intent data = result.getData();
                        }
                        else {
                            Toast.makeText(UploadActivity.this, "Error!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );*/



    public void uploadData(){
            String name = uploadname.getText().toString();
            String city = uploadcity.getText().toString();
            String lat = uploadLat.getText().toString();
            String lng = uploadLang.getText().toString();

            if (!name.isEmpty() && !city.isEmpty() && !lat.isEmpty() && !lng.isEmpty()) {

                DataClass dataClass = new DataClass(name, city, lat, lng);

                FirebaseDatabase.getInstance().getReference("Points of Interest").child(name)
                        .setValue(dataClass).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(UploadActivity.this, "Saved", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(UploadActivity.this, e.getMessage().toString(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
            else {
                Toast.makeText(UploadActivity.this, "Please Fill All Fields", Toast.LENGTH_SHORT).show();
            }
        }
}