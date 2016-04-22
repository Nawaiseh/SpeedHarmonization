package edu.smu.trl.safety.bluetooth;

/**
 * Created by TRL on 3/7/2016.
 */

import android.bluetooth.BluetoothSocket;
import android.os.Environment;
import android.os.SystemClock;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;

import edu.smu.trl.safety.Data.Car;
import edu.smu.trl.safety.Data.Car.DistanceUnit;
import edu.smu.trl.safety.utilities.Log;


/**
 * * This thread runs during a connection with a remote device.
 * * It handles all incoming and outgoing transmissions.
 **/
public class ConnectedToBluetooth_Thread extends Thread
{

	private static final String TAG = "BluetoothChatService";
	public static Object Lock;
	private final edu.smu.trl.safety.bluetooth.BluetoothChatService BluetoothChatService;
	private final BluetoothSocket BluetoothSocket;
	private InputStream InputStream = null;
	private OutputStream OutputStream = null;
	private Car MyCar;
	private Random Random = new Random();

	public ConnectedToBluetooth_Thread(edu.smu.trl.safety.bluetooth.BluetoothChatService BluetoothChatService, BluetoothSocket BluetoothSocket)
	{
		this.BluetoothChatService = BluetoothChatService;
		MyCar = BluetoothChatService.RendererActivity.MyCar;

		Lock = BluetoothChatService.RendererActivity.Cars;

		Log.e(TAG, "Create ConnectedThread.");
		this.BluetoothSocket = BluetoothSocket;


		// Get the BluetoothSocket input and output streams
		try
		{
			InputStream = BluetoothSocket.getInputStream();
			OutputStream = BluetoothSocket.getOutputStream();
		}
		catch (IOException Exception)
		{
			Exception.printStackTrace();
			Log.e(TAG, "Temp Sockets Not Created", Exception);
			InputStream = null;
			OutputStream = null;
		}
	}

	private static String ExtractID(byte[] Blob, Car.CarType _CarType)
	{
		return Car.BytesToHEX(Blob, ((_CarType == Car.CarType.MyCar) ? 1 : 39), 4).trim();
	}

	private String BluetoothMessageAsString(byte[] Bluetooth_Message)
	{
		String Result = String.format("%s\n%s", Car.BytesToHEX(Bluetooth_Message, 0, 38).trim(), Car.BytesToHEX(Bluetooth_Message, 38, 38).trim());
		return Result;
	}

	public void run()
	{
		Log.e(TAG, "BEGIN mConnectedThread");
		byte[] Bluetooth_Message = new byte[1024];
		int NumberOfBytes;

		float N1 = 2147483647L;
		float N2 = 4294967296L;
		Car OtherCar = new Car();


		FileOutputStream FileOutputStream = null;
		OutputStreamWriter Writer = null;
		try
		{
			File Path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
			boolean PathExist = Path.exists();
			if (!PathExist)
			{
				PathExist = Path.mkdirs();
				Path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
				int x = 0;
			}
			if (PathExist)
			{
				FileOutputStream = new FileOutputStream(new File(Path, "Bluetooth_Data.txt"), true);
				Writer = new OutputStreamWriter(FileOutputStream);
			} else
			{

			}
		}
		catch (Exception Exception)
		{
			Exception.printStackTrace();
			Log.e(TAG, "Exception", Exception);
		}

		SimpleDateFormat SimpleDateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss.SSS");

		while (true)
		{

			try
			{
				Bluetooth_Message = new byte[80];
				if (BluetoothSocket.isConnected())
				{
					if (InputStream.available() > 0)
					{
						NumberOfBytes = InputStream.read(Bluetooth_Message);  // Read from the InputStream
						if (NumberOfBytes == 76)
						{
							String Bluetooth_MessageAsString = BluetoothMessageAsString(Bluetooth_Message);
							Log.i(TAG, "NumberOfBytes:- " + NumberOfBytes);
							String Message = "";
							//   synchronized (Lock) {
							try
							{
								boolean Different = false;
								for (int i = 1; i < 5; i++)
								{
									if (Bluetooth_Message[i] != Bluetooth_Message[i + 38])
									{
										Different = true;

										break;
									}
								}
								if (!Different)
								{
									continue;
								}
								String OtherCarID = ExtractID(Bluetooth_Message, Car.CarType.OtherCar);
								if (BluetoothChatService.RendererActivity.Cars.containsKey(OtherCarID))
								{
									OtherCar = BluetoothChatService.RendererActivity.Cars.get(OtherCarID);
								} else
								{
									OtherCar = new Car();
									OtherCar.Vehicle = BluetoothChatService.RendererActivity.BitMaps[Random.nextInt(3) + 1];
									BluetoothChatService.RendererActivity.Cars.put(OtherCarID, OtherCar);
								}
								MyCar.SetData(Bluetooth_Message);
								OtherCar.SetData(Bluetooth_Message);

								OtherCar.Distance = OtherCar.DistanceFrom(MyCar, DistanceUnit.Meters);
								Message = String.format("Me:- %s\n OtherCar:- %s\nDistance = %.1f Meters",
										MyCar.Position(Car.PositionType.NoAltitude), OtherCar.Position(Car.PositionType.NoAltitude), OtherCar.Distance);

								if (Writer != null)
								{
									Calendar _Calendar = Calendar.getInstance();
									Writer.write(String.format("%s:\t%s\n", SimpleDateFormat.format(_Calendar.getTime()), Message.replace("\n", "\t")));
									Writer.flush();
								}
								BluetoothChatService.MessageHandler.obtainMessage(Constants.MESSAGE_READ, Message.length(), -1, Message).sendToTarget();
/*

								OtherCar.GraphicsLocation.X = OtherCar.Location.X - MyCar.Location.X;
								OtherCar.GraphicsLocation.Y = OtherCar.Location.X - MyCar.Location.Y;
								OtherCar.GraphicsLocation.Z = OtherCar.Location.Z - MyCar.Location.Z;


								OtherCar.GraphicsRotation.Z = OtherCar.Direction - MyCar.Direction;*/


							}
							catch (Exception Exception)
							{
								Exception.printStackTrace();
								Log.e(TAG, "Exception", Exception);
							}

							//    }
						} else
						{
							Log.e(TAG, "Exception:- Number of Bytes <> 76");
							int x = 0;
						}
					} else
					{
						SystemClock.sleep(10);
					}
				} else
				{

					Log.e(TAG, "Disconnected");
					BluetoothChatService.ConnectionLost();
				}
			}
			catch (Exception Exception)
			{
				Exception.printStackTrace();
				Log.e(TAG, "Disconnected", Exception);
				BluetoothChatService.ConnectionLost();

			}
		}

	}

	public void Write(byte[] Bluetooth_Message)
	{
		Log.e(TAG, "- write BluetoothChatFragment - ConnectedThread -");
		try
		{
			OutputStream.write(Bluetooth_Message);

			// Share the sent message back to the UI Activity
			BluetoothChatService.MessageHandler.obtainMessage(Constants.MESSAGE_WRITE, -1, -1, Bluetooth_Message).sendToTarget();
		}
		catch (IOException Exception)
		{
			Exception.printStackTrace();
			Log.e(TAG, "Exception During Write", Exception);
		}
	}

	public void Cancel()
	{
		try
		{
			BluetoothSocket.close();
		}
		catch (IOException Exception)
		{
			Exception.printStackTrace();
			Log.e(TAG, "Close() Of Connect Socket Failed", Exception);
		}
	}
}
