package com.example.linkup;

import androidx.appcompat.app.AppCompatActivity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.app.AlertDialog;
import java.util.ArrayList;

public class ThirdActivity extends AppCompatActivity {

    private ListView listViewEvents;
    private Button buttonPrevious;

    // Les listes doivent être déclarées en dehors de onCreate() pour qu'elles soient accessibles partout dans la classe
    private ArrayList<String> eventNames = new ArrayList<>();
    private ArrayList<EventDetails> eventDetails = new ArrayList<>();

    private void loadEventData() {
        // Récupérer la base de données en lecture
        EventsDbHelper helper = new EventsDbHelper(this);
        SQLiteDatabase db = helper.getReadableDatabase();

        Cursor cursor = db.query(
                EventsDbHelper.TABLE_EVENTS,
                new String[]{
                        EventsDbHelper.COL_NAME,
                        EventsDbHelper.COL_LAT,
                        EventsDbHelper.COL_LNG,
                        EventsDbHelper.COL_TYPE
                },
                null,
                null,
                null,
                null,
                EventsDbHelper.COL_ID + " ASC"
        );

        // Remplir les listes avec les données récupérées
        while (cursor.moveToNext()) {
            String name = cursor.getString(cursor.getColumnIndex(EventsDbHelper.COL_NAME) + 1);
            String lat = cursor.getString(cursor.getColumnIndex(EventsDbHelper.COL_LAT) + 1);
            String lng = cursor.getString(cursor.getColumnIndex(EventsDbHelper.COL_LNG) + 1);
            String type = cursor.getString(cursor.getColumnIndex(EventsDbHelper.COL_TYPE) + 1);

            // Ajouter le nom de l'événement à la liste des noms
            eventNames.add(name);

            // Ajouter les détails de l'événement à la liste des détails
            eventDetails.add(new EventDetails(lat, lng, type));
        }

        cursor.close();
        db.close();
    }


    // Classe interne pour stocker les détails de chaque événement
    static class EventDetails {
        String lat;
        String lng;
        String type;

        EventDetails(String lat, String lng, String type) {
            this.lat = lat;
            this.lng = lng;
            this.type = type;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_third);  // Charge le bon layout

        // Initialiser la ListView et le bouton Retour
        listViewEvents = findViewById(R.id.listViewEvents);
        buttonPrevious = findViewById(R.id.buttonPrevious);

        // Initialiser les listes pour les événements
        eventNames = new ArrayList<>();
        eventDetails = new ArrayList<>();

        // Charger les événements depuis la base de données
        loadEventData();

        // Créer un ArrayAdapter pour afficher les noms des événements dans la ListView
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1, // Layout simple pour chaque élément
                eventNames  // Liste des noms des événements
        );

        // Appliquer l'adaptateur à la ListView
        listViewEvents.setAdapter(adapter);

        // Ajouter un listener pour afficher les détails d'un événement lorsqu'on clique dessus
        listViewEvents.setOnItemClickListener((parent, view, position, id) -> {
            // Récupérer les détails de l'événement
            EventDetails event = eventDetails.get(position);

            // Afficher un AlertDialog avec les détails de l'événement
            new AlertDialog.Builder(ThirdActivity.this)
                    .setTitle(eventNames.get(position)) // Nom de l'événement
                    .setMessage(
                            "Latitude : " + event.lat +
                                    "\nLongitude : " + event.lng +
                                    "\nType : " + event.type
                    )
                    .setPositiveButton("OK", null)
                    .show();
        });

        // Action pour le bouton "Retour" pour fermer l'activité
        buttonPrevious.setOnClickListener(v -> finish());
    }

}
