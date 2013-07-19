/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.calendar.event;

import static android.provider.CalendarContract.EXTRA_EVENT_ALL_DAY;
import static android.provider.CalendarContract.EXTRA_EVENT_BEGIN_TIME;
import static android.provider.CalendarContract.EXTRA_EVENT_END_TIME;

import java.io.File;
import java.sql.Date;
import java.text.SimpleDateFormat;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.ContactsContract.DisplayPhoto;
import android.text.format.Time;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.calendar.AbstractCalendarActivity;
import com.android.calendar.CalendarController;
import com.android.calendar.CalendarController.EventInfo;
import com.android.calendar.CalendarController.EventStyle;
import com.android.calendar.R;
import com.android.calendar.Utils;

public class EditEventActivity extends AbstractCalendarActivity {
	private static final String TAG = "EditEventActivity";

	private static final int PICTURE_MAX_HEIGHT = 120;

	private static final String BUNDLE_KEY_EVENT_ID = "key_event_id";

	private static boolean mIsMultipane;

	private EditEventFragment mEditFragment;

	private EventInfo mEventInfo;

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.simple_frame_layout);

		mEventInfo = getEventInfoFromIntent(icicle);

		mEditFragment = (EditEventFragment) getFragmentManager()
				.findFragmentById(R.id.main_frame);

		mIsMultipane = Utils.getConfigBool(this, R.bool.multiple_pane_config);

		// if (mIsMultipane) {
		// getActionBar().setDisplayOptions(
		// ActionBar.DISPLAY_SHOW_TITLE,
		// ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_HOME
		// | ActionBar.DISPLAY_SHOW_TITLE);
		// getActionBar().setTitle(
		// mEventInfo.id == -1 ? R.string.event_create : R.string.event_edit);
		// }
		// else {
		// getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
		// ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_HOME|
		// ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_SHOW_CUSTOM);
		// }

		// if (mEditFragment == null) {
			Intent intent = null;
			if (mEventInfo.id == -1) {
				intent = getIntent();
				mEventInfo.eventStyle = intent.getIntExtra(Utils.EVENT_STYLE_TYPE, EventStyle.EVNET_STYLE);
			}

			// Intent intent = getIntent();
			if(intent == null){
				mEventInfo.eventStyle = EventStyle.EVNET_STYLE;
			}
			mEditFragment = new EditEventFragment(mEventInfo, false, intent);

			mEditFragment.mShowModifyDialogOnLaunch = getIntent()
					.getBooleanExtra(CalendarController.EVENT_EDIT_ON_LAUNCH,
							false);

			FragmentTransaction ft = getFragmentManager().beginTransaction();
			ft.replace(R.id.main_frame, mEditFragment);
			ft.show(mEditFragment);
			ft.commit();
		// }

		loadPhotoPickSize();
	}

	/**
	 * 
	 * @param icicle
	 * @return
	 */
	private EventInfo getEventInfoFromIntent(Bundle icicle) {
		EventInfo info = new EventInfo();
		long eventId = -1;
		Intent intent = getIntent();
		Uri data = intent.getData();
		if (data != null) {
			try {
				eventId = Long.parseLong(data.getLastPathSegment());
			} catch (NumberFormatException e) {

			}
		} else if (icicle != null && icicle.containsKey(BUNDLE_KEY_EVENT_ID)) {
			eventId = icicle.getLong(BUNDLE_KEY_EVENT_ID);
		}

		boolean allDay = intent.getBooleanExtra(EXTRA_EVENT_ALL_DAY, false);

		long begin = intent.getLongExtra(EXTRA_EVENT_BEGIN_TIME, -1);
		long end = intent.getLongExtra(EXTRA_EVENT_END_TIME, -1);
		if (end != -1) {
			info.endTime = new Time();
			if (allDay) {
				info.endTime.timezone = Time.TIMEZONE_UTC;
			}
			info.endTime.set(end);
		}
		if (begin != -1) {
			info.startTime = new Time();
			if (allDay) {
				info.startTime.timezone = Time.TIMEZONE_UTC;
			}
			info.startTime.set(begin);
		}
		info.id = eventId;

		if (allDay) {
			info.extraLong = CalendarController.EXTRA_CREATE_ALL_DAY;
		} else {
			info.extraLong = 0;
		}
		return info;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			Utils.returnToCalendarHome(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	// take picture or pick a picture code start, chenzhentao 2012-7-16
	public static final String KEY_SCALE_UP_IF_NEEDED = "scaleUpIfNeeded";
	private static final int REQUEST_CODE_CAMERA_WITH_DATA = 0;
	private static final int REQUEST_CODE_PHOTO_PICKED_WITH_DATA = 1;

	private static final File PHOTO_DIR = new File(
			Environment.getExternalStorageDirectory() + "/DCIM/Camera");

	private File mCurrentPhotoFile = null;
	private String mCurrentPhotoName = null;
	private Bitmap mPhoto = null;
	private int mPhotoPickSize;

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_CODE_PHOTO_PICKED_WITH_DATA:
			// Ignore failed requests
			if (resultCode != Activity.RESULT_OK) {
				return;
			}
			// As we are coming back to this view, the editor will be reloaded
			// automatically,
			// which will cause the photo that is set here to disappear. To
			// prevent this,
			// we remember to set a flag which is interpreted after loading.
			// This photo is set here already to reduce flickering.
			mPhoto = data.getParcelableExtra("data");
			mEditFragment.mView.addPicture(scaleImg(mPhoto), mCurrentPhotoName);
			break;
		case REQUEST_CODE_CAMERA_WITH_DATA:
			// Ignore failed requests
			if (resultCode != Activity.RESULT_OK) {
				return;
			}
			doCropPhoto(mCurrentPhotoFile);
			break;
		}
	}

	/**
	 * Sends a newly acquired photo to Gallery for cropping
	 */
	public void doCropPhoto(File f) {
		try {
			// Add the image to the media store
			MediaScannerConnection.scanFile(this,
					new String[] { f.getAbsolutePath() },
					new String[] { null }, null);

			// Launch gallery to crop the photo
			final Intent intent = getCropImageIntent(Uri.fromFile(f));
			startActivityForResult(intent, REQUEST_CODE_PHOTO_PICKED_WITH_DATA);
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(this, R.string.photoPickerNotFoundText,
					Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * Constructs an intent for image cropping.
	 */
	public static Intent getCropImageIntent(Uri photoUri) {
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(photoUri, "image/*");
		intent.putExtra("crop", "true");
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		// intent.putExtra("outputX", mPhotoPickSize);
		// intent.putExtra("outputY", mPhotoPickSize);
		// The following lines are provided and maintained by Mediatek inc.
		intent.putExtra(KEY_SCALE_UP_IF_NEEDED, true);
		// The following lines are provided and maintained by Mediatek inc.
		intent.putExtra("return-data", true);
		return intent;
	}

	private void loadPhotoPickSize() {
		Cursor c = getContentResolver()
				.query(DisplayPhoto.CONTENT_MAX_DIMENSIONS_URI,
						new String[] { DisplayPhoto.DISPLAY_MAX_DIM }, null,
						null, null);
		try {
			c.moveToFirst();
			mPhotoPickSize = c.getInt(0);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			c.close();
		}
	}

	protected void onTakePhotoChosen() {
		try {
			// Launch camera to take photo for selected contact
			PHOTO_DIR.mkdirs();
			mCurrentPhotoName = getPhotoFileName();
			mCurrentPhotoFile = new File(PHOTO_DIR, mCurrentPhotoName);
			final Intent intent = getTakePickIntent(mCurrentPhotoFile);
			startActivityForResult(intent, REQUEST_CODE_CAMERA_WITH_DATA);
		} catch (ActivityNotFoundException e) {
			Toast.makeText(this, R.string.photoPickerNotFoundText,
					Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * Launches Gallery to pick a photo.
	 */
	protected void onPickFromGalleryChosen() {
		try {
			// Launch picker to choose photo for selected contact
			final Intent intent = getPhotoPickIntent();
			startActivityForResult(intent, REQUEST_CODE_PHOTO_PICKED_WITH_DATA);
		} catch (ActivityNotFoundException e) {
			Toast.makeText(this, R.string.photoPickerNotFoundText,
					Toast.LENGTH_LONG).show();
		}
	}

	private String getPhotoFileName() {
		Date date = new Date(System.currentTimeMillis());
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"'IMG'_yyyyMMdd_HHmmss");
		return dateFormat.format(date) + ".jpg";
	}

	/**
	 * Constructs an intent for capturing a photo and storing it in a temporary
	 * file.
	 */
	public static Intent getTakePickIntent(File f) {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE, null);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
		return intent;
	}

	/**
	 * Constructs an intent for picking a photo from Gallery, cropping it and
	 * returning the bitmap.
	 */
	public Intent getPhotoPickIntent() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
		intent.setType("image/*");
		intent.putExtra("crop", "true");
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		// intent.putExtra("outputX", mPhotoPickSize);
		// intent.putExtra("outputY", mPhotoPickSize);
		intent.putExtra("return-data", true);

		// The following lines are provided and maintained by Mediatek inc.
		intent.putExtra(KEY_SCALE_UP_IF_NEEDED, true);
		// The following lines are provided and maintained by Mediatek inc.

		return intent;
	}

	private static final float MAX_HEIGHT = 120F;

	public static final Bitmap scaleImg(Bitmap bitmap) {
		int srcWidth = bitmap.getWidth();
		int srcHeight = bitmap.getHeight();
		float scale = MAX_HEIGHT / srcHeight;
		Matrix matrix = new Matrix();
		matrix.postScale(scale, scale);
		return Bitmap.createBitmap(bitmap, 0, 0, srcWidth, srcHeight, matrix,
				false);
	}
	// take picture or pick a picture code end
}
