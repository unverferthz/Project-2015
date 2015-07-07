package bit.hillcg2.SafetyMap;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;


public class DBManager{

    //Global variables
    private SQLiteDatabase incidentDB;
    private Context context;
    private String createQuery;


    //Constructor. Need to pass in context from main activity to allow use to the sql database for some reason
    public DBManager(Context mainContext){
        //Set context to global so other methods can use it
        context = mainContext;

        //Open existing or create new database
        incidentDB = context.openOrCreateDatabase("incidentDB", context.MODE_PRIVATE, null);

        //Create the table incase it doesn't already exist
        createQuery = "CREATE TABLE IF NOT EXISTS tblIncident(incidentID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "distance INTEGER NOT NULL, time TEXT NOT NULL, date TEXT NOT NULL, latitude TEXT NOT NULL, " +
                "longitude TEXT NOT NULL, data_used BIT NOT NULL)";
        incidentDB.execSQL(createQuery);

        //Close connection
        incidentDB.close();
    }

    //Inserts the passed in incident into the SQLite database
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

    //Returns an arraylist containing all of the incidents
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
            Incident currIncident = new Incident(distance, time, date, lat, lng);
            incidentArray.add(currIncident);

            //Get next record
            recordSet.moveToNext();
        }

        //Close connection to DB
        incidentDB.close();

        //Return all of the incidents
        return incidentArray;
    }

    //Returns an arraylist containing all of the incidents
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
                Incident currIncident = new Incident(distance, time, date, lat, lng);
                incidentArray.add(currIncident);

                String updateQuery = "UPDATE tblIncident SET data_used='1' WHERE incidentID='" + id + "';";
                incidentDB.execSQL(updateQuery);
            }

            //Get next record
            recordSet.moveToNext();
        }

        //Close connection to DB
        incidentDB.close();

        //Return all of the incidents
        return incidentArray;
    }

    //Drops the incident table from DB and recreates it
    public void resetDatabase(){
        incidentDB = context.openOrCreateDatabase("incidentDB", context.MODE_PRIVATE, null);

        String dropQuery = "DROP TABLE IF EXISTS tblIncident";

        incidentDB.execSQL(dropQuery);

        incidentDB.execSQL(createQuery);
        incidentDB.close();
    }
}
