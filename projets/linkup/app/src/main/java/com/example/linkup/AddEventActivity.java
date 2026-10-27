package com.example.linkup;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class AddEventActivity extends AppCompatActivity {

    private EditText eventNameEditText;
    private EditText eventTypeEditText;
    private Button saveEventButton;

    // Variables pour la latitude et la longitude (à récupérer de la carte ou du GPS)
    private double lat = 48.8583; // Exemple de latitude (Tour Eiffel)
    private double lng = 2.2944;  // Exemple de longitude (Tour Eiffel)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);  // Charge le bon layout

        // Initialisation des éléments de l'UI
        eventNameEditText = findViewById(R.id.eventName);
        eventTypeEditText = findViewById(R.id.eventType);
        saveEventButton = findViewById(R.id.saveEventButton);

        // Lors du clic sur le bouton "Save Event"
        saveEventButton.setOnClickListener(v -> {
            // Récupérer le nom et le type de l'événement
            String name = eventNameEditText.getText().toString();
            String type = eventTypeEditText.getText().toString();

            // Enregistrer l'événement dans la base de données
            saveEventToDatabase(name, type);
        });
    }

    // Méthode pour enregistrer l'événement dans la base de données
    private void saveEventToDatabase(String name, String type) {
        EventsDbHelper dbHelper = new EventsDbHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Créer un ContentValues pour insérer les données
        ContentValues values = new ContentValues();
        values.put(EventsDbHelper.COL_NAME, name);
        values.put(EventsDbHelper.COL_LAT, lat);  // Latitude
        values.put(EventsDbHelper.COL_LNG, lng);  // Longitude
        values.put(EventsDbHelper.COL_TYPE, type);

        // Insérer les valeurs dans la base de données
        long newRowId = db.insert(EventsDbHelper.TABLE_EVENTS, null, values);
        db.close();  // Fermer la base de données

        // Optionnel : Afficher un message ou faire une action après l'ajout
    }
}
