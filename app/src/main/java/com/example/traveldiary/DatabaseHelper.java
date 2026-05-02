package com.example.traveldiary;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "TravelDiary.db";
    private static final int DATABASE_VERSION = 4; // Bumped to 4 for Likes and Comments!

    // USERS Table
    private static final String TABLE_USERS = "users";
    private static final String COLUMN_USER_EMAIL = "email";
    private static final String COLUMN_USER_NAME = "name";
    private static final String COLUMN_USER_BIO = "bio";
    private static final String COLUMN_USER_PIC = "profile_pic";

    // TRIPS Table
    private static final String TABLE_TRIPS = "trips";
    private static final String COLUMN_TRIP_ID = "id";
    private static final String COLUMN_TRIP_USER_EMAIL = "user_email";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_LOCATION = "location";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_DESCRIPTION = "description";
    private static final String COLUMN_COVER_IMAGE = "cover_image";
    private static final String COLUMN_IS_PUBLIC = "is_public";

    // GALLERY Table
    private static final String TABLE_GALLERY = "gallery";
    private static final String COLUMN_GALLERY_ID = "id";
    private static final String COLUMN_GALLERY_TRIP_ID = "trip_id";
    private static final String COLUMN_GALLERY_URI = "image_uri";

    // TODO: FUTURE FIREBASE MIGRATION NOTICE
    // When moving to Firebase, these local SQLite tables will be replaced by 
    // Firestore collections ('users', 'trips', 'likes', 'comments').
    // The image_uri fields will store URLs from Firebase Storage.

    // NEW: LIKES Table
    private static final String TABLE_LIKES = "likes";
    private static final String COLUMN_LIKE_ID = "id";
    private static final String COLUMN_LIKE_TRIP_ID = "trip_id";
    private static final String COLUMN_LIKE_USER_EMAIL = "user_email";

    // NEW: COMMENTS Table
    private static final String TABLE_COMMENTS = "comments";
    private static final String COLUMN_COMMENT_ID = "id";
    private static final String COLUMN_COMMENT_TRIP_ID = "trip_id";
    private static final String COLUMN_COMMENT_USER_EMAIL = "user_email";
    private static final String COLUMN_COMMENT_TEXT = "comment_text";
    private static final String COLUMN_COMMENT_DATE = "comment_date";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_USERS + " (" +
                COLUMN_USER_EMAIL + " TEXT PRIMARY KEY, " +
                COLUMN_USER_NAME + " TEXT, " +
                COLUMN_USER_BIO + " TEXT, " +
                COLUMN_USER_PIC + " TEXT)");

        db.execSQL("CREATE TABLE " + TABLE_TRIPS + " (" +
                COLUMN_TRIP_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_TRIP_USER_EMAIL + " TEXT, " +
                COLUMN_TITLE + " TEXT, " +
                COLUMN_LOCATION + " TEXT, " +
                COLUMN_DATE + " TEXT, " +
                COLUMN_DESCRIPTION + " TEXT, " +
                COLUMN_COVER_IMAGE + " TEXT, " +
                COLUMN_IS_PUBLIC + " INTEGER DEFAULT 0, " +
                "FOREIGN KEY(" + COLUMN_TRIP_USER_EMAIL + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_EMAIL + "))");

        db.execSQL("CREATE TABLE " + TABLE_GALLERY + " (" +
                COLUMN_GALLERY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_GALLERY_TRIP_ID + " INTEGER, " +
                COLUMN_GALLERY_URI + " TEXT, " +
                "FOREIGN KEY(" + COLUMN_GALLERY_TRIP_ID + ") REFERENCES " + TABLE_TRIPS + "(" + COLUMN_TRIP_ID + ") ON DELETE CASCADE)");

        // Create LIKES table
        db.execSQL("CREATE TABLE " + TABLE_LIKES + " (" +
                COLUMN_LIKE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_LIKE_TRIP_ID + " INTEGER, " +
                COLUMN_LIKE_USER_EMAIL + " TEXT, " +
                "FOREIGN KEY(" + COLUMN_LIKE_TRIP_ID + ") REFERENCES " + TABLE_TRIPS + "(" + COLUMN_TRIP_ID + ") ON DELETE CASCADE)");

        // Create COMMENTS table
        db.execSQL("CREATE TABLE " + TABLE_COMMENTS + " (" +
                COLUMN_COMMENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_COMMENT_TRIP_ID + " INTEGER, " +
                COLUMN_COMMENT_USER_EMAIL + " TEXT, " +
                COLUMN_COMMENT_TEXT + " TEXT, " +
                COLUMN_COMMENT_DATE + " TEXT, " +
                "FOREIGN KEY(" + COLUMN_COMMENT_TRIP_ID + ") REFERENCES " + TABLE_TRIPS + "(" + COLUMN_TRIP_ID + ") ON DELETE CASCADE)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 4) {
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_LIKES + " (" +
                    COLUMN_LIKE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_LIKE_TRIP_ID + " INTEGER, " +
                    COLUMN_LIKE_USER_EMAIL + " TEXT, " +
                    "FOREIGN KEY(" + COLUMN_LIKE_TRIP_ID + ") REFERENCES " + TABLE_TRIPS + "(" + COLUMN_TRIP_ID + ") ON DELETE CASCADE)");

            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_COMMENTS + " (" +
                    COLUMN_COMMENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_COMMENT_TRIP_ID + " INTEGER, " +
                    COLUMN_COMMENT_USER_EMAIL + " TEXT, " +
                    COLUMN_COMMENT_TEXT + " TEXT, " +
                    COLUMN_COMMENT_DATE + " TEXT, " +
                    "FOREIGN KEY(" + COLUMN_COMMENT_TRIP_ID + ") REFERENCES " + TABLE_TRIPS + "(" + COLUMN_TRIP_ID + ") ON DELETE CASCADE)");
        }
    }

    // --- DASHBOARD DATA METHODS ---

    // FOR INDIVIDUAL USER DASHBOARD (Home/Profile)
    public int getTripCountForUser(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_TRIPS + " WHERE " + COLUMN_TRIP_USER_EMAIL + "=?", new String[]{email});
        int count = 0;
        if (cursor.moveToFirst()) count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    public int getPlaceCountForUser(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(DISTINCT " + COLUMN_LOCATION + ") FROM " + TABLE_TRIPS + " WHERE " + COLUMN_TRIP_USER_EMAIL + "=?", new String[]{email});
        int count = 0;
        if (cursor.moveToFirst()) count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    public int getPhotoCountForUser(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        // Count cover images + gallery images for a specific user
        Cursor cursor = db.rawQuery("SELECT " +
                        "(SELECT COUNT(*) FROM " + TABLE_TRIPS + " WHERE " + COLUMN_TRIP_USER_EMAIL + "=? AND " + COLUMN_COVER_IMAGE + " != '') + " +
                        "(SELECT COUNT(*) FROM " + TABLE_GALLERY + " WHERE " + COLUMN_GALLERY_TRIP_ID + " IN (SELECT " + COLUMN_TRIP_ID + " FROM " + TABLE_TRIPS + " WHERE " + COLUMN_TRIP_USER_EMAIL + "=?))",
                new String[]{email, email});
        int count = 0;
        if (cursor.moveToFirst()) count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    // FOR EXPLORE DASHBOARD (Overall Public Data)
    public int getTotalPublicTripCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_TRIPS + " WHERE " + COLUMN_IS_PUBLIC + "=1", null);
        int count = 0;
        if (cursor.moveToFirst()) count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    public int getTotalPublicPlaceCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(DISTINCT " + COLUMN_LOCATION + ") FROM " + TABLE_TRIPS + " WHERE " + COLUMN_IS_PUBLIC + "=1", null);
        int count = 0;
        if (cursor.moveToFirst()) count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    public int getTotalPublicPhotoCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        // Count all public cover images + their gallery images
        Cursor cursor = db.rawQuery("SELECT " +
                        "(SELECT COUNT(*) FROM " + TABLE_TRIPS + " WHERE " + COLUMN_IS_PUBLIC + "=1 AND " + COLUMN_COVER_IMAGE + " != '') + " +
                        "(SELECT COUNT(*) FROM " + TABLE_GALLERY + " WHERE " + COLUMN_GALLERY_TRIP_ID + " IN (SELECT " + COLUMN_TRIP_ID + " FROM " + TABLE_TRIPS + " WHERE " + COLUMN_IS_PUBLIC + "=1))",
                null);
        int count = 0;
        if (cursor.moveToFirst()) count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    // --- LIKE METHODS ---

    public boolean toggleLike(int tripId, String userEmail) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_LIKES + " WHERE " + COLUMN_LIKE_TRIP_ID + "=? AND " + COLUMN_LIKE_USER_EMAIL + "=?",
                new String[]{String.valueOf(tripId), userEmail});

        if (cursor.getCount() > 0) {
            db.delete(TABLE_LIKES, COLUMN_LIKE_TRIP_ID + "=? AND " + COLUMN_LIKE_USER_EMAIL + "=?",
                    new String[]{String.valueOf(tripId), userEmail});
            cursor.close();
            return false; // Unliked
        } else {
            ContentValues values = new ContentValues();
            values.put(COLUMN_LIKE_TRIP_ID, tripId);
            values.put(COLUMN_LIKE_USER_EMAIL, userEmail);
            db.insert(TABLE_LIKES, null, values);
            cursor.close();
            return true; // Liked
        }
    }

    public int getLikeCount(int tripId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_LIKES + " WHERE " + COLUMN_LIKE_TRIP_ID + "=?",
                new String[]{String.valueOf(tripId)});
        int count = 0;
        if (cursor.moveToFirst()) count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    public boolean isTripLikedByUser(int tripId, String userEmail) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_LIKES + " WHERE " + COLUMN_LIKE_TRIP_ID + "=? AND " + COLUMN_LIKE_USER_EMAIL + "=?",
                new String[]{String.valueOf(tripId), userEmail});
        boolean liked = cursor.getCount() > 0;
        cursor.close();
        return liked;
    }

    // --- COMMENT METHODS ---

    public void addComment(int tripId, String userEmail, String text) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_COMMENT_TRIP_ID, tripId);
        values.put(COLUMN_COMMENT_USER_EMAIL, userEmail);
        values.put(COLUMN_COMMENT_TEXT, text);
        values.put(COLUMN_COMMENT_DATE, String.valueOf(System.currentTimeMillis()));
        db.insert(TABLE_COMMENTS, null, values);
    }

    public int getCommentCount(int tripId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_COMMENTS + " WHERE " + COLUMN_COMMENT_TRIP_ID + "=?",
                new String[]{String.valueOf(tripId)});
        int count = 0;
        if (cursor.moveToFirst()) count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    public Cursor getCommentsForTrip(int tripId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT c.*, u.name FROM " + TABLE_COMMENTS + " c " +
                "LEFT JOIN " + TABLE_USERS + " u ON c." + COLUMN_COMMENT_USER_EMAIL + " = u." + COLUMN_USER_EMAIL +
                " WHERE c." + COLUMN_COMMENT_TRIP_ID + "=? ORDER BY c." + COLUMN_COMMENT_ID + " DESC",
                new String[]{String.valueOf(tripId)});
    }

    // --- EXISTING METHODS (Retained) ---

    public boolean saveOrUpdateUser(String email, String name, String bio, String profilePicUri) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_EMAIL, email);
        values.put(COLUMN_USER_NAME, name);
        values.put(COLUMN_USER_BIO, bio);
        values.put(COLUMN_USER_PIC, profilePicUri);
        long result = db.replace(TABLE_USERS, null, values);
        return result != -1;
    }

    public Cursor getUserProfile(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE " + COLUMN_USER_EMAIL + "=?", new String[]{email});
    }

    public boolean isDuplicateTrip(String title, String location, String userEmail, int excludeId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_TRIPS + " WHERE " + COLUMN_TITLE + " = ? AND " + COLUMN_LOCATION + " = ? AND " + COLUMN_TRIP_USER_EMAIL + " = ? AND " + COLUMN_TRIP_ID + " != ?";
        Cursor cursor = db.rawQuery(query, new String[]{title, location, userEmail, String.valueOf(excludeId)});
        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        return exists;
    }

    public long insertTrip(String userEmail, String title, String location, String date, String description, String coverImage, int isPublic) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TRIP_USER_EMAIL, userEmail);
        values.put(COLUMN_TITLE, title);
        values.put(COLUMN_LOCATION, location);
        values.put(COLUMN_DATE, date);
        values.put(COLUMN_DESCRIPTION, description);
        values.put(COLUMN_COVER_IMAGE, coverImage);
        values.put(COLUMN_IS_PUBLIC, isPublic);
        return db.insert(TABLE_TRIPS, null, values);
    }

    public boolean updateTrip(int tripId, String title, String location, String date, String description, String coverImage, int isPublic) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, title);
        values.put(COLUMN_LOCATION, location);
        values.put(COLUMN_DATE, date);
        values.put(COLUMN_DESCRIPTION, description);
        values.put(COLUMN_COVER_IMAGE, coverImage);
        values.put(COLUMN_IS_PUBLIC, isPublic);
        int result = db.update(TABLE_TRIPS, values, COLUMN_TRIP_ID + "=?", new String[]{String.valueOf(tripId)});
        return result > 0;
    }

    public boolean deleteTrip(int tripId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_GALLERY, COLUMN_GALLERY_TRIP_ID + "=?", new String[]{String.valueOf(tripId)});
        int result = db.delete(TABLE_TRIPS, COLUMN_TRIP_ID + "=?", new String[]{String.valueOf(tripId)});
        return result > 0;
    }

    public Cursor getUserTrips(String userEmail) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_TRIPS + " WHERE " + COLUMN_TRIP_USER_EMAIL + "=? ORDER BY " + COLUMN_TRIP_ID + " DESC", new String[]{userEmail});
    }

    public Cursor getPublicTrips() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT t.*, u.name as author_name FROM " + TABLE_TRIPS + " t " +
                "LEFT JOIN " + TABLE_USERS + " u ON t." + COLUMN_TRIP_USER_EMAIL + " = u." + COLUMN_USER_EMAIL +
                " WHERE t." + COLUMN_IS_PUBLIC + "=1 ORDER BY t." + COLUMN_TRIP_ID + " DESC", null);
    }

    public Cursor getSingleTrip(int tripId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_TRIPS + " WHERE " + COLUMN_TRIP_ID + "=?", new String[]{String.valueOf(tripId)});
    }
}
