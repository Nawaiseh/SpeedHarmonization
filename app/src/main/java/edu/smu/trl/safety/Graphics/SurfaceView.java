package edu.smu.trl.safety.Graphics;

import android.content.Context;
import android.content.res.Resources;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;

public class SurfaceView
		extends
		android.view.SurfaceView
		implements
		android.view.SurfaceView.OnTouchListener,
		android.view.SurfaceView.OnClickListener,
		SurfaceHolder.Callback2
{

	//private final RendererActivity RendererActivity;
	private Graphics Graphics;


	public SurfaceView(Context Context, Resources Resources)
	{
		super(Context);

		getHolder().addCallback(this);

		Graphics = new Graphics(Context, this.getHolder(), Resources);
	}

	@Override
	public boolean onTouch(View View, MotionEvent event)
	{
		return false;
	}

	@Override
	public void onClick(View View)
	{

	}

	@Override
	public void surfaceRedrawNeeded(SurfaceHolder SurfaceHolder)
	{

	}

	@Override
	public void surfaceCreated(SurfaceHolder SurfaceHolder)
	{
		if (Graphics != null)
		{
			Graphics.Start();
		}else{
			Graphics = new Graphics(getContext(), this.getHolder(), getResources());
			Graphics.Start();
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder SurfaceHolder, int format, int width, int height)
	{

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder SurfaceHolder)
	{
		if (Graphics != null)
		{
			Graphics.Stop();
		}
		Graphics = null;
	}
}
