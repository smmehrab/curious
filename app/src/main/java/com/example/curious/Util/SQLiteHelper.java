package com.example.curious.Util;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.view.Gravity;
import android.widget.Toast;

import com.example.curious.Models.User;


public class SQLiteHelper extends SQLiteOpenHelper {

    /*** Database ***/
    private static final String DATABASE_NAME = "database_local.db";

    /*** Others ***/
    private Context context;
    private static int VERSION_NUMBER = 15;
    private static final String DROP_TABLE = "DROP TABLE IF EXISTS ";

    /*** TABLE USER ***/
    private static final String TABLE_USER = "table_user";
    private static final String USER_ID = "uid";
    private static final String USER_EMAIL = "user_email";
    private static final String USER_NAME = "user_name";
    private static final String USER_PHOTO = "user_photo";
    private static final String USER_DEVICE = "user_device";

    private static final String CREATE_TABLE_USER = "CREATE TABLE " + TABLE_USER + "(" +
            USER_ID + " TEXT PRIMARY KEY, " +
            USER_EMAIL + " TEXT NOT NULL, " +
            USER_NAME + " TEXT NOT NULL, " +
            USER_PHOTO + " TEXT NOT NULL, " +
            USER_DEVICE + " TEXT NOT NULL); ";

    /*** Constructor ***/
    public SQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION_NUMBER);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        try {
            /*** To Create Tables ***/
            sqLiteDatabase.execSQL(CREATE_TABLE_USER);
        }
        catch (Exception e){
            showToast("Exception : " + e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        try {
            // To Drop & Recreate Tables
            sqLiteDatabase.execSQL(DROP_TABLE+TABLE_USER);
            onCreate(sqLiteDatabase);
        } catch (Exception e) {
            showToast("Exception : " + e);
        }
    }

    public void refreshDatabase(SQLiteDatabase sqLiteDatabase){
        try {
            sqLiteDatabase.execSQL(DROP_TABLE + TABLE_USER);
            onCreate(sqLiteDatabase);
        } catch (Exception e){
            showToast("Exception : " + e);
        }
    }

    /*** Query on TABLE_USER ***/
    public void insertUser(User user){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        String uid = user.getUid();
        String email = user.getEmail();
        String name = user.getName();
        String photo = user.getPhoto();
        String device = user.getDevice();

        contentValues.put(USER_ID, uid);
        contentValues.put(USER_EMAIL, email);
        contentValues.put(USER_NAME, name);
        contentValues.put(USER_PHOTO, photo);
        contentValues.put(USER_DEVICE, device);

        Boolean result = findUser(uid);
        if(!result) {
            sqLiteDatabase.insert(TABLE_USER, null, contentValues);
        }
    }

    public Boolean findUser(String uid){
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();

        Cursor cursor = sqLiteDatabase.query(TABLE_USER,
                null,
                USER_ID +  " = ?",
                new String[] {uid},
                null,
                null,
                null);

        cursor.moveToPosition(0);

        if(cursor.getCount()==0)
            return false;

        return true;
    }

    @SuppressLint("Range")
    public User getUser(){
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();

        Cursor cursor = sqLiteDatabase.rawQuery("SELECT *" +
                " FROM " + TABLE_USER + ";", null);

        cursor.moveToPosition(0);
        return new User(cursor.getString(cursor.getColumnIndex(USER_ID)),
                cursor.getString(cursor.getColumnIndex(USER_EMAIL)),
                cursor.getString(cursor.getColumnIndex(USER_NAME)),
                cursor.getString(cursor.getColumnIndex(USER_PHOTO)),
                cursor.getString(cursor.getColumnIndex(USER_DEVICE)));
    }

    public String getUid(){
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();

        Cursor cursor = sqLiteDatabase.rawQuery("SELECT *" +
                " FROM " + TABLE_USER + ";", null);

        cursor.moveToPosition(0);
        @SuppressLint("Range") String result = cursor.getString(cursor.getColumnIndex(USER_ID));
        return result;
    }

    /*** Additional Functions ***/
    public void showToast(String message){
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }
}
