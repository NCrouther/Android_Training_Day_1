package com.bignerdranch.android.networkingarchitecture.view;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;
import android.view.WindowManager;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

public class VenueView extends View implements Target {
    private static final int DIVIDER_SIZE = 1;
    private static final int LARGE_TEXT_SIZE = 20;
    private static final int SMALL_TEXT_SIZE = 16;
    private static final int PADDING_SIZE = 8;

    private String mTitle;
    private String mAddress;
    private Drawable mIcon;

    private Paint mBackgroundPaint;
    private Paint mDividerPaint;

    private TextPaint mTitleTextPaint;
    private TextPaint mAddressTextPaint;

    private float mScreenDensity;
    private float mDividerSize;
    float mLargeTextSize;
    float mSmallTextSize;
    float mPaddingSize;

    public VenueView(Context context) {
        this(context, null);
    }

    public VenueView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mScreenDensity = context.getResources().getDisplayMetrics().density;
        mDividerSize = Math.round(DIVIDER_SIZE * mScreenDensity);

        Configuration configuration = context.getResources().getConfiguration();
        float textScale = configuration.fontScale * mScreenDensity;
        mLargeTextSize = Math.round(LARGE_TEXT_SIZE * textScale);
        mSmallTextSize = Math.round(SMALL_TEXT_SIZE * textScale);

        mPaddingSize = Math.round(PADDING_SIZE * mScreenDensity);

        mBackgroundPaint = new Paint();
        mBackgroundPaint.setColor(Color.WHITE);
        mDividerPaint = new Paint();
        mDividerPaint.setColor(Color.LTGRAY);
        mDividerPaint.setStrokeWidth(mDividerSize);

        mTitleTextPaint = new TextPaint();
        mTitleTextPaint.setTextSize(mLargeTextSize);
        mTitleTextPaint.setTextAlign(Paint.Align.LEFT);
        mTitleTextPaint.setColor(Color.BLACK);
        mTitleTextPaint.setAntiAlias(true);
        mAddressTextPaint = new TextPaint();
        mAddressTextPaint.setTextSize(mSmallTextSize);
        mAddressTextPaint.setTextAlign(Paint.Align.LEFT);
        mAddressTextPaint.setColor(Color.BLACK);
        mAddressTextPaint.setAntiAlias(true);
    }

    public void setVenue(String title, String address) {
        mTitle = title;
        mAddress = address;
        invalidate();

        setContentDescription(title);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;

        if (widthMode == MeasureSpec.EXACTLY || widthMode == MeasureSpec.AT_MOST) {
            width = widthSize;
        } else {
            width = calculateWidth();
        }

        if (heightMode == MeasureSpec.EXACTLY || heightMode == MeasureSpec.AT_MOST) {
            height = heightSize;
        } else {
            height = calculateHeight();
        }

        setMeasuredDimension(width, height);
    }

    private int calculateWidth() {
        Point size = new Point();
        WindowManager windowManager = (WindowManager) getContext()
                .getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getSize(size);
        // use window width if unspecified
        return size.x;
    }

    private int calculateHeight() {
        int layoutPadding = getPaddingTop() + getPaddingBottom();
        Paint.FontMetrics titleFm = mTitleTextPaint.getFontMetrics();
        float titleHeight = getFontHeight(titleFm);
        Paint.FontMetrics addressFm = mAddressTextPaint.getFontMetrics();
        float addressHeight = getFontHeight(addressFm);
        float totalHeight = layoutPadding + mPaddingSize + titleHeight
                + addressHeight + mPaddingSize + mDividerSize;
        return (int) totalHeight;
    }

    private float getFontHeight(Paint.FontMetrics metrics) {
        return (float) (Math.ceil(Math.abs(metrics.top)) +
                Math.ceil(Math.abs(metrics.bottom)));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int height = canvas.getHeight();
        int width = canvas.getWidth();

        // draw paint to clear canvas
        canvas.drawPaint(mBackgroundPaint);
        // Draw the divider across the bottom of the canvas
        float dividerY = height - (mDividerSize / 2);
        canvas.drawLine(0, dividerY, width, dividerY, mDividerPaint);

        canvas.clipRect(mPaddingSize, mPaddingSize, width - mPaddingSize, height - mPaddingSize);

        float textLeft = mPaddingSize;

        if (mIcon != null) {
            mIcon.setBounds(
                    (int) mPaddingSize,
                    (int) mPaddingSize,
                    (int) mPaddingSize + mIcon.getIntrinsicWidth(),
                    (int) mPaddingSize + mIcon.getIntrinsicHeight());
            mIcon.draw(canvas);

            textLeft += mPaddingSize + mIcon.getIntrinsicWidth();
        }

        Paint.FontMetrics titleFm = mTitleTextPaint.getFontMetrics();
        float titleTop = (float) Math.ceil(Math.abs(titleFm.top));
        float titleBottom = (float) Math.ceil(Math.abs(titleFm.bottom));
        float titleBaseline = mPaddingSize + titleTop;
        canvas.drawText(mTitle, textLeft, titleBaseline, mTitleTextPaint);

        Paint.FontMetrics addressFm = mAddressTextPaint.getFontMetrics();
        float addressTop = (float) Math.ceil(Math.abs(addressFm.top));
        float addressBaseline = titleBaseline + titleBottom + addressTop;
        canvas.drawText(mAddress, textLeft, addressBaseline, mAddressTextPaint);
    }

    @Override
    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
        Drawable image = new BitmapDrawable(getResources(), bitmap);
        makeImageAllBlack(image);
        mIcon = image;
        invalidate();
    }

    @Override
    public void onBitmapFailed(Drawable errorDrawable) {
        makeImageAllBlack(errorDrawable);
        mIcon = errorDrawable;
        invalidate();
    }

    @Override
    public void onPrepareLoad(Drawable placeHolderDrawable) {
        makeImageAllBlack(placeHolderDrawable);
        mIcon = placeHolderDrawable;
        invalidate();
    }

    private void makeImageAllBlack(@Nullable Drawable image) {
        if (image != null) {
            image.mutate().setColorFilter(0xFF000000, PorterDuff.Mode.MULTIPLY);
        }
    }

    public void loadIcon(String iconPath) {
        Picasso.with(getContext()).load(iconPath).into(this);
    }

    public void clearIcon() {
        mIcon = null;
        invalidate();
    }
}
