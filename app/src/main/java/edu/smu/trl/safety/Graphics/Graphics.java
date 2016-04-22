package edu.smu.trl.safety.Graphics;


import android.content.Context;
import android.content.res.Resources;
import android.view.SurfaceHolder;

public class Graphics
{

	private Thread Thread;
	private GraphicsLoop GameLoop;

	public Graphics(Context Context, SurfaceHolder SurfaceHolder, Resources Resources)
	{
		GameLoop = new GraphicsLoop(Context, SurfaceHolder, Resources);
	}

	public void Start()
	{

		Thread = new Thread(GameLoop);
		Thread.start();

	}

	public void Stop()
	{

		if (GameLoop != null)
		{
			GameLoop.PleaseStop();
			try
			{
				Thread.join();
			}
			catch (InterruptedException InterruptedException)
			{
				InterruptedException.printStackTrace();
			}
		}

	}
}
