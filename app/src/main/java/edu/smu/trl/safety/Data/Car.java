package edu.smu.trl.safety.Data;


import android.graphics.Bitmap;

import java.util.Random;

import edu.smu.trl.safety.utilities.LatLonToXY;
import edu.smu.trl.safety.utilities.Log;

public class Car
{

	private static final String TAG = "BluetoothChatService";
	private static final char[] HEX_SYMBOLS = "0123456789ABCDEF".toCharArray();
	public static float Width = 12;
	public static float Length = 22;
	public static float Height = 10;
	public float Distance = 0;
	public long LastUpdated = System.currentTimeMillis();
	public String ID = null;
	public float Latitude = 0;
	public float Longitude = 0;
	public float Altitude = 0;
	public Number3d Location = new Number3d(0, 0, 0);
	public Number3d Velocity = new Number3d(0, 0, 0);
	public Number3d GraphicsLocation = new Number3d(0, 0, 0);
	public Number3d DrawingLocation = new Number3d(0, 0, 0);
	public Number3d GraphicsRotation = new Number3d(0, 0, 0);
	public Number3d RelativeLocation = new Number3d(0, 0, 0);


	public Number3d LatLongAltLocation = new Number3d(0, 0, 0);
	public float Speed = 0;
	public float Direction = 0;
	public CarType Type = CarType.OtherCar;
	public Bitmap Vehicle;

	public Car()
	{
		LastUpdated = System.currentTimeMillis();

		Random Random = new Random();
		GraphicsLocation.X = 10.0F + Random.nextFloat() * 80F;
		GraphicsLocation.Y = 10.0F + Random.nextFloat() * 80F;
		Velocity.X = -0.5F + Random.nextFloat();
		Velocity.Y = -0.5F + Random.nextFloat();
	}


	public Car(String ID, float Latitude, float Longitude, float Altitude, float Speed, float Direction, float Width, float Length, float Height)
	{
		this.ID = ID;
		this.Latitude = Latitude;
		this.Longitude = Longitude;
		this.Altitude = Altitude;

		LatLongAltLocation.X = this.Longitude;
		LatLongAltLocation.Y = this.Latitude;
		LatLongAltLocation.Z = this.Altitude;

		Location.X = (float) LatLonToXY.LatLonToXYInMiles(0, Longitude, 0, 0, LatLonToXY.Unit.Mile);
		Location.Y = (float) LatLonToXY.LatLonToXYInMiles(Latitude, 0, 0, 0, LatLonToXY.Unit.Mile);
		Location.Z = this.Altitude;

		this.Speed = Speed;
		this.Direction = Direction;
		this.Width = Width;
		this.Length = Length;
		this.Height = Height;
		synchronized (this)
		{
			LastUpdated = System.currentTimeMillis();
		}
	}

	public Car(byte[] Blob)
	{
		UpdatePosition(this, Blob);
		UpdateSpeed(this, Blob);
		UpdateDirection(this, Blob);
		synchronized (this)
		{
			LastUpdated = System.currentTimeMillis();
		}
	}

	public static float ConvertDistance(float Distance, DistanceUnit DistanceUnit)
	{

		// 1 Inch =   0.000016 Mile
		float InchRatio = 0.000016F;
		float MeterRatio = 0.000621371F;
		float KiloMeterRatio = 0.621371F;
		switch (DistanceUnit)
		{
			case Inches:
				return (Distance / InchRatio);
			case Meters:
				return (Distance / MeterRatio);
			case KiloMeters:
				return (Distance / KiloMeterRatio);
			default:
				return Distance;
		}
	}

	private static void UpdateID(Car Car, byte[] Blob)
	{
		//  Car.ID = BytesToHEX(Blob, ((Car.Type == CarType.MyCar) ? 2 : 42), 4).trim();
		Car.ID = BytesToHEX(Blob, ((Car.Type == CarType.MyCar) ? 1 : 39), 4).trim();
	}

