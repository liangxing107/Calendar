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

import com.android.calendar.CalendarController.EventInfo;
import com.android.calendar.CalendarController.EventStyle;
import com.android.calendar.CalendarEventModel;
import com.android.calendar.CalendarEventModel.Attendee;
import com.android.calendar.CalendarEventModel.ReminderEntry;
import com.android.calendar.EmailAddressAdapter;
import com.android.calendar.EventInfoFragment;
import com.android.calendar.GeneralPreferences;
import com.android.calendar.LogUtil;
import com.android.calendar.SelectMapActivity;
import com.android.calendar.lunar.LunarUtil;
import com.android.calendar.R;
import com.android.calendar.RecipientAdapter;
import com.android.calendar.TimezoneAdapter;
import com.android.calendar.TimezoneAdapter.TimezoneRow;
import com.android.calendar.Utils;
import com.android.calendar.event.EditEventHelper.EditDoneRunnable;
import com.android.calendar.lunar.LunarDatePicker;
import com.android.calendar.lunar.LunarDatePickerDialog;
import com.android.calendarcommon2.EventRecurrence;
import com.android.common.Rfc822InputFilter;
import com.android.common.Rfc822Validator;
import com.android.ex.chips.AccountSpecifier;
import com.android.ex.chips.BaseRecipientAdapter;
import com.android.ex.chips.ChipsUtil;
import com.android.ex.chips.RecipientEditTextView;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.ProgressDialog;
import android.app.Service;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.provider.CalendarContract.Attendees;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract.Reminders;
import android.provider.Settings;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.text.util.Rfc822Token;
import android.text.util.Rfc822Tokenizer;
import android.util.Log;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.EditText;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.MultiAutoCompleteTextView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ResourceCursorAdapter;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.TimeZone;

