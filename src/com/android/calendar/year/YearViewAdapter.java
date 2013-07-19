package com.android.calendar.year;

import java.util.HashMap;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;

import com.android.calendar.R;

public class YearViewAdapter extends BaseAdapter {

	private static final int MONTH_COUNT = 12;
	
	private Resources mResources = null;
	private LayoutInflater mInflater = null;
	private Time mTime = null;
	private GridView mGridView = null;
	private int mOrientation = Configuration.ORIENTATION_PORTRAIT;

	public YearViewAdapter(Context context, GridView gridView, Time time) {
		super();
		// TODO Auto-generated constructor stub
		this.mInflater = LayoutInflater.from(context);
		this.mResources = context.getResources();
		this.mGridView = gridView;

		this.setTime(time);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		if (mTime == null) {
			return 0;
		}
		return MONTH_COUNT;
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@SuppressWarnings("unchecked")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		YearByMonthItem monthView = null;
		HashMap<String, Integer> params = null;
		mOrientation = mResources.getConfiguration().orientation;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.month_view_item, null);
			monthView = (YearByMonthItem) convertView.findViewById(R.id.monthView);
			convertView.setTag(monthView);
		} else {
			monthView = (YearByMonthItem) convertView.getTag();
			params = (HashMap<String, Integer>)monthView.getTag();
		}
		if (params == null) {
			params = new HashMap<String, Integer>();
        }
		Time tempTime = new Time(mTime);
		tempTime.month = position;
		tempTime.monthDay = 1;
		tempTime.normalize(false);
		
		int lines = mGridView.getCount() / mGridView.getNumColumns();
		params.put(YearByMonthItem.VIEW_PARAMS_VIEW_HEIGHT, parent.getHeight() / lines);
		params.put(YearByMonthItem.VIEW_PARAMS_VIEW_ORIENTATION, mOrientation);
		monthView.setViewParams(params, tempTime);

		return convertView;
	}

	public Time getTime() {
		return mTime;
	}

	public void setTime(Time time) {
		this.mTime = time;
	}
}
