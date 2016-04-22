package edu.smu.trl.safety.Graphics;


import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.SurfaceHolder;

import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import edu.smu.trl.safety.Data.Car;
import edu.smu.trl.safety.Data.Car.DistanceUnit;
import edu.smu.trl.safety.Data.Number3d;
import edu.smu.trl.safety.Data.Route;
import edu.smu.trl.safety.RendererActivity;
import edu.smu.trl.safety.bluetooth.Constants;

public class GraphicsLoop implements Runnable
{

	private static final int MAXIMUM_ALLOWED_SKIPPED_FRAMES = 10;
	private static final long SIMULATION_TIME_IN_MILLISECONDS = 2;

	private static final long FRAME_TIME_IN_MILLISECONDS = 20;


	private static final long LAST_UPDATE__MAX_THRESHOLD = 50000000;
	private final RendererActivity RendererActivity;
	private final SurfaceHolder SurfaceHolder;
	private final Resources Resources;
	private final Paint Paint = new Paint();
	private final Car MyCar;
	Random Random = new Random();
	float DX = (Car.Width / 2F);
	float DY = (Car.Length / 2F);
	Number3d My_Car_Location = new Number3d(0, 0, 0);
	Number3d Other_Car_Location = new Number3d(0, 0, 0);
	Number3d Location = new Number3d(0, 0, 0);
	float MinDistance = Float.MAX_VALUE;
	private boolean Running = true;
	private float Width, Width__2;
	private float Height, Height_2;
	private float My_Rotation_Angle_In_Degrees = 0;
	private float Theta;
	private float Scale = 0;
	private float Screen_To_Display_Field__Width_Ratio;
	private float Screen_To_Display_Field_Length_Ratio;
	private float Translated_CenterX;
	private float Translated_CenterY;

	public GraphicsLoop(Context Context, SurfaceHolder SurfaceHolder, Resources Resources)
	{
		this.SurfaceHolder = SurfaceHolder;
		this.Resources = Resources;
		this.RendererActivity = (RendererActivity) Context;
		MyCar = RendererActivity.MyCar;
		Paint.setTextSize(10);   // Font Size
	}

	@Override
	public void run()
	{
		long Simulation_Time = new Date().getTime();
		long Frame_Start_Time = Simulation_Time;
		while (Running)
		{
			Simulation_Time = ApplyPhysics(Simulation_Time, Frame_Start_Time);
			TryToDrawGraphics();
			Frame_Start_Time = WaitForNextFrame(Frame_Start_Time);

		}
	}

	private long WaitForNextFrame(long Frame_Start_Time)
	{
		long Frame_Next_Start_Time = new Date().getTime();
		long How_Long_It_Took = Frame_Next_Start_Time - Frame_Start_Time;
		long Wait_Time = FRAME_TIME_IN_MILLISECONDS - How_Long_It_Took;
		if (Wait_Time > 0)
		{
			try
			{
				Thread.sleep(Wait_Time);
			}
			catch (InterruptedException InterruptedException)
			{
				InterruptedException.printStackTrace();
			}
		}

		return Frame_Next_Start_Time;
	}

	private long ApplyPhysics(long Simulation_Time, long Frame_Start_Time)
	{
		for (int Skipped = 0; Skipped < MAXIMUM_ALLOWED_SKIPPED_FRAMES; Skipped++)
		{
			if (Simulation_Time >= Frame_Start_Time)
			{
				break;
			}
		}
		Simulation_Time += SIMULATION_TIME_IN_MILLISECONDS;
		return Simulation_Time;
	}

