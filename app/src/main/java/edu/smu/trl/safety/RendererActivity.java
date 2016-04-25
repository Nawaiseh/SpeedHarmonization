package edu.smu.trl.safety;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.support.annotation.StringRes;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.TreeMap;

import edu.smu.trl.safety.Data.Car;
import edu.smu.trl.safety.Graphics.SurfaceView;
import edu.smu.trl.safety.Graphics.Utilities;
import edu.smu.trl.safety.bluetooth.BluetoothChatService;
import edu.smu.trl.safety.bluetooth.Constants;
import edu.smu.trl.safety.utilities.Log;
import edu.smu.trl.safety.utilities.SoundAnimation;

public class RendererActivity extends AppCompatActivity
{

	private static final String TAG = "Safety";
	public static int NormalColor;
	public static int AlerColor;
	public static int UsedColor;
	public static ValueAnimator ValueAnimator;
	public static RelativeLayout Layout;
	public final TreeMap<String, Car> Cars = new TreeMap<>();
	public CharSequence Title = "V2V Interface";
	public android.view.Menu Menu;
	public MenuItem BluetoothMenuItem;
	public SoundAnimation SoundAnimation;
	public BluetoothChatService BluetoothChatService;
	public BluetoothAdapter BluetoothAdapter = null;
	public String ConnectedDeviceName = null;
	public TextView TextView;
	public MenuItem ConnectionStatus_Menu;
	public ListView BluetoothChatView;
	public ArrayAdapter<String> BluetoothChatArrayAdapter;

