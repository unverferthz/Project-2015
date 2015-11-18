package bit.hillcg2.SafetyMap.BlueTooth;


import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.os.Build;
import android.os.Handler;
import android.widget.Toast;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import bit.hillcg2.SafetyMap.MainActivity;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BTLEManager {

    //Globals
    public static final int SCAN_PERIOD = 5000;
    public static final int SCAN_DELAY = 5000;

    public static UUID UART_UUID = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E");
    public static UUID TX_UUID = UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E");
    public static UUID RX_UUID = UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E");
    public static final UUID CLIENT_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    private BluetoothGatt connectedGatt;
    private BluetoothAdapter BTAdapter;
    private BluetoothGattCharacteristic RXChar;
    private BluetoothGattCharacteristic TXChar;

    private boolean isScanning;
    private boolean isConnected;
    private boolean stopLoop;

    private Timer doubleValueCheckerTimer;
    private Double timeBetweenValues;

    private MainActivity mainActivity;
    private BTMaster btMaster;
    private Handler handler;

    //Constructor
    public BTLEManager(Activity activity, BTMaster btMaster){
        mainActivity = (MainActivity)activity;
        doubleValueCheckerTimer = new Timer();
        timeBetweenValues = 0.00;
        this.btMaster = btMaster;

        //Get instance of bluetooth manager from system
        BluetoothManager BTManager = (BluetoothManager)mainActivity.getSystemService(mainActivity.BLUETOOTH_SERVICE);
        BTAdapter = BTManager.getAdapter();
    }

    //Pre-condition: None
    //Post-condition: Tries to start scanning for bluetooth devices
    public void startScanning(){
        //Check if it's already scanning or already connected to the device
        if(!isScanning && !isConnected && !stopLoop)
        {
            mainActivity.scanning();

            //Update value
            isScanning = true;

            //Start the scan
            BTAdapter.startLeScan(scanCallback);

            //Set up to make the scanning stop after a time period
            handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //Check if still scanning(connected to a device if it stopped)
                    if (isScanning) {
                        //Stop the scanning
                        isScanning = false;
                        BTAdapter.stopLeScan(scanCallback);

                        //Set up to start scanning again after a delay
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                startScanning();
                            }
                        }, SCAN_PERIOD);
                    }
                }
            }, SCAN_DELAY);
        }
    }

    //Pre-condition: Device passed in should be compatible with bluetooth low energy
    //Post-condition: Starts a connection to the bluetooth device
    public void connectToDevice(final BluetoothDevice device)
    {
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                connectedGatt = device.connectGatt(mainActivity.getApplicationContext(), false, mGattCallback);
            }
        });

    }

    //Pre-condition: None
    //Post-condition: Stops scanning for bluetooth devices if already scanning
    public void stopScan(){
        stopLoop = true;
        if(isScanning)
            BTAdapter.stopLeScan(scanCallback);

        if(handler != null)
        {
            handler.removeCallbacksAndMessages(null);
            handler = null;
        }
    }

    //Pre-condition: None
    //Post-condition: Closes connection to bluetooth device if connection and resets all values related to it
    public void closeConnection(){
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
                mainActivity.connectedToBT();

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
                btMaster.disconnected();

                try
                {
                    //Make sure that everything is set up so scanning will start if disconnected
                    isScanning = false;
                    isConnected = false;
                    stopLoop = false;
                    BTAdapter.stopLeScan(scanCallback);

                    //Close off gatt for a fresh connection
                    connectedGatt.disconnect();
                    connectedGatt.close();
                    connectedGatt = null;
                    btMaster.disconnected();

                }
                catch(NullPointerException e)
                {
                    e.printStackTrace();
                }
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

            //If statement to stop receiving double values from bluetooth
            if(timeBetweenValues == 0.00)
            {
                try
                {
                    btMaster.messageReceived(characteristic.getStringValue(0));
                }
                catch (Exception e)
                {
                    Toast.makeText(mainActivity.getBaseContext(), "Error creating incident", Toast.LENGTH_LONG).show();
                }

                timeBetweenValues = 0.00;

                doubleValueCheckerTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        timeBetweenValues += 0.01;
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

    //Function for handling scanning
    private BluetoothAdapter.LeScanCallback scanCallback = new BluetoothAdapter.LeScanCallback(){

        //Called when a device is found
        @Override
        public void onLeScan(final BluetoothDevice bluetoothDevice, int i, byte[] bytes) {
            //If true, UART device was found
            if(parseUUIDs(bytes).contains(UART_UUID)){
                if(bluetoothDevice.getName().equals("Ardu"))
                {
                    //Stop scanning
                    isScanning = false;
                    BTAdapter.stopLeScan(scanCallback);

                    //Inform bt master class that device was found
                    btMaster.deviceFound(bluetoothDevice);
                    //connectedGatt = bluetoothDevice.connectGatt(mainActivity.getApplicationContext(), false, mGattCallback);
                }
            }
        }
    };

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
                            e.printStackTrace();
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