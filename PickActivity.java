package com.example.tom.aussiefinal;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.support.v4.content.ContextCompat;
import android.Manifest;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.app.Activity;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;




public class PickActivity extends AppCompatActivity{

    //Initialize things- instance, button, cursor, adapter.


    //DB definitions
    SQLiteExample instanceSQL;
    Button mSQLSubmitButton;
    Cursor mSQLCursor;
    SimpleCursorAdapter mSQLCursorAdapter;
    private static final String TAG = "SQLActivity";
    SQLiteDatabase newDB;

    //On create
    @Override
    protected void onCreate(final Bundle savedInstanceState) {


        //Set the view
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick);

        //Create an instance, using the constructor of the class that extends SQLiteOpenHelper
        instanceSQL = new SQLiteExample(this);
        //Get a DB we can use
        newDB = instanceSQL.getWritableDatabase();

        //A button. adds a row. We will call onConnected when the button is clicked
        mSQLSubmitButton = (Button) findViewById(R.id.sql_add_row_button);
        mSQLSubmitButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //call onconnected
                onConnected(savedInstanceState);
            }
        });

        populateTable();
    }

    //End of onCreate



    public void onConnected(@Nullable Bundle bundle) {


        //if we have a DB, do stuff
        if(newDB != null){

            //Thing to hold some values, text and GPS values
            ContentValues vals = new ContentValues();
            //Take text from user, regardless of permissions
            vals.put(DBContract.DemoTable.COLUMN_NAME_DEMO_STRING, ((EditText)findViewById(R.id.sql_text_input)).getText().toString());





            //And insert whichever values we got.
            newDB.insert(DBContract.DemoTable.TABLE_NAME,null,vals);
            populateTable();
        } else {
            Log.d(TAG, "Unable to access database for writing.");
        }

    }



    private void populateTable(){
        //make sure the DB exists and there is a cursor
        if(newDB != null) {
            try {
                if(mSQLCursorAdapter != null && mSQLCursorAdapter.getCursor() != null){
                    if(!mSQLCursorAdapter.getCursor().isClosed()){
                        mSQLCursorAdapter.getCursor().close();
                    }
                }
                //This needs to have 3 columns... user input, lat, and long
                mSQLCursor = newDB.query(DBContract.DemoTable.TABLE_NAME,
                        new String[]{DBContract.DemoTable._ID,
                                DBContract.DemoTable.COLUMN_NAME_DEMO_STRING,
                                DBContract.DemoTable.LAT_COLUMN,
                                DBContract.DemoTable.LONG_COLUMN}, null, null, null, null, null);

                //List View stuff. Define List View, define adapter, set adapter to the listview.
                ListView SQLListView = (ListView) findViewById(R.id.sql_list_view);
                mSQLCursorAdapter = new SimpleCursorAdapter(this,
                        R.layout.sql_item,
                        mSQLCursor,
                        new String[]{DBContract.DemoTable.COLUMN_NAME_DEMO_STRING, DBContract.DemoTable.LAT_COLUMN, DBContract.DemoTable.LONG_COLUMN},
                        new int[]{R.id.sql_listview_string, R.id.sql_listview_int, R.id.sql_listview_int2},
                        0);
                SQLListView.setAdapter(mSQLCursorAdapter);
            } catch (Exception e) {
                Log.d(TAG, "Error loading data from database");
            }
        }
    }

//End of large class

}


//use open helper to create DB's, and to return DB's that we can use in our program.
class SQLiteExample extends SQLiteOpenHelper {

    //Constructor, so we can make an instance
    public SQLiteExample(Context context) {
        //Call super constructor, and pass context, DB name, ignore cursor factory, and DB version
        super(context, DBContract.DemoTable.DB_NAME, null, DBContract.DemoTable.DB_VERSION);
    }

    //On create should be called once, ever since it should persist between runs
    @Override
    public void onCreate(SQLiteDatabase db) {
        //Execute raw SQL to create the DB, the paramter is a string defined in the DBContract
        db.execSQL(DBContract.DemoTable.SQL_CREATE_DEMO_TABLE);

        //To test, let's make a thing, testValues, and put some values
        ContentValues testValues = new ContentValues();
        testValues.put(DBContract.DemoTable.COLUMN_NAME_DEMO_STRING, "Winner DB Created");
        testValues.put(DBContract.DemoTable.LAT_COLUMN, "Winner1");
        //now actually insert those values
        db.insert(DBContract.DemoTable.TABLE_NAME,null,testValues);

    }

    //Upgrade, we dont care much about this. Just drop the table and make a new one.
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DBContract.DemoTable.SQL_DROP_DEMO_TABLE);
        onCreate(db);
    }
}

//Contract class. Defines a bunch of constant string that will be used in and on DB.
final class DBContract {
    private DBContract() {
    }

    ;

    public final class DemoTable implements BaseColumns {
        public static final String DB_NAME = "demo_db";
        public static final String TABLE_NAME = "demo";
        public static final String COLUMN_NAME_DEMO_STRING = "demo_string";
        //  public static final String COLUMN_NAME_DEMO_INT = "demo_int";
        //Needs Lat and Long columns
        public static final String LAT_COLUMN = "lat_col";
        public static final String LONG_COLUMN = "long_col";
        public static final int DB_VERSION = 4;

        //This string is used to create Table, with db.execSQL()
        public static final String SQL_CREATE_DEMO_TABLE = "CREATE TABLE " +
                DemoTable.TABLE_NAME + "(" + DemoTable._ID + " INTEGER PRIMARY KEY NOT NULL," +
                DemoTable.COLUMN_NAME_DEMO_STRING + " VARCHAR(255)," +
                //        DemoTable.COLUMN_NAME_DEMO_INT + " INTEGER);" +
                DemoTable.LAT_COLUMN + " VARCHAR(255)," +
                DemoTable.LONG_COLUMN + " VARCHAR(255));";


        //This string is used to insert into Table, with db.execSQL()
        public static final String SQL_TEST_DEMO_TABLE_INSERT = "INSERT INTO " + TABLE_NAME +
                " (" + COLUMN_NAME_DEMO_STRING + "," + LAT_COLUMN + "," + LONG_COLUMN + ") VALUES ('test', 'team1', 'teamTwo');";

        //This string is used to drop Table, with db.execSQL()
        public static final String SQL_DROP_DEMO_TABLE = "DROP TABLE IF EXISTS " + DemoTable.TABLE_NAME;
    }
}