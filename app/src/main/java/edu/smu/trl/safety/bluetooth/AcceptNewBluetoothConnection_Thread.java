package edu.smu.trl.safety.bluetooth;

import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.SystemClock;

import java.io.IOException;
import java.util.UUID;

import edu.smu.trl.safety.utilities.Log;

public class AcceptNewBluetoothConnection_Thread extends Thread
{

	private static final String TAG = "Bluetooth Streaming";
	private static final UUID MY_UUID = UUID.fromString("66841278-c3d1-11df-ab31-001de000a901");
	private static final String NAME = "Android Bluetooth Messaging";
	// The local server socket
	private final BluetoothServerSocket BluetoothServerSocket;
	private final edu.smu.trl.safety.bluetooth.BluetoothChatService BluetoothChatService;


	public AcceptNewBluetoothConnection_Thread(edu.smu.trl.safety.bluetooth.BluetoothChatService BluetoothChatService)
	{
		this.BluetoothChatService = BluetoothChatService;
		BluetoothServerSocket BluetoothServerSocket_Temporary = null;

		// Create a new listening server socket
		while (BluetoothServerSocket_Temporary == null)
		{
			try
			{
				BluetoothServerSocket_Temporary = BluetoothChatService.RendererActivity.BluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
				SystemClock.sleep(50);

			}
			catch (IOException Exception)
			{
				Exception.printStackTrace();
				Log.e(TAG, "Listen() Failed", Exception);
			}
		}
		BluetoothServerSocket = BluetoothServerSocket_Temporary;
	}

	public void run()
	{
		Log.e(TAG, "BEGIN mAcceptThread" + this);
		setName("AcceptNewBluetoothConnection_Thread");

		BluetoothSocket BluetoothSocket;

		// Listen to the server socket if we're not connected
		while (BluetoothChatService.ConnectionState != Constants.STATE_CONNECTED)
		{
			try
			{
				// This is a blocking call and will only return on a
				// successful connection or an exception
				BluetoothSocket = BluetoothServerSocket.accept();
			}
			catch (IOException Exception)
			{
				Exception.printStackTrace();
				Log.e(TAG, "Accept() Failed", Exception);
				break;
			}

			// If a connection was accepted
			if (BluetoothSocket != null)
			{
				synchronized (BluetoothChatService)
				{
					switch (BluetoothChatService.ConnectionState)
					{
						case Constants.STATE_LISTEN:
						case Constants.STATE_CONNECTING:
							BluetoothChatService.Connected(BluetoothSocket, BluetoothSocket.getRemoteDevice());
							break;
						case Constants.STATE_NONE:
						case Constants.STATE_CONNECTED:
							try
							{
								BluetoothSocket.close();
							}
							catch (IOException Exception)
							{
								Exception.printStackTrace();
								Log.e(TAG, "Could Not Close Unwanted Socket", Exception);
							}
							break;
					}
				}
			}
		}
		Log.e(TAG, "End mAcceptThread.");
	}

	public void Cancel()
	{
		Log.e(TAG, "Cancel " + this);
		try
		{
			BluetoothServerSocket.close();
		}
		catch (IOException Exception)
		{
			Exception.printStackTrace();
			Log.e(TAG, "Close() of server failed", Exception);
		}
	}
}


