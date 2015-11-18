package bit.hillcg2.SafetyMap;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.internal.widget.AdapterViewCompat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.view.View.OnClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import android.widget.ArrayAdapter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import bit.hillcg2.SafetyMap.Managers.DBManager;
import bit.hillcg2.SafetyMap.Models.Incident;


public class MapActivity extends Activity implements OnMapReadyCallback {

    //Globals
    private MapFragment mapFragment;
    private GoogleMap map;
    private LocationManager locationManager;
    private Criteria defaultCriteria;
    private String providerName;
    private Button btnBackFromMap;
    private DBManager dbManager;
    private String months[];
    private String days[];
    private String years[];
    private Spinner spinDay;
    private Spinner spinMonth;
    private Spinner spinYear;
    private ArrayAdapter dayAdapter;
    private ArrayAdapter monthAdapter;
    private ArrayAdapter yearAdapter;
    private MarkerInfoDialog markerInfoDialog;
    private ArrayList<Incident> displayedIncidents;
    private ArrayList<Marker> allMarkers;
    private Marker lastClickedMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        //Call to get everything initialized
        init();
    }

    //Pre-condition: None
    //Post-condition: Initialize everything for use
    public void init(){
        //Get instances of views from layouts and set them up
        btnBackFromMap = (Button)findViewById(R.id.btnBackFromMap);
        btnBackFromMap.setOnClickListener(new backToMainScreen());

        spinMonth = (Spinner)findViewById(R.id.spinMonth);
        spinDay = (Spinner)findViewById(R.id.spinDay);
        spinYear = (Spinner)findViewById(R.id.spinYear);

        displayedIncidents = new ArrayList<Incident>();
        allMarkers = new ArrayList<Marker>();

        Calendar cal = Calendar.getInstance();

        //Get the current month and day to set initial values into spinners
        int currMonth = cal.get(Calendar.MONTH);
        int currYear = cal.get(Calendar.YEAR);

        //Find out how many days the current month has
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        int daysArraySize = daysInMonth + 1;
        days = new String[daysArraySize];
        days[0] = "All";

        //Set up array to have all the days of the current month to use for spinner
        for(int i=0; i < daysInMonth; i++)
        {
            String currDay = String.valueOf(i + 1);
            days[i + 1] = currDay;
        }

        //Number of years since project started
        int numYearsSinceProjectStart = currYear - 2015;
        years = new String[numYearsSinceProjectStart + 1];

        for(int i=0; i <= numYearsSinceProjectStart; i++){
            int newYear = 2015 + i;

            years[i] = String.valueOf(newYear);
        }

        //Array holding all months to use for spinner
        months = new String[]{"All", "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};

        //Set up adapters for the spinners
        dayAdapter = new ArrayAdapter(this, R.layout.spinner_item, days);
        monthAdapter = new ArrayAdapter(this, R.layout.spinner_item, months);
        yearAdapter = new ArrayAdapter(this, R.layout.spinner_item, years);

        //Set spinners adapters
        spinMonth.setAdapter(monthAdapter);
        spinDay.setAdapter(dayAdapter);
        spinYear.setAdapter(yearAdapter);

        //Set up spinners with their initial values
        spinDay.setSelection(0, false);
        spinMonth.setSelection(currMonth + 1, false);
        spinYear.setSelection(numYearsSinceProjectStart, false);

        spinDay.setOnItemSelectedListener(new updateMapOnSelectHandler());
        spinMonth.setOnItemSelectedListener(new monthSelected());
        spinYear.setOnItemSelectedListener(new updateMapOnSelectHandler());

        //Get the database manager
        dbManager = new DBManager(this);

        //Set up location service
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        defaultCriteria = new Criteria();
        providerName = locationManager.getBestProvider(defaultCriteria, false);

        //Get map and set onMapReady to trigger
        mapFragment = (MapFragment)getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    //Pre-condition: None
    //Post-condition: Called when the map is finished being set up
    @Override
    public void onMapReady(GoogleMap googleMap) {
        //Set map to global so other methods can use it
        map = googleMap;

        //Set marker click handler
        map.setOnMarkerClickListener(new markerClickHandler());

        //Move map to current location
        Location currentLocation = locationManager.getLastKnownLocation(providerName);

        //If currentLocation then move map to there, if not default to Dunedin
        if (currentLocation != null)
        {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), 16));
        }
        else
        {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-45.874036, 170.503566), 17));
        }

        //Fill map with markers
        addIncidentsToMap();
    }

    //Pre-condition: None
    //Post-condition: Adds all of the incidents onto the map for the current selected date
    public void addIncidentsToMap(){
        //Pull all incidents out from database
        ArrayList<Incident> allIncidents = dbManager.getAllIncidents();

        //Get the date values from spinners
        String selectedMonth = String.valueOf(spinMonth.getSelectedItemPosition());
        String selectedDay = spinDay.getSelectedItem().toString();
        String selectedYear = spinYear.getSelectedItem().toString();

        //Loop over all the incidents
        for(Incident i : allIncidents)
        {
            //Get the date value to check if it's the same as spinner values
            String date = i.getDate();

            //Split it apart for comparison
            String[] splitDate = date.split("/");
            String currMonth = splitDate[1];
            String currDay = splitDate[2];
            String currYear = splitDate[0];

            //If all months selected
            if(selectedMonth.equals("0") && currYear.equals(selectedYear))
            {
                addSingleMarkerToMap(i);
            }
            //If all days in a specific month selected
            else if(currMonth.equals(selectedMonth) && selectedDay.equals("All") && currYear.equals(selectedYear))
            {
                addSingleMarkerToMap(i);
            }
            //If month and a day are selected
            else if(currMonth.equals(selectedMonth) && currDay.equals(selectedDay) && currYear.equals(selectedYear))
            {
                addSingleMarkerToMap(i);
            }
        }
    }

    //Pre-condition: Incident can't be null
    //Post-condition: Adds a single marker to the map for passed in incident
    public void addSingleMarkerToMap(Incident i){
        //Get location of incident
        String lat = i.getLat();
        String lng = i.getLng();

        //Check that the incident has a GPS location
        if (!lat.equals("null") && !lng.equals("null"))
        {
            //Turn the gps locations into one the map can work with
            LatLng incidentPos = new LatLng(Double.valueOf(lat), Double.valueOf(lng));

            //Add marker for incident onto the map
            Marker newMarker = map.addMarker(new MarkerOptions()
                    .position(incidentPos));

            allMarkers.add(newMarker);
            displayedIncidents.add(i);
        }
    }

    //Handles when markers are clicked, opens dialog fragment on click
    public class markerClickHandler implements GoogleMap.OnMarkerClickListener{

        @Override
        public boolean onMarkerClick(Marker marker) {
            //open dialog
            markerInfoDialog = new MarkerInfoDialog();

            //Get current incident
            int indexOfClickedMarker = allMarkers.indexOf(marker);
            Incident currIncident = displayedIncidents.get(indexOfClickedMarker);
            lastClickedMarker = marker;

            //Pass incident into the dialog fragment
            markerInfoDialog.setCurrIncident(currIncident);

            //Show fragment
            FragmentManager fm = getFragmentManager();
            markerInfoDialog.show(fm, "MarkerInfo");

            return false;
        }
    }

    //Pre-condition: None
    //Post-condition: Clears the map and adds asks for markers to be added back onto the map
    public void updateMap(){
        map.clear();
        addIncidentsToMap();
    }

    //Pre-condition: None
    //Post-condition: Deletes an incident from the database and the map
    public void deleteIncident(int incidentID){
        int arrayIndex = allMarkers.indexOf(lastClickedMarker);

        //Remove marker and incident from arrayLists
        lastClickedMarker.remove();
        allMarkers.remove(arrayIndex);
        displayedIncidents.remove(arrayIndex);

        //Get database to delete incident
        dbManager.deleteIncident(incidentID);
    }

    //Pre-condition: Dialog should be open when called
    //Post-condition: Closes dialog popup
    public void closeDialog(){
        markerInfoDialog.dismiss();
    }

    //Handler for when a month is selected from spinner
    public class monthSelected implements OnItemSelectedListener{

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
            //The number of the month that was selected
            int clickedMonth = pos;

            //0 = all months
            if(clickedMonth != 0)
            {
                //Set up and calculate how many days the selected month has
                Calendar calToGetYear = Calendar.getInstance();
                int currYear = calToGetYear.get(Calendar.YEAR);

                Calendar calToWorkOutNumDays = new GregorianCalendar(currYear, clickedMonth, 1);
                int daysInMonth = calToWorkOutNumDays.getActualMaximum(Calendar.DAY_OF_MONTH);

                //Set up a new array for days
                int daysArraySize = daysInMonth + 1;
                days = new String[daysArraySize];
                days[0] = "All";

                //Set up array to have all the days of the current month to use for spinner
                for (int i = 0; i < daysInMonth; i++)
                {
                    String currDay = String.valueOf(i + 1);
                    days[i + 1] = currDay;
                }
            }
            else
            {
                days = new String[1];
                days[0] = "All";
            }

            //Update the spinner with the right amount of days for the month
            dayAdapter = new ArrayAdapter(getBaseContext(), R.layout.spinner_item, days);
            spinDay.setAdapter(dayAdapter);
            dayAdapter.notifyDataSetChanged();

            //Update the map
            updateMap();
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    }

    //Handler for when a spinner is updated, asks to update the map with new selected values
    public class updateMapOnSelectHandler implements OnItemSelectedListener{

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            //Update the map
            updateMap();
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    }

    //Handler for back button pressed
    public class backToMainScreen implements OnClickListener{

        @Override
        public void onClick(View view) {
            //Send user back to main screen
            Intent backToMainIntent = new Intent(getBaseContext(), MainActivity.class);
            startActivity(backToMainIntent);
        }
    }
}