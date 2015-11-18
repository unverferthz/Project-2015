package bit.hillcg2.SafetyMap.BlueTooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;

import bit.hillcg2.SafetyMap.MainActivity;

public class BTManager {
    //Globals
    public static final int SCAN_PERIOD = 15000;
    public static final int SCAN_DELAY = 5000;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private ArrayList<BluetoothDevice> deviceList;
    private BluetoothAdapter BTAdapter;
    private ConnectedThread connectionThread;
    private ConnectThread connectThread;
    private IntentFilter deviceFoundFilter;
    private IntentFilter disconnectedFilter;

    private MainActivity mainActivity;
    private BTMaster btMaster;

    private String collectedMessage;

    private boolean isScanning;
    private boolean isConnected;
    private boolean stopLoop;

    private Handler handler;

    //Constructor
    public BTManager(MainActivity activity, BTMaster btMaster){

        mainActivity = activity;
        this.btMaster = btMaster;
        init();
    }

    //Pre-condition: None
    //Post-condition: Initialize everything that is needed
    private void init(){
        BTAdapter = BluetoothAdapter.getDefaultAdapter();
        deviceList = new ArrayList<BluetoothDevice>();
        connectionThread = null;
        connectThread = null;
        isConnected = false;
        isScanning = false;
        stopLoop = false;

        collectedMessage = "";

        // Register the BroadcastReceiver to main activity
        deviceFoundFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        disconnectedFilter = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        mainActivity.registerReceiver(mReceiver, deviceFoundFilter);
        mainActivity.registerReceiver(mReceiver, disconnectedFilter);
    }

    //Pre-condition: None
    //Post-condition: Disconnect from device if already connected. Reset everything to be ready to scan again
    public void reset(){
        if(connectionThread != null)
            connectionThread.cancel();

        if(connectThread != null)
            connectThread.cancel();

        BTAdapter.cancelDiscovery();
        BTAdapter = null;
        BTAdapter = BluetoothAdapter.getDefaultAdapter();

        deviceList = new ArrayList<BluetoothDevice>();
        collectedMessage = "";
        connectionThread = null;
        connectThread = null;
        isConnected = false;
        isScanning = false;
        stopLoop = false;
    }

    //Pre-condition: None
    //Post-condition: Call from another class to terminate connections
    public void closeConnections(){
        if(connectionThread != null)
            connectionThread.cancel();

        if(connectThread != null)
            connectThread.cancel();

        BTAdapter.cancelDiscovery();
        BTAdapter = null;

        mainActivity.unregisterReceiver(mReceiver);
    }

    //Pre-condition: Device needs to use a regular bluetooth connection, not low energy
    //Post-condition: Connect to the bluetooth device passed in
    public void ConnectToDevice(BluetoothDevice device)
    {
        connectThread = new ConnectThread(device);
        connectThread.run();
    }

