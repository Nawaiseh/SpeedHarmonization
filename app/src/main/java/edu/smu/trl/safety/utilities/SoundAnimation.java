package edu.smu.trl.safety.utilities;

import android.content.Context;
import android.media.MediaPlayer;

/**
 * Created by TRL on 2/18/2016.
 */


public class SoundAnimation
{

	MediaPlayer MediaPlayer;
	Context Context;
	int SoundFileID;

	public SoundAnimation(Context Context, int SoundFileID)
	{
		this.Context = Context;
		this.SoundFileID = SoundFileID;
	}

	public void Start()
	{
		MediaPlayer = MediaPlayer.create(Context, SoundFileID);
		try
		{
			if (MediaPlayer != null)
			{
				MediaPlayer.setLooping(true);
				//  MediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
				MediaPlayer.start();
			} else
			{

				int x = 0;
			}
		}
		catch (Exception Exception)
		{

		}

	}

	public void Stop()
	{
		try
		{
			if (MediaPlayer != null)
			{
				if (MediaPlayer.isPlaying())
				{
					MediaPlayer.stop();
					MediaPlayer.setLooping(false);
				}
				MediaPlayer.release();
				MediaPlayer = null;
			}
		}
		catch (Exception Exception)
		{

		}
	}
}

