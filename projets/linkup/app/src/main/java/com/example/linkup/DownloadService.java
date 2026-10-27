package com.example.linkup;

import android.app.IntentService;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;
import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.File;

public class DownloadService extends IntentService {

    public static final String ACTION_DOWNLOAD_DONE = "com.example.linkup.DOWNLOAD_DONE";

    public DownloadService() {
        super("DownloadService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String fileUrl = "https://drive.google.com/uc?export=download&id=1oa5a6P8nJ6aQgsbjurdiRorzAF92V2wP";
        try {
            URL url = new URL(fileUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            InputStream input = new BufferedInputStream(connection.getInputStream());
            File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (!downloadDir.exists()) downloadDir.mkdirs();
            File outFile = new File(downloadDir, "events.txt");

            FileOutputStream output = new FileOutputStream(outFile);
            byte[] data = new byte[1024];
            int count;
            while ((count = input.read(data)) != -1) {
                output.write(data, 0, count);
            }

            output.flush();
            output.close();
            input.close();
            connection.disconnect();

            Log.d("DownloadService", "Fichier téléchargé : " + outFile.getAbsolutePath());

            Intent doneIntent = new Intent(ACTION_DOWNLOAD_DONE);
            sendBroadcast(doneIntent);
        } catch (Exception e) {
            Log.e("DownloadService", "Erreur de téléchargement", e);
        }
    }
}
