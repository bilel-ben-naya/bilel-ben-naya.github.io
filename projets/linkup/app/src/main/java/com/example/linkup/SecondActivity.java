package com.example.linkup;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class SecondActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        TextView textViewInfo = findViewById(R.id.textViewInfo);
        ImageView imageView = findViewById(R.id.imageView);
        Button buttonNext = findViewById(R.id.buttonNext);

        Intent intent = getIntent();
        String name = null;
        String email = null;

        if (intent != null) {
            name = intent.getStringExtra("NAME");
            email = intent.getStringExtra("EMAIL");
        }

        if (name == null) name = Prefs.getName(this);
        if (email == null) email = Prefs.getEmail(this);

        textViewInfo.setText("Bienvenue " + name + "\n(" + email + ")");

        buttonNext.setOnClickListener(v -> {
            Intent nextIntent = new Intent(SecondActivity.this, ThirdActivity.class);
            startActivity(nextIntent);
        });
    }
}
