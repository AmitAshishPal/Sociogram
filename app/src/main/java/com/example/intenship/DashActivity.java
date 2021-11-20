package com.example.intenship;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FacebookAuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ViewHolder;

import de.hdodenhof.circleimageview.CircleImageView;

public class DashActivity extends AppCompatActivity {

    CircleImageView cprofile, android, robot, books;
    Button signout;
    TextView dashname;
    DatabaseReference databaseReference;
    String name;
    String snap;
    DataSnapshot snapshot;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dash);
        cprofile = findViewById(R.id.cprofile);
        android = findViewById(R.id.android);
        robot = findViewById(R.id.robot);
        books = findViewById(R.id.books);
        dashname = findViewById(R.id.dashname);
        signout = findViewById(R.id.signout);

        name = getIntent().getStringExtra("name");
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        name = getIntent().getStringExtra("name");
        Uri photoUri;
        if(user!=null){
            photoUri = user.getPhotoUrl();
            String dash = user.getDisplayName();
            dashname.setText(dash);
            Glide.with(DashActivity.this).load(photoUri).placeholder(R.drawable.android).error(R.drawable.android).into(cprofile);
            Toast.makeText(DashActivity.this, "Success", Toast.LENGTH_SHORT).show();
            dashname.setText(name);

            
        }
        else{
            if(name!=null) {
                databaseReference = FirebaseDatabase.getInstance().getReference("Signin").child("EmailSignin");

                databaseReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        dashname.setText(name);
                        snap = snapshot.child(name).child("photo").getValue().toString();
                        Glide.with(DashActivity.this).load(snap).placeholder(R.drawable.android).error(R.drawable.android).into(cprofile);
                        Toast.makeText(DashActivity.this, "Finally Done!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(DashActivity.this, "DBException" + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
            else{
                Toast.makeText(DashActivity.this, "Error", Toast.LENGTH_SHORT).show();
            }

        }



        android.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DashActivity.this, StoryActivity.class);
                intent.putExtra("photo", R.drawable.images);
                intent.putExtra("name", "Android");
                startActivity(intent);
                overridePendingTransition(0,0);
            }
        });

        robot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(DashActivity.this, StoryActivity.class);
                intent.putExtra("photo", R.drawable.thx);
                intent.putExtra("name", "Robot");
                startActivity(intent);
                overridePendingTransition(0,0);
            }
        });

        books.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(DashActivity.this, StoryActivity.class);
                intent.putExtra("photo", R.drawable.books);
                intent.putExtra("name", "Books");
                startActivity(intent);
                overridePendingTransition(0,0);
            }
        });
        signout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();

                SharedPreferences sharedPreferences = getSharedPreferences(LoginActivity.PREFS_NAME, 0);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("hasloggedIn", false);
                Intent intent = new Intent(DashActivity.this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                editor.apply();
                finish();

            }
        });

    }

}