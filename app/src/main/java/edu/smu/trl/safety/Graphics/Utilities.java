package edu.smu.trl.safety.Graphics;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;


public class Utilities
{

	private static final int REQUEST_CODE_ASK_PERMISSIONS = 123;
	private static final String[] PermissionsToCheck = new String[]
			{
					android.Manifest.permission.BLUETOOTH_ADMIN,
					android.Manifest.permission.BLUETOOTH,
					android.Manifest.permission.BLUETOOTH_PRIVILEGED,
					android.Manifest.permission.WRITE_SETTINGS,
					android.Manifest.permission.WAKE_LOCK,
					android.Manifest.permission.READ_EXTERNAL_STORAGE,
					android.Manifest.permission.WRITE_EXTERNAL_STORAGE
			};

/* public static void AskForPermissions(Context Context)
 {
     List<String> NeededPermissions = new ArrayList<>();
     for(String Permission : PermissionsToCheck)
     {
         int WritePermission = Context.checkSelfPermission(Permission);
         if (WritePermission != PackageManager.PERMISSION_GRANTED)
         {
             NeededPermissions.add(Permission);
         }
     }

     String [] RequestestPermissions = NeededPermissions.toArray(new String[NeededPermissions.size()]);
     if (RequestestPermissions!= null && RequestestPermissions.length>0)
     {
         ( (AppCompatActivity) Context).requestPermissions(RequestestPermissions, REQUEST_CODE_ASK_PERMISSIONS);
     }
 }*/

	public static Bitmap RotateBitmap(Bitmap SourceBitmap, float Theta)
	{
		Matrix matrix = new Matrix();
		matrix.postRotate(Theta);
		return Bitmap.createBitmap(SourceBitmap, 0, 0, SourceBitmap.getWidth(), SourceBitmap.getHeight(), matrix, true);
	}

	public static void SetTextSize(Paint Paint, float NewFontSize, String text)
	{
		final float TestFontSize = 48f;
		Paint.setTextSize(TestFontSize);
		Rect Bounds = new Rect();
		Paint.getTextBounds(text, 0, text.length(), Bounds);
		float desiredTextSize = TestFontSize * NewFontSize / Bounds.width();
		Paint.setTextSize(desiredTextSize);
	}

	public static Bitmap ResizeBitmap(Bitmap Bitmap, float New_Width, float New_Height)
	{
		float Old_Width = Bitmap.getWidth();
		float Old_Height = Bitmap.getHeight();
		float Width_Scale = ((float) New_Width) / Old_Width;
		float Height_Scale = ((float) New_Height) / Old_Height;

		Matrix Matrix = new Matrix();
		Matrix.postScale(Width_Scale, Height_Scale);

		Bitmap resizedBitmap = Bitmap.createBitmap(Bitmap, 0, 0, (int) Math.round(Old_Width), (int) Math.round(Old_Height), Matrix, false);
		Bitmap.recycle();
		return resizedBitmap;
	}
}
