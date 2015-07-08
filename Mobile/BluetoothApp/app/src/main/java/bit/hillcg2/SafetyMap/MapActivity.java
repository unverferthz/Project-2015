package bit.hillcg2.SafetyMap;

import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.view.View.OnClickListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;


public class MapActivity extends ActionBarActivity implements OnMapReadyCallback {

    MapFragment mapFragment;
    GoogleMap map;
    LocationManager locationManager;
    Criteria defaultCriteria;
    String providerName;
    Button btnBackFromMap;

    DBManager dbManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        btnBackFromMap = (Button)findViewById(R.id.btnBackFromMap);
        btnBackFromMap.setOnClickListener(new backToMainScreen());

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

        //If currentlocation then move map to there, if not default to Dunedin
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

        for(Incident i : allIncidents)
        {
            String lat = i.getLat();
            String lng = i.getLng();
            String time = i.getTime();
            String date = i.getDate();
            int distance = i.getDistance();

            LatLng incidentPos = new LatLng(Double.valueOf(lat), Double.valueOf(lng));

            map.addMarker(new MarkerOptions()
                    .position(incidentPos)
                    .title("Vehicle distance: " + String.valueOf(distance) + "\nTime: " + time + "\nDate: " + date));
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
