package bit.hillcg2.bluetoothdatasharing;

import android.os.Bundle;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import java.util.Set;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;


public class MainActivity extends Activity {

    ListView deviceList;
    Button btnListPaired;
    Button btnFindDevices;
    BluetoothAdapter bluetoothAdapter;
    Set<BluetoothDevice> pairedDevices;
    ArrayAdapter<String> bluetoothArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
    }

    private void init() {
        deviceList = (ListView)findViewById(R.id.deviceList);
        btnListPaired = (Button)findViewById(R.id.btnListPaired);
        btnFindDevices = (Button)findViewById(R.id.btnFindDevices);

        bluetoothArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        deviceList.setAdapter(bluetoothArrayAdapter);

        //Set event handlers
        btnListPaired.setOnClickListener(new listDevices());
        btnFindDevices.setOnClickListener(new findDevices());
        deviceList.setOnItemClickListener(new connectToClickedDevice());


        //Get instance of bluetooth adapter;
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        //Check if bluetooth is supported on the device
        if(bluetoothAdapter != null)
        {
            //Check if blue tooth is turned n
            if(!bluetoothAdapter.isEnabled())
            {
                //Bring up screen for user to enable bluetooth
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
        //Bluetooth not supported
        else
        {
            Toast.makeText(getApplicationContext(), "Bluetooth not supported", Toast.LENGTH_LONG);
        }
    }


    public class listDevices implements OnClickListener{

        @Override
        public void onClick(View view) {
            //Get paired devices
            pairedDevices = bluetoothAdapter.getBondedDevices();

            for(BluetoothDevice device: pairedDevices)
            {
                bluetoothArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }

            Toast.makeText(getApplicationContext(), "Listed paired devices", Toast.LENGTH_LONG);
        }
    }

    public class findDevices implements OnClickListener{
        @Override
        public void onClick(View view) {
            //Stop discovery if already discovering
            if(bluetoothAdapter.isDiscovering())
                bluetoothAdapter.cancelDiscovery();
            else
            {
                //Clear out previous listed devices
                bluetoothArrayAdapter.clear();

                //Start search for devices
                bluetoothAdapter.startDiscovery();

                registerReceiver(mReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
            }
        }
    }

    final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            //When discovery finds a new device
            if(BluetoothDevice.ACTION_FOUND.equals(action)){
                //Get found device
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //Add to array adapter
                bluetoothArrayAdapter.add(device.getName() + "\n" + device.getAddress());

                //Update list to show device
                bluetoothArrayAdapter.notifyDataSetChanged();
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        //Bluetooth enable request
        if(requestCode == 1)
        {
            if(resultCode == RESULT_CANCELED)
            {
                Toast.makeText(getApplicationContext(), "App requires bluetooth", Toast.LENGTH_LONG);
                finish();
            }
        }
    }

    public class connectToClickedDevice implements OnItemClickListener{

        @Override
        public void onItemClick(AdapterView<?> list, View item, int position, long id) {

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //Unregister receiver to stop crash
        unregisterReceiver(mReceiver);
    }
}
