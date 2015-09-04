package bit.hillcg2.SafetyMap;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ListView;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import android.widget.Toast;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import android.os.Vibrator;


@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class MainActivity extends Activity {

    //TODO clean up this class

    /***Global variables****/
    public static UUID UART_UUID = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E");
    public static UUID TX_UUID = UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E");
    public static UUID RX_UUID = UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E");
    public static final UUID CLIENT_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    public static final int SCAN_PERIOD = 5000;
    public static final int SCAN_DELAY = 5000;

    private BluetoothGatt connectedGatt;
    private BluetoothAdapter BTAdapter;
    private BluetoothGattCharacteristic RXChar;
    private BluetoothGattCharacteristic TXChar;

    private LocationManager locationManager;
    private Criteria defaultCriteria;
    private String providerName;

    private boolean isScanning;
    private boolean isConnected;
    private boolean locationUpdating;
    private boolean stopLoop;

    private customLocationListener customListener;
    private Location currLocation;

    private ArrayAdapter<String> messageAdapter;
    private MenuArrayAdapter menuAdapter;
    private ListView menuList;
    private ImageView btnConnectDevice;
    private ImageView imageCenterOfRotation;
    private TextView boxConnectionStatus;

    private DBManager dbManager;
    private Vibrator vibrator;
    private Timer doubleValueCheckerTimer;
    private Double timeBetweenValues;

    private Animation animation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //Get everything to initialize
        init();
    }

    //Method to initialize everything
    public void init(){
        //Check if phone supports low energy bluetooth
        if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE))
        {
            Toast.makeText(this, "Low energy bluetooth not supported", Toast.LENGTH_SHORT).show();
            finish();
        }

        //Get instance of bluetooth manager from system
        BluetoothManager BTManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        BTAdapter = BTManager.getAdapter();

        if(BTAdapter != null) {
            //Check if blue tooth is turned on
            if (!BTAdapter.isEnabled()) {
                //Bring up screen for user to enable bluetooth
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
        else
        {
            Toast.makeText(getBaseContext(), "Problem encountered", Toast.LENGTH_LONG).show();
            finish();
        }

        //Set up location service
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        defaultCriteria = new Criteria();
        providerName = locationManager.getBestProvider(defaultCriteria, false);
        customListener = new customLocationListener();
        currLocation = null;

        isScanning = false;
        isConnected = false;
        locationUpdating = false;
        stopLoop = false;

        //Get instance of class that manages the database
        dbManager = new DBManager(getBaseContext());

        vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);

        animation = AnimationUtils.loadAnimation(this, R.anim.rotate_around_center_point);

        //Adapter for the listview
        messageAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);

        imageCenterOfRotation = (ImageView)findViewById(R.id.imageView3);
        boxConnectionStatus = (TextView)findViewById(R.id.boxConnectionStatus);

        menuAdapter = new MenuArrayAdapter(getBaseContext(), R.layout.menu_custom_listview);

        menuList = (ListView)findViewById(R.id.menuList);
        menuList.setAdapter(menuAdapter);
        menuList.setOnItemClickListener(new menuListHandler());

        Drawable viewDataIcon = ContextCompat.getDrawable(this, R.drawable.view_data);
        CustomMenuItem menuData = new CustomMenuItem(getResources().getString(R.string.btn_view_incidents), viewDataIcon);

        Drawable mapIcon = ContextCompat.getDrawable(this, R.drawable.map);
        CustomMenuItem menuMap = new CustomMenuItem(getResources().getString(R.string.view_map), mapIcon);

        Drawable instructionsIcon = ContextCompat.getDrawable(this, R.drawable.instructions);
        CustomMenuItem menuInstructions = new CustomMenuItem(getResources().getString(R.string.instructions_text), instructionsIcon);

        menuAdapter.add(menuData);
        menuAdapter.add(menuMap);
        menuAdapter.add(menuInstructions);
        menuAdapter.notifyDataSetChanged();

        btnConnectDevice = (ImageView)findViewById(R.id.btnConnectToDevice);
        btnConnectDevice.setOnClickListener(new connectDevice());

        doubleValueCheckerTimer = new Timer();
        timeBetweenValues = 0.00;
    }//End init()

    //Tries to start scanning for bluetooth devices
    private void tryToScanForBTDevices(){
        //Check if it's already scanning or already connected to the device
        if(!isScanning && !isConnected && !stopLoop)
        {
            Animation animation = AnimationUtils.loadAnimation(this, R.anim.rotate_around_center_point);
            btnConnectDevice.startAnimation(animation);
            
            //Clear out the listbox to make it cleaner to look at
            messageAdapter.clear();
            boxConnectionStatus.setText("Searching");

            //Update value
            isScanning = true;
            //displayMessage("Please make sure device is switched on");
            //displayMessage("Scanning");

            //Start the scan
            BTAdapter.startLeScan(scanCallback);

            //Set up to make the scanning stop after a time period
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //Check if still scanning(connected to a device if it stopped)
                    if(isScanning)
                    {
                        //Stop the scanning
                        isScanning = false;
                        //displayMessage("Stopped Scanning");
                        BTAdapter.stopLeScan(scanCallback);

                        //Set up to start scanning again after a delay
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                tryToScanForBTDevices();
                            }
                        }, SCAN_PERIOD);
                    }

                }
            }, SCAN_DELAY);
        }
        else
        {
            //Give feedback to the user
            if(isScanning)
                Toast.makeText(getBaseContext(), "Already scanning", Toast.LENGTH_LONG).show();

            if(isConnected)
                Toast.makeText(getBaseContext(), "Already connected", Toast.LENGTH_LONG).show();
        }
    }

    //On resume, scan and connect back to the arduino
    @Override
    protected void onResume() {
        super.onResume();

        if(BTAdapter != null)
        {
            //Check if blue tooth is turned on
            if (BTAdapter.isEnabled())
            {
                if(!isScanning)
                    tryToScanForBTDevices();
            }
        }
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

        if(connectedGatt != null)
        {
            connectedGatt.disconnect();
            connectedGatt.close();
            connectedGatt = null;
        }

        if(isScanning)
            BTAdapter.stopLeScan(scanCallback);

        isScanning = false;
        isConnected = false;
        stopLoop = true;

        BTAdapter = null;

        RXChar = null;
        TXChar = null;
    }

    //Handles bluetooth related events
    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        //Called automatically when bluetooth changes state
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);

            //Check if bluetooth was connected
            if(newState == BluetoothGatt.STATE_CONNECTED)
            {
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
                    }
                });

                //Display message to user
                //displayMessage("Connected!");
                isConnected = true;

                //Try discover bluetooth devices services
                if(!gatt.discoverServices())
                {
                    //displayMessage("Failed to start discovering services");
                }
            }
            //Check if disconnected
            else if(newState == BluetoothGatt.STATE_DISCONNECTED)
            {
                //Since bluetooth runs on a different thread, need to talk to UI thread
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //Stop updating position to save on battery
                        if(locationUpdating)
                        {
                            locationManager.removeUpdates(customListener);
                            locationUpdating = false;
                        }

                        try
                        {
                            //Make sure that everything is set up so scanning will start if disconnected
                            //displayMessage("Stopped Scanning");
                            isScanning = false;
                            BTAdapter.stopLeScan(scanCallback);

                            //displayMessage("Disconnected");
                            isConnected = false;

                            //Close off gatt for a fresh connection
                            connectedGatt.disconnect();
                            connectedGatt.close();
                            connectedGatt = null;
                        }
                        catch(NullPointerException e)
                        {

                        }

                        //Set up to start scanning again after connection was lost
                        tryToScanForBTDevices();
                    }
                });
            }
            else
            {
                //displayMessage("Connection State changed. State: " + newState);
            }
        }

        //Called automatically when services are discovered
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            if(status == BluetoothGatt.GATT_SUCCESS)
            {
                //displayMessage("Service discovery completed");
            }
            else
            {
                //displayMessage("Service discovery failed. Status: " + status);
            }

            TXChar = gatt.getService(UART_UUID).getCharacteristic(TX_UUID);
            RXChar = gatt.getService(UART_UUID).getCharacteristic(RX_UUID);

            if(!gatt.setCharacteristicNotification(RXChar, true))
            {
                //displayMessage("Couldn't get RX notification");
            }

            if(RXChar.getDescriptor(CLIENT_UUID) != null)
            {
                BluetoothGattDescriptor descriptor = RXChar.getDescriptor(CLIENT_UUID);
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);

                if(!connectedGatt.writeDescriptor(descriptor))
                {
                    //displayMessage("Couldn't write descriptor");
                }
            }
            else
            {
                //displayMessage("Couldn't get descriptor");
            }
        }

        //Called when a value is sent over bluetooth
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);

            if(timeBetweenValues == 0.00)
            {
                //Update user on the value
                //displayMessage("Distance of object: " + characteristic.getStringValue(0) + "cm");

                vibrator.vibrate(1000);

                try
                {
                    //Make a incident from the value sent over
                    Incident newIncident = createIncident(characteristic.getStringValue(0));

                    //Insert value into DB
                    dbManager.insertIncident(newIncident);
                }
                catch (Exception e)
                {
                    Toast.makeText(getBaseContext(), "Error creating incident", Toast.LENGTH_LONG).show();
                }

                timeBetweenValues = 0.00;

                doubleValueCheckerTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        timerMethod();
                    }
                }, 0, 10);
            }
            else
            {
                if(timeBetweenValues > 0.5)
                {
                    doubleValueCheckerTimer.cancel();
                    timeBetweenValues = 0.00;
                }
            }
        }
    };

    //Method called by the timer, can only interact with timer thread
    public void timerMethod(){
        //Call thread to use UI
        runOnUiThread(Timer_Tick);
    }

    //Thread to interact with UI and update time
    private Runnable Timer_Tick = new Runnable() {
        @Override
        public void run() {
            timeBetweenValues += 0.01;
        }
    };

    //Method for handling scanning
    private LeScanCallback scanCallback = new LeScanCallback(){

        //Called when a device is found
        @Override
        public void onLeScan(final BluetoothDevice bluetoothDevice, int i, byte[] bytes) {
            //If true, UART device was found
            if(parseUUIDs(bytes).contains(UART_UUID)){
                //Stop scanning
                isScanning = false;
                BTAdapter.stopLeScan(scanCallback);


                //Connect to the device
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        connectedGatt = bluetoothDevice.connectGatt(getApplicationContext(), false, mGattCallback);
                    }
                });
            }
        }
    };

    //Handler for button to try scanning for device again if it stops for some reason, user can manually start it
    public class connectDevice implements OnClickListener{

        @Override
        public void onClick(View view) {
            Toast.makeText(getBaseContext(), "Scanning", Toast.LENGTH_LONG).show();
            tryToScanForBTDevices();
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

    //Pre-condition: Accepts a distance value as a string
    //Post-condition: Returns an incident with the passed in distance value
    public Incident createIncident(String newDistance){
        int distance = Integer.parseInt(newDistance);

        DateFormat df = new SimpleDateFormat("h:mm a");
        String time = df.format((Calendar.getInstance().getTime()));

        //Changed from ("d/MM/yyyy")
        df = new SimpleDateFormat("d/M/yyyy");
        String date = df.format((Calendar.getInstance().getTime()));

        //Get the most recent location value
        Location currentLocation = currLocation;
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
            lat = "null";
            lng = "null";
        }

        //Make an incident with all the values
        Incident newIncident = new Incident(distance, time, date, lat, lng);

        return newIncident;
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
                tryToScanForBTDevices();
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

    //Builds a list of the devices UUIDs
    private List<UUID> parseUUIDs(final byte[] advertisedData) {
        List<UUID> uuids = new ArrayList<UUID>();

        int offset = 0;
        while (offset < (advertisedData.length - 2)) {
            int len = advertisedData[offset++];
            if (len == 0)
                break;

            int type = advertisedData[offset++];
            switch (type) {
                case 0x02: // Partial list of 16-bit UUIDs
                case 0x03: // Complete list of 16-bit UUIDs
                    while (len > 1) {
                        int uuid16 = advertisedData[offset++];
                        uuid16 += (advertisedData[offset++] << 8);
                        len -= 2;
                        uuids.add(UUID.fromString(String.format("%08x-0000-1000-8000-00805f9b34fb", uuid16)));
                    }
                    break;
                case 0x06:// Partial list of 128-bit UUIDs
                case 0x07:// Complete list of 128-bit UUIDs
                    // Loop through the advertised 128-bit UUID's.
                    while (len >= 16) {
                        try {
                            // Wrap the advertised bits and order them.
                            ByteBuffer buffer = ByteBuffer.wrap(advertisedData, offset++, 16).order(ByteOrder.LITTLE_ENDIAN);
                            long mostSignificantBit = buffer.getLong();
                            long leastSignificantBit = buffer.getLong();
                            uuids.add(new UUID(leastSignificantBit,
                                    mostSignificantBit));
                        } catch (IndexOutOfBoundsException e) {
                            // Defensive programming.
                            //Log.e(LOG_TAG, e.toString());
                            continue;
                        } finally {
                            // Move the offset to read the next uuid.
                            offset += 15;
                            len -= 16;
                        }
                    }
                    break;
                default:
                    offset += (len - 1);
                    break;
            }
        }
        return uuids;
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
            itemImageView.setImageDrawable(currentItem.getPicture());
            itemTextView.setText(currentItem.getText());

            //Return the layout
            return customView;
        }
    }

}
