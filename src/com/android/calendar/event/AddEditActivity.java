package com.android.calendar.event;

import static android.provider.CalendarContract.EXTRA_EVENT_ALL_DAY;
import static android.provider.CalendarContract.EXTRA_EVENT_BEGIN_TIME;
import static android.provider.CalendarContract.EXTRA_EVENT_END_TIME;

import com.android.calendar.CalendarController;
import com.android.calendar.CalendarController.EventStyle;
import com.android.calendar.R;
import com.android.calendar.CalendarController.EventInfo;
import com.android.calendar.Utils;

import android.app.ActivityGroup;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.Time;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class AddEditActivity extends ActivityGroup implements OnCheckedChangeListener {

	private static final String BUNDLE_KEY_EVENT_ID = "key_event_id";
	protected static final String EXTRA_KEY_EVENT_INFO = "event_info";

	private TabHost mTabhost = null;
	private RadioGroup mRadioGroup = null;
	private Resources mResources = null;

	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		setContentView(R.layout.add_edit_activity);

		// build event info
		Intent intent = getIntent();

		// init intent0
		Intent intent0 = new Intent(intent);
		intent0.setClass(this, EditEventActivity.class);
		intent0.putExtra(Utils.EVENT_STYLE_TYPE, EventStyle.EVNET_STYLE);

		// init intent1
		Intent intent1 = new Intent(intent);
		intent1.setClass(this, EditEventActivity.class);
		intent1.putExtra(Utils.EVENT_STYLE_TYPE, EventStyle.TASK_STYLE);

		// init tabs
		mResources = getResources();
		mTabhost = (TabHost) findViewById(R.id.tabhost);
		mTabhost.setup(this.getLocalActivityManager());
		mTabhost.addTab(buildTabSpec("000", "0", R.drawable.cal_bg_top, intent0));
		mTabhost.addTab(buildTabSpec("111", "1", R.drawable.splaner_noitems,
				intent1));
		
		mRadioGroup = ((RadioGroup) findViewById(R.id.main_radio));
		mRadioGroup.setOnCheckedChangeListener(this);
		setCurrentTabBackground(mTabhost.getCurrentTab());
	}
	
	/**
	 * 
	 * @param tag
	 * @param label
	 * @param drawableId
	 * @param intent
	 * @return
	 */
	private TabSpec buildTabSpec(String tag, String label, int drawableId,
			Intent intent) {
		return mTabhost.newTabSpec(tag)
				.setIndicator(label, mResources.getDrawable(drawableId))
				.setContent(intent);
	}
	
	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		// TODO Auto-generated method stub
		int tab = -1;
		switch(checkedId){
		case R.id.radio_button0:
			tab = 0;
			break;
		case R.id.radio_button1:
			tab = 1;
			break;
		}
		if(tab != -1){
			mTabhost.setCurrentTab(tab);
			setCurrentTabBackground(tab);
		}
	}
	
	/**
	 * 
	 * @param tab
	 */
	private void setCurrentTabBackground(int tab){
		int count = mRadioGroup.getChildCount();
		for (int i = 0; i < count; i++) {
			mRadioGroup.getChildAt(i).setBackgroundResource(R.drawable.cal_bt_01_n);
		}
		mRadioGroup.getChildAt(tab).setBackgroundResource(R.drawable.cal_bt_01_p);
	}
}