public class EditEventView implements View.OnClickListener, OnCancelListener,
		OnClickListener, OnItemSelectedListener {
	private static final String TAG = "EditEvent";
	private static final String GOOGLE_SECONDARY_CALENDAR = "calendar.google.com";
	private static final String PERIOD_SPACE = ". ";
	// /M:Max Input length @{
	private static final int TITLE_MAX_LENGTH = 2000;
	private static final int LOCATION_MAX_LENGTH = 2000;
	private static final int DESCRIPTION_MAX_LENGTH = 10000;
	// /@}
	// /M:#Lunar#Year Range @{
	private static final int END_YEAR = 2036;
	private static final int BEGIN_YEAR = 1970;
	// /@}
	protected static final int ITEM_INDEX_TAKE_PHOTO = 0;
	protected static final int ITEM_INDEX_PICK_PHOTO = 1;
	ArrayList<View> mEditOnlyList = new ArrayList<View>();
	ArrayList<View> mEditViewList = new ArrayList<View>();
	ArrayList<View> mViewOnlyList = new ArrayList<View>();
	TextView mLoadingMessage;
	ScrollView mScrollView;
	Button mStartDateButton;
	Button mEndDateButton;
	Button mStartTimeButton;
	Button mEndTimeButton;
	Button mTimezoneButton;
	View mTimezoneRow;
	TextView mStartTimeHome;
	TextView mStartDateHome;
	TextView mEndTimeHome;
	TextView mEndDateHome;
	CheckBox mAllDayCheckBox;
	Spinner mCalendarsSpinner;
	Spinner mRepeatsSpinner;
	Spinner mAvailabilitySpinner;
	Spinner mAccessLevelSpinner;
	RadioGroup mResponseRadioGroup;
	EditText mEt_Title;
	TextView mLocationTextView;
	TextView mDescriptionTextView;
	TextView mWhenView;
	TextView mTimezoneTextView;
	TextView mTimezoneLabel;
	LinearLayout mRemindersContainer;
	MultiAutoCompleteTextView mAttendeesList;
	View mCalendarSelectorGroup;
	View mCalendarSelectorWrapper;
	View mCalendarStaticGroup;
	View mLocationGroup;
	View mDescriptionGroup;
	View mRemindersGroup;
	View mResponseGroup;
	View mOrganizerGroup;
	View mAttendeesGroup;
	View mStartHomeGroup;
	View mEndHomeGroup;

	// private int[] mOriginalSpinnerPadding = new int[4];
	private int[] mOriginalPadding = new int[4];

	private boolean mIsMultipane;
	private ProgressDialog mLoadingCalendarsDialog;
	private AlertDialog mNoCalendarsDialog;
	private AlertDialog mTimezoneDialog;
	private Activity mActivity;
	private EditDoneRunnable mDone;
	private View mView;
	private CalendarEventModel mModel;
	private Cursor mCalendarsCursor;
	private AccountSpecifier mAddressAdapter;
	private Rfc822Validator mEmailValidator;
	private TimezoneAdapter mTimezoneAdapter;

	private ArrayList<Integer> mRecurrenceIndexes = new ArrayList<Integer>(0);

	/**
	 * Contents of the "minutes" spinner. This has default values from the XML
	 * file, augmented with any additional values that were already associated
	 * with the event.
	 */
	private ArrayList<Integer> mReminderMinuteValues;
	private ArrayList<String> mReminderMinuteLabels;

	/**
	 * Contents of the "methods" spinner. The "values" list specifies the method
	 * constant (e.g. {@link Reminders#METHOD_ALERT}) associated with the
	 * labels. Any methods that aren't allowed by the Calendar will be removed.
	 */
	private ArrayList<Integer> mReminderMethodValues;
	private ArrayList<String> mReminderMethodLabels;

	/**
	 * Contents of the "availability" spinner. The "values" list specifies the
	 * type constant (e.g. {@link Events#AVAILABILITY_BUSY}) associated with the
	 * labels. Any types that aren't allowed by the Calendar will be removed.
	 */
	private ArrayList<Integer> mAvailabilityValues;
	private ArrayList<String> mAvailabilityLabels;

	private int mDefaultReminderMinutes;

	private boolean mSaveAfterQueryComplete = false;

	private Time mStartTime;
	private Time mEndTime;
	private String mTimezone;
	private boolean mAllDay = false;
	private int mModification = EditEventHelper.MODIFY_UNINITIALIZED;

	private EventRecurrence mEventRecurrence = new EventRecurrence();

	private ArrayList<LinearLayout> mReminderItems = new ArrayList<LinearLayout>(
			0);
	private ArrayList<LinearLayout> mPictureItems = new ArrayList<LinearLayout>(
			0);
	private ArrayList<ReminderEntry> mUnsupportedReminders = new ArrayList<ReminderEntry>();

	private static StringBuilder mSB = new StringBuilder(50);
	private static Formatter mF = new Formatter(mSB, Locale.getDefault());

	private DialogManager mDialogManager = new DialogManager();

	// /M:#Lunar# add for lunar calendar. @{
	public boolean mIsUseLunarDatePicker = false;
	// /@}
	// /M:@{
	private Toast mToast = null;
	// /@}

	// chenzhentao 2012-7-14 add start
	private static final int INTENT_REQUEST_MAP_SELECT = 1001;
	private EventInfo mEventInfo = null;
	private View mCalendar_selector_group2 = null;
	private View mCalendar_repeat_group = null;
	private View mTask_reminds_row = null;
	private View mTask_priority_row = null;
	private View mTask_group_row = null;
	private TextView mTv_TaskRemindsValue = null;
	private TextView mTv_TaskPriorityValue = null;
	private TextView mTv_TaskGroupValue = null;
	private TextView mTv_Calendar_repeat_value = null;
	private ImageButton mImgBtn_memorandumAdd = null;
	private ImageButton mImgBtn_pictureAdd = null;
	private ImageButton mImgBtn_map = null;
	private EditText mEt_local = null;
	protected Button mBtn_saveDone = null;
	protected Button mBtn_saveCancle = null;
	protected Button mBtn_taskEndTime = null;
	private LinearLayout mLinearLayout_end_date_row = null;
	protected CheckBox mCheckbox_taskNoEndTime = null;
	protected LinearLayout mPictureContainer = null;

	private static final int CALENDAR_MAX_PICTURE = 5;

	// chenzhentao 2012-7-14 add end

	/* This class is used to update the time buttons. */
	private class TimeListener implements OnTimeSetListener {
		private View mView;

		public TimeListener(View view) {
			mView = view;
		}

		@Override
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			// Cache the member variables locally to avoid inner class overhead.
			Time startTime = mStartTime;
			Time endTime = mEndTime;

			// Cache the start and end millis so that we limit the number
			// of calls to normalize() and toMillis(), which are fairly
			// expensive.
			long startMillis;
			long endMillis;
			if (mView == mStartTimeButton) {
				// The start time was changed.
				int hourDuration = endTime.hour - startTime.hour;
				int minuteDuration = endTime.minute - startTime.minute;

				startTime.hour = hourOfDay;
				startTime.minute = minute;
				startMillis = startTime.normalize(true);

				// Also update the end time to keep the duration constant.
				endTime.hour = hourOfDay + hourDuration;
				endTime.minute = minute + minuteDuration;
			} else {
				// The end time was changed.
				startMillis = startTime.toMillis(true);
				endTime.hour = hourOfDay;
				endTime.minute = minute;

				// Move to the start time if the end time is before the start
				// time.
				if (endTime.before(startTime)) {
					endTime.monthDay = startTime.monthDay + 1;
				}
			}

			endMillis = endTime.normalize(true);

			setDate(mEndDateButton, endMillis);
			setTime(mStartTimeButton, startMillis);
			setTime(mEndTimeButton, endMillis);
			updateHomeTime();
		}
	}

	private class TimeClickListener implements View.OnClickListener {
		private Time mTime;

		public TimeClickListener(Time time) {
			mTime = time;
		}

		@Override
		public void onClick(View v) {
			// /M: Multi-dialog issue @{
			if (mDialogManager.isAnyDialogShown()) {
				LogUtil.d(TAG,
						"There is a dialog shown, abort creating dialog: " + v);
				return;
			}
			// / @}
			TimePickerDialog tp = new TimePickerDialog(mActivity,
					new TimeListener(v), mTime.hour, mTime.minute,
					DateFormat.is24HourFormat(mActivity));
			// /M: Multi-dialog issue
			tp.setOnDismissListener(mDialogManager);
			tp.setCanceledOnTouchOutside(true);
			tp.show();
			// /M: Multi-dialog issue
			mDialogManager.dialogShown();
		}
	}

	private class DateListener implements OnDateSetListener {
		View mView;

		public DateListener(View view) {
			mView = view;
		}

		@Override
		public void onDateSet(DatePicker view, int year, int month, int monthDay) {
			Log.d(TAG, "onDateSet: " + year + " " + month + " " + monthDay);
			// Cache the member variables locally to avoid inner class overhead.
			Time startTime = mStartTime;
			Time endTime = mEndTime;

			// Cache the start and end millis so that we limit the number
			// of calls to normalize() and toMillis(), which are fairly
			// expensive.
			long startMillis;
			long endMillis;
			if (mView == mStartDateButton) {
				// The start date was changed.
				int yearDuration = endTime.year - startTime.year;
				int monthDuration = endTime.month - startTime.month;
				int monthDayDuration = endTime.monthDay - startTime.monthDay;

				startTime.year = year;
				startTime.month = month;
				startTime.monthDay = monthDay;
				startMillis = startTime.normalize(true);

				// Also update the end date to keep the duration constant.
				endTime.year = year + yearDuration;
				endTime.month = month + monthDuration;
				endTime.monthDay = monthDay + monthDayDuration;
				endMillis = endTime.normalize(true);

				// If the start date has changed then update the repeats.
				populateRepeats();
			} else {
				// The end date was changed.
				startMillis = startTime.toMillis(true);
				endTime.year = year;
				endTime.month = month;
				endTime.monthDay = monthDay;
				endMillis = endTime.normalize(true);

				// Do not allow an event to have an end time before the start
				// time.
				if (endTime.before(startTime)) {
					endTime.set(startTime);
					endMillis = startMillis;
				}
			}

			setDate(mStartDateButton, startMillis);
			setDate(mEndDateButton, endMillis);
			setTime(mEndTimeButton, endMillis); // In case end time had to be
			// reset
			updateHomeTime();
		}
	}

	// /M:#Lunar#
	private class LunarDateListener implements
			LunarDatePickerDialog.OnDateSetListener {
		View mView;

		public LunarDateListener(View view) {
			mView = view;
		}

		@Override
		public void onDateSet(LunarDatePicker view, int year, int month,
				int monthDay) {
			Log.d(TAG, "onDateSet: " + year + " " + month + " " + monthDay);
			// Cache the member variables locally to avoid inner class overhead.
			Time startTime = mStartTime;
			Time endTime = mEndTime;

			// Cache the start and end millis so that we limit the number
			// of calls to normalize() and toMillis(), which are fairly
			// expensive.
			long startMillis;
			long endMillis;
			if (mView == mStartDateButton) {
				// The start date was changed.
				int yearDuration = endTime.year - startTime.year;
				int monthDuration = endTime.month - startTime.month;
				int monthDayDuration = endTime.monthDay - startTime.monthDay;

				startTime.year = year;
				startTime.month = month;
				startTime.monthDay = monthDay;
				startMillis = startTime.normalize(true);

				// Also update the end date to keep the duration constant.
				endTime.year = year + yearDuration;
				endTime.month = month + monthDuration;
				endTime.monthDay = monthDay + monthDayDuration;
				endMillis = endTime.normalize(true);

				// If the start date has changed then update the repeats.
				populateRepeats();
			} else {
				// The end date was changed.
				startMillis = startTime.toMillis(true);
				endTime.year = year;
				endTime.month = month;
				endTime.monthDay = monthDay;
				endMillis = endTime.normalize(true);

				// Do not allow an event to have an end time before the start
				// time.
				if (endTime.before(startTime)) {
					endTime.set(startTime);
					endMillis = startMillis;
				}
			}

			setDate(mStartDateButton, startMillis);
			setDate(mEndDateButton, endMillis);
			setTime(mEndTimeButton, endMillis); // In case end time had to be
			// reset
			updateHomeTime();
		}
	}

	// /@}

	// Fills in the date and time fields
	private void populateWhen() {
		long startMillis = mStartTime.toMillis(false /* use isDst */);
		long endMillis = mEndTime.toMillis(false /* use isDst */);
		setDate(mStartDateButton, startMillis);
		setDate(mEndDateButton, endMillis);

		setTime(mStartTimeButton, startMillis);
		setTime(mEndTimeButton, endMillis);

		mStartDateButton.setOnClickListener(new DateClickListener(mStartTime));
		mEndDateButton.setOnClickListener(new DateClickListener(mEndTime));

		mStartTimeButton.setOnClickListener(new TimeClickListener(mStartTime));
		mEndTimeButton.setOnClickListener(new TimeClickListener(mEndTime));
	}

	private void populateTimezone() {
		mTimezoneButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// /M: multi-dialog issue @{
				if (mDialogManager.isAnyDialogShown()) {
					LogUtil.d(TAG,
							"There is dailog shown, abort showing timezone dialog");
					return;
				}
				// / @}
				showTimezoneDialog();
			}
		});
		setTimezone(mTimezoneAdapter.getRowById(mTimezone));
	}

	private void showTimezoneDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
		final Context alertDialogContext = builder.getContext();
		mTimezoneAdapter = new TimezoneAdapter(alertDialogContext, mTimezone);
		builder.setTitle(R.string.timezone_label);
		builder.setSingleChoiceItems(mTimezoneAdapter,
				mTimezoneAdapter.getRowById(mTimezone), this);
		mTimezoneDialog = builder.create();
		// /M: multi-dialog issue
		mTimezoneDialog.setOnDismissListener(mDialogManager);

		LayoutInflater layoutInflater = (LayoutInflater) alertDialogContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final TextView timezoneFooterView = (TextView) layoutInflater.inflate(
				R.layout.timezone_footer, null);

		timezoneFooterView.setText(mActivity
				.getString(R.string.edit_event_show_all) + " >");
		timezoneFooterView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mTimezoneDialog.getListView().removeFooterView(
						timezoneFooterView);
				mTimezoneAdapter.showAllTimezones();
				final int row = mTimezoneAdapter.getRowById(mTimezone);
				// we need to post the selection changes to have them have
				// any effect
				mTimezoneDialog.getListView().post(new Runnable() {
					@Override
					public void run() {
						mTimezoneDialog.getListView().setItemChecked(row, true);
						mTimezoneDialog.getListView().setSelection(row);
					}
				});
			}
		});
		mTimezoneDialog.getListView().addFooterView(timezoneFooterView);
		mTimezoneDialog.setCanceledOnTouchOutside(true);
		mTimezoneDialog.show();
		// /M: multi-dialog issue
		mDialogManager.dialogShown();
	}

	private void populateRepeats() {
		Time time = mStartTime;
		Resources r = mActivity.getResources();

		String[] days = new String[] {
				DateUtils.getDayOfWeekString(Calendar.SUNDAY,
						DateUtils.LENGTH_MEDIUM),
				DateUtils.getDayOfWeekString(Calendar.MONDAY,
						DateUtils.LENGTH_MEDIUM),
				DateUtils.getDayOfWeekString(Calendar.TUESDAY,
						DateUtils.LENGTH_MEDIUM),
				DateUtils.getDayOfWeekString(Calendar.WEDNESDAY,
						DateUtils.LENGTH_MEDIUM),
				DateUtils.getDayOfWeekString(Calendar.THURSDAY,
						DateUtils.LENGTH_MEDIUM),
				DateUtils.getDayOfWeekString(Calendar.FRIDAY,
						DateUtils.LENGTH_MEDIUM),
				DateUtils.getDayOfWeekString(Calendar.SATURDAY,
						DateUtils.LENGTH_MEDIUM), };
		String[] ordinals = r.getStringArray(R.array.ordinal_labels);

		// Only display "Custom" in the spinner if the device does not support
		// the recurrence functionality of the event. Only display every weekday
		// if the event starts on a weekday.
		boolean isCustomRecurrence = isCustomRecurrence();
		boolean isWeekdayEvent = isWeekdayEvent();

		ArrayList<String> repeatArray = new ArrayList<String>(0);
		ArrayList<Integer> recurrenceIndexes = new ArrayList<Integer>(0);

		repeatArray.add(r.getString(R.string.does_not_repeat));
		recurrenceIndexes.add(EditEventHelper.DOES_NOT_REPEAT);

		repeatArray.add(r.getString(R.string.daily));
		recurrenceIndexes.add(EditEventHelper.REPEATS_DAILY);

		if (isWeekdayEvent) {
			repeatArray.add(r.getString(R.string.every_weekday));
			recurrenceIndexes.add(EditEventHelper.REPEATS_EVERY_WEEKDAY);
		}

		String format = r.getString(R.string.weekly);
		repeatArray.add(String.format(format, time.format("%A")));
		recurrenceIndexes.add(EditEventHelper.REPEATS_WEEKLY_ON_DAY);

		// Calculate whether this is the 1st, 2nd, 3rd, 4th, or last appearance
		// of the given day.
		int dayNumber = (time.monthDay - 1) / 7;
		format = r.getString(R.string.monthly_on_day_count);
		repeatArray.add(String.format(format, ordinals[dayNumber],
				days[time.weekDay]));
		recurrenceIndexes.add(EditEventHelper.REPEATS_MONTHLY_ON_DAY_COUNT);

		format = r.getString(R.string.monthly_on_day);
		repeatArray.add(String.format(format, time.monthDay));
		recurrenceIndexes.add(EditEventHelper.REPEATS_MONTHLY_ON_DAY);

		long when = time.toMillis(false);
		format = r.getString(R.string.yearly);
		int flags = 0;
		if (DateFormat.is24HourFormat(mActivity)) {
			flags |= DateUtils.FORMAT_24HOUR;
		}
		repeatArray.add(String.format(format,
				DateUtils.formatDateTime(mActivity, when, flags)));
		recurrenceIndexes.add(EditEventHelper.REPEATS_YEARLY);

		if (isCustomRecurrence) {
			repeatArray.add(r.getString(R.string.custom));
			recurrenceIndexes.add(EditEventHelper.REPEATS_CUSTOM);
		}
		mRecurrenceIndexes = recurrenceIndexes;

		int position = recurrenceIndexes
				.indexOf(EditEventHelper.DOES_NOT_REPEAT);
		if (!TextUtils.isEmpty(mModel.mRrule)) {
			if (isCustomRecurrence) {
				position = recurrenceIndexes
						.indexOf(EditEventHelper.REPEATS_CUSTOM);
			} else {
				switch (mEventRecurrence.freq) {
				case EventRecurrence.DAILY:
					position = recurrenceIndexes
							.indexOf(EditEventHelper.REPEATS_DAILY);
					break;
				case EventRecurrence.WEEKLY:
					if (mEventRecurrence.repeatsOnEveryWeekDay()) {
						position = recurrenceIndexes
								.indexOf(EditEventHelper.REPEATS_EVERY_WEEKDAY);
					} else {
						position = recurrenceIndexes
								.indexOf(EditEventHelper.REPEATS_WEEKLY_ON_DAY);
					}
					break;
				case EventRecurrence.MONTHLY:
					if (mEventRecurrence.repeatsMonthlyOnDayCount()) {
						position = recurrenceIndexes
								.indexOf(EditEventHelper.REPEATS_MONTHLY_ON_DAY_COUNT);
					} else {
						position = recurrenceIndexes
								.indexOf(EditEventHelper.REPEATS_MONTHLY_ON_DAY);
					}
					break;
				case EventRecurrence.YEARLY:
					position = recurrenceIndexes
							.indexOf(EditEventHelper.REPEATS_YEARLY);
					break;
				}
			}
		}
		//ArrayAdapter<String> adapter = new ArrayAdapter<String>(mActivity,
				// android.R.layout.simple_spinner_item, repeatArray);
		// adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// mRepeatsSpinner.setAdapter(adapter);
		// mRepeatsSpinner.setSelection(position);

		// chenzhentao add 2012-9-17
		mCalendar_repeat_group.setTag(repeatArray);
		int pos = 0;
		mTv_Calendar_repeat_value.setText(repeatArray.get(pos));
		mTv_Calendar_repeat_value.setTag(pos);

		// Don't allow the user to make exceptions recurring events.
		if (mModel.mOriginalSyncId != null) {
			// mRepeatsSpinner.setEnabled(false);
		}
	}

	private boolean isCustomRecurrence() {

		if (mEventRecurrence.until != null
				|| (mEventRecurrence.interval != 0 && mEventRecurrence.interval != 1)
				|| mEventRecurrence.count != 0) {
			return true;
		}

		if (mEventRecurrence.freq == 0) {
			return false;
		}

		switch (mEventRecurrence.freq) {
		case EventRecurrence.DAILY:
			return false;
		case EventRecurrence.WEEKLY:
			if (mEventRecurrence.repeatsOnEveryWeekDay() && isWeekdayEvent()) {
				return false;
			} else if (mEventRecurrence.bydayCount == 1) {
				return false;
			}
			break;
		case EventRecurrence.MONTHLY:
			if (mEventRecurrence.repeatsMonthlyOnDayCount()) {
				/* this is a "3rd Tuesday of every month" sort of rule */
				return false;
			} else if (mEventRecurrence.bydayCount == 0
					&& mEventRecurrence.bymonthdayCount == 1
					&& mEventRecurrence.bymonthday[0] > 0) {
				/* this is a "22nd day of every month" sort of rule */
				return false;
			}
			break;
		case EventRecurrence.YEARLY:
			return false;
		}

		return true;
	}

	private boolean isWeekdayEvent() {
		if (mStartTime.weekDay != Time.SUNDAY
				&& mStartTime.weekDay != Time.SATURDAY) {
			return true;
		}
		return false;
	}

	private class DateClickListener implements View.OnClickListener {
		private Time mTime;

		public DateClickListener(Time time) {
			mTime = time;
		}

		public void onClick(View v) {
			// /M: if dialog is shown no need to show dialog any more @{
			if (mDialogManager.isAnyDialogShown()) {
				LogUtil.d(TAG,
						"There is a dialog shown, abort creating dialog: " + v);
				return;
			}
			// /@}
			// /M:#Lunar# modify for lunar calendar.@{
			Dialog dpd = null;
			CalendarView cv;
			mIsUseLunarDatePicker = isLunarDataPickerClicked();
			if (mIsUseLunarDatePicker) {
				dpd = (Dialog) new LunarDatePickerDialog(mActivity,
						new LunarDateListener(v), mTime.year, mTime.month,
						mTime.monthDay);
				cv = ((LunarDatePickerDialog) dpd).getDatePicker()
						.getCalendarView();
				// used to test.
				LogUtil.w(TAG, "use lunar calendar date picker!!");
			} else {
				dpd = new DatePickerDialog(mActivity, new DateListener(v),
						mTime.year, mTime.month, mTime.monthDay);
				cv = ((DatePickerDialog) dpd).getDatePicker().getCalendarView();
				// M: for the date range@{
				Time maxDate = new Time();
				// The maxDate and minDate in DatePicker, is in millis rather
				// than in day.
				// So, we need to set the max border in 2036.12.31.59.59.999,
				// which is 1 millis
				// before 2037.1.1.0.0.0.000
				maxDate.set(0, 0, 0, 1, 0, END_YEAR + 1);
				Time minDate = new Time();
				minDate.set(1, 0, BEGIN_YEAR);
				// The maxDate and minDate in DatePicker, is in millis rather
				// than in day.
				// So, we need to set the max border in 2036.12.31.59.59.999,
				// which is 1 millis
				// before 2037.1.1.0.0.0.000
				// TODO: this solution should not be a final one.
				((DatePickerDialog) dpd).getDatePicker().setMaxDate(
						maxDate.toMillis(false) - 1);
				((DatePickerDialog) dpd).getDatePicker().setMinDate(
						minDate.toMillis(false));
				// /:@}
			}
			// /:@}

			cv.setShowWeekNumber(Utils.getShowWeekNumber(mActivity));
			// /M: multi-dialog issue
			dpd.setOnDismissListener(mDialogManager);

			int startOfWeek = Utils.getFirstDayOfWeek(mActivity);
			// Utils returns Time days while CalendarView wants Calendar days
			if (startOfWeek == Time.SATURDAY) {
				startOfWeek = Calendar.SATURDAY;
			} else if (startOfWeek == Time.SUNDAY) {
				startOfWeek = Calendar.SUNDAY;
			} else {
				startOfWeek = Calendar.MONDAY;
			}
			cv.setFirstDayOfWeek(startOfWeek);
			dpd.setCanceledOnTouchOutside(true);
			dpd.show();
			// /M: multi-dialog issue
			mDialogManager.dialogShown();
		}
	}

	static private class CalendarsAdapter extends ResourceCursorAdapter {
		public CalendarsAdapter(Context context, Cursor c) {
			super(context, R.layout.calendars_item, c);
			setDropDownViewResource(R.layout.calendars_dropdown_item);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			View colorBar = view.findViewById(R.id.color);
			int colorColumn = cursor
					.getColumnIndexOrThrow(Calendars.CALENDAR_COLOR);
			int nameColumn = cursor
					.getColumnIndexOrThrow(Calendars.CALENDAR_DISPLAY_NAME);
			int ownerColumn = cursor
					.getColumnIndexOrThrow(Calendars.OWNER_ACCOUNT);
			if (colorBar != null) {
				colorBar.setBackgroundColor(Utils
						.getDisplayColorFromColor(cursor.getInt(colorColumn)));
			}

			TextView name = (TextView) view.findViewById(R.id.calendar_name);
			if (name != null) {
				String displayName = cursor.getString(nameColumn);
				name.setText(displayName);

				TextView accountName = (TextView) view
						.findViewById(R.id.account_name);
				if (accountName != null) {
					accountName.setText(cursor.getString(ownerColumn));
					accountName.setVisibility(TextView.VISIBLE);
				}
			}
		}
	}

	/**
	 * Does prep steps for saving a calendar event.
	 * 
	 * This triggers a parse of the attendees list and checks if the event is
	 * ready to be saved. An event is ready to be saved so long as a model
	 * exists and has a calendar it can be associated with, either because it's
	 * an existing event or we've finished querying.
	 * 
	 * @return false if there is no model or no calendar had been loaded yet,
	 *         true otherwise.
	 */
	public boolean prepareForSave() {
		if (mModel == null || (mCalendarsCursor == null && mModel.mUri == null)) {
			return false;
		}
		return fillModelFromUI();
	}

	public boolean fillModelFromReadOnlyUi() {
		if (mModel == null || (mCalendarsCursor == null && mModel.mUri == null)) {
			return false;
		}
		mModel.mReminders = EventViewUtils.reminderItemsToReminders(
				mReminderItems, mReminderMinuteValues, mReminderMethodValues);
		mModel.mReminders.addAll(mUnsupportedReminders);
		mModel.normalizeReminders();
		int status = EventInfoFragment
				.getResponseFromButtonId(mResponseRadioGroup
						.getCheckedRadioButtonId());
		if (status != Attendees.ATTENDEE_STATUS_NONE) {
			mModel.mSelfAttendeeStatus = status;
		}
		return true;
	}

	// This is called if the user clicks on one of the buttons: "Save",
	// "Discard", or "Delete". This is also called if the user clicks
	// on the "remove reminder" button.
	@Override
	public void onClick(View view) {
		int pos = 0;
		final Resources r = mActivity.getResources();
		switch (view.getId()) {
		case R.id.reminder_remove:
			// This must be a click on one of the "remove reminder" buttons
			LinearLayout reminderItem = (LinearLayout) view.getParent();
			LinearLayout parent = (LinearLayout) reminderItem.getParent();
			parent.removeView(reminderItem);
			mReminderItems.remove(reminderItem);
			updateRemindersVisibility(mReminderItems.size());
			break;
		case R.id.picture_remove:
			LinearLayout pictureItem = (LinearLayout) view.getParent();
			LinearLayout pictureItemParent = (LinearLayout) pictureItem
					.getParent();
			pictureItemParent.removeView(pictureItem);
			mPictureItems.remove(pictureItem);
			updatePictureVisibility(mPictureItems.size());
			break;
		case R.id.calendar_selector_group2:
			pos = findDefaultCalendarPosition(mCalendarsCursor);
			int titleResId = mEventInfo.eventStyle == EventStyle.EVNET_STYLE ? R.string.calendar
					: R.string.task;
			new AlertDialog.Builder(mActivity)
					.setTitle(titleResId)
					.setNegativeButton(R.string.cancel, null)
					.setSingleChoiceItems(mCalendarsCursor, pos,
							Calendars.CALENDAR_DISPLAY_NAME,
							new OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int position) {
									// TODO Auto-generated method stub
									changeCalendarText(position);
									dialog.dismiss();
								}
							}).create().show();
			break;
		case R.id.calendar_repeat_layout:
			ArrayList<String> repeatList = (ArrayList<String>) mCalendar_repeat_group.getTag();
			final String[] repeatArray = parseListToArray(repeatList);
			int repeatPos = (Integer) mTv_Calendar_repeat_value.getTag();
			new AlertDialog.Builder(mActivity)
					.setTitle(R.string.repeats_label)
					.setNegativeButton(R.string.cancel, null)
					.setSingleChoiceItems(repeatArray, repeatPos,
							new OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int position) {
									// TODO Auto-generated method stub
									int newPos = position;
									mTv_Calendar_repeat_value
											.setText(repeatArray[newPos]);
									mTv_Calendar_repeat_value.setTag(newPos);
									dialog.dismiss();
								}
							}).create().show();
			break;
		case R.id.task_reminds_row:
			final String[] remindArray = r
					.getStringArray(R.array.task_remind_lable_arrays);
			pos = findIndexByText(remindArray, mTv_TaskRemindsValue.getText()
					.toString());
			new AlertDialog.Builder(mActivity)
					.setTitle(R.string.event_info_reminders_label)
					.setNegativeButton(R.string.cancel, null)
					.setSingleChoiceItems(remindArray, pos,
							new OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int position) {
									// TODO Auto-generated method stub
									mTv_TaskRemindsValue
											.setText(remindArray[position]);
									dialog.dismiss();
								}
							}).create().show();
			break;
		case R.id.task_priority_row:
			final String[] priorityArray = r
					.getStringArray(R.array.task_priority_lable_arrays);
			pos = findIndexByText(priorityArray, mTv_TaskPriorityValue
					.getText().toString());
			new AlertDialog.Builder(mActivity)
					.setTitle(R.string.task_priority_lable)
					.setNegativeButton(R.string.cancel, null)
					.setSingleChoiceItems(priorityArray, pos,
							new OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int position) {
									// TODO Auto-generated method stub
									mTv_TaskPriorityValue
											.setText(priorityArray[position]);
									dialog.dismiss();
								}
							}).create().show();
			break;
		case R.id.task_group_row:
			final String[] groupArray = r
					.getStringArray(R.array.task_group_lable_arrays);
			pos = findIndexByText(groupArray, mTv_TaskGroupValue.getText()
					.toString());
			new AlertDialog.Builder(mActivity)
					.setTitle(R.string.task_group_lable)
					.setNegativeButton(R.string.cancel, null)
					.setSingleChoiceItems(groupArray, pos,
							new OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int position) {
									// TODO Auto-generated method stub
									mTv_TaskGroupValue
											.setText(groupArray[position]);
									dialog.dismiss();
								}
							}).create().show();
			break;
		case R.id.picture_add:
			new AlertDialog.Builder(mActivity)
					.setTitle(R.string.picture_lable)
					.setNegativeButton(R.string.cancel, null)
					.setSingleChoiceItems(R.array.add_picture_method_arrays,
							pos, new OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int position) {
									// TODO Auto-generated method stub
									EditEventActivity activity = (EditEventActivity) mActivity;
									switch (position) {
									case ITEM_INDEX_PICK_PHOTO:
										activity.onPickFromGalleryChosen();
										break;
									case ITEM_INDEX_TAKE_PHOTO:
										activity.onTakePhotoChosen();
										break;
									}
									dialog.dismiss();
								}
							}).create().show();
			break;
		case R.id.memorandum_add:
			// ToastMsg(R.string.function_not_wellDone);
			break;
		case R.id.imgBtn_local:
			// Intent intent = new Intent(mActivity, SelectMapActivity.class);
			// mActivity.startActivityForResult(intent,
			// INTENT_REQUEST_MAP_SELECT);
			break;
		case R.id.end_date_button:
			Time time = mEventInfo.startTime;
			DatePickerDialog datePickerDialog = new DatePickerDialog(mActivity,
					new OnSetEndDateListener(mBtn_taskEndTime), time.year,
					time.month, time.monthDay);
			CalendarView cv = datePickerDialog.getDatePicker()
					.getCalendarView();
			cv.setShowWeekNumber(Utils.getShowWeekNumber(mActivity));

			int startOfWeek = Utils.getFirstDayOfWeek(mActivity);
			// Utils returns Time days while CalendarView wants Calendar days
			if (startOfWeek == Time.SATURDAY) {
				startOfWeek = Calendar.SATURDAY;
			} else if (startOfWeek == Time.SUNDAY) {
				startOfWeek = Calendar.SUNDAY;
			} else {
				startOfWeek = Calendar.MONDAY;
			}
			cv.setFirstDayOfWeek(startOfWeek);
			datePickerDialog.setCanceledOnTouchOutside(true);
			datePickerDialog.show();

			break;
		}
	}

	// This is called if the user cancels the "No calendars" dialog.
	// The "No calendars" dialog is shown if there are no syncable calendars.
	@Override
	public void onCancel(DialogInterface dialog) {
		if (dialog == mLoadingCalendarsDialog) {
			mLoadingCalendarsDialog = null;
			mSaveAfterQueryComplete = false;
		} else if (dialog == mNoCalendarsDialog) {
			mDone.setDoneCode(Utils.DONE_REVERT);
			mDone.run();
			return;
		}
	}

	// This is called if the user clicks on a dialog button.
	@Override
	public void onClick(DialogInterface dialog, int which) {
		if (dialog == mNoCalendarsDialog) {
			mDone.setDoneCode(Utils.DONE_REVERT);
			mDone.run();
			if (which == DialogInterface.BUTTON_POSITIVE) {
				Intent nextIntent = new Intent(Settings.ACTION_ADD_ACCOUNT);
				final String[] array = { "com.android.calendar" };
				nextIntent.putExtra(Settings.EXTRA_AUTHORITIES, array);
				nextIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
						| Intent.FLAG_ACTIVITY_NEW_TASK);
				mActivity.startActivity(nextIntent);
			}
		} else if (dialog == mTimezoneDialog) {
			if (which >= 0 && which < mTimezoneAdapter.getCount()) {
				setTimezone(which);
				updateHomeTime();
				dialog.dismiss();
			}
		}
	}

	// Goes through the UI elements and updates the model as necessary
	private boolean fillModelFromUI() {
		if (mModel == null) {
			return false;
		}
		mModel.mReminders = EventViewUtils.reminderItemsToReminders(
				mReminderItems, mReminderMinuteValues, mReminderMethodValues);
		mModel.mReminders.addAll(mUnsupportedReminders);
		mModel.normalizeReminders();
		mModel.mHasAlarm = mReminderItems.size() > 0;
		mModel.mTitle = mEt_Title.getText().toString();
		mModel.mAllDay = mAllDayCheckBox.isChecked();
		mModel.mLocation = mLocationTextView.getText().toString();
		mModel.mDescription = mDescriptionTextView.getText().toString();
		if (TextUtils.isEmpty(mModel.mLocation)) {
			mModel.mLocation = null;
		}
		if (TextUtils.isEmpty(mModel.mDescription)) {
			mModel.mDescription = null;
		}

		int status = EventInfoFragment
				.getResponseFromButtonId(mResponseRadioGroup
						.getCheckedRadioButtonId());
		if (status != Attendees.ATTENDEE_STATUS_NONE) {
			mModel.mSelfAttendeeStatus = status;
		}

		if (mAttendeesList != null) {
			mEmailValidator.setRemoveInvalid(true);
			mAttendeesList.performValidation();
			mModel.mAttendeesList.clear();
			// /M:@{
			String address = mAttendeesList.getText().toString();
			if (!TextUtils.isEmpty(address)
					&& isHasInvalidAddress(address, mEmailValidator)) {
				ToastMsg(R.string.attendees_invalid_tip);
			}
			// /@}
			mModel.addAttendees(mAttendeesList.getText().toString(),
					mEmailValidator);
			mEmailValidator.setRemoveInvalid(false);
		}

		// If this was a new event we need to fill in the Calendar information
		if (mModel.mUri == null) {
			mModel.mCalendarId = mCalendarsSpinner.getSelectedItemId();
			int calendarCursorPosition = mCalendarsSpinner
					.getSelectedItemPosition();
			if (mCalendarsCursor.moveToPosition(calendarCursorPosition)) {
				String defaultCalendar = mCalendarsCursor
						.getString(EditEventHelper.CALENDARS_INDEX_OWNER_ACCOUNT);
				Utils.setSharedPreference(mActivity,
						GeneralPreferences.KEY_DEFAULT_CALENDAR,
						defaultCalendar);
				mModel.mOwnerAccount = defaultCalendar;
				mModel.mOrganizer = defaultCalendar;
				mModel.mCalendarId = mCalendarsCursor
						.getLong(EditEventHelper.CALENDARS_INDEX_ID);
			}
		}

		if (mModel.mAllDay) {
			// Reset start and end time, increment the monthDay by 1, and set
			// the timezone to UTC, as required for all-day events.
			mTimezone = Time.TIMEZONE_UTC;
			mStartTime.hour = 0;
			mStartTime.minute = 0;
			mStartTime.second = 0;
			mStartTime.timezone = mTimezone;
			mModel.mStart = mStartTime.normalize(true);

			mEndTime.hour = 0;
			mEndTime.minute = 0;
			mEndTime.second = 0;
			mEndTime.timezone = mTimezone;
			// When a user see the event duration as "X - Y" (e.g. Oct. 28 -
			// Oct. 29), end time
			// should be Y + 1 (Oct.30).
			final long normalizedEndTimeMillis = mEndTime.normalize(true)
					+ DateUtils.DAY_IN_MILLIS;
			if (normalizedEndTimeMillis < mModel.mStart) {
				// mEnd should be midnight of the next day of mStart.
				mModel.mEnd = mModel.mStart + DateUtils.DAY_IN_MILLIS;
			} else {
				mModel.mEnd = normalizedEndTimeMillis;
			}
		} else {
			mStartTime.timezone = mTimezone;
			mEndTime.timezone = mTimezone;
			mModel.mStart = mStartTime.toMillis(true);
			mModel.mEnd = mEndTime.toMillis(true);
		}
		mModel.mTimezone = mTimezone;
		mModel.mAccessLevel = mAccessLevelSpinner.getSelectedItemPosition();
		// TODO set correct availability value
		mModel.mAvailability = mAvailabilityValues.get(mAvailabilitySpinner
				.getSelectedItemPosition());

		int selection;
		// If we're making an exception we don't want it to be a repeating
		// event.
		if (mModification == EditEventHelper.MODIFY_SELECTED) {
			selection = EditEventHelper.DOES_NOT_REPEAT;
		} else {
			// int position = mRepeatsSpinner.getSelectedItemPosition();
			int position = (Integer) mTv_Calendar_repeat_value.getTag();
			selection = mRecurrenceIndexes.get(position);
		}

		EditEventHelper.updateRecurrenceRule(selection, mModel,
				Utils.getFirstDayOfWeek(mActivity) + 1);

		// Save the timezone so we can display it as a standard option next time
		if (!mModel.mAllDay) {
			mTimezoneAdapter.saveRecentTimezone(mTimezone);
		}
		return true;
	}

	public EditEventView(Activity activity, View view, EditDoneRunnable done,
			EventInfo eventInfo) {

		mActivity = activity;
		mView = view;
		mDone = done;
		mEventInfo = eventInfo;
		// cache top level view elements
		mLoadingMessage = (TextView) view.findViewById(R.id.loading_message);
		mScrollView = (ScrollView) view.findViewById(R.id.scroll_view);

		// cache all the widgets
		mCalendarsSpinner = (Spinner) view.findViewById(R.id.calendars_spinner);
		mEt_Title = (EditText) view.findViewById(R.id.title);
		mLocationTextView = (TextView) view.findViewById(R.id.location);
		mDescriptionTextView = (TextView) view.findViewById(R.id.description);
		mTimezoneLabel = (TextView) view.findViewById(R.id.timezone_label);
		mStartDateButton = (Button) view.findViewById(R.id.start_date);
		mEndDateButton = (Button) view.findViewById(R.id.end_date);
		mWhenView = (TextView) mView.findViewById(R.id.when);
		mTimezoneTextView = (TextView) mView
				.findViewById(R.id.timezone_textView);
		mStartTimeButton = (Button) view.findViewById(R.id.start_time);
		mEndTimeButton = (Button) view.findViewById(R.id.end_time);
		mTimezoneButton = (Button) view.findViewById(R.id.timezone_button);
		mTimezoneRow = view.findViewById(R.id.timezone_button_row);
		mStartTimeHome = (TextView) view.findViewById(R.id.start_time_home_tz);
		mStartDateHome = (TextView) view.findViewById(R.id.start_date_home_tz);
		mEndTimeHome = (TextView) view.findViewById(R.id.end_time_home_tz);
		mEndDateHome = (TextView) view.findViewById(R.id.end_date_home_tz);
		mAllDayCheckBox = (CheckBox) view.findViewById(R.id.is_all_day);
		// mRepeatsSpinner = (Spinner) view.findViewById(R.id.repeats);
		mAvailabilitySpinner = (Spinner) view.findViewById(R.id.availability);
		mAccessLevelSpinner = (Spinner) view.findViewById(R.id.visibility);
		mCalendarSelectorGroup = view
				.findViewById(R.id.calendar_selector_group);
		mCalendarSelectorWrapper = view
				.findViewById(R.id.calendar_selector_wrapper);
		mCalendarStaticGroup = view.findViewById(R.id.calendar_group);
		mRemindersGroup = view.findViewById(R.id.reminders_row);
		mResponseGroup = view.findViewById(R.id.response_row);
		mOrganizerGroup = view.findViewById(R.id.organizer_row);
		mAttendeesGroup = view.findViewById(R.id.add_attendees_row);
		mLocationGroup = view.findViewById(R.id.where_row);
		mDescriptionGroup = view.findViewById(R.id.description_row);
		mStartHomeGroup = view.findViewById(R.id.from_row_home_tz);
		mEndHomeGroup = view.findViewById(R.id.to_row_home_tz);
		mAttendeesList = (MultiAutoCompleteTextView) view
				.findViewById(R.id.attendees);

		mEt_Title.setTag(mEt_Title.getBackground());
		mLocationTextView.setTag(mLocationTextView.getBackground());
		mDescriptionTextView.setTag(mDescriptionTextView.getBackground());
		// mRepeatsSpinner.setTag(mRepeatsSpinner.getBackground());
		mAttendeesList.setTag(mAttendeesList.getBackground());
		mOriginalPadding[0] = mLocationTextView.getPaddingLeft();
		mOriginalPadding[1] = mLocationTextView.getPaddingTop();
		mOriginalPadding[2] = mLocationTextView.getPaddingRight();
		mOriginalPadding[3] = mLocationTextView.getPaddingBottom();
		// mOriginalSpinnerPadding[0] = mRepeatsSpinner.getPaddingLeft();
		// mOriginalSpinnerPadding[1] = mRepeatsSpinner.getPaddingTop();
		// mOriginalSpinnerPadding[2] = mRepeatsSpinner.getPaddingRight();
		// mOriginalSpinnerPadding[3] = mRepeatsSpinner.getPaddingBottom();
		mEditViewList.add(mEt_Title);
		mEditViewList.add(mLocationTextView);
		mEditViewList.add(mDescriptionTextView);
		mEditViewList.add(mAttendeesList);

		mViewOnlyList.add(view.findViewById(R.id.when_row));
		mViewOnlyList.add(view.findViewById(R.id.timezone_textview_row));

		mEditOnlyList.add(view.findViewById(R.id.all_day_row));
		// mEditOnlyList.add(view.findViewById(R.id.availability_row));
		// mEditOnlyList.add(view.findViewById(R.id.visibility_row));
		mEditOnlyList.add(view.findViewById(R.id.from_row));
		mEditOnlyList.add(view.findViewById(R.id.to_row));
		mEditOnlyList.add(mTimezoneRow);
		mEditOnlyList.add(mStartHomeGroup);
		mEditOnlyList.add(mEndHomeGroup);

		mResponseRadioGroup = (RadioGroup) view
				.findViewById(R.id.response_value);
		mRemindersContainer = (LinearLayout) view
				.findViewById(R.id.reminder_items_container);

		mTimezone = Utils.getTimeZone(activity, null);
		mIsMultipane = activity.getResources().getBoolean(R.bool.tablet_config);
		mStartTime = new Time(mTimezone);
		mEndTime = new Time(mTimezone);
		mTimezoneAdapter = new TimezoneAdapter(mActivity, mTimezone);
		mEmailValidator = new Rfc822Validator(null);
		initMultiAutoCompleteTextView((RecipientEditTextView) mAttendeesList);

		// /M:add input filter @{
		setLengthInputFilter(mEt_Title, mActivity, TITLE_MAX_LENGTH);
		setLengthInputFilter(mLocationTextView, mActivity, LOCATION_MAX_LENGTH);
		setLengthInputFilter(mDescriptionTextView, mActivity,
				DESCRIPTION_MAX_LENGTH);
		// /@}

		// Display loading screen
		setModel(null);

		// chenzhentao add
		mImgBtn_map = (ImageButton) view.findViewById(R.id.imgBtn_local);
		mImgBtn_map.setOnClickListener(this);
		mEt_local = (EditText) view.findViewById(R.id.location);
		mPictureContainer = (LinearLayout) view
				.findViewById(R.id.picture_items_container);
		mImgBtn_pictureAdd = (ImageButton) view.findViewById(R.id.picture_add);
		mImgBtn_pictureAdd.setOnClickListener(this);
		mImgBtn_memorandumAdd = (ImageButton) view
				.findViewById(R.id.memorandum_add);
		mImgBtn_memorandumAdd.setOnClickListener(this);

		mLinearLayout_end_date_row = (LinearLayout) view
				.findViewById(R.id.end_date_row);
		mBtn_taskEndTime = (Button) view.findViewById(R.id.end_date_button);
		mBtn_taskEndTime.setOnClickListener(this);
		setDate(mBtn_taskEndTime, mEventInfo.startTime.normalize(false));

		mCheckbox_taskNoEndTime = (CheckBox) view.findViewById(R.id.no_end_day);
		mCheckbox_taskNoEndTime
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton arg0,
							boolean isChecked) {
						// TODO Auto-generated method stub
						setTaskEndDayVisible(isChecked);
					}
				});

		mBtn_saveDone = (Button) view.findViewById(R.id.btn_done_savePane);
		mBtn_saveCancle = (Button) view.findViewById(R.id.btn_cancel_savePane);

		mCalendar_selector_group2 = view
				.findViewById(R.id.calendar_selector_group2);
		mCalendar_selector_group2.setOnClickListener(this);

		mCalendar_repeat_group = view.findViewById(R.id.calendar_repeat_layout);
		mCalendar_repeat_group.setOnClickListener(this);
		mTv_Calendar_repeat_value = (TextView) mCalendar_repeat_group
				.findViewById(R.id.tv_repeat_value);

		mTask_reminds_row = view.findViewById(R.id.task_reminds_row);
		mTask_reminds_row.setOnClickListener(this);
		mTask_priority_row = view.findViewById(R.id.task_priority_row);
		mTask_priority_row.setOnClickListener(this);
		mTask_group_row = view.findViewById(R.id.task_group_row);
		mTask_group_row.setOnClickListener(this);
		mTv_TaskRemindsValue = (TextView) mTask_reminds_row
				.findViewById(R.id.tv_task_reminds_displayName);
		mTv_TaskPriorityValue = (TextView) mTask_priority_row
				.findViewById(R.id.tv_task_priority_displayName);
		mTv_TaskGroupValue = (TextView) mTask_group_row
				.findViewById(R.id.tv_task_group_displayName);

		setupEditView(activity, view, eventInfo.eventStyle);
		mEt_Title.requestFocus();
	}

	private void setupEditView(Activity activity, View v, int viewStyle) {
		View view = null;
		TextView tv = (TextView) mCalendar_selector_group2
				.findViewById(R.id.tv_calendar_style);
		switch (viewStyle) {
		case EventStyle.EVNET_STYLE:
			// set calendar visible
			view = v.findViewById(R.id.calendar_layout_visible01);
			view.setVisibility(View.VISIBLE);
			view = v.findViewById(R.id.reminders_row);
			view.setVisibility(View.VISIBLE);

			// set task gone
			view = v.findViewById(R.id.task_layout_visible01);
			view.setVisibility(View.GONE);
			view = v.findViewById(R.id.task_layout_visible02);
			view.setVisibility(View.GONE);

			tv.setText(R.string.calendar);
			break;
		case EventStyle.TASK_STYLE:
			// set calendar gone
			view = v.findViewById(R.id.calendar_layout_visible01);
			view.setVisibility(View.GONE);
			view = v.findViewById(R.id.reminders_row);
			view.setVisibility(View.GONE);

			// set task visible
			view = v.findViewById(R.id.task_layout_visible01);
			view.setVisibility(View.VISIBLE);
			view = v.findViewById(R.id.task_layout_visible02);
			view.setVisibility(View.VISIBLE);

			tv.setText(R.string.task);
			break;
		}
		view = v.findViewById(R.id.response_row);
		view.setVisibility(View.GONE);
		view = v.findViewById(R.id.organizer_row);
		view.setVisibility(View.GONE);
		view = v.findViewById(R.id.add_attendees_row);
		view.setVisibility(View.GONE);
		view = v.findViewById(R.id.availability_row);
		view.setVisibility(View.GONE);
		view = v.findViewById(R.id.visibility_row);
		view.setVisibility(View.GONE);
	}

	/**
	 * Loads an integer array asset into a list.
	 */
	private static ArrayList<Integer> loadIntegerArray(Resources r, int resNum) {
		int[] vals = r.getIntArray(resNum);
		int size = vals.length;
		ArrayList<Integer> list = new ArrayList<Integer>(size);

		for (int i = 0; i < size; i++) {
			list.add(vals[i]);
		}

		return list;
	}

	/**
	 * Loads a String array asset into a list.
	 */
	private static ArrayList<String> loadStringArray(Resources r, int resNum) {
		String[] labels = r.getStringArray(resNum);
		ArrayList<String> list = new ArrayList<String>(Arrays.asList(labels));
		return list;
	}

	private void prepareAvailability() {
		Resources r = mActivity.getResources();

		mAvailabilityValues = loadIntegerArray(r, R.array.availability_values);
		mAvailabilityLabels = loadStringArray(r, R.array.availability);

		if (mModel.mCalendarAllowedAvailability != null) {
			EventViewUtils.reduceMethodList(mAvailabilityValues,
					mAvailabilityLabels, mModel.mCalendarAllowedAvailability);
		}

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(mActivity,
				android.R.layout.simple_spinner_item, mAvailabilityLabels);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mAvailabilitySpinner.setAdapter(adapter);
	}

	/**
	 * Prepares the reminder UI elements.
	 * <p>
	 * (Re-)loads the minutes / methods lists from the XML assets, adds/removes
	 * items as needed for the current set of reminders and calendar properties,
	 * and then creates UI elements.
	 */
	private void prepareReminders() {
		CalendarEventModel model = mModel;
		Resources r = mActivity.getResources();

		// Load the labels and corresponding numeric values for the minutes and
		// methods lists
		// from the assets. If we're switching calendars, we need to clear and
		// re-populate the
		// lists (which may have elements added and removed based on calendar
		// properties). This
		// is mostly relevant for "methods", since we shouldn't have any
		// "minutes" values in a
		// new event that aren't in the default set.
		mReminderMinuteValues = loadIntegerArray(r,
				R.array.reminder_minutes_values);
		mReminderMinuteLabels = loadStringArray(r,
				R.array.reminder_minutes_labels);
		mReminderMethodValues = loadIntegerArray(r,
				R.array.reminder_methods_values);
		mReminderMethodLabels = loadStringArray(r,
				R.array.reminder_methods_labels);

		// Remove any reminder methods that aren't allowed for this calendar. If
		// this is
		// a new event, mCalendarAllowedReminders may not be set the first time
		// we're called.
		if (mModel.mCalendarAllowedReminders != null) {
			EventViewUtils.reduceMethodList(mReminderMethodValues,
					mReminderMethodLabels, mModel.mCalendarAllowedReminders);
		}

		int numReminders = 0;
		if (model.mHasAlarm) {
			ArrayList<ReminderEntry> reminders = model.mReminders;
			numReminders = reminders.size();
			// Insert any minute values that aren't represented in the minutes
			// list.
			for (ReminderEntry re : reminders) {
				if (mReminderMethodValues.contains(re.getMethod())) {
					EventViewUtils.addMinutesToList(mActivity,
							mReminderMinuteValues, mReminderMinuteLabels,
							re.getMinutes());
				}
			}

			// Create a UI element for each reminder. We display all of the
			// reminders we get
			// from the provider, even if the count exceeds the calendar
			// maximum. (Also, for
			// a new event, we won't have a maxReminders value available.)
			mUnsupportedReminders.clear();
			for (ReminderEntry re : reminders) {
				if (mReminderMethodValues.contains(re.getMethod())
						|| re.getMethod() == Reminders.METHOD_DEFAULT) {
					EventViewUtils.addReminder(mActivity, mScrollView, this,
							mReminderItems, mReminderMinuteValues,
							mReminderMinuteLabels, mReminderMethodValues,
							mReminderMethodLabels, re, Integer.MAX_VALUE, null);
				} else {
					// TODO figure out a way to display unsupported reminders
					mUnsupportedReminders.add(re);
				}
			}
		}

		updateRemindersVisibility(numReminders);
	}

	/**
	 * Fill in the view with the contents of the given event model. This allows
	 * an edit view to be initialized before the event has been loaded. Passing
	 * in null for the model will display a loading screen. A non-null model
	 * will fill in the view's fields with the data contained in the model.
	 * 
	 * @param model
	 *            The event model to pull the data from
	 */
	public void setModel(CalendarEventModel model) {
		mModel = model;

		// Need to close the autocomplete adapter to prevent leaking cursors.
		if (mAddressAdapter != null
				&& mAddressAdapter instanceof EmailAddressAdapter) {
			((EmailAddressAdapter) mAddressAdapter).close();
			mAddressAdapter = null;
		}

		if (model == null) {
			// Display loading screen
			mLoadingMessage.setVisibility(View.VISIBLE);
			mScrollView.setVisibility(View.GONE);
			return;
		}

		boolean canRespond = EditEventHelper.canRespond(model);

		long begin = model.mStart;
		long end = model.mEnd;
		mTimezone = model.mTimezone; // this will be UTC for all day events

		// Set up the starting times
		if (begin > 0) {
			mStartTime.timezone = mTimezone;
			mStartTime.set(begin);
			mStartTime.normalize(true);
		}
		if (end > 0) {
			mEndTime.timezone = mTimezone;
			mEndTime.set(end);
			mEndTime.normalize(true);
		}
		String rrule = model.mRrule;
		if (!TextUtils.isEmpty(rrule)) {
			mEventRecurrence.parse(rrule);
		}

		// If the user is allowed to change the attendees set up the view and
		// validator
		if (!model.mHasAttendeeData) {
			mAttendeesGroup.setVisibility(View.GONE);
		}

		mAllDayCheckBox
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						setAllDayViewsVisibility(isChecked);
					}
				});

		boolean prevAllDay = mAllDayCheckBox.isChecked();
		mAllDay = false; // default to false. Let setAllDayViewsVisibility
							// update it as needed
		if (model.mAllDay) {
			mAllDayCheckBox.setChecked(true);
			// put things back in local time for all day events
			mTimezone = TimeZone.getDefault().getID();
			mStartTime.timezone = mTimezone;
			mStartTime.normalize(true);
			mEndTime.timezone = mTimezone;
			mEndTime.normalize(true);
		} else {
			mAllDayCheckBox.setChecked(false);
		}
		// On a rotation we need to update the views but onCheckedChanged
		// doesn't get called
		if (prevAllDay == mAllDayCheckBox.isChecked()) {
			setAllDayViewsVisibility(prevAllDay);
		}

		mTimezoneAdapter = new TimezoneAdapter(mActivity, mTimezone);
		if (mTimezoneDialog != null) {
			mTimezoneDialog.getListView().setAdapter(mTimezoneAdapter);
		}

		SharedPreferences prefs = GeneralPreferences
				.getSharedPreferences(mActivity);
		String defaultReminderString = prefs.getString(
				GeneralPreferences.KEY_DEFAULT_REMINDER,
				GeneralPreferences.NO_REMINDER_STRING);
		mDefaultReminderMinutes = Integer.parseInt(defaultReminderString);

		prepareReminders();
		prepareAvailability();

		View reminderAddButton = mView.findViewById(R.id.reminder_add);
		View.OnClickListener addReminderOnClickListener = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				addReminder();
			}
		};
		reminderAddButton.setOnClickListener(addReminderOnClickListener);

		if (!mIsMultipane) {
			mView.findViewById(R.id.is_all_day_label).setOnClickListener(
					new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							mAllDayCheckBox.setChecked(!mAllDayCheckBox
									.isChecked());
						}
					});
		}

		if (model.mTitle != null) {
			mEt_Title.setTextKeepState(model.mTitle);
		}

		if (model.mIsOrganizer || TextUtils.isEmpty(model.mOrganizer)
				|| model.mOrganizer.endsWith(GOOGLE_SECONDARY_CALENDAR)) {
			mView.findViewById(R.id.organizer_label).setVisibility(View.GONE);
			mView.findViewById(R.id.organizer).setVisibility(View.GONE);
			mOrganizerGroup.setVisibility(View.GONE);
		} else {
			((TextView) mView.findViewById(R.id.organizer))
					.setText(model.mOrganizerDisplayName);
		}

		if (model.mLocation != null) {
			mLocationTextView.setTextKeepState(model.mLocation);
		}

		if (model.mDescription != null) {
			mDescriptionTextView.setTextKeepState(model.mDescription);
		}

		int availIndex = mAvailabilityValues.indexOf(model.mAvailability);
		if (availIndex != -1) {
			mAvailabilitySpinner.setSelection(availIndex);
		}
		mAccessLevelSpinner.setSelection(model.mAccessLevel);

		View responseLabel = mView.findViewById(R.id.response_label);
		if (canRespond) {
			int buttonToCheck = EventInfoFragment
					.findButtonIdForResponse(model.mSelfAttendeeStatus);
			mResponseRadioGroup.check(buttonToCheck); // -1 clear all radio
														// buttons
			mResponseRadioGroup.setVisibility(View.VISIBLE);
			responseLabel.setVisibility(View.VISIBLE);
		} else {
			responseLabel.setVisibility(View.GONE);
			mResponseRadioGroup.setVisibility(View.GONE);
			mResponseGroup.setVisibility(View.GONE);
		}

		int displayColor = Utils.getDisplayColorFromColor(model.mCalendarColor);
		if (model.mUri != null) {
			// This is an existing event so hide the calendar spinner
			// since we can't change the calendar.
			View calendarGroup = mView
					.findViewById(R.id.calendar_selector_group);
			calendarGroup.setVisibility(View.GONE);
			TextView tv = (TextView) mView.findViewById(R.id.calendar_textview);
			tv.setText(model.mCalendarDisplayName);
			tv = (TextView) mView
					.findViewById(R.id.calendar_textview_secondary);
			if (tv != null) {
				tv.setText(model.mOwnerAccount);
			}
			if (mIsMultipane) {
				mView.findViewById(R.id.calendar_textview).setBackgroundColor(
						displayColor);
			} else {
				mView.findViewById(R.id.calendar_group).setBackgroundColor(
						displayColor);
			}
		} else {
			View calendarGroup = mView.findViewById(R.id.calendar_group);
			calendarGroup.setVisibility(View.GONE);
		}

		populateTimezone();
		populateWhen();
		populateRepeats();
		updateAttendees(model.mAttendeesList);

		updateView();
		mScrollView.setVisibility(View.VISIBLE);
		mLoadingMessage.setVisibility(View.GONE);
		sendAccessibilityEvent();

		// /M:#Lunar# @{
		if (Utils.isShowLunarCalendar()) {
			mIsUseLunarDatePicker = model.mIsLunar;
			if (model.mIsLunar) {
				RadioButton radioBtn = (RadioButton) mActivity
						.findViewById(R.id.switch_lunar);
				if (radioBtn != null) {
					radioBtn.setChecked(true);
				}
			} else {
				RadioButton radioBtn = (RadioButton) mActivity
						.findViewById(R.id.switch_gregorain);
				if (radioBtn != null) {
					radioBtn.setChecked(true);
				}
			}
		}
		// /@}
	}

	private void sendAccessibilityEvent() {
		AccessibilityManager am = (AccessibilityManager) mActivity
				.getSystemService(Service.ACCESSIBILITY_SERVICE);
		if (!am.isEnabled() || mModel == null) {
			return;
		}
		StringBuilder b = new StringBuilder();
		addFieldsRecursive(b, mView);
		CharSequence msg = b.toString();

		AccessibilityEvent event = AccessibilityEvent
				.obtain(AccessibilityEvent.TYPE_VIEW_FOCUSED);
		event.setClassName(getClass().getName());
		event.setPackageName(mActivity.getPackageName());
		event.getText().add(msg);
		event.setAddedCount(msg.length());

		am.sendAccessibilityEvent(event);
	}

	private void addFieldsRecursive(StringBuilder b, View v) {
		if (v == null || v.getVisibility() != View.VISIBLE) {
			return;
		}
		if (v instanceof TextView) {
			CharSequence tv = ((TextView) v).getText();
			if (!TextUtils.isEmpty(tv.toString().trim())) {
				b.append(tv + PERIOD_SPACE);
			}
		} else if (v instanceof RadioGroup) {
			RadioGroup rg = (RadioGroup) v;
			int id = rg.getCheckedRadioButtonId();
			if (id != View.NO_ID) {
				b.append(((RadioButton) (v.findViewById(id))).getText()
						+ PERIOD_SPACE);
			}
		} else if (v instanceof Spinner) {
			Spinner s = (Spinner) v;
			if (s.getSelectedItem() instanceof String) {
				String str = ((String) (s.getSelectedItem())).trim();
				if (!TextUtils.isEmpty(str)) {
					b.append(str + PERIOD_SPACE);
				}
			}
		} else if (v instanceof ViewGroup) {
			ViewGroup vg = (ViewGroup) v;
			int children = vg.getChildCount();
			for (int i = 0; i < children; i++) {
				addFieldsRecursive(b, vg.getChildAt(i));
			}
		}
	}

	/**
	 * Creates a single line string for the time/duration
	 */
	protected void setWhenString() {
		String when;
		int flags = DateUtils.FORMAT_SHOW_DATE;
		String tz = mTimezone;
		if (mModel.mAllDay) {
			flags |= DateUtils.FORMAT_SHOW_WEEKDAY;
			tz = Time.TIMEZONE_UTC;
		} else {
			flags |= DateUtils.FORMAT_SHOW_TIME;
			if (DateFormat.is24HourFormat(mActivity)) {
				flags |= DateUtils.FORMAT_24HOUR;
			}
		}
		long startMillis = mStartTime.normalize(true);
		long endMillis = mEndTime.normalize(true);
		mSB.setLength(0);
		when = DateUtils.formatDateRange(mActivity, mF, startMillis, endMillis,
				flags, tz).toString();
		mWhenView.setText(when);
	}

	/**
	 * Configures the Calendars spinner. This is only done for new events,
	 * because only new events allow you to select a calendar while editing an
	 * event.
	 * <p>
	 * We tuck a reference to a Cursor with calendar database data into the
	 * spinner, so that we can easily extract calendar-specific values when the
	 * value changes (the spinner's onItemSelected callback is configured).
	 */
	public void setCalendarsCursor(Cursor cursor, boolean userVisible) {
		// If there are no syncable calendars, then we cannot allow
		// creating a new event.
		mCalendarsCursor = cursor;
		if (cursor == null || cursor.getCount() == 0) {
			// Cancel the "loading calendars" dialog if it exists
			if (mSaveAfterQueryComplete) {
				mLoadingCalendarsDialog.cancel();
			}
			if (!userVisible) {
				return;
			}
			// Create an error message for the user that, when clicked,
			// will exit this activity without saving the event.
			AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
			builder.setTitle(R.string.no_syncable_calendars)
					.setIconAttribute(android.R.attr.alertDialogIcon)
					.setMessage(R.string.no_calendars_found)
					.setPositiveButton(R.string.add_account, this)
					.setNegativeButton(android.R.string.no, this)
					.setOnCancelListener(this);
			mNoCalendarsDialog = builder.show();
			return;
		}

		int defaultCalendarPosition = findDefaultCalendarPosition(cursor);

		// populate the calendars spinner
		CalendarsAdapter adapter = new CalendarsAdapter(mActivity, cursor);
		mCalendarsSpinner.setAdapter(adapter);
		mCalendarsSpinner.setSelection(defaultCalendarPosition);
		mCalendarsSpinner.setOnItemSelectedListener(this);

		// chenzhentao 2012-7-16 add start
		int pos = defaultCalendarPosition < cursor.getCount() ? defaultCalendarPosition
				: 0;
		changeCalendarText(pos);
		// chenzhentao 2012-7-16 add end

		if (mSaveAfterQueryComplete) {
			mLoadingCalendarsDialog.cancel();
			if (prepareForSave() && fillModelFromUI()) {
				int exit = userVisible ? Utils.DONE_EXIT : 0;
				mDone.setDoneCode(Utils.DONE_SAVE | exit);
				mDone.run();
			} else if (userVisible) {
				mDone.setDoneCode(Utils.DONE_EXIT);
				mDone.run();
			} else if (Log.isLoggable(TAG, Log.DEBUG)) {
				Log.d(TAG,
						"SetCalendarsCursor:Save failed and unable to exit view");
			}
			return;
		}
	}

	/**
	 * Updates the view based on {@link #mModification} and {@link #mModel}
	 */
	public void updateView() {
		if (mModel == null) {
			return;
		}
		if (EditEventHelper.canModifyEvent(mModel)) {
			setViewStates(mModification);
		} else {
			setViewStates(Utils.MODIFY_UNINITIALIZED);
		}
	}

	private void setViewStates(int mode) {
		// Extra canModify check just in case
		if (mode == Utils.MODIFY_UNINITIALIZED
				|| !EditEventHelper.canModifyEvent(mModel)) {
			setWhenString();

			for (View v : mViewOnlyList) {
				v.setVisibility(View.VISIBLE);
			}
			for (View v : mEditOnlyList) {
				v.setVisibility(View.GONE);
			}
			for (View v : mEditViewList) {
				v.setEnabled(false);
				v.setBackgroundDrawable(null);
			}
			mCalendarSelectorGroup.setVisibility(View.GONE);
			mCalendarStaticGroup.setVisibility(View.VISIBLE);
			// mRepeatsSpinner.setEnabled(false); // chenzhentao delete 2012-9-17
			// mRepeatsSpinner.setBackgroundDrawable(null);
			setAllDayViewsVisibility(mAllDayCheckBox.isChecked());
			if (EditEventHelper.canAddReminders(mModel)) {
				// mRemindersGroup.setVisibility(View.VISIBLE);
			} else {
				mRemindersGroup.setVisibility(View.GONE);
			}
			if (TextUtils.isEmpty(mLocationTextView.getText())) {
				mLocationGroup.setVisibility(View.GONE);
			}
			if (TextUtils.isEmpty(mDescriptionTextView.getText())) {
				mDescriptionGroup.setVisibility(View.GONE);
			}
		} else {
			for (View v : mViewOnlyList) {
				v.setVisibility(View.GONE);
			}
			for (View v : mEditOnlyList) {
				v.setVisibility(View.VISIBLE);
			}
			for (View v : mEditViewList) {
				v.setEnabled(true);
				if (v.getTag() != null) {
					v.setBackgroundDrawable((Drawable) v.getTag());
					v.setPadding(mOriginalPadding[0], mOriginalPadding[1],
							mOriginalPadding[2], mOriginalPadding[3]);
				}
			}
			if (mModel.mUri == null) {
				mCalendarSelectorGroup.setVisibility(View.VISIBLE);
				mCalendarStaticGroup.setVisibility(View.GONE);
			} else {
				mCalendarSelectorGroup.setVisibility(View.GONE);
				mCalendarStaticGroup.setVisibility(View.VISIBLE);
			}
			// mRepeatsSpinner.setBackgroundDrawable((Drawable) mRepeatsSpinner // chenzhentao 2012-9-17 add
					// .getTag());
			// mRepeatsSpinner.setPadding(mOriginalSpinnerPadding[0],
					// mOriginalSpinnerPadding[1], mOriginalSpinnerPadding[2],
					// mOriginalSpinnerPadding[3]);
			if (mModel.mOriginalSyncId == null) {
				// mRepeatsSpinner.setEnabled(true);
			} else {
				// mRepeatsSpinner.setEnabled(false);
			}
			// mRemindersGroup.setVisibility(View.VISIBLE);

			mLocationGroup.setVisibility(View.VISIBLE);
			mDescriptionGroup.setVisibility(View.VISIBLE);
		}
	}

	public void setModification(int modifyWhich) {
		mModification = modifyWhich;
		updateView();
		updateHomeTime();
	}

	// Find the calendar position in the cursor that matches calendar in
	// preference
	private int findDefaultCalendarPosition(Cursor calendarsCursor) {
		if (calendarsCursor.getCount() <= 0) {
			return -1;
		}

		String defaultCalendar = Utils.getSharedPreference(mActivity,
				GeneralPreferences.KEY_DEFAULT_CALENDAR, null);

		if (defaultCalendar == null) {
			return 0;
		}
		int calendarsOwnerColumn = calendarsCursor
				.getColumnIndexOrThrow(Calendars.OWNER_ACCOUNT);

		// /M: error handling @{
		if (calendarsOwnerColumn < 0) {
			LogUtil.w(TAG,
					"getColumnIndexOrThrow(Calendar.OWNER_ACCOUNT) failed, return 0");
			return 0;
		}
		// / @}

		int position = 0;
		calendarsCursor.moveToPosition(-1);
		while (calendarsCursor.moveToNext()) {
			if (defaultCalendar.equals(calendarsCursor
					.getString(calendarsOwnerColumn))) {
				return position;
			}
			position++;
		}
		return 0;
	}

	private void updateAttendees(HashMap<String, Attendee> attendeesList) {
		if (attendeesList == null || attendeesList.isEmpty()) {
			return;
		}
		mAttendeesList.setText(null);
		for (Attendee attendee : attendeesList.values()) {
			mAttendeesList.append(attendee.mEmail);
		}
	}

	private void updateRemindersVisibility(int numReminders) {
		if (numReminders == 0) {
			mRemindersContainer.setVisibility(View.GONE);
		} else {
			mRemindersContainer.setVisibility(View.VISIBLE);
		}

		// /M: if reminders can't be added more, the "add" button should gone.
		// @{
		View reminderAddButton = mView.findViewById(R.id.reminder_add);
		if (numReminders >= mModel.mCalendarMaxReminders) {
			reminderAddButton.setVisibility(View.GONE);
		} else {
			reminderAddButton.setVisibility(View.VISIBLE);
		}
		// / @}
	}

	private void updatePictureVisibility(int numPictures) {
		if (numPictures == 0) {
			mPictureContainer.setVisibility(View.GONE);
		} else {
			mPictureContainer.setVisibility(View.VISIBLE);
		}

		if (numPictures >= CALENDAR_MAX_PICTURE) {
			mImgBtn_pictureAdd.setVisibility(View.GONE);
		} else {
			mImgBtn_pictureAdd.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * Add a new reminder when the user hits the "add reminder" button. We use
	 * the default reminder time and method.
	 */
	private void addReminder() {
		// TODO: when adding a new reminder, make it different from the
		// last one in the list (if any).
		if (mDefaultReminderMinutes == GeneralPreferences.NO_REMINDER) {
			EventViewUtils.addReminder(mActivity, mScrollView, this,
					mReminderItems, mReminderMinuteValues,
					mReminderMinuteLabels, mReminderMethodValues,
					mReminderMethodLabels, ReminderEntry
							.valueOf(GeneralPreferences.REMINDER_DEFAULT_TIME),
					mModel.mCalendarMaxReminders, null);
		} else {
			EventViewUtils.addReminder(mActivity, mScrollView, this,
					mReminderItems, mReminderMinuteValues,
					mReminderMinuteLabels, mReminderMethodValues,
					mReminderMethodLabels,
					ReminderEntry.valueOf(mDefaultReminderMinutes),
					mModel.mCalendarMaxReminders, null);
		}
		updateRemindersVisibility(mReminderItems.size());
	}

	public void addPicture(Bitmap bitmap, String name) {
		EventViewUtils.addPicture(mActivity, mScrollView, this, mPictureItems,
				bitmap, name, CALENDAR_MAX_PICTURE, null);
		updatePictureVisibility(mPictureItems.size());
	}

	// From com.google.android.gm.ComposeActivity
	private MultiAutoCompleteTextView initMultiAutoCompleteTextView(
			RecipientEditTextView list) {
		if (ChipsUtil.supportsChipsUi()) {
			mAddressAdapter = new RecipientAdapter(mActivity);
			list.setAdapter((BaseRecipientAdapter) mAddressAdapter);
			list.setOnFocusListShrinkRecipients(false);
			/*
			//sunrise begin
			Resources r = mActivity.getResources();
			Bitmap def = BitmapFactory.decodeResource(r,
					R.drawable.ic_contact_picture);
            
			list.setChipDimensions(r.getDrawable(R.drawable.chip_background),
					r.getDrawable(R.drawable.chip_background_selected),
					r.getDrawable(R.drawable.chip_background_invalid),
					r.getDrawable(R.drawable.chip_delete), def,
					R.layout.more_item, R.layout.chips_alternate_item,
					r.getDimension(R.dimen.chip_height),
					r.getDimension(R.dimen.chip_padding),
					r.getDimension(R.dimen.chip_text_size),
					R.layout.copy_chip_dialog_layout);
		    */
			//sunrise end
		} else {
			mAddressAdapter = new EmailAddressAdapter(mActivity);
			list.setAdapter((EmailAddressAdapter) mAddressAdapter);
		}
		list.setTokenizer(new Rfc822Tokenizer());
		list.setValidator(mEmailValidator);

		// NOTE: assumes no other filters are set
		list.setFilters(sRecipientFilters);

		return list;
	}

	/**
	 * From com.google.android.gm.ComposeActivity Implements special address
	 * cleanup rules: The first space key entry following an "@" symbol that is
	 * followed by any combination of letters and symbols, including one+ dots
	 * and zero commas, should insert an extra comma (followed by the space).
	 * M:add one length filter(LengthFilter) into sRecipientFilters.
	 */
	private static InputFilter[] sRecipientFilters = { new Rfc822InputFilter(),
			new InputFilter.LengthFilter(2000) };

	private void setDate(TextView view, long millis) {
		int flags = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR
				| DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_ABBREV_MONTH
				| DateUtils.FORMAT_ABBREV_WEEKDAY;

		// Unfortunately, DateUtils doesn't support a timezone other than the
		// default timezone provided by the system, so we have this ugly hack
		// here to trick it into formatting our time correctly. In order to
		// prevent all sorts of craziness, we synchronize on the TimeZone class
		// to prevent other threads from reading an incorrect timezone from
		// calls to TimeZone#getDefault()
		// TODO fix this if/when DateUtils allows for passing in a timezone
		String dateString;
		synchronized (TimeZone.class) {
			TimeZone.setDefault(TimeZone.getTimeZone(mTimezone));
			dateString = DateUtils.formatDateTime(mActivity, millis, flags);
			// setting the default back to null restores the correct behavior
			TimeZone.setDefault(null);
			// /M:#Lunar# modify for lunar calendar. @{
			mIsUseLunarDatePicker = isLunarDataPickerClicked();
			if (mIsUseLunarDatePicker) {
				Time time = new Time();
				time.set(millis);
				dateString = LunarUtil.getLunarDateString(time.year,
						time.month + 1, time.monthDay);
			}
			// /@}
		}
		view.setText(dateString);
	}

	private void setTime(TextView view, long millis) {
		int flags = DateUtils.FORMAT_SHOW_TIME;
		if (DateFormat.is24HourFormat(mActivity)) {
			flags |= DateUtils.FORMAT_24HOUR;
		}

		// Unfortunately, DateUtils doesn't support a timezone other than the
		// default timezone provided by the system, so we have this ugly hack
		// here to trick it into formatting our time correctly. In order to
		// prevent all sorts of craziness, we synchronize on the TimeZone class
		// to prevent other threads from reading an incorrect timezone from
		// calls to TimeZone#getDefault()
		// TODO fix this if/when DateUtils allows for passing in a timezone
		String timeString;
		synchronized (TimeZone.class) {
			TimeZone.setDefault(TimeZone.getTimeZone(mTimezone));
			timeString = DateUtils.formatDateTime(mActivity, millis, flags);
			TimeZone.setDefault(null);
		}
		view.setText(timeString);
	}

	private void setTimezone(int i) {
		if (i < 0 || i >= mTimezoneAdapter.getCount()) {
			return; // do nothing
		}
		TimezoneRow timezone = mTimezoneAdapter.getItem(i);
		mTimezoneTextView.setText(timezone.toString());
		mTimezoneButton.setText(timezone.toString());
		mTimezone = timezone.mId;
		mStartTime.timezone = mTimezone;
		mStartTime.normalize(true);
		mEndTime.timezone = mTimezone;
		mEndTime.normalize(true);
		mTimezoneAdapter.setCurrentTimezone(mTimezone);
	}

	/**
	 * @param isChecked
	 */
	protected void setAllDayViewsVisibility(boolean isChecked) {
		if (isChecked) {
			if (mEndTime.hour == 0 && mEndTime.minute == 0) {
				if (mAllDay != isChecked) {
					mEndTime.monthDay--;
				}

				long endMillis = mEndTime.normalize(true);

				// Do not allow an event to have an end time
				// before the
				// start time.
				if (mEndTime.before(mStartTime)) {
					mEndTime.set(mStartTime);
					endMillis = mEndTime.normalize(true);
				}
				setDate(mEndDateButton, endMillis);
				setTime(mEndTimeButton, endMillis);
			}

			mStartTimeButton.setVisibility(View.GONE);
			mEndTimeButton.setVisibility(View.GONE);
			mTimezoneRow.setVisibility(View.GONE);
		} else {
			if (mEndTime.hour == 0 && mEndTime.minute == 0) {
				if (mAllDay != isChecked) {
					mEndTime.monthDay++;
				}

				long endMillis = mEndTime.normalize(true);
				setDate(mEndDateButton, endMillis);
				setTime(mEndTimeButton, endMillis);
			}
			mStartTimeButton.setVisibility(View.VISIBLE);
			mEndTimeButton.setVisibility(View.VISIBLE);
			mTimezoneRow.setVisibility(View.VISIBLE);
		}
		mAllDay = isChecked;
		updateHomeTime();
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		// This is only used for the Calendar spinner in new events, and only
		// fires when the
		// calendar selection changes or on screen rotation
		Cursor c = (Cursor) parent.getItemAtPosition(position);
		if (c == null) {
			// TODO: can this happen? should we drop this check?
			Log.w(TAG, "Cursor not set on calendar item");
			return;
		}

		int colorColumn = c.getColumnIndexOrThrow(Calendars.CALENDAR_COLOR);
		int color = c.getInt(colorColumn);
		int displayColor = Utils.getDisplayColorFromColor(color);

		if (mIsMultipane) {
			mCalendarSelectorWrapper.setBackgroundColor(displayColor);
		} else {
			mCalendarSelectorGroup.setBackgroundColor(displayColor);
		}

		// Do nothing if the selection didn't change so that reminders will not
		// get lost
		int idColumn = c.getColumnIndexOrThrow(Calendars._ID);
		long calendarId = c.getLong(idColumn);
		if (calendarId == mModel.mCalendarId) {
			return;
		}
		mModel.mCalendarId = calendarId;
		mModel.mCalendarColor = color;
		// Update the max/allowed reminders with the new calendar properties.
		int maxRemindersColumn = c
				.getColumnIndexOrThrow(Calendars.MAX_REMINDERS);
		mModel.mCalendarMaxReminders = c.getInt(maxRemindersColumn);
		int allowedRemindersColumn = c
				.getColumnIndexOrThrow(Calendars.ALLOWED_REMINDERS);
		mModel.mCalendarAllowedReminders = c.getString(allowedRemindersColumn);
		int allowedAttendeeTypesColumn = c
				.getColumnIndexOrThrow(Calendars.ALLOWED_ATTENDEE_TYPES);
		mModel.mCalendarAllowedAttendeeTypes = c
				.getString(allowedAttendeeTypesColumn);
		int allowedAvailabilityColumn = c
				.getColumnIndexOrThrow(Calendars.ALLOWED_AVAILABILITY);
		mModel.mCalendarAllowedAvailability = c
				.getString(allowedAvailabilityColumn);

		// Discard the current reminders and replace them with the model's
		// default reminder set.
		// We could attempt to save & restore the reminders that have been
		// added, but that's
		// probably more trouble than it's worth.
		mModel.mReminders.clear();
		mModel.mReminders.addAll(mModel.mDefaultReminders);
		mModel.mHasAlarm = mModel.mReminders.size() != 0;

		// Update the UI elements.
		mReminderItems.clear();
		LinearLayout reminderLayout = (LinearLayout) mScrollView
				.findViewById(R.id.reminder_items_container);
		reminderLayout.removeAllViews();
		prepareReminders();
		prepareAvailability();
	}

	/**
	 * Checks if the start and end times for this event should be displayed in
	 * the Calendar app's time zone as well and formats and displays them.
	 */
	private void updateHomeTime() {
		String tz = Utils.getTimeZone(mActivity, null);
		if (!mAllDayCheckBox.isChecked() && !TextUtils.equals(tz, mTimezone)
				&& mModification != EditEventHelper.MODIFY_UNINITIALIZED) {
			int flags = DateUtils.FORMAT_SHOW_TIME;
			boolean is24Format = DateFormat.is24HourFormat(mActivity);
			if (is24Format) {
				flags |= DateUtils.FORMAT_24HOUR;
			}
			long millisStart = mStartTime.toMillis(false);
			long millisEnd = mEndTime.toMillis(false);

			boolean isDSTStart = mStartTime.isDst != 0;
			boolean isDSTEnd = mEndTime.isDst != 0;

			// First update the start date and times
			String tzDisplay = TimeZone.getTimeZone(tz).getDisplayName(
					isDSTStart, TimeZone.LONG, Locale.getDefault());
			StringBuilder time = new StringBuilder();

			mSB.setLength(0);
			time.append(
					DateUtils.formatDateRange(mActivity, mF, millisStart,
							millisStart, flags, tz)).append(" ")
					.append(tzDisplay);
			mStartTimeHome.setText(time.toString());

			flags = DateUtils.FORMAT_ABBREV_ALL | DateUtils.FORMAT_SHOW_DATE
					| DateUtils.FORMAT_SHOW_YEAR
					| DateUtils.FORMAT_SHOW_WEEKDAY;
			mSB.setLength(0);
			mStartDateHome.setText(DateUtils.formatDateRange(mActivity, mF,
					millisStart, millisStart, flags, tz).toString());

			// Make any adjustments needed for the end times
			if (isDSTEnd != isDSTStart) {
				tzDisplay = TimeZone.getTimeZone(tz).getDisplayName(isDSTEnd,
						TimeZone.LONG, Locale.getDefault());
			}
			flags = DateUtils.FORMAT_SHOW_TIME;
			if (is24Format) {
				flags |= DateUtils.FORMAT_24HOUR;
			}

			// Then update the end times
			time.setLength(0);
			mSB.setLength(0);
			time.append(
					DateUtils.formatDateRange(mActivity, mF, millisEnd,
							millisEnd, flags, tz)).append(" ")
					.append(tzDisplay);
			mEndTimeHome.setText(time.toString());

			flags = DateUtils.FORMAT_ABBREV_ALL | DateUtils.FORMAT_SHOW_DATE
					| DateUtils.FORMAT_SHOW_YEAR
					| DateUtils.FORMAT_SHOW_WEEKDAY;
			mSB.setLength(0);
			mEndDateHome.setText(DateUtils.formatDateRange(mActivity, mF,
					millisEnd, millisEnd, flags, tz).toString());

			mStartHomeGroup.setVisibility(View.VISIBLE);
			mEndHomeGroup.setVisibility(View.VISIBLE);
		} else {
			mStartHomeGroup.setVisibility(View.GONE);
			mEndHomeGroup.setVisibility(View.GONE);
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
	}

	// /M:Use to limit input length,when input max length,vibrate.{
	private void setLengthInputFilter(TextView inputText,
			final Context context, final int maxLength) {
		InputFilter[] contentFilters = new InputFilter[1];
		contentFilters[0] = new InputFilter.LengthFilter(maxLength) {
			public CharSequence filter(CharSequence source, int start, int end,
					Spanned dest, int dstart, int dend) {
				if (source != null
						&& source.length() > 0
						&& (((dest == null ? 0 : dest.length()) + dstart - dend) == maxLength)) {
					Vibrator vibrator = (Vibrator) context
							.getSystemService(Context.VIBRATOR_SERVICE);
					boolean hasVibrator = vibrator.hasVibrator();
					if (hasVibrator) {
						vibrator.vibrate(new long[] { 100, 100 }, -1);
					}
					Log.w(TAG, "input out of range,hasVibrator:" + hasVibrator);
					return "";
				}
				return super.filter(source, start, end, dest, dstart, dend);
			}
		};
		inputText.setFilters(contentFilters);
	}

	// /@}

	// /M: @{
	/**
	 * DialogManager is used to manage all potential dialogs of this
	 * EditEventView It remembered whether there exists a dialog. Use it to
	 * determine whether a dialog can be shown.
	 */
	private class DialogManager implements DialogInterface.OnDismissListener,
			DialogInterface.OnShowListener {

		private boolean mIsAnyDialogShown = false;

		public boolean isAnyDialogShown() {
			return mIsAnyDialogShown;
		}

		public void dialogShown() {
			if (isAnyDialogShown()) {
				LogUtil.w(TAG,
						"There is already a dialog shown, but another dialog is "
								+ "going to show.");
			}
			mIsAnyDialogShown = true;
		}

		private void dialogDismissed() {
			if (!isAnyDialogShown()) {
				LogUtil.w(TAG,
						"There is no dialog shown, but some dialog dismissed.");
			}
			mIsAnyDialogShown = false;
		}

		@Override
		public void onDismiss(DialogInterface dialog) {
			dialogDismissed();
		}

		@Override
		public void onShow(DialogInterface dialog) {
			dialogShown();
		}
	}

	// / @}

	// /M:#Lunar# add for lunar calendar. @{
	public void updateDatePickerSelection() {
		RadioGroup radioGroup = (RadioGroup) mActivity
				.findViewById(R.id.switch_date_picker);
		if (radioGroup != null) {
			if (Utils.isShowLunarCalendar()) {
				radioGroup.setVisibility(View.VISIBLE);
				/*
				 * RadioButton gregorain = (RadioButton)
				 * radioGroup.findViewById(R.id.switch_gregorain);
				 * if(gregorain!=null){ gregorain.setChecked(true); }
				 */
				// TODO: reset the start date button's text.
				populateWhen();

				// set the listener.
				radioGroup
						.setOnCheckedChangeListener(new OnCheckedChangeListener() {
							public void onCheckedChanged(RadioGroup group,
									int checkedId) {
								switch (checkedId) {
								case R.id.switch_lunar:
									mIsUseLunarDatePicker = true;
									populateWhen();
									mModel.mIsLunar = true;
									break;
								case R.id.switch_gregorain:
								default:
									mIsUseLunarDatePicker = false;
									populateWhen();
									mModel.mIsLunar = false;
									break;
								}
							}

						});
			} else {
				radioGroup.setVisibility(View.GONE);
			}
		}
	}

	// /@}

	/**
	 * if lunar data picker is ckecked return true ,else return false.
	 */
	private boolean isLunarDataPickerClicked() {
		RadioGroup radioGroup = (RadioGroup) mActivity
				.findViewById(R.id.switch_date_picker);
		// /M:check @{
		if (radioGroup == null) {
			LogUtil.w(TAG,
					"RadioGroup is null,return false to show Gregorian Datepicker.");
			return false;
		}
		// if radioGroup is not visible,It says now cannot show lunar so can't
		// be clicked.
		if (radioGroup.getVisibility() != View.VISIBLE) {
			LogUtil.d(TAG,
					"RadioGroup is not visible,return false to show Gregorian Datepicker");
			return false;
		}
		// /@}
		int selectedID = radioGroup.getCheckedRadioButtonId();
		return selectedID == R.id.switch_lunar ? true : false;
	}

	/**
	 * M: Record and reset the scroll position to scroll view, when the next
	 * time the scroll view shows. @{
	 **/
	private int mScrollPosition;

	public void dispatchOnStop() {
		mScrollPosition = mScrollView.getScrollY();
	}

	public void dispatchOnStart() {
		mScrollView.clearFocus();
		mScrollView.setScrollY(mScrollPosition);
	}

	/** @} **/

	// /M:dismiss all spinner. @{
	public void dismissAllSpinners() {
		// dimissSpinner(mRepeatsSpinner);
		dimissSpinner(mCalendarsSpinner);
		dimissSpinner(mAvailabilitySpinner);
		dimissSpinner(mAccessLevelSpinner);

		// dismiss the reminder minutes spinners.
		LinearLayout parent = (LinearLayout) mScrollView
				.findViewById(R.id.reminder_items_container);
		if (parent != null) {
			int count = parent.getChildCount();
			Spinner childSpinner;
			for (int i = 0; i < count; i++) {
				childSpinner = (Spinner) parent.getChildAt(i).findViewById(
						R.id.reminder_minutes_value);
				dimissSpinner(childSpinner);
			}
		}
	}

	private void dimissSpinner(Spinner spinner) {
		// if(spinner != null && spinner.isPopupShowing()) {
		// if (spinner != null && spinner.isShown()) {
		// spinner.dismissPopup();
		// }
		System.out.println("dimissSpinner null!!!");
	}

	// /@}

	// /M: give a tip when contain valid attendees.@{

	private void ToastMsg(int resId) {
		if (mToast == null) {
			mToast = Toast.makeText(mActivity, resId, Toast.LENGTH_SHORT);
		} else {
			mToast.setText(resId);
		}
		mToast.show();
	}

	/**
	 * Checks whether a string attendee address has invalid address. M: change
	 * from EditEventHeper.getAddressesFromList()
	 */
	private boolean isHasInvalidAddress(String list, Rfc822Validator validator) {
		LinkedHashSet<Rfc822Token> addresses = new LinkedHashSet<Rfc822Token>();
		Rfc822Tokenizer.tokenize(list, addresses);
		if (validator == null) {
			return false;
		}

		boolean isHasInvalidAttendee = false;
		Iterator<Rfc822Token> addressIterator = addresses.iterator();
		while (addressIterator.hasNext()) {
			Rfc822Token address = addressIterator.next();
			if (!validator.isValid(address.getAddress())) {
				isHasInvalidAttendee = true;
			}
		}
		return isHasInvalidAttendee;
	}

	// /@}

	/**
	 * @param isChecked
	 */
	protected void setTaskEndDayVisible(boolean isChecked) {
		if (isChecked) {
			mLinearLayout_end_date_row.setVisibility(View.GONE);
		} else {
			mLinearLayout_end_date_row.setVisibility(View.VISIBLE);
		}
		// updateHomeTime();
	}

	class OnSetEndDateListener implements OnDateSetListener {
		private View mView = null;

		public OnSetEndDateListener(View view) {
			// TODO Auto-generated constructor stub
			this.mView = view;
		}

		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			Button button = (Button) mView;
			Time time = new Time(mTimezone);
			time.set(dayOfMonth, monthOfYear, year);
			setDate(button, time.normalize(false));
		}
	}

	private void changeCalendarText(int position) {
		TextView textView = (TextView) mCalendar_selector_group2
				.findViewById(R.id.tv_calendar_displayName);
		if (mCalendarsCursor != null && mCalendarsCursor.getCount() > 0) {
			int pos = position < mCalendarsCursor.getCount() ? position : 0;
			int displayNameIndex = mCalendarsCursor
					.getColumnIndexOrThrow(Calendars.CALENDAR_DISPLAY_NAME);
			mCalendarsCursor.moveToPosition(pos);
			textView.setText(mCalendarsCursor.getString(displayNameIndex));
			mCalendarsSpinner.setSelection(pos);
		}
	}

	private static int findIndexByText(String[] textArray, String text) {
		int len = textArray.length;
		for (int i = 0; i < len; i++) {
			if (textArray[i].equals(text)) {
				return i;
			}
		}
		return 0;
	}

	private static String[] parseListToArray(ArrayList<String> list) {
		if (list != null) {
			int len = list.size();
			String[] data = new String[len];
			for (int i = 0; i < len; i++) {
				data[i] = list.get(i);
			}
			return data;
		}
		return null;
	}
}
