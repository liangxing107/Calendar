package com.android.calendar.month;

import android.graphics.Camera;
import android.graphics.Matrix;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;

class MonthEventListAnimation extends Animation {

	// private static final int FRONT_ANIM = 0;
	// private static final int BACK_ANIM = 1;
	// private int mFlag = FRONT_ANIM;

	private int mCenterX; // 记录 View 的中间坐标
	private int mCenterY;
	private Camera mCamera = null;
	private float deg = 0;

	public MonthEventListAnimation() {
		super();
		// TODO Auto-generated constructor stub
		mCamera = new Camera();
		setInterpolator(new LinearInterpolator());
		// setFillAfter(true);
	}

	@Override
	public void initialize(int width, int height, int parentWidth,
			int parentHeight) {
		super.initialize(width, height, parentWidth, parentHeight);

		// 初始化中间坐标值
		mCenterX = width / 2;
		mCenterY = height / 2;
	}

	@Override
	protected void applyTransformation(float interpolatedTime, Transformation t) {
		final Matrix matrix = t.getMatrix();
		mCamera.save();
		if (interpolatedTime >= 0.5F) {
			deg = (float) (360 - interpolatedTime * 360);
		}
		mCamera.rotateX(deg);
		mCamera.getMatrix(matrix);
		mCamera.restore();
		matrix.preTranslate(-mCenterX, -mCenterY);
		matrix.postTranslate(mCenterX, mCenterY);
	}

	// @Override
	// protected void applyTransformation(float interpolatedTime, Transformation
	// t) {
	// final Matrix matrix = t.getMatrix();
	// mCamera.save();
	// System.out.println("interpolatedTime = " + interpolatedTime);
	// if (mFlag == FRONT_ANIM) {
	// if (interpolatedTime <= 0.5F) {
	// mCamera.rotateX(interpolatedTime * 2 * 90);
	// // 第一个view 在动画时间的前半段旋转90° 和屏幕垂直（不可见）
	// // System.out.println("time:"+interpolatedTime*2*90);
	// } else {
	// mCamera.rotateX(90);
	// }
	// } else if (mFlag == BACK_ANIM) {
	// if (interpolatedTime >= 0.5F) {
	// mCamera.rotateX((float) (270 + (interpolatedTime - 0.5) * 90 * 2));
	// // 第二个view 在动画时间的后半段从270°旋转至360°
	// // 即从垂直于屏幕 旋转到与屏幕平行
	// // 由不可见变为可见
	// } else {
	// mCamera.rotateX(270);
	// }
	// }
	// mCamera.getMatrix(matrix);
	// mCamera.restore();
	// matrix.preTranslate(-mCenterX, -mCenterY);
	// matrix.postTranslate(mCenterX, mCenterY);
	// }
}