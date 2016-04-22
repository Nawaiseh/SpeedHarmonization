package edu.smu.trl.safety.Data;

import java.util.ArrayList;
import java.util.List;

public class Route
{

	public static final float FIELD_LENGTH = 1000; // In Meteres
	public static final float FIELD__WIDTH = 1000; // In Meteres

	public static final float FOCUSED_FIELD_LENGTH = 150; // In Meteres
	public static final float FOCUSED_FIELD__WIDTH = 150; // In Meteres

	public static final float FIELD_LENGTH_2 = FIELD_LENGTH / 2F; // In Meteres
	public static final float FIELD__WIDTH_2 = FIELD__WIDTH / 2F; // In Meteres

	public static final float DISPLAYED_FIELD_LENGTH = 1200; // In Meteres
	public static final float DISPLAYED_FIELD__WIDTH = 1200; // In Meteres

	public static final float DISPLAYED_FIELD_LENGTH_2 = DISPLAYED_FIELD_LENGTH / 2F; // In Meteres
	public static final float DISPLAYED_FIELD__WIDTH_2 = DISPLAYED_FIELD__WIDTH / 2F; // In Meteres

	public static final float DISPLAYED_LENGTH_RATIO = DISPLAYED_FIELD_LENGTH / FIELD_LENGTH;
	public static final float DISPLAYED__WIDTH_RATIO = DISPLAYED_FIELD__WIDTH / FIELD__WIDTH;

	public static final float LENGTH_RATIO = FIELD_LENGTH  / DISPLAYED_FIELD_LENGTH;
	public static final float _WIDTH_RATIO = FIELD__WIDTH / DISPLAYED_FIELD__WIDTH;

	public static final float GapX = 1F - ( ( FIELD__WIDTH / DISPLAYED_FIELD__WIDTH ) / 2F );
	public static final float GapY = 1F - ( ( FIELD_LENGTH / DISPLAYED_FIELD_LENGTH ) / 2F );

	public static final List<Number3d> Locations = new ArrayList<>();
	public static final List<Number3d> MyRoute = new ArrayList<>();
	public static final List<Number3d> InnerRoute = new ArrayList<>();
	public static final List<Number3d> OuterRoute = new ArrayList<>();

	private static void FilleRoute(List<Number3d> Route, float WIDTH, float HEIGHT)
	{
		float StartX = -WIDTH / 2f;
		float __EndX = WIDTH / 2f;


		float StartY = -HEIGHT / 2f;
		float __EndY = HEIGHT / 2f;
		for (float X = StartX; X <= __EndX; X++)
		{
			Route.add(new Number3d(X, StartY, 0));
		}
		for (float Y = StartY; Y <= __EndY; Y++)
		{
			Route.add(new Number3d(__EndX, Y, 0));
		}
		for (float X = __EndX; X >= StartX; X--)
		{
			Route.add(new Number3d(X, __EndY, 0));
		}
		for (float Y = __EndY; Y >= StartY; Y--)
		{
			Route.add(new Number3d(StartX, Y, 0));
		}

	}

	public static void BuildRoute()
	{

		float X_GAP = 5;
		float Y_GAP = 10;
		FilleRoute(Locations, FIELD__WIDTH, FIELD_LENGTH);
		//	FilleRoute(InnerRoute, FIELD__WIDTH - (X_GAP * 2F), FIELD_LENGTH - (Y_GAP * 2F));
		//	FilleRoute(OuterRoute, FIELD__WIDTH + (X_GAP * 2F), FIELD_LENGTH + (Y_GAP * 2F));

		String Msg = "Inner_X \t Inner_Y\tX \t Y\tOuter_X \t Outer_Y";
		Number3d Location;

		for (int Index = 0; Index < Locations.size(); Index++)
		{
			Location = Locations.get(Index);

			Msg = String.format("%s\n%f\t%f", Msg, Location.X, Location.Y);
		}
		/*for (int Index = 0; Index < OuterRoute.size(); Index++)
		{
			if (Index < InnerRoute.size())
			{
				Location = InnerRoute.get(Index);
				Msg = String.format("%s\n%f\t%f", Msg, Location.X, Location.Y);
			} else
			{
				Msg = String.format("%s\n\t\t", Msg);
			}
			if (Index < Locations.size())
			{
				Location = Locations.get(Index);
				Msg = String.format("%s\t%f\t%f", Msg, Location.X, Location.Y);
			} else
			{
				Msg = String.format("%s\t\t\t", Msg);
			}
			if (Index < OuterRoute.size())
			{
				Location = OuterRoute.get(Index);
				Msg = String.format("%s\t%f\t%f", Msg, Location.X, Location.Y);
			} else
			{
				Msg = String.format("%s", Msg);
			}
		}*/

	}

}
