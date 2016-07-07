package com.tan.CaptureImageAndCrop;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

	private static final int ACTIVITY_START_CAMERA_APP = 0;
	private static final int ACTIVITY_START_GALLARY = 1;
	private static final int ACTIVITY_START_CROP = 2;
	public ImageView mPhotoCapturedImageView;
	String mImageFileLocation;
	ImageViewGetterSetter imageViewGetterSetter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mPhotoCapturedImageView = (ImageView) findViewById(R.id.capturePhotoImageView);
		imageViewGetterSetter = new ImageViewGetterSetter();
		imageViewGetterSetter.setImageView(mPhotoCapturedImageView);

	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		finish();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mPhotoCapturedImageView = (ImageView) findViewById(R.id.capturePhotoImageView);
		ImageViewGetterSetter imageViewGetterSetter = new ImageViewGetterSetter();
		imageViewGetterSetter.setImageView(mPhotoCapturedImageView);
	}

	public void openCamera(View view) {
		Intent callCameraApplicationIntent = new Intent();
		callCameraApplicationIntent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
		File photofile = null;
		try {
			photofile = createImageFile();
		} catch (IOException e) {
			e.printStackTrace();
			Toast.makeText(MainActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
		}

		callCameraApplicationIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photofile));
		startActivityForResult(callCameraApplicationIntent, ACTIVITY_START_CAMERA_APP);
	}

	public void openGallery(View view) {

		Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media
				.EXTERNAL_CONTENT_URI);
		startActivityForResult(intent, ACTIVITY_START_GALLARY);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == ACTIVITY_START_CAMERA_APP && resultCode == RESULT_OK) {
			rotateImage(setReduceImageSizeFromGallery(mImageFileLocation), mImageFileLocation);
		}
		if (requestCode == ACTIVITY_START_CROP && data!=null) {
//			Toast.makeText(MainActivity.this, "Cropeed image at MainActivity", Toast.LENGTH_SHORT)
//					.show();
			String dataString="";
			dataString = data.getStringExtra("outputUri");

			if (dataString != null || !dataString.equalsIgnoreCase("")) {
//				Uri uri = (Uri) data.getExtras().get("outputUri");
//				File f = new File(uri.getPath());
//			String location = data.getData().toString();
				Bitmap bitmap = BitmapFactory.decodeFile(dataString);
				mPhotoCapturedImageView.setImageBitmap(bitmap);
			}
		}
		if (requestCode == ACTIVITY_START_GALLARY && resultCode == RESULT_OK) {
			Uri URI = data.getData();
			String[] FILE = {MediaStore.Images.Media.DATA};
			Cursor cursor = getContentResolver().query(URI,
					FILE, null, null, null);
			cursor.moveToFirst();
			int columnIndex = cursor.getColumnIndex(FILE[0]);
			String ImageDecode = cursor.getString(columnIndex);

			Bitmap photoCapturedBitmap = BitmapFactory.decodeFile(ImageDecode);
			cursor.close();
			rotateImage(setReduceImageSizeFromGallery(ImageDecode), ImageDecode);
		}
	}

	File createImageFile() throws IOException {
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		String imageFileName = "IMAGE_" + timeStamp + "_";
		File storageDirectory = Environment.getExternalStoragePublicDirectory(Environment
				.DIRECTORY_PICTURES);
		File image = File.createTempFile(imageFileName, ".jpg", storageDirectory);
		mImageFileLocation = image.getAbsolutePath();
		return image;
	}


	private Bitmap setReduceImageSizeFromGallery(String location) {

		int targetImageViewWidth = imageViewGetterSetter.getImageView().getWidth();
		int targetImageViewHeight = imageViewGetterSetter.getImageView().getHeight();

		BitmapFactory.Options bmOptions = new BitmapFactory.Options();
		bmOptions.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(location, bmOptions);
		int cameraImageWidth = bmOptions.outWidth;
		int cameraImageHeight = bmOptions.outHeight;

		int scaleFactor = Math.min(cameraImageWidth / targetImageViewWidth, cameraImageHeight /
				targetImageViewHeight);
		bmOptions.inSampleSize = scaleFactor;
		bmOptions.inJustDecodeBounds = false;

		return BitmapFactory.decodeFile(location, bmOptions);
	}

	private void rotateImage(Bitmap bitmap, String location) {
		ExifInterface exifInterface = null;
		try {
			exifInterface = new ExifInterface(location);
		} catch (IOException e) {
			e.printStackTrace();
		}
		int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
				ExifInterface.ORIENTATION_UNDEFINED);
		Matrix matrix = new Matrix();
		switch (orientation) {
			case ExifInterface.ORIENTATION_ROTATE_90:
				matrix.setRotate(90);
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				matrix.setRotate(180);
				break;
			case ExifInterface.ORIENTATION_ROTATE_270:
				matrix.setRotate(270);

			case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
				matrix.preScale(true ? -1 : 1, false ? -1 : 1);
//				return flip(bitmap, true, false);

			case ExifInterface.ORIENTATION_FLIP_VERTICAL:
				matrix.preScale(false ? -1 : 1, true ? -1 : 1);

			default:
				break;
		}


		Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap
				.getHeight(), matrix, true);

		Intent intent = new Intent(this, CropActivity.class);
//		Uri uri = Uri.fromFile(new File(location));
		intent.putExtra("location", location);
		startActivityForResult(intent, ACTIVITY_START_CROP);


		//creating byteArray
//		ByteArrayOutputStream stream = new ByteArrayOutputStream();
//		rotatedBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
//		byte[] byteArray = stream.toByteArray();
//		Bundle bundle = new Bundle();
//		bundle.putByteArray("image", byteArray);
//
//		mPhotoCapturedImageView.setImageBitmap(rotatedBitmap);
	}
}

class ImageViewGetterSetter {

	ImageView imageView;

	public ImageViewGetterSetter() {
	}

	public ImageView getImageView() {
		return imageView;
	}

	public void setImageView(ImageView imageView) {
		this.imageView = imageView;
	}
}
