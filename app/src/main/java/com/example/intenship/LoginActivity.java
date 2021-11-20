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
import com.facebook.login.Login;
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

//Facebook imports
import com.facebook.FacebookSdk;


public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 50;
    public static String PREFS_NAME = "MyPrefsFile";
    Button emaillogin2, googlelogin2;
    TextView redirect2;

    //Facebook work
    LoginButton loginButton;
    CallbackManager mCallbackManager;
    public static final int FACEBOOK_ID = 75;

    //Google work
    GoogleSignInClient mGoogleSignInClient;
    FirebaseAuth auth;
    DatabaseReference databaseReference;

    //For getting date,
    Calendar calendar;
    SimpleDateFormat simpledateformat;
    String Date;

    String copy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        emaillogin2 = findViewById(R.id.emaillogin2);
        googlelogin2 = findViewById(R.id.googlelogin2);
        loginButton = findViewById(R.id.login_button2);
        redirect2 = findViewById(R.id.redirect2);

        //Calendar work
        calendar = Calendar.getInstance();
        simpledateformat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        Date = simpledateformat.format(calendar.getTime());

        //Google login
        auth = FirebaseAuth.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mGoogleSignInClient.revokeAccess();

        //Facebook login
        FacebookSdk.sdkInitialize(getApplicationContext());
        mCallbackManager = CallbackManager.Factory.create();
        loginButton.setReadPermissions("email", "public_profile");
        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Toast.makeText(LoginActivity.this, "Login success!", Toast.LENGTH_SHORT).show();
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Toast.makeText(LoginActivity.this, "Login cancelled!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(@NonNull FacebookException e) {
                Toast.makeText(LoginActivity.this, "Error is: "+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });




        //Email Login
        emaillogin2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, EmailLogin.class));
                overridePendingTransition(0,0);
            }
        });

        googlelogin2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
                //SharedPreferences;
                FirebaseUser user = auth.getCurrentUser();
                if(user!=null) {
                    SharedPreferences sharedPreferences = getSharedPreferences(LoginActivity.PREFS_NAME, 0);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("hasloggedIn", true);
                    editor.apply();

                    startActivity(new Intent(LoginActivity.this, DashActivity.class));
                    finish();
                }
                else{
                    Toast.makeText(LoginActivity.this, "Login in progress!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //Redirecting to signin page
        redirect2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, SigninActivity.class));
                overridePendingTransition(0,0);
            }
        });

    }


    //Facebook login
    private void handleFacebookAccessToken(AccessToken accessToken) {
        Toast.makeText(LoginActivity.this, "HandleFacebookAccessToken:" + accessToken, Toast.LENGTH_SHORT).show();
        AuthCredential credential = FacebookAuthProvider.getCredential(accessToken.getToken());
        auth.signInWithCredential(credential)
                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "SignInWithCredential:success", Toast.LENGTH_SHORT).show();
                            FirebaseUser user = auth.getCurrentUser();
                            if(user!=null){
                                String personName = user.getDisplayName();

                                databaseReference = FirebaseDatabase.getInstance().getReference("Login").child("FacebookLogin").child(personName);
                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("name", personName);
                                hashMap.put("login_time", Date);
                                databaseReference.setValue(hashMap);
                                Intent intent = new Intent(LoginActivity.this, DashActivity.class);
                                intent.putExtra("name", personName);

                                startActivity(intent);

                            }


                        }else{
                            Toast.makeText(LoginActivity.this, "Authentication failed.",Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    //Google login
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == RC_SIGN_IN){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try{
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            }catch (ApiException e) {
                Toast.makeText(LoginActivity.this, "Login failed!"+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

        else if(requestCode == FACEBOOK_ID){
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            Toast.makeText(LoginActivity.this, "Login success", Toast.LENGTH_SHORT).show();
                            FirebaseUser user = auth.getCurrentUser();
                            GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(LoginActivity.this);
                            if (acct != null) {
                                String personName = acct.getDisplayName();
                                String personGivenName = acct.getGivenName();
                                String personFamilyName = acct.getFamilyName();
                                String personEmail = acct.getEmail();
                                String personId = acct.getId();
                                Uri personPhoto = acct.getPhotoUrl();

                                databaseReference = FirebaseDatabase.getInstance().getReference("Login").child("GoogleLogin").child(personName);
                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("name", personName);
                                hashMap.put("email", personEmail);
                                hashMap.put("login_time", Date);
                                databaseReference.setValue(hashMap);

                                Intent intent = new Intent(LoginActivity.this, DashActivity.class);
                                intent.putExtra("name", personName);
                                startActivity(intent);
                                Toast.makeText(LoginActivity.this, "Date is: "+Date, Toast.LENGTH_SHORT).show();
                            }

                        } else {
                            Toast.makeText(LoginActivity.this, "Exception occured!"+task.getException(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

}