	public Handler MessageHandler = new Handler()
	{
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
					DisplayMessage(Message);
					break;
				case Constants.MESSAGE_DEVICE_NAME:
					MessageDeviceName(Message);
					break;
				case Constants.MESSAGE_TOAST:
					MessageToast(Message);
					break;
				case Constants.PLAY_ALERT_SOUND:
					MessagePlaySound(Message);
					break;
				case Constants.STOP_ALERT_SOUND:
					MessageStopSound(Message);
					break;

				/*case Constants.ROTATE_COMPASS:
					RotateCompass(Message);
					break;*/
			}
		}
	};
	public Car MyCar = new Car();
	public boolean RotationAllowed = false;
	public Bitmap[] BitMaps;
	public Bitmap Car_Blue;
	public Bitmap Compass;
	public Bitmap Data_Header, Row_0, Row;
	public boolean ShowInformation = true;
	public boolean TableView = true;
	protected PowerManager.WakeLock WakeLock;
	private Bitmap Car_Red;
	private Bitmap Car_Green;
	private Bitmap Car_Yellow;

	protected void SetStatus(int ResourceID)
	{

		try
		{
			final android.app.ActionBar ActionBar = getActionBar();
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
			final android.app.ActionBar ActionBar = getActionBar();
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
		return getResources().getString(resId, formatArgs);
	}

	protected void MessageStatusChanged(Message Message)
	{
		switch (Message.arg1)
		{
			case Constants.STATE_CONNECTED:
				String Status = GetString(R.string.title_connected_to, ConnectedDeviceName);
				SetStatus(Status);
				if (BluetoothMenuItem != null)
				{
					BluetoothMenuItem.setIcon(R.drawable.bluetooth_connected);
				}
				BluetoothChatArrayAdapter.clear();
				break;
			case Constants.STATE_CONNECTING:
				SetStatus(R.string.title_connecting);
				if (BluetoothMenuItem != null)
				{
					BluetoothMenuItem.setIcon(R.drawable.bluetooth_disconnected);
				}
				break;
			case Constants.STATE_LISTEN:
			case Constants.STATE_NONE:
				SetStatus(R.string.title_not_connected);
				if (BluetoothMenuItem != null)
				{
					BluetoothMenuItem.setIcon(R.drawable.bluetooth_disconnected);
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

			BluetoothChatArrayAdapter.add("Me:  " + writeMessage);
		}
		catch (Exception Exception)
		{
			Exception.printStackTrace();
			Log.e(TAG, "Exception", Exception);
		}
	}

/*

	private void RotateCompass(Message Message)
	{

		float Theta = 0;
		if (Message.obj instanceof String)
		{
			String _Message = (String) Message.obj;
			Theta = Float.parseFloat(_Message);
		} else
		{
			String _Message = new String((byte[]) Message.obj, 0, Message.arg1);
			Theta = Float.parseFloat(_Message);
		}


		Matrix matrix = new Matrix();
		CompassImage.setScaleType(ImageView.ScaleType.MATRIX);   //required
		matrix.postRotate((float) Theta, CompassImage.getWidth() / 2, CompassImage.getHeight() / 2);
		CompassImage.setImageMatrix(matrix);
		CompassImage.invalidate();
	}
*/

	@SuppressLint("SetTextI18n")
	protected void MessageDeviceName(Message Message)
	{
		try
		{
			ConnectedDeviceName = Message.getData().getString(Constants.DEVICE_NAME);
			BluetoothChatArrayAdapter.add(String.format("Connected To: %s", ConnectedDeviceName));
			// Toast.makeText(this, "Connected To: " + ConnectedDeviceName, Toast.LENGTH_SHORT).show();
			TextView.setText("Connected To: " + ConnectedDeviceName);
			ConnectionStatus_Menu.setTitle("Connected To: " + ConnectedDeviceName);

			if (BluetoothMenuItem != null)
			{
				BluetoothMenuItem.setIcon(R.drawable.bluetooth_connected);
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
		DisplayMessage(Message);
		//Toast.makeText(this, Message.getData().getString(Constants.TOAST), Toast.LENGTH_SHORT).show();
	}

	public ValueAnimator StartColorAnimation(View View)
	{
		try
		{
			int colorStart = View.getSolidColor();
			int colorEnd = getResources().getColor(R.color.alert_color);


			ValueAnimator ValueAnimator = ObjectAnimator.ofInt(View, "backgroundColor", colorStart, colorEnd);

			// colorAnim.setDuration(ValueAnimator.INFINITE);
			ValueAnimator.setEvaluator(new ArgbEvaluator());
			ValueAnimator.setRepeatCount(ValueAnimator.INFINITE);
			ValueAnimator.setRepeatMode(ValueAnimator.INFINITE);
			//colorAnim.setRepeatMode(ValueAnimator.REVERSE);
			ValueAnimator.start();
			SoundAnimation.Start();
			return ValueAnimator;
		}
		catch (Exception Exception)
		{
			Exception.printStackTrace();
			Log.e(TAG, "Exception", Exception);
		}
		return null;
	}

	public void StopColorAnimation(View View, ValueAnimator ValueAnimator, int Color)
	{
		int colorStart = Color;
		int colorEnd = 0xFFFF0000;
		try
		{
			if (View != null && ValueAnimator != null)
			{
				ValueAnimator.cancel();
				SoundAnimation.Stop();
				View.setBackgroundColor(Color);
			}
		}
		catch (Exception Exception)
		{
			Exception.printStackTrace();
			Log.e(TAG, "Exception", Exception);
		}
	}

	public void DisplayMessage(Message msg)
	{
		try
		{
			if (msg.obj instanceof String)
			{
				String readMessage = (String) msg.obj;

				BluetoothChatArrayAdapter.add(readMessage);
			} else
			{
				byte[] readBuf = (byte[]) msg.obj;
				String readMessage = new String(readBuf, 0, msg.arg1);

				BluetoothChatArrayAdapter.add(readMessage);
			}
		}
		catch (Exception Exception)
		{
			Exception.printStackTrace();
			Log.e(TAG, "Exception", Exception);
		}
	}

	public void EnsureDiscoverable()
	{
		Log.e(TAG, "- Ensure Discoverable BluetoothChatFragment -");
		if (BluetoothAdapter == null)
		{
			BluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
			if (BluetoothAdapter == null)
			{
				//  Toast.makeText(this, "Bluetooth Is Not Enabled", Toast.LENGTH_LONG).show();
				finish();
			}
		}
		if (BluetoothAdapter != null)
		{
			if (BluetoothAdapter.getScanMode() != android.bluetooth.BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE)
			{
				try
				{
					Intent DiscoverableIntent = new Intent(android.bluetooth.BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
					DiscoverableIntent.putExtra(android.bluetooth.BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, Integer.MAX_VALUE);

					startActivity(DiscoverableIntent);

					BluetoothChatArrayAdapter.add("Bluetooth Connection Is Enabled");
					//   Toast.makeText(this, "Bluetooth Connection Enabled!", Toast.LENGTH_SHORT).show();

					//MenuItem.setIcon(getResources().getDrawable(R.drawable.bluetooth_disconnected));
					if (BluetoothMenuItem != null)
					{
						BluetoothMenuItem.setIcon(R.drawable.bluetooth_disconnected);
					}
					int x = 0;
				}
				catch (Exception Exception)
				{
					Exception.printStackTrace();
					Log.e("EnsureDiscoverable:-", "Error Ensuring Bluetooth Discoverability", Exception);
					int x = 0;
				}
			} else
			{
				Log.i("EnsureDiscoverable:-", "Bluetooth is Already Discoverable");
				return;

			}
			Log.i(TAG, "- Ensure Discoverable  Extended for 300 Seconds");
		} else
		{
			Log.i(TAG, "- Ensure Discoverable  Could Not Extended for 300 Seconds");
		}
	}

	public void MessagePlaySound(Message Message)
	{
		DisplayMessage(Message);
		if (!BluetoothChatService.AlertIsGoing)
		{
			BluetoothChatService.AlertIsGoing = true;
			ValueAnimator = StartColorAnimation(Layout);
			BluetoothChatArrayAdapter.add("Alert!:- A Car is Too Close");
		}
	}

	public void MessageStopSound(Message Message)
	{
		DisplayMessage(Message);
		if (BluetoothChatService.AlertIsGoing)
		{
			BluetoothChatService.AlertIsGoing = false;
			//  BluetoothChatArrayAdapter.add("Cars Are Far Enough Now");
			StopColorAnimation(Layout, ValueAnimator, NormalColor);
		}
	}

	public void RestoreActionBar()
	{
		ActionBar actionBar = getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setTitle(Title);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem MenuItem)
	{

		switch (MenuItem.getItemId())
		{
			case R.id.ShowBluetoothNameToOthers:
			{
				// Ensure this device is discoverable by others
				BluetoothMenuItem = MenuItem;
				EnsureDiscoverable();
				return true;
			}
			case R.id.StartAlert:
			{

				synchronized (Cars)
				{

					BluetoothChatService.LastUpdate = System.currentTimeMillis();
					BluetoothChatService.AlertIsGoing = true;
					ValueAnimator = StartColorAnimation(Layout);
				}

				return true;
			}
			case R.id.StopAlert:
			{
				StopColorAnimation(Layout, ValueAnimator, NormalColor);
				synchronized (Cars)
				{
					BluetoothChatService.AlertIsGoing = false;
				}

				return true;
			}

			case R.id.Direction:
			{
				if (MenuItem.getTitle().toString().toUpperCase().startsWith("ENABLE"))
				{
					MenuItem.setTitle("Disable Angle Rotation");
					RotationAllowed = true;
				} else
				{
					MenuItem.setTitle("Enable Angle Rotation");
					RotationAllowed = false;
				}

				return true;
			}
			case R.id.Five:
			{
				synchronized (Cars)
				{
					BluetoothChatService.ThreasholdDistance = 5;
				}
				return true;
			}

			case R.id.Ten:
			{
				synchronized (Cars)
				{
					BluetoothChatService.ThreasholdDistance = 10;
				}
				return true;
			}

			case R.id.Fifteen:
			{
				synchronized (Cars)
				{
					BluetoothChatService.ThreasholdDistance = 15;
				}
				return true;
			}

			case R.id.Twenty:
			{
				synchronized (Cars)
				{
					BluetoothChatService.ThreasholdDistance = 20;
				}
				return true;
			}
			case R.id.TwentyFive:
			{
				synchronized (Cars)
				{
					BluetoothChatService.ThreasholdDistance = 25;
				}
				return true;
			}
			case R.id.ShowInformation:
			{
				if (this.ShowInformation)
				{
					ShowInformation = false;
					MenuItem.setIcon(R.drawable.information_disabled);
				} else
				{
					ShowInformation = true;
					MenuItem.setIcon(R.drawable.information_enabled);
				}
				return true;
			}

			case R.id.TableView:
			{
				TableView = !TableView;
				MenuItem.setIcon((TableView) ? R.drawable.table : R.drawable.normal_view);
				return true;
			}
		}
		return super.onOptionsItemSelected(MenuItem);
	}

	protected void SetupChat()
	{
		Log.e(TAG, "setupChat()");

		if (BluetoothAdapter == null)
		{
			BluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
			if (BluetoothAdapter == null)
			{
				Toast.makeText(this, "Bluetooth Is Not Enabled", Toast.LENGTH_LONG).show();
				finish();
			}
		}

		if (BluetoothChatService == null)
		{
			BluetoothChatService = new BluetoothChatService(this, MessageHandler);
		}

		BluetoothChatArrayAdapter = new ArrayAdapter<String>(this, R.layout.message);

		BluetoothChatView.setAdapter(BluetoothChatArrayAdapter);
	}

	@Override
	public void onStart()
	{
		super.onStart();
		Log.e(TAG, "- On Start BluetoothChatFragment -");
		if (BluetoothAdapter == null)
		{
			SetupChat();
		}
		if (!BluetoothAdapter.isEnabled())
		{
			Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, Constants.REQUEST_ENABLE_BT);
		} else if (BluetoothChatService == null)
		{
			SetupChat();
		}
	}

	@Override
	public void onResume()
	{
		super.onResume();
		Log.e(TAG, "- On Resume BluetoothChatFragment -");
		if (BluetoothAdapter == null)
		{
			SetupChat();
		}
		if (BluetoothChatService != null)
		{
			if (BluetoothChatService.getConnectionState() == BluetoothChatService.STATE_NONE)
			{
				BluetoothChatService.Start();
			}
		}
	}

	@Override
	public synchronized void onPause()
	{
		super.onPause();
		Log.e(TAG, "- On Pause BluetoothChatFragment -");

	}

	@Override
	public void onStop()
	{
		super.onStop();
		Log.e(TAG, "- On Stop BluetoothChatFragment -");
	}

	@Override
	public void onDestroy()
	{
		if (WakeLock != null)
		{
			this.WakeLock.release();
		}
		super.onDestroy();
		Log.e(TAG, "- On Destroy BluetoothChatFragment -");
		if (BluetoothChatService != null)
		{
			BluetoothChatService.Stop();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu Menu)
	{
		getMenuInflater().inflate(R.menu.bluetooh_menu, Menu);
		RestoreActionBar();

		this.Menu = Menu;
		BluetoothMenuItem = Menu.findItem(R.id.ShowBluetoothNameToOthers);
		ConnectionStatus_Menu = Menu.findItem(R.id.ConnectionStatus_Menu);
		return true;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);


		// Utilities.AskForPermissions(this);

		setContentView(R.layout.activity_renderer);

		MyCar.Type = Car.CarType.MyCar;
		BluetoothChatView = (ListView) this.findViewById(R.id.ListView);

		BluetoothChatView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
		final PowerManager pm = (PowerManager) getSystemService(this.POWER_SERVICE);
		this.WakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, TAG);
		this.WakeLock.acquire();

		TextView = (TextView) this.findViewById(R.id.ConnectionStatus);


		SoundAnimation = new SoundAnimation(this, R.raw.beep);

		Layout = (RelativeLayout) findViewById(R.id.DrawingArea);
		NormalColor = Layout.getSolidColor();
		AlerColor = getResources().getColor(R.color.normal_color);
		UsedColor = NormalColor;

		Car_Red = Utilities.ResizeBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.car_red), Car.Width, Car.Length);
		Car_Green = Utilities.ResizeBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.car_green), Car.Width, Car.Length);
		Car_Blue = Utilities.ResizeBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.car_blue), Car.Width, Car.Length);
		Car_Yellow = Utilities.ResizeBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.car_yellow), Car.Width, Car.Length);
		Compass = Utilities.ResizeBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.compass), 100, 100);

		Data_Header = Utilities.ResizeBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.data_header), 200, 30);
		Row_0 = Utilities.ResizeBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.data_row_0), 200, 20);
		Row = Utilities.ResizeBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.data_row), 200, 20);

		BitMaps = new Bitmap[]{Car_Blue, Car_Red, Car_Green, Car_Yellow};

		MyCar.Vehicle = BitMaps[0];

		Layout.addView(new SurfaceView(this, getResources()));
