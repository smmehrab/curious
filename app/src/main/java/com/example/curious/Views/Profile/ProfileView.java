package com.example.curious.Views.Profile;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
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
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.curious.R;
import com.example.curious.Util.NetworkReceiver;
import com.example.curious.Util.SQLiteHelper;
import com.example.curious.Views.General.ArticlesView;
import com.example.curious.Views.Auth.AuthView;
import com.example.curious.Views.Moderate.ModerateArticlesView;
import com.example.curious.Views.General.SavedArticlesView;
import com.example.curious.Views.Others.SettingsView;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.kusu.loadingbutton.LoadingButton;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

public class ProfileView extends AppCompatActivity implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener {

    /** Network Variables */
    private BroadcastReceiver networkReceiver = null;

    /** Firebase Variables */
    private static final int RC_SIGN_IN = 1;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth.AuthStateListener mAuthListener;
    GoogleSignInOptions googleSignInOptions;
    private Query query;
    private boolean isModerator = false;

    /** Navigation Drawer Variables */
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private NavigationView userNavigationView;
    private ImageView profilePictureImageView;
    private TextView profileEmailTextView;

    /** Toolbar Variables */
    private androidx.appcompat.widget.Toolbar toolbar;
    private Button userDrawerBtn;
    private Button newArticleBtn;
    private TextView activityTitle;

    /** View Variables */
    CircularImageView userPhoto;
    TextView userName;
    TextView userEmail;
    LinearLayout publishedArticlesClickable;
    LoadingButton publishedArticlesLoading;
    LinearLayout postedArticlesClickable;
    LoadingButton postedArticlesLoading;

    /** Active User Variable */
    public static com.example.curious.Models.User activeUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_view);

        if(!isConnectedToInternet()) {
            showToast("No Internet Connection");
        }

        getActiveUser();
        setUI();
    }

    public void getActiveUser() {
        SQLiteHelper sqLiteDatabaseHelper = new SQLiteHelper(this);
        SQLiteDatabase sqLiteDatabase = sqLiteDatabaseHelper.getReadableDatabase();
        activeUser = sqLiteDatabaseHelper.getUser();

        getActiveUserRole();
    }

    public void getActiveUserRole() {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference moderatorRef = database.collection("moderators").document(activeUser.getUid());

        moderatorRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    isModerator = task.getResult().exists();
                }
                else {
                    showToast("[ERROR - Firestore] Couldn't Read User Role");
                }
                updateViewBasedOnRole();
            }
        });
    }

    public void updateViewBasedOnRole() {
        userNavigationView.getMenu().findItem(R.id.user_moderate_option).setVisible(isModerator);
    }

    void setUI(){
        findXmlElements();
        setToolbar();
        setListeners();
        initializeUI();
    }

    public void findXmlElements(){
        // Parent Layout
        drawerLayout = (DrawerLayout) findViewById(R.id.profile_drawer_layout);

        // Toolbar
        toolbar = (androidx.appcompat.widget.Toolbar) findViewById(R.id.profile_toolbar);
        userDrawerBtn = (Button) findViewById(R.id.user_drawer_btn);
        newArticleBtn = (Button) findViewById(R.id.new_article_btn);
        activityTitle = (TextView) findViewById(R.id.activity_title);

        // Navigation Drawer
        userNavigationView = (NavigationView) findViewById(R.id.user_navigation_view);
        profilePictureImageView = (ImageView) userNavigationView.getHeaderView(0).findViewById(R.id.user_profile_picture);
        profileEmailTextView = (TextView) userNavigationView.getHeaderView(0).findViewById(R.id.user_profile_email);

        // View
        userPhoto = findViewById(R.id.profile_user_photo);
        userName = findViewById(R.id.profile_user_name);
        userEmail = findViewById(R.id.profile_user_email);
        publishedArticlesClickable = findViewById(R.id.profile_published_articles_clickable);
        publishedArticlesLoading = findViewById(R.id.profile_published_articles_loading);
        postedArticlesClickable = findViewById(R.id.profile_posted_articles_clickable);
        postedArticlesLoading = findViewById(R.id.profile_posted_articles_loading);
    }

    public void setToolbar(){
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        newArticleBtn.setVisibility(View.INVISIBLE);
        activityTitle.setText(R.string.txt_profile);
    }

    public void setListeners(){
        drawerLayout.setDrawerListener(drawerToggle);
        userDrawerBtn.setOnClickListener(this);
        userNavigationView.setNavigationItemSelectedListener(this);
        publishedArticlesClickable.setOnClickListener(this);
        postedArticlesClickable.setOnClickListener(this);
    }

    public void initializeUI(){
        mAuth = FirebaseAuth.getInstance();
        networkReceiver = new NetworkReceiver();
        broadcastIntent();

        userNavigationView.getMenu().getItem(0).setChecked(true);
        Picasso.get().load(activeUser.getPhoto()).into(profilePictureImageView);
        profileEmailTextView.setText(activeUser.getName());
        userNavigationView.getMenu().findItem(R.id.user_moderate_option).setVisible(isModerator);

        Picasso.get().load(activeUser.getPhoto()).into(userPhoto);
        userName.setText(activeUser.getName());
        userEmail.setText(activeUser.getEmail());
    }

    /** Listeners */

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
        else if(view == publishedArticlesClickable) {
            publishedArticlesLoading.setVisibility(View.VISIBLE);
            new CountDownTimer(100, 20) {
                int i;
                @Override
                public void onTick(long l) {
                    if (i % 2 == 0) {
                        publishedArticlesClickable.setVisibility(View.INVISIBLE);
                    } else {
                        publishedArticlesClickable.setVisibility(View.VISIBLE);
                    }
                    i++;
                }

                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void onFinish() {
                    publishedArticlesClickable.setVisibility(View.VISIBLE);

                    Intent intent = new Intent(getApplicationContext(), PublishedArticlesView.class);
                    startActivity(intent);
                }
            }.start();
        }
        else if(view == postedArticlesClickable) {
            postedArticlesLoading.setVisibility(View.VISIBLE);
            new CountDownTimer(100, 20) {
                int i;
                @Override
                public void onTick(long l) {
                    if (i % 2 == 0) {
                        postedArticlesClickable.setVisibility(View.INVISIBLE);
                    } else {
                        postedArticlesClickable.setVisibility(View.VISIBLE);
                    }
                    i++;
                }

                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void onFinish() {
                    postedArticlesClickable.setVisibility(View.VISIBLE);

                    Intent intent = new Intent(getApplicationContext(), PostedArticlesView.class);
                    startActivity(intent);
                }
            }.start();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.user_profile_option) {
            startActivity(getIntent());
        }
        else if(id == R.id.user_moderate_option) {
            Intent intent = new Intent(getApplicationContext(), ModerateArticlesView.class);
            startActivity(intent);
        }
        else if (id == R.id.user_articles_option) {
            onBackPressed();
        }
        else if (id == R.id.user_saved_option) {
            Intent intent = new Intent(getApplicationContext(), SavedArticlesView.class);
            startActivity(intent);
        }

        else if (id == R.id.user_settings_option) {
             Intent intent = new Intent(getApplicationContext(), SettingsView.class);
             startActivity(intent);
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
                        SQLiteHelper sqLiteDatabaseHelper = new SQLiteHelper(getApplicationContext());
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
                .requestIdToken("907047041355-ebs8gk59fam1crnkq3ob4ddet6fua76o.apps.googleusercontent.com")
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);
    }


    /** Others */

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), ArticlesView.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    public void showToast(String message){
        Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER | Gravity.BOTTOM, 0, 150);
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