package bit.hillcg2.SafetyMap.Managers;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;

import bit.hillcg2.SafetyMap.Models.Incident;


public class DBManager{

    //Global variables
    private SQLiteDatabase incidentDB;
    private Context context;
    private String createQuery;


    //Constructor. Need to pass in context from main activity to allow use to the sql database
    public DBManager(Context mainContext){
        //Set context to global so other methods can use it
        context = mainContext;

        //Open existing or create new database
        incidentDB = context.openOrCreateDatabase("incidentDB", context.MODE_PRIVATE, null);

        //Create the table in case it doesn't already exist
        createQuery = "CREATE TABLE IF NOT EXISTS tblIncident(incidentID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "distance INTEGER NOT NULL, time TEXT NOT NULL, date TEXT NOT NULL, latitude TEXT NOT NULL, " +
                "longitude TEXT NOT NULL, data_used BIT NOT NULL)";
        incidentDB.execSQL(createQuery);

        //Close connection
        incidentDB.close();
    }

    //Pre-condition: Needs database set up. newIncident shouldn't be null
    //Post-condition: Inserts the passed in incident into the SQLite database
    public void insertIncident(Incident newIncident){
        //Reconnect to database
        incidentDB = context.openOrCreateDatabase("incidentDB", context.MODE_PRIVATE, null);

        //Separate out values from incident
        int distance = newIncident.getDistance();
        String time = newIncident.getTime();
        String date = newIncident.getDate();
        String lat = newIncident.getLat();
        String lng = newIncident.getLng();
        int dataUsed = 0;

        //Insert incident into database
        String insertQuery = "INSERT INTO tblIncident VALUES(null, '" + distance + "','" + time + "','" +
                date + "','" + lat + "','" + lng +"','" + dataUsed + "')";

        incidentDB.execSQL(insertQuery);

        //Close connection
        incidentDB.close();
    }

    //Pre-condition: None
    //Post-condition: Returns an arraylist containing all of the incidents
    public ArrayList<Incident> getAllIncidents(){
        //Reconnect to database
        incidentDB = context.openOrCreateDatabase("incidentDB", context.MODE_PRIVATE, null);
        ArrayList<Incident> incidentArray = new ArrayList<Incident>();

        //Make query and execute to get all records
        String selectQuery = "SELECT * FROM tblIncident";
        Cursor recordSet = incidentDB.rawQuery(selectQuery, null);

        //Set up to loop
        recordSet.moveToFirst();

        int recordCount = recordSet.getCount();

        //Loop over all of the records
        for(int i=0; i < recordCount; i++)
        {
            //Get all of the values to create an incident
            int idIndex = recordSet.getColumnIndex("incidentID");
            int id = recordSet.getInt(idIndex);

            //Get all of the values to create an incident
            int distanceIndex = recordSet.getColumnIndex("distance");
            int distance = recordSet.getInt(distanceIndex);

            int timeIndex = recordSet.getColumnIndex("time");
            String time = recordSet.getString(timeIndex);

            int dateIndex = recordSet.getColumnIndex("date");
            String date = recordSet.getString(dateIndex);

            int latIndex = recordSet.getColumnIndex("latitude");
            String lat = recordSet.getString(latIndex);

            int lngIndex = recordSet.getColumnIndex("longitude");
            String lng = recordSet.getString(lngIndex);

            //Create the incident and add it into array
            Incident currIncident = new Incident(id, distance, time, date, lat, lng);
            incidentArray.add(currIncident);

            //Get next record
            recordSet.moveToNext();
        }

        //Close connection to DB
        incidentDB.close();

        //Return all of the incidents
        return incidentArray;
    }

