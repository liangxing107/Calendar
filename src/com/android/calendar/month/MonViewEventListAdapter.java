package com.android.calendar.month;

import java.util.ArrayList;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.calendar.CalendarController;
import com.android.calendar.Event;
import com.android.calendar.R;
import com.android.calendar.CalendarController.EventType;

public class MonViewEventListAdapter extends BaseAdapter implements
		OnItemClickListener {

	private Context mContext = null;
	private LayoutInflater mInflater = null;
	private CalendarController mCalendarController = null;
	private ArrayList<Event> mTappedDayEvents = null;
	private String mTimeRangeFormat = null;
	private String mTimeRange = null;

	public MonViewEventListAdapter(Context context, ListView listView,
			ArrayList<Event> tappedDayEvents) {
		super();
		// TODO Auto-generated constructor stub
		this.mInflater = LayoutInflater.from(context);
		this.mContext = context;
		this.mTimeRangeFormat = context.getString(R.string.event_time_range);
		this.mCalendarController = CalendarController.getInstance(mContext);
		listView.setOnItemClickListener(this);

		// set cursor
		setEventList(tappedDayEvents, false);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		if (mTappedDayEvents == null) {
			return 0;
		}
		return mTappedDayEvents.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parentView) {
		// TODO Auto-generated method stub
		ViewHolder viewHolder = null;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.month_view_event_item,
					null);
			viewHolder = new ViewHolder();
			viewHolder.mTv_title = (TextView) convertView
					.findViewById(R.id.tv_event_item_title);
			viewHolder.mTv_time = (TextView) convertView
					.findViewById(R.id.tv_event_item_time);
			viewHolder.mImg_typeIcon = (ImageView) convertView
					.findViewById(R.id.img_event_item_typeImg);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		Event event = mTappedDayEvents.get(position);
		mTimeRange = String.format(mTimeRangeFormat,
				formatTimeMillis(mContext, event.startMillis),
				formatTimeMillis(mContext, event.endMillis));

		viewHolder.mTv_time.setText(mTimeRange.toUpperCase());
		viewHolder.mTv_title.setText(event.title);

		return convertView;
	}

	/**
	 * format a time millis
	 * 
	 * @param context
	 * @param timeMillis
	 * @return
	 */
	public static final String formatTimeMillis(Context context, long timeMillis) {
		return DateUtils.formatDateTime(context, timeMillis,
				DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_12HOUR);
	}

	public void setEventList(ArrayList<Event> tappedDayEvents, boolean doFresh) {
		this.mTappedDayEvents = tappedDayEvents;
		if (doFresh) {
			notifyDataSetChanged();
		}
	}

	class ViewHolder {
		TextView mTv_title;
		TextView mTv_time;
		ImageView mImg_typeIcon;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		if (mTappedDayEvents != null && mTappedDayEvents.size() > position) {
			Event event = mTappedDayEvents.get(position);
			mCalendarController.sendEventRelatedEvent(mContext,
					EventType.VIEW_EVENT, event.id, event.startMillis,
					event.endMillis, 0, 0, event.startMillis);
		}
	}
}