/*
		CompassImage = (ImageView) this.findViewById(R.id.Compass);

		CompassImage.bringToFront();*/
		SetupChat();
/*
		MyCar.ID = "Me";
		Car _Car = new Car(0, 0, 0);
		_Car.Vehicle = BitMaps[1];
		_Car.ID = String.format("%d", 1);
		Cars.put(_Car.ID, _Car);
		_Car.Location.X = 3.6f;
		_Car.Location.Y = 0;*/
/*
		if (UseDummyData)
		{

			int CarsCount = 2;
			int Speed, Location_Index, MODIFY_SPEED_RATE;
			Random Random = new Random();
			Route.BuildRoute();

			Location_Index = Random.nextInt(Route.Locations.size());
			Speed = Random.nextInt(4) + 1;
			MODIFY_SPEED_RATE = 1500;//5 + Random.nextInt(50);
			MyCar.Speed = Speed;
			MyCar.Location_Index = Location_Index;
			MyCar.MODIFY_SPEED_RATE = MyCar.TIME_LEFT_TO_UPDATE_SPEED = MODIFY_SPEED_RATE;

			MyCar.GraphicsLocation.X = Route.Locations.get(Location_Index).X;
			MyCar.GraphicsLocation.Y = Route.Locations.get(Location_Index).Y;
			MyCar.ID = "MyCar";
			MyCar.Vehicle = BitMaps[0];

			for (int CarIndex = 0; CarIndex < CarsCount; CarIndex++)
			{
				Location_Index = Random.nextInt(Route.Locations.size());
				Speed = Random.nextInt(2) + 1;
				MODIFY_SPEED_RATE = 1000;// 5 + Random.nextInt(50);
				Car Car = new Car(Speed, Location_Index, MODIFY_SPEED_RATE);
				Car.Vehicle = BitMaps[Random.nextInt(1) + 1];
				Car.ID = String.format("%d", CarIndex);
				Cars.put(Car.ID, Car);
			}
		}
		*/
	}
}
