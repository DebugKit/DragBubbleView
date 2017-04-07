package com.boco.dragbubbleview;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TimeInterpolator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;


public class DragBubbleView extends View {

    private Paint mBubblePaint;
    private Paint mTextPaint;
    private Rect mTextRect;
    private Path mBezierPath;
    private RectF mBubble2RectF;
    private RectF mBubble3RectF;
    private Paint mExplosionPaint;
    private Rect mExplosionRect;
    /**
     * 黏质小球半径
     */
    private float mCircleRadius;
    /**
     * 手指拖拽气泡半径
     */
    private float mBubbleRadius;
    /**
     * 气泡颜色
     */
    private int mBubbleColor;
    /**
     * 气泡消息文本
     */
    private String mBubbleText;
    /**
     * 气泡消息文本颜色
     */
    private int mTextColor;
    /**
     * 气泡消息文本大小
     */
    private float mTextSize;


    /**
     * 手指拖拽气泡圆心X坐标
     */
    private float mBubbleCenterX;
    /**
     * 手指拖拽气泡圆心Y坐标
     */
    private float mBubbleCenterY;
    /**
     * 黏质小球圆心X坐标
     */
    private float mCircleCenterX;
    /**
     * 黏质小球圆心Y坐标
     */
    private float mCircleCenterY;

    /**
     * 控制点X坐标
     */
    private float mControlX;
    /**
     * 控制点Y坐标
     */
    private float mControlY;

    private float mCircleStartX;
    private float mCircleStartY;
    private float mBubbleEndX;
    private float mBubbleEndY;
    private float mBubbleStartX;
    private float mBubbleStartY;
    private float mCircleEndX;
    private float mCircleEndY;

    /**
     * 气泡状态
     */
    private int mState;
    /**
     * 默认状态，无法拖动
     */
    private static final int STATE_DEFAULT = 0x00;
    /**
     * 拖拽
     */
    private static final int STATE_DRAG = 0x01;
    /**
     * 移动
     */
    private static final int STATE_MOVE = 0x02;
    /**
     * 消失
     */
    private static final int STATE_DISMISS = 0x03;

    /**
     * 黏质小球和拖拽小球之间的距离
     */
    private float mDistance;
    /**
     * 可拖拽的最大间距
     */
    private float mMaxDistance;

    /**
     * 气泡爆炸的图片ID
     */
    private int[] mExplosionDrawables = {R.mipmap.explosion_one,
            R.mipmap.explosion_two,
            R.mipmap.explosion_three,
            R.mipmap.explosion_four,
            R.mipmap.explosion_five};

    /**
     * 气泡爆炸的bitmap数组
     */
    private Bitmap[] mExplosionBitmaps;

    /**
     * 气泡爆炸进行到当前第几张
     */
    private int mCurrentExplosionIndex;
    /**
     * 气泡爆炸是否开始
     */
    private boolean mIsExplosionAnimStart = false;

    public DragBubbleView(Context context) {
        this(context, null);
    }

