package com.example.curious.Views;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.curious.Models.Article;
import com.example.curious.Models.Comment;
import com.example.curious.Models.Like;
import com.example.curious.Models.User;
import com.example.curious.R;
import com.example.curious.Util.NetworkReceiver;
import com.example.curious.Util.SQLiteHelper;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.kusu.loadingbutton.LoadingButton;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

public class ArticleView extends AppCompatActivity implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener{

    /** Article */
    private Article article;
    private String check, aid, cid;
    private ArrayList<Comment> comments;
    private boolean likeClicked = false;
    private boolean commentClicked = false;

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

    LinearLayout articleCommentSection;
    TextView articleCommentUser;
    EditText articleCommentBody;
    LoadingButton articleCommentPost;
    RecyclerView articleComments;

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
        SQLiteHelper sqLiteDatabaseHelper = new SQLiteHelper(this);
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

        articleCommentSection = findViewById(R.id.article_comment_section);
        articleCommentUser = findViewById(R.id.article_comment_user);
        articleCommentBody = findViewById(R.id.article_comment_body);
        articleCommentPost = findViewById(R.id.article_comment_post);
        articleComments = findViewById(R.id.article_comments);
    }

    public void setToolbar(){
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        newArticleBtn.setVisibility(View.INVISIBLE);
        activityTitle.setText(R.string.txt_view_article);
    }

    public void setListeners(){
        drawerLayout.setDrawerListener(drawerToggle);
        userDrawerBtn.setOnClickListener(this);
        userNavigationView.setNavigationItemSelectedListener(this);
        articleLikeClickable.setOnClickListener(this);
        articleCommentClickable.setOnClickListener(this);
        articleCommentPost.setOnClickListener(this);

        articleCommentBody.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus && TextUtils.isEmpty(articleCommentBody.getText().toString())){
                    articleCommentBody.setText(getResources().getString(R.string.txt_share_your_comment));
                    articleCommentBody.setTextColor(getResources().getColor(R.color.secondary_text));
                }
                else if (hasFocus && articleCommentBody.getText().toString().equals(getResources().getString(R.string.txt_share_your_comment))){
                    articleCommentBody.setText("");
                    articleCommentBody.setTextColor(getResources().getColor(R.color.primary_text));
                }
            }
        });

    }

    private void initializeArticle() {
        Intent intent = getIntent();
        check = intent.getStringExtra("status");
        if(check.equals("view_article")) {
            aid = intent.getStringExtra("view_article_aid");
            loadArticle(aid);
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
    }

    /** Load Article */

    public void loadArticle(String aid) {
        // Initialize Firestore
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        CollectionReference articlesRef = database.collection("articles");
        Query query = articlesRef.whereEqualTo("aid", aid);

        // Query
        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
             @Override
             public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                 for(QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots){
                     article = documentSnapshot.toObject(Article.class);
                     Date date = documentSnapshot.getDate("timestamp");
                     article.setDate(DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT).format(date).toString());
                     setArticleView(article);
                 }
             }
         }).addOnFailureListener(new OnFailureListener() {
             @Override
             public void onFailure(@NonNull Exception e) {
                 showToast("[ERROR - Firestore] " + e.getMessage());
             }
         });
    }

    /** Set Article View */

    public void setArticleView(Article article) {
        Picasso.get().load(article.getCoverUrl()).into(articleCover);
        articleDate.setText(article.getDate());
        articleTitle.setText(article.getTitle());
        articleAuthor.setText(article.getUid());
        articleViews.setText(String.valueOf(article.getViewCount()) + " Views");
        articleBody.setText(article.getBody());
        articleLikeCount.setText(String.valueOf(article.getLikeCount()));
        articleCommentCount.setText(String.valueOf(article.getCommentCount()));
    }

    /** Like */

    public void postLike() {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference newCommentRef = database.collection("articles").document(aid).collection("likes").document();

        Like like = new Like(aid, mAuth.getUid());

        newCommentRef.set(like).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                showToast("Liked");
                likeClicked = true;
                articleLikeImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_like_active));
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                showToast("[ERROR - FIRESTORE] " + e.toString());
            }
        });
    }

    public void postUnlike() {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        Query query = database.collection("articles").document(aid).collection("likes").whereEqualTo("aid", aid).whereEqualTo("uid", mAuth.getUid());

        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for(DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                    documentSnapshot.getReference().delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            showToast("Unliked");
                            likeClicked = false;
                            articleLikeImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_like));
                        }
                    });
                }
            }
        });
    }

    /** Comment */

    public void postComment() {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference newCommentRef = database.collection("articles").document(aid).collection("comments").document();

        cid = newCommentRef.getId();
        Comment comment = new Comment(cid, aid, mAuth.getUid(), articleCommentBody.getText().toString());

        newCommentRef.set(comment).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                articleCommentPost.hideLoading();
                showToast("Comment Posted");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                showToast("[ERROR - FIRESTORE] " + e.toString());
            }
        });
    }


    /** Listeners */

    @SuppressLint("UseCompatLoadingForDrawables")
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
        else if(view == articleLikeClickable) {
            if(!likeClicked) {
                postLike();
            }
            else {
                postUnlike();
            }
        }
        else if(view == articleCommentClickable) {
            if(commentClicked) {
                articleCommentSection.setVisibility(View.GONE);
                commentClicked = false;
                articleCommentImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_comment));
            }
            else {
                articleCommentSection.setVisibility(View.VISIBLE);
                commentClicked = true;
                articleCommentImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_comment_active));
            }
        }
        else if(view == articleCommentPost) {
            articleCommentPost.showLoading();
            postComment();
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
            Intent intent = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                intent = new Intent(ArticleView.this, ArticlesView.class);
            }
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

    @Override
    public void onBackPressed() {
        Intent intent = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            intent = new Intent(ArticleView.this, ArticlesView.class);
        }
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