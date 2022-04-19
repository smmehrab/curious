package com.example.curious.Views;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.curious.Models.User;
import com.example.curious.R;
import com.example.curious.Util.SQLiteHelper;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kusu.loadingbutton.LoadingButton;

import java.util.Objects;

public class AuthView extends AppCompatActivity implements View.OnClickListener, Animation.AnimationListener {
    /** Network Variables */
    private BroadcastReceiver networkReceiver = null;

    /** Firebase Variables */
    private static final int RC_SIGN_IN = 1;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth.AuthStateListener mAuthListener;
    GoogleSignInOptions googleSignInOptions;

    /** Button */
    private LinearLayout signInWithGoogle;
    private LoadingButton signInWithGoogleLoading;
    private Animation anim;
    private ImageView logo;

    /** SQLite Variable */
    SQLiteHelper sqLiteDatabaseHelper;

    /** Others */
    private boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth_view);

        initializeGoogleVariable();
        initializeSQLiteVariable();
        setUI();
    }

    private void initializeGoogleVariable() {
        mAuth = FirebaseAuth.getInstance();
        checkIfAlreadySignIn();

        // Configuring Google Sign In
        googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("907047041355-ebs8gk59fam1crnkq3ob4ddet6fua76o.apps.googleusercontent.com")
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);
    }

    private void checkIfAlreadySignIn() {
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser() != null){
                    enterApp();
                }
            }
        };
    }

    public void enterApp(){
        Intent intent = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            intent = new Intent(AuthView.this, ArticlesView.class);
        }
        startActivity(intent);
    }


    /** Animation */
    @Override
    public void onAnimationStart(Animation animation) {

    }

    public void onAnimationEnd(Animation animation) {
        if (animation == anim) {
            signInWithGoogle.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }


    /** UI */

    void setUI(){
        findXmlElements();
        setListeners();
        broadcastIntent();
    }

    void findXmlElements() {
        signInWithGoogle = (LinearLayout) findViewById(R.id.sign_in_with_google);
        signInWithGoogleLoading = findViewById(R.id.sign_in_with_google_loading);
        logo = findViewById(R.id.auth_logo);

        anim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.zoom_in);

        anim.setAnimationListener(this);
        logo.startAnimation(anim);
    }

    void setListeners() {
        signInWithGoogle.setOnClickListener(this);
        signInWithGoogleLoading.setOnClickListener(this);
    }

    /** */
    @Override
    protected void onStart() {
        mAuth.addAuthStateListener(mAuthListener);
        super.onStart();
    }

    @Override
    public void onClick(View view) {
        if(view == signInWithGoogle) {
            signInWithGoogle.setVisibility(View.GONE);
            signInWithGoogleLoading.setVisibility(View.VISIBLE);
            if (!isConnectedToInternet()) {
                signInWithGoogle.setVisibility(View.VISIBLE);
                signInWithGoogleLoading.setVisibility(View.GONE);
                Snackbar.make(findViewById(R.id.sign_in_activity), "Can't Sign In Without Internet Access!", Snackbar.LENGTH_SHORT).show();
            }
            else {
                handleSignInWithGoogle();
            }
        }
    }

    void handleSignInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In Successful | Authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                signInWithGoogle.setVisibility(View.VISIBLE);
                signInWithGoogleLoading.setVisibility(View.GONE);
                showToast(e.toString());
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            String deviceId = findDeviceId();

                            retrieveDataFromFirebase();
                            addUserToLocalDatabase(acct, deviceId);
                            addUserToFirestore(acct, deviceId);
                        } else {
                            signInWithGoogle.setVisibility(View.VISIBLE);
                            signInWithGoogleLoading.setVisibility(View.GONE);
                            Snackbar.make(findViewById(R.id.sign_in_activity), "Authentication Failed! Try Again!", Snackbar.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void retrieveDataFromFirebase(){
        FirebaseUser user = mAuth.getCurrentUser();
    }

    public void addUserToFirestore(GoogleSignInAccount acct, String deviceId) {
        User user = new User(mAuth.getUid(), acct.getEmail(), acct.getDisplayName(), Objects.requireNonNull(acct.getPhotoUrl()).toString(), deviceId);

        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference newUserRef = database.collection("users").document(Objects.requireNonNull(mAuth.getUid()));

        newUserRef.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                signInWithGoogle.setVisibility(View.VISIBLE);
                signInWithGoogleLoading.setVisibility(View.GONE);
                enterApp();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                showToast("[ERROR - FIRESTORE] " + e.toString());
            }
        });
    }

    /** Local DB */

    void initializeSQLiteVariable(){
        sqLiteDatabaseHelper = new SQLiteHelper(this);
        SQLiteDatabase sqLiteDatabase = sqLiteDatabaseHelper.getWritableDatabase();
    }

    public void addUserToLocalDatabase(GoogleSignInAccount acct, String deviceId){
        // Creating New User
        User currentUser = new User(mAuth.getUid(), acct.getEmail(), acct.getDisplayName(), Objects.requireNonNull(acct.getPhotoUrl()).toString(), deviceId);
        // Inserting to the Local Database
        sqLiteDatabaseHelper.insertUser(currentUser);
    }

    public String findDeviceId(){
        String id = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        return id;
    }

    /** Others */

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.finish();
            moveTaskToBack(true);
            return;
        }
        this.doubleBackToExitPressedOnce = true;
        showToast("Press Once Again to EXIT");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    public void showToast(String message){
        Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER | Gravity.BOTTOM, 0, 30);
        toast.show();
    }

    /** For Checking Network Connection */
    public void broadcastIntent() {
        registerReceiver(networkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    public boolean isConnectedToInternet(){
        ConnectivityManager cm =
                (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }
}