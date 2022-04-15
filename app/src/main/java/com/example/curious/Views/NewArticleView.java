package com.example.curious.Views;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.curious.Models.Article;
import com.example.curious.Models.Comment;
import com.example.curious.R;
import com.example.curious.Util.NetworkReceiver;
import com.example.curious.Util.SQLiteHelper;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.List;

public class NewArticleView extends AppCompatActivity implements View.OnClickListener{

    /** Article */
    Article article;
    String aid, uid, title, body, coverUrl, date;
    Integer likeCount, viewCount;
    List<Comment> comments;
    Uri coverUri;

    /** Network Variables */
    private BroadcastReceiver networkReceiver = null;

    /** Firebase Variables */
    private static final int RC_SIGN_IN = 1;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth.AuthStateListener mAuthListener;
    GoogleSignInOptions googleSignInOptions;

    /** View Variables */
    LinearLayout newArticleUploadCoverClickableLL;
    LinearLayout newArticleUploadCoverLL;
    LinearLayout newArticleCoverLL;
    ImageView newArticleCover;
    EditText newArticleTitle;
    EditText newArticleBody;
    Button newArticleCancel;
    Button newArticlePost;

    /** Toolbar Variables */
    private androidx.appcompat.widget.Toolbar toolbar;
    private Button userDrawerBtn;
    private Button newArticleBtn;
    private TextView activityTitle;

    /** Active User Variable */
    public static com.example.curious.Models.User activeUser;