	private static void UpdatePosition(Car Car, byte[] Blob)
	{
		String Latitude_Str = BytesToHEX(Blob, 7 + ((Car.Type == CarType.MyCar) ? 0 : 38), 4).trim();
		String LongitudeStr = BytesToHEX(Blob, 11 + ((Car.Type == CarType.MyCar) ? 0 : 38), 4).trim();
		String AltitudeStr = BytesToHEX(Blob, 15 + ((Car.Type == CarType.MyCar) ? 0 : 38), 2).trim();

		Car.Latitude = Long.parseLong(Latitude_Str, 16);
		Car.Longitude = Long.parseLong(LongitudeStr, 16);
		Car.Altitude = Long.parseLong(AltitudeStr, 16);


		float N1 = 2147483647L;
		float N2 = 4294967296L;


		Car.Latitude = (Car.Latitude > N1) ? (Car.Latitude - N2) : Car.Latitude;
		Car.Longitude = (Car.Longitude > N1) ? (Car.Longitude - N2) : Car.Longitude;

		Car.Latitude = Car.Latitude / 10000000F;
		Car.Longitude = Car.Longitude / 10000000F;
		Car.Altitude = Car.Altitude / 10F;

		Car.LatLongAltLocation.X = Car.Longitude;
		Car.LatLongAltLocation.Y = Car.Latitude;
		Car.LatLongAltLocation.Z = Car.Altitude;


		Car.Location.X = (float) LatLonToXY.LatLonToXYInMiles(0, Car.Longitude, 0, 0, LatLonToXY.Unit.Mile);
		Car.Location.Y = (float) LatLonToXY.LatLonToXYInMiles(Car.Latitude, 0, 0, 0, LatLonToXY.Unit.Mile);
		Car.Location.Z = Car.Altitude;

	}

	private static void UpdateSpeed(Car Car, byte[] Blob)
	{
		String SpeedStr = BytesToHEX(Blob, 21 + ((Car.Type == CarType.MyCar) ? 0 : 38), 2).trim();
		Car.Speed = Long.parseLong(SpeedStr, 16);
		Car.Speed = Car.Speed * 0.02F;
	}

	private static void UpdateDirection(Car Car, byte[] Blob)
	{
		String DirectionStr = BytesToHEX(Blob, 25 + ((Car.Type == CarType.MyCar) ? 0 : 38), 1).trim();
		Car.Direction = Long.parseLong(DirectionStr, 16);
		Car.Direction = Car.Direction * 1.5F;

	}

	private static String BytesToHEXWithGaps(byte[] Blob, int Start, int Count)
	{
		int End = Start + Count;

		char[] HexBuffer = new char[Count * 3];
		int ByteIndex = 0;
		for (int j = Start; j < End; j++)
		{
			int v = Blob[j] & 0xFF;
			HexBuffer[ByteIndex] = HEX_SYMBOLS[v >>> 4];
			ByteIndex++;
			HexBuffer[ByteIndex] = HEX_SYMBOLS[v & 0x0F];
			ByteIndex++;
			HexBuffer[ByteIndex] = ' ';
			ByteIndex++;
		}
		return new String(HexBuffer).trim();
	}

	public static String BytesToHEX(byte[] Blob, int Start, int Count)
	{
		int End = Start + Count;

		char[] HexBuffer = new char[Count * 2];
		int ByteIndex = 0;
		for (int j = Start; j < End; j++)
		{
			int v = Blob[j] & 0xFF;
			HexBuffer[ByteIndex] = HEX_SYMBOLS[v >>> 4];
			ByteIndex++;
			HexBuffer[ByteIndex] = HEX_SYMBOLS[v & 0x0F];
			ByteIndex++;
		}
		return new String(HexBuffer).trim();
	}


