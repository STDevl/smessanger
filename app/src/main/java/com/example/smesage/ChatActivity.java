package com.example.smesage;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import android.provider.MediaStore;
import android.content.pm.PackageManager; // Добавьте этот импорт
import android.util.Log;

public class ChatActivity extends AppCompatActivity {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private RecyclerView recyclerView;
    private EditText messageInput;
    private Button sendButton;
    private Button capturePhotoButton; // Новая кнопка
    private DatabaseHelper databaseHelper;
    private MessagesAdapter messagesAdapter;
    private String currentUserId;
    private String chatUserId;
    private boolean isAppInBackground = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        recyclerView = findViewById(R.id.recyclerViewMessages);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);
        capturePhotoButton = findViewById(R.id.capturePhotoButton); // Инициализация кнопки

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        databaseHelper = new DatabaseHelper(this);

        currentUserId = "1";  // Заменить на идентификатор текущего пользователя
        chatUserId = getIntent().getStringExtra("userId");  // Получаем идентификатор пользователя для чата
        Log.d("ChatActivity", "Chat with user: " + chatUserId); // Логирование

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageText = messageInput.getText().toString();
                if (!TextUtils.isEmpty(messageText)) {
                    databaseHelper.addMessage(currentUserId, chatUserId, messageText);
                    messageInput.setText("");
                    Log.d("ChatActivity", "Message sent: " + messageText); // Логирование отправки сообщения
                    updateMessageList();
                } else {
                    Toast.makeText(ChatActivity.this, "Введите сообщение", Toast.LENGTH_SHORT).show();
                }
            }
        });

        capturePhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCamera();
            }
        });

        updateMessageList();
    }

    private void openCamera() {
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.CAMERA}, 0);
        } else {
            dispatchTakePictureIntent();
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            // Здесь можно отправить фото как сообщение, если нужно
            // Например, добавив его в базу данных
            // databaseHelper.addImageMessage(currentUserId, chatUserId, imageBitmap);
            Toast.makeText(this, "Фото захвачено", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Не удалось сделать снимок", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateMessageList() {
        List<Message> messageList = databaseHelper.getMessagesBetweenUsers(currentUserId, chatUserId);
        Log.d("ChatActivity", "Messages loaded, count: " + messageList.size()); // Логирование загрузки сообщений
        for (Message message : messageList) {
            Log.d("ChatActivity", "Message: " + message.getMessageText() + " from " + message.getSenderId() + " to " + message.getReceiverId()); // Логирование сообщений
        }
        if (messagesAdapter == null) {
            messagesAdapter = new MessagesAdapter(messageList);
            recyclerView.setAdapter(messagesAdapter);
        } else {
            messagesAdapter.updateMessages(messageList);
        }
        recyclerView.scrollToPosition(messageList.size() - 1);
    }

    @Override
    protected void onPause() {
        super.onPause();
        isAppInBackground = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isAppInBackground) {
            isAppInBackground = false;
            updateMessageList(); // Обновляем список сообщений при возврате в активность
        }
    }
}
