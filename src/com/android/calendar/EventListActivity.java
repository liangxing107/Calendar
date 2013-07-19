package com.android.calendar;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.os.Bundle;

public class EventListActivity extends Activity {

	public static final String EXTRA_KEY_ACTIVITY_TYPE = "activity_type";
	public static final int ACTIVITY_TYPE_MAIN_LIST = 0;
	public static final int ACTIVITY_TYPE_DELETE_LIST = 1;
	public static final int ACTIVITY_TYPE_SEARCH_LIST = 2;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.event_list_activity);

		FragmentTransaction ft = getFragmentManager().beginTransaction();
		ft.replace(R.id.fragment, new EventListFragment());
		ft.commit();
	}
}
