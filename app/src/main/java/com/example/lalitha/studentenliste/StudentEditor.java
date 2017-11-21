package com.example.lalitha.studentenliste;
import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.example.lalitha.studentenliste.data.StudentContract;
import com.hendraanggrian.bundler.BindExtra;
import com.hendraanggrian.bundler.Bundler;
import com.hendraanggrian.kota.content.Themes;
import com.hendraanggrian.reveallayout.Radius;
import com.hendraanggrian.reveallayout.RevealableLayout;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import butterknife.BindView;
import butterknife.ButterKnife;
/**
 * Created by Lalitha on 01.11.2017.
 */

public class StudentEditor extends AppCompatActivity implements View.OnClickListener,
        LoaderManager.LoaderCallbacks<Cursor> {


     //Identifier

    private static final int EXISTING_STUDENT_LOADER = 0;

     //Content URI für den vorhandenen Studenten (null wennn es ein neuer Student ist)

    private Uri mAktuellerStudentUri;
    //EditText felder

    private EditText mVorNameEditText;
    private EditText mNachNameEditText;
    private EditText mFachbereich;
    private EditText mStudiengang;
    private EditText mWohnort;
    private EditText mTelefon;
    private EditText mEmail;


    private Spinner mGeschlechtSpinner;


    private ImageView profileImageView;
    private Button pickImage;

    private static final int SELECT_PHOTO = 1;
    private static final int CAPTURE_PHOTO = 2;

    private ProgressDialog progressBar;
    private int progressBarStatus = 0;
    private Handler progressBarbHandler = new Handler();
    private boolean hasImageChanged = false;
    Bitmap thumbnail;

 //Geschlecht
    private int mGeschlecht = StudentContract.StudentEntry.GESCHLECHT_UNBEKANNT;



     // Boolesche Markierung, die festhält, ob der Student bearbeitet wurde (true) oder nicht (false)

    private boolean mStudentHasChanged = false;


    View rootLayout;

    public static final String EXTRA_RECT = "com.example.lalitha.studentenliste";
    @BindExtra(EXTRA_RECT)
    Rect rect;

    @BindView(R.id.revealLayout)
    RevealableLayout revealLayout;

    @BindView(R.id.layout)
    ViewGroup layout;

    /**
     * OnTouchListener, der auf einen Benutzer wartet.
     * Wenn er eine View berührt, bedeutet dies, dass er die View ändert und wir ändern den booleschen Wert von mStudentHasChanged in true
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mStudentHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Bundler.bindExtras(this);
        ButterKnife.bind(this);


        // Untersuchung des intents, die zum starten dieser Activity verwendet wurde,
        // um herauszufinden ob ein neuer Student erstellt wurde oder ein bereits vorhandener bearbeitet wird
        Intent intent = getIntent();
        mAktuellerStudentUri = intent.getData();

        // Wenn Intent keine student content URI beinhaltet, dann wissen wir, dass wir einen neuen Studenten erstellen
        if (mAktuellerStudentUri == null) {
            // Das ist ein neuer Student, deswegen ändert die App die App Bar und sagt "Student hinzufügen"
            setTitle(getString(R.string.editor_activity_title_new_student));

            // Option ,,Löschen" ungültig machen, noch nicht vorhandener Student kann nicht gelöscht werden
            invalidateOptionsMenu();
        } else {
            // Ein vorhandener Student, App Bar: "Student bearbeiten"
            setTitle(getString(R.string.editor_activity_title_edit_student));

            // Initialisierung eines Loaders um Studendaten von der DB auszulesen
            // und die aktuellen Werte im Editor anzuzeigen
            getLoaderManager().initLoader(EXISTING_STUDENT_LOADER, null, this);
        }

        // alle relevanten Views finden, die wir benötigen, um Benutzereingaben zu bekommen

        mVorNameEditText = (EditText) findViewById(R.id.edit_student_vorname);
        mNachNameEditText = (EditText) findViewById(R.id.edit_student_nachname);
        mFachbereich = (EditText) findViewById(R.id.edit_student_fachbereich);
        mStudiengang = (EditText) findViewById(R.id.edit_student_studiengang);
        mWohnort = (EditText) findViewById(R.id.edit_student_wohnort);
        mTelefon = (EditText) findViewById(R.id.edit_student_telefon);
        mEmail = (EditText) findViewById(R.id.edit_student_email);
        mGeschlechtSpinner = (Spinner) findViewById(R.id.spinner_geschlecht);


        // Setup OnTouchListeners auf alle Input Felder, damit wir wissen, ob der Nutzer
        // sie berührt oder verändert hat. Wir wissen dann ob nicht gespeicherte Änderungen gemacht wurden
        // oder nicht, wennd er NUtzer den Editor ohne zu speichern verlässt
        mVorNameEditText.setOnTouchListener(mTouchListener);
        mNachNameEditText.setOnTouchListener(mTouchListener);
        mFachbereich.setOnTouchListener(mTouchListener);
        mStudiengang.setOnTouchListener(mTouchListener);
        mWohnort.setOnTouchListener(mTouchListener);
        mTelefon.setOnTouchListener(mTouchListener);
        mEmail.setOnTouchListener(mTouchListener);
        mGeschlechtSpinner.setOnTouchListener(mTouchListener);

        setupSpinner();

        rootLayout = findViewById(R.id.root_layout);

        layout.post(new Runnable() {
            @Override
            public void run() {
                Animator animator = revealLayout.reveal(layout, rect.centerX(), rect.centerY(), Radius.GONE_ACTIVITY);
                animator.setDuration(1000);
                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (Build.VERSION.SDK_INT >= 21) {
                            getWindow().setStatusBarColor(Themes.getColor(getTheme(), R.attr.colorAccent, true));
                        }
                    }
                });
                animator.start();
            }
        });


        profileImageView = (ImageView) findViewById(R.id.profileImageView);
        pickImage = (Button) findViewById(R.id.pick_image);

        pickImage.setOnClickListener(this);

        if (ContextCompat.checkSelfPermission(StudentEditor.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            profileImageView.setEnabled(false);
            ActivityCompat.requestPermissions(StudentEditor.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        } else {
            profileImageView.setEnabled(true);
        }


    }


    //Dropdown Spinner
    private void setupSpinner() {
        // Adapter für den Spinner erstellen. die list options sind vom String array, welche es benutzen wird
        // Der Spinner benutzt das default layout
        ArrayAdapter geschlechtSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_geschlecht_options, android.R.layout.simple_spinner_item);

        //  dropdown layout style - simple list view mit 1 item pro Zeile
        geschlechtSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Anwendung des Adapters auf den Spinner
        mGeschlechtSpinner.setAdapter(geschlechtSpinnerAdapter);

        // Setzt integer mSelected auf die Werte
        mGeschlechtSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.geschlecht_maennlich))) {
                        mGeschlecht = StudentContract.StudentEntry.GESCHLECHT_MAENNLICH;
                    } else if (selection.equals(getString(R.string.geschlecht_weiblich))) {
                        mGeschlecht = StudentContract.StudentEntry.GESCHLECHT_WEIBLICH;
                    } else {
                        mGeschlecht = StudentContract.StudentEntry.GESCHLECHT_UNBEKANNT;
                    }
                }
            }

            // Da AdapterView eine abstrakte Klasse ist, musst onNothingSelected definiert werden
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mGeschlecht = StudentContract.StudentEntry.GESCHLECHT_UNBEKANNT;
            }
        });
    }

    /**
     * Benutzereingaben vom Editor erhalten und Studenten in der Datenbank speichern
     */
    private void saveStudent() {
        // Lesen von Input Feldern
        // Benutzen von trim um White Space zu entfernen
        String vornameString = mVorNameEditText.getText().toString().trim();
        String nachnameString = mNachNameEditText.getText().toString().trim();
        String fachbereichString = mFachbereich.getText().toString().trim();
        String studiengangString = mStudiengang.getText().toString().trim();
        String wohnortString = mWohnort.getText().toString().trim();
        String telefonString = mTelefon.getText().toString().trim();
        String emailString = mEmail.getText().toString().trim();


        profileImageView.setDrawingCacheEnabled(true);
        profileImageView.buildDrawingCache();
        Bitmap bitmap = profileImageView.getDrawingCache();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();


        // Überprüfen ob es ein neuer Student werden soll
        // und überprüfen ob alle Felder im Editor leer sind
        if (mAktuellerStudentUri == null &&
                TextUtils.isEmpty(vornameString) && TextUtils.isEmpty(nachnameString) &&
                TextUtils.isEmpty(fachbereichString) && TextUtils.isEmpty(studiengangString) &&
                TextUtils.isEmpty(wohnortString) && TextUtils.isEmpty(telefonString) &&
                TextUtils.isEmpty(emailString) && mGeschlecht == StudentContract.StudentEntry.GESCHLECHT_UNBEKANNT) {
            // Wenn keine Felder verändert wurden, Rückkehr ohne die Erstellung eines neuen Studenten

            return;
        }

        // Erzeugung eines ContentValues object bei der die Spaltennamen keys sind,
        // und die Studenten Attribute aus dem Editor sind die Werte
        ContentValues values = new ContentValues();
        values.put(StudentContract.StudentEntry.COLUMN_VORNAME, vornameString);
        values.put(StudentContract.StudentEntry.COLUMN_NACHNAME, nachnameString);
        values.put(StudentContract.StudentEntry.COLUMN_FACHBEREICH, fachbereichString);
        values.put(StudentContract.StudentEntry.COLUMN_STUDIENGANG, studiengangString);
        values.put(StudentContract.StudentEntry.COLUMN_WOHNORT, wohnortString);
        values.put(StudentContract.StudentEntry.COLUMN_TELEFON, telefonString);
        values.put(StudentContract.StudentEntry.COLUMN_IMAGE, data);
        values.put(StudentContract.StudentEntry.COLUMN_EMAIL, emailString);

        values.put(StudentContract.StudentEntry.COLUMN_STUDENT_GESCHLECHT, mGeschlecht);



        // Überprüfung anhand der mCurrentStudentUri, ob es ein neuer Student ist oder ein bereits vorhandener
        if (mAktuellerStudentUri == null) {
            // Neuer Student also hinzufügen in den provider,
            // content URI für den neuen Student zurückgeben
            Uri newUri = getContentResolver().insert(StudentContract.StudentEntry.CONTENT_URI, values);

            // Toast ob das Hinzufügen erfolgreich war
            if (newUri == null) {
                // Ist die neue Content URI gleich null, gab es beim Hinzufügen ein Problem
                Toast.makeText(this, getString(R.string.editor_insert_student_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Hinzufügen war erfolgreich
                Toast.makeText(this, getString(R.string.editor_insert_student_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            // Ein vorhandener Student, aktualisieren der content URI: mCurrentStudentUri
            // und die neuen ContentValues übergeben. Übergabe null für die selection und selection args,
            // da mCurrentStudentUri bereits die richtige Zeile in der Datenbank ausgewählt hat, die verändert werden soll
            int rowsAffected = getContentResolver().update(mAktuellerStudentUri, values, null, null);

            // Toast, op das Update erfolgreich war oder nicht
            if (rowsAffected == 0) {
                // Wenn keine Zeile betroffen war, Error
                Toast.makeText(this, getString(R.string.editor_update_student_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Update war erfolgreich -> Toast
                Toast.makeText(this, getString(R.string.editor_update_student_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Füllt die menu options von der res/menu/menu_editor.xml file auf.
        // Fügt Menü Items der App Bar hinzu
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    /**
     * Aufruf dieser Methode nach invalidateOptionsMenu(), so das das Menü aktualisiert
     * werden kann(einige menu items können versteckt werden).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // Ist dies ein neuer Student, "Löschen" nicht sichtbar
        if (mAktuellerStudentUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Nutzer klickt auf menu option in der app bar
        switch (item.getItemId()) {

            case R.id.action_save:
                // Speichert den Studenten
                saveStudent();
                // Verlassen der activity
                finish();
                return true;
            // Klick auf "Löschen" menu option
            case R.id.action_delete:
                // Pop up Bestätigung für das Löschen
                showDeleteConfirmationDialog();
                return true;

            case android.R.id.home:
                // Hat sich der Student nicht geändert, fortfahren mit hoch navigieren zur StudentActivity

                if (!mStudentHasChanged) {
                    NavUtils.navigateUpFromSameTask(StudentEditor.this);
                    return true;
                }

                // Gibt es nicht gespeicherte Änderungen, erscheint ein Dialog, die den Nutzer warnt.

                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // Nutzer klickt "Löschen" button, navigieren zur parent activity.
                                NavUtils.navigateUpFromSameTask(StudentEditor.this);
                            }
                        };

                // Anzeige Dialog, das nicht gespeicherte Änderungen vorgenommen wurden
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Diese Methode wird aufgerufen, wenn der Zurück Button geklickt wird
     */
    @Override
    public void onBackPressed() {

        Animator animator = revealLayout.reveal(layout, rect.centerX(), rect.centerY(), Radius.ACTIVITY_GONE);
        animator.setDuration(1000);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (Build.VERSION.SDK_INT >= 21) {
                    getWindow().setStatusBarColor(Themes.getColor(getTheme(), R.attr.colorPrimaryDark, true));
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                layout.setVisibility(View.INVISIBLE);
                finish();
                overridePendingTransition(0, 0);
            }
        });
        animator.start();
        // Hat sich der Student nicht verändert, wieder zurück
        if (!mStudentHasChanged) {
            super.onBackPressed();
            return;
        }

        // Wurde etwas verändert und nicht gespeichert,erscheint ein Warndialog für den Nutzer
        // Erzeugung eines click listeners, Änderungen werden verworfen (wenn Nutzer dies auswählt)
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Nutzer klickt "Löschen" button, beendet aktuelle  Activity.
                        finish();
                    }
                };

        // SDialog erscheint, das es nicht gespeicherte Änderungen gibt
        showUnsavedChangesDialog(discardButtonClickListener);


    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // der editor zeigt alle Studenten Attribute an. Definition einer Projektion, welche alle Spalten der Studententabelle enthält
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

        // Dieser Loader führt die Abfragemethode des ContentProvider in einem Hintergrundthread aus
        return new CursorLoader(this,   // Parent activity Kontext
                mAktuellerStudentUri,         // Abfrage content Uri von Student
                projection,             // Spalten, die in den resultierenden Cursor eingefügt werden sollen
                null,                   // Keine selection clause
                null,                   // Keine selection arguments
                null);                  // Sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Verschwindet, wenn cursor gleich null oder weniger als eine Zeile im Cursor ist
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Fortfahren mit der ersten Zeile des Cursors  und auslesen der Daten
        // (Dies sollte die einzige Zeile im Cursor sein)
        if (cursor.moveToFirst()) {
            // Finden der Studenten Attribute in den Spalten, die für uns interessant sind
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


            // Update der views auf dem screen mit den Werten aus der Datenbank
            mVorNameEditText.setText(vorName);
            mNachNameEditText.setText(nachName);
            mFachbereich.setText(fachbereich);
            mStudiengang.setText(studiengang);
            mWohnort.setText(wohnort);
            mTelefon.setText(telefon);
            Bitmap bmp = BitmapFactory.decodeByteArray(image, 0, image.length);
            profileImageView.setImageBitmap(Bitmap.createScaledBitmap(bmp, 200,
                    200, false));
            mEmail.setText(email);

            // Geschlecht ist als Dropdown Spinner umgesetzt
            // Aufruf der Methode setSelection() zeigt aktuelle Auswahl auf dem Screen an
            switch (geschlecht) {
                case StudentContract.StudentEntry.GESCHLECHT_MAENNLICH:
                    mGeschlechtSpinner.setSelection(1);
                    break;
                case StudentContract.StudentEntry.GESCHLECHT_WEIBLICH:
                    mGeschlechtSpinner.setSelection(2);
                    break;
                default:
                    mGeschlechtSpinner.setSelection(0);
                    break;
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Wenn der Loader ungültig ist, löschen aller Daten aus den Edit Feldern
        mVorNameEditText.setText("");
        mNachNameEditText.setText("");
        mFachbereich.setText("");
        mStudiengang.setText("");
        mWohnort.setText("");
        mTelefon.setText("");
        mEmail.setText("");
        mGeschlechtSpinner.setSelection(0); // Select "Unknown" gender
    }

    /**
     * Zeigt Dialog an, der den Nutzer auf nicht gespeicherte durchgeführte Änderungen hinweist
     * wenn er den Editor zur Bearbeitung der Daten verlassen will
     *
     * @param discardButtonClickListener der click listener dafür, wenn der Nutzer die Änderungen verwerfen will
     *
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Nutzer klickt auf ,,Bearbeiten", der DIalog verschwindet
                // und fährt mit der Bearbeitung fort
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Erzeugt und zeigt den AlertDialog an
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Aufforderung des Nutzer zur Bestätigung des Löschvorganges
     */
    private void showDeleteConfirmationDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // NUtzer klickt auf ,,Löschen", Student wird gelöscht
                deleteStudent();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Nutzer klickt auf ,,Abbrechen", Dialog verschwindet
                // und fährt mit der Bearbeitung fort
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Erzeugt und zeigt den AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


     // Student wird in der Datenbank gelöscht

    private void deleteStudent() {
        // Löschung erfolgt nur, wenn der Student vorhanden ist
        if (mAktuellerStudentUri != null) {
            // Ruft den ContentResolver auf um den Studenten anhand der content URI zu löschen
            // Übergabe von null an selection und selection args da die mAktuellerStudentUri
            // content URI identifiziert den betroffenen Studenten
            int rowsDeleted = getContentResolver().delete(mAktuellerStudentUri, null, null);

            // Toast, Löschen war erfolgreich oder nicht
            if (rowsDeleted == 0) {
                // falls keine Zeile gelöscht wurde -> Error
                Toast.makeText(this, getString(R.string.editor_delete_student_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Toast, Löschen war erfolgreich
                Toast.makeText(this, getString(R.string.editor_delete_student_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Beenden der activity
        finish();
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.pick_image:
                new MaterialDialog.Builder(this)
                        .title(R.string.uploadImages)
                        .items(R.array.uploadImages)
                        .itemsIds(R.array.itemIds)
                        .itemsCallback(new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                switch (which) {
                                    case 0:
                                        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                                        photoPickerIntent.setType("image/*");
                                        startActivityForResult(photoPickerIntent, SELECT_PHOTO);
                                        break;
                                    case 1:
                                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                        startActivityForResult(intent, CAPTURE_PHOTO);
                                        break;
                                    case 2:
                                        profileImageView.setImageResource(R.drawable.ic_account_circle_black);
                                        break;
                                }
                            }
                        })
                        .show();
                break;

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 0) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                profileImageView.setEnabled(true);
            }
        }
    }

    public void setProgressBar() {
        progressBar = new ProgressDialog(this);
        progressBar.setCancelable(true);
        progressBar.setMessage("Bitte warte...");
        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressBar.setProgress(0);
        progressBar.setMax(100);
        progressBar.show();
        progressBarStatus = 0;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (progressBarStatus < 100) {
                    progressBarStatus += 30;

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    progressBarbHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setProgress(progressBarStatus);
                        }
                    });
                }
                if (progressBarStatus >= 100) {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    progressBar.dismiss();
                }

            }
        }).start();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SELECT_PHOTO) {
            if (resultCode == RESULT_OK) {
                try {
                    final Uri imageUri = data.getData();
                    final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                    final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);

                    setProgressBar();
                    profileImageView.setImageBitmap(selectedImage);


                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }

        } else if (requestCode == CAPTURE_PHOTO) {
            if (resultCode == RESULT_OK) {
                onCaptureImageResult(data);
            }
        }
    }

    private void onCaptureImageResult(Intent data) {
        thumbnail = (Bitmap) data.getExtras().get("data");


        setProgressBar();
        profileImageView.setMaxWidth(200);
        profileImageView.setImageBitmap(thumbnail);

    }
}

