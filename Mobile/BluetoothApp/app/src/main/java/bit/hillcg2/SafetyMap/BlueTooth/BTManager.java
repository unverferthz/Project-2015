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

    Handler handler;

    public BTManager(MainActivity activity, BTMaster btMaster){

        mainActivity = activity;
        this.btMaster = btMaster;
        init();
    }

    private void init(){
        BTAdapter = BluetoothAdapter.getDefaultAdapter();
        deviceList = new ArrayList<BluetoothDevice>();
        connectionThread = null;
        connectThread = null;
        isConnected = false;
        isScanning = false;
        stopLoop = false;

        collectedMessage = "";

        // Register the BroadcastReceiver
        deviceFoundFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        disconnectedFilter = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        mainActivity.registerReceiver(mReceiver, deviceFoundFilter);
        mainActivity.registerReceiver(mReceiver, disconnectedFilter);
    }

    //Disconnect from device if already connected. Reset everything and scan for devices
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

    //Call on ondestroy from main activity
    public void closeConnections(){
        if(connectionThread != null)
            connectionThread.cancel();

        if(connectThread != null)
            connectThread.cancel();

        BTAdapter.cancelDiscovery();
        BTAdapter = null;

        mainActivity.unregisterReceiver(mReceiver);
    }

    //Connect to the bluetooth device passed in
    public void ConnectToDevice(BluetoothDevice device)
    {
        connectThread = new ConnectThread(device);
        connectThread.run();
    }

    //Start scanning for bluetooth devices
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

    public void stopScanning(){
        stopLoop = true;
        BTAdapter.cancelDiscovery();

        if(handler != null)
        {
            handler.removeCallbacksAndMessages(null);
            handler = null;
        }
    }

    //Send message passed in over bluetooth
    public void sendMessage(String message){
        connectionThread.write(message);
    }

    //Adds device into device list
    public void addFoundDevice(BluetoothDevice device){
        deviceList.add(device);
    }

    //Returns a list of all the bluetooth devices in the area
    public ArrayList<BluetoothDevice> getDeviceList(){
        return deviceList;
    }

    //Class for handling connection to bluetooth device
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private Thread thread;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            thread = null;

            try
            {
                tmpIn = mmSocket.getInputStream();
                tmpOut = mmSocket.getOutputStream();
            }
            catch (IOException e)
            {
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        //Method for reading from bluetooth device
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

                                collectedMessage += message;
                                if(collectedMessage.contains("|"))
                                {
                                    int count = 0;
                                    for(int i=0; i < collectedMessage.length(); i++)
                                    {
                                        if(collectedMessage.charAt(i) == '|')
                                        {
                                            count++;
                                        }
                                    }

                                    ArrayList<String> messages = new ArrayList<String>();

                                    while(count > 0)
                                    {
                                        //-2 to eliminate the /r/n attatched to the end
                                        int indexOfPipe = collectedMessage.indexOf("|") - 2;
                                        messages.add(collectedMessage.substring(0, indexOfPipe));
                                        collectedMessage = collectedMessage.substring(indexOfPipe + 3);
                                        count--;
                                    }

                                    //Send message
                                    for(String s:messages)
                                    {
                                        btMaster.messageRecieved(s);
                                    }
                                }
                            }
                            catch (IOException e)
                            {
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

            thread.start();
        }

        //TODO make this stop crashing if not connected
        /* Call this from the main activity to send data to the remote device */
        public void write(String message) {
            try {
                byte[] bytes = message.getBytes();
                mmOutStream.write(bytes);
            } catch (IOException e) {
            }
        }

        /* Call this from the main activity to shutdown the connection */
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
            }
        }
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if(BluetoothDevice.ACTION_FOUND.equals(action))
            {
                //Get BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                boolean alreadyInList = false;

                for(BluetoothDevice b : deviceList)
                {
                    if(b.getAddress().equals(device.getAddress()))
                        alreadyInList = true;
                }

                if(!alreadyInList)
                {
                    addFoundDevice(device);

                    //mainActivity.deviceFound(device);

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

        public ConnectThread(BluetoothDevice device) {
            BluetoothSocket tmp = null;
            mmDevice = device;

            try
            {
                tmp = mmDevice.createRfcommSocketToServiceRecord(MY_UUID);
            }
            catch(IOException e)
            {
            }
            mmSocket = tmp;
        }

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
                }
                return;
            }

            //Bluetooth connected
            connectionThread = new ConnectedThread(mmSocket);

            //Run method that listens for bluetooth messages
            connectionThread.run();

            //cancel();
        }

        /**
         Will cancel an in-progress connection, and close the socket
         **/
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }
    }
}