	public void SetData(String ID, float Latitude, float Longitude, float Altitude, float Speed, float Direction, float Width, float Length, float Height)
	{
		this.ID = ID;
		this.Latitude = Latitude;
		this.Longitude = Longitude;
		this.Altitude = Altitude;

		LatLongAltLocation.X = this.Longitude;
		LatLongAltLocation.Y = this.Latitude;
		LatLongAltLocation.Z = this.Altitude;

		Location.X = (float) LatLonToXY.LatLonToXYInMiles(0, Longitude, 0, 0, LatLonToXY.Unit.Mile);
		Location.Y = (float) LatLonToXY.LatLonToXYInMiles(Latitude, 0, 0, 0, LatLonToXY.Unit.Mile);
		Location.Z = this.Altitude;

		this.Speed = Speed;
		this.Direction = Direction;
		this.Width = Width;
		this.Length = Length;
		this.Height = Height;
		synchronized (this)
		{
			LastUpdated = System.currentTimeMillis();
		}
	}

	public void SetData(Car Car)
	{

		this.ID = Car.ID;
		this.Latitude = Car.Latitude;
		this.Longitude = Car.Longitude;
		this.Altitude = Car.Altitude;

		LatLongAltLocation.X = Car.LatLongAltLocation.X;
		LatLongAltLocation.Y = Car.LatLongAltLocation.Y;
		LatLongAltLocation.Z = Car.LatLongAltLocation.Z;

		Location.X = Car.Location.X;
		Location.Y = Car.Location.Y;
		Location.Z = Car.Location.Z;

		this.Speed = Car.Speed;
		this.Direction = Car.Direction;
		this.Width = Car.Width;
		this.Length = Car.Length;
		this.Height = Car.Height;

		synchronized (this)
		{
			LastUpdated = System.currentTimeMillis();
		}
	}

	public void SetData(byte[] Blob)
	{
		UpdateID(this, Blob);
		UpdatePosition(this, Blob);
		UpdateSpeed(this, Blob);
		UpdateDirection(this, Blob);

		synchronized (this)
		{
			LastUpdated = System.currentTimeMillis();
		}
	}

	public void SetData(byte[] buffer, int NumberOfBytes)
	{

		String LocalGPSData = new String(buffer, 38, NumberOfBytes);

		String[] DualTokens = LocalGPSData.split(",");
		for (String DualToken : DualTokens)
		{

			try
			{
				if (DualToken.split(":").length < 2)
				{
					continue;
				}
				String Token1 = DualToken.split(":")[0].trim();
				String Token2 = DualToken.split(":")[1].trim();
				switch (Token1)
				{
					case "MyID":
						if (ID == "")
						{
							ID = Token2;
						}
						break;
					case "Latitude":
						Latitude = Float.parseFloat(Token2);
						LatLongAltLocation.Y = Latitude;
						Location.Y = (float) LatLonToXY.LatLonToXYInMiles(Latitude, 0, 0, 0, LatLonToXY.Unit.Mile);
						break;
					case "Longitude":
						Longitude = Float.parseFloat(Token2);
						LatLongAltLocation.X = Longitude;
						Location.X = (float) LatLonToXY.LatLonToXYInMiles(0, Longitude, 0, 0, LatLonToXY.Unit.Mile);
						break;
					case "Altitude":
						Altitude = Float.parseFloat(Token2);
						LatLongAltLocation.Z = Altitude;
						Location.Z = this.Altitude;
						break;
					case "Speed":
						Speed = Float.parseFloat(Token2);
						break;
					case "Direction":
						Direction = Float.parseFloat(Token2);
						break;
					default:
						Log.e(TAG, "Wrong Local Data Received From Bluetooth Device");
						break;
				}

			}
			catch (Exception Exception)
			{
				Log.e(TAG, Exception.getMessage(), Exception);
			}
		}
		synchronized (this)
		{
			LastUpdated = System.currentTimeMillis();
		}
	}

	public float DistanceFrom(Car Car)
	{
		float DX_2 = (Location.X - Car.Location.X);
		DX_2 = DX_2 * DX_2;
		float DY_2 = (Location.Y - Car.Location.Y);
		DY_2 = DY_2 * DY_2;
		return (float) Math.sqrt(DX_2 + DY_2);

	}

