package com.example.smesage;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private boolean isAppInBackground = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Запрет скриншотов и записи экрана
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        DatabaseHelper databaseHelper = new DatabaseHelper(this);

        // Проверка наличия данных
        List<User> userList = databaseHelper.getAllUsers();
        if (userList.isEmpty()) {
            // Временный пользователь для проверки
            databaseHelper.addUser("1", "Myself", "1234");
            userList = databaseHelper.getAllUsers();
        }
        Log.d("MainActivity", "Number of users: " + userList.size());
        for (User user : userList) {
            Log.d("MainActivity", "User: " + user.getUsername());
        }

        ContactsAdapter.OnUserClickListener onUserClickListener = user -> {
            Intent intent = new Intent(MainActivity.this, ChatActivity.class);
            intent.putExtra("userId", user.getUserId());
            startActivity(intent);
        };

        ContactsAdapter contactsAdapter = new ContactsAdapter(userList, onUserClickListener);
        recyclerView.setAdapter(contactsAdapter);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
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
            Intent intent = new Intent(MainActivity.this, PinActivity.class);
            intent.putExtra("isRegistration", false);
            startActivity(intent);
        }
    }
}
