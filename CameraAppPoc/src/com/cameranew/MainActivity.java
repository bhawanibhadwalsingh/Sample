package com.cameranew;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;



import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends Activity {
	ImageView photoset;
	Button click, sharetowhatsapp;
	AlertDialog dialog;
	Bitmap sharebitmap;

	private static final int REQ_CODE_PICK_IMAGE = 3;
	Uri recus = null;
	private static final String TEMP_PHOTO_FILE = "temporary_holder.jpg";
	private Uri picUri;
	File file;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);

		init();
		methodDialogOpen();
		click.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dialog.show();
			}
		});

		sharetowhatsapp.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				try {
					sharemethodForWhatsapp();
				} catch (Exception e) {
					// TODO: handle exception
				}
				
			}
		});

	}

	protected void sharemethodForWhatsapp() {
		// TODO Auto-generated method stub



		if (sharebitmap != null) {
			if (!iswhatsupClientInstalled(MainActivity.this)) {
				goTowhatsupMarket(MainActivity.this);
				return;
			} else {

				Intent shareIntent = new Intent();
				shareIntent.setAction(Intent.ACTION_SEND);
				shareIntent.putExtra(Intent.EXTRA_TEXT, "Visto Card");
				// File file = new File(file);
				shareIntent.putExtra(Intent.EXTRA_STREAM, recus);
				shareIntent.setType("image/jpeg");
				shareIntent.setPackage("com.whatsapp");
				startActivityForResult(
						(Intent.createChooser(shareIntent, "Share...")), 100);
				// mShare.setVisibility(View.VISIBLE);
				photoset.invalidate();

			}
		} else {
			Toast.makeText(MainActivity.this, "Share Problem",
					Toast.LENGTH_LONG).show();
		}
	}

	// method to open dialog
	protected void methodDialogOpen() {
		// TODO Auto-generated method stub

		final String[] items = new String[] { "Take from camera",
				"Select from gallery" };
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.select_dialog_item, items);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setTitle("Select Image");
		builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) { // pick from
																	// camera
				if (item == 0) {

					// intent from camera

					Intent intent = new Intent(
							"android.media.action.IMAGE_CAPTURE");
					/* create instance of File with name img.jpg */
					// file = new
					// File(Environment.getExternalStorageDirectory()+File.separator
					// + "imgonrw.jpg");
					/* put uri as extra in intent object */
					String root = Environment.getExternalStorageDirectory()
							.toString();
					File myDir = new File(root + "/POC");
					myDir.mkdirs();

					Random generator = new Random();
					int n = 10000;
					n = generator.nextInt(n);
					String fname = "Image-" + n + ".jpg";
					file = new File(myDir, fname);
					if (file.exists())
						file.delete();

					recus = Uri.fromFile(file);
					intent.putExtra(MediaStore.EXTRA_OUTPUT, recus);
					/*
					 * start activity for result pass intent as argument and
					 * request code
					 */
					startActivityForResult(intent, 1);

				} else { // pick from file

					Intent photoPickerIntent = new Intent(
							Intent.ACTION_PICK,
							android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
					photoPickerIntent.setType("image/*");
					photoPickerIntent.putExtra("crop", "true");
					photoPickerIntent.putExtra(MediaStore.EXTRA_OUTPUT,
							getTempUri());
					
					
					photoPickerIntent.putExtra("outputFormat",
							Bitmap.CompressFormat.JPEG.toString());
					startActivityForResult(photoPickerIntent,
							REQ_CODE_PICK_IMAGE);
					
				}
			}
		});

		dialog = builder.create();
	}

	// initialize variable
	private void init() {
		// TODO Auto-generated method stub
		photoset = (ImageView) findViewById(R.id.photo);
		click = (Button) findViewById(R.id.btn_edit);
		sharetowhatsapp = (Button) findViewById(R.id.btnsharetowharsapp);

	}

	// onActivity result

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		// if request code is same we pass as argument in startActivityForResult
		if (requestCode == 1) {

			try {

				cropCapturedImage(recus);
			} catch (ActivityNotFoundException aNFE) {
				// display an error message if user device doesn't support
				String errorMessage = "Sorry - your device doesn't support the crop action!";
				Toast toast = Toast.makeText(this, errorMessage,
						Toast.LENGTH_SHORT);
				toast.show();
			}
		} else if (requestCode == 2) {
			// Create an instance of bundle and get the returned data
			Bundle extras = data.getExtras();
			// get the cropped bitmap from extras
			Bitmap thePic = extras.getParcelable("data");
			// set image bitmap to image view
			
			sharebitmap=thePic;
			photoset.setImageBitmap(thePic);
		} else if (requestCode == REQ_CODE_PICK_IMAGE) {

			// pick from gallery

			if (resultCode == RESULT_OK) {
				if (data != null) {

					File tempFile = getTempFile();

					String filePath = Environment.getExternalStorageDirectory()
							+ "/" + TEMP_PHOTO_FILE;
					System.out.println("path " + filePath);
					
					

					Bitmap selectedImage = BitmapFactory.decodeFile(filePath);
					sharebitmap=selectedImage;
					photoset.setImageBitmap(selectedImage);

//					if (tempFile.exists()) {
//						tempFile.delete();
//					}
				}
			}

			//
		}

	}

	// create cropCapturedImage method
	public void cropCapturedImage(Uri picUri) {
		// call the standard crop action intent
		Intent cropIntent = new Intent("com.android.camera.action.CROP");
		// indicate image type and Uri of image
		cropIntent.setDataAndType(picUri, "image/*");
		// set crop properties
		cropIntent.putExtra("crop", "true");
		// indicate aspect of desired crop
		cropIntent.putExtra("aspectX", 1);
		cropIntent.putExtra("aspectY", 1);
		// indicate output X and Y
		cropIntent.putExtra("outputX", 256);
		cropIntent.putExtra("outputY", 256);
		// retrieve data on return
		cropIntent.putExtra("return-data", true);
		// start the activity - we handle returning in onActivityResult
		startActivityForResult(cropIntent, 2);
	}

	private Uri getTempUri() {
		
		recus=	Uri.fromFile(getTempFile());
		System.out.println("update value"+recus);
		return Uri.fromFile(getTempFile());
	}

	private File getTempFile() {

		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {

			File file = new File(Environment.getExternalStorageDirectory(),
					TEMP_PHOTO_FILE);
			try {
				file.createNewFile();
			} catch (IOException e) {
			}

			return file;
		} else {

			return null;
		}
	}

	public boolean iswhatsupClientInstalled(Context myContext) {
		PackageManager myPackageMgr = myContext.getPackageManager();
		try {
			myPackageMgr.getPackageInfo("com.whatsapp",
					PackageManager.GET_ACTIVITIES);
		} catch (PackageManager.NameNotFoundException e) {
			return (false);
		}
		return (true);
	}

	public void goTowhatsupMarket(Context myContext) {
		Uri marketUri = Uri.parse("market://details?id=com.whatsapp");

		Intent myIntent = new Intent(Intent.ACTION_VIEW, marketUri);
		myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		myContext.startActivity(myIntent);

		return;
	}

}
