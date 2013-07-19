package com.android.calendar.agenda;

import com.android.calendar.R;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;

public class SearchTopFragment extends Fragment implements OnQueryTextListener {

	public static enum SEARCH_TYPE {
		TYPE_AGENDA, TYPE_TASK
	}

	public final static String KEY_SEARCH_TYPE = "search_type";

	private SEARCH_TYPE mType = SEARCH_TYPE.TYPE_AGENDA;
	private Time mTime = new Time();
	private SearchView mSearchView = null;

	public SearchTopFragment() {
		this(0, null);
	}

	public SearchTopFragment(long timemillis, SEARCH_TYPE type) {
		if (timemillis == 0) {
			mTime.setToNow();
		} else {
			mTime.set(timemillis);
		}
		if (type != null) {
			mType = type;
		}
	}

	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View v = inflater.inflate(R.layout.search_top_fragment, container,
				false);
		int imgId = mType == SEARCH_TYPE.TYPE_AGENDA ? R.drawable.calendar_icon_selector
				: R.drawable.calendar_icon_task_01_selector;
		ImageView imgIcon = (ImageView) v.findViewById(R.id.img_searchIcon);
		imgIcon.setImageResource(imgId);

		mSearchView = (SearchView) v.findViewById(R.id.img_searchView);
		mSearchView.setOnQueryTextListener(this);
		return v;
	}

	public boolean onQueryTextChange(String newText) {
		// TODO Auto-generated method stub
		System.out.println("newText = " + newText);
		return false;
	}

	public boolean onQueryTextSubmit(String query) {
		// TODO Auto-generated method stub
		System.out.println("query = " + query);
		return false;
	}
}
