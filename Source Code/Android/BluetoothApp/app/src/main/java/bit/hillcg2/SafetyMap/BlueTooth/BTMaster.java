package bit.hillcg2.SafetyMap.BlueTooth;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.widget.Toast;
import java.util.ArrayList;
import bit.hillcg2.SafetyMap.MainActivity;

public class BTMaster {

    //Globals
    private static final int REQUEST_ENABLE_BT = 1;
    private BTLEManager btleManager;
    private BTManager btManager;
    private ArrayList<BluetoothDevice> deviceList;
    private MainActivity mainActivity;
    private boolean connected;

    //Constructor, sets up the bluetooth manager classes
    public BTMaster(MainActivity mainActivity){
        btleManager = new BTLEManager(mainActivity, this);
        btManager = new BTManager(mainActivity, this);
        deviceList = new ArrayList<>();
        this.mainActivity = mainActivity;
        connected = false;
    }

    //Pre-condition: None
    //Post-condition: Returns true if bluetooth enabled, false if not and asks user to enable it. Closes app if device doesn't support bluetooth
    public boolean checkBTEnabled(){
        BluetoothAdapter BTAdapter = BluetoothAdapter.getDefaultAdapter();

        if (BTAdapter == null) {
            Toast.makeText(mainActivity, "Device does not support bluetooth", Toast.LENGTH_LONG).show();
            mainActivity.finish();
        }
        if (!BTAdapter.isEnabled())
        {
            Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            mainActivity.startActivityForResult(enableBT, REQUEST_ENABLE_BT);
            return false;
        }

        return true;
    }

    //Pre-condition: None
    //Post-condition: Start scanning with both bluetooth types
    public void startScanning(){
        if(!connected)
        {
            if(btleManager != null)
                btleManager.startScanning();
            if(btManager != null)
                btManager.startScanning();
        }
    }

    //Pre-condition: None
    //Post-condition: Stops both bluetooth types scanning
    public void stopScans(){
        if(btleManager != null)
            btleManager.stopScan();
        if(btManager != null)
            btManager.stopScanning();
    }

    //Pre-condition: None
    //Post-condition: When bluetooth disconnected, inform main activity and start scanning again
    public void disconnected(){
        mainActivity.disconnectedFromBT();
        connected = false;
        startScanning();
    }

    //Pre-condition: None
    //Post-condition: Close off all connections
    public void closeConnections(){
        if(btManager != null)
        {
            btManager.stopScanning();
            btManager.closeConnections();
            btManager = null;
        }
        if(btleManager != null)
        {
            btleManager.closeConnection();
            btleManager = null;
        }
    }

    //Pre-condition: Device needs to be one of the Arduinos we've made
    //Post-condition: Called when an Arduino has been found. Figures out which connection type is required and tries to connect to it
    public void deviceFound(BluetoothDevice device){
        stopScans();

        //Inform main activity
        mainActivity.connectedToBT();
        connected = true;

        try
        {
            String name = device.getName();

            //Check which device it is to determine which kind of connection is needed
            if(name.equals("Ardu"))
            {
                btleManager.connectToDevice(device);
                btManager.closeConnections();
                btManager = null;
            } else
            {
                btManager.ConnectToDevice(device);
                btleManager = null;
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    //Pre-condition: Message needs to be a number that's a distance passed in from the arduino
    //Post-condition:Pass message to main activity
    public void messageReceived(String message){
        mainActivity.messageReceived(message);
    }
}
