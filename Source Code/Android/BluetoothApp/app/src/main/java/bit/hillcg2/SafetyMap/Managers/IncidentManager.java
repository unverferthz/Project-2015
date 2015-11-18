package bit.hillcg2.SafetyMap.Managers;

import android.location.Location;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import bit.hillcg2.SafetyMap.MainActivity;
import bit.hillcg2.SafetyMap.Models.Incident;


public class IncidentManager {
    private DBManager dbManager;
    private MainActivity mainActivity;

    //Constructor
    public IncidentManager(MainActivity mainActivity, DBManager dbManager){
        this.dbManager = dbManager;
        this.mainActivity = mainActivity;
    }

    //Pre-condition: None
    //Post-condition: Add new incident to database
    public void incidentReceived(String newDistance){
        //Create incident from the distance received
        Incident newIncident = createIncident(newDistance);

        //Insert value into DB
        if(newIncident != null)
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
            //Don't create an incident if there wasn't any gps coordinates
            return null;
        }

        //Make an incident with all the values
        Incident newIncident = new Incident(distance, time, date, lat, lng);

        return newIncident;
    }
}