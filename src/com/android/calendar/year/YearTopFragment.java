package com.android.calendar.year;

import com.android.calendar.CalendarController;
import com.android.calendar.Utils;
import com.android.calendar.CalendarController.EventType;
import com.android.calendar.CalendarController.ViewType;
import com.android.calendar.R;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Fragment;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Context;
import android.os.Bundle;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TextView;

public class YearTopFragment extends Fragment implements OnClickListener {

	public static final String KEY_BUNDLE_TIME = "key_bundle_time";
	
	private CalendarController mCalendarController = null;
	private Activity mContext = null;
	private TextView mTv_curentYear = null;
	private Time mTime = new Time();

	public YearTopFragment() {
		this(0);
	}

	public YearTopFragment(long timeMillis) {
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
		View v = inflater.inflate(R.layout.year_top_fragment, container, false);
		v.findViewById(R.id.btn_prevMonth_year).setOnClickListener(this);
		v.findViewById(R.id.btn_nextMonth_year).setOnClickListener(this);
		mTv_curentYear = (TextView)v.findViewById(R.id.btn_currentMonth_year);
		mTv_curentYear.setOnClickListener(this);
		setCurrentYear(mTime);
		return v;
	}

	public void onClick(View v) {
		// TODO Auto-generated method stub
		Time time = new Time();
		time.set(mCalendarController.getTime());
		switch (v.getId()) {
		case R.id.btn_prevMonth_year:
			time.year--;
			break;
		case R.id.btn_currentMonth_year:
			new DatePickerDialog(mContext, mOnDateSetListener, time.year, time.month,
					time.monthDay).show();
			return;
		case R.id.btn_nextMonth_year:
			time.year++;
			break;
		}
		goTo(time);
	}

	private void goTo(Time time){
		setCurrentYear(time);
		mCalendarController.sendEvent(mContext, EventType.GO_TO, time, null, -1,
				ViewType.CURRENT);
	}
	
	public void setCurrentYear(Time time){
		mTv_curentYear.setText(String.valueOf(time.year));
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
