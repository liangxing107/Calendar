package com.android.calendar;

import com.android.calendar.CalendarController.EventType;
import com.android.calendar.CalendarController.ViewType;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

public class DayTopFragment extends Fragment implements OnClickListener {

	private Time mTime = new Time();
	private Context mContext = null;
	private TextView mTv_currentTime = null;
	private int mNumDays;

	public DayTopFragment() {
		mTime.setToNow();
	}

	public DayTopFragment(long timeMillis, int numOfDays) {
		mNumDays = numOfDays;
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
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View v = inflater.inflate(R.layout.day_view_top, container, false);
		v.findViewById(R.id.btn_arrow_left).setOnClickListener(this);
		v.findViewById(R.id.btn_arrow_right).setOnClickListener(this);
		mTv_currentTime = (TextView) v.findViewById(R.id.tv_day_view_date);
		setCurrentYear(mTime);
		return v;
	}

	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub
		switch (view.getId()) {
		case R.id.btn_arrow_left:
			mTime.monthDay -= mNumDays; // for viewType week and day
			break;
		case R.id.btn_arrow_right:
			mTime.monthDay += mNumDays; // for viewType week and day
			break;
		}
		mTime.normalize(false);
		goTo(mTime);
	}

	private void goTo(Time time) {
		setCurrentYear(time);
		CalendarController.getInstance(mContext).sendEvent(mContext,
				EventType.GO_TO, time, time, -1, ViewType.CURRENT);
	}

	private void setCurrentYear(Time time) {
		mTv_currentTime.setText(DayFragment.formatTime(mContext, time));
	}
}
