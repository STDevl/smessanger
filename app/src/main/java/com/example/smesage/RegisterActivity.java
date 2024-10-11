package com.example.smesage;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.util.UUID;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";
    private EditText usernameInput;
    private DatabaseHelper dbHelper;
    private ActivityResultLauncher<Intent> pinActivityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Запрет скриншотов и записи экрана
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);

        dbHelper = new DatabaseHelper(this);

        // Регистрация ActivityResultLauncher
        pinActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        String pinCode = result.getData().getStringExtra("pinCode");
                        String username = usernameInput.getText().toString().trim();
                        String userId = getUserIdFromServer();
                        dbHelper.addUser(userId, username, pinCode);
                        Toast.makeText(this, "Пользователь зарегистрирован", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
        );

        if (dbHelper.isUserExists()) {
            Intent intent = new Intent(RegisterActivity.this, PinActivity.class);
            intent.putExtra("isRegistration", false);
            startActivity(intent);
            finish();
        } else {
            usernameInput = findViewById(R.id.usernameInput);
            Button registerButton = findViewById(R.id.registerButton);

            if (usernameInput == null) {
                Log.e(TAG, "usernameInput is null");
            } else {
                Log.i(TAG, "usernameInput initialized");
            }

            if (registerButton == null) {
                Log.e(TAG, "registerButton is null");
            } else {
                Log.i(TAG, "registerButton initialized");
            }

            assert registerButton != null;
            registerButton.setOnClickListener(v -> {
                String username = usernameInput.getText().toString().trim();
                if (!username.isEmpty()) {
                    Intent intent = new Intent(RegisterActivity.this, PinActivity.class);
                    intent.putExtra("isRegistration", true);
                    pinActivityResultLauncher.launch(intent);
                } else {
                    Toast.makeText(RegisterActivity.this, "Введите имя пользователя", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private String getUserIdFromServer() {
        return UUID.randomUUID().toString();
    }
}
