/*
 * Copyright (C) 2007 The Android Open Source Project
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

package com.android.calendar.agenda;

import com.android.calendar.R;
import com.android.calendar.Utils;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.provider.CalendarContract.Attendees;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.view.View;
import android.widget.ImageView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import java.util.Formatter;
import java.util.Locale;
import java.util.TimeZone;

public class AgendaAdapter extends ResourceCursorAdapter {
    private String mNoTitleLabel;
    private Resources mResources;
	// private int mDeclinedColor;
	// private int mStandardColor;
	// private int mWhereColor;
	// private int mWhereDeclinedColor;
    // Note: Formatter is not thread safe. Fine for now as it is only used by the main thread.
    private Formatter mFormatter;
    private StringBuilder mStringBuilder;
    private float mScale;

	// chenzhentao add
	private int mTitleColor;
	private int mTimeColor;

    private Runnable mTZUpdater = new Runnable() {
        @Override
        public void run() {
            notifyDataSetChanged();
        }
    };

    static class ViewHolder {

        public static final int DECLINED_RESPONSE = 0;
        public static final int TENTATIVE_RESPONSE = 1;
        public static final int ACCEPTED_RESPONSE = 2;

        /* Event */
		TextView mTv_title;
		TextView mTv_time;
		ImageView mImg_typeIcon;
        long instanceId;
        long startTimeMilli;
        boolean allDay;
        boolean grayed;
        int julianDay;
    }

    public AgendaAdapter(Context context, int resource) {
        super(context, resource, null);

        mResources = context.getResources();
        mNoTitleLabel = mResources.getString(R.string.no_title_label);
		// mDeclinedColor = mResources.getColor(R.color.agenda_item_declined_color);
		// mStandardColor = mResources.getColor(R.color.agenda_item_standard_color);
		// mWhereDeclinedColor = mResources.getColor(R.color.agenda_item_where_declined_text_color);
		// mWhereColor = mResources.getColor(R.color.agenda_item_where_text_color);
        mStringBuilder = new StringBuilder(50);
        mFormatter = new Formatter(mStringBuilder, Locale.getDefault());

		// chenzhentao add
		mTitleColor = mResources.getColor(R.color.white);
		mTimeColor = mResources.getColor(R.color.event_time_text_color);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = null;

        // Listview may get confused and pass in a different type of view since
        // we keep shifting data around. Not a big problem.
        Object tag = view.getTag();
        if (tag instanceof ViewHolder) {
            holder = (ViewHolder) view.getTag();
        }

        if (holder == null) {
            holder = new ViewHolder();
            view.setTag(holder);
			holder.mTv_title = (TextView) view.findViewById(R.id.tv_event_item_title);
			holder.mTv_time = (TextView) view.findViewById(R.id.tv_event_item_time);
			holder.mImg_typeIcon = (ImageView) view.findViewById(R.id.img_event_item_typeImg);
        }

        holder.startTimeMilli = cursor.getLong(AgendaWindowAdapter.INDEX_BEGIN);
        // Fade text if event was declined and set the color chip mode (response
        boolean allDay = cursor.getInt(AgendaWindowAdapter.INDEX_ALL_DAY) != 0;
        holder.allDay = allDay;
        int selfAttendeeStatus = cursor.getInt(AgendaWindowAdapter.INDEX_SELF_ATTENDEE_STATUS);
        if (selfAttendeeStatus == Attendees.ATTENDEE_STATUS_DECLINED) {
			// holder.mTv_title.setTextColor(mDeclinedColor); // chenzhentao delete
			// holder.mTv_time.setTextColor(mWhereDeclinedColor);
        } else {
			// holder.mTv_title.setTextColor(mStandardColor); // chenzhentao delete
			// holder.mTv_time.setTextColor(mWhereColor);
            }
		holder.mTv_title.setTextColor(mTitleColor);
		holder.mTv_time.setTextColor(mTimeColor);

        // Deal with exchange events that the owner cannot respond to
        int canRespond = cursor.getInt(AgendaWindowAdapter.INDEX_CAN_ORGANIZER_RESPOND);
        if (canRespond == 0) {
            String owner = cursor.getString(AgendaWindowAdapter.INDEX_OWNER_ACCOUNT);
            String organizer = cursor.getString(AgendaWindowAdapter.INDEX_ORGANIZER);
            if (owner.equals(organizer)) {
				// holder.mTv_title.setTextColor(mStandardColor); // chenzhentao delete
				// holder.mTv_time.setTextColor(mStandardColor);
            }
        }

		TextView title = holder.mTv_title;
		TextView when = holder.mTv_time;

        holder.instanceId = cursor.getLong(AgendaWindowAdapter.INDEX_INSTANCE_ID);


        // What
        String titleString = cursor.getString(AgendaWindowAdapter.INDEX_TITLE);
        if (titleString == null || titleString.length() == 0) {
            titleString = mNoTitleLabel;
        }
        title.setText(titleString);

        // When
        long begin = cursor.getLong(AgendaWindowAdapter.INDEX_BEGIN);
        long end = cursor.getLong(AgendaWindowAdapter.INDEX_END);
        String eventTz = cursor.getString(AgendaWindowAdapter.INDEX_TIME_ZONE);
        int flags = 0;
        String whenString;
        // It's difficult to update all the adapters so just query this each
        // time we need to build the view.
        String tzString = Utils.getTimeZone(context, mTZUpdater);
        if (allDay) {
            tzString = Time.TIMEZONE_UTC;
        } else {
            flags = DateUtils.FORMAT_SHOW_TIME;
        }
        if (DateFormat.is24HourFormat(context)) {
            flags |= DateUtils.FORMAT_24HOUR;
        }
        mStringBuilder.setLength(0);
        whenString = DateUtils.formatDateRange(context, mFormatter, begin, end, flags, tzString)
                .toString();
        if (!allDay && !TextUtils.equals(tzString, eventTz)) {
            String displayName;
            // Figure out if this is in DST
            Time date = new Time(tzString);
            date.set(begin);

            TimeZone tz = TimeZone.getTimeZone(tzString);
            if (tz == null || tz.getID().equals("GMT")) {
                displayName = tzString;
            } else {
                displayName = tz.getDisplayName(date.isDst != 0, TimeZone.LONG);
            }
            whenString += " (" + displayName + ")";
        }
        when.setText(whenString);

   /* Recurring event icon is removed
        String rrule = cursor.getString(AgendaWindowAdapter.INDEX_RRULE);
        if (!TextUtils.isEmpty(rrule)) {
            when.setCompoundDrawablesWithIntrinsicBounds(null, null,
                    context.getResources().getDrawable(R.drawable.ic_repeat_dark), null);
            when.setCompoundDrawablePadding(5);
        } else {
            when.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        } */

        /*
        // Repeating info
        View repeatContainer = view.findViewById(R.id.repeat_icon);
        String rrule = cursor.getString(AgendaWindowAdapter.INDEX_RRULE);
        if (!TextUtils.isEmpty(rrule)) {
            repeatContainer.setVisibility(View.VISIBLE);
        } else {
            repeatContainer.setVisibility(View.GONE);
        }
        */

        /*
        // Reminder
        boolean hasAlarm = cursor.getInt(AgendaWindowAdapter.INDEX_HAS_ALARM) != 0;
        if (hasAlarm) {
            updateReminder(view, context, begin, cursor.getLong(AgendaWindowAdapter.INDEX_EVENT_ID));
        }
        */

        // Where
        /*
        String whereString = cursor.getString(AgendaWindowAdapter.INDEX_EVENT_LOCATION);
        if (whereString != null && whereString.length() > 0) {
            where.setVisibility(View.VISIBLE);
            where.setText(whereString);
        } else {
            where.setVisibility(View.GONE);
        }
        */
    }

    /*
    public static void updateReminder(View view, Context context, long begin, long eventId) {
        ContentResolver cr = context.getContentResolver();
        Uri uri = Reminders.CONTENT_URI;
        String where = String.format(REMINDERS_WHERE, eventId);

        Cursor remindersCursor = cr.query(uri, REMINDERS_PROJECTION, where, null, null);
        if (remindersCursor != null) {
            LayoutInflater inflater =
                    (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            LinearLayout parent = (LinearLayout) view.findViewById(R.id.reminders_container);
            parent.removeAllViews();
            while (remindersCursor.moveToNext()) {
                int alarm = remindersCursor.getInt(REMINDERS_INDEX_MINUTES);
                String before = EditEvent.constructReminderLabel(context, alarm, true);
                LinearLayout reminderItem = (LinearLayout)
                        inflater.inflate(R.layout.agenda_reminder_item, null);
                TextView reminderItemText = (TextView) reminderItem.findViewById(R.id.reminder);
                reminderItemText.setText(before);
                parent.addView(reminderItem);
            }
        }
        remindersCursor.close();
    }
    */
}
