package com.testdemo.togglebutton.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.Interpolator;
import android.widget.Scroller;

import com.testdemo.togglebutton.R;


/**自定义ToggleButton控件
 * Created by Mengy on 2016/6/1.
 */
public class MToggleButton extends View implements View.OnTouchListener{
    private static final int SCROLL_DURATION = 600;
    public static final int STATE_LEFT = 1;
    public static final int STATE_RIGHT = 2;
    private static final int TOUCH_STATE_DOWN = 1;
    private static final int TOUCH_STATE_NONE = 0;
    private static final int TOUCH_STATE_UP = 2;
    private Paint mBgPaint;
    private RectF mBgRectF;
    private CircleItem mCircleItem;
    private Paint mCirclePaint;
    private float mCircleSize;
    private ColorInterpolator mColorInterpolator;
    private long mDownTime;
    private float mLastMotionX;
    private int mLeftColor;
    private int mRightColor;
    private int mRollColor;
    private Scroller mScroller;
    private int mState;
    private OnStateChangeListener mStateChangeListener;
    private int mTouchState;

    public MToggleButton(Context context) {
        super(context);
        init(context,null);
    }

    public MToggleButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context,attrs);
    }

    public MToggleButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context,attrs);
    }

    private void init(Context context, AttributeSet paramAttributeSet) {
        TypedArray typedArray = getResources().obtainAttributes(paramAttributeSet, R.styleable.MToggleButton);
        mLeftColor = typedArray.getColor(R.styleable.MToggleButton_leftBackground, 262627);
        mRightColor = typedArray.getColor(R.styleable.MToggleButton_rightBackground, 113700);
        typedArray.recycle();
        setClickable(true);
        setOnTouchListener(this);
        mCirclePaint = new Paint();
        mCirclePaint.setAntiAlias(true);
        mCirclePaint.setColor(Color.WHITE);
        mCircleItem = new CircleItem(0.0F, 0.0F);

        mBgPaint = new Paint();
        mBgPaint.setAntiAlias(true);
        mBgPaint.setColor(mLeftColor);
        mBgRectF = new RectF();

        mRollColor = mLeftColor;
        mColorInterpolator = new ColorInterpolator(mLeftColor, mRightColor);
        mTouchState = TOUCH_STATE_NONE;

        mScroller = new Scroller(context, new Interpolator() {
            @Override
            public float getInterpolation(float input) {
                input -= 1;
                return input * input * input * input * input + 1;
            }
        });

        switchState(STATE_LEFT);
    }

    private boolean checkToRoll(MotionEvent paramMotionEvent) {
        if ((paramMotionEvent.getY() < 0.0F) || (paramMotionEvent.getY() > getHeight())) {
            rollEnd();
            return false;
        }
        return true;
    }
    private void rollBy(int diff) {
        int rollX = -getScrollX();
        int offset = diff;
        if (rollX + diff < mCircleItem.rollStartX) {
            offset = (int) (mCircleItem.rollStartX - rollX);
        }
        if (rollX + offset > mCircleItem.rollEndX) {
            offset = (int) (mCircleItem.rollEndX - rollX);
        }

        mBgRectF.offset(-offset, 0);
        scrollBy(-offset, 0);
        mRollColor = mColorInterpolator.getColor(Math.abs(getScrollX()) / (mCircleItem.rollEndX - mCircleItem.rollStartX));
    }

    private void rollEnd() {
        if (System.currentTimeMillis() - mDownTime < ViewConfiguration.getTapTimeout()) {
            toggle();
            return;
        }
        int rollX = -getScrollX();
        boolean slideLeft = rollX < mCircleItem.centerRollX;
        if (slideLeft) {
            rollTo(0.0F, 0.0F);
        } else {
            rollTo(-mCircleItem.rollEndX, 0.0F);
        }
        invalidate();
    }

    private void rollTo(float paramFloat1, float paramFloat2) {
        this.mBgRectF.offsetTo(paramFloat1, paramFloat2);
        scrollTo((int) paramFloat1, (int) paramFloat2);
        this.mRollColor = this.mColorInterpolator.getColor(Math.abs(getScrollX()) / (this.mCircleItem.rollEndX - this.mCircleItem.rollStartX));
    }

    private void smoothRollTo(float paramFloat1, float paramFloat2) {
        int i = (int) (paramFloat1 - getScrollX());
        int j = (int) (paramFloat2 - getScrollY());
        this.mScroller.startScroll(getScrollX(), getScrollY(), i, j, SCROLL_DURATION);
        invalidate();
    }

    private void switchState(int paramInt) {
        this.mState = paramInt;
    }

    public void computeScroll() {
        if (this.mScroller.computeScrollOffset()) {
            rollTo(this.mScroller.getCurrX(), this.mScroller.getCurrY());
            postInvalidate();
            return;
        }
        if ((Math.abs(getScrollX()) == this.mCircleItem.rollStartX) && (this.mTouchState == TOUCH_STATE_UP)) {
            switchState(STATE_LEFT);
            this.mTouchState = TOUCH_STATE_NONE;
            if (this.mStateChangeListener != null) {
                mStateChangeListener.onStateChange(mState == STATE_RIGHT);
            }
            return;
        }

        if ((Math.abs(getScrollX()) == this.mCircleItem.rollEndX) && (this.mTouchState == TOUCH_STATE_UP)) {
            switchState(STATE_RIGHT);
            this.mTouchState = TOUCH_STATE_NONE;
            if (mStateChangeListener != null) {
                mStateChangeListener.onStateChange(mState == STATE_RIGHT);
            }
            return;
        }
    }

    public boolean getToggleState() {
        return this.mState == STATE_RIGHT;
    }

    public void initState(boolean open) {
        switchState(open ? STATE_RIGHT : STATE_LEFT);
    }

    public boolean isOpen() {
        return this.mState == STATE_RIGHT;
    }

    protected void onDraw(Canvas paramCanvas) {
        this.mBgPaint.setColor(this.mRollColor);
        paramCanvas.drawRoundRect(this.mBgRectF, getHeight() / 2.0F, getHeight() / 2.0F, this.mBgPaint);
        paramCanvas.drawCircle(this.mCircleItem.posX, this.mCircleItem.posY, this.mCircleSize / 2.0F, this.mCirclePaint);
    }

    protected void onLayout(boolean paramBoolean, int left, int top, int right, int bottom) {
        int height = bottom - top;
        this.mCircleItem.posX = (height / 2.0F);
        this.mCircleItem.posY = (height / 2.0F);
        if (this.mState == STATE_RIGHT) {
            rollTo(-this.mCircleItem.rollEndX, 0.0F);
            return;
        }
        rollTo(0.0F, 0.0F);
    }

    protected void onMeasure(int paramInt1, int paramInt2) {
        int i = MeasureSpec.getSize(paramInt1);
        int j = MeasureSpec.getSize(paramInt2);
        this.mCircleSize = (j - getPaddingTop() - getPaddingBottom());
        this.mCircleItem.rollStartX = 0.0F;
        this.mCircleItem.rollEndX = (i - j);
        this.mCircleItem.centerRollX = ((i - j) / 2);
        this.mBgRectF.set(0.0F, 0.0F, i, j);
        super.onMeasure(paramInt1, paramInt2);
    }

    public boolean onTouch(View paramView, MotionEvent motionEvent) {
        getParent().requestDisallowInterceptTouchEvent(true);
        switch (motionEvent.getAction()) {
            default:
                break;
            case MotionEvent.ACTION_DOWN:
                mTouchState = TOUCH_STATE_DOWN;
                mLastMotionX = motionEvent.getX();
                mDownTime = System.currentTimeMillis();
                break;
            case MotionEvent.ACTION_MOVE:
                if (checkToRoll(motionEvent)) {
                    int xDiff = (int) (motionEvent.getX() - mLastMotionX);
                    mLastMotionX = motionEvent.getX();
                    rollBy(xDiff);
                }
                break;
            case MotionEvent.ACTION_UP:
                mTouchState = 2;
                rollEnd();
                break;
        }
        return true;

    }

    public void setOnStateChangeListener(OnStateChangeListener paramOnStateChangeListener) {
        this.mStateChangeListener = paramOnStateChangeListener;
    }

    public void setToggleState(int state) {
        switch (state) {
            default:
                throw new IllegalArgumentException("ToggleButton setToggleState 参数state不符合要求");
            case STATE_LEFT:
                mTouchState = TOUCH_STATE_UP;
                smoothRollTo(0.0F, 0.0F);
                switchState(state);
                return;
            case STATE_RIGHT:
                mTouchState = TOUCH_STATE_UP;
                smoothRollTo(-this.mCircleItem.rollEndX, 0.0F);
                switchState(state);
                return;
        }
    }

    public void setToggleState(Boolean open) {
        setToggleState(open ? STATE_RIGHT : STATE_LEFT);
    }

    public void toggle() {
        setToggleState(!(mState == STATE_RIGHT));
    }

    public class CircleItem {
        public float centerRollX;
        public float posX;
        public float posY;
        public float rollEndX;
        public float rollStartX;

        public CircleItem(float paramFloat1, float paramFloat2) {
            this.posX = paramFloat1;
            this.posY = paramFloat2;
        }
    }

    public class ColorInterpolator {
        private int mStartColor;
        private int mEndColor;
        private int[] mStartRGB;
        private int[] mEndRGB;

        public ColorInterpolator(int startColor, int endColor) {
            this.mStartColor = startColor;
            this.mEndColor = endColor;
            this.mStartRGB = new int[4];
            this.mEndRGB = new int[4];
            int colorShift = 0;
            for (int i = 0; i < 4; i++) {
                this.mStartRGB[i] = (this.mStartColor >> colorShift & 0xFF);
                this.mEndRGB[i] = (this.mEndColor >> colorShift & 0xFF);
                colorShift += 8;
            }
        }

        public int getColor(float paramFloat) {
            int[] arrayOfInt = new int[4];
            for (int i = 0; i < 4; i++) {
                arrayOfInt[i] = ((int) (this.mStartRGB[i] + (this.mEndRGB[i] - this.mStartRGB[i]) * paramFloat));
            }
            return Color.argb(arrayOfInt[3], arrayOfInt[2], arrayOfInt[1], arrayOfInt[0]);
        }
    }

    public abstract interface OnStateChangeListener {
        public abstract void onStateChange(boolean bool);
    }

}
