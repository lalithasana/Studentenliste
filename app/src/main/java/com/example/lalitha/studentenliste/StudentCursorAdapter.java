package com.example.lalitha.studentenliste;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;
import com.example.lalitha.studentenliste.data.StudentContract;
/**
 * Created by Lalitha on 01.11.2017.
 */

        import com.example.lalitha.studentenliste.data.StudentContract;


/**
 * {@link StudentCursorAdapter} Ein Adapter für eine ListView oder GridView
 * Verwendet {@link Cursor} der Studentendaten als Datenquelle. Der Adapter weißt
 * wie er die list items für jede Reihe von Studenten füllen musst {@link Cursor}.
 */
public class StudentCursorAdapter  extends CursorAdapter {

    /**
     * Erstellt einen neuen {@link StudentCursorAdapter}.
     *
     * @param context Der Kontext
     * @param c       Der Cursor, vondem die Daten entnommen werden
     */
    public StudentCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /**
     * Erstellt eine neue leere Listenelementansicht. Zu den Ansichten sind noch keine Daten gesetzt (oder gebunden).
     *
     * @param context App Kontext
     * @param cursor  Der Cursor, von dem die Daten abgerufen werden sollen. Der Cursor ist bereits
     * in die richtige Position verschoben.
     * @param parent  Das übergeordnete Element, an das die neue Ansicht angehängt ist
     * @return neu erstellte list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        //Füllt eine Listenelementansicht mit dem in list_item.xml angegebenen Layout auf
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * Diese Methode bindet die Studentendaten (in der aktuellen Zeile, auf die der Cursor zeigt) an das angegebene
     * list item layout. Zum Beispiel kann der Name für den aktuellen Studenten auf den Namen TextView im list item layout gesetzt werden
     *
     * @param view    Vorhandene View, zuvor von newView () zurückgegeben
     * @param context App Kontext
     * @param cursor  Der Cursor, von dem die Daten abgerufen werden sollen. Der Cursor ist bereits
     *                  in der richtigen Reihe.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        //Suche einzelner Views, die wir im Listenelement-Layout ändern möchten
        TextView vornameTextView = (TextView) view.findViewById(R.id.vorname);
        TextView nachnameTextView = (TextView) view.findViewById(R.id.nachname);
        TextView fachbereichTextView = (TextView) view.findViewById(R.id.fachbereich);

        // Suchen der Studenten Attribute
        int vornameColumnIndex = cursor.getColumnIndex(StudentContract.StudentEntry.COLUMN_VORNAME);
        int nachnameColumnIndex = cursor.getColumnIndex(StudentContract.StudentEntry.COLUMN_NACHNAME);
        int fachbereichColumnIndex = cursor.getColumnIndex(StudentContract.StudentEntry.COLUMN_FACHBEREICH);

        // Lesen der Studenten Attribute aus dem Cursor von dem aktuellen Student
        String studentVorName = cursor.getString(vornameColumnIndex);
        String studentNachName = cursor.getString(nachnameColumnIndex);
        String studentFachbereich = cursor.getString(fachbereichColumnIndex);


        // Ist der Vorname des Studenten leer or null, dann ercheint in der TextView "Unbekannt",
        //damit kein leeres Feld angezeigt wird
        if (TextUtils.isEmpty(studentVorName)) {
            studentVorName = context.getString(R.string.unbekannt);
        }

        if (TextUtils.isEmpty(studentFachbereich)) {
            studentFachbereich = context.getString(R.string.unbekannt);
        }

        // Aktualisierung der TextView mit den neuen Studenten Daten

        vornameTextView.setText(studentVorName);
        nachnameTextView.setText(studentNachName);
        fachbereichTextView.setText(studentFachbereich);

    }
}
