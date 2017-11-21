package com.example.lalitha.studentenliste.data;

/**
 * Created by Lalitha on 02.11.2017.
 */
import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

import java.util.HashMap;

public class StudentProvider extends ContentProvider{
    /** Tag für die log messages */
    public static final String LOG_TAG = StudentProvider.class.getSimpleName();

    /** URI matcher code für die content URI für die Studententabelle */
    private static final int STUDENTEN = 100;

    /** URI matcher code für die content URI für einen einzelnen Studenten in der Tabelle */
    private static final int STUDENT_ID = 101;

    private static final int SEARCH_SUGGEST = 102;

    private static final HashMap<String, String> SEARCH_SUGGEST_PROJECTION_MAP;
    static {
        SEARCH_SUGGEST_PROJECTION_MAP = new HashMap<String, String>();
        SEARCH_SUGGEST_PROJECTION_MAP.put(StudentContract.StudentEntry._ID, StudentContract.StudentEntry._ID);
        SEARCH_SUGGEST_PROJECTION_MAP.put(SearchManager.SUGGEST_COLUMN_TEXT_1, StudentContract.StudentEntry.COLUMN_VORNAME + " AS "   + SearchManager.SUGGEST_COLUMN_TEXT_1);
        SEARCH_SUGGEST_PROJECTION_MAP.put(SearchManager.SUGGEST_COLUMN_TEXT_2, StudentContract.StudentEntry.COLUMN_NACHNAME + " AS "    + SearchManager.SUGGEST_COLUMN_TEXT_2);
        SEARCH_SUGGEST_PROJECTION_MAP.put(SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID, StudentContract.StudentEntry._ID + " AS " + SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID);
    }


