package edu.smu.trl.safety.bluetooth;

/**
 * Created by TRL on 3/7/2016.
 */
public class LastUpdateChecker_Thread extends Thread
{

	public static Object Lock;
	private final BluetoothChatService BluetoothChatService;

	public LastUpdateChecker_Thread(BluetoothChatService BluetoothChatService)
	{

		this.BluetoothChatService = BluetoothChatService;
		Lock = BluetoothChatService.RendererActivity.Cars;
	}

	public void run()
	{
		while (true)
		{
			long TimeNow = System.currentTimeMillis();

			synchronized (Lock)
			{
				if ((BluetoothChatService.AlertIsGoing) && ((TimeNow - BluetoothChatService.LastUpdate) > 3000))
				{
					BluetoothChatService.MessageHandler.post(new Runnable()
					{
						@Override
						public void run()
						{
							BluetoothChatService.RendererActivity.StopColorAnimation(BluetoothChatService.RendererActivity.Layout, BluetoothChatService.RendererActivity.ValueAnimator, BluetoothChatService.RendererActivity.NormalColor);
						}
					});

					// MessageHandler.obtainMessage(Constants.STOP_ALERT_SOUND, "No Message".length(), -1, "No Message").sendToTarget();
					BluetoothChatService.AlertIsGoing = false;
				}
			}
			try
			{
				Thread.sleep(50);

			}
			catch (Exception Exception)
			{
				Exception.printStackTrace();
			}
		}
	}
}


