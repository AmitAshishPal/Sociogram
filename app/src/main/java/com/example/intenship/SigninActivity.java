package com.example.intenship;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import com.facebook.FacebookSdk;


public class SigninActivity extends AppCompatActivity {

    Button email, google;
    GoogleSignInClient mGoogleSignInClient;
    int RC_SIGN_IN = 75;
    FirebaseAuth mAuth;
    DatabaseReference databaseReference;

    //For getting date,
    Calendar calendar;
    SimpleDateFormat simpledateformat;
    String Date;

    //Facebook signup
    CallbackManager mCallbackManager;
    LoginButton loginButton;
    public static final int FACEBOOK_ID_SIGN = 75;

    TextView redirect;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        redirect = findViewById(R.id.redirect);

        email = findViewById(R.id.emaillogin2);
        google = findViewById(R.id.googlelogin2);
        mAuth = FirebaseAuth.getInstance();

        calendar = Calendar.getInstance();
        simpledateformat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        Date = simpledateformat.format(calendar.getTime());

        //1) Email signin
        email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SigninActivity.this, Emailsignin.class);
                startActivity(intent);
                overridePendingTransition(0, 0);

            }
        });


        //2) Google signin
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mGoogleSignInClient.revokeAccess();
        google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });


        //3) Facebook signin
        FacebookSdk.sdkInitialize(getApplicationContext());
        mCallbackManager = CallbackManager.Factory.create();
        loginButton = findViewById(R.id.login_button2);
        loginButton.setReadPermissions("email", "public_profile");
        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Toast.makeText(SigninActivity.this, "Signup success!", Toast.LENGTH_SHORT).show();
                Toast.makeText(SigninActivity.this, "Please Login using same!", Toast.LENGTH_SHORT).show();
                handleFacebookAccessToken(loginResult.getAccessToken());

            }

            @Override
            public void onCancel() {
                Toast.makeText(SigninActivity.this, "Signup cancelled!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(@NonNull FacebookException e) {
                Toast.makeText(SigninActivity.this, "Error is: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        //4) Redirecting the user to Login Page
        redirect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SigninActivity.this, LoginActivity.class));
                overridePendingTransition(0, 0);
            }
        });


    }

    //Facebook signup
    private void handleFacebookAccessToken(AccessToken accessToken) {
        AuthCredential credential = FacebookAuthProvider.getCredential(accessToken.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(SigninActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(SigninActivity.this, "SignInWithCredential:success", Toast.LENGTH_SHORT).show();
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                String personName = user.getDisplayName();
                                String uri = user.getPhotoUrl().toString();
                                databaseReference = FirebaseDatabase.getInstance().getReference("Signin").child(personName);
                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("name", personName);
                                hashMap.put("photo", uri);
                                hashMap.put("login_time", Date);
                                databaseReference.setValue(hashMap);
                            }
                            Toast.makeText(SigninActivity.this, "Please Login using same!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(SigninActivity.this, LoginActivity.class));

                        } else {
                            Toast.makeText(SigninActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    //Google signup
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Toast.makeText(SigninActivity.this, "firebaseAuthWithGoogle:" + account.getId(), Toast.LENGTH_SHORT).show();
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Toast.makeText(SigninActivity.this, "Something went wrong!" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == FACEBOOK_ID_SIGN) {
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            Toast.makeText(SigninActivity.this, "Signin success!", Toast.LENGTH_SHORT).show();
                            Toast.makeText(SigninActivity.this, "Please Login using same!", Toast.LENGTH_SHORT).show();
                            FirebaseUser user = mAuth.getCurrentUser();

                            GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(SigninActivity.this);
                            if (acct != null) {
                                String personName = acct.getDisplayName();
                                String personGivenName = acct.getGivenName();
                                String personFamilyName = acct.getFamilyName();
                                String personEmail = acct.getEmail();
                                String personId = acct.getId();
                                Uri personPhoto = acct.getPhotoUrl();

                                databaseReference = FirebaseDatabase.getInstance().getReference("Signin").child(personName);
                                HashMap<String, Object> map = new HashMap<>();
                                map.put("email", personEmail);
                                map.put("name", personName);
                                map.put("login_time", Date);
                                map.put("photo", personPhoto.toString());
                                databaseReference.setValue(map);

                                Intent intent = new Intent(SigninActivity.this, LoginActivity.class);
                                startActivity(intent);

                            }


                        } else {
                            Toast.makeText(SigninActivity.this, "Something went wrong! Error is: " + task.getException(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


}