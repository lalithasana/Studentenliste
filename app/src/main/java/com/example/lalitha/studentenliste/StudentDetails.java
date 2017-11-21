package com.example.lalitha.studentenliste;

/**
 * Created by Lalitha on 01.11.2017.
 */

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.lalitha.studentenliste.data.StudentContract;

public class StudentDetails extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

private Uri mAktuellerStudentUri;


private TextView mVorName;
private TextView mNachName;
private TextView mFachbereich;
private TextView mStudiengang;
private TextView mWohnort;
private TextView mTelefon;
private TextView mEmail;
private TextView mGeschlecht;
private ImageView imageView;



@Override
protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.student_details);

        Intent intent = getIntent();
        mAktuellerStudentUri = intent.getData();

        // Wenn der intent KEINE student content URI enthält, wissen wir, dass wir keine Details haben
        if (mAktuellerStudentUri == null) {

        Toast.makeText(this, " Keine Studentendaten vorhanden", Toast.LENGTH_SHORT).show();
        } else {

        // Initialisiere einen Loader, um die Studentendaten aus der Datenbank zu lesen
        // und zeige die aktuellen Werte im Editor an
        getSupportLoaderManager().initLoader(0, null, this);}

        mVorName = (TextView) findViewById(R.id.student_vorname);
        mNachName = (TextView) findViewById(R.id.student_nachname);
        mFachbereich = (TextView) findViewById(R.id.student_fachbereich);
        mStudiengang = (TextView) findViewById(R.id.student_studiengang);
        mWohnort = (TextView) findViewById(R.id.student_wohnort);
        mTelefon = (TextView) findViewById(R.id.student_telefon);
        mEmail = (TextView) findViewById(R.id.student_email);
        mGeschlecht = (TextView) findViewById(R.id.student_geschlecht);
        imageView = (ImageView) findViewById(R.id.profileImageView);



        }

@Override
public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                StudentContract.StudentEntry._ID,
                StudentContract.StudentEntry.COLUMN_VORNAME,
                StudentContract.StudentEntry.COLUMN_NACHNAME,
                StudentContract.StudentEntry.COLUMN_FACHBEREICH,
                StudentContract.StudentEntry.COLUMN_STUDIENGANG,
                StudentContract.StudentEntry.COLUMN_WOHNORT,
                StudentContract.StudentEntry.COLUMN_TELEFON,
                StudentContract.StudentEntry.COLUMN_IMAGE,
                StudentContract.StudentEntry.COLUMN_EMAIL,
                StudentContract.StudentEntry.COLUMN_STUDENT_GESCHLECHT,
        };

        // Dieser Loader führt die Abfragemethode des ContentProviders in einem Hintergrundthread aus
        return new CursorLoader(this,   // Parent activity Kontext
        mAktuellerStudentUri,         // Abfragen der Content-URI für den aktuellen Studenten
        projection,             // Spalten, die in den resultierenden Cursor eingefügt werden sollen
        null,                   // Keine selection clause
        null,                   // Keine selection arguments
        null);
        }

@Override
public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        if (cursor == null || cursor.getCount() < 1) {
        return;
        }

        // Fortfahren mit der Bewegung in die erste Zeile des Cursors  und auslesen der Daten
        // (Sollte die einzige Zeile im Cursor sein)
        if (cursor.moveToFirst()) {
        // Finden der Spalten mit den Studenten Attributen, die uns interessieren
        int vornameColumnIndex = cursor.getColumnIndex(StudentContract.StudentEntry.COLUMN_VORNAME);
        int nachnameColumnIndex = cursor.getColumnIndex(StudentContract.StudentEntry.COLUMN_NACHNAME);
        int fachbereichColumnIndex = cursor.getColumnIndex(StudentContract.StudentEntry.COLUMN_FACHBEREICH);
        int studiengangColumnIndex = cursor.getColumnIndex(StudentContract.StudentEntry.COLUMN_STUDIENGANG);
        int wohnortColumnIndex = cursor.getColumnIndex(StudentContract.StudentEntry.COLUMN_WOHNORT);
        int telefonColumnIndex = cursor.getColumnIndex(StudentContract.StudentEntry.COLUMN_TELEFON);
        int imageColumnIndex = cursor.getColumnIndex(StudentContract.StudentEntry.COLUMN_IMAGE);
        int emailColumnIndex = cursor.getColumnIndex(StudentContract.StudentEntry.COLUMN_EMAIL);
        int geschlechtColumnIndex = cursor.getColumnIndex(StudentContract.StudentEntry.COLUMN_STUDENT_GESCHLECHT);


        // Entnehmen des Wertes aus dem Cursor für den angegebenen Spaltenindex
        String vorName = cursor.getString(vornameColumnIndex);
        String nachName = cursor.getString(nachnameColumnIndex);
        String fachbereich = cursor.getString(fachbereichColumnIndex);
        String studiengang = cursor.getString(studiengangColumnIndex);
        String wohnort = cursor.getString(wohnortColumnIndex);
        String telefon = cursor.getString(telefonColumnIndex);
        byte[] image = cursor.getBlob(imageColumnIndex);
        String email = cursor.getString(emailColumnIndex);
        int geschlecht = cursor.getInt(geschlechtColumnIndex);


        // Update der views auf dem Screen mit den Werten aus der Datenbank
        setTitle(vorName);
        mVorName.setText(vorName);
        mNachName.setText(nachName);
        mFachbereich.setText(fachbereich);
        mStudiengang.setText(studiengang);
        mWohnort.setText(wohnort);
        mTelefon.setText(telefon);
        Bitmap bmp = BitmapFactory.decodeByteArray(image, 0, image.length);
        imageView.setImageBitmap(Bitmap.createScaledBitmap(bmp, 200,
        200, false));
        mEmail.setText(email);

        switch (geschlecht) {
        case StudentContract.StudentEntry.GESCHLECHT_MAENNLICH:
        mGeschlecht.setText("Männlich");
        break;
        case StudentContract.StudentEntry.GESCHLECHT_WEIBLICH:
        mGeschlecht.setText("Weiblich");
        break;
default:
        mGeschlecht.setText("Unbekannt");
        break;
        }
        }

        }

@Override
public void onLoaderReset(Loader<Cursor> loader) {

        }
        }
