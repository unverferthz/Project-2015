package bit.hillcg2.SafetyMap;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
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
import com.google.android.gms.maps.model.MarkerOptions;
import android.widget.ArrayAdapter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;


public class MapActivity extends ActionBarActivity implements OnMapReadyCallback {

    private MapFragment mapFragment;
    private GoogleMap map;
    private LocationManager locationManager;
    private Criteria defaultCriteria;
    private String providerName;
    private Button btnBackFromMap;
    private DBManager dbManager;
    private String months[];
    private String days[];
    private Spinner spinMonth;
    private Spinner spinDay;
    private ArrayAdapter monthAdapter;
    private ArrayAdapter dayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        //Call to get everything initialized
        init();
    }

    //Initialize everything for use
    public void init(){
        //Get instances of views from layouts and set them up
        btnBackFromMap = (Button)findViewById(R.id.btnBackFromMap);
        btnBackFromMap.setOnClickListener(new backToMainScreen());
        btnBackFromMap.getBackground().setColorFilter(0xFF00FF00, PorterDuff.Mode.MULTIPLY);

        spinMonth = (Spinner)findViewById(R.id.spinMonth);
        spinDay = (Spinner)findViewById(R.id.spinDay);

        Calendar cal = Calendar.getInstance();

        //Get the current month and day to set initial values into spinners
        int currMonth = cal.get(Calendar.MONTH);
        int today = cal.get(Calendar.DAY_OF_MONTH) - 1;

        //Find out how many days the current month has
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        days = new String[daysInMonth];

        //Set up array to have all the days of the current month to use for spinner
        for(int i=0; i < daysInMonth; i++)
        {
            String currDay = String.valueOf(i + 1);
            days[i] = currDay;
        }

        //Array holding all months to use for spinner
        months = new String[]{"January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};

        //Set up adapters for the spinners
        monthAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, months);
        dayAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, days);

        //Set spinners adapters
        spinMonth.setAdapter(monthAdapter);
        spinDay.setAdapter(dayAdapter);

        //Set up spinners with their initial values
        spinMonth.setSelection(currMonth, false);
        spinDay.setSelection(today, false);

        spinDay.setOnItemSelectedListener(new daySelected());
        spinMonth.setOnItemSelectedListener(new monthSelected());

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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        //Set map to global so other methods can use it
        map = googleMap;

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

        addIncidentsToMap();
    }

    //Adds all of the incidents onto the map for the current selected date
    public void addIncidentsToMap(){
        //Pull all incidents out from database
        ArrayList<Incident> allIncidents = dbManager.getAllIncidents();

        //Todo remove the 0
        //Get the date values from spinners
        String selectedMonth = "0" + String.valueOf(spinMonth.getSelectedItemPosition() + 1);
        String selectedDay = spinDay.getSelectedItem().toString();

        //Loop over all the incidents
        for(Incident i : allIncidents)
        {
            //Get the date value to check if it's the same as spinner values
            String date = i.getDate();

            //Split it apart for comparison
            String[] splitDate = date.split("/");
            String currMonth = splitDate[1];
            String currDay = splitDate[0];

            //Check if month and day are the same for incident and spinner values
            if(currMonth.equals(selectedMonth) && currDay.equals(selectedDay))
            {
                //Get the rest of the incidents data
                int distance = i.getDistance();
                String lat = i.getLat();
                String lng = i.getLng();
                String time = i.getTime();

                //TODO do something with the null values
                //Check that the incident has a GPS location
                if (!lat.equals("null") && !lng.equals("null"))
                {
                    //Turn the gps locations into one the map can work with
                    LatLng incidentPos = new LatLng(Double.valueOf(lat), Double.valueOf(lng));

                    //Add marker for incident onto the map
                    map.addMarker(new MarkerOptions()
                            .position(incidentPos)
                            .title("Vehicle distance: " + String.valueOf(distance) + "\nTime: " + time + "\nDate: " + date));
                }
            }
        }
    }

    //Clears the map and adds asks for markers to be added back onto the map
    public void updateMap(){
        map.clear();
        addIncidentsToMap();
    }

    //Handler for when a month is selected from spinner
    public class monthSelected implements OnItemSelectedListener{

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
            //The number of the month that was selected
            int clickedMonth = pos;

            //Set up and calculate how many days the selected month has
            Calendar myCal = new GregorianCalendar(2015, clickedMonth, Integer.valueOf(spinDay.getSelectedItem().toString()));
            int daysInMonth = myCal.getActualMaximum(Calendar.DAY_OF_MONTH);

            //Set up a new array for days
            days = new String[daysInMonth];

            //Add the values into the days array
            for (int i = 0; i < daysInMonth; i++)
            {
                String currDay = String.valueOf(i + 1);
                days[i] = currDay;
            }

            //TODO this is ugly, fix it
            //Update the spinner with the right amount of days for the month
            dayAdapter = new ArrayAdapter(getBaseContext(), android.R.layout.simple_spinner_dropdown_item, days);
            spinDay.setAdapter(dayAdapter);
            dayAdapter.notifyDataSetChanged();

            //Update the map
            updateMap();
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    }

    //Handler for when a new day is selected from spinner
    public class daySelected implements OnItemSelectedListener{

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
