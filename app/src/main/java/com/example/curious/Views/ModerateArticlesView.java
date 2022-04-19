package com.example.curious.Views;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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
import com.example.curious.Models.User;
import com.example.curious.R;
import com.example.curious.Util.NetworkReceiver;
import com.example.curious.Util.SQLiteHelper;
import com.example.curious.ViewModels.ArticleAdapter;
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

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

@RequiresApi(api = Build.VERSION_CODES.M)
public class ModerateArticlesView extends AppCompatActivity implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener, ArticleAdapter.OnArticleClickListener {

    ArrayList<Article> articles;
    ArrayList<Article> newArticles;

    /** Network Variables */
    private BroadcastReceiver networkReceiver = null;

    /** Firebase Variables */
    private static final int RC_SIGN_IN = 1;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth.AuthStateListener mAuthListener;
    GoogleSignInOptions googleSignInOptions;
    private Query query;
    private Integer numberOfDocumentsPerQuery=10;
    private boolean isModerator = true;

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
    ArticleAdapter articleAdapter;

    /** View Variables */
    Button articlesRefresh;
    LoadingButton articlesLoading;

    /** Active User Variable */
    public static com.example.curious.Models.User activeUser;

    /** Others */
    private boolean doubleBackToExitPressedOnce = false;
    private Integer scrollViewPos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moderate_articles_view);

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
    }

    void setUI(){
        findXmlElements();
        setToolbar();
        setListeners();
        initializeUI();
    }

    public void findXmlElements(){
        // Parent Layout
        drawerLayout = (DrawerLayout) findViewById(R.id.moderate_articles_drawer_layout);

        // Toolbar
        toolbar = (androidx.appcompat.widget.Toolbar) findViewById(R.id.moderate_articles_toolbar);
        userDrawerBtn = (Button) findViewById(R.id.user_drawer_btn);
        newArticleBtn = (Button) findViewById(R.id.new_article_btn);
        activityTitle = (TextView) findViewById(R.id.activity_title);

        // Navigation Drawer
        userNavigationView = (NavigationView) findViewById(R.id.user_navigation_view);
        profilePictureImageView = (ImageView) userNavigationView.getHeaderView(0).findViewById(R.id.user_profile_picture);
        profileEmailTextView = (TextView) userNavigationView.getHeaderView(0).findViewById(R.id.user_profile_email);

        // Recycler View
        articlesRecyclerView = (RecyclerView) findViewById(R.id.moderate_articles_recycler_view);

        // View
        articlesRefresh = findViewById(R.id.moderate_articles_refresh);
        articlesLoading = findViewById(R.id.moderate_articles_loading);
    }

    @SuppressLint("RestrictedApi")
    public void setToolbar(){
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        newArticleBtn.setVisibility(View.INVISIBLE);
        activityTitle.setText(R.string.txt_moderate_articles);
    }

    public void setListeners(){
        drawerLayout.setDrawerListener(drawerToggle);
        userDrawerBtn.setOnClickListener(this);
        userNavigationView.setNavigationItemSelectedListener(this);
        newArticleBtn.setOnClickListener(this);
        articlesRefresh.setOnClickListener(this);
        articlesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        articlesRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if(newState == RecyclerView.SCROLL_INDICATOR_BOTTOM) {
                    articlesRefresh.setVisibility(View.VISIBLE);
                }
            }

            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy < 0) {
                    articlesRefresh.setVisibility(View.GONE);
                }
            }
        });
    }

    public void initializeUI(){
        mAuth = FirebaseAuth.getInstance();
        networkReceiver = new NetworkReceiver();
        broadcastIntent();

        Picasso.get().load(activeUser.getPhoto()).into(profilePictureImageView);
        profileEmailTextView.setText(activeUser.getEmail());
        userNavigationView.getMenu().findItem(R.id.user_moderate_option).setVisible(isModerator);

        // Recycle View
        articlesRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        articleAdapter = new ArticleAdapter(this, articles, this);
        articlesRecyclerView.setAdapter(articleAdapter);
        articleAdapter.notifyDataSetChanged();
    }

    /** Load Articles */

    @Override
    protected void onStart() {
        super.onStart();
        loadArticles();
    }

    public void loadArticles() {
        newArticles = new ArrayList<>();

        FirebaseFirestore database = FirebaseFirestore.getInstance();
        CollectionReference articlesRef = database.collection("pendingArticles");
        query = articlesRef.orderBy("timestamp", Query.Direction.ASCENDING).limit(numberOfDocumentsPerQuery);

        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot documentSnapshots) {
                for(QueryDocumentSnapshot documentSnapshot : documentSnapshots){
                    Article article = documentSnapshot.toObject(Article.class);
                    Date date = documentSnapshot.getDate("timestamp");
                    article.setDate(DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT).format(date));
                    newArticles.add(article);
                }

                articles = newArticles;
                updateView();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                showToast("[ERROR - Firestore] " + e.getMessage());
            }
        });
    }

    public void updateView() {
        articlesLoading.setVisibility(View.GONE);
        articleAdapter.updateArticlesAdapter(articles);
        if(articles.size()==0) {
            showToast("No Pending Articles Found");
            articlesRefresh.setVisibility(View.VISIBLE);
        }
        else if(articles.size()<numberOfDocumentsPerQuery) {
            articlesRefresh.setVisibility(View.VISIBLE);
        }
    }

    /** View Article */

    public void moderateArticle(int position){
        Article article = articles.get(position);
        Intent intent = new Intent(ModerateArticlesView.this, ModerateArticleView.class);
        intent.putExtra("status", "moderate_article");
        sendAidToActivity(article.getAid(), intent);
        startActivity(intent);
    }

    public void sendAidToActivity(String aid, Intent intent){
        intent.putExtra("moderate_article_aid", aid);
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
        else if(view == articlesRefresh) {
            articlesRefresh.setVisibility(View.GONE);
            articlesLoading.setVisibility(View.VISIBLE);
            loadArticles();
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
        else if(id == R.id.user_moderate_option) {
            startActivity(getIntent());
        }
        else if (id == R.id.user_articles_option) {
            Intent intent = new Intent(getApplicationContext(), ArticlesView.class);
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

    @Override
    public void onArticleClick(View view, int position) {
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
                moderateArticle(position);
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