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

package com.android.calendar.month;

import com.android.calendar.Event;
import com.android.calendar.LogUtil;
import com.android.calendar.lunar.LunarUtil;
import com.android.calendar.R;
import com.android.calendar.Utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Service;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.provider.CalendarContract.Attendees;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.Log;
import android.view.MotionEvent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class MonthWeekEventsView extends SimpleWeekView {

    private static final String TAG = "MonthView";

    public static final String VIEW_PARAMS_ORIENTATION = "orientation";
    public static final String VIEW_PARAMS_ANIMATE_TODAY = "animate_today";

	private static final int SELECTED_DAY_COLOR = 0xFF4B92B7; // chenzhentao 2012-7-24 add
    private static final int MONTH_EVENT_COLOR = 0xFF50CCFF; // chenzhentao 2012-7-24 add
    private static final int SUNDAY_NUM_COLOR = 0xFFB7504C;	// chenzhentao 2012-6-30 add
    private static int TEXT_SIZE_MONTH_NUMBER = 32;
    ///M:#Lunar#
    private static int TEXT_SIZE_MONTH_NUMBER_LUNAR_OFFSET = 4;///M: add for lunar calendar.
    private static int TEXT_X_MONTH_NUMBER_LUNAR_OFFSET = 2;///M: add for lunar calendar.
    ///@}
    private static int TEXT_SIZE_EVENT = 10;
    private static int TEXT_SIZE_EVENT_TITLE = 14;
    private static int TEXT_SIZE_MORE_EVENTS = 12;
    private static int TEXT_SIZE_MONTH_NAME = 14;
    private static int TEXT_SIZE_WEEK_NUM = 12;

    private static int DNA_MARGIN = 4;
    private static int DNA_ALL_DAY_HEIGHT = 4;
    private static int DNA_MIN_SEGMENT_HEIGHT = 4;
    private static int DNA_WIDTH = 8;
    private static int DNA_ALL_DAY_WIDTH = 32;
    private static int DNA_SIDE_PADDING = 6;
    private static int CONFLICT_COLOR = Color.BLACK;

    private static int DEFAULT_EDGE_SPACING = 0;
    private static int SIDE_PADDING_MONTH_NUMBER = 4;
    private static int TOP_PADDING_MONTH_NUMBER = 4;
    private static int TOP_PADDING_WEEK_NUMBER = 4;
    private static int SIDE_PADDING_WEEK_NUMBER = 20;
    private static int DAY_SEPARATOR_OUTER_WIDTH = 0;
    private static int DAY_SEPARATOR_INNER_WIDTH = 1;
    private static int DAY_SEPARATOR_VERTICAL_LENGTH = 53;
    private static int DAY_SEPARATOR_VERTICAL_LENGHT_PORTRAIT = 64;
    private static int MIN_WEEK_WIDTH = 50;

    private static int EVENT_X_OFFSET_LANDSCAPE = 38;
    private static int EVENT_Y_OFFSET_LANDSCAPE = 11;
    private static int EVENT_Y_OFFSET_PORTRAIT = 16;
    private static int EVENT_SQUARE_WIDTH = 10;
    private static int EVENT_SQUARE_BORDER = 2;
    private static int EVENT_LINE_PADDING = 8;
    private static int EVENT_LINE_EXTRA_PADDING = 2;
    private static int EVENT_RIGHT_PADDING = 4;
    private static int EVENT_BOTTOM_PADDING = 12;

    private static int TODAY_HIGHLIGHT_WIDTH = 2;

    private static int SPACING_WEEK_NUMBER = 24;
    private static boolean mScaled = false;
    private static boolean mShowDetailsInMonth;

    protected Time mToday = new Time();
    protected boolean mHasToday = false;
    protected int mTodayIndex = -1;
    protected int mOrientation = Configuration.ORIENTATION_LANDSCAPE;
    protected List<ArrayList<Event>> mEvents = null;
    protected ArrayList<Event> mUnsortedEvents = null;
    HashMap<Integer, Utils.DNAStrand> mDna = null;
    // This is for drawing the outlines around event chips and supports up to 10
    // events being drawn on each day. The code will expand this if necessary.
    protected FloatRef mEventOutlines = new FloatRef(10 * 4 * 4 * 7);

    protected static StringBuilder mStringBuilder = new StringBuilder(50);
    // TODO recreate formatter when locale changes
    protected static Formatter mFormatter = new Formatter(mStringBuilder, Locale.getDefault());

    protected Paint mMonthNamePaint;
    protected TextPaint mEventPaint;
    protected TextPaint mDeclinedEventPaint;
    protected TextPaint mEventExtrasPaint;
    protected TextPaint mEventDeclinedExtrasPaint;
    protected Paint mWeekNumPaint;
    protected Paint mDNAAllDayPaint;
    protected Paint mDNATimePaint;
    protected Paint mEventSquarePaint;


    protected Drawable mTodayDrawable;

    protected int mMonthNumHeight;
    protected int mEventHeight;
    protected int mExtrasHeight;
    protected int mWeekNumHeight;

    protected int mMonthBGColor;
    protected int mMonthBGOtherColor;
    protected int mMonthBGTodayColor;
    protected int mMonthNumColor;
    protected int mMonthNumOtherColor;
    protected int mMonthNumTodayColor;
    protected int mMonthNameColor;
    protected int mMonthNameOtherColor;
    protected int mMonthEventColor;
    protected int mMonthDeclinedEventColor;
    protected int mMonthDeclinedExtrasColor;
    protected int mMonthEventExtraColor;
    protected int mMonthEventOtherColor;
    protected int mMonthEventExtraOtherColor;
    protected int mMonthWeekNumColor;
    protected int mMonthBusyBitsBgColor;
    protected int mMonthBusyBitsBusyTimeColor;
    protected int mMonthBusyBitsConflictTimeColor;

    protected int mEventChipOutlineColor = 0xFFFFFFFF;
    protected int mDaySeparatorInnerColor;
    protected int mTodayAnimateColor;

    private boolean mAnimateToday;
    private int mAnimateTodayAlpha = 0;
    private ObjectAnimator mTodayAnimator = null;

    private TodayAnimatorListener mAnimatorListener = new TodayAnimatorListener();

    class TodayAnimatorListener extends AnimatorListenerAdapter {
        private volatile Animator mAnimator = null;
        private volatile boolean mFadingIn = false;

        @Override
        public void onAnimationEnd(Animator animation) {
            synchronized (this) {
                if (mAnimator != animation) {
                    animation.removeAllListeners();
                    animation.cancel();
                    return;
                }
                if (mFadingIn) {
                    if (mTodayAnimator != null) {
                        mTodayAnimator.removeAllListeners();
                        mTodayAnimator.cancel();
                    }
                    mTodayAnimator = ObjectAnimator.ofInt(MonthWeekEventsView.this,
                            "animateTodayAlpha", 255, 0);
                    mAnimator = mTodayAnimator;
                    mFadingIn = false;
                    mTodayAnimator.addListener(this);
                    mTodayAnimator.setDuration(600);
                    mTodayAnimator.start();
                } else {
                    mAnimateToday = false;
                    mAnimateTodayAlpha = 0;
                    mAnimator.removeAllListeners();
                    mAnimator = null;
                    mTodayAnimator = null;
                    invalidate();
                }
            }
        }

        public void setAnimator(Animator animation) {
            mAnimator = animation;
        }

        public void setFadingIn(boolean fadingIn) {
            mFadingIn = fadingIn;
        }

    }

    private int[] mDayXs;

    /**
     * This provides a reference to a float array which allows for easy size
     * checking and reallocation. Used for drawing lines.
     */
    private class FloatRef {
        float[] array;

        public FloatRef(int size) {
            array = new float[size];
        }

        public void ensureSize(int newSize) {
            if (newSize >= array.length) {
                // Add enough space for 7 more boxes to be drawn
                array = Arrays.copyOf(array, newSize + 16 * 7);
            }
        }
    }

    /**
     * Shows up as an error if we don't include this.
     */
    public MonthWeekEventsView(Context context) {
        super(context);
    }

    // Sets the list of events for this week. Takes a sorted list of arrays
    // divided up by day for generating the large month version and the full
    // arraylist sorted by start time to generate the dna version.
    public void setEvents(List<ArrayList<Event>> sortedEvents, ArrayList<Event> unsortedEvents) {
        setEvents(sortedEvents);
        // The MIN_WEEK_WIDTH is a hack to prevent the view from trying to
        // generate dna bits before its width has been fixed.
        createDna(unsortedEvents);
    }

    /**
     * Sets up the dna bits for the view. This will return early if the view
     * isn't in a state that will create a valid set of dna yet (such as the
     * views width not being set correctly yet).
     */
    public void createDna(ArrayList<Event> unsortedEvents) {
        if (unsortedEvents == null || mWidth <= MIN_WEEK_WIDTH || getContext() == null) {
            // Stash the list of events for use when this view is ready, or
            // just clear it if a null set has been passed to this view
            mUnsortedEvents = unsortedEvents;
            mDna = null;
            return;
        } else {
            // clear the cached set of events since we're ready to build it now
            mUnsortedEvents = null;
        }
        // Create the drawing coordinates for dna
        if (!mShowDetailsInMonth) {
            int numDays = mEvents.size();
            int effectiveWidth = mWidth - mPadding * 2;
            if (mShowWeekNum) {
                effectiveWidth -= SPACING_WEEK_NUMBER;
            }
            DNA_ALL_DAY_WIDTH = effectiveWidth / numDays - 2 * DNA_SIDE_PADDING;
            mDNAAllDayPaint.setStrokeWidth(DNA_ALL_DAY_WIDTH);
            mDayXs = new int[numDays];
            for (int day = 0; day < numDays; day++) {
                mDayXs[day] = computeDayLeftPosition(day) + DNA_WIDTH / 2 + DNA_SIDE_PADDING;

            }

            int top = DAY_SEPARATOR_INNER_WIDTH + DNA_MARGIN + DNA_ALL_DAY_HEIGHT + 1;
            int bottom = mHeight - DNA_MARGIN;
            mDna = Utils.createDNAStrands(mFirstJulianDay, unsortedEvents, top, bottom,
                    DNA_MIN_SEGMENT_HEIGHT, mDayXs, getContext());
        }
    }

    public void setEvents(List<ArrayList<Event>> sortedEvents) {
        mEvents = sortedEvents;
        if (sortedEvents == null) {
            return;
        }
        if (sortedEvents.size() != mNumDays) {
            if (Log.isLoggable(TAG, Log.ERROR)) {
                Log.wtf(TAG, "Events size must be same as days displayed: size="
                        + sortedEvents.size() + " days=" + mNumDays);
            }
            mEvents = null;
            return;
        }
    }

    protected void loadColors(Context context) {
        Resources res = context.getResources();
        mMonthWeekNumColor = res.getColor(R.color.month_week_num_color);
        mMonthNumColor = res.getColor(R.color.month_day_number);
        mMonthNumOtherColor = res.getColor(R.color.month_day_number_other);
        mMonthNumTodayColor = res.getColor(R.color.month_today_number);
        mMonthNameColor = mMonthNumColor;
        mMonthNameOtherColor = mMonthNumOtherColor;
        mMonthEventColor = res.getColor(R.color.month_event_color);
        mMonthDeclinedEventColor = res.getColor(R.color.agenda_item_declined_color);
        mMonthDeclinedExtrasColor = res.getColor(R.color.agenda_item_where_declined_text_color);
        mMonthEventExtraColor = res.getColor(R.color.month_event_extra_color);
        mMonthEventOtherColor = res.getColor(R.color.month_event_other_color);
        mMonthEventExtraOtherColor = res.getColor(R.color.month_event_extra_other_color);
        mMonthBGTodayColor = res.getColor(R.color.month_today_bgcolor);
        mMonthBGOtherColor = res.getColor(R.color.month_other_bgcolor);
        mMonthBGColor = res.getColor(R.color.month_bgcolor);
        mDaySeparatorInnerColor = res.getColor(R.color.month_grid_lines);
        mTodayAnimateColor = res.getColor(R.color.today_highlight_color);

        mTodayDrawable = res.getDrawable(R.drawable.today_blue_week_holo_light);
    }

    /**
     * Sets up the text and style properties for painting. Override this if you
     * want to use a different paint.
     */
    @Override
    protected void initView() {
        super.initView();

        if (!mScaled) {
            Resources resources = getContext().getResources();
            mShowDetailsInMonth = Utils.getConfigBool(getContext(), R.bool.show_details_in_month);
            TEXT_SIZE_MONTH_NUMBER = resources.getInteger(R.integer.text_size_month_number);
            SIDE_PADDING_MONTH_NUMBER = resources.getInteger(R.integer.month_day_number_margin);
            CONFLICT_COLOR = resources.getColor(R.color.month_dna_conflict_time_color);
            if (mScale != 1) {
                TOP_PADDING_MONTH_NUMBER *= mScale;
                TOP_PADDING_WEEK_NUMBER *= mScale;
                SIDE_PADDING_MONTH_NUMBER *= mScale;
                SIDE_PADDING_WEEK_NUMBER *= mScale;
                SPACING_WEEK_NUMBER *= mScale;
                TEXT_SIZE_MONTH_NUMBER *= mScale;
                TEXT_SIZE_EVENT *= mScale;
                TEXT_SIZE_EVENT_TITLE *= mScale;
                TEXT_SIZE_MORE_EVENTS *= mScale;
                TEXT_SIZE_MONTH_NAME *= mScale;
                TEXT_SIZE_WEEK_NUM *= mScale;
                DAY_SEPARATOR_OUTER_WIDTH *= mScale;
                DAY_SEPARATOR_INNER_WIDTH *= mScale;
                DAY_SEPARATOR_VERTICAL_LENGTH *= mScale;
                DAY_SEPARATOR_VERTICAL_LENGHT_PORTRAIT *= mScale;
                EVENT_X_OFFSET_LANDSCAPE *= mScale;
                EVENT_Y_OFFSET_LANDSCAPE *= mScale;
                EVENT_Y_OFFSET_PORTRAIT *= mScale;
                EVENT_SQUARE_WIDTH *= mScale;
                EVENT_SQUARE_BORDER *= mScale;
                EVENT_LINE_PADDING *= mScale;
                EVENT_LINE_EXTRA_PADDING *= mScale;
                EVENT_BOTTOM_PADDING *= mScale;
                EVENT_RIGHT_PADDING *= mScale;
                DNA_MARGIN *= mScale;
                DNA_WIDTH *= mScale;
                DNA_ALL_DAY_HEIGHT *= mScale;
                DNA_MIN_SEGMENT_HEIGHT *= mScale;
                DNA_SIDE_PADDING *= mScale;
                DEFAULT_EDGE_SPACING *= mScale;
                DNA_ALL_DAY_WIDTH *= mScale;
                TODAY_HIGHLIGHT_WIDTH *= mScale;
            }
            if (!mShowDetailsInMonth) {
                TOP_PADDING_MONTH_NUMBER += DNA_ALL_DAY_HEIGHT + DNA_MARGIN;
            }
            mScaled = true;
        }
        mPadding = DEFAULT_EDGE_SPACING;
        loadColors(getContext());
        // TODO modify paint properties depending on isMini

        mMonthNumPaint = new Paint();
        mMonthNumPaint.setFakeBoldText(false);
        mMonthNumPaint.setAntiAlias(true);
        mMonthNumPaint.setTextSize(TEXT_SIZE_MONTH_NUMBER);
        mMonthNumPaint.setColor(mMonthNumColor);
        mMonthNumPaint.setStyle(Style.FILL);
        mMonthNumPaint.setTypeface(Typeface.DEFAULT);
        mMonthNumPaint.setTextAlign(Align.CENTER);
        mMonthNumHeight = (int) (-mMonthNumPaint.ascent());

        mEventPaint = new TextPaint();
        mEventPaint.setFakeBoldText(true);
        mEventPaint.setAntiAlias(true);
        mEventPaint.setTextSize(TEXT_SIZE_EVENT_TITLE - 2);
        mMonthEventColor = MONTH_EVENT_COLOR; // chenzhentao add
        mEventPaint.setColor(mMonthEventColor);

        mDeclinedEventPaint = new TextPaint();
        mDeclinedEventPaint.setFakeBoldText(true);
        mDeclinedEventPaint.setAntiAlias(true);
        mDeclinedEventPaint.setTextSize(TEXT_SIZE_EVENT_TITLE);
        mMonthDeclinedEventColor = MONTH_EVENT_COLOR; // chenzhentao add
        mDeclinedEventPaint.setColor(mMonthDeclinedEventColor);

        mEventHeight = (int) (-mEventPaint.ascent());

        mEventExtrasPaint = new TextPaint();
        mEventExtrasPaint.setFakeBoldText(false);
        mEventExtrasPaint.setAntiAlias(true);
        mEventExtrasPaint.setStrokeWidth(EVENT_SQUARE_BORDER);
        mEventExtrasPaint.setTextSize(TEXT_SIZE_EVENT);
        mMonthEventExtraColor = MONTH_EVENT_COLOR; // chenzhentao add
        mEventExtrasPaint.setColor(mMonthEventExtraColor);
        mEventExtrasPaint.setStyle(Style.FILL);
        mEventExtrasPaint.setTextAlign(Align.LEFT);

        mEventDeclinedExtrasPaint = new TextPaint();
        mEventDeclinedExtrasPaint.setFakeBoldText(false);
        mEventDeclinedExtrasPaint.setAntiAlias(true);
        mEventDeclinedExtrasPaint.setStrokeWidth(EVENT_SQUARE_BORDER);
        mEventDeclinedExtrasPaint.setTextSize(TEXT_SIZE_EVENT);
        mEventDeclinedExtrasPaint.setColor(mMonthDeclinedExtrasColor);
        mEventDeclinedExtrasPaint.setStyle(Style.FILL);
        mEventDeclinedExtrasPaint.setTextAlign(Align.LEFT);

        mWeekNumPaint = new Paint();
        mWeekNumPaint.setFakeBoldText(false);
        mWeekNumPaint.setAntiAlias(true);
        mWeekNumPaint.setTextSize(TEXT_SIZE_WEEK_NUM);
        mWeekNumPaint.setColor(mWeekNumColor);
        mWeekNumPaint.setStyle(Style.FILL);
        mWeekNumPaint.setTextAlign(Align.RIGHT);

        mWeekNumHeight = (int) (-mWeekNumPaint.ascent());

        mDNAAllDayPaint = new Paint();
        mDNATimePaint = new Paint();
        mDNATimePaint.setColor(mMonthBusyBitsBusyTimeColor);
        mDNATimePaint.setStyle(Style.FILL_AND_STROKE);
        mDNATimePaint.setStrokeWidth(DNA_WIDTH);
        mDNATimePaint.setAntiAlias(false);
        mDNAAllDayPaint.setColor(mMonthBusyBitsConflictTimeColor);
        mDNAAllDayPaint.setStyle(Style.FILL_AND_STROKE);
        mDNAAllDayPaint.setStrokeWidth(DNA_ALL_DAY_WIDTH);
        mDNAAllDayPaint.setAntiAlias(false);

        mEventSquarePaint = new Paint();
        mEventSquarePaint.setStrokeWidth(EVENT_SQUARE_BORDER);
        mEventSquarePaint.setAntiAlias(false);
    }

    @Override
    public void setWeekParams(HashMap<String, Integer> params, String tz) {
        super.setWeekParams(params, tz);

        if (params.containsKey(VIEW_PARAMS_ORIENTATION)) {
            mOrientation = params.get(VIEW_PARAMS_ORIENTATION);
        }

        updateToday(tz);
        mNumCells = mNumDays + 1;

        if (params.containsKey(VIEW_PARAMS_ANIMATE_TODAY) && mHasToday) {
            synchronized (mAnimatorListener) {
                if (mTodayAnimator != null) {
                    mTodayAnimator.removeAllListeners();
                    mTodayAnimator.cancel();
                }
                mTodayAnimator = ObjectAnimator.ofInt(this, "animateTodayAlpha",
                        Math.max(mAnimateTodayAlpha, 80), 255);
                mTodayAnimator.setDuration(150);
                mAnimatorListener.setAnimator(mTodayAnimator);
                mAnimatorListener.setFadingIn(true);
                mTodayAnimator.addListener(mAnimatorListener);
                mAnimateToday = true;
                mTodayAnimator.start();
            }
        }
        
        mSelectedDay = params.get(VIEW_PARAMS_SELECTED_DAY);
    }

    /**
     * @param tz
     */
    public boolean updateToday(String tz) {
        mToday.timezone = tz;
        mToday.setToNow();
        mToday.normalize(true);
        int julianToday = Time.getJulianDay(mToday.toMillis(false), mToday.gmtoff);
        if (julianToday >= mFirstJulianDay && julianToday < mFirstJulianDay + mNumDays) {
            mHasToday = true;
            mTodayIndex = julianToday - mFirstJulianDay;
        } else {
            mHasToday = false;
            mTodayIndex = -1;
        }
        return mHasToday;
    }

    public void setAnimateTodayAlpha(int alpha) {
        mAnimateTodayAlpha = alpha;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawBackground(canvas);
        drawWeekNums(canvas);
        drawDaySeparators(canvas);
        if (mHasToday && mAnimateToday) {
            drawToday(canvas, mTodayIndex);
        }
        if (mShowDetailsInMonth) {
            drawEvents(canvas);
        } else {
            if (mDna == null && mUnsortedEvents != null) {
                createDna(mUnsortedEvents);
            }
            drawDNA(canvas);
        }
    }

    protected void drawToday(Canvas canvas, int todayIndex) {
        r.top = DAY_SEPARATOR_INNER_WIDTH + (TODAY_HIGHLIGHT_WIDTH / 2);
        r.bottom = mHeight - (int) Math.ceil(TODAY_HIGHLIGHT_WIDTH / 2.0f);
        p.setStyle(Style.STROKE);
        p.setStrokeWidth(TODAY_HIGHLIGHT_WIDTH);
    	r.left = computeDayLeftPosition(todayIndex) + (TODAY_HIGHLIGHT_WIDTH / 2);
    	r.right = computeDayLeftPosition(todayIndex + 1)
                - (int) Math.ceil(TODAY_HIGHLIGHT_WIDTH / 2.0f);
        p.setColor(mTodayAnimateColor | (mAnimateTodayAlpha << 24));
        canvas.drawRect(r, p);
        p.setStyle(Style.FILL);
    }

    // TODO move into SimpleWeekView
    // Computes the x position for the left side of the given day
    private int computeDayLeftPosition(int day) {
        int effectiveWidth = mWidth;
        int x = 0;
        int xOffset = 0;
        if (mShowWeekNum) {
            xOffset = SPACING_WEEK_NUMBER + mPadding;
            effectiveWidth -= xOffset;
        }
        x = day * effectiveWidth / mNumDays + xOffset;
        return x;
    }

    @Override
    protected void drawDaySeparators(Canvas canvas) {
        float lines[] = new float[8 * 4];
        int count = 6 * 4;
        int wkNumOffset = 0;
        int i = 0;
        if (mShowWeekNum) {
            // This adds the first line separating the week number
            int xOffset = SPACING_WEEK_NUMBER + mPadding;
            count += 4;
            lines[i++] = xOffset;
            lines[i++] = 0;
            lines[i++] = xOffset;
            lines[i++] = mHeight;
            wkNumOffset++;
        }
        count += 4;
        lines[i++] = 0;
        lines[i++] = 0;
        lines[i++] = mWidth;
        lines[i++] = 0;
        int y0 = 0;
        int y1 = mHeight;

        while (i < count) {
            int x = computeDayLeftPosition(i / 4 - wkNumOffset);
            lines[i++] = x;
            lines[i++] = y0;
            lines[i++] = x;
            lines[i++] = y1;
        }
        p.setColor(mDaySeparatorInnerColor);
        p.setStrokeWidth(DAY_SEPARATOR_INNER_WIDTH);
        canvas.drawLines(lines, 0, count, p);
    }

    @Override
    protected void drawBackground(Canvas canvas) {
        int i = 0;
        int offset = 0;
        r.top = DAY_SEPARATOR_INNER_WIDTH;
        r.bottom = mHeight;
        if (mShowWeekNum) {
            i++;
            offset++;
        }
        if (!mFocusDay[i]) {
            while (++i < mFocusDay.length && !mFocusDay[i])
                ;
            r.right = computeDayLeftPosition(i - offset);
            r.left = 0;
            p.setColor(mMonthBGOtherColor);
            canvas.drawRect(r, p);
            // compute left edge for i, set up r, draw
        } else if (!mFocusDay[(i = mFocusDay.length - 1)]) {
            while (--i >= offset && !mFocusDay[i])
                ;
            i++;
            // compute left edge for i, set up r, draw
            r.right = mWidth;
            r.left = computeDayLeftPosition(i - offset);
            p.setColor(mMonthBGOtherColor);
            canvas.drawRect(r, p);
        }
        if (mHasToday) {
            p.setColor(mMonthBGTodayColor);
            r.left = computeDayLeftPosition(mTodayIndex);
            r.right = computeDayLeftPosition(mTodayIndex + 1);
            canvas.drawRect(r, p);
        }
        if(mSelectedDay != -1){
        	p.setColor(SELECTED_DAY_COLOR);
            r.left = computeDayLeftPosition(mSelectedDay);
            r.right = computeDayLeftPosition(mSelectedDay + 1);
            canvas.drawRect(r, p);
        }
    }

    @Override
    protected void drawWeekNums(Canvas canvas) {
        int y;

        int i = 0;
        int offset = 0;
        int todayIndex = mTodayIndex;
        int x = 0;
        int numCount = mNumDays;
        if (mShowWeekNum) {
            x = SIDE_PADDING_WEEK_NUMBER + mPadding;
            y = mWeekNumHeight + TOP_PADDING_WEEK_NUMBER;
            canvas.drawText(mDayNumbers[0], x, y, mWeekNumPaint);
            numCount++;
            i++;
            todayIndex++;
            offset++;
        }

        y = mMonthNumHeight + TOP_PADDING_MONTH_NUMBER;

        boolean isFocusMonth = mFocusDay[i];
        boolean isBold = false;
        mMonthNumPaint.setColor(isFocusMonth ? mMonthNumColor : mMonthNumOtherColor);
        for (; i < numCount; i++) {
            if (mHasToday && todayIndex == i) {
                mMonthNumPaint.setColor(mMonthNumTodayColor);
                mMonthNumPaint.setFakeBoldText(isBold = true);
                if (i + 1 < numCount) {
                    // Make sure the color will be set back on the next
                    // iteration
                    isFocusMonth = !mFocusDay[i + 1];
                }
            } else if (mFocusDay[i] != isFocusMonth) {
                isFocusMonth = mFocusDay[i];
                mMonthNumPaint.setColor(isFocusMonth ? mMonthNumColor : mMonthNumOtherColor);
            }
            int color = mMonthNumPaint.getColor();
            if(i == Time.SUNDAY) {
            	mMonthNumPaint.setColor(SUNDAY_NUM_COLOR);
            }
            x = computeDayLeftPosition(i + offset) + (SIDE_PADDING_MONTH_NUMBER);
            if(mSelectedDay != -1 && i == mSelectedDay){ // chenzhentao add
            	mMonthNumPaint.setColor(Color.WHITE);
            }
            canvas.drawText(mDayNumbers[i], x, y, mMonthNumPaint);
            ///M:#Lunar#@{
            if(Utils.isShowLunarCalendar()) {
                Time weekDayTime = getDayFromLocation(x);
                if (weekDayTime == null) {
                    LogUtil.e(TAG, "drawWeekNums(),getDayFromLocation(" + x + ") return null,for loop continue");
                    continue;
                }
                String lunarTextDisplay = LunarUtil.getLunarFestivalChineseString(
                        weekDayTime.year, weekDayTime.month + 1, weekDayTime.monthDay);
                mMonthNumPaint.setTextSize(TEXT_SIZE_MONTH_NUMBER-TEXT_SIZE_MONTH_NUMBER_LUNAR_OFFSET);
                canvas.drawText(lunarTextDisplay, x + TEXT_X_MONTH_NUMBER_LUNAR_OFFSET,
                                y + mMonthNumHeight, mMonthNumPaint);
                mMonthNumPaint.setTextSize(TEXT_SIZE_MONTH_NUMBER);
            }
            ///@}
            if (isBold) {
                mMonthNumPaint.setFakeBoldText(isBold = false);
            }
            mMonthNumPaint.setColor(color);
        }
    }

    protected void drawEvents(Canvas canvas) {
        if (mEvents == null) {
            return;
        }

        int day = -1;
        for (ArrayList<Event> eventDay : mEvents) {
            day++;
            if (eventDay == null || eventDay.size() == 0) {
                continue;
            }
            int xSquare = computeDayLeftPosition(day) + 2;
            int rightEdge = computeDayLeftPosition(day + 1);
            int ySquare = mMonthNumHeight + TOP_PADDING_MONTH_NUMBER + 
            		(mOrientation == Configuration.ORIENTATION_PORTRAIT ? 
            		EVENT_Y_OFFSET_PORTRAIT : EVENT_Y_OFFSET_LANDSCAPE);
            
            int eventCount = 0;
            Iterator<Event> iter = eventDay.iterator();
            while (iter.hasNext()) {
                Event event = iter.next();
                int newY = drawEvent(canvas, event, day, xSquare, ySquare, rightEdge, iter.hasNext());
                if (newY == ySquare) {
                    break;
                }
                eventCount++;
                ySquare = newY;
            }

            // xSquare = computeDayLeftPosition(day) + SIDE_PADDING_MONTH_NUMBER + 15;
            xSquare = rightEdge - SIDE_PADDING_MONTH_NUMBER;
            int remaining = eventDay.size() - eventCount;
            if (remaining > 0) {
            	mEventExtrasPaint.setColor(mSelectedDay != -1 && day == mSelectedDay ? Color.WHITE : mMonthEventExtraColor);
                drawMoreEvents(canvas, remaining, xSquare);
            }
        }
    }

    protected int addChipOutline(FloatRef lines, int count, int x, int y) {
        lines.ensureSize(count + 16);
        // top of box
        lines.array[count++] = x;
        lines.array[count++] = y;
        lines.array[count++] = x + EVENT_SQUARE_WIDTH;
        lines.array[count++] = y;
        // right side of box
        lines.array[count++] = x + EVENT_SQUARE_WIDTH;
        lines.array[count++] = y;
        lines.array[count++] = x + EVENT_SQUARE_WIDTH;
        lines.array[count++] = y + EVENT_SQUARE_WIDTH;
        // left side of box
        lines.array[count++] = x;
        lines.array[count++] = y;
        lines.array[count++] = x;
        lines.array[count++] = y + EVENT_SQUARE_WIDTH + 1;
        // bottom of box
        lines.array[count++] = x;
        lines.array[count++] = y + EVENT_SQUARE_WIDTH;
        lines.array[count++] = x + EVENT_SQUARE_WIDTH + 1;
        lines.array[count++] = y + EVENT_SQUARE_WIDTH;

        return count;
    }

    /**
     * Attempts to draw the given event. Returns the y for the next event or the
     * original y if the event will not fit. An event is considered to not fit
     * if the event and its extras won't fit or if there are more events and the
     * more events line would not fit after drawing this event.
     *
     * @param event the event to draw
     * @param x the top left corner for this event's color chip
     * @param y the top left corner for this event's color chip
     * @return the y for the next event or the original y if it won't fit
     */
    protected int drawEvent(
            Canvas canvas, Event event, int day, int x, int y, int rightEdge, boolean moreEvents) {
        int requiredSpace = EVENT_LINE_PADDING + mEventHeight;
        int multiplier = 1;
        if (moreEvents) {
            // multiplier++;
        }
        if (!event.allDay) {
            // multiplier++;
        }
        requiredSpace *= multiplier;
        // The last one doesn't need the EVENT_LINE_PADDING as it will have
        // EVENT_BOTTOM_PADDING instead
        requiredSpace -= EVENT_LINE_PADDING + (event.allDay ? 0 : EVENT_LINE_PADDING);
        if (requiredSpace + y > mHeight - EVENT_BOTTOM_PADDING) {
            // Not enough space, return
            return y;
        }
        r.left = x;
        r.right = x + EVENT_SQUARE_WIDTH;
        r.top = y;
        r.bottom = y + EVENT_SQUARE_WIDTH;
        boolean isDeclined = event.selfAttendeeStatus == Attendees.ATTENDEE_STATUS_DECLINED;
        int color = event.color;
        if (isDeclined) {
            color = Utils.getDeclinedColorFromColor(color);
        }
        Style style = event.selfAttendeeStatus == Attendees.ATTENDEE_STATUS_NONE ? Style.STROKE
                : Style.FILL_AND_STROKE;
        mEventSquarePaint.setStyle(style);
        //canvas.drawRect(r, mEventSquarePaint);

        // int textX = x + EVENT_SQUARE_WIDTH + EVENT_RIGHT_PADDING;
        // int textY = y + EVENT_SQUARE_WIDTH;
        int textX = x + EVENT_RIGHT_PADDING;
        int textY = y;
        float avail = rightEdge - textX - 2;
        CharSequence text = TextUtils.ellipsize(
                event.title, mEventPaint, avail, TextUtils.TruncateAt.END);
        if(isDeclined){
        	mDeclinedEventPaint.setColor(mSelectedDay != -1 && day == mSelectedDay ? Color.WHITE : color);
        }else{
        	mEventPaint.setColor(mSelectedDay != -1 && day == mSelectedDay ? Color.WHITE : mMonthEventColor);
        }
        canvas.drawText(text.toString(), textX, textY, isDeclined ? mDeclinedEventPaint
                : mEventPaint);
//        if (!event.allDay) {
//            textY += mEventHeight + EVENT_LINE_EXTRA_PADDING;
//            mStringBuilder.setLength(0);
//            text = DateUtils.formatDateRange(getContext(), mFormatter, event.startMillis,
//                    event.endMillis, DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_ABBREV_ALL,
//                    Utils.getTimeZone(getContext(), null)).toString();
//            text = TextUtils.ellipsize(text, mEventExtrasPaint, avail, TextUtils.TruncateAt.END);
//            canvas.drawText(text.toString(), textX, textY, isDeclined ? mEventDeclinedExtrasPaint
//                    : mEventExtrasPaint);
//        }

        // return textY + EVENT_LINE_PADDING;
        return textY + mEventHeight + 3;
    }

    protected void drawMoreEvents(Canvas canvas, int remainingEvents, int x) {
        // int y = mHeight - EVENT_BOTTOM_PADDING;
    	int y = EVENT_BOTTOM_PADDING;
        String text = getContext().getResources().getQuantityString(
                R.plurals.month_more_events, remainingEvents);
        mEventExtrasPaint.setAntiAlias(true);
        mEventExtrasPaint.setFakeBoldText(true);
        canvas.drawText(String.format(text, remainingEvents), x, y, mEventExtrasPaint);
        mEventExtrasPaint.setFakeBoldText(false);
    }

    /**
     * Draws a line showing busy times in each day of week The method draws
     * non-conflicting times in the event color and times with conflicting
     * events in the dna conflict color defined in colors.
     *
     * @param canvas
     */
    protected void drawDNA(Canvas canvas) {
        // Draw event and conflict times
        if (mDna != null) {
            for (Utils.DNAStrand strand : mDna.values()) {
                if (strand.color == CONFLICT_COLOR || strand.points == null
                        || strand.points.length == 0) {
                    continue;
                }
                mDNATimePaint.setColor(strand.color);
                canvas.drawLines(strand.points, mDNATimePaint);
            }
            // Draw black last to make sure it's on top
            Utils.DNAStrand strand = mDna.get(CONFLICT_COLOR);
            if (strand != null && strand.points != null && strand.points.length != 0) {
                mDNATimePaint.setColor(strand.color);
                canvas.drawLines(strand.points, mDNATimePaint);
            }
            if (mDayXs == null) {
                return;
            }
            int numDays = mDayXs.length;
            int xOffset = (DNA_ALL_DAY_WIDTH - DNA_WIDTH) / 2;
            if (strand != null && strand.allDays != null && strand.allDays.length == numDays) {
                for (int i = 0; i < numDays; i++) {
                    // this adds at most 7 draws. We could sort it by color and
                    // build an array instead but this is easier.
                    if (strand.allDays[i] != 0) {
                        mDNAAllDayPaint.setColor(strand.allDays[i]);
                        canvas.drawLine(mDayXs[i] + xOffset, DNA_MARGIN, mDayXs[i] + xOffset,
                                DNA_MARGIN + DNA_ALL_DAY_HEIGHT, mDNAAllDayPaint);
                    }
                }
            }
        }
    }

    @Override
    protected void updateSelectionPositions() {
        if (mHasSelectedDay) {
            int selectedPosition = mSelectedDay - mWeekStart;
            if (selectedPosition < 0) {
                selectedPosition += 7;
            }
            int effectiveWidth = mWidth - mPadding * 2;
            effectiveWidth -= SPACING_WEEK_NUMBER;
            mSelectedLeft = selectedPosition * effectiveWidth / mNumDays + mPadding;
            mSelectedRight = (selectedPosition + 1) * effectiveWidth / mNumDays + mPadding;
            mSelectedLeft += SPACING_WEEK_NUMBER;
            mSelectedRight += SPACING_WEEK_NUMBER;
        }
    }

    @Override
    public Time getDayFromLocation(float x) {
        int dayStart = mShowWeekNum ? SPACING_WEEK_NUMBER + mPadding : mPadding;
        if (x < dayStart || x > mWidth - mPadding) {
            return null;
        }
        // Selection is (x - start) / (pixels/day) == (x -s) * day / pixels
        int dayPosition = (int) ((x - dayStart) * mNumDays / (mWidth - dayStart - mPadding));
        int day = mFirstJulianDay + dayPosition;

        Time time = new Time(mTimeZone);
        /// M: Delete a google walk around, which was used to correct the error 
        /// at the first day of 1970 caused by google original framework.
        /// The framework error has been corrected,so the walk around is no longer needed.
        /// @{
            /*
             *        if (mWeek == 0) {
             *             // This week is weird...
             *             if (day < Time.EPOCH_JULIAN_DAY) {
             *                 day++;
             *            } else if (day == Time.EPOCH_JULIAN_DAY) {
             *                 time.set(1, 0, 1970);
             *                 time.normalize(true);
             *                return time;
             *            }
             *        }
             */
        ///@}
        time.setJulianDay(day);
        return time;
    }

    @Override
    public boolean onHoverEvent(MotionEvent event) {
        Context context = getContext();
        // only send accessibility events if accessibility and exploration are
        // on.
        AccessibilityManager am = (AccessibilityManager) context
                .getSystemService(Service.ACCESSIBILITY_SERVICE);
        if (!am.isEnabled() || !am.isTouchExplorationEnabled()) {
            return super.onHoverEvent(event);
        }
        if (event.getAction() != MotionEvent.ACTION_HOVER_EXIT) {
            Time hover = getDayFromLocation(event.getX());
            if (hover != null
                    && (mLastHoverTime == null || Time.compare(hover, mLastHoverTime) != 0)) {
                Long millis = hover.toMillis(true);
                String date = Utils.formatDateRange(context, millis, millis,
                        DateUtils.FORMAT_SHOW_DATE);
                AccessibilityEvent accessEvent = AccessibilityEvent
                        .obtain(AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED);
                accessEvent.getText().add(date);
                if (mShowDetailsInMonth && mEvents != null) {
                    int dayStart = SPACING_WEEK_NUMBER + mPadding;
                    int dayPosition = (int) ((event.getX() - dayStart) * mNumDays / (mWidth
                            - dayStart - mPadding));
                    ArrayList<Event> events = mEvents.get(dayPosition);
                    List<CharSequence> text = accessEvent.getText();
                    for (Event e : events) {
                        text.add(e.getTitleAndLocation() + ". ");
                        int flags = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR;
                        if (!e.allDay) {
                            flags |= DateUtils.FORMAT_SHOW_TIME;
                            if (DateFormat.is24HourFormat(context)) {
                                flags |= DateUtils.FORMAT_24HOUR;
                            }
                        } else {
                            flags |= DateUtils.FORMAT_UTC;
                        }
                        text.add(Utils.formatDateRange(context, e.startMillis, e.endMillis,
                                flags) + ". ");
                    }
                }
                sendAccessibilityEventUnchecked(accessEvent);
                mLastHoverTime = hover;
            }
        }
        return true;
    }
}
