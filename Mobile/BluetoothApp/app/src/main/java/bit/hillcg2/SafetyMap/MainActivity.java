package bit.hillcg2.SafetyMap;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ListView;
import android.widget.Toast;
import android.os.Vibrator;

import bit.hillcg2.SafetyMap.BlueTooth.BTMaster;
import bit.hillcg2.SafetyMap.Models.CustomMenuItem;
import bit.hillcg2.SafetyMap.Managers.DBManager;
import bit.hillcg2.SafetyMap.Managers.IncidentManager;


@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class MainActivity extends Activity {

    //TODO get normal bluetooth working
    /***Global variables****/
    private LocationManager locationManager;
    private Criteria defaultCriteria;
    private String providerName;

    private boolean locationUpdating;

    private customLocationListener customListener;
    private Location currLocation;

    private ArrayAdapter<String> messageAdapter;
    private MenuArrayAdapter menuAdapter;
    private ListView menuList;
    private ImageView btnConnectDevice;
    private ImageView imageCenterOfRotation;
    private TextView boxConnectionStatus;

    private DBManager dbManager;
    private IncidentManager incidentManager;
    private Vibrator vibrator;

    private Animation animation;

    BTMaster btMaster;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Get everything to initialize
        init();
    }

    //Method to initialize everything
    public void init(){
        //Set up location service
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        defaultCriteria = new Criteria();
        providerName = locationManager.getBestProvider(defaultCriteria, false);
        customListener = new customLocationListener();
        currLocation = null;

        locationUpdating = false;

        //Get instance of class that manages the database
        dbManager = new DBManager(this);
        incidentManager = new IncidentManager(this, dbManager);

        vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);

        animation = AnimationUtils.loadAnimation(this, R.anim.rotate_around_center_point);

        //btleManager = new BTLEManager(this);
        btMaster = new BTMaster(this);

        //Adapter for the listview
        messageAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);

        imageCenterOfRotation = (ImageView)findViewById(R.id.imageView3);
        boxConnectionStatus = (TextView)findViewById(R.id.boxConnectionStatus);

        menuAdapter = new MenuArrayAdapter(getBaseContext(), R.layout.menu_custom_listview);

        menuList = (ListView)findViewById(R.id.menuList);
        menuList.setAdapter(menuAdapter);
        menuList.setOnItemClickListener(new menuListHandler());

        CustomMenuItem menuData = new CustomMenuItem(getResources().getString(R.string.btn_view_incidents), R.drawable.view_data);
        CustomMenuItem menuMap = new CustomMenuItem(getResources().getString(R.string.view_map), R.drawable.map);
        CustomMenuItem menuInstructions = new CustomMenuItem(getResources().getString(R.string.instructions_text), R.drawable.instructions);

        menuAdapter.add(menuData);
        menuAdapter.add(menuMap);
        menuAdapter.add(menuInstructions);
        menuAdapter.notifyDataSetChanged();

        btnConnectDevice = (ImageView)findViewById(R.id.btnConnectToDevice);
        btnConnectDevice.setOnClickListener(new connectDevice());

        if(btMaster.checkBTEnabled())
        {
            btMaster.startScanning();
            scanning();
        }
    }//End init()


    //On resume, scan and connect back to the arduino
    @Override
    protected void onResume() {
        super.onResume();
        if(btMaster.checkBTEnabled())
            btMaster.startScanning();
    }

    //When program is terminated, disconnect everything related to bluetooth
    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        finishForActivityChange();
    }

    public void finishForActivityChange(){
        if(locationUpdating)
        {
            locationManager.removeUpdates(customListener);
            locationUpdating = false;
        }

        //btleManager.closeConnection();
        btMaster.closeConnections();
    }

    public void scanning(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                animation = AnimationUtils.loadAnimation(getBaseContext(), R.anim.rotate_around_center_point);
                btnConnectDevice.startAnimation(animation);
                boxConnectionStatus.setText("Searching");
            }
        });

    }

    public void connectedToBT(){
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //Start listening for GPS location change
                locationManager.requestLocationUpdates(providerName, 5000, 5, customListener);
                locationUpdating = true;

                btnConnectDevice.setImageResource(R.drawable.tick);
                imageCenterOfRotation.setImageDrawable(null);
                boxConnectionStatus.setText("Connected");
                btnConnectDevice.clearAnimation();

                btnConnectDevice.setOnClickListener(null);
            }
        });
    }

    public void disconnectedFromBT(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //Stop updating position to save on battery
                if (locationUpdating) {
                    locationManager.removeUpdates(customListener);
                    locationUpdating = false;
                }

                btnConnectDevice.setOnClickListener(new connectDevice());

                btnConnectDevice.setImageResource(R.drawable.connect_to_device_arrow);
                imageCenterOfRotation.setImageResource(R.drawable.connect_to_device_center);
            }
        });
    }

    //Handler for button to try scanning for device again if it stops for some reason, user can manually start it
    public class connectDevice implements OnClickListener{

        @Override
        public void onClick(View view) {
            scanning();
            btMaster.startScanning();
        }
    }

    public class menuListHandler implements AdapterView.OnItemClickListener{

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            //Clean things up to prepare for exiting activity
            finishForActivityChange();

            Intent switchViewIntent = new Intent(getBaseContext(), MainActivity.class);
            switch(position)
            {
                case 0:
                    switchViewIntent = new Intent(getBaseContext(), ViewIncidents.class);
                    break;
                case 1:
                    switchViewIntent = new Intent(getBaseContext(), MapActivity.class);
                    break;
                case 2:
                    switchViewIntent = new Intent(getBaseContext(), Instructions.class);
                    break;
            }

            startActivity(switchViewIntent);
            finish();
        }
    }

    public void messageReceived(final String message){
        Location currLocation = getCurrentLocation();

        if(currLocation != null) {
            vibrator.vibrate(1000);
            incidentManager.incidentReceived(message);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 100);//(Steam type, volume)
                    toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 300);//(tone type, duration)
                    //Toast.makeText(getBaseContext(), message, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    public Location getCurrentLocation(){
        return currLocation;
    }

    //Handles startActivityForReset calls returning
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Bluetooth enable request
        if(requestCode == 1)
        {
            //End application if user didn't enable bluetooth because it's required to work
            if(resultCode == RESULT_CANCELED)
            {
                Toast.makeText(getApplicationContext(), "App requires bluetooth", Toast.LENGTH_LONG);
                finish();
            }
            if(resultCode == RESULT_OK)
            {
                //btleManager.startScanning();
                btMaster.startScanning();
            }
        }
    }

    //Custom class for handling gps change
    public class customLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            //Update the location
            currLocation = location;
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    }

    public class MenuArrayAdapter extends ArrayAdapter<CustomMenuItem>{

        public MenuArrayAdapter(Context context, int resource) {
            super(context, resource);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup container) {
            LayoutInflater inflater = LayoutInflater.from(getBaseContext());

            //Inflate the layout
            View customView = inflater.inflate(R.layout.menu_custom_listview, container, false);

            //Get references to widgets
            ImageView itemImageView = (ImageView) customView.findViewById(R.id.ivItemImage);
            TextView itemTextView = (TextView) customView.findViewById(R.id.ivItemWords);

            //Get the current item
            CustomMenuItem currentItem = getItem(position);

            //Put picture into listview and text into textview
            itemImageView.setImageResource(currentItem.getPictureResourceID());
            itemTextView.setText(currentItem.getText());

            //Return the layout
            return customView;
        }
    }

}
