package com.android.calendar.year;

import android.content.Context;
import android.text.format.Time;
import android.util.AttributeSet;
import android.widget.GridView;

public class YearGridView extends GridView {

	private Context mContext = null;
	private YearViewAdapter mAdapter = null;
	private Time mTime = null;
	
	public static final int CURRENT_YEAR = 1;
	public static final int NOT_CURRENT_YEAR = 0;

	public YearGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		this.mContext = context;
	}

	public YearGridView(Context context) {
		this(context, null);
		// TODO Auto-generated constructor stub
	}

	public void setTime(Time time) {
		this.mTime = time;
		if (mAdapter == null) {
			mAdapter = new YearViewAdapter(mContext, this, time);
			setAdapter(mAdapter);
		} else {
			mAdapter.setTime(time);
			mAdapter.notifyDataSetChanged();
		}
	}

	public Time getTime() {
		return this.mTime;
	}
}
