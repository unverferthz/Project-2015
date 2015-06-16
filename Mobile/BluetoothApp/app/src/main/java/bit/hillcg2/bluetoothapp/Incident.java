package bit.hillcg2.bluetoothapp;


public class Incident {

    int distance;
    String time;
    String date;
    String lat;
    String lng;

    public Incident(int statDistance, String startTime, String startDate, String startLat, String startLng){
        distance = statDistance;
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