	private void Check_If_A_Car_Not_Sending_Data_For_A_Long_Time(long TimeNow)
	{
		ArrayList<Car> OutDatedCars = new ArrayList<>();
		for (Car Car : RendererActivity.Cars.values())
		{
			if ((TimeNow - Car.LastUpdated) > LAST_UPDATE__MAX_THRESHOLD)
			{
				OutDatedCars.add(Car);
				continue;
			}
			MinDistance = (MinDistance > Car.Distance) ? Car.Distance : MinDistance;
		}

		if (!OutDatedCars.isEmpty() && !RendererActivity.Cars.isEmpty())
		{
			for (Car Car : OutDatedCars)
			{
				RendererActivity.Cars.remove(Car.ID);
			}
		}

	}

	private void ProcessData(Canvas Canvas)
	{
		long TimeNow = System.currentTimeMillis();


		if (MyCar != null)
		{
			MyCar.DrawingLocation.X = MyCar.RelativeLocation.X = 0.5F * Canvas.getWidth();
			MyCar.DrawingLocation.Y = MyCar.RelativeLocation.Y = 0.5F * Canvas.getHeight();

			Check_If_A_Car_Not_Sending_Data_For_A_Long_Time(TimeNow);
			if (MinDistance <= RendererActivity.BluetoothChatService.ThreasholdDistance)
			{
				String Message = String.format(Locale.US, "Alert!:- A Car is Closer than %d", RendererActivity.BluetoothChatService.ThreasholdDistance);
				RendererActivity.MessageHandler.obtainMessage(Constants.PLAY_ALERT_SOUND, Message.length(), -1, Message).sendToTarget();
				RendererActivity.BluetoothChatService.LastUpdate = System.currentTimeMillis();
			} else
			{

				String Message = "Cars Are Far Enough";
				RendererActivity.MessageHandler.obtainMessage(Constants.STOP_ALERT_SOUND, Message.length(), -1, Message).sendToTarget();
			}

		}

	}

	public void PleaseStop()
	{
		Running = false;
	}

	private void TryToDrawGraphics()
	{
		Canvas Canvas = SurfaceHolder.lockCanvas();
		if (Canvas == null)
		{
			return;
		}
		try
		{
			synchronized (RendererActivity.Cars)
			{
				ProcessData(Canvas);
				DrawGraphics(Canvas);
			}
		}
		finally
		{
			SurfaceHolder.unlockCanvasAndPost(Canvas);
		}
	}

	private void Initialize_Graphics_Settings(Canvas Canvas)
	{
		Canvas.drawColor(Color.LTGRAY);
		int Orientation = Resources.getConfiguration().orientation;
		Width = (Orientation == Configuration.ORIENTATION_LANDSCAPE) ? Canvas.getWidth() : Canvas.getHeight();
		Height = (Orientation == Configuration.ORIENTATION_LANDSCAPE) ? Canvas.getHeight() : Canvas.getWidth();

		Width__2 = Width / 2F;
		Height_2 = Height / 2F;

		Translated_CenterX = Width__2;
		Translated_CenterY = Height_2;

		Screen_To_Display_Field__Width_Ratio = Width / Route.DISPLAYED_FIELD__WIDTH;
		Screen_To_Display_Field_Length_Ratio = Height / Route.DISPLAYED_FIELD_LENGTH;


		My_Rotation_Angle_In_Degrees = (MyCar.Direction);
		Theta = (float) Math.toRadians(My_Rotation_Angle_In_Degrees);

		float ScreenRatio = Width / Height; // > 1 : X Width is bigger
		Scale = (ScreenRatio <= 1) ? (Width / Route.FOCUSED_FIELD__WIDTH) : (Height / Route.FOCUSED_FIELD_LENGTH);

	}

	private void Draw_My_Car(Canvas Canvas)
	{
		Canvas.save();
		Canvas.translate((Width__2 - DX), (Height_2 - DY));
		Canvas.drawBitmap(RendererActivity.Car_Blue, 0, 0, Paint);


		if (RendererActivity.ShowInformation && MyCar != null)
		{
			String GL = String.format("(%.2f , %.2f)", Width__2, Height_2);
			Canvas.drawText(GL, -(GL.length() / 2f) * 5f,  - 50, Paint);
			Canvas.drawText(MyCar.Location(), -(MyCar.Location().length() / 2f) * 5f, -30, Paint);
			Canvas.drawText(MyCar.Speed(), -(MyCar.Speed().length() / 2f) * 5f, -10, Paint);
		}
		Canvas.restore();
		Canvas.save();
		Canvas.drawBitmap(Utilities.RotateBitmap(RendererActivity.Compass, My_Rotation_Angle_In_Degrees), 0, 0, Paint);
		Canvas.restore();
	}

