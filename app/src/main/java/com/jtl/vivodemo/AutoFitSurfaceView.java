package com.jtl.vivodemo;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceView;

/**
 * 作者:jtl
 * 日期:Created in 2019/3/19 16:48
 * 描述:
 * 更改:
 */
public class AutoFitSurfaceView extends SurfaceView {
    private int mRatioWidth = 0;
    private int mRatioHeight = 0;

    public AutoFitSurfaceView(Context context) {
        this(context,null);
    }

    public AutoFitSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public AutoFitSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setAspectRatio(int width, int height) {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("Size cannot be negative.");
        }
        mRatioWidth = width;
        mRatioHeight = height;
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (0 == mRatioWidth || 0 == mRatioHeight) {
            setMeasuredDimension(width, height);
        } else {
            if (width < height * mRatioWidth / mRatioHeight) {
                setMeasuredDimension(width, width * mRatioHeight / mRatioWidth);
            } else {
                setMeasuredDimension(height * mRatioWidth / mRatioHeight, height);
            }
        }
    }
}