	public float DistanceFrom(Car Car, DistanceUnit DistanceUnit)
	{
		float DX = ConvertDistance((Location.X - Car.Location.X), DistanceUnit);
		float DX_2 = DX * DX;
		float DY = ConvertDistance((Location.Y - Car.Location.Y), DistanceUnit);
		float DY_2 = DY * DY;
		RelativeLocation.X = DX;
		RelativeLocation.Y = DY;
		RelativeLocation.Z = (Direction - Car.Direction);
		return (float) Math.sqrt(DX_2 + DY_2);

	}

	public String ID()
	{
		return String.format("ID = %s", ID);
	}

	/*
	public String Location()
	{
		return String.format("@(%.2f , %.2f )", Location.X, Location.Y);
	}
*/
	public String Location()
	{
		return String.format("@(%.3f , %.3f )", Longitude, Latitude);
	}

	public String Distance()
	{
		return String.format("Distance = %.3f", Distance);
	}

	public String Speed()
	{
		return String.format("Speed = %.2f M/H", Speed);
	}

	public String Position(PositionType PositionType)
	{
		switch (PositionType)
		{
			case Latitude:
				return String.format("(%.2f)", Latitude);
			case Longitude:
				return String.format("(%.2f)", Longitude);
			case Altitude:
				return String.format("(%.2f)", Altitude);
			case NoLatitude:
				return String.format("(%.2f,%.2f)", Longitude, Altitude);
			case NoLongitude:
				return String.format("(%.2f,%.2f)", Latitude, Altitude);
			case NoAltitude:
				return String.format("(%.2f,%.2f)", Latitude, Longitude);
			case All:
			default:
				return String.format("(%.2f,%.2f,%.2f)", Latitude, Longitude, Altitude);
		}
	}

	public enum CarType
	{
		OtherCar, MyCar
	}

	public enum PositionType
	{
		Latitude, Longitude, Altitude, NoLatitude, NoLongitude, NoAltitude, All
	}


	public enum DistanceUnit
	{
		Miles, Meters, KiloMeters, Inches
	}

	public int Location_Index;
	public int MODIFY_SPEED_RATE = 10; // in Seconds
	public int TIME_LEFT_TO_UPDATE_SPEED = 10; // in Seconds

	public Car(float Speed, int Location_Index, int MODIFY_SPEED_RATE)
	{
		this.MODIFY_SPEED_RATE = TIME_LEFT_TO_UPDATE_SPEED = MODIFY_SPEED_RATE;
		this.Speed = Speed;
		this.Location_Index = Location_Index;
		/*this.GraphicsLocation.X = Route.Locations.get(Location_Index).X;
		this.GraphicsLocation.Y = Route.Locations.get(Location_Index).Y;*/
	}


	public void ModifySpeed(float Speed)
	{
		this.Speed = Speed;
	}

	public void Move(Car MyCar)
	{

		{
			Location_Index += (int) Speed;
			if (Location_Index >= Route.Locations.size())
			{
				Location_Index = Location_Index - Route.Locations.size();
			}
			GraphicsLocation.X = Location.X = Longitude = Route.Locations.get(Location_Index).X;
			GraphicsLocation.X = Location.Y = Latitude = Route.Locations.get(Location_Index).Y;
			TIME_LEFT_TO_UPDATE_SPEED--;
			int DIR = (int) Math.floor((float) Location_Index / ((float) (Route.FIELD__WIDTH + 1)));
			switch (DIR)
			{
				case 0:
					this.Direction = 90;
					break;
				case 1:
					this.Direction = 180;
					break;
				case 2:
					this.Direction = 270;
					break;
				case 3:
					this.Direction = 0;
					break;
				default:
					this.Direction = 0;
			}
			Distance = DistanceFrom(MyCar);
			//this.Direction = 0;
		}
		this.LastUpdated = System.currentTimeMillis();
	}

}
