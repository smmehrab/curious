package com.example.curious.Views.Profile;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
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
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.curious.Models.ArticleItem;
import com.example.curious.R;
import com.example.curious.Util.NetworkReceiver;
import com.example.curious.Util.SQLiteHelper;
import com.example.curious.ViewModels.ArticleItemAdapter;
import com.example.curious.Views.General.ArticleView;
import com.example.curious.Views.Auth.AuthView;
import com.example.curious.Views.Moderate.ModerateArticlesView;
import com.example.curious.Views.General.SavedArticlesView;
import com.example.curious.Views.Others.AboutView;
import com.example.curious.Views.Others.SettingsView;
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

public class PostedArticlesView extends AppCompatActivity implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener, ArticleItemAdapter.OnArticleItemClickListener {

    private ArrayList<ArticleItem> articles;
    private ArrayList<ArticleItem> newArticles;

    /** Network Variables */
    private BroadcastReceiver networkReceiver = null;

    /** Firebase Variables */
    private static final int RC_SIGN_IN = 1;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth.AuthStateListener mAuthListener;
    GoogleSignInOptions googleSignInOptions;
    private Query query;
    private Query nextQuery;
    private Integer numberOfDocumentsPerQuery=10;
    private DocumentSnapshot lastArticle;
    private Integer pageCount = 0;
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

    /** RecyclerView Variables */
    RecyclerView articlesRecyclerView;
    ArticleItemAdapter articleAdapter;

    /** View Variables */
    Button articlesOlder;
    Button articlesLatest;
    LoadingButton articlesLoading;
    LinearLayout articlesButtons;

