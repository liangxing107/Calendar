package com.android.calendar;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class DisplaySetting extends Activity implements OnClickListener,
		OnCheckedChangeListener {

	// protected static interface CHECKBOX_ARRAY {
	// public static final int SELECT_ALL = 0;
	// public static final int MY_CALENDAR = 1;
	// public static final int MY_TASK = 2;
	// public static final int DISPLAY_BIRTHDAY = 3;
	// public static final int CONTACTS_BIRTHDAY = 4;
	// }

	private View mLayout_selectAllRow = null;
	private View mLayout_myCalendarRow = null;
	private View mLayout_myTaskRow = null;
	private View mlayout_diaplayBirthdayRow = null;
	private View mLayout_contactsBirthdayRow = null;

	private ImageView mBtn_back_out = null;
	private ImageView mBtn_back_out_icon = null;
	private TextView mBtn_cancel = null;
	private TextView mBtn_ok = null;
	private Button mBtn_add_account = null;
	private CheckBox mCheckbox_checkAll = null;
	private CheckBox mCheckbox_myCalendar = null;
	private CheckBox mCheckbox_myTask = null;
	private CheckBox mCheckbox_contactBrithday = null;

	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		setContentView(R.layout.display_setting);

		mLayout_selectAllRow = findViewById(R.id.layout_selectAll_row);
		mLayout_myCalendarRow = findViewById(R.id.my_calendar_row);
		mLayout_myTaskRow = findViewById(R.id.my_task_row);
		mlayout_diaplayBirthdayRow = findViewById(R.id.layout_display_birthday);
		mLayout_contactsBirthdayRow = findViewById(R.id.contacts_birthday_row);

		mBtn_back_out = (ImageView) findViewById(R.id.imgBtn_backToMain);
		mBtn_back_out_icon = (ImageView) findViewById(R.id.img_splaner_noitems);
		mBtn_ok = (TextView) findViewById(R.id.btn_cancal);
		mBtn_cancel = (TextView) findViewById(R.id.btn_save);
		mBtn_add_account = (Button) findViewById(R.id.btn_add_account);
		mCheckbox_checkAll = (CheckBox) findViewById(R.id.checkBox_selectAll);
		mCheckbox_myCalendar = (CheckBox) findViewById(R.id.checkBox_my_calendar);
		mCheckbox_myTask = (CheckBox) findViewById(R.id.checkBox_my_task);
		mCheckbox_contactBrithday = (CheckBox) findViewById(R.id.checkBox_contacts_birthday);

		mBtn_back_out.setOnClickListener(this);
		mBtn_back_out_icon.setOnClickListener(this);
		mBtn_ok.setOnClickListener(this);
		mBtn_cancel.setOnClickListener(this);
		mBtn_add_account.setOnClickListener(this);

		mCheckbox_checkAll.setOnCheckedChangeListener(this);
		mCheckbox_myCalendar.setOnCheckedChangeListener(this);
		mCheckbox_myTask.setOnCheckedChangeListener(this);
		mCheckbox_contactBrithday.setOnCheckedChangeListener(this);

		mlayout_diaplayBirthdayRow.setOnClickListener(this);
		mLayout_selectAllRow.setOnClickListener(this);
		mLayout_myCalendarRow.setOnClickListener(this);
		mLayout_myTaskRow.setOnClickListener(this);
		mLayout_contactsBirthdayRow.setOnClickListener(this);
	}

	@Override
	public void onCheckedChanged(CompoundButton checkBox, boolean checked) {
		// TODO Auto-generated method stub
		switch (checkBox.getId()) {
		case R.id.checkBox_selectAll:
			break;
		case R.id.checkBox_my_calendar:
			mLayout_contactsBirthdayRow.setEnabled(checked);
			mCheckbox_contactBrithday.setEnabled(checked);
			break;
		case R.id.checkBox_my_task:
			break;
		case R.id.checkBox_contacts_birthday:
			break;
		}
	}

	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub
		switch (view.getId()) {
		case R.id.btn_save:
		case R.id.imgBtn_backToMain:
		case R.id.img_splaner_noitems:
			finish();
			break;
		case R.id.btn_cancal:
			finish();
			break;
		case R.id.btn_add_account:
			Intent intent = new Intent();
			//intent.setClassName("com.android.settings", "com.android.settings.Settings$ManageAccountsSettingsActivity");
            intent.setClassName("com.android.settings", "com.android.settings.accounts.AddAccountSettings");
			startActivity(intent);
			return;
		case R.id.layout_selectAll_row:
			if (mCheckbox_checkAll.isChecked()
					&& mCheckbox_myCalendar.isChecked()
					&& mCheckbox_myTask.isChecked()
					&& mCheckbox_contactBrithday.isChecked()) {
				mCheckbox_checkAll.setChecked(false);
				mCheckbox_myCalendar.setChecked(false);
				mCheckbox_myTask.setChecked(false);
				mCheckbox_contactBrithday.setChecked(false);
			} else {
				mCheckbox_checkAll.setChecked(true);
				mCheckbox_myCalendar.setChecked(true);
				mCheckbox_myTask.setChecked(true);
				mCheckbox_contactBrithday.setChecked(true);
			}
			break;
		case R.id.my_calendar_row:
			mCheckbox_myCalendar.setChecked(!mCheckbox_myCalendar.isChecked());
			break;
		case R.id.my_task_row:
			mCheckbox_myTask.setChecked(!mCheckbox_myTask.isChecked());
			break;
		case R.id.layout_display_birthday:
			ImageView imageView = (ImageView)findViewById(R.id.image_display_birthday);
			if (mLayout_contactsBirthdayRow.getVisibility() == View.GONE) {
				imageView.setImageResource(R.drawable.expandable_open);
				mLayout_contactsBirthdayRow.setVisibility(View.VISIBLE);
			} else {
				imageView.setImageResource(R.drawable.expandable_close);
				mLayout_contactsBirthdayRow.setVisibility(View.GONE);
			}
			break;
		case R.id.contacts_birthday_row:
			mCheckbox_contactBrithday.setChecked(!mCheckbox_contactBrithday
					.isChecked());
			break;
		}
	}

}