    public DragBubbleView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DragBubbleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.DragBubbleView,
                defStyleAttr, 0);
        mBubbleRadius = typedArray.getDimension(R.styleable.DragBubbleView_bubbleRadius,
                DensityUtils.dp2px(context, 12f));
        mBubbleColor = typedArray.getColor(R.styleable.DragBubbleView_bubbleColor, Color.RED);
        mBubbleText = typedArray.getString(R.styleable.DragBubbleView_text);
        mTextColor = typedArray.getColor(R.styleable.DragBubbleView_textColor, Color.WHITE);
        mTextSize = typedArray.getDimension(R.styleable.DragBubbleView_textSize, DensityUtils
                .dp2px(context, 12));
        typedArray.recycle();
        init();
    }

    private void init() {
        mState = STATE_DEFAULT;
        mCircleRadius = mBubbleRadius;
        mMaxDistance = 8 * mBubbleRadius;
        mBubblePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBubblePaint.setColor(mBubbleColor);
        mBubblePaint.setStyle(Paint.Style.FILL);

        mBezierPath = new Path();

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(mTextColor);
        mTextPaint.setTextSize(mTextSize);
        mTextRect = new Rect();

        mBubble2RectF = new RectF();
        mBubble3RectF = new RectF();

        mExplosionPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mExplosionPaint.setFilterBitmap(true);
        mExplosionRect = new Rect();
        mExplosionBitmaps = new Bitmap[mExplosionDrawables.length];
        for (int i = 0; i < mExplosionDrawables.length; i++) {
            mExplosionBitmaps[i] = BitmapFactory.decodeResource(getResources(),
                    mExplosionDrawables[i]);
        }
    }

    private void setBubbleRect(float bubbleCenterX, float bubbleCenterY) {
        mBubble2RectF.set(bubbleCenterX - mBubbleRadius * 4 / 3,
                bubbleCenterY - mBubbleRadius * 1,
                bubbleCenterX + mBubbleRadius * 4 / 3,
                bubbleCenterY + mBubbleRadius * 1);
        mBubble3RectF.set(bubbleCenterX - mBubbleRadius * 3 / 2,
                bubbleCenterY - mBubbleRadius * 1,
                bubbleCenterX + mBubbleRadius * 3 / 2,
                bubbleCenterY + mBubbleRadius * 1
        );
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int width;
        int height;
        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else {
            width = (int) (2 * mBubbleRadius);
            if (widthMode == MeasureSpec.AT_MOST) {
                width = Math.min(width, widthSize);
            }
        }
        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else {
            height = (int) (2 * mBubbleRadius);
            if (heightMode == MeasureSpec.AT_MOST) {
                height = Math.min(width, heightSize);
            }
        }
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        initCenter(w, h);
    }

    private void initCenter(int w, int h) {
        mBubbleCenterX = w / 2;
        mBubbleCenterY = h / 2;
        setBubbleRect(mBubbleCenterX, mBubbleCenterY);
        if (mBubbleText.length() <= 1) {
            mCircleCenterX = mBubbleCenterX;
            mCircleCenterY = mBubbleCenterY;
        } else if (mBubbleText.length() > 1 && mBubbleText.length() <= 2) {
            mCircleCenterX = (mBubble2RectF.left + mBubble2RectF.right) / 2;
            mCircleCenterY = (mBubble2RectF.top + mBubble2RectF.bottom) / 2;
        } else {
            mCircleCenterX = (mBubble3RectF.left + mBubble3RectF.right) / 2;
            mCircleCenterY = (mBubble3RectF.top + mBubble3RectF.bottom) / 2;
        }
        mState = STATE_DEFAULT;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                getParent().requestDisallowInterceptTouchEvent(true);
                if (mState != STATE_DISMISS) {
                    mDistance = (float) Math.hypot(event.getX() - mBubbleCenterX, event.getY() -
                            mBubbleCenterY);
                    if (mDistance < mBubbleRadius + mMaxDistance / 4) {
                        //当指尖坐标在圆内的时候，才认为是可拖拽的
                        //一般气泡比较小，增加(mMaxDistance/4)像素是为了更轻松的拖拽
                        mState = STATE_DRAG;
                    } else {
                        mState = STATE_DEFAULT;
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mState != STATE_DEFAULT) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                    mBubbleCenterX = event.getX();
                    mBubbleCenterY = event.getY();
                    setBubbleRect(mBubbleCenterX, mBubbleCenterY);
                    mDistance = (float) Math.hypot(mBubbleCenterX - mCircleCenterX, mBubbleCenterY -
                            mCircleCenterY);
                    if (mState == STATE_DRAG) {//如果可以拖拽
                        //间距小于可黏连的最大距离
                        //减去(mMaxDistance/4) 的像素大小，是为了让黏连小球半径到一个较小值快消失时直接消失
                        if (mDistance < mMaxDistance - mMaxDistance / 4) {
                            //使黏质小球半径渐渐变小
                            mCircleRadius = mBubbleRadius - mDistance / 10;
                            if (mOnBubbleStateListener != null) {
                                mOnBubbleStateListener.onDrag();
                            }
                        } else {//间距大于可连接间距
                            //改为移动状态
                            mState = STATE_MOVE;
                            if (mOnBubbleStateListener != null) {
                                mOnBubbleStateListener.onMove();
                            }
                        }
                    }
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
                getParent().requestDisallowInterceptTouchEvent(false);
                if (mState == STATE_DRAG) {//正在拖拽时松开手指，气泡恢复原来的位置并颤动一下
                    setBubbleRestoreAnim();
                } else if (mState == STATE_MOVE) {
                    if (mDistance < 2 * mBubbleRadius) {//如果移动状态下间距回到两倍半径以内，我们认为用户不想取消该气泡
                        setBubbleRestoreAnim();
                    } else {//气泡消失
                        setBubbleDismissAnim();
                    }
                }
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //画拖拽气泡
        if (!TextUtils.isEmpty(mBubbleText)) {
            if (mState != STATE_DISMISS) {
                mBubblePaint.setColor(Color.RED);

                if (mBubbleText.length() <= 1) {
                    canvas.drawCircle(mBubbleCenterX, mBubbleCenterY, mBubbleRadius, mBubblePaint);
                } else if (mBubbleText.length() > 1 && mBubbleText.length() <= 2) {
                    canvas.drawRoundRect(mBubble2RectF, mBubbleRadius, mBubbleRadius, mBubblePaint);
                } else {
                    canvas.drawRoundRect(mBubble3RectF, mBubbleRadius, mBubbleRadius, mBubblePaint);
                }
            }
            if (mState == STATE_DRAG && mDistance < mMaxDistance - mMaxDistance / 4) {
                //画黏质小圆
                canvas.drawCircle(mCircleCenterX, mCircleCenterY, mCircleRadius, mBubblePaint);
                //计算二阶贝塞尔曲线的起点，终点和控制点
                calculateBezierCoordinate();
                mBezierPath.reset();
                mBezierPath.moveTo(mCircleStartX, mCircleStartY);
                mBezierPath.quadTo(mControlX, mControlY, mBubbleEndX, mBubbleEndY);
                mBezierPath.lineTo(mBubbleStartX, mBubbleStartY);
                mBezierPath.quadTo(mControlX, mControlY, mCircleEndX, mCircleEndY);
                mBezierPath.close();
                canvas.drawPath(mBezierPath, mBubblePaint);
            }
            //画消息个数文本
            if (mState != STATE_DISMISS && !TextUtils.isEmpty(mBubbleText)) {
                mTextPaint.getTextBounds(mBubbleText, 0, mBubbleText.length(), mTextRect);
                if (mBubbleText.length() <= 1) {
                    canvas.drawText(mBubbleText,
                            mBubbleCenterX - mTextRect.width() / 2,
                            mBubbleCenterY + mTextRect.height() / 2,
                            mTextPaint);
                } else if (mBubbleText.length() > 1 && mBubbleText.length() <= 2) {
                    canvas.drawText(mBubbleText,
                            (mBubble2RectF.left + mBubble2RectF.right) / 2 - mTextRect.width() / 2,
                            (mBubble2RectF.top + mBubble2RectF.bottom) / 2 + mTextRect.height() / 2,
                            mTextPaint);
                } else {
                    canvas.drawText(mBubbleText,
                            (mBubble3RectF.left + mBubble3RectF.right) / 2 - mTextRect.width() / 2,
                            (mBubble3RectF.top + mBubble3RectF.bottom) / 2 + mTextRect.height() / 2,
                            mTextPaint);
                }
            }

            if (mIsExplosionAnimStart && mCurrentExplosionIndex < mExplosionDrawables.length) {
                mExplosionRect.set((int) (mBubbleCenterX - mBubbleRadius),
                        (int) (mBubbleCenterY - mBubbleRadius),
                        (int) (mBubbleCenterX + mBubbleRadius),
                        (int) (mBubbleCenterY + mBubbleRadius));
                canvas.drawBitmap(mExplosionBitmaps[mCurrentExplosionIndex], null,
                        mExplosionRect, mExplosionPaint);
            }
        }
    }

    private void calculateBezierCoordinate() {
        //计算控制点坐标
        mControlX = (mBubbleCenterX + mCircleCenterX) / 2;
        mControlY = (mBubbleCenterY + mCircleCenterY) / 2;
        //计算两条贝塞尔曲线的终点和起点
        float sin = (mBubbleCenterY - mCircleCenterY) / mDistance;
        float cos = (mBubbleCenterX - mCircleCenterX) / mDistance;

        mCircleStartX = mCircleCenterX - mCircleRadius * sin;
        mCircleStartY = mCircleCenterY + mCircleRadius * cos;

        mBubbleEndX = mBubbleCenterX - mBubbleRadius * sin;
        mBubbleEndY = mBubbleCenterY + mBubbleRadius * cos;

        mBubbleStartX = mBubbleCenterX + mBubbleRadius * sin;
        mBubbleStartY = mBubbleCenterY - mBubbleRadius * cos;

        mCircleEndX = mCircleCenterX + mCircleRadius * sin;
        mCircleEndY = mCircleCenterY - mCircleRadius * cos;
    }

    /**
     * 设置气泡复原的动画
     */
    private void setBubbleRestoreAnim() {
        ValueAnimator animator = ValueAnimator.ofObject(new PointFEvaluator(),
                new PointF(mBubbleCenterX, mBubbleCenterY),
                new PointF(mCircleCenterX, mCircleCenterY));
        animator.setDuration(500);
        //使用OvershootInterpolator差值器达到颤动效果
//        animator.setInterpolator(new OvershootInterpolator(5));
        animator.setInterpolator(new TimeInterpolator() {
            @Override
            public float getInterpolation(float input) {
                //http://inloop.github.io/interpolator/
                float f = 0.571429f;
                return (float) (Math.pow(2, -4 * input) * Math.sin((input - f / 4) * (2 * Math
                        .PI) / f) + 1);
            }
        });
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                PointF pointF = (PointF) animation.getAnimatedValue();
                mBubbleCenterX = pointF.x;
                mBubbleCenterY = pointF.y;
                setBubbleRect(mBubbleCenterX, mBubbleCenterY);
                invalidate();
            }
        });

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mState = STATE_DEFAULT;
                if (mOnBubbleStateListener != null) {
                    //动画结束后状态改为默认
                    mOnBubbleStateListener.onRestore();
                }
            }
        });
        animator.start();
    }

    /**
     * 设置气泡消失的动画
     */
    private void setBubbleDismissAnim() {
        mState = STATE_DISMISS;
        mIsExplosionAnimStart = true;
        if (mOnBubbleStateListener != null) {
            mOnBubbleStateListener.onDismiss();
        }
        ValueAnimator animator = ValueAnimator.ofInt(0, mExplosionDrawables.length);
        animator.setInterpolator(new LinearInterpolator());
        animator.setDuration(500);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mCurrentExplosionIndex = (int) animation.getAnimatedValue();
                invalidate();
            }
        });

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mIsExplosionAnimStart = false;
            }
        });
        animator.start();
    }

    public interface OnBubbleStateListener {
        /**
         * 拖拽气泡
         */
        void onDrag();

        /**
         * 移动气泡
         */
        void onMove();

        /**
         * 恢复气泡
         */
        void onRestore();

        /**
         * 气泡消失
         */
        void onDismiss();
    }

    private OnBubbleStateListener mOnBubbleStateListener;

    public void setOnBubbleStateListener(OnBubbleStateListener onBubbleStateListener) {
        mOnBubbleStateListener = onBubbleStateListener;
    }

    private class PointFEvaluator implements TypeEvaluator<PointF> {
        @Override
        public PointF evaluate(float fraction, PointF startValue, PointF endValue) {
            float x = startValue.x + (endValue.x - startValue.x) * fraction;
            float y = startValue.y + (endValue.y - startValue.y) * fraction;
            return new PointF(x, y);
        }
    }


    public void setText(String text) {
        mBubbleText = text;
        invalidate();
    }

    public void reCreate() {
        initCenter(getWidth(), getHeight());
        invalidate();
    }
}
