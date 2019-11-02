package me.juangoncalves.blloc.switchview;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.annotation.Nullable;
import androidx.vectordrawable.graphics.drawable.ArgbEvaluator;

public class BllocSwitchView extends View {

    private static final long ANIMATION_DURATION = 330L;
    private static final int ACTUAL_WIDTH = 140;
    private static final int ACTUAL_HEIGHT = 70;
    private static final float MIN_INNER_SHAPE_WIDTH = 1;
    private static final float PADDING = 21;

    private int onBackgroundColor;
    private int offBackgroundColor;
    private boolean checked = true;

    private RectF containerRect = new RectF();
    private RectF innerShapeRect = new RectF();
    private Paint innerShapePaint = new Paint();
    private Paint containerPaint = new Paint();

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            float roundedCornerRadius = containerRect.height() / 2;
            canvas.drawRoundRect(containerRect, roundedCornerRadius, roundedCornerRadius, containerPaint);
            canvas.drawArc(innerShapeRect, 0, 360, true, innerShapePaint);
        }
    }

    public BllocSwitchView(Context context) {
        super(context);
        setSaveEnabled(true);
    }

    public BllocSwitchView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.BllocSwitchView);
        setChecked(ta.getBoolean(R.styleable.BllocSwitchView_sv_checked, true));
        ta.recycle();
        onBackgroundColor = getResources().getColor(R.color.switch_view_background_on);
        offBackgroundColor = getResources().getColor(R.color.switch_view_background_off);
        updateInnerShapePaint();
        updateContainerPaint();
    }

    public BllocSwitchView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
        updateContainerPaint();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            performClick();
        }
        return true;
    }

    @Override
    public boolean performClick() {
        if (checked) {
            getValueAnimatorToShrinkCircle().start();
        } else {
            getValueAnimatorToExpandCircle().start();
        }
        checked = !checked;
        return super.performClick();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        switch (widthMode) {
            case MeasureSpec.EXACTLY:
                width = widthSize;
                break;
            case MeasureSpec.AT_MOST:
                width = Math.min(ACTUAL_WIDTH, widthSize);
                break;
            default:
                width = ACTUAL_WIDTH;
                break;
        }

        int height;
        switch (heightMode) {
            case MeasureSpec.EXACTLY:
                height = heightSize;
                break;
            case MeasureSpec.AT_MOST:
                height = Math.min(ACTUAL_HEIGHT, heightSize);
                break;
            default:
                height = ACTUAL_HEIGHT;
                break;
        }
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        containerRect.right = w;
        containerRect.left = w - ACTUAL_WIDTH;
        int verticalCenter = h / 2;
        containerRect.top = verticalCenter - ACTUAL_HEIGHT / 2;
        containerRect.bottom = verticalCenter + ACTUAL_HEIGHT / 2;
        innerShapeRect.top = containerRect.top + PADDING;
        innerShapeRect.bottom = containerRect.bottom - PADDING;
        // Decide depending on the switch checked whether to draw the full circle (ON) or the straight line (OFF)
        if (isChecked()) {
            innerShapeRect.left = containerRect.left + PADDING;
            innerShapeRect.right = innerShapeRect.left + innerShapeRect.height();
        } else {
            innerShapeRect.right = getEndCoordinateForInnerShape();
            innerShapeRect.left = innerShapeRect.right - MIN_INNER_SHAPE_WIDTH;
        }
    }

    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState customState = new SavedState(superState);
        customState.isChecked = checked;
        return customState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        super.onRestoreInstanceState(state);
        SavedState customState = (SavedState) state;
        setChecked(customState.isChecked);
    }

    private float getEndCoordinateForInnerShape() {
        return containerRect.right - PADDING - innerShapeRect.height() / 2;
    }

    private void updateInnerShapePaint() {
        innerShapePaint.setStyle(Paint.Style.STROKE);
        innerShapePaint.setColor(Color.WHITE);
        innerShapePaint.setStrokeWidth(4);
    }

    private void updateContainerPaint() {
        containerPaint.setStyle(Paint.Style.FILL);
        containerPaint.setColor(getColorForState());
    }

    private int getColorForState() {
        if (checked) {
            return onBackgroundColor;
        } else {
            return offBackgroundColor;
        }
    }

    private AnimatorSet getValueAnimatorToShrinkCircle() {
        ValueAnimator shrinkValueAnimator = ValueAnimator.ofFloat(innerShapeRect.width(), MIN_INNER_SHAPE_WIDTH);
        shrinkValueAnimator.setDuration(ANIMATION_DURATION);
        shrinkValueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        shrinkValueAnimator.addUpdateListener(new InnerShapeWidthUpdateListener());

        ValueAnimator positionAnimator = ValueAnimator.ofFloat(innerShapeRect.left, getEndCoordinateForInnerShape());
        positionAnimator.setDuration(ANIMATION_DURATION);
        positionAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        positionAnimator.addUpdateListener(new InnerShapePositionUpdateListener());

        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), containerPaint.getColor(), offBackgroundColor);
        colorAnimation.setDuration(ANIMATION_DURATION);
        colorAnimation.addUpdateListener(new BackgroundColorUpdateListener());

        AnimatorSet set = new AnimatorSet();
        set.playTogether(shrinkValueAnimator, positionAnimator, colorAnimation);
        return set;
    }

    private AnimatorSet getValueAnimatorToExpandCircle() {
        ValueAnimator shapeAnimator = ValueAnimator.ofFloat(0f, innerShapeRect.height());
        shapeAnimator.setDuration(ANIMATION_DURATION);
        shapeAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        shapeAnimator.addUpdateListener(new InnerShapeWidthUpdateListener());

        ValueAnimator positionAnimator = ValueAnimator.ofFloat(innerShapeRect.left, containerRect.left + PADDING);
        positionAnimator.setDuration(ANIMATION_DURATION);
        positionAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        positionAnimator.addUpdateListener(new InnerShapePositionUpdateListener());

        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), containerPaint.getColor(), onBackgroundColor);
        colorAnimation.setDuration(ANIMATION_DURATION);
        colorAnimation.addUpdateListener(new BackgroundColorUpdateListener());

        AnimatorSet set = new AnimatorSet();
        set.playTogether(shapeAnimator, positionAnimator, colorAnimation);
        return set;
    }


    private class BackgroundColorUpdateListener implements ValueAnimator.AnimatorUpdateListener {
        @Override
        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            int updatedColor = (int) valueAnimator.getAnimatedValue();
            containerPaint.setColor(updatedColor);
            invalidate();
        }
    }

    private class InnerShapeWidthUpdateListener implements ValueAnimator.AnimatorUpdateListener {
        @Override
        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            float updatedWidth = (float) valueAnimator.getAnimatedValue();
            innerShapeRect.right = innerShapeRect.left + updatedWidth;
            invalidate();
        }
    }

    private class InnerShapePositionUpdateListener implements ValueAnimator.AnimatorUpdateListener {
        @Override
        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            float updatedPosition = (float) valueAnimator.getAnimatedValue();
            float width = innerShapeRect.width();
            innerShapeRect.left = updatedPosition;
            innerShapeRect.right = updatedPosition + width;
            invalidate();
        }
    }

    private static class SavedState extends BaseSavedState {
        boolean isChecked;
        private static final int TRUE = 1;
        private static final int FALSE = 0;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            isChecked = in.readInt() == TRUE;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(isChekedAsInt());
        }

        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };

        private int isChekedAsInt() {
            if (isChecked)
                return TRUE;
            else
                return FALSE;
        }

    }

}
