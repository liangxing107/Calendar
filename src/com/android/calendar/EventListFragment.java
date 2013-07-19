package com.android.calendar;

import static com.android.calendar.EventListActivity.EXTRA_KEY_ACTIVITY_TYPE;
import static com.android.calendar.EventListActivity.ACTIVITY_TYPE_MAIN_LIST;
import static com.android.calendar.EventListActivity.ACTIVITY_TYPE_DELETE_LIST;
import static com.android.calendar.EventListActivity.ACTIVITY_TYPE_SEARCH_LIST;

import com.android.calendar.CalendarController.EventInfo;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;

public class EventListFragment extends Fragment implements OnQueryTextListener,
		OnClickListener, CalendarController.EventHandler {

	private Activity mContext = null;
	private EventExpandableAdapter mCalendarExpandableAdapter = null;
	private ExpandableListView mExpandableListView = null;
	private SearchView mSearchView_event = null;
	private LinearLayout mLayout_selectAll = null;
	private LinearLayout mLayout_deleteHead = null;
	private CheckBox mCheckbox_selectAll = null;

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
		View view = inflater.inflate(R.layout.event_list_fragment, container,
				false);
		View emptyView = view.findViewById(R.id.emptyView);
		mExpandableListView = (ExpandableListView) view
				.findViewById(R.id.calendarExpandable);
		mExpandableListView.setEmptyView(emptyView);

		mSearchView_event = (SearchView) view
				.findViewById(R.id.searchView_event);
		mSearchView_event.setOnQueryTextListener(this);

		mLayout_selectAll = (LinearLayout) view
				.findViewById(R.id.selectAll_layout);
		mLayout_selectAll.setOnClickListener(this);
		mCheckbox_selectAll = (CheckBox) view
				.findViewById(R.id.checkBox_selectAll_box);

		mLayout_deleteHead = (LinearLayout) view
				.findViewById(R.id.delete_head_container);
		view.findViewById(R.id.btn_cancle).setOnClickListener(this);
		view.findViewById(R.id.btn_done).setOnClickListener(this);

		mCalendarExpandableAdapter = new EventExpandableAdapter(mContext,
				mExpandableListView);
		mExpandableListView.setAdapter(mCalendarExpandableAdapter);
		initView(mContext.getIntent());
		return view;
	}

	private void initView(Intent intent) {
		int activityType = intent.getIntExtra(EXTRA_KEY_ACTIVITY_TYPE,
				ACTIVITY_TYPE_MAIN_LIST);
		switch (activityType) {
		case ACTIVITY_TYPE_MAIN_LIST:
		case ACTIVITY_TYPE_SEARCH_LIST:
			mLayout_deleteHead.setVisibility(View.GONE);
			mLayout_selectAll.setVisibility(View.GONE);
			mSearchView_event.setVisibility(View.VISIBLE);
			mCalendarExpandableAdapter.setCheckMode(false);
			break;
		case ACTIVITY_TYPE_DELETE_LIST:
			mLayout_deleteHead.setVisibility(View.VISIBLE);
			mLayout_selectAll.setVisibility(View.VISIBLE);
			mSearchView_event.setVisibility(View.GONE);
			mCalendarExpandableAdapter.setCheckMode(true);
			break;
		}
	}

	@Override
	public boolean onQueryTextChange(String newText) {
		// TODO Auto-generated method stub
		mCalendarExpandableAdapter.queryEvents(newText);
		return true;
	}

	@Override
	public boolean onQueryTextSubmit(String query) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub
		switch (view.getId()) {
		case R.id.selectAll_layout:
			boolean selectAll = !mCheckbox_selectAll.isChecked();
			mCheckbox_selectAll.setChecked(selectAll);
			mCalendarExpandableAdapter.setSelectAll(selectAll);
			break;
		case R.id.btn_done:
			mCalendarExpandableAdapter.deleteEvents(mContext);
			break;
		case R.id.btn_cancle:

			break;
		}
	}

	@Override
	public long getSupportedEventTypes() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void handleEvent(EventInfo event) {
		// TODO Auto-generated method stub

	}

	@Override
	public void eventsChanged() {
		// TODO Auto-generated method stub

	}

}
