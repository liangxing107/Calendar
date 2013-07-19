/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.calendar.selectcalendars;

import java.util.ArrayList;

import com.android.calendar.AsyncQueryService;
import com.android.calendar.CalendarController.EventInfo;
import com.android.calendar.CalendarController.EventType;
import com.android.calendar.CalendarController.ViewType;
import com.android.calendar.AllInOneActivity;
import com.android.calendar.LogUtil;
import com.android.calendar.R;
import com.android.calendar.CalendarController;
import com.android.calendar.Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract.Events;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

///M:#ClearAllEvents#
public class SelectClearableCalendarsFragment extends Fragment
        implements AdapterView.OnItemClickListener/*, CalendarController.EventHandler*/ {

    private static final String TAG = "Calendar";
    private static final String IS_PRIMARY = "\"primary\"";
    private static final String SELECTION = Calendars.SYNC_EVENTS + "=?";
    private static final String[] SELECTION_ARGS = new String[] {"1"};

    private static final String[] PROJECTION = new String[] {
        Calendars._ID,
        Calendars.ACCOUNT_NAME,
        Calendars.OWNER_ACCOUNT,
        Calendars.CALENDAR_DISPLAY_NAME,
        Calendars.CALENDAR_COLOR,
        Calendars.VISIBLE,
        Calendars.SYNC_EVENTS,
        "(" + Calendars.ACCOUNT_NAME + "=" + Calendars.OWNER_ACCOUNT + ") AS " + IS_PRIMARY,
      };
    private static int mDeleteToken;
    private static int mQueryToken;
    private static int mCalendarItemLayout = R.layout.mini_calendar_item;

    private View mView = null;
    private ListView mList;
    private SelectCalendarsSimpleAdapter mAdapter;
    private Activity mContext;
    private AsyncQueryService mService;
    private Cursor mCursor;

    private Button mBtnDelete;
    private AlertDialog mAlertDialog;
    private ArrayList<Long> mCalendarIds = new ArrayList<Long>();
    
    public SelectClearableCalendarsFragment() {
    }

    public SelectClearableCalendarsFragment(int itemLayout) {
        mCalendarItemLayout = itemLayout;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity;
        mService = new AsyncQueryService(activity) {
            @Override
            protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
                mAdapter.changeCursor(cursor);
                //because we reused the SelectCalendarsSimpleAdapter, its info is from database(not what we want),
                //so we need to reset the data here.
                mAdapter.setAllItemInvisible();
                mCursor = cursor;
                if(!mCalendarIds.isEmpty()) {
                    mCalendarIds.clear();
                    mBtnDelete.setEnabled(false);
                }
            }
            
            @Override
            protected void onDeleteComplete(int token, Object cookie, int result) {
                LogUtil.i(TAG, "Clear all events,onDeleteComplete.  result(delete number)="+result);
                if(mProgressDialog != null && mProgressDialog.isShowing()){
                    mProgressDialog.cancel();
                    LogUtil.i(TAG, "Cancel Progress dialog.");
                }

                Toast.makeText(mContext, R.string.delete_completed, Toast.LENGTH_LONG).show();

                CalendarController controller = CalendarController.getAllinOneController();
                if (controller != null) {
                    LogUtil.d(TAG, "Clear all events, sendEvent EVENTS_CLEARED.");
                    /*
                     * TODO: need to fix one special case:
                     * when delete events is going on, lock screen--unlock screen,
                     * if the delete is still not complete, wait until it complete and press back key at once.
                     * in this condition, the agenda list view will still not be updated.
                     */
                    controller.sendEvent(this, EventType.EVENTS_CLEARED, null, null, -1, ViewType.AGENDA);
                }
                
                super.onDeleteComplete(token, cookie, result);
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        mView = inflater.inflate(R.layout.select_calendars_to_clear_fragment, null);
        mList = (ListView)mView.findViewById(R.id.list);

        // Hide the Calendars to Sync button on tablets for now.
        // Long terms stick it in the list of calendars
        if (Utils.getConfigBool(getActivity(), R.bool.multiple_pane_config)) {
            // Don't show dividers on tablets
            mList.setDivider(null);
            View v = mView.findViewById(R.id.manage_sync_set);
            if (v != null) {
                v.setVisibility(View.GONE);
            }
        }
        mBtnDelete = (Button) mView.findViewById(R.id.btn_ok);
        if(mBtnDelete != null){
            mBtnDelete.setOnClickListener(mClickListener);
            mBtnDelete.setEnabled(false);
        }
        Button cancel = (Button) mView.findViewById(R.id.btn_cancel);
        if(cancel != null){
            cancel.setOnClickListener(mClickListener);
        }
        return mView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mAdapter = new SelectCalendarsSimpleAdapter(mContext, mCalendarItemLayout, null);
        mList.setAdapter(mAdapter);
        mList.setOnItemClickListener(this);
    }
    

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mProgressDialog = createProgressDialog();
    }
    
    @Override
    public void onResume() {
        super.onResume();
        mQueryToken = mService.getNextToken();
        mService.startQuery(mQueryToken, null, Calendars.CONTENT_URI, PROJECTION, SELECTION,
                SELECTION_ARGS, Calendars.ACCOUNT_NAME);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mCalendarIds != null && !mCalendarIds.isEmpty()) {
            mCalendarIds.clear();
        }
        dismissAlertDialog();
        
        if(mProgressDialog!=null){
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }
    
    @Override
    public void onDetach() {
        super.onDetach();
        if (mCursor != null) {
            mAdapter.changeCursor(null);
            mCursor.close();
            mCursor = null;
        }
    }

    private OnClickListener mClickListener = new OnClickListener() {
        public void onClick(View view) {
            switch (view.getId()) {
            case R.id.btn_ok:
                LogUtil.d(TAG, "Clear all events, ok");
                AlertDialog dialog = new AlertDialog.Builder(mContext)
                .setTitle(R.string.delete_label)
                .setMessage(R.string.clear_all_selected_events_title)
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setNegativeButton(android.R.string.cancel, null).create();
                
                dialog.setButton(DialogInterface.BUTTON_POSITIVE,
                        mContext.getText(R.string.delete_certain),
                        mClearEventsDialogListener);
                dialog.show();
                mAlertDialog = dialog;
                break;
            case R.id.btn_cancel:
                LogUtil.d(TAG, "Clear all events, cancel");
                mContext.finish();
                break;
            }
        }
    };

    private DialogInterface.OnClickListener mClearEventsDialogListener =
            new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int button) {
            LogUtil.d(TAG, "Clear all events, to delete.");
            dismissAlertDialog();
            String selection = "_id>0";
            selection = getSelection(selection);
            mDeleteToken = mService.getNextToken();
            //mContext.finish();
            if(mProgressDialog != null){
                mProgressDialog.show();
            }
            LogUtil.i(TAG, "Clear all events, start delete, selection="+selection);
            mService.startDelete(mDeleteToken, null, CalendarContract.Events.CONTENT_URI, selection, null, 0);

        }
    };
    
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)  {
        if (mAdapter == null || mAdapter.getCount() <= position) {
            return;
        }
        saveCalendarId(position);
    }

    public void saveCalendarId(int position) {
        Log.d(TAG, "Toggling calendar at " + position);
        long calId = mAdapter.getItemId(position);
        int selected = mAdapter.getVisible(position)^1;
        
        mAdapter.setVisible(position, selected);
        if(selected != 0){
            mCalendarIds.add(calId);
        } else {
            if(mCalendarIds.contains(calId)) {
                mCalendarIds.remove(calId);
            }
        }
        
        if(!mCalendarIds.isEmpty()){
            mBtnDelete.setEnabled(true);
        } else {
            mBtnDelete.setEnabled(false);
        }
    }
    
    private String getSelection(String selection) {
        String tmpSelection = "";
        for (Long calId : mCalendarIds) {
            tmpSelection += " OR " + Events.CALENDAR_ID + "=" + String.valueOf(calId);
        }
        if(!TextUtils.isEmpty(tmpSelection)){
            tmpSelection = tmpSelection.replaceFirst(" OR ", "");
            return selection + " AND ("+tmpSelection+")";
        } else {
            return selection;
        }
    }
    
    private void dismissAlertDialog() {
        if (mAlertDialog != null) {
            mAlertDialog.dismiss();
        }
    }
    
    ProgressDialog mProgressDialog = null ;
    private ProgressDialog createProgressDialog(){
        ProgressDialog dialog = new ProgressDialog(mContext);
        dialog.setMessage(getString(R.string.wait_deleting_tip));
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        return dialog;
    }

    public boolean isProgressDialogShowing(){
        if(mProgressDialog != null){
            return mProgressDialog.isShowing(); 
        } else {
            return false;
        }
    }
}