    //Pre-condition: None
    //Post-condition: Returns an arraylist containing all incidents not already uploaded to the server
    public ArrayList<Incident> getNewIncidents(){
        //Reconnect to database
        incidentDB = context.openOrCreateDatabase("incidentDB", context.MODE_PRIVATE, null);
        ArrayList<Incident> incidentArray = new ArrayList<Incident>();

        //Make query and execute to get all records
        String selectQuery = "SELECT * FROM tblIncident";
        Cursor recordSet = incidentDB.rawQuery(selectQuery, null);

        //Set up to loop
        recordSet.moveToFirst();

        int recordCount = recordSet.getCount();

        //Loop over all of the records
        for(int i=0; i < recordCount; i++)
        {
            int dataUsedIndex = recordSet.getColumnIndex("data_used");
            int dataUsed = recordSet.getInt(dataUsedIndex);

            //Check if the data hasn't already been uploaded to the server
            if(dataUsed == 0)
            {
                //Get all of the values to create an incident
                int idIndex = recordSet.getColumnIndex("incidentID");
                int id = recordSet.getInt(idIndex);

                //Get all of the values to create an incident
                int distanceIndex = recordSet.getColumnIndex("distance");
                int distance = recordSet.getInt(distanceIndex);

                int timeIndex = recordSet.getColumnIndex("time");
                String time = recordSet.getString(timeIndex);

                int dateIndex = recordSet.getColumnIndex("date");
                String date = recordSet.getString(dateIndex);

                int latIndex = recordSet.getColumnIndex("latitude");
                String lat = recordSet.getString(latIndex);

                int lngIndex = recordSet.getColumnIndex("longitude");
                String lng = recordSet.getString(lngIndex);

                //Create the incident and add it into array
                Incident currIncident = new Incident(id, distance, time, date, lat, lng);
                incidentArray.add(currIncident);
            }

            //Get next record
            recordSet.moveToNext();
        }

        //Close connection to DB
        incidentDB.close();

        //Return all of the incidents
        return incidentArray;
    }

    //Pre-condition: None
    //Post-condition: Deletes an incident from the database off of it's ID
    public void deleteIncident(int incidentID){
        incidentDB = context.openOrCreateDatabase("incidentDB", context.MODE_PRIVATE, null);

        String deleteQuery = "DELETE FROM tblIncident WHERE incidentID = " + incidentID;

        incidentDB.execSQL(deleteQuery);

        incidentDB.close();
    }

    //Pre-condition: None
    //Post-condition: Changes status of all incidents that haven't been uploaded to say they have been
    public void confirmedUpload(){
        //Reconnect to database
        incidentDB = context.openOrCreateDatabase("incidentDB", context.MODE_PRIVATE, null);

        //Make query and execute to get all records
        String selectQuery = "SELECT * FROM tblIncident";
        Cursor recordSet = incidentDB.rawQuery(selectQuery, null);

        //Set up to loop
        recordSet.moveToFirst();

        int recordCount = recordSet.getCount();

        //Loop over all of the records
        for(int i=0; i < recordCount; i++)
        {
            int dataUsedIndex = recordSet.getColumnIndex("data_used");
            int dataUsed = recordSet.getInt(dataUsedIndex);

            //Check if the data hasn't already been uploaded to the server
            if(dataUsed == 0)
            {
                //Get all of the values to create an incident
                int idIndex = recordSet.getColumnIndex("incidentID");
                int id = recordSet.getInt(idIndex);

                //Update the value of the entry to say that it has been uploaded to the server
                String updateQuery = "UPDATE tblIncident SET data_used='1' WHERE incidentID='" + id + "';";
                incidentDB.execSQL(updateQuery);
            }

            //Get next record
            recordSet.moveToNext();
        }

        //Close connection to DB
        incidentDB.close();
    }

    //Pre-condition: None
    //Post-condition: Drops the incident table from DB and recreates it
    public void resetDatabase(){
        incidentDB = context.openOrCreateDatabase("incidentDB", context.MODE_PRIVATE, null);

        String dropQuery = "DROP TABLE IF EXISTS tblIncident";

        incidentDB.execSQL(dropQuery);

        incidentDB.execSQL(createQuery);
        incidentDB.close();
    }
}