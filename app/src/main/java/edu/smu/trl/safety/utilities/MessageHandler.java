package edu.smu.trl.safety.utilities;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.StringRes;

import edu.smu.trl.safety.R;
import edu.smu.trl.safety.RendererActivity;
import edu.smu.trl.safety.bluetooth.Constants;

public class MessageHandler extends Handler
{

	private static final String TAG = "Safety";
	private final RendererActivity RendererActivity;

	public MessageHandler(RendererActivity RendererActivity)
	{
		super();
		this.RendererActivity = RendererActivity;
	}


	protected void SetStatus(int ResourceID)
	{

		try
		{
			final android.app.ActionBar ActionBar = RendererActivity.getActionBar();
			if (ActionBar == null)
			{
				return;
			}
			ActionBar.setSubtitle(ResourceID);
		}
		catch (Exception Exception)
		{
			Exception.printStackTrace();
			Log.e(TAG, "Exception", Exception);
		}
	}

	protected void SetStatus(CharSequence SubTitle)
	{
		try
		{
			final android.app.ActionBar ActionBar = RendererActivity.getActionBar();
			if (ActionBar == null)
			{
				return;
			}
			ActionBar.setSubtitle(SubTitle);
		}
		catch (Exception Exception)
		{
			Exception.printStackTrace();
			Log.e(TAG, "Exception", Exception);
		}
	}

	public final String GetString(@StringRes int resId, Object... formatArgs)
	{
		return RendererActivity.getResources().getString(resId, formatArgs);
	}

	protected void MessageStatusChanged(Message Message)
	{
		switch (Message.arg1)
		{
			case Constants.STATE_CONNECTED:
				String Status = GetString(R.string.title_connected_to, RendererActivity.ConnectedDeviceName);
				SetStatus(Status);
				if (RendererActivity.BluetoothMenuItem != null)
				{
					RendererActivity.BluetoothMenuItem.setIcon(R.drawable.bluetooth_connected);
				}
				RendererActivity.BluetoothChatArrayAdapter.clear();
				break;
			case Constants.STATE_CONNECTING:
				SetStatus(R.string.title_connecting);
				if (RendererActivity.BluetoothMenuItem != null)
				{
					RendererActivity.BluetoothMenuItem.setIcon(R.drawable.bluetooth_disconnected);
				}
				break;
			case Constants.STATE_LISTEN:
			case Constants.STATE_NONE:
				SetStatus(R.string.title_not_connected);
				if (RendererActivity.BluetoothMenuItem != null)
				{
					RendererActivity.BluetoothMenuItem.setIcon(R.drawable.bluetooth_disconnected);
				}
				break;
		}
	}

	protected void MessageWrite(Message Message)
	{
		try
		{
			byte[] writeBuf = (byte[]) Message.obj;
			String writeMessage = new String(writeBuf);

			RendererActivity.BluetoothChatArrayAdapter.add("Me:  " + writeMessage);
		}
		catch (Exception Exception)
		{
			Exception.printStackTrace();
			Log.e(TAG, "Exception", Exception);
		}
	}

	@SuppressLint("SetTextI18n")
	protected void MessageDeviceName(Message Message)
	{
		try
		{
			RendererActivity.ConnectedDeviceName = Message.getData().getString(Constants.DEVICE_NAME);
			RendererActivity.BluetoothChatArrayAdapter.add(String.format("Connected To: %s", RendererActivity.ConnectedDeviceName));
			// Toast.makeText(this, "Connected To: " + ConnectedDeviceName, Toast.LENGTH_SHORT).show();
			RendererActivity.TextView.setText("Connected To: " + RendererActivity.ConnectedDeviceName);
			RendererActivity.ConnectionStatus_Menu.setTitle("Connected To: " + RendererActivity.ConnectedDeviceName);

			if (RendererActivity.BluetoothMenuItem != null)
			{
				RendererActivity.BluetoothMenuItem.setIcon(R.drawable.bluetooth_connected);
			}

		}
		catch (Exception Exception)
		{
			Exception.printStackTrace();
			Log.e(TAG, "Exception", Exception);
		}
	}

	protected void MessageToast(Message Message)
	{
		RendererActivity.DisplayMessage(Message);
		//Toast.makeText(this, Message.getData().getString(Constants.TOAST), Toast.LENGTH_SHORT).show();
	}

	@Override
	public void handleMessage(Message Message)
	{

		switch (Message.what)
		{
			case Constants.MESSAGE_STATE_CHANGE:
				MessageStatusChanged(Message);
				break;
			case Constants.MESSAGE_WRITE:
				MessageWrite(Message);
				break;
			case Constants.MESSAGE_READ:
				RendererActivity.DisplayMessage(Message);
				break;
			case Constants.MESSAGE_DEVICE_NAME:
				MessageDeviceName(Message);
				break;
			case Constants.MESSAGE_TOAST:
				MessageToast(Message);
				break;
			case Constants.PLAY_ALERT_SOUND:
				RendererActivity.MessagePlaySound(Message);
				break;
			case Constants.STOP_ALERT_SOUND:
				RendererActivity.MessageStopSound(Message);
				break;
		}
	}
}
