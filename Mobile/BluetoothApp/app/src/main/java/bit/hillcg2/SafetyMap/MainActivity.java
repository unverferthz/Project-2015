package bit.hillcg2.SafetyMap;

import android.annotation.TargetApi;
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
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import android.widget.Toast;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;
import android.os.Vibrator;


@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class MainActivity extends ActionBarActivity {

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

    private customLocationListener customListener;
    private Location currLocation;

    private Button btnConnect;
    //Button btnSend;
    private Button btnViewMap;
    private Button btnViewIncidents;
    ListView messageList;
    //EditText messageBox;
    private ArrayAdapter<String> messageAdapter;

    private DBManager dbManager;

    private Vibrator vibrator;

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

        //Get instance of class that manages the database
        dbManager = new DBManager(getBaseContext());

        vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);

        //Adapter for the listview
        messageAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);

        //Get instances of views from layout, and set them up
        btnConnect = (Button)findViewById(R.id.btnConnect);
        btnConnect.setOnClickListener(new connectDevice());

        btnViewIncidents = (Button)findViewById(R.id.btnViewIncidents);
        btnViewIncidents.setOnClickListener(new viewIncidents());

        btnViewMap = (Button)findViewById(R.id.btnViewMap);
        btnViewMap.setOnClickListener(new viewMap());

        messageList = (ListView)findViewById(R.id.messageList);
        messageList.setAdapter(messageAdapter);

        /*btnSend = (Button)findViewById(R.id.btnSend);
        btnSend.setOnClickListener(new sendMessage());
        btnSend.setEnabled(false);*/

        //Set up edittext
        /*messageBox = (EditText)findViewById(R.id.messageBox);
        messageBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //Enable button to send message when the edittext has been typed in
                btnSend.setEnabled(true);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });*/

        //Set up listview
    }//End init()

    //Tries to start scanning for bluetooth devices
    private void tryToScanForBTDevices(){
        //Check if it's already scanning or already connected to the device
        if(!isScanning && !isConnected)
        {
            //Clear out the listbox to make it cleaner to look at
            messageAdapter.clear();

            //Update value
            isScanning = true;
            displayStatus("Please make sure device is switched on");
            displayStatus("Scanning");

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
                        displayStatus("Stopped Scanning");
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
                    }
                });

                //Display message to user
                displayStatus("Connected!");
                isConnected = true;

                //Try discover bluetooth devices services
                if(!gatt.discoverServices())
                {
                    //displayStatus("Failed to start discovering services");
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


                        //TODO might not be needed
                        /******************************/
                        /*displayStatus("Stopped Scanning");
                        isScanning = false;
                        BTAdapter.stopLeScan(scanCallback);*/
                        /******************************/

                        displayStatus("Disconnected");
                        isConnected = false;

                        //Set up to start scanning again after connection was lost
                        tryToScanForBTDevices();
                    }
                });
            }
            else
            {
                displayStatus("Connection State changed. State: " + newState);
            }
        }

        //Called automatically when services are discovered
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            if(status == BluetoothGatt.GATT_SUCCESS)
            {
                //displayStatus("Service discovery completed");
            }
            else
            {
                //displayStatus("Service discovery failed. Status: " + status);
            }

            TXChar = gatt.getService(UART_UUID).getCharacteristic(TX_UUID);
            RXChar = gatt.getService(UART_UUID).getCharacteristic(RX_UUID);

            if(!gatt.setCharacteristicNotification(RXChar, true))
            {
                displayStatus("Couldn't get RX notification");
            }

            if(RXChar.getDescriptor(CLIENT_UUID) != null)
            {
                BluetoothGattDescriptor descriptor = RXChar.getDescriptor(CLIENT_UUID);
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);

                if(!connectedGatt.writeDescriptor(descriptor))
                {
                    //displayStatus("Couldn't write descriptor");
                }
            }
            else
            {
                //displayStatus("Couldn't get descriptor");
            }
        }

        //Called when a value is sent over bluetooth
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);

            //Update user on the value
            displayStatus("Distance of object: " + characteristic.getStringValue(0) + "cm");

            vibrator.vibrate(1000);

            try
            {
                //Make a incident from the value sent over
                Incident newIncident = createIncident(characteristic.getStringValue(0));

                //Insert value into DB
                dbManager.insertIncident(newIncident);
            }
            catch(Exception e)
            {
                Toast.makeText(getBaseContext(), "Error creating incident", Toast.LENGTH_LONG).show();
            }
        }
    };

    //Method for handling scanning
    private LeScanCallback scanCallback = new LeScanCallback(){

        //Called when a device is found
        @Override
        public void onLeScan(BluetoothDevice bluetoothDevice, int i, byte[] bytes) {
            String name = bluetoothDevice.getName();
            //String address = bluetoothDevice.getAddress();
            //displayStatus("Found device: " + name + ", " + address);

            //If true, UART device was found
            if(parseUUIDs(bytes).contains(UART_UUID)){
                //Inform user that the device is being connected to
                displayStatus("Scanning stopped");
                displayStatus("Connecting to " + name);

                //Stop scanning
                isScanning = false;
                BTAdapter.stopLeScan(scanCallback);

                //displayStatus("Found UART device");

                //Connect to the device
                connectedGatt = bluetoothDevice.connectGatt(getApplicationContext(), false, mGattCallback);
            }
        }
    };

    //Puts the string message into the lisview for user to see
    //Can be called from threads that don't run on UI
    private void displayStatus(final String message){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
                messageAdapter.add(message);
                messageAdapter.notifyDataSetChanged();
                messageList.setSelection(messageAdapter.getCount() - 1);
            }
        });
    }

    //Handler for button to try scanning for device again if it stops for some reason, user can manually start it
    public class connectDevice implements OnClickListener{

        @Override
        public void onClick(View view) {
            tryToScanForBTDevices();
        }
    }

    public class viewIncidents implements OnClickListener{

        @Override
        public void onClick(View view) {
            //Clean things up to prepare for exiting activity
            finishForActivityChange();

            //Send user to other activity
            Intent newIntent = new Intent(getBaseContext(), ViewIncidents.class);
            startActivity(newIntent);
        }
    }

    public class viewMap implements OnClickListener{

        @Override
        public void onClick(View view) {
            //Clean things up to prepare for exiting activity
            finishForActivityChange();

            //Send user to other activity
            Intent newIntent = new Intent(getBaseContext(), MapActivity.class);
            startActivity(newIntent);
        }
    }

    //Class for handling sending a message over bluetooth
    /*public class sendMessage implements OnClickListener{

        @Override
        public void onClick(View view) {
            String message = messageBox.getText().toString();
            messageBox.setText("");

            btnSend.setEnabled(false);

            if(TXChar == null || message == null || message.isEmpty())
            {
                return;
            }

            TXChar.setValue(message.getBytes(Charset.forName("UTF-8")));
            if(connectedGatt.writeCharacteristic(TXChar))
            {
                Toast.makeText(getBaseContext(), "Sent", Toast.LENGTH_LONG).show();
            }
            else
            {
                Toast.makeText(getBaseContext(), "Couldn't send", Toast.LENGTH_LONG).show();
            }
        }
    }*/

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

    //Pre-condition: Accepts a distance value as a string
    //Post-condition: Returns an incident with the passed in distance value
    public Incident createIncident(String newDistance){
        int distance = Integer.parseInt(newDistance);

        DateFormat df = new SimpleDateFormat("h:mm a");
        String time = df.format((Calendar.getInstance().getTime()));

        df = new SimpleDateFormat("d/MM/yyyy");
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

}
