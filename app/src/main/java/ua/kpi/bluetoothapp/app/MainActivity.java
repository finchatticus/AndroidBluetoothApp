package ua.kpi.bluetoothapp.app;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class MainActivity extends Activity {

    private static final int REQUEST_ENABLE_BT = 1;
    private Button onBtn;
    private Button offBtn;
    private Button listBtn;
    private Button findBtn;
    private TextView text;
    private BluetoothAdapter myBluetoothAdapter;
    private Set<BluetoothDevice> pairedDevices;
    private ListView myListView;
    private ArrayAdapter<String> BTArrayAdapter;
    private List<BluetoothDevice> bluetoothList = new ArrayList<BluetoothDevice>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);

        // take an instance of BluetoothAdapter - Bluetooth radio
        myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(myBluetoothAdapter == null) {
            onBtn.setEnabled(false);
            offBtn.setEnabled(false);
            listBtn.setEnabled(false);
            findBtn.setEnabled(false);
            text.setText("Status: not supported");

            Toast.makeText(getApplicationContext(), "Your device does not support Bluetooth", Toast.LENGTH_LONG).show();
        } else {
            text = (TextView) findViewById(R.id.text);
            onBtn = (Button)findViewById(R.id.turnOn);
            onBtn.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    on(v);
                }
            });

            offBtn = (Button)findViewById(R.id.turnOff);
            offBtn.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    off(v);
                }
            });

            listBtn = (Button)findViewById(R.id.paired);
            listBtn.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    list(v);
                }
            });

            findBtn = (Button)findViewById(R.id.search);
            findBtn.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    find(v);
                }
            });

            myListView = (ListView)findViewById(R.id.listView1);

            // create the arrayAdapter that contains the BTDevices, and set it to the ListView
            BTArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
            myListView.setAdapter(BTArrayAdapter);


            myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String deviceInfo = ((TextView) view).getText().toString();
                    String[] arrDeviceInfo = deviceInfo.split("\n");
                    String name = arrDeviceInfo[0];
                    String bluetoothClass = arrDeviceInfo[1];
                    String bluetoothMAC = arrDeviceInfo[2];
                    String bluetoothBondState = arrDeviceInfo[3];
                    Log.d("myLogs", "itemClick: position = " + position + ", id = " + id + ", name = " + name + ", class = " + bluetoothClass + ", deviceMAC = " + bluetoothMAC + ", bond state = " + bluetoothBondState );
                    Log.d("myLogs bl dev", "itemClick: list = " + bluetoothList.get(position).toString() + ", size = " + bluetoothList.size());
                }
            });
        }
    }

    public void on(View view){
        if (!myBluetoothAdapter.isEnabled()) {
            Intent turnOnIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOnIntent, REQUEST_ENABLE_BT);

            Toast.makeText(getApplicationContext(),"Bluetooth turned on" ,
                    Toast.LENGTH_LONG).show();
        }
        else {
            Toast.makeText(getApplicationContext(),"Bluetooth is already on",
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        if(requestCode == REQUEST_ENABLE_BT){
            if(myBluetoothAdapter.isEnabled()) {
                text.setText("Status: Enabled");
            } else {
                text.setText("Status: Disabled");
            }
        }
    }

    public void list(View view){
        // get paired devices
        pairedDevices = myBluetoothAdapter.getBondedDevices();

        // put it's one to the adapter
        for(BluetoothDevice device : pairedDevices)
            BTArrayAdapter.add(device.getName()+ "\n" + getBluetoothClass(device.getBluetoothClass().getDeviceClass()) + "\n" + device.getAddress() + "\n" + getBondState(device.getBondState()));

        Toast.makeText(getApplicationContext(),"Show Paired Devices",
                Toast.LENGTH_SHORT).show();

    }

    final BroadcastReceiver bReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                /*
                //add only paired and MISC devices
                if(device.getBluetoothClass().getDeviceClass() == 0 && device.getBondState() == 12) {
                    // add the name and the MAC address of the object to the arrayAdapter
                    BTArrayAdapter.add(device.getName() + "\n" + getBluetoothClass(device.getBluetoothClass().getDeviceClass()) + "\n" + device.getAddress() + "\n" + getBondState(device.getBondState()));
                    BTArrayAdapter.notifyDataSetChanged();

                    bluetoothList.add(device);
                }
                */
                //add only MISC devices
                if(device.getBluetoothClass().getDeviceClass() == 0) {
                    // add the name and the MAC address of the object to the arrayAdapter
                    BTArrayAdapter.add(device.getName() + "\n" + getBluetoothClass(device.getBluetoothClass().getDeviceClass()) + "\n" + device.getAddress() + "\n" + getBondState(device.getBondState()));
                    BTArrayAdapter.notifyDataSetChanged();

                    bluetoothList.add(device);
                }
            }
        }
    };

    public void find(View view) {
        if (myBluetoothAdapter.isDiscovering()) {
            // the button is pressed when it discovers, so cancel the discovery
            myBluetoothAdapter.cancelDiscovery();
            Toast.makeText(getApplicationContext(),"Bluetooth cancel", Toast.LENGTH_LONG).show();
        }
        else {
            BTArrayAdapter.clear();
            bluetoothList.clear();
            myBluetoothAdapter.startDiscovery();
            Toast.makeText(getApplicationContext(),"Bluetooth search", Toast.LENGTH_LONG).show();

            registerReceiver(bReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        }
    }

    public void off(View view){
        myBluetoothAdapter.disable();
        text.setText("Status: Disconnected");

        Toast.makeText(getApplicationContext(),"Bluetooth turned off",
                Toast.LENGTH_LONG).show();
    }

    private String getBluetoothClass(int i) {
        switch (i) {
            case BluetoothClass.Device.Major.AUDIO_VIDEO:
                return "AUDIO_VIDEO" + "=" + i;
            case BluetoothClass.Device.Major.COMPUTER:
                return "COMPUTER" + "=" + i;
            case BluetoothClass.Device.Major.HEALTH:
                return "HEALTH" + "-" + i;
            case BluetoothClass.Device.Major.IMAGING:
                return "IMAGING" + "=" + i;
            case BluetoothClass.Device.Major.MISC:
                return "MISC" + "=" + i;
            case BluetoothClass.Device.Major.NETWORKING:
                return "NETWORKING" + "=" + i;
            case BluetoothClass.Device.Major.PERIPHERAL:
                return "PERIPHERAL" + "=" + i;
            case BluetoothClass.Device.Major.PHONE:
                return "PHONE" + "=" + i;
            case BluetoothClass.Device.Major.TOY:
                return "TOY" + "=" + i;
            case BluetoothClass.Device.Major.UNCATEGORIZED:
                return "UNCATEGORIZED" + "-" + i;
            case BluetoothClass.Device.Major.WEARABLE:
                return "WEARABLE" + "=" + i;
            default:
                return "NOT_SPECIFIED" + "=" + i;
        }
    }

    private String getBondState(int i) {
        switch (i) {
            case BluetoothDevice.BOND_BONDED:
                return "PAIRED=" + i;
            case BluetoothDevice.BOND_BONDING:
                return "PAIRING=" + i;
            case BluetoothDevice.BOND_NONE:
                return "NOT_PAIRED=" + i;
            default:
                return "UNKNOWN=" + i;
        }
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        unregisterReceiver(bReceiver);
    }
}