    /** Active User Variable */
    public static com.example.curious.Models.User activeUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posted_articles_view);

        if(!isConnectedToInternet()) {
            showToast("No Internet Connection");
        }
        articles = new ArrayList<>();
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
        drawerLayout = (DrawerLayout) findViewById(R.id.posted_articles_drawer_layout);

        // Toolbar
        toolbar = (androidx.appcompat.widget.Toolbar) findViewById(R.id.posted_articles_toolbar);
        userDrawerBtn = (Button) findViewById(R.id.user_drawer_btn);
        newArticleBtn = (Button) findViewById(R.id.new_article_btn);
        activityTitle = (TextView) findViewById(R.id.activity_title);

        // Navigation Drawer
        userNavigationView = (NavigationView) findViewById(R.id.user_navigation_view);
        profilePictureImageView = (ImageView) userNavigationView.getHeaderView(0).findViewById(R.id.user_profile_picture);
        profileEmailTextView = (TextView) userNavigationView.getHeaderView(0).findViewById(R.id.user_profile_email);

        // Recycler View
        articlesRecyclerView = (RecyclerView) findViewById(R.id.posted_articles_recycler_view);

        // View
        articlesButtons = findViewById(R.id.posted_articles_buttons_ll);
        articlesOlder = findViewById(R.id.posted_articles_older);
        articlesLatest = findViewById(R.id.posted_articles_latest);
        articlesLoading = findViewById(R.id.posted_articles_loading);
    }

    public void setToolbar(){
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        newArticleBtn.setVisibility(View.GONE);
        activityTitle.setText(R.string.txt_posted_articles);
    }

    public void setListeners(){
        drawerLayout.setDrawerListener(drawerToggle);
        userDrawerBtn.setOnClickListener(this);
        userNavigationView.setNavigationItemSelectedListener(this);
        newArticleBtn.setOnClickListener(this);
        articlesOlder.setOnClickListener(this);
        articlesLatest.setOnClickListener(this);
        articlesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        articlesRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if(newState == RecyclerView.SCROLL_INDICATOR_BOTTOM) {
                    articlesButtons.setVisibility(View.VISIBLE);
                }
            }

            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy < 0) {
                    articlesButtons.setVisibility(View.GONE);
                }
            }
        });
    }

    public void initializeUI(){
        mAuth = FirebaseAuth.getInstance();
        networkReceiver = new NetworkReceiver();
        broadcastIntent();

        Picasso.get().load(activeUser.getPhoto()).into(profilePictureImageView);
        profileEmailTextView.setText(activeUser.getName());

        articlesRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        articleAdapter = new ArticleItemAdapter(this, articles, this);
        articlesRecyclerView.setAdapter(articleAdapter);
        articleAdapter.notifyDataSetChanged();
    }

    /** Load Articles */

    @Override
    protected void onStart() {
        super.onStart();
        loadArticles("");
    }

    public void loadArticles(String mode) {
        newArticles = new ArrayList<>();

        FirebaseFirestore database = FirebaseFirestore.getInstance();
        CollectionReference publishedRef = database.collection("users").document(activeUser.getUid()).collection("posted");

        if(mode.isEmpty() || mode.equals("latest")) {
            query = publishedRef.orderBy("timestamp", Query.Direction.DESCENDING).limit(numberOfDocumentsPerQuery);
        }
        else {
            query = nextQuery;
        }

        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot documentSnapshots) {
                for(QueryDocumentSnapshot documentSnapshot : documentSnapshots){
                    ArticleItem article = documentSnapshot.toObject(ArticleItem.class);
                    Date date = documentSnapshot.getDate("timestamp");
                    article.setDate(DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT).format(date));
                    newArticles.add(article);
                }

                // Next Query
                if(documentSnapshots.size()==0) {
                    updateView("no_more");
                    return;
                }
                else {
                    lastArticle = documentSnapshots.getDocuments().get(documentSnapshots.size()-1);
                    nextQuery = publishedRef.orderBy("timestamp", Query.Direction.DESCENDING).startAfter(lastArticle).limit(numberOfDocumentsPerQuery);
                }

                articles = newArticles;
                updateView("changed");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                showToast("[ERROR - Firestore] " + e.getMessage());
                Log.d("[ERROR]",e.getMessage());
            }
        });
    }

    public void updateView(String mode) {
        articlesLoading.setVisibility(View.GONE);

        if(mode.equals("changed")) {
            articleAdapter.updateArticlesAdapter(articles);
        }
        else if(mode.equals("no_more")) {
            showToast("No Articles Found");
        }

        if(articles.size()<numberOfDocumentsPerQuery) {
            articlesButtons.setVisibility(View.VISIBLE);
            articlesOlder.setVisibility(View.GONE);
        }
    }

    /** View Article */

    public void viewArticle(int position){
        ArticleItem article = articles.get(position);
        String status = article.getStatus();

        if(status.equals("Published")) {
            Intent intent = new Intent(getApplicationContext(), ArticleView.class);
            intent.putExtra("status", "view_posted_article");
            sendAidToActivity(article.getAid(), intent);
            startActivity(intent);
        }
        else {
            showToast("You Can Only View Published Articles");
        }
    }

    public void sendAidToActivity(String aid, Intent intent){
        intent.putExtra("view_posted_article_aid", aid);
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
        else if(view == articlesOlder) {
            articlesButtons.setVisibility(View.GONE);
            articlesLoading.setVisibility(View.VISIBLE);
            pageCount++;
            loadArticles("older");
        }
        else if(view == articlesLatest) {
            articlesOlder.setVisibility(View.VISIBLE);
            articlesButtons.setVisibility(View.GONE);
            articlesLoading.setVisibility(View.VISIBLE);
            pageCount = 0;
            loadArticles("latest");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.user_profile_option) {
            Intent intent = new Intent(getApplicationContext(), ProfileView.class);
            startActivity(intent);
        }
        else if(id == R.id.user_moderate_option) {
            Intent intent = new Intent(getApplicationContext(), ModerateArticlesView.class);
            startActivity(intent);
        }
        else if (id == R.id.user_articles_option) {
            startActivity(getIntent());
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
            Intent intent = new Intent(getApplicationContext(), AboutView.class);
            startActivity(intent);
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

    @Override
    public void onArticleItemClick(View view, int position) {
        new CountDownTimer(100, 20) {
            int i;
            @Override
            public void onTick(long l) {
                if (i % 2 == 0) {
                    view.setVisibility(View.INVISIBLE);
                } else {
                    view.setVisibility(View.VISIBLE);
                }
                i++;
            }

            @Override
            public void onFinish() {
                view.setVisibility(View.VISIBLE);

                if(!isConnectedToInternet()) {
                    showToast("No Internet Connection");
                }
                else {
                    viewArticle(position);
                }
            }
        }.start();
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
        Intent intent = new Intent(getApplicationContext(), ProfileView.class);
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