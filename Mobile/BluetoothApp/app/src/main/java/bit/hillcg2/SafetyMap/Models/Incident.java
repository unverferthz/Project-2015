package bit.hillcg2.SafetyMap.Models;


public class Incident {

    //Variables
    private int incidentID;
    private int distance;
    private String time;
    private String date;
    private String lat;
    private String lng;

    //Constructor if there isn't an ID because it hasn't gone into the database yet
    public Incident(int startDistance, String startTime, String startDate, String startLat, String startLng){
        incidentID = -1;
        distance = startDistance;
        time = startTime;
        date = startDate;
        lat = startLat;
        lng = startLng;
    }

    //Constructor that includes ID
    public Incident(int startIncidentID, int startDistance, String startTime, String startDate, String startLat, String startLng){
        incidentID = startIncidentID;
        distance = startDistance;
        time = startTime;
        date = startDate;
        lat = startLat;
        lng = startLng;
    }

    public int getIncidentID(){return incidentID;}
    public int getDistance(){
        return distance;
    }
    public String getTime(){
        return time;
    }
    public String getDate(){
        return date;
    }
    public String getLat(){
        return lat;
    }
    public String getLng(){
        return lng;
    }
}
