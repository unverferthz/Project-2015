package bit.hillcg2.SafetyMap.Models;


public class Incident {

    private int incidentID;
    private int distance;
    private String time;
    private String date;
    private String lat;
    private String lng;

    public Incident(int startDistance, String startTime, String startDate, String startLat, String startLng){
        incidentID = -1;
        distance = startDistance;
        time = startTime;
        date = startDate;
        lat = startLat;
        lng = startLng;
    }

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