    /** Request for Permission Variable */
    static int PReqCode = 1;
    static int ReqCode = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_article_view);

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
        initializeUI();
    }

    public void findXmlElements() {
        // Toolbar
        toolbar = (androidx.appcompat.widget.Toolbar) findViewById(R.id.new_article_toolbar);
        userDrawerBtn = (Button) findViewById(R.id.user_drawer_btn);
        activityTitle = (TextView) findViewById(R.id.activity_title);
        newArticleBtn = (Button) findViewById(R.id.new_article_btn);

        // View Variables
        newArticleUploadCoverClickableLL = findViewById(R.id.new_article_upload_cover_clickable_ll);
        newArticleUploadCoverLL = findViewById(R.id.new_article_upload_cover_ll);
        newArticleCoverLL = findViewById(R.id.new_article_cover_ll);
        newArticleCover = findViewById(R.id.new_article_cover);
        newArticleTitle = findViewById(R.id.new_article_title);
        newArticleBody = findViewById(R.id.new_article_body);
        newArticleCancel = findViewById(R.id.new_article_cancel_btn);
        newArticlePost = findViewById(R.id.new_article_post_btn);
    }

    public void setToolbar(){
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        activityTitle.setText(R.string.txt_new_article);
        newArticleBtn.setVisibility(View.INVISIBLE);
        userDrawerBtn.setVisibility(View.INVISIBLE);
    }

    public void setListeners(){
        newArticleUploadCoverClickableLL.setOnClickListener(this);
        newArticleCancel.setOnClickListener(this);
        newArticlePost.setOnClickListener(this);

        newArticleTitle.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus && TextUtils.isEmpty(newArticleTitle.getText().toString())){
                    newArticleTitle.setText(getResources().getString(R.string.txt_give_a_catchy_title));
                    newArticleTitle.setTextColor(getResources().getColor(R.color.secondary_text));
                }
                else if (hasFocus && newArticleTitle.getText().toString().equals(getResources().getString(R.string.txt_give_a_catchy_title))){
                    newArticleTitle.setText("");
                    newArticleTitle.setTextColor(getResources().getColor(R.color.primary_text));
                }
            }
        });

        newArticleBody.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus && TextUtils.isEmpty(newArticleBody.getText().toString())){
                    newArticleBody.setText(getResources().getString(R.string.txt_write_your_article));
                    newArticleBody.setTextColor(getResources().getColor(R.color.secondary_text));
                }
                else if (hasFocus && newArticleBody.getText().toString().equals(getResources().getString(R.string.txt_write_your_article))){
                    newArticleBody.setText("");
                    newArticleBody.setTextColor(getResources().getColor(R.color.primary_text));
                }
            }
        });
    }

    public void initializeUI(){
        mAuth = FirebaseAuth.getInstance();
        networkReceiver = new NetworkReceiver();
        broadcastIntent();

        SQLiteHelper sqLiteDatabaseHelper = new SQLiteHelper(this);
        SQLiteDatabase sqLiteDatabase = sqLiteDatabaseHelper.getReadableDatabase();
    }

    /** Cover Upload */
    public void uploadCover() {
        if(Build.VERSION.SDK_INT >= 22) {
            checkAndRequestForPermission();
        }
        else {
            openGallery();
        }
    }

    public void checkAndRequestForPermission() {
        if(ContextCompat.checkSelfPermission(NewArticleView.this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED) {
            if(ActivityCompat.shouldShowRequestPermissionRationale(NewArticleView.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                showToast("Must Grant Permission to Upload");
            }
            else {
                ActivityCompat.requestPermissions(NewArticleView.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PReqCode);
            }
        }
        else {
            openGallery();
        }
    }

    public void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, ReqCode);
    }

    public void setCover(Uri uri) {
        newArticleUploadCoverLL.setVisibility(View.GONE);
        newArticleCoverLL.setVisibility(View.VISIBLE);
        newArticleCover.setImageURI(coverUri);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK && requestCode == ReqCode && data!=null) {
            // Successfully selected an image
            // Save its reference to a URI variable
            coverUri = data.getData();
            setCover(coverUri);
        }
    }

    /** Post Article */
    public void postArticle() {
        // Upload Cover to Storage
        String coverDownloadLink = "";
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("article_covers");
        StorageReference coverFilePath = storageReference.child(coverUri.getLastPathSegment());
        coverFilePath.putFile(coverUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                coverFilePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        coverUrl = uri.toString();
                        title = newArticleTitle.getText().toString();
                        body = newArticleBody.getText().toString();
                        uid = activeUser.getUid().toString();
                        article = new Article(uid, title, coverUrl, body);
                        postArticleToFirebase(article);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        showToast("[ERROR - Storage] " + e.getMessage());
                    }
                });
            }
        });
    }

    public void postArticleToFirebase(Article article) {
        // initialize new article on database
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference newArticleRef = database.collection("articles").document();

        // get & set aid
        aid = newArticleRef.getId();
        article.setAid(aid);

        // set article to database
        newArticleRef.set(article).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                showToast("Article Added Successfully");
                Intent intent = new Intent(NewArticleView.this, ArticlesView.class);
                startActivity(intent);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                showToast("[ERROR - FIREBASE] " + e.toString());
            }
        });
    }

    /** Listeners */
    @Override
    public void onClick(View view) {
        if(view == newArticleUploadCoverClickableLL) {
            new CountDownTimer(100, 20) {
                int i;
                @Override
                public void onTick(long l) {
                    if (i % 2 == 0) {
                        newArticleUploadCoverClickableLL.setVisibility(View.INVISIBLE);
                    } else {
                        newArticleUploadCoverClickableLL.setVisibility(View.VISIBLE);
                    }
                    i++;
                }

                @Override
                public void onFinish() {
                    newArticleUploadCoverClickableLL.setVisibility(View.VISIBLE);
                    uploadCover();
                }
            }.start();
        }
        else if(view == newArticleCancel) {
            Intent intent = new Intent(NewArticleView.this, ArticlesView.class);
            startActivity(intent);
            finish();
        }
        else if(view == newArticlePost) {
            if(!newArticleTitle.getText().toString().isEmpty()
                    && !newArticleTitle.getText().toString().equals(getResources().getString(R.string.txt_give_a_catchy_title))
                    && !newArticleBody.getText().toString().isEmpty()
                    && !newArticleBody.getText().toString().equals(getResources().getString(R.string.txt_write_your_article))
                    && coverUri!=null) {
                postArticle();
            }
            else {
                if(newArticleTitle.getText().toString().isEmpty()
                        || newArticleTitle.getText().toString().equals(getResources().getString(R.string.txt_give_a_catchy_title))) {
                    showToast("Article Must Have A Title");
                }
                else if(newArticleBody.getText().toString().isEmpty()
                        || newArticleBody.getText().toString().equals(getResources().getString(R.string.txt_write_your_article))) {
                    showToast("Article Body Can't Be Blank");
                }
                else if(coverUri == null) {
                    showToast("Article Must Have A Cover Image");
                }
            }
        }
    }

    /** Others */
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(NewArticleView.this, ArticlesView.class);
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