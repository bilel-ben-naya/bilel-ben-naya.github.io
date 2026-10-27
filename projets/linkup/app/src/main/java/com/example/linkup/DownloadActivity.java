package com.example.linkup;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class DownloadActivity extends AppCompatActivity {

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(DownloadActivity.this, "Téléchargement terminé ✅", Toast.LENGTH_LONG).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);

        Button buttonDownload = findViewById(R.id.buttonDownload);
        Button buttonImport = findViewById(R.id.buttonImport);

        buttonDownload.setOnClickListener(v -> {
            Intent serviceIntent = new Intent(DownloadActivity.this, DownloadService.class);
            startService(serviceIntent);
        });

        buttonImport.setOnClickListener(v -> importFileToDb());
    }

    private void importFileToDb() {
        try {
            File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File file = new File(downloadDir, "events.txt");
            if (!file.exists()) {
                Toast.makeText(this, "Fichier introuvable ❌", Toast.LENGTH_LONG).show();
                return;
            }

            EventsRepository repo = new EventsRepository(this);
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            int count = 0;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(";;");
                if (parts.length == 4) {
                    repo.insertEvent(parts[0], parts[1], parts[2], parts[3]);
                    count++;
                }
            }
            br.close();

            Toast.makeText(this, count + " événements importés ✅", Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Toast.makeText(this, "Erreur import : " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, new IntentFilter(DownloadService.ACTION_DOWNLOAD_DONE), Context.RECEIVER_NOT_EXPORTED);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }
}
