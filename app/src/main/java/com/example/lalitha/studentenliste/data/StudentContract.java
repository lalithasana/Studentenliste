package com.example.lalitha.studentenliste.data;
import android.net.Uri;
import android.content.ContentResolver;
import android.provider.BaseColumns;
/**
 * Created by Lalitha on 02.11.2017.
 */

public final class StudentContract {

   //Leerer Konstruktor damit keine Contract class erzeugt werden kann
    private StudentContract() {}


     //"Content authority" ist der Name des gesamten content provider

    public static final String CONTENT_AUTHORITY = "com.example.lalitha.studentenliste";


     //Verwendung von CONTENT_AUTHORITY zur Erzeugung aller URI's, welche die App benutzt um den content provider zu kontaktieren

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);


    public static final String PATH_STUDENTEN = "studenten-path";

    /**Innere Klasse, die konstante Werte für die Studenten Datenbnak definiert
     *Jeder Eintrag in der Tabelle repräsentiert einen einzelnen Studenten
     */
    public static final class StudentEntry implements BaseColumns {

        //Die content URI für den Zugang zu den Studenten Daten im provider */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_STUDENTEN);

        /**
         * Der MIME typ der Content Uri für eine Studentenliste
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_STUDENTEN;

        /**
         * Der MIME typ der Content Uri für einen Studenten
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_STUDENTEN;


        public final static String TABLE_NAME = "studenten";


         // Einmalige ID Nummer für die Studenten


        public final static String _ID = BaseColumns._ID;

        //Name, Nachname,...
        public final static String COLUMN_VORNAME = "vorname";
        public final static String COLUMN_NACHNAME = "nachname";
        public final static String COLUMN_FACHBEREICH = "fachbereich";
        public final static String COLUMN_STUDIENGANG = "studiengang";
        public final static String COLUMN_WOHNORT = "wohnort";
        public final static String COLUMN_TELEFON = "telefon";
        public final static String COLUMN_IMAGE = "image";
        public final static String COLUMN_EMAIL = "email";


        public final static String COLUMN_STUDENT_GESCHLECHT = "geschlecht";


        public static final int GESCHLECHT_UNBEKANNT = 0;
        public static final int GESCHLECHT_MAENNLICH = 1;
        public static final int GESCHLECHT_WEIBLICH = 2;

        public static boolean isValidGender(int geschlecht) {
            if (geschlecht == GESCHLECHT_UNBEKANNT || geschlecht == GESCHLECHT_MAENNLICH || geschlecht == GESCHLECHT_WEIBLICH) {
                return true;
            }
            return false;
        }
    }

}



