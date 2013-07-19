package com.android.calendar.year;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import com.android.calendar.Utils;

import com.android.calendar.R;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Typeface;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

public class YearByMonthItem extends View {

	private Time mTime = null;
	private Time mToday = null;
	
	private Paint mMonthTitlePaint = null; // month name
	private Paint mMonthNumPaint = null;
	private Paint mTodayPaint = null;
	
	private float mMonthNumTextSize = 0;
	private float mWeekTitleTextSize = 0;
	private float mMonthTitleTextSize = 0;
	
	private int mDayCountInMonth = 0;
	private boolean mHasToday = false;

	private int mViewWidth = 0;
	private int mViewHeight = 0;
	private int mOrientation = Configuration.ORIENTATION_PORTRAIT;
	private float mOffsetX = 0;
	private float mOffsetY = 0;
	private float mCellWidth = 0;

	private static String[] mWeekDayLabels = new String[Calendar.SATURDAY];
	private static String[] mMonthLabels = new String[Calendar.DECEMBER + 1];

	private static final int WEEK_DAYS = 7;
	private float mWeekWordMargin = 0;
	private float mMonthItemPaddingLeftRight = 0;
	private float mMonthNumMarginBottom = 0;

	public static final String VIEW_PARAMS_VIEW_HEIGHT = "view_height";
	public static final String VIEW_PARAMS_VIEW_ORIENTATION = "view_orientation";

	public YearByMonthItem(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		
		Resources resources = context.getResources();
		mMonthNumTextSize = resources.getDimension(R.dimen.year_view_month_number_text_size);
		mWeekTitleTextSize = resources.getDimension(R.dimen.year_view_week_title_text_size);
		mMonthTitleTextSize = resources.getDimension(R.dimen.year_view_month_title_text_size);
		mWeekWordMargin = resources.getDimension(R.dimen.year_view_week_name_divider_marginBottom);
		mMonthItemPaddingLeftRight = resources.getDimension(R.dimen.year_view_month_item_margin_left_right);
		mMonthNumMarginBottom = resources.getDimension(R.dimen.year_view_month_num_marginBottom);
		
		mMonthNumPaint = new Paint();
		mMonthNumPaint.setAntiAlias(true);
		mMonthNumPaint.setStyle(Style.FILL);
		mMonthNumPaint.setTypeface(Typeface.SERIF);
		mMonthNumPaint.setTextSize(mMonthNumTextSize);
		mMonthNumPaint.setTextAlign(Align.CENTER);
		
		mMonthTitlePaint = new Paint();
		mMonthTitlePaint.setAntiAlias(true);
		mMonthTitlePaint.setStyle(Style.FILL);
		mMonthTitlePaint.setTypeface(Typeface.SERIF);
		mMonthTitlePaint.setColor(Color.BLACK);
		mMonthTitlePaint.setTextSize(mMonthTitleTextSize);
		mMonthTitlePaint.setTextAlign(Align.CENTER);

		mTodayPaint = new Paint();
		mTodayPaint.setAntiAlias(true);
		mTodayPaint.setColor(resources.getColor(R.color.year_view_today_rect_color));
		mTodayPaint.setTextSize(mMonthNumTextSize);

		this.mToday = new Time();
		initWeekAndMonthLabels();
	}

	public YearByMonthItem(Context context) {
		this(context, null);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		if (mTime == null) {
			return;
		}
		mCellWidth = (mViewWidth - mMonthItemPaddingLeftRight * 2) / WEEK_DAYS;
		drawMonthHead(canvas);
		drawMonthNum(canvas);
	}
	
