package com.android.calendar.month;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.android.calendar.R;

public class RotateImageView extends ImageView {

	private Animation mAnimToLeft = null;
	private Animation mAnimToRight = null;
	private boolean mIsDirectLeft = true;

	public RotateImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		mAnimToLeft = AnimationUtils.loadAnimation(context, R.anim.rotate_left);
		mAnimToRight = AnimationUtils.loadAnimation(context,
				R.anim.rotate_right);
		mAnimToLeft.setFillAfter(true);
		mAnimToRight.setFillAfter(true);
	}

	public boolean getDirectLeft() {
		return mIsDirectLeft;
	}

	public void setDirectLeft(boolean directLeft) {
		this.mIsDirectLeft = directLeft;
		this.clearAnimation();
		if (mIsDirectLeft) {
			startAnimation(mAnimToLeft);
		} else {
			startAnimation(mAnimToRight);
		}
	}
}
