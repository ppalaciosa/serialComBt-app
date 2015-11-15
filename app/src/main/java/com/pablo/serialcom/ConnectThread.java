package com.pablo.serialcom;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by User on 6/3/2015.
 */

public class ConnectThread extends Thread {
    private final BluetoothSocket mmSocket;
    private final BluetoothDevice mmDevice;
    static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    BluetoothAdapter mBluetoothAdapter = MainActivity.mBluetoothAdapter;
    private OutputStream outStream = null;

    // Method to send data
    private void sendData(String message) {
        byte[] msgBuffer = message.getBytes();

        try {
            //attempt to place data on the outstream to the BT device
            outStream.write(msgBuffer);
        } catch (IOException e) {
            //if the sending fails this is most likely because device is no longer there
            //Toast.makeText(getBaseContext(), "ERROR - Device not found", Toast.LENGTH_SHORT).show();
            //finish();
        }
    }

    public void manageConnectedSocket (BluetoothSocket Socket) {
        try {
            outStream = mmSocket.getOutputStream();
        } catch (IOException e) {
            //Toast.makeText(getBaseContext(), "ERROR - Could not create bluetooth outstream", Toast.LENGTH_SHORT).show();
            Log.d("CONNECTTHREAD", "ERROR - Could not create bluetooth outstream");
        }
        //When activity is resumed, attempt to send a piece of junk data ('x') so that it will fail if not connected
        // i.e don't wait for a user to press button to recognise connection failure

        //
        // "a" -> prende
        // "b" -> apaga
        sendData("a");

    }

    public ConnectThread(BluetoothDevice device) {
        // Use a temporary object that is later assigned to mmSocket,
        // because mmSocket is final
        BluetoothSocket tmp = null;
        mmDevice = device;
        // Get a BluetoothSocket to connect with the given BluetoothDevice
        try {
            // MY_UUID is the app's UUID string, also used by the server code
            tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            Log.d("CONNECTTHREAD", "Could not create RFCOMM socket:" + e.toString());
            //return false;
        }
        mmSocket = tmp;
    }

    public void run() {
        // Cancel discovery because it will slow down the connection
        mBluetoothAdapter.cancelDiscovery();

        try {
            // Connect the device through the socket. This will block
            // until it succeeds or throws an exception
            mmSocket.connect();
        } catch (IOException connectException) {
            // Unable to connect; close the socket and get out
            connectException.printStackTrace();
            try {
                mmSocket.close();
            } catch (IOException closeException) {
                closeException.printStackTrace();
            }
            return;
        }

        // Do work to manage the connection (in a separate thread)
        manageConnectedSocket(mmSocket);
    }


}