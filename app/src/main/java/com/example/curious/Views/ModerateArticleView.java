package com.example.curious.Views;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
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
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
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
import com.example.curious.Models.ArticleItem;
import com.example.curious.Models.Comment;
import com.example.curious.Models.Like;
import com.example.curious.R;
import com.example.curious.Util.NetworkReceiver;
import com.example.curious.Util.SQLiteHelper;
import com.example.curious.ViewModels.CommentAdapter;
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

public class ModerateArticleView extends AppCompatActivity implements View.OnClickListener {

    /** Article */
    private Article article;
    private ArticleItem articleItem;
    private String check, aid;
    private boolean verifyClicked = false;
    private boolean discardClicked = false;
    private boolean isModerator = true;

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
    TextView articleBody;

    LinearLayout articleVerifyClickable;
    ImageView articleVerifyImage;
    TextView articleVerifyText;

    LinearLayout articleDiscardClickable;
    ImageView articleDiscardImage;
    TextView articleDiscardText;

    LoadingButton articleLoading;

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
        setContentView(R.layout.activity_moderate_article_view);

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
        // Toolbar
        toolbar = (androidx.appcompat.widget.Toolbar) findViewById(R.id.moderate_article_toolbar);
        userDrawerBtn = (Button) findViewById(R.id.user_drawer_btn);
        activityTitle = (TextView) findViewById(R.id.activity_title);
        newArticleBtn = (Button) findViewById(R.id.new_article_btn);

        // View Variables
        articleCover = findViewById(R.id.moderate_article_cover);
        articleDate = findViewById(R.id.moderate_article_date);
        articleTitle = findViewById(R.id.moderate_article_title);
        articleAuthor = findViewById(R.id.moderate_article_author);
        articleBody = findViewById(R.id.moderate_article_body);

        articleVerifyClickable = findViewById(R.id.moderate_article_verify_clickable);
        articleVerifyImage = findViewById(R.id.moderate_article_verify_image);
        articleVerifyText = findViewById(R.id.moderate_article_verify_text);

        articleDiscardClickable = findViewById(R.id.moderate_article_discard_clickable);
        articleDiscardImage = findViewById(R.id.moderate_article_discard_image);
        articleDiscardText = findViewById(R.id.moderate_article_discard_text);

        articleLoading = findViewById(R.id.moderate_article_loading);
    }

    public void setToolbar(){
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        userDrawerBtn.setVisibility(View.INVISIBLE);
        newArticleBtn.setVisibility(View.INVISIBLE);
        activityTitle.setText(R.string.txt_moderate);
    }

    public void setListeners(){
        articleVerifyClickable.setOnClickListener(this);
        articleDiscardClickable.setOnClickListener(this);
    }

    private void initializeArticle() {
        Intent intent = getIntent();
        check = intent.getStringExtra("status");
        if(check.equals("moderate_article")) {
            aid = intent.getStringExtra("moderate_article_aid");
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
    }

    /** Set Article View */

    public void setArticleView(Article article) {
        Picasso.get().load(article.getCoverUrl()).into(articleCover);
        articleDate.setText(article.getDate());
        articleTitle.setText(article.getTitle());
        articleAuthor.setText(article.getUname());
        articleBody.setText(article.getBody());
    }

    public void resetButtons() {
        verifyClicked = false;
        discardClicked = false;
        articleVerifyImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_verify));
        articleDiscardImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_discard));
        articleLoading.setVisibility(View.GONE);
        articleVerifyClickable.setVisibility(View.VISIBLE);
        articleDiscardClickable.setVisibility(View.VISIBLE);
    }

    /** Load Article */

    public void loadArticle(String aid) {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        CollectionReference articlesRef = database.collection("pendingArticles");
        Query query = articlesRef.whereEqualTo("aid", aid);

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
                resetButtons();
            }
        });
    }

    /** Verify */

    public void verifyArticle(){
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference articleRef = database.collection("articles").document(aid);

        articleRef.set(article).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                discardArticle("verify");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                showToast("[ERROR - FIRESTORE] " + e.toString());
                resetButtons();
            }
        });
    }

    /** Discard */

    public void discardArticle(String mode){
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        Query query = database.collection("pendingArticles").whereEqualTo("aid", aid);

        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for(DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                    documentSnapshot.getReference().delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            updateArticleItems(mode);
                        }
                    });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                resetButtons();
            }
        });
    }

    /** Update Article Items */

    public void updateArticleItems(String mode) {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference userPostedArticleItemRef = database.collection("users").document(article.getUid()).collection("posted").document(aid);
        DocumentReference userPublishedArticleItemRef = database.collection("users").document(article.getUid()).collection("published").document(aid);

        String status = (mode.isEmpty()) ?  "Discarded" : "Published";
        articleItem = new ArticleItem(article.getAid(), article.getUid(), article.getTitle(), article.getCoverUrl(), article.getUname(), status);

        userPostedArticleItemRef.set(articleItem).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                if(mode.isEmpty()) {
                    showToast("Discarded");
                    onBackPressed();
                }
                else {
                    userPublishedArticleItemRef.set(articleItem).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            showToast("Verified & Published");
                            onBackPressed();
                        }
                    });
                }
            }
        });
    }

    /** Listeners */

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onClick(View view) {
        if(view == articleVerifyClickable) {
            if(!verifyClicked) {
                verifyClicked = true;
                articleVerifyImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_verify_active));
                articleVerifyClickable.setVisibility(View.GONE);
                articleLoading.setVisibility(View.VISIBLE);
                verifyArticle();
            }
        }
        else if(view == articleDiscardClickable) {
            if(!discardClicked) {
                discardClicked = true;
                articleDiscardImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_discard_active));
                articleDiscardClickable.setVisibility(View.GONE);
                articleLoading.setVisibility(View.VISIBLE);
                discardArticle("");
            }
        }
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
            intent = new Intent(ModerateArticleView.this, ModerateArticlesView.class);
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