package bit.hillcg2.bluetoothdatasharing;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

public class ConnectThread extends Thread{
    private final BluetoothSocket mSocket;
    private final BluetoothDevice mDevice;
    private final BluetoothAdapter bluetoothAdapter;

    public ConnectThread(BluetoothDevice device, BluetoothAdapter adapter){
        BluetoothSocket tmp = null;
        bluetoothAdapter = adapter;
        mDevice = device;

        try
        {
            //TelephonyManager tManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
            // String stringUUID = tManager.getDeviceId();

            //UUID needs to match the same one used by the server socket - find out what the arduino uses
            UUID uuid = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E");
            tmp = device.createInsecureRfcommSocketToServiceRecord(uuid);
        }
        catch(IOException e)
        {
            Log.e("Log", "Create failed", e);
        }
        mSocket = tmp;
    }

    public void run(){
        bluetoothAdapter.cancelDiscovery();
        setName("Connect Thead");

        try
        {
            mSocket.connect();
        }
        catch (IOException connectException)
        {
            try
            {
                mSocket.close();
            }
            catch(IOException closeException)
            {

            }
            return;
        }


        //Do work to manage connection (in a separate thread)
        //manageConnectionSocket(mSocket);
    }

    //Will cancel an in-progress connection, and close the socket
    public void cancel() {
        try
        {
            mSocket.close();
        }
        catch (IOException e)
        {
        }
    }
}