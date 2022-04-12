package com.example.curious.Views;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.example.curious.R;
import com.example.curious.Util.NetworkReceiver;
import com.example.curious.Util.SQLiteDatabaseHelper;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

public class ArticlesView extends AppCompatActivity implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener{
    /** Network Variables */
    private BroadcastReceiver networkReceiver = null;

    /** Firebase Variables */
    private static final int RC_SIGN_IN = 1;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth.AuthStateListener mAuthListener;
    GoogleSignInOptions googleSignInOptions;

    /** Navigation Drawer Variables */
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;

    private NavigationView userNavigationView;

    private ImageView profilePictureImageView;
    private TextView profileEmailTextView;

    /** Toolbar Variables */
    private androidx.appcompat.widget.Toolbar toolbar;
    private Button userDrawerBtn;

    /** Active User Variable */
    public static com.example.curious.Models.User activeUser;

    /** Others */
    private boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_articles);

        if(!isConnectedToInternet()) {
            showToast("No Internet Connection");
        }

        activateUser();
        setUI();
    }

    public void activateUser(){
        SQLiteDatabaseHelper sqLiteDatabaseHelper = new SQLiteDatabaseHelper(this);
        SQLiteDatabase sqLiteDatabase = sqLiteDatabaseHelper.getReadableDatabase();
        activeUser = sqLiteDatabaseHelper.getUser();
    }

    void setUI(){
        findXmlElements();
        setToolbar();
        setListeners();
        initializeUI();
    }

    public void findXmlElements(){
        // Parent Layout
        drawerLayout = (DrawerLayout) findViewById(R.id.articles_drawer_layout);

        // Toolbar
        toolbar = (androidx.appcompat.widget.Toolbar) findViewById(R.id.articles_toolbar);
        userDrawerBtn = (Button) findViewById(R.id.user_drawer_btn);

        // Navigation Drawer
        userNavigationView = (NavigationView) findViewById(R.id.user_navigation_view);
        profilePictureImageView = (ImageView) userNavigationView.getHeaderView(0).findViewById(R.id.user_profile_picture);
        profileEmailTextView = (TextView) userNavigationView.getHeaderView(0).findViewById(R.id.user_profile_email);
    }

    public void setToolbar(){
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    public void setListeners(){
        drawerLayout.setDrawerListener(drawerToggle);
        userDrawerBtn.setOnClickListener(this);
        userNavigationView.setNavigationItemSelectedListener(this);
    }

    public void initializeUI(){
        mAuth = FirebaseAuth.getInstance();
        networkReceiver = new NetworkReceiver();
        broadcastIntent();

        Picasso.get().load(activeUser.getPhoto()).into(profilePictureImageView);
        profileEmailTextView.setText(activeUser.getEmail());

        SQLiteDatabaseHelper sqLiteDatabaseHelper = new SQLiteDatabaseHelper(this);
        SQLiteDatabase sqLiteDatabase = sqLiteDatabaseHelper.getReadableDatabase();
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
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    @Override
    public void onClick(View view) {
        if(view == userDrawerBtn) {
            new CountDownTimer(100, 20) {
                int i;

                @Override
                public void onTick(long l) {
                    if (i % 2 == 0) {
                        userDrawerBtn.setVisibility(View.INVISIBLE);
                    } else {
                        userDrawerBtn.setVisibility(View.VISIBLE);
                    }
                    i++;
                }

                @Override
                public void onFinish() {
                    userDrawerBtn.setVisibility(View.VISIBLE);
                    if (drawerLayout.isDrawerOpen(userNavigationView)) {
                        drawerLayout.closeDrawer(userNavigationView);
                    }
                    else if (!drawerLayout.isDrawerOpen(userNavigationView)) {
                        drawerLayout.openDrawer(userNavigationView);
                    }
                }
            }.start();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.user_profile_option) {
            Snackbar.make(drawerLayout, "Profile View", Snackbar.LENGTH_SHORT).show();
            // Intent intent = new Intent(getApplicationContext(), ProfileView.class);
            // startActivity(intent);
        }
        else if (id == R.id.user_articles_option) {
            Snackbar.make(drawerLayout, "Articles View", Snackbar.LENGTH_SHORT).show();
            // Intent intent = new Intent(getApplicationContext(), ArticlesView.class);
            // startActivity(intent);
        }
        else if (id == R.id.user_saved_option) {
            Snackbar.make(drawerLayout, "Saved Articles View", Snackbar.LENGTH_SHORT).show();
            // Intent intent = new Intent(getApplicationContext(), SavedArticlesView.class);
            // startActivity(intent);
        }

        else if (id == R.id.user_settings_option) {
            Snackbar.make(drawerLayout, "Settings View", Snackbar.LENGTH_SHORT).show();
            // Intent intent = new Intent(getApplicationContext(), SettingsView.class);
            // startActivity(intent);
        }
        else if (id == R.id.user_about_option) {
            Snackbar.make(drawerLayout, "About View", Snackbar.LENGTH_SHORT).show();
            // Intent intent = new Intent(getApplicationContext(), AboutView.class);
            // startActivity(intent);
        }
        else if (id == R.id.user_sign_out_option) {
            if(!isConnectedToInternet())
                Snackbar.make(drawerLayout, "Can't Sign Out Without Internet Access!", Snackbar.LENGTH_SHORT).show();
            else
                signOut();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    /** Authentication & Sign Out */
    public void signOut() {
        initializeGoogleVariable();
        mAuth.signOut();

        mGoogleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        SQLiteDatabaseHelper sqLiteDatabaseHelper = new SQLiteDatabaseHelper(getApplicationContext());
                        SQLiteDatabase sqLiteDatabase = sqLiteDatabaseHelper.getWritableDatabase();

                        sqLiteDatabaseHelper.refreshDatabase(sqLiteDatabase);

                        Intent intent = new Intent(getApplicationContext(), AuthView.class);
                        startActivity(intent);
                        finish();
                    }
                });
    }

    private void initializeGoogleVariable() {
        mAuth = FirebaseAuth.getInstance();

        // Configuring Google Sign In
        googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);
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