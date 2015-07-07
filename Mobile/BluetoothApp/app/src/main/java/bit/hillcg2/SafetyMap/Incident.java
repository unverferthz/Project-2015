package bit.hillcg2.SafetyMap;


public class Incident {

    private int distance;
    private String time;
    private String date;
    private String lat;
    private String lng;

    public Incident(int startDistance, String startTime, String startDate, String startLat, String startLng){
        distance = startDistance;
        time = startTime;
        date = startDate;
        lat = startLat;
        lng = startLng;
    }

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
