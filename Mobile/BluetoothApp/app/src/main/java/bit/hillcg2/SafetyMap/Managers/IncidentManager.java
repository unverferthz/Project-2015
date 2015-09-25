package bit.hillcg2.SafetyMap.Managers;

import android.location.Location;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import bit.hillcg2.SafetyMap.MainActivity;
import bit.hillcg2.SafetyMap.Models.Incident;


public class IncidentManager {
    DBManager dbManager;
    MainActivity mainActivity;

    public IncidentManager(MainActivity mainActivity, DBManager dbManager){
        this.dbManager = dbManager;
        this.mainActivity = mainActivity;
    }

    public void incidentRecieved(String newDistance){
        Incident newIncident = createIncident(newDistance);

        //Insert value into DB
        dbManager.insertIncident(newIncident);
    }

    //Pre-condition: Accepts a distance value as a string
    //Post-condition: Returns an incident with the passed in distance value
    private Incident createIncident(String newDistance){
        int distance = Integer.parseInt(newDistance);

        DateFormat df = new SimpleDateFormat("h:mm a");
        String time = df.format((Calendar.getInstance().getTime()));

        //Changed from ("d/MM/yyyy")
        df = new SimpleDateFormat("yyyy/M/d");
        String date = df.format((Calendar.getInstance().getTime()));

        //Get the most recent location value
        Location currentLocation = mainActivity.getCurrentLocation();
        String lat = "";
        String lng = "";

        //Check if there is a value for location
        if(currentLocation != null)
        {
            //Get the lat and longitude values
            lat = String.valueOf(currentLocation.getLatitude());
            lng = String.valueOf(currentLocation.getLongitude());
        }
        //Location is empty but incidents still occurred
        else
        {
            lat = "null";
            lng = "null";
        }

        //Make an incident with all the values
        Incident newIncident = new Incident(distance, time, date, lat, lng);

        return newIncident;
    }
}
