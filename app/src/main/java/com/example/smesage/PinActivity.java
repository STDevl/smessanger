package com.example.smesage;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class PinActivity extends AppCompatActivity {
    private static final String TAG = "PinActivity";
    private TextView pinDisplay;
    private final StringBuilder pinBuilder = new StringBuilder();
    private boolean isRegistration;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin);

        // Запрет скриншотов и записи экрана
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);

        pinDisplay = findViewById(R.id.pinDisplay);
        dbHelper = new DatabaseHelper(this);

        isRegistration = getIntent().getBooleanExtra("isRegistration", false);

        findViewById(R.id.button0).setOnClickListener(v -> appendPin("0"));
        findViewById(R.id.button1).setOnClickListener(v -> appendPin("1"));
        findViewById(R.id.button2).setOnClickListener(v -> appendPin("2"));
        findViewById(R.id.button3).setOnClickListener(v -> appendPin("3"));
        findViewById(R.id.button4).setOnClickListener(v -> appendPin("4"));
        findViewById(R.id.button5).setOnClickListener(v -> appendPin("5"));
        findViewById(R.id.button6).setOnClickListener(v -> appendPin("6"));
        findViewById(R.id.button7).setOnClickListener(v -> appendPin("7"));
        findViewById(R.id.button8).setOnClickListener(v -> appendPin("8"));
        findViewById(R.id.button9).setOnClickListener(v -> appendPin("9"));
        findViewById(R.id.buttonDelete).setOnClickListener(v -> deleteLastPinDigit());

        findViewById(R.id.buttonSubmit).setOnClickListener(v -> {
            if (pinBuilder.length() >= 4 && pinBuilder.length() <= 12) {
                if (isRegistration) {
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("pinCode", pinBuilder.toString());
                    setResult(RESULT_OK, resultIntent);
                    finish();
                } else {
                    Log.i(TAG, "Checking PIN: " + pinBuilder);
                    if (dbHelper.checkPin(pinBuilder.toString())) {
                        Toast.makeText(PinActivity.this, "Авторизация успешна", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(PinActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(PinActivity.this, "Неверный PIN-код", Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                Toast.makeText(PinActivity.this, "Введите PIN-код от 4 до 12 символов", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void appendPin(String digit) {
        if (pinBuilder.length() < 12) {
            pinBuilder.append(digit);
            updatePinDisplay();
        }
    }

    private void deleteLastPinDigit() {
        if (pinBuilder.length() > 0) {
            pinBuilder.deleteCharAt(pinBuilder.length() - 1);
            updatePinDisplay();
        }
    }

    private void updatePinDisplay() {
        StringBuilder display = new StringBuilder();
        for (int i = 0; i < pinBuilder.length(); i++) {
            display.append("*");
        }
        for (int i = pinBuilder.length(); i < 12; i++) {
            display.append(" ");
        }
        pinDisplay.setText(display.toString());
    }
}
