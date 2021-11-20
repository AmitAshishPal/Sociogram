package com.example.intenship;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class EmailLogin extends AppCompatActivity {

    Button firex;
    EditText lemail, lpass, lname;

    //Firebase work
    FirebaseAuth mAuth2;
    DatabaseReference databaseReference;

    //For getting date,
    Calendar calendar;
    SimpleDateFormat simpledateformat;
    String Date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_login);

        firex = findViewById(R.id.firex);
        lemail = findViewById(R.id.lemail);
        lpass = findViewById(R.id.lpass);
        lname = findViewById(R.id.lname);


        mAuth2 = FirebaseAuth.getInstance();

        calendar = Calendar.getInstance();
        simpledateformat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        Date = simpledateformat.format(calendar.getTime());

        firex.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(lname.getText().toString().isEmpty()){
                    lname.setError("Name can't be empty!");
                }
                else if(lemail.getText().toString().isEmpty()){
                    lemail.setError("Email can't be empty!");
                }
                else if(lpass.getText().toString().isEmpty()){
                    lpass.setError("Password can't be empty!");
                }
                else{
                    String email = lemail.getText().toString();
                    String password = lpass.getText().toString();
                    mAuth2.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(EmailLogin.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()){
                                        Toast.makeText(EmailLogin.this, "Login successful!", Toast.LENGTH_SHORT).show();

                                        databaseReference = FirebaseDatabase.getInstance().getReference("Login").child("EmailLogin").child(lname.getText().toString());
                                        HashMap<String, Object>map = new HashMap<>();
                                        map.put("name", lname.getText().toString());
                                        map.put("email", email);
                                        map.put("login_time", Date);
                                        databaseReference.setValue(map);

                                        Intent intent = new Intent(EmailLogin.this, DashActivity.class);
                                        intent.putExtra("name", lname.getText().toString());
                                        Toast.makeText(EmailLogin.this, "Name is: "+lname.getText().toString(), Toast.LENGTH_SHORT).show();
                                        startActivity(intent);


                                    }
                                    else{

                                        Toast.makeText(EmailLogin.this, "Please Signin first", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(EmailLogin.this, Emailsignin.class));
                                        overridePendingTransition(0,0);
                                    }
                                }
                            });
                }
            }
        });

    }
}