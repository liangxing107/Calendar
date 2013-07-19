package com.android.calendar.year;

import com.android.calendar.CalendarController;
import com.android.calendar.CalendarController.EventInfo;
import com.android.calendar.CalendarController.EventType;
import com.android.calendar.CalendarController.ViewType;
import com.android.calendar.month.MonthByWeekAdapter;
import com.android.calendar.R;
import com.android.calendar.Utils;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.DatePickerDialog.OnDateSetListener;
import android.graphics.Color;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewSwitcher;
import android.widget.ViewSwitcher.ViewFactory;

public class YearFragment extends Fragment implements
		CalendarController.EventHandler, OnClickListener, OnItemClickListener {

	/**
	 * a viewSwitcher's child Count must be 2, not 0,1 or other number.
	 */
	private static final int VIEW_SWITCHER_MAX_CHILD_NUM = 2;

	private ViewSwitcher mViewSwitcher = null;
	private Activity mActivity = null;
	private int mGridNumColumns = 0;

	private String mTimeZone = null;
	private Time mTime = null; // view's time, UI freshed by it

	private Runnable mTZUpdater = new Runnable() {
		@Override
		public void run() {
			mTimeZone = Utils.getTimeZone(getActivity(), this);
			mTime.switchTimezone(mTimeZone);
		}
	};

	public YearFragment() {
		this(0, false);
	}

	public YearFragment(long timeMillis, boolean usedForSearch) {
		mTime = new Time();
		if (timeMillis == 0) {
			mTime.setToNow();
		} else {
			mTime.set(timeMillis);
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mTimeZone = Utils.getTimeZone(activity, mTZUpdater);
		mTime.switchTimezone(mTimeZone);
		mActivity = activity;
		mViewSwitcher = new ViewSwitcher(activity);
		mViewSwitcher.setLayoutParams(new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		mGridNumColumns = activity.getResources().getInteger(
				R.integer.year_grid_view_num_columns);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		for (int i = 0; i < VIEW_SWITCHER_MAX_CHILD_NUM; i++) {
			View child = inflater.inflate(R.layout.year_fragment_layout,
					container, false);
			mViewSwitcher.addView(child);

			YearGridView gridView = (YearGridView) child
					.findViewById(R.id.yearGridView);
			gridView.setNumColumns(mGridNumColumns);
			gridView.setOnItemClickListener(this);
			gridView.setTime(mTime);

			TextView tv_year = (TextView) child
					.findViewById(R.id.btn_currentMonth_year);
			tv_year.setText(mTime.format(DateUtils.YEAR_FORMAT));
			tv_year.setOnClickListener(this);
			child.findViewById(R.id.btn_prevMonth_year)
					.setOnClickListener(this);
			child.findViewById(R.id.btn_nextMonth_year)
					.setOnClickListener(this);
		}
		mViewSwitcher.setDisplayedChild(0);
		return mViewSwitcher;
	}

	public Time getTime() {
		return mTime;
	}

	public void setTime(Time time) {
		if(mViewSwitcher == null){
			return;
		}
		if (time != null && mTime.year != time.year) {
			View nextView = mViewSwitcher.getNextView();
			YearGridView next_gridView = (YearGridView) nextView
					.findViewById(R.id.yearGridView);
			TextView next_tv_year = (TextView) nextView
					.findViewById(R.id.btn_currentMonth_year);
			next_gridView.setTime(time);
			next_tv_year.setText(time.format(DateUtils.YEAR_FORMAT));
			if (time.year > mTime.year) { // next
				mViewSwitcher.setInAnimation(mActivity, R.anim.slide_left_in);
				mViewSwitcher.setOutAnimation(mActivity, R.anim.slide_left_out);
				mViewSwitcher.showNext();
			} else {
				mViewSwitcher.setInAnimation(mActivity, R.anim.slide_right_in);
				mViewSwitcher
						.setOutAnimation(mActivity, R.anim.slide_right_out);
				mViewSwitcher.showPrevious();
			}
			mTime = time;
		}
	}

	@Override
	public long getSupportedEventTypes() {
		// TODO Auto-generated method stub
		return EventType.GO_TO | EventType.EVENTS_CHANGED;
	}

	@Override
	public void handleEvent(EventInfo event) {
		// TODO Auto-generated method stub
		Time startTime = event.startTime == null ? event.selectedTime
				: event.startTime;
		if (event.eventType == EventType.GO_TO) {
			setTime(startTime);
		} else if (event.eventType == EventType.EVENTS_CHANGED) {

		}
	}

	@Override
	public void eventsChanged() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub
		Time time = new Time(mTime);
		switch (view.getId()) {
		case R.id.btn_prevMonth_year:
			time.year--;
			time.normalize(false);
			setTime(time);
			break;
		case R.id.btn_nextMonth_year:
			time.year++;
			time.normalize(false);
			setTime(time);
			break;
		case R.id.btn_currentMonth_year:
			new DatePickerDialog(mActivity, mOnDateSetListener, mTime.year,
					mTime.month, mTime.monthDay).show();
			break;
		}
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
			Time time = new Time(mTime);
			if (time.year != year) {
				time.year = year;
				time.normalize(false);
				setTime(time);
			}
		}
	};

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		Time time = new Time(mTime);
		time.month = position;
		CalendarController controller = CalendarController
				.getInstance(mActivity);
		controller.sendEvent(mActivity, EventType.GO_TO, mTime, time, -1,
				ViewType.MONTH);
	}
}
