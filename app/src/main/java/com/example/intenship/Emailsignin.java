package com.example.intenship;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.UUID;

public class Emailsignin extends AppCompatActivity {

    EditText ename, eemail, epass;
    ImageView profile;
    Button fire;
    private final int PICK_IMAGE_REQUEST = 71;
    Uri filePath;
    DatabaseReference databaseReference;
    StorageReference firebaseStorage;
    String uid;
    FirebaseAuth mAuth;

    //For getting date,
    Calendar calendar;
    SimpleDateFormat simpledateformat;
    String Date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emailsignin);
        ename = findViewById(R.id.ename);
        epass = findViewById(R.id.lpass);
        eemail = findViewById(R.id.lemail);
        fire = findViewById(R.id.firex);
        profile = findViewById(R.id.profile);
        uid = UUID.randomUUID().toString();

        calendar = Calendar.getInstance();
        simpledateformat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        Date = simpledateformat.format(calendar.getTime());

        //Firebase work
        databaseReference = FirebaseDatabase.getInstance().getReference("Signin").child("EmailSignin").child(ename.getText().toString()+uid);
        firebaseStorage = FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
            }
        });

        fire.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ename.getText().toString().isEmpty()) {
                    ename.setError("Name can't be empty!");
                } else if (eemail.getText().toString().isEmpty()) {
                    eemail.setError("Email can't be empty!");
                } else if (epass.getText().toString().isEmpty()) {
                    epass.setError("Password can't be empty!");
                } else if (epass.getText().toString().length() < 6) {
                    epass.setError("Password must be atleast 6 digit long!");
                }
                else if(eemail.getText().toString()!=null && epass.toString()!=null && filePath!=null){
                    String email = eemail.getText().toString();
                    String pass = epass.getText().toString();
                    mAuth.createUserWithEmailAndPassword(email, pass)
                            .addOnCompleteListener(Emailsignin.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(Emailsignin.this, "Signin Success!", Toast.LENGTH_SHORT).show();
                                        StorageReference ref = firebaseStorage.child("images/" + ename.getText().toString());
                                        ref.putFile(filePath)
                                                .addOnSuccessListener(Emailsignin.this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                    @Override
                                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                        taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                            @Override
                                                            public void onSuccess(Uri uri) {
                                                                databaseReference = FirebaseDatabase.getInstance().getReference("Signin").child(ename.getText().toString());
                                                                HashMap<String, Object> map = new HashMap<>();
                                                                map.put("name", ename.getText().toString());
                                                                map.put("email", eemail.getText().toString());
                                                                map.put("photo", uri.toString());
                                                                map.put("login_time", Date);
                                                                databaseReference.setValue(map);
                                                                Intent intent = new Intent(Emailsignin.this, LoginActivity.class);
                                                                Toast.makeText(Emailsignin.this, "Please wait", Toast.LENGTH_SHORT).show();
                                                                startActivity(intent);
                                                                overridePendingTransition(0,0);
                                                                ename.setText("");
                                                                eemail.setText("");
                                                                epass.setText("");
                                                                profile.setImageResource(R.drawable.ic_baseline_person_24);

                                                            }
                                                        });
                                                    }
                                                })
                                                .addOnFailureListener(Emailsignin.this, new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(Emailsignin.this, "Error is: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                });

                                    }
                                    else{
                                        Toast.makeText(Emailsignin.this, "Error!", Toast.LENGTH_SHORT).show();
                                    }

                                }
                            });
                }
                else if (filePath == null) {
                    Toast.makeText(Emailsignin.this, "Please select an Image!", Toast.LENGTH_SHORT).show();
                }

            }

        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data!=null && data.getData() != null){
            filePath = data.getData();
            profile.setImageURI(filePath);
            profile.setBackground(ContextCompat.getDrawable(this, R.drawable.whiteback));
        }
        else{
            Toast.makeText(Emailsignin.this, "Please select an Image!", Toast.LENGTH_SHORT).show();
        }
    }
}