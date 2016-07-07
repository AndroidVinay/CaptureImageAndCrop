package com.tan.CaptureImageAndCrop;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.isseiaoki.simplecropview.CropImageView;
import com.isseiaoki.simplecropview.callback.CropCallback;
import com.isseiaoki.simplecropview.callback.LoadCallback;
import com.isseiaoki.simplecropview.callback.SaveCallback;

import java.io.File;

public class CropActivity extends AppCompatActivity {

	CropImageView crop_image_view;
	Button crop;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_crop);
		crop_image_view = (CropImageView) findViewById(R.id.crop_image_view);
		crop = (Button) findViewById(R.id.crop_image);
		final String location = getIntent().getExtras().getString("location");
		Bitmap bitmap = BitmapFactory.decodeFile(location);

		crop_image_view.setImageBitmap(bitmap);

		Uri uri = Uri.fromFile(new File(location));

		crop_image_view.startLoad(uri,
				new LoadCallback() {
					@Override
					public void onSuccess() {

					}

					@Override
					public void onError() {

					}
				});

		crop.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				final Uri SaveUri = Uri.fromFile(new File(location));

				crop_image_view.startCrop(SaveUri, new CropCallback() {
					@Override
					public void onSuccess(Bitmap cropped) {
						crop_image_view.setImageBitmap(cropped);

					}

					@Override
					public void onError() {

					}
				}, new SaveCallback() {
					@Override
					public void onSuccess(Uri outputUri) {
//						Toast.makeText(CropActivity.this, "" + outputUri, Toast.LENGTH_SHORT)
//								.show();
						String location = outputUri.getPath().toString();
						Intent intent = new Intent();
						intent.putExtra("outputUri", location);
						setResult(2, intent);
						finish();
					}

					@Override
					public void onError() {

					}
				});
			}
		});

	}


	@Override
	public void onBackPressed() {
		super.onBackPressed();
		finish();
	}
}