    /**
     * UriMatcher object um eine content URI mit einem entsprechendem Code abzugleichen
     * Die an den Konstruktor übergebene Eingabe stellt den Code dar, der für die root-URI zurückgegeben werden soll
     * Es ist üblich, NO_MATCH als Eingabe für diesen Fall zu verwenden
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    public static final Uri SEARCH_SUGGEST_URI = Uri.parse("content://" + StudentContract.CONTENT_AUTHORITY + "/" + StudentContract.PATH_STUDENTEN + "/" + SearchManager.SUGGEST_URI_PATH_QUERY);


   // Static initializer
    static {
        // Die Aufrufe von addURI() kommen hier hin, für alle content URI patterns die der provider erkennen soll
       // Alle zum UriMatcher hinzugefügten Pfade haben einen entsprechenden Code als Rückgabe
        // wenn eine Übereinstimmung gefunden wurde

        // Die content URI der Form "content://com.example.android.studenten/studenten" bildet ab auf den
        // integer code {@link #STUDENT}. Diese URI wird benutzt um den Zugang zu mehreren Zeilen der Studententabelle bereit zu stellen
        sUriMatcher.addURI(StudentContract.CONTENT_AUTHORITY, StudentContract.PATH_STUDENTEN, STUDENTEN);

        // Die content URI der Form "content://com.example.android.studenten/studenten" bildet ab auf den
       // integer code  {@link #STUDENT_ID}. Diese URI wird benutzt um den Zugang zu einer einzigen Zeile in der Tabelle bereit zu ermöglichen

        sUriMatcher.addURI(StudentContract.CONTENT_AUTHORITY, StudentContract.PATH_STUDENTEN + "/#", STUDENT_ID);

    }

    public StudentProvider(){
        sUriMatcher.addURI(StudentContract.CONTENT_AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY, SEARCH_SUGGEST);
        sUriMatcher.addURI(StudentContract.CONTENT_AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY + "/*", SEARCH_SUGGEST);

    }


    private StudentDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new StudentDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {

        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // Cursor enthält das Ergebnis der Abfrage
        Cursor cursor = null;

        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(StudentContract.StudentEntry.TABLE_NAME);

        // Herausfinden, ob der URI matcher die URI einem bestimmten Code zuordnen kann
        int match = sUriMatcher.match(uri);
        switch (match) {
            case SEARCH_SUGGEST:
                selectionArgs = new String[] { "%" + selectionArgs[0] + "%", "%" + selectionArgs[0] + "%" };
                queryBuilder.setProjectionMap(SEARCH_SUGGEST_PROJECTION_MAP);

                cursor = queryBuilder.query(database, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case STUDENTEN:
                // Für den student code, abfragen der studenten tabelle mit der geg.
                // projection, selection, selection arguments und sort order. Der Cursor könnte
                // mehrer Zeilen der Studenten Tabelle enthalten
                cursor = database.query(StudentContract.StudentEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case STUDENT_ID:
                // Für den student_ID code, entnehme die ID aus der URI
                // Zum Beisiel eine URI wie "content://com.example.android.studenten/studenten/3",
                // die selection ist "_id=?" und die selection argument ist ein
                // String array, die in diesem Fall die String ID 3 enthält
                //
                // Für jedes "?" in der selection, braucht man ein Element in der selection
                // arguments welches das "?" füllt. Da wir ein Fragezeichen selection haben,
                // haben wir 1 String in der von selection arguments String array.
                selection = StudentContract.StudentEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                // Startet eine Abfrage auf die Studententabelle wo die _id equals 3 ist um einen Cursor zurück zu geben,
                // die die Zeile in der Tabelle enthält
                cursor = database.query(StudentContract.StudentEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        // Setzt eine Benachrichtigungs URI auf den Cursor
        // Ändern sich die Daten dieser URI, weiß man ob der Cursor aktualisiert werden muss
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        // Gibt Cursor zurück
        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case STUDENTEN:
                return insertStudent(uri, contentValues);

            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Hinzufügen eines Studenten in die Datenbank. Gibt neue content URI zurück
     * für die aktuelle Zeile in der Datenbank
     */
    private Uri insertStudent(Uri uri, ContentValues values) {

        String vorname = values.getAsString(StudentContract.StudentEntry.COLUMN_VORNAME);
        if (vorname == null) {
            throw new IllegalArgumentException("Student braucht einen Vornamen!");
        }

        String nachname = values.getAsString(StudentContract.StudentEntry.COLUMN_NACHNAME);
        if (nachname == null) {
            throw new IllegalArgumentException("Student braucht einen Nachnamen!");
        }

        String fachbereich = values.getAsString(StudentContract.StudentEntry.COLUMN_FACHBEREICH);
        if (fachbereich == null) {
            throw new IllegalArgumentException("Student musst einem Fachbereich angehören!");
        }


        String wohnort = values.getAsString(StudentContract.StudentEntry.COLUMN_WOHNORT);
        if (wohnort == null) {
            throw new IllegalArgumentException("Student musst irgendwo wohnen!");
        }

        String telefon = values.getAsString(StudentContract.StudentEntry.COLUMN_TELEFON);
        if (telefon == null) {
            throw new IllegalArgumentException("Student braucht eine Telefonnummer!");
        }

        String email = values.getAsString(StudentContract.StudentEntry.COLUMN_EMAIL);
        if (email == null) {
            throw new IllegalArgumentException("Student braucht eine E-Mail Adresse!");
        }

        Integer geschlecht = values.getAsInteger(StudentContract.StudentEntry.COLUMN_STUDENT_GESCHLECHT);
        if (geschlecht == null || !StudentContract.StudentEntry.isValidGender(geschlecht)) {
            throw new IllegalArgumentException("Student braucht ein gültiges Geschlecht!");
        }


        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Fügt neuen Studenten mit den übergegebenen Werten hinzu
        long id = database.insert(StudentContract.StudentEntry.TABLE_NAME, null, values);
        // Ist die ID  -1, dann schlug das Hinzufügen fehl
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Informiert alle listeners das die Daten sich für die student content URI geändert haben
        getContext().getContentResolver().notifyChange(uri, null);

        // Gibt die neue URI mit der ID (der neu eingefügten Zeile) am Ende angehängt zurück
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case STUDENTEN:
                return updateStudent(uri, contentValues, selection, selectionArgs);
            case STUDENT_ID:
                // Für den student_ID code -> entnehmen der ID aus der URI,
                // um zu wissen welche Zeilen von einem Update betroffen sind. Selection ist_id=?" und selection
                // arguments ist ein String array, die die aktuelle ID beinhaltet
                selection = StudentContract.StudentEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updateStudent(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Update der Studenten in der DB mit den gegebenen Werten.
     * Gibt Anzahl der erfolgreich upgedateten Zeilen zurück
     */
    private int updateStudent(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        if (values.containsKey(StudentContract.StudentEntry.COLUMN_VORNAME)) {
            String vorname = values.getAsString(StudentContract.StudentEntry.COLUMN_VORNAME);
            if (vorname == null) {
                throw new IllegalArgumentException("Student braucht einen Vornamen!");
            }
        }

        if (values.containsKey(StudentContract.StudentEntry.COLUMN_NACHNAME)) {
            String nachname = values.getAsString(StudentContract.StudentEntry.COLUMN_NACHNAME);
            if (nachname == null) {
                throw new IllegalArgumentException("Student braucht einen Nachnamen!");
            }
        }

        if (values.containsKey(StudentContract.StudentEntry.COLUMN_FACHBEREICH)) {
            String fachbereich = values.getAsString(StudentContract.StudentEntry.COLUMN_FACHBEREICH);
            if (fachbereich == null) {
                throw new IllegalArgumentException("Student musst einem Fachbereich angehören!");
            }
        }

        if (values.containsKey(StudentContract.StudentEntry.COLUMN_STUDIENGANG)) {
            String studiengang = values.getAsString(StudentContract.StudentEntry.COLUMN_STUDIENGANG);
            if (studiengang == null) {
                throw new IllegalArgumentException("Student musst etwas studieren!");
            }
        }

        if (values.containsKey(StudentContract.StudentEntry.COLUMN_WOHNORT)) {
            String wohnort = values.getAsString(StudentContract.StudentEntry.COLUMN_WOHNORT);
            if (wohnort == null) {
                throw new IllegalArgumentException("Student musst irgendwo wohnen!");
            }
        }

        if (values.containsKey(StudentContract.StudentEntry.COLUMN_TELEFON)) {
            String telefon = values.getAsString(StudentContract.StudentEntry.COLUMN_TELEFON);
            if (telefon == null) {
                throw new IllegalArgumentException("Student braucht eine Telefonnummer!");
            }
        }

        if (values.containsKey(StudentContract.StudentEntry.COLUMN_EMAIL)) {
            String email = values.getAsString(StudentContract.StudentEntry.COLUMN_EMAIL);
            if (email == null) {
                throw new IllegalArgumentException("Student braucht eine E-Mail Adresse!");
            }
        }

        // Falls der Schlüssel{@link studentEntry#COLUMN_STUDENT_GESCHLECHT} vorhanden ist,
        // wird überprüft ob der Wert des Geschlechts gültig ist
        if (values.containsKey(StudentContract.StudentEntry.COLUMN_STUDENT_GESCHLECHT)) {
            Integer geschlecht = values.getAsInteger(StudentContract.StudentEntry.COLUMN_STUDENT_GESCHLECHT);
            if (geschlecht == null || !StudentContract.StudentEntry.isValidGender(geschlecht)) {
                throw new IllegalArgumentException("Student musst ein Geschlecht haben!");
            }
        }

        // Falls es keine Werte zum updaten gibt, dann findet auch kein update statt
        if (values.size() == 0) {
            return 0;
        }

        //um die Daten zu updaten
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Führt den Update auf der DB aus und holt sich die Anzahl der betroffenen Zeilen
        int rowsUpdated = database.update(StudentContract.StudentEntry.TABLE_NAME, values, selection, selectionArgs);

        //  Wenn 1 oder mehrere Zeilen gelöscht wurden, dann werden alle listeners informiert, dessen Daten durch die gegebene URI sich geändert haben
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Gibt Anzahl der aktualisierten Zeilen wieder
        return rowsUpdated;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Tracken der Anzahl der gelöschten Zeilen
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case STUDENTEN:
                // Löscht alle Zeilen
                rowsDeleted = database.delete(StudentContract.StudentEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case STUDENT_ID:
                // Löscht eine Zeile
                selection = StudentContract.StudentEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowsDeleted = database.delete(StudentContract.StudentEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        // Wenn 1 oder mehrere Zeilen gelöscht wurden, dann werden alle listeners  informiert, dessen Daten durch die gegebene URI sich geändert haben

        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Gibt die Anzahl der gelöschten Zeilen zurück
        return rowsDeleted;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case STUDENTEN:
                return StudentContract.StudentEntry.CONTENT_LIST_TYPE;
            case STUDENT_ID:
                return StudentContract.StudentEntry.CONTENT_ITEM_TYPE;
            case SEARCH_SUGGEST:
                return null;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}