	private void Draw_Other_Cars(Canvas Canvas)
	{
		int I = 0;
		for (Car Car : RendererActivity.Cars.values())
		{
			if (Car != null && Car.ID != null && Car != MyCar)
			{

				Other_Car_Location.X = Car.ConvertDistance(Car.Location.X, DistanceUnit.Meters);
				Other_Car_Location.Y = Car.ConvertDistance(Car.Location.Y, DistanceUnit.Meters);


				Other_Car_Location.RotateAroundPoint(My_Car_Location, Theta);


				Other_Car_Location.X = (Other_Car_Location.X - My_Car_Location.X) * RendererActivity.SignX;
				Other_Car_Location.Y = (Other_Car_Location.Y - My_Car_Location.Y) * RendererActivity.SignY;


				Other_Car_Location.X = (Other_Car_Location.X * Scale);
				Other_Car_Location.Y = (Other_Car_Location.Y * Scale);


				float Theta = (Car.Direction - MyCar.Direction);


				Canvas.rotate(Theta, Other_Car_Location.X, -Other_Car_Location.Y);
				Canvas.drawBitmap(Car.Vehicle, Other_Car_Location.X, -Other_Car_Location.Y, Paint);
				Canvas.rotate(-Theta, Other_Car_Location.X, -Other_Car_Location.Y);

				if (RendererActivity.ShowInformation)
				{

					String GL = String.format("(%.2f , %.2f)", Other_Car_Location.X, Other_Car_Location.Y);
					Canvas.drawText(GL, -( GL.length() / 2f) * 5f, 80 + (100 * I), Paint);
					Canvas.drawText(Car.Location(), -( Car.Location().length() / 2f) * 5f, 60 + (100 * I), Paint);
					Canvas.drawText(Car.Distance(), -( Car.Distance().length() / 2f) * 5f,  40 + (100 * I), Paint);
					Canvas.drawText(Car.Speed(), -( Car.Speed().length() / 2f) * 5f,  20 + (100 * I), Paint);
					I++;
/*
					String GL = String.format("(%.2f , %.2f_", Other_Car_Location.X, Other_Car_Location.Y);
					Canvas.drawText(GL,-(Other_Car_Location.X - GL.length() / 2f) * 5f, Other_Car_Location.Y - 70, Paint);
					Canvas.drawText(Car.Location(),-(Other_Car_Location.X - Car.Location().length() / 2f) * 5f, Other_Car_Location.Y - 50, Paint);
					Canvas.drawText(Car.Distance(), -(Other_Car_Location.X - Car.Distance().length() / 2f) * 5f, Other_Car_Location.Y - 30, Paint);
					Canvas.drawText(Car.Speed(), -(Other_Car_Location.X - Car.Speed().length() / 2f) * 5f, Other_Car_Location.Y - 10, Paint);*/
				}

			}
		}
	}

	private void DrawGraphics(Canvas Canvas)
	{
		Initialize_Graphics_Settings(Canvas);
		Canvas.save();
		Canvas.translate((Width__2 - DX), (Height_2 - DY));
		My_Car_Location.X = Car.ConvertDistance(MyCar.Location.X, DistanceUnit.Meters);
		My_Car_Location.Y = Car.ConvertDistance(MyCar.Location.Y, DistanceUnit.Meters);
		Draw_Other_Cars(Canvas);
		Canvas.restore();
		Draw_My_Car(Canvas);
	}
}
