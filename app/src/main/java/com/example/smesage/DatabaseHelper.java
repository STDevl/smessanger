package com.example.smesage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;
import android.util.Log; // Добавьте для логирования ошибок
import java.util.List;
import java.util.ArrayList;


public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHelper";
    private static final String DATABASE_NAME = "messenger.db";
    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_PASSWORD = "your_secure_password"; // Замените на ваш пароль

    private static final String TABLE_USERS = "users";
    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_PIN = "pin";

    private static final String TABLE_MESSAGES = "messages";
    private static final String COLUMN_MESSAGE_ID = "message_id";
    private static final String COLUMN_SENDER_ID = "sender_id";
    private static final String COLUMN_RECEIVER_ID = "receiver_id";
    private static final String COLUMN_MESSAGE_TEXT = "message_text";
    private static final String COLUMN_TIMESTAMP = "timestamp";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        SQLiteDatabase.loadLibs(context); // Загрузка библиотек SQLCipher
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
                + COLUMN_USER_ID + " TEXT PRIMARY KEY,"
                + COLUMN_USERNAME + " TEXT,"
                + COLUMN_PIN + " TEXT" + ")";
        db.execSQL(CREATE_USERS_TABLE);

        String CREATE_MESSAGES_TABLE = "CREATE TABLE " + TABLE_MESSAGES + "("
                + COLUMN_MESSAGE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_SENDER_ID + " TEXT,"
                + COLUMN_RECEIVER_ID + " TEXT,"
                + COLUMN_MESSAGE_TEXT + " TEXT,"
                + COLUMN_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
                + "FOREIGN KEY(" + COLUMN_SENDER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + "),"
                + "FOREIGN KEY(" + COLUMN_RECEIVER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + ")"
                + ")";
        db.execSQL(CREATE_MESSAGES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COLUMN_PIN + " TEXT");
        }
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true); // Включение поддержки внешних ключей
    }

    /**
     * Открытие базы данных с паролем.
     */
    private SQLiteDatabase getWritableDatabaseWithPassword() {
        return this.getWritableDatabase(DATABASE_PASSWORD);
    }

    private SQLiteDatabase getReadableDatabaseWithPassword() {
        return this.getReadableDatabase(DATABASE_PASSWORD);
    }

    public void addUser(String userId, String username, String pin) {
        SQLiteDatabase db = null;
        try {
            db = getWritableDatabaseWithPassword();
            ContentValues values = new ContentValues();
            values.put(COLUMN_USER_ID, userId);
            values.put(COLUMN_USERNAME, username);
            values.put(COLUMN_PIN, HashUtil.hashWithSHA3(pin));
            db.insert(TABLE_USERS, null, values);
        } catch (Exception e) {
            Log.e(TAG, "Error adding user", e);
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

    public boolean isUserExists() {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = getReadableDatabaseWithPassword();
            cursor = db.rawQuery("SELECT 1 FROM " + TABLE_USERS + " LIMIT 1", null);
            return cursor.getCount() > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error checking if user exists", e);
            return false;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

    public boolean checkPin(String pin) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = getReadableDatabaseWithPassword();
            cursor = db.rawQuery("SELECT 1 FROM " + TABLE_USERS + " WHERE " + COLUMN_PIN + " = ?", new String[]{HashUtil.hashWithSHA3(pin)});
            return cursor.getCount() > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error checking PIN", e);
            return false;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

    public List<User> getAllUsers() {
        List<User> userList = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = getReadableDatabaseWithPassword();
            cursor = db.query(TABLE_USERS, null, null, null, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int userIdIndex = cursor.getColumnIndex(COLUMN_USER_ID);
                    int usernameIndex = cursor.getColumnIndex(COLUMN_USERNAME);

                    if (userIdIndex >= 0 && usernameIndex >= 0) {
                        String userId = cursor.getString(userIdIndex);
                        String username = cursor.getString(usernameIndex);
                        userList.add(new User(userId, username));
                        Log.d(TAG, "User ID: " + userId + ", Username: " + username); // Логируем пользователей
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error retrieving users", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null && db.isOpen()) {
                db.close();
            }
        }

        return userList;
    }
    public void addMessage(String senderId, String receiverId, String messageText) {
        SQLiteDatabase db = null;
        try {
            db = getWritableDatabaseWithPassword();
            ContentValues values = new ContentValues();
            values.put(COLUMN_SENDER_ID, senderId);
            values.put(COLUMN_RECEIVER_ID, receiverId);
            values.put(COLUMN_MESSAGE_TEXT, messageText);

            Log.d(TAG, "Attempting to add message from " + senderId + " to " + receiverId + ": " + messageText);

            // Проверка существования senderId и receiverId в таблице пользователей
            Cursor cursor = db.rawQuery("SELECT " + COLUMN_USER_ID + " FROM " + TABLE_USERS + " WHERE " + COLUMN_USER_ID + "=? OR " + COLUMN_USER_ID + "=?", new String[]{senderId, receiverId});
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String userId = cursor.getString(cursor.getColumnIndex(COLUMN_USER_ID));
                    Log.d(TAG, "Found user in users table: " + userId);
                } while (cursor.moveToNext());
                cursor.close();
            } else {
                Log.e(TAG, "User IDs not found in users table for senderId: " + senderId + " or receiverId: " + receiverId);
            }

            long result = db.insert(TABLE_MESSAGES, null, values);
            if (result == -1) {
                Log.e(TAG, "Failed to insert message: " + messageText);
            } else {
                Log.d(TAG, "Message added, result: " + result);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error adding message", e);
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

    public List<Message> getAllMessages() {
        List<Message> messageList = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = getReadableDatabaseWithPassword();
            cursor = db.query(TABLE_MESSAGES, null, null, null, null, null, COLUMN_TIMESTAMP + " ASC");

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int messageIdIndex = cursor.getColumnIndex(COLUMN_MESSAGE_ID);
                    int senderIdIndex = cursor.getColumnIndex(COLUMN_SENDER_ID);
                    int receiverIdIndex = cursor.getColumnIndex(COLUMN_RECEIVER_ID);
                    int messageTextIndex = cursor.getColumnIndex(COLUMN_MESSAGE_TEXT);
                    int timestampIndex = cursor.getColumnIndex(COLUMN_TIMESTAMP);

                    if (messageIdIndex >= 0 && senderIdIndex >= 0 && receiverIdIndex >= 0 && messageTextIndex >= 0 && timestampIndex >= 0) {
                        int messageId = cursor.getInt(messageIdIndex);
                        String senderId = cursor.getString(senderIdIndex);
                        String receiverId = cursor.getString(receiverIdIndex);
                        String messageText = cursor.getString(messageTextIndex);
                        String timestamp = cursor.getString(timestampIndex);
                        messageList.add(new Message(messageId, senderId, receiverId, messageText, timestamp));
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error retrieving messages", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null && db.isOpen()) {
                db.close();
            }
        }

        return messageList;
    }

    public List<Message> getMessagesBetweenUsers(String userId1, String userId2) {
        List<Message> messageList = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = getReadableDatabaseWithPassword();
            String selection = "(" + COLUMN_SENDER_ID + "=? AND " + COLUMN_RECEIVER_ID + "=?) OR (" + COLUMN_SENDER_ID + "=? AND " + COLUMN_RECEIVER_ID + "=?)";
            String[] selectionArgs = {userId1, userId2, userId2, userId1};
            cursor = db.query(TABLE_MESSAGES, null, selection, selectionArgs, null, null, COLUMN_TIMESTAMP + " ASC");

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int messageIdIndex = cursor.getColumnIndex(COLUMN_MESSAGE_ID);
                    int senderIdIndex = cursor.getColumnIndex(COLUMN_SENDER_ID);
                    int receiverIdIndex = cursor.getColumnIndex(COLUMN_RECEIVER_ID);
                    int messageTextIndex = cursor.getColumnIndex(COLUMN_MESSAGE_TEXT);
                    int timestampIndex = cursor.getColumnIndex(COLUMN_TIMESTAMP);

                    if (messageIdIndex >= 0 && senderIdIndex >= 0 && receiverIdIndex >= 0 && messageTextIndex >= 0 && timestampIndex >= 0) {
                        int messageId = cursor.getInt(messageIdIndex);
                        String senderId = cursor.getString(senderIdIndex);
                        String receiverId = cursor.getString(receiverIdIndex);
                        String messageText = cursor.getString(messageTextIndex);
                        String timestamp = cursor.getString(timestampIndex);
                        messageList.add(new Message(messageId, senderId, receiverId, messageText, timestamp));
                        Log.d(TAG, "Loaded message: " + messageText + " from " + senderId + " to " + receiverId); // Логирование сообщений из базы данных
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error retrieving messages", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null && db.isOpen()) {
                db.close();
            }
        }

        return messageList;
    }


}
