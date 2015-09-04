package bit.hillcg2.bluetoothtesting;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;
import java.util.ArrayList;
import android.widget.ListView;
import android.widget.Button;
import android.widget.ArrayAdapter;
import android.view.View.OnClickListener;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 1;

    private IntentFilter filter;
    private ListView listView;
    private Button btnSend;
    private Button btnReset;
    private BTManager btManager;
    private ArrayAdapter listAdapter;
    private Boolean connected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        btManager = new BTManager(this);
        if(btManager.CheckBTEnabled())
            init();
    }


    public void init() {
        btManager = new BTManager(this);
        connected = false;

        listAdapter = new ArrayAdapter(getBaseContext(), android.R.layout.simple_list_item_1);

        listView = (ListView)findViewById(R.id.listView);
        listView.setOnItemClickListener(new listClickHandler());
        listView.setAdapter(listAdapter);

        btnSend = (Button)findViewById(R.id.btnSend);
        btnSend.setOnClickListener(new sendMessage());

        btnReset = (Button)findViewById(R.id.btnReset);
        btnReset.setOnClickListener(new resetBT());

        // Register the BroadcastReceiver
        filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy

        btManager.startScanning();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(mReceiver);
    }

    public class resetBT implements OnClickListener{
        @Override
        public void onClick(View v) {
            listAdapter.clear();
            listAdapter.notifyDataSetChanged();
            btManager.reset();
        }
    }

    public class sendMessage implements OnClickListener{

        @Override
        public void onClick(View v) {
            btManager.sendMessage("Test Message");
        }
    }

    public class listClickHandler implements AdapterView.OnItemClickListener{

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if(!connected)
            {
                ArrayList<BluetoothDevice> deviceList = btManager.getDeviceList();
                BluetoothDevice clickedDevice = deviceList.get(position);

                listAdapter.clear();
                listAdapter.notifyDataSetChanged();

                Toast.makeText(getBaseContext(), "Connecting to " + clickedDevice.getName(), Toast.LENGTH_LONG).show();
                btManager.ConnectToDevice(clickedDevice);
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

                ArrayList<BluetoothDevice> deviceList = btManager.getDeviceList();

                for(BluetoothDevice b : deviceList)
                {
                    if(b.getAddress().equals(device.getAddress()))
                        alreadyInList = true;
                }

                if(!alreadyInList)
                {
                    btManager.addFoundDevice(device);

                    listAdapter.add("Device name: " + device.getName() + "\nDevice Address: " + device.getAddress());
                    listAdapter.notifyDataSetChanged();
                }
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode != RESULT_OK) {
                Toast.makeText(this, "App needs bluetooth", Toast.LENGTH_LONG).show();
                finish();
            }
            else if(resultCode == RESULT_OK)
            {
                init();
                btManager.startScanning();
            }
        }
    }

    public void displayMessage(String message){
        listAdapter.add(message);
        listAdapter.notifyDataSetChanged();
    }
}
