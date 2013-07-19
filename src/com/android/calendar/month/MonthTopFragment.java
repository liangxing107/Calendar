package com.android.calendar.month;

import com.android.calendar.CalendarController;
import com.android.calendar.R;
import com.android.calendar.Utils;
import com.android.calendar.CalendarController.EventType;
import com.android.calendar.CalendarController.ViewType;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Fragment;
import android.app.DatePickerDialog.OnDateSetListener;
import android.os.Bundle;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.DatePicker;
import android.widget.TextView;

public class MonthTopFragment extends Fragment implements OnClickListener {

	public static final String KEY_BUNDLE_TIME = "key_bundle_time";
	
	private CalendarController mCalendarController = null;
	private Activity mContext = null;
	private Time mTime = new Time();
	private TextView mTv_curentMonth = null;
	private TextView mTv_prevMonth = null;
	private TextView mTv_nextMonth = null;

	public MonthTopFragment() {
		this(0);
	}

	public MonthTopFragment(long timeMillis) {
		if (timeMillis == 0) {
			mTime.setToNow();
		} else {
			mTime.set(timeMillis);
		}
	}

	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		mContext = activity;
		mCalendarController = CalendarController.getInstance(mContext);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View v = inflater.inflate(R.layout.month_name_layout, container, false);
		mTv_prevMonth = (TextView)v.findViewById(R.id.btn_prevMonth);
		mTv_nextMonth = (TextView)v.findViewById(R.id.btn_nextMonth);
		mTv_curentMonth = (TextView)v.findViewById(R.id.btn_currentMonth);
		mTv_prevMonth.setOnClickListener(this);
		mTv_nextMonth.setOnClickListener(this);
		mTv_curentMonth.setOnClickListener(this);
		setMonthNames(mTime);
		return v;
	}

	public void onClick(View v) {
		// TODO Auto-generated method stub
		CalendarController calendarController = CalendarController
				.getInstance(mContext);
		Time time = new Time();
		time.set(calendarController.getTime());
		switch (v.getId()) {
		case R.id.btn_prevMonth:
			time.month--;
			break;
		case R.id.btn_currentMonth:
			new DatePickerDialog(mContext, mOnDateSetListener, time.year, time.month,
					time.monthDay).show();
			return;
		case R.id.btn_nextMonth:
			time.month++;
			break;
		}
		goTo(time);
	}
	
	private void goTo(Time time){
		setMonthNames(time);
		mCalendarController.sendEvent(mContext, EventType.GO_TO, time, null, -1,
				ViewType.CURRENT);
	}
	
	private void setMonthNames(Time time){
		Time tempTime = new Time(time);
		mTv_curentMonth.setText(Utils.formatMonthYear(mContext, tempTime));
		tempTime.month += 1;
		tempTime.normalize(true);
		mTv_nextMonth.setText(Utils.formatOnlyMonth(mContext, tempTime));
		tempTime.month -= 2;
		tempTime.normalize(true);
		mTv_prevMonth.setText(Utils.formatOnlyMonth(mContext, tempTime));
	}
	
	/**
	 * datePicker listener
	 * 
	 * @author chenzhentao
	 */
	private OnDateSetListener mOnDateSetListener = new OnDateSetListener() {
		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			// TODO Auto-generated method stub
			Time t = new Time();
			t.year = year;
			t.month = monthOfYear;
			t.normalize(false);
			goTo(t);
		}
	};
}
