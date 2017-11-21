package com.example.lalitha.studentenliste;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;


import com.example.lalitha.studentenliste.data.StudentContract;
import com.example.lalitha.studentenliste.data.StudentDbHelper;

import butterknife.BindView;
import butterknife.ButterKnife;

public class StudentActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {



    // Identifier
    private static final int STUDENT_LOADER = 0;

    // Adapter für die ListView
    StudentCursorAdapter mCursorAdapter;


    @BindView(R.id.fab) FloatingActionButton button;

    public StudentActivity(){

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student);

        // Einrichten Floating Action Button, um Student Editor zu öffnen
        //View mit Butterknife binden
        ButterKnife.bind(this);
        findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(StudentActivity.this, StudentEditor.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                intent.putExtra(StudentEditor.EXTRA_RECT, createRect(button));
                startActivity(intent);
            }
        });

        // ListView, wird von Studentendaten gefüllt
        ListView studentListView = (ListView) findViewById(R.id.list);

        // Suchen und setzen der leeren Ansicht in der ListView, so dass sie nur angezeigt wird, wenn die Liste keine Einträge enthält
        View emptyView = findViewById(R.id.empty_view);
        studentListView.setEmptyView(emptyView);

        //Einrichtung Adapter, um für jede Zeile der Studentendaten im Cursor ein Listenelement zu erstellen
        // Es sind noch keine Studentendaten vorhanden (bis der Loader fertig ist), also Eigabe null für den Cursor
        mCursorAdapter = new StudentCursorAdapter(this, null);
        studentListView.setAdapter(mCursorAdapter);


        // Einrichten von item click listener
        studentListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                Intent intent = new Intent(StudentActivity.this, StudentDetails.class);

                // Den Content-URI bilden, der den bestimmten Studenten darstellt, auf den geklickt wurde,
                // durch Anhängen der "id" (Übergabe als Eingabe in die Methode) an
                // {@link StudentEntry#CONTENT_URI}.
                // Z.B.: Die URI wäre "content://com.example.lalitha.studentenliste/studenten/2"
                // falls der Student mit der ID 2 angeklickt wird.
                Uri aktuellerStudentUri = ContentUris.withAppendedId(StudentContract.StudentEntry.CONTENT_URI, id);

                // Setzen der URI auf das Datenfeld des Intents
                intent.setData(aktuellerStudentUri);
                intent.putExtra(StudentEditor.EXTRA_RECT, createRect(button));

                startActivity(intent);

            }
        });


        studentListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id){
                // Neuer Intent um zu {@link StudentEditor} gelangen
                Intent intent = new Intent(StudentActivity.this, StudentEditor.class);

                Uri aktuellerStudentUri = ContentUris.withAppendedId(StudentContract.StudentEntry.CONTENT_URI, id);

                // Setzen der URI auf das Datenfeld des Intents
                intent.setData(aktuellerStudentUri);
                intent.putExtra(StudentEditor.EXTRA_RECT, createRect(button));

                // Launchen von {@link StudentEditor} um die Daten für den aktuellen Studenten anzuzeigen
                startActivity(intent);

                return true;

            }
        });

        // Loader starten
        getLoaderManager().initLoader(STUDENT_LOADER, null, this);

    }

    private Rect createRect(View view) {
        Rect rect = new Rect();
        view.getDrawingRect(rect);
        ((ViewGroup) view.getParent()).offsetDescendantRectToMyCoords(view, rect);
        return rect;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Menü füllen, dadurch werden der Action Bar Elemente hinzugefügt, sofern diese vorhanden sind
        getMenuInflater().inflate(R.menu.menu_catalog, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_search:
                onSearchRequested();
                return true;
            default:
                return false;
        }
    }


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Definition der Projektionen (für uns relevante Spalten)
        String[] projection = {
                StudentContract.StudentEntry._ID,
                StudentContract.StudentEntry.COLUMN_VORNAME,
                StudentContract.StudentEntry.COLUMN_NACHNAME,
                StudentContract.StudentEntry.COLUMN_FACHBEREICH,
                StudentContract.StudentEntry.COLUMN_STUDIENGANG,
                StudentContract.StudentEntry.COLUMN_WOHNORT,
                StudentContract.StudentEntry.COLUMN_TELEFON,
                StudentContract.StudentEntry.COLUMN_EMAIL
        };

        // Dieser Loader führt die Abfragemethode des ContentProvider in einem Hintergrundthread aus
        return new CursorLoader(this,
                StudentContract.StudentEntry.CONTENT_URI,   // Provider content URI zum abfragen
                projection,             // Spalten, die in den resultierenden Cursor eingefügt werden sollen
                null,                   // Keine selection clause
                null,                   // Keine selection arguments
                null);                  // sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Update {@link StudentCursorAdapter} mit neuen Cursor mit aktualisierten Studentendaten
        mCursorAdapter.swapCursor(cursor);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        // Callback wird aufgerufen, wenn Daten gelöscht werden müssen
        mCursorAdapter.swapCursor(null);

    }
}
