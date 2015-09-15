package bit.hillcg2.SafetyMap.BlueTooth;


import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Build;
import android.widget.Toast;

import java.util.ArrayList;

import bit.hillcg2.SafetyMap.MainActivity;

public class BTMaster {

    private static final int REQUEST_ENABLE_BT = 1;
    BTLEManager btleManager;
    BTManager btManager;
    ArrayList<BluetoothDevice> deviceList;
    MainActivity mainActivity;
    boolean connected;

    public BTMaster(MainActivity mainActivity){
        btleManager = new BTLEManager(mainActivity, this);
        btManager = new BTManager(mainActivity, this);
        deviceList = new ArrayList<>();
        this.mainActivity = mainActivity;
        connected = false;
    }

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

    public void startScanning(){
        if(!connected)
        {
            if(btleManager != null)
                btleManager.startScanning();
            if(btManager != null)
                btManager.startScanning();
        }
    }

    public void stopScans(){
        if(btleManager != null)
            btleManager.stopScan();
        if(btManager != null)
            btManager.stopScanning();
    }

    public void disconnected(){
        mainActivity.disconnectedFromBT();
        connected = false;
        startScanning();
    }

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

    public void deviceFound(BluetoothDevice device){
        stopScans();
        mainActivity.connectedToBT();
        connected = true;

        try
        {
            //int bluetoothType = device.getType();
            String name = device.getName();

            //if (bluetoothType == BluetoothDevice.DEVICE_TYPE_LE)
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
            //btManager.ConnectToDevice(device);
        }
    }

    public void messageRecieved(String message){
        mainActivity.messageRecieved(message);
    }
}
