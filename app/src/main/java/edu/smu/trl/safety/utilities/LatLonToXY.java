package edu.smu.trl.safety.utilities;


public class LatLonToXY
{

	public static double LatLonToXYInMiles(double latitude1, double longitude1, double latitude2, double longitude2, Unit Unit)
	{
		double theta = longitude1 - longitude2;
		double Distance = Math.sin(Math.toRadians(latitude1)) * Math.sin(Math.toRadians(latitude2)) + Math.cos(Math.toRadians(latitude1)) * Math.cos(Math.toRadians(latitude2)) * Math.cos(Math.toRadians(theta));
		Distance = Math.acos(Distance);
		Distance = Math.toDegrees(Distance);
		Distance = Distance * 60 * 1.1515;
		switch (Unit)
		{
			case Mile:
				Distance = Distance * 0.8684;
				break;
			case KiloMeter:
				Distance = Distance * 1.609344;
				break;
			case Meter:
				Distance = Distance * 0.001609344;
				break;
		}
		if (latitude1 < 0 && longitude1 == 0 && latitude2 == 0 && longitude2 == 0)
		{
			Distance = (Distance < 0) ? Distance : -Distance;
		}
		if (latitude2 < 0 && longitude1 == 0 && latitude1 == 0 && longitude2 == 0)
		{
			Distance = (Distance < 0) ? Distance : -Distance;
		}
		if (longitude1 < 0 && latitude1 == 0 && latitude2 == 0 && longitude2 == 0)
		{
			Distance = (Distance < 0) ? Distance : -Distance;
		}
		if (longitude2 < 0 && longitude1 == 0 && latitude1 == 0 && longitude2 == 0)
		{
			Distance = (Distance < 0) ? Distance : -Distance;
		}
		return (Distance);
	}

	public enum Unit
	{
		KiloMeter, Mile, Meter
	}

}
