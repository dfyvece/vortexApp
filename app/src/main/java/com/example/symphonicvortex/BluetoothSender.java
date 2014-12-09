package com.example.symphonicvortex;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

import android.annotation.TargetApi;
import android.bluetooth.*;
import android.os.Build;
import android.os.ParcelUuid;
import android.util.Log;

@TargetApi(Build.VERSION_CODES.KITKAT)
public class BluetoothSender implements AutoCloseable{
	
	private static final int REQUEST_ENABLE_BT = 500;
	private BluetoothAdapter mBluetoothAdapter;
	private Set<BluetoothDevice>pairedDevices;
    private BluetoothDevice dev;
	private boolean wasEnabled = true;
    private UUID MY_UUID;
    private ConnectedThread mComm = null;
    private BluetoothSocket mSocket;

	public BluetoothSender()
	{
		this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	    // Device has Bluetooth
		if(mBluetoothAdapter != null)
		{
			// Bluetooth is Enabled
			if (!mBluetoothAdapter.isEnabled()) {
				wasEnabled = false;
                mBluetoothAdapter.enable();
			}
			if (mBluetoothAdapter.isEnabled()) {


				pairedDevices = mBluetoothAdapter.getBondedDevices();
				if (pairedDevices.size() > 0) {
				    // Loop through paired devices to find our chip
				    for (BluetoothDevice device : pairedDevices) {
				        // Add the name and address to an array adapter to show in a ListView
				        //mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                        Log.d("VORTEXname", device.getName());
                        Log.d("VORTEXaddress", device.getAddress());
                        ParcelUuid[] uuids = device.getUuids();

                        if (device.getName().equals("HC-06")) {
                            for(ParcelUuid u: uuids)
                                MY_UUID = UUID.fromString(u.toString());
                            (new ConnectThread(device,this)).run();
                        }


                    }
				}
			}
		}
	}
   
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private final BluetoothSender mParent;

        public ConnectThread(BluetoothDevice device, BluetoothSender parent) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            mmDevice = device;
            mParent = parent;
            Log.d("VORTEX", "starting connect thread");


            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
                Log.d("VORTEX", "connect thread has socket");
            } catch (IOException e) {
                Log.d("VORTEX", "connect thread no socket");
            }

            mmSocket = tmp;

        }

        public void run() {
            // Cancel discovery because it will slow down the connection
            mBluetoothAdapter.cancelDiscovery();
            Log.d("VORTEX", "attempting to connect");


            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                try {
                    mmSocket.close();
                } catch (IOException closeException) { }
                Log.d("VORTEX", "could not get socket");

                return;
            }

            // Do work to manage the connection (in a separate thread)
            Log.d("VORTEX", "got bluetooth socket");

            manageConnectedSocket(mmSocket);
        }

        private void manageConnectedSocket(BluetoothSocket mmSocket) {
            Thread conn = new ConnectedThread(mmSocket, mParent);
            conn.start();
        }

        /** Will cancel an in-progress connection, and close the socket */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket, BluetoothSender parent) {
            mmSocket = socket;
            parent.mSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            parent.mComm = this;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
            Log.d("VORTEX", "connected to bluetooth");
        }

        public String byteToString(byte[] _bytes, int len)
        {
            String str = "";

            for(int i = 0; i < len; i++)
            {
                str += (char)_bytes[i];
            }

            return str;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    // Send the obtained bytes to the UI activity
                    Log.d("VORTEXBLUETOOTH", "Received message: '" + byteToString(buffer, bytes) + "'");
                    //mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) { }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }

	public void send(String message)
	{
		if (mComm != null) {
            message = message + "\n";
            Log.d("VORTEXSend", "sending message: " + message);
            mComm.write(message.getBytes());
        }
        else {
            Log.d("VORTEXSend", "NOT CONNECTED sending message: " + message);

        }
	}

    public boolean isConnected() {
        if (mSocket != null)
            return mSocket.isConnected();
        return false;
    }
	
	@Override
	public void close() throws Exception {
		if (mComm != null) mComm.cancel();
        if (!wasEnabled) mBluetoothAdapter.disable();
	}
}
