package com.android.calendar;

import static com.android.calendar.Utils.EVENT_URI;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.CalendarContract.Events;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ExpandableListView.OnChildClickListener;

public class EventExpandableAdapter extends BaseExpandableListAdapter
		implements OnChildClickListener {

	private static final int QUERY_TOKEN_JULIAN_DAY = 0;
	private static final int QUERY_TOKEN_EVENT_ID = 1;
	private static final int MSG_FRESH_CHILD = 0;

	// group
	public static final int GROUP_LAYOUT = R.layout.event_select_group_layout;
	private static final int CHILD_LAYOUT = R.layout.event_select_child_item;
	public static final String[] PROJECTS = { Events._ID, Events.TITLE,
			Events.DTSTART, Events.DTEND };
	private static final int EVENT_ID_INDEX = 0;
	private static final int EVENT_TITLE_INDEX = 1;
	private static final int EVENT_START_INDEX = 2;
	private static final int EVENT_END_INDEX = 3;

	private final String mWaitTitle;
	private final String mSearchMsgFmt;
	
	private Activity mContext = null;
	private ExpandableListView mExpandableListView = null;
	private ContentResolver mResolver = null;
	private LayoutInflater mInflater = null;
	private EventQueryHandler mQueryHandler = null;
	private String mTimeRangeFormat = null;
	private Time mTime = new Time();
	private boolean mIsCheckMode = false;

	private ArrayList<ArrayList<ChildViewData>> mChildViewDataList = new ArrayList<ArrayList<ChildViewData>>();

	public EventExpandableAdapter(Activity context,
			ExpandableListView expandableListView) {
		super();
		// TODO Auto-generated constructor stub
		this.mContext = context;
		this.mExpandableListView = expandableListView;
		this.mResolver = context.getContentResolver();
		mExpandableListView.setOnChildClickListener(this);

		mInflater = LayoutInflater.from(context);
		mTime.setToNow();
		// this.mTimeRangeFormat = context.getString(R.string.event_time_range);
		this.mWaitTitle = context.getString(R.string.awaiting);
		this.mSearchMsgFmt = context
				.getString(R.string.directory_searching_fmt);

		this.mQueryHandler = new EventQueryHandler(mResolver);
		startEventsQuery(QUERY_TOKEN_JULIAN_DAY, null, EVENT_URI, PROJECTS,
				null, null, null);
	}

	@Override
	public ChildViewData getChild(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return mChildViewDataList.get(groupPosition).get(childPosition);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ChildViewData data = mChildViewDataList.get(groupPosition).get(
				childPosition);
		ChildViewHolder holder = null;
		if (convertView == null) {
			convertView = mInflater.inflate(CHILD_LAYOUT, null);
			holder = new ChildViewHolder();
			holder.mTv_title = (TextView) convertView
					.findViewById(R.id.tv_event_item_title);
			holder.mTv_timeRange = (TextView) convertView
					.findViewById(R.id.tv_event_item_time);
			holder.mCheckBox = (CheckBox) convertView
					.findViewById(R.id.checkBox_select_event);
			holder.mImg_icon = (ImageView) convertView
					.findViewById(R.id.img_event_item_typeImg);
			convertView.setTag(holder);
		} else {
			holder = (ChildViewHolder) convertView.getTag();
		}
		if (data.title == null || data.timeRange == null) {
			new LoadChildThread(mResolver, data, holder).start();
		} else {
			holder.mTv_timeRange.setText(data.timeRange);
			holder.mTv_title.setText(data.title);
		}
		if (mIsCheckMode) {
			holder.mCheckBox.setVisibility(View.VISIBLE);
			holder.mImg_icon.setVisibility(View.GONE);
		} else {
			holder.mCheckBox.setVisibility(View.GONE);
			holder.mImg_icon.setVisibility(View.VISIBLE);
		}
		holder.mCheckBox.setChecked(data.checked);
		return convertView;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		// TODO Auto-generated method stub
		int childCount = 0;
		if (mChildViewDataList.size() > groupPosition) {
			ArrayList<ChildViewData> dataList = mChildViewDataList
					.get(groupPosition);
			if (dataList != null) {
				childCount = dataList.size();
			}
		}
		System.out.println("groupPosition = " + groupPosition
				+ ", childCount = " + childCount);
		return childCount;
	}

	@Override
	public Object getGroup(int groupPosition) {
		// TODO Auto-generated method stub
		return groupPosition;
	}

	@Override
	public int getGroupCount() {
		// TODO Auto-generated method stub
		return mChildViewDataList.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		GroupViewHolder holder = null;
		if (convertView == null) {
			convertView = mInflater.inflate(GROUP_LAYOUT, null);
			holder = new GroupViewHolder();
			holder.tv_groupTime = (TextView) convertView
					.findViewById(R.id.tv_group_time);
			holder.img_groupIndicator = (ImageView) convertView
					.findViewById(R.id.imgView_indicator);
			convertView.setTag(holder);
		} else {
			holder = (GroupViewHolder) convertView.getTag();
		}
		holder.img_groupIndicator
				.setImageResource(isExpanded ? R.drawable.splanner_list_icon_expandable_open
						: R.drawable.splanner_list_icon_expandable_close);

		ArrayList<ChildViewData> childList = mChildViewDataList
				.get(groupPosition);
		if (childList.size() > 0) {
			int julianday = childList.get(0).julianday;
			mTime.setJulianDay(julianday);
			holder.tv_groupTime.setText(Utils.formatToDate(mContext,
					mTime.toMillis(false)));
			return convertView;
		}
		return null;
	}

	@Override
	public boolean hasStableIds() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isChildSelectable(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return true;
	}

	class EventQueryHandler extends AsyncQueryHandler {

		public EventQueryHandler(ContentResolver cr) {
			super(cr);
			// TODO Auto-generated constructor stub
		}

		@Override
		protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
			// TODO Auto-generated method stub
			if (cursor != null) {
				if (QUERY_TOKEN_JULIAN_DAY == token) {
					ArrayList<Integer> eventJulianDays = new ArrayList<Integer>();
					while (cursor.moveToNext()) {
						long dstart = cursor.getLong(EVENT_START_INDEX);
						int julianDay = Time.getJulianDay(dstart, mTime.gmtoff);
						if (!eventJulianDays.contains(julianDay)) {
							eventJulianDays.add(julianDay);
						}
					}
					cursor.close();
					// sort julianDays
					Utils.arrayListSort(eventJulianDays);
					startEventsQuery(QUERY_TOKEN_EVENT_ID, eventJulianDays,
							EVENT_URI, PROJECTS, null, null, null);
				} else if (QUERY_TOKEN_EVENT_ID == token) {
					@SuppressWarnings("unchecked")
					ArrayList<Integer> eventJulianDays = (ArrayList<Integer>) cookie;
					TreeMap<Integer, ArrayList<Long>> eventJulianDayEvents = new TreeMap<Integer, ArrayList<Long>>();
					while (cursor.moveToNext()) {
						long dstart = cursor.getLong(EVENT_START_INDEX);
						long eventId = cursor.getLong(EVENT_ID_INDEX);
						int julianDay = Time.getJulianDay(dstart, mTime.gmtoff);
						if (eventJulianDays.contains(julianDay)) {
							if (!eventJulianDayEvents.containsKey(julianDay)) {
								ArrayList<Long> eventsId = new ArrayList<Long>();
								eventJulianDayEvents.put(julianDay, eventsId);
							}
							ArrayList<Long> eventsId = eventJulianDayEvents
									.get(julianDay);
							eventsId.add(eventId);
						}
					}
					cursor.close();
					resetData(eventJulianDayEvents); // import
					notifyDataSetChanged();
				}
			}

			Utils.hideProgress();
			for (int i = 0; i < EventExpandableAdapter.this.getGroupCount(); i++) {
				mExpandableListView.expandGroup(i);
			}
		}
	}

	class GroupViewHolder {
		TextView tv_groupTime;
		ImageView img_groupIndicator;
	}

	class ChildViewHolder implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		protected static final String DATA_KEY = "child_view";
		TextView mTv_timeRange;
		TextView mTv_title;
		CheckBox mCheckBox;
		ImageView mImg_icon;
	}

	class ChildViewData implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		protected static final String DATA_KEY = "child_data";
		int groupPosition = -1;
		int julianday;
		long eventId;
		String timeRange;
		String title;
		boolean checked;
	}

	class LoadChildThread extends Thread {

		private ContentResolver mResolver = null;
		private ChildViewData mData = null;
		private ChildViewHolder mHolder = null;

		public LoadChildThread(ContentResolver cr, ChildViewData data,
				ChildViewHolder holder) {
			super();
			// TODO Auto-generated constructor stub
			mResolver = cr;
			mData = data;
			mHolder = holder;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			Cursor cursor = mResolver.query(
					ContentUris.withAppendedId(EVENT_URI, mData.eventId),
					PROJECTS, null, null, null);
			cursor.moveToFirst();
			long dstart = cursor.getLong(EVENT_START_INDEX);
			// long dend = cursor.getLong(EVENT_END_INDEX);
			mData.title = cursor.getString(EVENT_TITLE_INDEX);
			// mData.timeRange = String.format(mTimeRangeFormat,
			// Utils.formatTimeMillis(mContext, dstart),
			// Utils.formatTimeMillis(mContext, dend)).toUpperCase();
			cursor.close();
			mData.timeRange = DateUtils.formatDateTime(mContext, dstart,
					DateUtils.FORMAT_ABBREV_MONTH | DateUtils.FORMAT_SHOW_DATE
					| DateUtils.FORMAT_SHOW_YEAR);

			// send message
			Message msg = mHandler.obtainMessage(MSG_FRESH_CHILD);
			Bundle data = new Bundle();
			data.putSerializable(ChildViewData.DATA_KEY, mData);
			data.putSerializable(ChildViewHolder.DATA_KEY, mHolder);
			msg.setData(data);
			mHandler.sendMessage(msg);
		}
	}

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			if (msg.what == MSG_FRESH_CHILD) {
				Bundle data = msg.getData();
				ChildViewData childData = (ChildViewData) data
						.getSerializable(ChildViewData.DATA_KEY);
				ChildViewHolder childView = (ChildViewHolder) data
						.getSerializable(ChildViewHolder.DATA_KEY);
				childView.mTv_timeRange.setText(childData.timeRange);
				childView.mTv_title.setText(childData.title);
				return;
			}
			super.handleMessage(msg);
		}
	};

	private void startEventsQuery(int token, Object cookie, Uri uri,
			String[] projects, String query, String[] selectArgs, String orderBy) {
		String selection = query == null ? null : buildQuery(query);
		Utils.showProgress(mContext, mWaitTitle,
				String.format(mSearchMsgFmt, query == null ? "" : query), null);
		mQueryHandler.startQuery(token, cookie, uri, projects, selection,
				selectArgs, orderBy);
	}

	public void setCheckMode(boolean isCheckMode) {
		this.mIsCheckMode = isCheckMode;
		notifyDataSetChanged();
	}

	public void setSelectAll(boolean isCheckAll) {
		for (int i = 0; i < mChildViewDataList.size(); i++) {
			ArrayList<ChildViewData> dataList = mChildViewDataList.get(i);
			for (int j = 0; j < dataList.size(); j++) {
				dataList.get(j).checked = isCheckAll;
			}
		}
		notifyDataSetChanged();
	}

	// add null data
	private void resetData(
			TreeMap<Integer, ArrayList<Long>> eventJulianDayEvents) {
		clearCacheData();
		notifyDataSetChanged();
		Iterator<Integer> keyIterator = eventJulianDayEvents.keySet()
				.iterator();
		ArrayList<Long> eventList = null;
		ArrayList<ChildViewData> dataList;
		ChildViewData data = null;
		int julianday = 0;
		while (keyIterator.hasNext()) {
			julianday = keyIterator.next();
			eventList = eventJulianDayEvents.get(julianday);
			dataList = new ArrayList<EventExpandableAdapter.ChildViewData>();
			for (int i = 0; i < eventList.size(); i++) {
				data = new ChildViewData();
				data.julianday = julianday;
				data.eventId = eventList.get(i);
				dataList.add(data);
			}
			mChildViewDataList.add(dataList);
		}
	}

	@Override
	public boolean onChildClick(ExpandableListView listView, View view,
			int groupPosition, int childPosition, long id) {
		// TODO Auto-generated method stub
		ChildViewData data = getChild(groupPosition, childPosition);
		data.checked = !data.checked;
		notifyDataSetChanged();
		return true;
	}

	public void deleteEvents(Context context) {
		new AlertDialog.Builder(context)
				.setTitle(R.string.delete_label)
				.setMessage(R.string.delete_tip)
				.setPositiveButton(R.string.delete_label,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int position) {
								// TODO Auto-generated method stub
								new DeleteTask(mContext).execute();
							}
						})
				.setNegativeButton(R.string.cancel,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								// TODO Auto-generated method stub
								mContext.finish();
							}
						}).create().show();
	}

	public class DeleteTask extends AsyncTask<Void, ChildViewData, Void> {

		private Activity mContext = null;
		private ContentResolver mResolver = null;
		private String mStr_delete = null;
		private String mStr_delete_progress = null;

		public DeleteTask(Activity context) {
			mContext = context;
			mResolver = context.getContentResolver();
			mStr_delete = context.getString(R.string.delete_label);
			mStr_delete_progress = context.getString(R.string.delete_progress);
		}

		@Override
		protected void onPostExecute(Void result) {
			hideDeleteProgress();
			queryEvents(null);
			// mContext.finish();
		}

		@Override
		protected void onProgressUpdate(ChildViewData... values) {
			// TODO Auto-generated method stub
			ChildViewData data = values[0];
			showDeleteProgress(mContext, mStr_delete,
					String.format(mStr_delete_progress, data.title));
		}

		@Override
		protected Void doInBackground(Void... params) {
			ChildViewData tempDat = null;
			for (int i = 0; i < getGroupCount(); i++) {
				for (int j = 0; j < getChildrenCount(i); j++) {
					tempDat = getChild(i, j);
					if (tempDat.checked) {
						publishProgress(tempDat); // notify progress
						try {
							Utils.deleteCalendarEventById(mResolver,
									tempDat.eventId);
							Thread.sleep(1000);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
			return null;
		}
	}

	public static void showDeleteProgress(Context context, String title,
			String message) {
		Utils.showProgress(context, title, message, null);
	}

	public static void hideDeleteProgress() {
		Utils.hideProgress();
	}

	public void queryEvents(String query) {
		startEventsQuery(QUERY_TOKEN_JULIAN_DAY, null, EVENT_URI, PROJECTS,
				query, null, null);
	}

	private void clearCacheData() {
		for (int i = 0; i < mChildViewDataList.size(); i++) {
			mChildViewDataList.get(i).clear();
		}
		mChildViewDataList.clear();
	}

	private static final String buildQuery(String queryText) {
		return Events.TITLE + " like " + "\"%" + queryText + "%\"";
	}
}
