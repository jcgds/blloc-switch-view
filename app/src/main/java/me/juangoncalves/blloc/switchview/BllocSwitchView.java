package me.juangoncalves.blloc.switchview;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.annotation.Nullable;
import androidx.vectordrawable.graphics.drawable.ArgbEvaluator;

public class BllocSwitchView extends View {

    private enum State {
        ON, OFF
    }

    private static final long ANIMATION_DURATION = 330L;
    private static final int ACTUAL_WIDTH = 140;
    private static final int ACTUAL_HEIGHT = 70;
    private static final float PADDING = 21;

    private Paint innerShapePaint = getInnerShapePaint();
    private Paint containerPaint = getContainerPaint();
    private int onBackgroundColor;
    private int offBackgroundColor;

    private State state = State.OFF;
    private RectF containerRect = new RectF();
    private RectF innerShapeRect = new RectF();

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
    }

    public BllocSwitchView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        onBackgroundColor = getResources().getColor(R.color.switch_view_background_on);
        offBackgroundColor = getResources().getColor(R.color.switch_view_background_off);
    }

    public BllocSwitchView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private AnimatorSet getValueAnimatorToShrinkCircle() {
        ValueAnimator shrinkValueAnimator = ValueAnimator.ofFloat(innerShapeRect.width(), 0f);
        shrinkValueAnimator.setDuration(ANIMATION_DURATION);
        shrinkValueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        shrinkValueAnimator.addUpdateListener(new InnerShapeWidthUpdateListener());

        ValueAnimator positionAnimator = ValueAnimator.ofFloat(innerShapeRect.left, containerRect.right - PADDING - innerShapeRect.height() / 2);
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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (state == State.OFF) {
                AnimatorSet animatorSet = getValueAnimatorToShrinkCircle();
                animatorSet.start();
                state = State.ON;
            } else {
                AnimatorSet anim = getValueAnimatorToExpandCircle();
                anim.start();
                state = State.OFF;
            }
        }
        return true;
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
        innerShapeRect.left = containerRect.left + PADDING;
        innerShapeRect.right = innerShapeRect.left + innerShapeRect.height();
    }

    private Paint getInnerShapePaint() {
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(4);
        return paint;
    }

    private Paint getContainerPaint() {
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(getResources().getColor(R.color.switch_view_background_on));
        return paint;
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

}
