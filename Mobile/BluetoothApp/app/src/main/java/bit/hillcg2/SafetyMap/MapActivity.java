package bit.hillcg2.SafetyMap;

import android.content.Context;
import android.content.Intent;
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
import android.widget.Toast;
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

    MapFragment mapFragment;
    GoogleMap map;
    LocationManager locationManager;
    Criteria defaultCriteria;
    String providerName;
    Button btnBackFromMap;
    DBManager dbManager;
    String months[];
    String days[];
    Spinner spinMonth;
    Spinner spinDay;
    ArrayAdapter monthAdapter;
    ArrayAdapter dayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        init();
    }

    public void init(){
        btnBackFromMap = (Button)findViewById(R.id.btnBackFromMap);
        btnBackFromMap.setOnClickListener(new backToMainScreen());

        spinMonth = (Spinner)findViewById(R.id.spinMonth);
        spinMonth.setOnItemSelectedListener(new monthSelected());

        spinDay = (Spinner)findViewById(R.id.spinDay);
        spinDay.setOnItemSelectedListener(new daySelected());

        Calendar cal = Calendar.getInstance();

        int currMonth = cal.get(Calendar.MONTH);
        int today = cal.get(Calendar.DAY_OF_MONTH) - 1;

        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        days = new String[daysInMonth];

        for(int i=0; i < daysInMonth; i++)
        {
            String currDay = String.valueOf(i + 1);
            days[i] = currDay;
        }

        months = new String[]{"January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};

        monthAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, months);
        dayAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, days);

        Toast.makeText(this, String.valueOf(daysInMonth), Toast.LENGTH_LONG).show();

        spinMonth.setAdapter(monthAdapter);
        spinDay.setAdapter(dayAdapter);

        spinMonth.setSelection(currMonth);
        spinDay.setSelection(today);

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

        //Move map to current player location
        Location currentLocation = locationManager.getLastKnownLocation(providerName);

        //If currentLocation then move map to there, if not default to Dunedin
        if (currentLocation != null)
        {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), 16));
        }
        else
        {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-45.874036, 170.503566), 16));
        }

        addIncidentsToMap();
    }

    public void addIncidentsToMap(){
        ArrayList<Incident> allIncidents = dbManager.getAllIncidents();

        //TODO figure out how to remove 0 from input
        String selectedMonth = "0" + String.valueOf(spinMonth.getSelectedItemPosition() + 1);
        String selectedDay = spinDay.getSelectedItem().toString();

        for(Incident i : allIncidents)
        {
            String lat = i.getLat();
            String lng = i.getLng();
            String time = i.getTime();
            String date = i.getDate();
            int distance = i.getDistance();

            String[] splitDate = date.split("/");
            String currMonth = splitDate[1];
            String currDay = splitDate[0];

            if(currMonth.equals(selectedMonth) && currDay.equals(selectedDay))
            {
                if (!lat.equals("null") && !lng.equals("null")) {
                    LatLng incidentPos = new LatLng(Double.valueOf(lat), Double.valueOf(lng));

                    map.addMarker(new MarkerOptions()
                            .position(incidentPos)
                            .title("Vehicle distance: " + String.valueOf(distance) + "\nTime: " + time + "\nDate: " + date));
                }
            }
        }
    }

    public void updateData(){
        map.clear();
        addIncidentsToMap();
    }

    public class monthSelected implements OnItemSelectedListener{

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
            int clickedMonth = pos;

            Calendar myCal = new GregorianCalendar(2015, clickedMonth, Integer.valueOf(spinDay.getSelectedItem().toString()));
            int daysInMonth = myCal.getActualMaximum(Calendar.DAY_OF_MONTH);

            days = new String[daysInMonth];

            for (int i = 0; i < daysInMonth; i++)
            {
                String currDay = String.valueOf(i + 1);
                days[i] = currDay;
            }

            //TODO this is ugly, fix it
            dayAdapter = new ArrayAdapter(getBaseContext(), android.R.layout.simple_spinner_dropdown_item, days);
            spinDay.setAdapter(dayAdapter);
            dayAdapter.notifyDataSetChanged();

            updateData();
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    }

    public class daySelected implements OnItemSelectedListener{

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            updateData();
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    }

    public class backToMainScreen implements OnClickListener{

        @Override
        public void onClick(View view) {
            Intent backToMainIntent = new Intent(getBaseContext(), MainActivity.class);
            startActivity(backToMainIntent);
        }
    }
}
