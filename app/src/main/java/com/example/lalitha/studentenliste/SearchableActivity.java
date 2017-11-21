package com.example.lalitha.studentenliste;

/**
 * Created by Lalitha on 01.11.2017.
 */
import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.lalitha.studentenliste.data.StudentContract;
import com.example.lalitha.studentenliste.data.StudentProvider;

import java.util.Timer;
import java.util.TimerTask;

public class SearchableActivity extends ListActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        Intent intent = getIntent();

        checkIntent(intent);
    }

    @Override
    protected void onNewIntent(Intent newIntent) {
        // aktualisieren und überprüfen des Intents zum Starten der Activity
        setIntent(newIntent);

        checkIntent(newIntent);
    }


    private void checkIntent(Intent intent) {
        String query = "";
        String intentAction = intent.getAction();
        if (Intent.ACTION_SEARCH.equals(intentAction)) {
            query = intent.getStringExtra(SearchManager.QUERY);
        } else if (Intent.ACTION_VIEW.equals(intentAction)) {

            Uri details = intent.getData();
            Intent detailsIntent = new Intent(Intent.ACTION_VIEW, details);
            startActivity(detailsIntent);

        }
        fillList(query);
    }

    private void fillList(String query) {

        String wildcardQuery = "%" + query + "%";

        Cursor cursor = getContentResolver().query(
                StudentContract.StudentEntry.CONTENT_URI,
                null,
                StudentContract.StudentEntry.COLUMN_VORNAME + " LIKE ? OR " + StudentContract.StudentEntry.COLUMN_NACHNAME + " LIKE ?",
                new String[] { wildcardQuery, wildcardQuery },
                null);


        if(cursor.getCount() == 0){
            Toast.makeText(this, " KEIN ERGEBNIS " , Toast.LENGTH_LONG).show();
            int timeout = 6000; // Activity für 8 Sek. sichtbar machen

            Timer timer = new Timer();
            timer.schedule(new TimerTask() {

                @Override
                public void run() {
                    finish();
                    Intent intent = new Intent(SearchableActivity.this, StudentActivity.class);
                    startActivity(intent);
                }
            }, timeout);
        }

        ListAdapter adapter = new SimpleCursorAdapter(
                this,
                android.R.layout.simple_list_item_2,
                cursor,
                new String[] { StudentContract.StudentEntry.COLUMN_VORNAME, StudentContract.StudentEntry.COLUMN_NACHNAME },
                new int[] { android.R.id.text1, android.R.id.text2 },
                0);

        setListAdapter(adapter);
    }

    @Override
    protected void onListItemClick(ListView l, View view, int position, long id) {
        Intent intent = new Intent(SearchableActivity.this, StudentDetails.class);

        Uri details = Uri.withAppendedPath(StudentContract.StudentEntry.CONTENT_URI, "" + id);
        intent.setData(details);
        startActivity(intent);

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_catalog, menu);

        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {

            // Aufruf erfolgt, wenn der Home (Up) Button gedrückt ist in der Action Bar.

            Intent parentActivityIntent = new Intent(this, StudentActivity.class);
            parentActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(parentActivityIntent);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);

    }

}

