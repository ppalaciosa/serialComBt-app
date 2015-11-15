package com.pablo.serialcom;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    // Valor de request
    private static final int REQUEST_ENABLE_BT = 1;

    //public ArrayAdapter<String> mArrayAdapter;
    private ListView listDevicesFound;
    private Button btScan;
    ArrayAdapter<BluetoothDevice> mArrayAdapter;
    ArrayList<BluetoothDevice> pairedDeviceArrayList;
    static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    static BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    ConnectThread mConnectThread;

    // *** NOTE ***
    // For each device, the system will broadcast the ACTION_FOUND Intent.
    // This Intent carries the extra fields EXTRA_DEVICE and EXTRA_CLASS,
    // containing a BluetoothDevice and a BluetoothClass, respectively.

    // Instanciando un BroadcastReceiver "mReceiver" (definido con clase anonima) para ACTION_FOUND
    // Registrando mReceiver que todas las clases puedan usar

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Add the name and address to an array adapter to show in a ListView
                Log.d(device.getName(), device.getAddress());
                String new_device = device.getName() + "\n" + device.getAddress();
                mArrayAdapter.add(device);
                Toast.makeText(getBaseContext(), "Dispositivo detectado:" + "\n" + new_device,
                        Toast.LENGTH_SHORT).show();
            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Toast.makeText(getBaseContext(), "Fin de la búsqueda", Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        Switch btButton = (Switch)findViewById(R.id.btButton);
        //set the switch to ON if BT is ON
        if (mBluetoothAdapter.isEnabled()) {
            btButton.setChecked(true);
        }

        btButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {

                    if (mBluetoothAdapter == null) {
                        // Mensaje de no soporte
                        Toast.makeText(getApplicationContext(),
                                "Bluetooth no es soportado por este hardware.",
                                Toast.LENGTH_LONG).show();
                        finish();
                    }

                    if (!mBluetoothAdapter.isEnabled()) { // Si esta apagado, prende
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        // else... pide activar Bluetooth
                        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                        Toast.makeText(getApplicationContext(), "Bluetooth encendido"
                                , Toast.LENGTH_LONG).show();
                    } else { // Si esta prendido, que siga asi
                        Toast.makeText(getApplicationContext(), "Bluetooth sigue encendido",
                                Toast.LENGTH_LONG).show();
                    }
                } else {
                    mBluetoothAdapter.disable();
                    Toast.makeText(getApplicationContext(), "Bluetooth apagado",
                            Toast.LENGTH_LONG).show();
                }
            }
        });


        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            pairedDeviceArrayList = new ArrayList<>();
            for (BluetoothDevice device : pairedDevices) {
                // Cada 'pairedDevice' es listado en mArrayAdapter.
                pairedDeviceArrayList.add(device);
            }
            Toast.makeText(getApplicationContext(),"Mostrando dispositivos apareados",
                    Toast.LENGTH_SHORT).show();

            listDevicesFound = (ListView)findViewById(R.id.devicesfound);
            mArrayAdapter = new ArrayAdapter<>(MainActivity.this,
                    android.R.layout.simple_list_item_1,pairedDeviceArrayList);
            listDevicesFound.setAdapter(mArrayAdapter); // Asociado data al ListView

        }

        // Descubrir dispositivos
        btScan = (Button)findViewById(R.id.scan);
        btScan.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mArrayAdapter.clear();
                mBluetoothAdapter.startDiscovery();
                Toast.makeText(getApplicationContext(), "Iniciando búsqueda de dispositivos bluetooth",
                        Toast.LENGTH_SHORT).show();
                IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy
            }
        });

        listDevicesFound.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BluetoothDevice device = (BluetoothDevice) parent.getItemAtPosition(position);
                Toast.makeText(MainActivity.this,
                        "Name: " + device.getName() + "\n"
                                + "Address: " + device.getAddress() + "\n"
                                + "BondState: " + device.getBondState() + "\n"
                                + "BluetoothClass: " + device.getBluetoothClass() + "\n"
                                + "Class: " + device.getClass(),
                        Toast.LENGTH_LONG).show();
                mConnectThread = new ConnectThread(device);
                mConnectThread.run();
            }
        });


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // desactivando mReceiver
        unregisterReceiver(mReceiver);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