    //Pre-condition: None
    //Post-condition: Start scanning for bluetooth devices
    public void startScanning(){
        //Check if it's already scanning or already connected to the device
        if(!isScanning && !isConnected && !stopLoop)
        {
            mainActivity.scanning();

            //Update value
            isScanning = true;

            //Start the scan
            BTAdapter.startDiscovery();

            //Set up to make the scanning stop after a time period
            handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //Check if still scanning(connected to a device if it stopped)
                    if (isScanning) {
                        //Stop the scanning
                        isScanning = false;
                        //displayMessage("Stopped Scanning");
                        BTAdapter.cancelDiscovery();

                        //Set up to start scanning again after a delay
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                startScanning();
                            }
                        }, SCAN_DELAY);
                    }

                }
            }, SCAN_PERIOD);
        }
    }

    //Pre-condition: None
    //Post-condition: Stops class from scanning for more devices
    public void stopScanning(){
        stopLoop = true;
        BTAdapter.cancelDiscovery();

        if(handler != null)
        {
            handler.removeCallbacksAndMessages(null);
            handler = null;
        }
    }

    //Pre-condition: Connection to a device required before use
    //Post-condition: Sends a message over bluetooth
    public void sendMessage(String message){
        connectionThread.write(message);
    }

    //Pre-condition: None
    //Post-condition: Adds bluetooth device into the device list
    public void addFoundDevice(BluetoothDevice device){
        deviceList.add(device);
    }

    //Class for handling connection to bluetooth device
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private Thread thread;

        //Constructor
        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            thread = null;

            try
            {
                //Try get input and output streams
                tmpIn = mmSocket.getInputStream();
                tmpOut = mmSocket.getOutputStream();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            //Set streams
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        //Pre-condition: Requires connection to be successful to bluetooth device
        //Post-condition: Starts listening for messages to be sent across bluetooth
        public void run() {
            thread = new Thread() {
                @Override
                public void run() {
                    try
                    {
                        while(true)
                        {
                            try
                            {
                                final byte[] buffer = new byte[1024]; // buffer store for the stream

                                //Blocking call
                                int bytes = mmInStream.read(buffer); // bytes returned from read()

                                String message = new String(buffer, 0, bytes);

                                //build up the message string
                                collectedMessage += message;

                                //Check if end of the message
                                if(collectedMessage.contains("|"))
                                {
                                    int count = 0;

                                    //Find where the end character is
                                    for(int i=0; i < collectedMessage.length(); i++)
                                    {
                                        if(collectedMessage.charAt(i) == '|')
                                        {
                                            count++;
                                        }
                                    }

                                    ArrayList<String> messages = new ArrayList<String>();

                                    //Get the message without the garbage characters in it
                                    while(count > 0)
                                    {
                                        //-2 to eliminate the /r/n attatched to the end
                                        int indexOfPipe = collectedMessage.indexOf("|") - 2;
                                        messages.add(collectedMessage.substring(0, indexOfPipe));
                                        collectedMessage = collectedMessage.substring(indexOfPipe + 3);
                                        count--;
                                    }

                                    //Send message to btMaster class
                                    for(String s:messages)
                                    {
                                        btMaster.messageReceived(s);
                                    }
                                }
                            }
                            catch (IOException e)
                            {
                                e.printStackTrace();
                                break;
                            }
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            };

            //Start asynchronous method
            thread.start();
        }

        //Pre-condition: Requires a successful connection to a bluetooth device
        //Post-condition: Sends a message over bluetooth
        public void write(String message) {
            if(mmOutStream != null)
            {
                try
                {
                    byte[] bytes = message.getBytes();
                    mmOutStream.write(bytes);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }

        //Pre-condition: None
        //Post-condition: Closes off the connection to the bluetooth device
        public void cancel() {
            try
            {
                thread.interrupt();
                thread = null;

                mmInStream.close();
                mmOutStream.close();

                mmSocket.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    //Pre-condition: Broadcast receiver needs to be registered
    //Post-condition: Used to listen out for events like when a bluetooth device is found, discovered etc
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            //If bluetooth device was found
            if(BluetoothDevice.ACTION_FOUND.equals(action))
            {
                //Get BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                boolean alreadyInList = false;

                //Check if device was already in the gathered list
                for(BluetoothDevice b : deviceList)
                {
                    if(b.getAddress().equals(device.getAddress()))
                        alreadyInList = true;
                }

                //Add into list if not already in
                if(!alreadyInList)
                {
                    addFoundDevice(device);

                    //If device is one of our arduinos, inform BTMaster
                    String deviceName = device.getName();
                    if(deviceName != null)
                    {
                        if (deviceName.equals("BLuey"))
                            btMaster.deviceFound(device);
                        else if(deviceName.equals("BLuey-184C"))
                            btMaster.deviceFound(device);
                    }
                }
            }

            //If disconnected from bluetooth device, reset everything and set up to scan again
            if(BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action))
            {
                reset();
                btMaster.disconnected();
            }
        }
    };

    //Class for connecting to bluetooth device
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        //Constructor that sets up the communication socket
        public ConnectThread(BluetoothDevice device) {
            BluetoothSocket tmp = null;
            mmDevice = device;

            try
            {
                tmp = mmDevice.createRfcommSocketToServiceRecord(MY_UUID);
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
            mmSocket = tmp;
        }

        //Pre-condition: None
        //Post-condition: Tries to connect to the already set up socket
        public void run() {
            BTAdapter.cancelDiscovery();

            try
            {
                mmSocket.connect();
            }
            catch (IOException connectException)
            {
                // Unable to connect; close the socket and get out
                try
                {
                    mmSocket.close();
                }
                catch(IOException closeException)
                {
                    closeException.printStackTrace();
                }
                return;
            }

            //Bluetooth connected, set up class that handles connection
            connectionThread = new ConnectedThread(mmSocket);

            //Run method that listens for bluetooth messages
            connectionThread.run();

            //cancel();
        }


        //Pre-condition: None
        //Post-condition: Will cancel an in-progress connection and close the socket
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
