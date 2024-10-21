package com.example.cs360project;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.core.util.Pair;
import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper implements Serializable {
    private static final String DATABASE_NAME = "weighdown.db";
    private static final int DATABASE_VERSION = 1;
    private static final String COLUMN_COMPARISON = "comparison";

    // Table names
    private static final String TABLE_USERS = "users";
    private static final String TABLE_WEIGHT = "weight";
    private static final String TABLE_GOALS = "goals";

    // Common column names
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_USERNAME = "username";

    // Users table columns
    private static final String COLUMN_PASSWORD = "password";

    // Weight table columns
    private static final String COLUMN_WEIGHT = "weight";
    private static final String COLUMN_DATE = "date";

    // Goals table columns
    private static final String COLUMN_GOAL_WEIGHT = "goal_weight";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create users table
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_USERNAME + " TEXT UNIQUE,"
                + COLUMN_PASSWORD + " TEXT"
                + ")";
        db.execSQL(CREATE_USERS_TABLE);

        // Create weight table
        String CREATE_WEIGHT_TABLE = "CREATE TABLE " + TABLE_WEIGHT + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_USERNAME + " TEXT,"
                + COLUMN_WEIGHT + " REAL,"
                + COLUMN_DATE + " TEXT"
                + ")";
        db.execSQL(CREATE_WEIGHT_TABLE);

        // Create goals table
        String CREATE_GOALS_TABLE = "CREATE TABLE " + TABLE_GOALS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_USERNAME + " TEXT UNIQUE,"
                + COLUMN_GOAL_WEIGHT + " REAL,"
                + COLUMN_COMPARISON + " TEXT"
                + ")";
        db.execSQL(CREATE_GOALS_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older tables if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WEIGHT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GOALS);

        // Create tables again
        onCreate(db);
    }

    // Hash password using SHA-256
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder(2 * encodedhash.length);
            for (byte b : encodedhash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Add a new user
    public boolean addUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_PASSWORD, hashPassword(password));

        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }

    // Check if user exists and password is correct
    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = COLUMN_USERNAME + " = ? AND " + COLUMN_PASSWORD + " = ?";
        String[] selectionArgs = {username, hashPassword(password)};
        Cursor cursor = db.query(TABLE_USERS, null, selection, selectionArgs, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        return count > 0;
    }

    // Add a weight entry
    public boolean addWeight(String username, float weight) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_WEIGHT, weight);
        values.put(COLUMN_DATE, new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));

        long result = db.insert(TABLE_WEIGHT, null, values);
        return result != -1;
    }

    // Get the weight entries from a user
    public Cursor getWeightEntries(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + COLUMN_ID + ", " + COLUMN_WEIGHT + ", " + COLUMN_DATE +
                " FROM " + TABLE_WEIGHT +
                " WHERE " + COLUMN_USERNAME + " = ?" +
                " ORDER BY " + COLUMN_DATE + " DESC";
        return db.rawQuery(query, new String[]{username});
    }

    // Delete all entries by user
    public boolean deleteAllWeightEntries(String username) {
        SQLiteDatabase db = this.getWritableDatabase();
        int deletedRows = db.delete(TABLE_WEIGHT, COLUMN_USERNAME + "=?", new String[]{username});
        return deletedRows > 0;
    }

    // Method to save or update goal weight and comparison
    public boolean saveGoalWeight(String username, float goalWeight, String comparison) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_GOAL_WEIGHT, goalWeight);
        values.put(COLUMN_COMPARISON, comparison);

        // Try to insert, if it fails (due to unique constraint), then update
        long result = db.insertWithOnConflict(TABLE_GOALS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        return result != -1;
    }

    // Method to get the goal weight and comparison
    public Pair<Float, String> getGoalWeight(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + COLUMN_GOAL_WEIGHT + ", " + COLUMN_COMPARISON + " FROM " + TABLE_GOALS +
                " WHERE " + COLUMN_USERNAME + " = ? LIMIT 1";
        Cursor cursor = db.rawQuery(query, new String[]{username});
        float goalWeight = 0f;
        String comparison = "less than";
        if (cursor.moveToFirst()) {
            goalWeight = cursor.getFloat(0);
            comparison = cursor.getString(1);
        }
        cursor.close();
        return new Pair<>(goalWeight, comparison);
    }

    // Update a weight
    public boolean updateWeight(int id, float newWeight) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_WEIGHT, newWeight);
        int rowsAffected = db.update(TABLE_WEIGHT, values, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        return rowsAffected > 0;
    }

    // Delete a weight
    public boolean deleteWeight(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = db.delete(TABLE_WEIGHT, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        return rowsAffected > 0;
    }

    // Getters
    public static String getColumnWeight() {
        return COLUMN_WEIGHT;
    }

    public static String getColumnDate() {
        return COLUMN_DATE;
    }

    public static String getColumnId() {
        return COLUMN_ID;
    }
}