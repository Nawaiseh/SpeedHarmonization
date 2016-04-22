package edu.smu.trl.safety.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.util.UUID;

import edu.smu.trl.safety.RendererActivity;
import edu.smu.trl.safety.utilities.Log;

public class BluetoothChatService
{

	// Constants that indicate the current connection state
	public static final int STATE_NONE = 0;       // we're doing nothing
	public static final int STATE_LISTEN = 1;     // now listening for incoming connections
	public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection

	// Debugging
	public static final int STATE_CONNECTED = 3;  // now connected to a remote device
	private static final String TAG = "BluetoothChatService";
	private static final String NAME = "AndroidLocomateMessaging";
	private static final UUID MY_UUID = UUID.fromString("66841278-c3d1-11df-ab31-001de000a901");
	public int ThreasholdDistance = 5;
	public static boolean AlertIsGoing = false;
	public long LastUpdate = System.currentTimeMillis();

	public final RendererActivity RendererActivity;
	public final Handler MessageHandler;
	public AcceptNewBluetoothConnection_Thread AcceptNewBluetoothConnection_Thread;
	public ConnectToBluetooth_Thread ConnectToBluetoothDevice_Thread;
	public ConnectedToBluetooth_Thread BluetoothConnected_Thread;
	public int ConnectionState;

	public BluetoothChatService(RendererActivity Renderer_Activity, Handler MessageHandler)
	{
		this.RendererActivity = Renderer_Activity;
		this.MessageHandler = MessageHandler;
		ConnectionState = STATE_NONE;


		LastUpdateChecker_Thread CheckLastUpdate_Thread = new LastUpdateChecker_Thread(this);
		CheckLastUpdate_Thread.start();

	}

	private synchronized void SetState(int state)
	{
		Log.e(TAG, "SetState() " + ConnectionState + " -> " + state);
		ConnectionState = state;

		// Give the new state to the Handler so the UI Activity can update
		MessageHandler.obtainMessage(Constants.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
	}

	public synchronized int getConnectionState()
	{
		return ConnectionState;
	}

	private void CancelThread(Thread Thread)
	{

		if (Thread != null)
		{
			Thread = null;
			if (Thread instanceof AcceptNewBluetoothConnection_Thread)
			{
				((AcceptNewBluetoothConnection_Thread) Thread).Cancel();
			} else if (Thread instanceof ConnectedToBluetooth_Thread)
			{
				((ConnectedToBluetooth_Thread) Thread).Cancel();
			} else if (Thread instanceof ConnectToBluetooth_Thread)
			{
				((ConnectToBluetooth_Thread) Thread).Cancel();
			}
		}
	}

	public synchronized void Start()
	{
		Log.e(TAG, "Start");


		CancelThread(ConnectToBluetoothDevice_Thread);
		CancelThread(BluetoothConnected_Thread);

		// Start the thread to listen on a BluetoothServerSocket
		if (AcceptNewBluetoothConnection_Thread == null)
		{
			AcceptNewBluetoothConnection_Thread = new AcceptNewBluetoothConnection_Thread(this);
			AcceptNewBluetoothConnection_Thread.start();
		}

		SetState(STATE_LISTEN);

	}

	public synchronized void Connect(BluetoothDevice device)
	{
		Log.e(TAG, "connect to: " + device);
		// Cancel any thread attempting to make a connection
		if (ConnectionState == STATE_CONNECTING)
		{
			CancelThread(ConnectToBluetoothDevice_Thread);
		}

		CancelThread(BluetoothConnected_Thread);


		// Start the thread to connect with the given device
		ConnectToBluetoothDevice_Thread = new ConnectToBluetooth_Thread(this, device);
		ConnectToBluetoothDevice_Thread.start();
		SetState(STATE_CONNECTING);
	}

	public synchronized void Connected(BluetoothSocket socket, BluetoothDevice device)
	{
		Log.e(TAG, "Connected.");

		CancelThread(ConnectToBluetoothDevice_Thread);
		CancelThread(BluetoothConnected_Thread);
		CancelThread(AcceptNewBluetoothConnection_Thread);


		// Start the thread to manage the connection and perform transmissions
		BluetoothConnected_Thread = new ConnectedToBluetooth_Thread(this, socket);
		BluetoothConnected_Thread.start();

		// Send the name of the connected device back to the UI Activity
		Message msg = MessageHandler.obtainMessage(Constants.MESSAGE_DEVICE_NAME);
		Bundle bundle = new Bundle();
		bundle.putString(Constants.DEVICE_NAME, device.getName());

		msg.setData(bundle);
		MessageHandler.sendMessage(msg);

		SetState(STATE_CONNECTED);
	}


	public synchronized void Stop()
	{
		Log.e(TAG, "Stop");


		CancelThread(ConnectToBluetoothDevice_Thread);
		CancelThread(BluetoothConnected_Thread);
		CancelThread(AcceptNewBluetoothConnection_Thread);
		SetState(STATE_NONE);
	}

	public void Write(byte[] out)
	{
		Log.e(TAG, "- Write BluetoothChatFragment -");

		// Create temporary object
		ConnectedToBluetooth_Thread r;
		// Synchronize a copy of the ConnectedThread
		synchronized (this)
		{
			if (ConnectionState != STATE_CONNECTED)
			{
				return;
			}
			r = BluetoothConnected_Thread;
		}
		// Perform the write unsynchronized
		r.Write(out);
	}

	private void ConnectionFailed()
	{
		if (AcceptNewBluetoothConnection_Thread == null && ConnectionState != STATE_NONE)
		{
			Log.e(TAG, "Restarting ConnectionState " + ConnectionState);
			AcceptNewBluetoothConnection_Thread = new AcceptNewBluetoothConnection_Thread(this);
			AcceptNewBluetoothConnection_Thread.start();
		}
		SetState(STATE_LISTEN);
		// Send a failure message back to the Activity
		Message msg = MessageHandler.obtainMessage(Constants.MESSAGE_TOAST);
		Bundle bundle = new Bundle();
		bundle.putString(Constants.TOAST, "Unable to connect device");
		msg.setData(bundle);
		MessageHandler.sendMessage(msg);

		// Start the service over to restart listening mode
		//BluetoothChatService.this.start();
	}

	public void ConnectionLost()
	{
		if (AcceptNewBluetoothConnection_Thread == null && ConnectionState != STATE_NONE)
		{
			Log.e(TAG, "Restarting ConnectionState " + ConnectionState);
			AcceptNewBluetoothConnection_Thread = new AcceptNewBluetoothConnection_Thread(this);
			AcceptNewBluetoothConnection_Thread.start();
		}
		SetState(STATE_LISTEN);

		// Send a failure message back to the Activity
		Message msg = MessageHandler.obtainMessage(Constants.MESSAGE_TOAST);
		Bundle bundle = new Bundle();
		bundle.putString(Constants.TOAST, "Device connection was lost");
		msg.setData(bundle);
		MessageHandler.sendMessage(msg);
	}

}