	private void drawMonthHead(Canvas canvas) {
		// month title
		mMonthTitlePaint.setTextSize(mMonthTitleTextSize);
		mOffsetY = getTHeightFromPaint(mMonthTitlePaint);
		canvas.drawText(mMonthLabels[mTime.month], mViewWidth / 2, mOffsetY,
				mMonthTitlePaint);
		
		// draw diviver line
		mOffsetY += 5;
		canvas.drawLine(mMonthItemPaddingLeftRight, mOffsetY, mViewWidth - mMonthItemPaddingLeftRight,
				mOffsetY, mMonthTitlePaint);
		
		// draw week head
		mMonthTitlePaint.setTextSize(mWeekTitleTextSize);
		mOffsetY += getTHeightFromPaint(mMonthTitlePaint) + mWeekWordMargin;
		mOffsetX = mMonthItemPaddingLeftRight;
		for (int i = Calendar.SUNDAY; i <= Calendar.SATURDAY; i++) {
			mMonthTitlePaint.setColor(i == Calendar.SUNDAY ? Color.RED : Color.BLACK);
			canvas.drawText(mWeekDayLabels[i - Calendar.SUNDAY], mOffsetX + mCellWidth / 2,
					mOffsetY, mMonthTitlePaint);
			mOffsetX += mCellWidth;
		}
	}

	/**
	 * 
	 * @param canvas
	 */
	private void drawMonthNum(Canvas canvas) {
		int weekDay = Utils.getFirstWeekdayOfMonth(mTime); // first week
		mOffsetX = mMonthItemPaddingLeftRight + mCellWidth * (weekDay - Calendar.SUNDAY);
		mOffsetY += getTHeightFromPaint(mMonthNumPaint) + mMonthNumMarginBottom;
		for (int i = 1; i <= mDayCountInMonth; i++) {
			if (mHasToday && i == mToday.monthDay) {
				canvas.drawRect(mOffsetX, mOffsetY - mMonthNumTextSize, mOffsetX
					+ mCellWidth, mOffsetY, mTodayPaint);
			}
			mMonthNumPaint.setColor(weekDay == Calendar.SUNDAY ? Color.RED : Color.BLACK);
			canvas.drawText(String.valueOf(i), mOffsetX + mCellWidth / 2, mOffsetY, mMonthNumPaint);
			if (weekDay++ == Calendar.SATURDAY) {
				mOffsetY += getTHeightFromPaint(mMonthNumPaint) + mMonthNumMarginBottom;
				weekDay = Calendar.SUNDAY;
				mOffsetX = mMonthItemPaddingLeftRight;
			} else {
				mOffsetX += mCellWidth;
			}
		}
	}


	public float getMonthViewHeight() {
		return mViewHeight;
	}

	public Time getMonthTime() {
		return mTime;
	}
	
	private static final float getTWidthByPaintText(Paint paint, String text){
		return paint.measureText(text);
	}

	private static final float getTHeightFromPaint(Paint paint){
		return -paint.ascent();
	}

	public void setViewParams(HashMap<String, Integer> params, Time time) {
		if (params == null || time == null) {
			return;
		}
		setTag(params);
		if (params.containsKey(VIEW_PARAMS_VIEW_HEIGHT)) {
			this.mViewHeight = params.get(VIEW_PARAMS_VIEW_HEIGHT);
		}
		if (params.containsKey(VIEW_PARAMS_VIEW_ORIENTATION)) {
			this.mOrientation = params.get(VIEW_PARAMS_VIEW_ORIENTATION);
		}
		if (mViewHeight > 0) {
			this.mTime = time;
			mTime.normalize(false);
			this.mDayCountInMonth = Utils.getDaysOfMonth(mTime);

			this.mToday.setToNow();
			this.mHasToday = (mToday.year == mTime.year && mToday.month == mTime.month);
		}
	}

	private static void initWeekAndMonthLabels() {
		// init week names
		for (int i = Calendar.SUNDAY; i <= Calendar.SATURDAY; i++) {
			mWeekDayLabels[i - Calendar.SUNDAY] = DateUtils.getDayOfWeekString(
					i, DateUtils.LENGTH_SHORTEST).toUpperCase();
		}
		
		// init month names
		for (int i = Calendar.JANUARY; i <= Calendar.DECEMBER; i++) {
			mMonthLabels[i] = DateUtils.getMonthString(i,
					DateUtils.FORMAT_ABBREV_MONTH);
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), mViewHeight);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		// TODO Auto-generated method stub
		mViewWidth = w;
		super.onSizeChanged(w, h, oldw, oldh);
	}
}
