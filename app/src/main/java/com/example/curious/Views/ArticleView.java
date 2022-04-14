package com.example.curious.Views;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.curious.Models.Article;
import com.example.curious.R;
import com.example.curious.Util.NetworkReceiver;
import com.example.curious.Util.RecyclerTouchListener;
import com.example.curious.Util.SQLiteDatabaseHelper;
import com.example.curious.ViewModels.ArticleAdapter;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ArticleView extends AppCompatActivity implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener{

    /** Article */
    Article article;
    String check, articleId, author, title, coverUrl, body, date;
    Integer likeCount, viewCount;
    String[] comments;

    /** Network Variables */
    private BroadcastReceiver networkReceiver = null;

    /** Firebase Variables */
    private static final int RC_SIGN_IN = 1;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth.AuthStateListener mAuthListener;
    GoogleSignInOptions googleSignInOptions;

    /** View Variables */
    ImageView articleCover;
    TextView articleDate;
    TextView articleTitle;
    TextView articleAuthor;
    TextView articleViews;
    TextView articleBody;
    LinearLayout articleLikeClickable;
    ImageView articleLikeImage;
    TextView articleLikeCount;
    LinearLayout articleCommentClickable;
    ImageView articleCommentImage;
    TextView articleCommentCount;

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

    /** Active User Variable */
    public static com.example.curious.Models.User activeUser;

    /** Others */
    private boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_view);

        if(!isConnectedToInternet()) {
            showToast("No Internet Connection");
            onBackPressed();
        }
        else {
            getActiveUser();
            setUI();
        }
    }

    public void getActiveUser(){
        SQLiteDatabaseHelper sqLiteDatabaseHelper = new SQLiteDatabaseHelper(this);
        SQLiteDatabase sqLiteDatabase = sqLiteDatabaseHelper.getReadableDatabase();
        activeUser = sqLiteDatabaseHelper.getUser();
    }

    void setUI(){
        findXmlElements();
        setToolbar();
        setListeners();
        initializeArticle();
        initializeUI();
    }

    public void findXmlElements() {
        // Parent Layout
        drawerLayout = (DrawerLayout) findViewById(R.id.article_drawer_layout);

        // Toolbar
        toolbar = (androidx.appcompat.widget.Toolbar) findViewById(R.id.article_toolbar);
        userDrawerBtn = (Button) findViewById(R.id.user_drawer_btn);
        activityTitle = (TextView) findViewById(R.id.activity_title);
        newArticleBtn = (Button) findViewById(R.id.new_article_btn);
        newArticleBtn.setVisibility(View.INVISIBLE);

        // Navigation Drawer
        userNavigationView = (NavigationView) findViewById(R.id.user_navigation_view);
        profilePictureImageView = (ImageView) userNavigationView.getHeaderView(0).findViewById(R.id.user_profile_picture);
        profileEmailTextView = (TextView) userNavigationView.getHeaderView(0).findViewById(R.id.user_profile_email);

        // View Variables
        articleCover = findViewById(R.id.article_cover);
        articleDate = findViewById(R.id.article_date);
        articleTitle = findViewById(R.id.article_title);
        articleAuthor = findViewById(R.id.article_author);
        articleViews = findViewById(R.id.article_views);
        articleBody = findViewById(R.id.article_body);
        articleLikeClickable = findViewById(R.id.article_like_clickable);
        articleLikeImage = findViewById(R.id.article_like_image);
        articleLikeCount = findViewById(R.id.article_like_count);
        articleCommentClickable = findViewById(R.id.article_comment_clickable);
        articleCommentImage = findViewById(R.id.article_comment_image);
        articleCommentCount = findViewById(R.id.article_comment_count);
    }

    public void setToolbar(){
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        activityTitle.setText(R.string.txt_view_article);
    }

    public void setListeners(){
        drawerLayout.setDrawerListener(drawerToggle);
        userDrawerBtn.setOnClickListener(this);
        userNavigationView.setNavigationItemSelectedListener(this);
        newArticleBtn.setOnClickListener(this);
    }

    private void initializeArticle() {
        Intent intent = getIntent();
        check = intent.getStringExtra("status");
        if(check.equals("view_article")) {
            articleId = intent.getStringExtra("view_article_id");
            author = intent.getStringExtra("view_article_author");
            title = intent.getStringExtra("view_article_title");
            coverUrl = intent.getStringExtra("view_article_coverURL");
            body = intent.getStringExtra("view_article_body");
            date = intent.getStringExtra("view_article_date");
            likeCount = intent.getIntExtra("view_article_likeCount", 0);
            viewCount = intent.getIntExtra("view_article_viewCount", 0);
            comments = intent.getStringArrayExtra("view_article_comments");

            article = new Article(articleId, author, title, coverUrl, body, date, likeCount, viewCount, comments);

            // Set View
            Picasso.get().load(coverUrl).into(articleCover);
            articleDate.setText(date);
            articleTitle.setText(title);
            articleAuthor.setText(author);
            articleViews.setText(String.valueOf(viewCount) + " Views");
            articleBody.setText(body);
            articleLikeCount.setText(String.valueOf(likeCount));
            articleCommentCount.setText(String.valueOf(comments.length));
        }
        else {
            showToast("[ERROR] Couldn't Find Selected Article");
        }
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
        Intent intent = new Intent(ArticleView.this, ArticlesView.class);
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
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
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
        else if(view == newArticleBtn) {
            new CountDownTimer(100, 20) {
                int i;
                @Override
                public void onTick(long l) {
                    if (i % 2 == 0) {
                        newArticleBtn.setVisibility(View.INVISIBLE);
                    } else {
                        newArticleBtn.setVisibility(View.VISIBLE);
                    }
                    i++;
                }

                @Override
                public void onFinish() {
                    newArticleBtn.setVisibility(View.VISIBLE);
                    Snackbar.make(drawerLayout, "New Article View", Snackbar.LENGTH_SHORT).show();
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
            Intent intent = new Intent(ArticleView.this, ArticlesView.class);
            startActivity(intent);
            finish();
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
                .requestIdToken("907047041355-ebs8gk59fam1crnkq3ob4ddet6fua76o.apps.googleusercontent.com")
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