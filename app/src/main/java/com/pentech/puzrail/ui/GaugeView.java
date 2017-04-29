package com.pentech.puzrail.ui;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.graphics.Typeface;
import android.support.annotation.ColorInt;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;

/**
 * Created by c0932 on 2017/03/17.
 */

public class GaugeView extends View {
    private enum Mode {
        SINGLE_COLOR,
        GRADATION,
        RAINBOW
    }

    private float MARGIN_UNIT = 1f;
    private float MARGIN_INNER_RING = 2f;

    private static final float PERCENTAGE_RING_STROKE = 0.25f;
    private static final float PERCENTAGE_INNER_RING_STROKE = 0.7f;

    private static final int COLOR_BASE_RING = Color.rgb(242, 242, 242);
    private static final int COLOR_BASE_RING_INNER = Color.rgb(229, 229, 229);
    private static final int COLOR_SPLIT_LINE = Color.WHITE;
    private static final int COLOR_SECOND_LOOP_BASE_RING = Color.rgb(242, 89, 12);
    private static final int COLOR_SECOND_LOOP = Color.rgb(245, 139, 85);
    private static final int COLOR_CURRENT_POINT = Color.rgb(155, 155, 155);
    private static final int COLOR_UNIT_TEXT = Color.rgb(128,128,128);

    private String TAG = "GaugeView";

    private float density;

    // data
    private boolean initialized;
    private Mode mode;
    private int value;
    private String text;
    private float textSize;
    private float unitSize;
    private String unit;
    private int color;
    private int current;
    private boolean animate;
    private boolean limitBreak;

    // gradation mode
    private int colorStep;

    // rainbow mode
    @ColorInt
    private int[] colors;
    private float[] positions;
    private Shader arcShader;

    // for animation
    private ObjectAnimator animator;
    private int phase;

    public void setPhase(int phase) {
        this.phase = phase;
        invalidate();
    }

    // for draw
    private Paint paint;
    private Rect labelRect;
    private RectF rectF;
    private Path currentPointPath;

    public GaugeView(Context context) {
        this(context, null);
    }

    public GaugeView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GaugeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        density = displayMetrics(getContext()).density;

        // 補正
        MARGIN_UNIT *= density;
        MARGIN_INNER_RING *= density;

        paint = new Paint();
        labelRect = new Rect();
        rectF = new RectF();

        // label setting
        paint.setAntiAlias(true);
        paint.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (initialized) {
            // center
            float centerX = canvas.getWidth() / 2;
            float centerY = canvas.getHeight() / 2;

            boolean isLandscape = canvas.getWidth() > canvas.getHeight();

            float ringStrokeWidth;
            float ringRadius;
            if (isLandscape) {
                ringStrokeWidth = canvas.getHeight() * PERCENTAGE_RING_STROKE;
                ringRadius = (canvas.getHeight() / 2) - ringStrokeWidth;
            } else {
                ringStrokeWidth = canvas.getWidth() * PERCENTAGE_RING_STROKE;
                ringRadius = (canvas.getWidth() / 2) - ringStrokeWidth;
            }
            float innerRingStrokeWidth = ringStrokeWidth * PERCENTAGE_INNER_RING_STROKE;

            if (isLandscape) {
                rectF.set(centerX - ringRadius - ringStrokeWidth / 2, ringStrokeWidth / 2, centerX + ringRadius + ringStrokeWidth / 2, canvas.getHeight() - ringStrokeWidth / 2);
            } else {
                rectF.set(ringStrokeWidth / 2, centerY - ringRadius - ringStrokeWidth / 2, canvas.getWidth() - ringStrokeWidth / 2, centerY + ringRadius + ringStrokeWidth / 2);
            }

            // サイズ調整
            if (!TextUtils.isEmpty(text) && textSize == 0) {
                do {
                    textSize += density;
                    paint.setTextSize(textSize);
                    paint.getTextBounds(text, 0, text.length(), labelRect);
                }
                while (labelRect.width() < ringRadius * 1.25 && labelRect.height() < ringRadius);
            }
            if (!TextUtils.isEmpty(unit) && unitSize == 0) {
                do {
                    unitSize += density;
                    paint.setTextSize(unitSize);
                    paint.getTextBounds(unit, 0, unit.length(), labelRect);
                }
                while (labelRect.height() < ringRadius / 2);
            }

            // base ring
            paint.setColor(COLOR_BASE_RING);
            paint.setStrokeWidth(ringStrokeWidth);
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawCircle(centerX, centerY, ringRadius + ringStrokeWidth / 2, paint);

            switch (mode) {
                case RAINBOW: {
                    if (arcShader == null) {
                        arcShader = new SweepGradient(centerX, centerY, colors, positions);
                    }
                    // ring
                    paint.setShader(arcShader);
                    paint.setStrokeWidth(innerRingStrokeWidth);
                    paint.setStyle(Paint.Style.STROKE);
                    canvas.drawCircle(centerX, centerY, ringRadius + innerRingStrokeWidth / 2 + (ringStrokeWidth - innerRingStrokeWidth) / 2, paint);

                    // text
                    paint.setShader(null);
                    paint.setColor(COLOR_CURRENT_POINT);
                    paint.setTypeface(Typeface.DEFAULT_BOLD);
                    paint.setStyle(Paint.Style.FILL);
                    paint.setTextSize(textSize);
                    paint.getTextBounds(text, 0, text.length(), labelRect);
                    canvas.drawText(text, centerX, centerY + labelRect.height() / 3, paint);
                    break;
                }
                case SINGLE_COLOR:
                case GRADATION: {
                    paint.setShader(null);

                    // base ring inner
                    paint.setColor(COLOR_BASE_RING_INNER);
                    paint.setStrokeWidth(innerRingStrokeWidth);
                    paint.setStyle(Paint.Style.STROKE);
                    canvas.drawCircle(centerX, centerY, ringRadius + innerRingStrokeWidth / 2 + (ringStrokeWidth - innerRingStrokeWidth) / 2, paint);

                    // inner fill
                    if (limitBreak) {
//                        paint.setColor(COLOR_SECOND_LOOP_BASE_RING);
                        paint.setColor(color);
                        paint.setStyle(Paint.Style.FILL);
                        canvas.drawCircle(centerX, centerY, ringRadius, paint);

                        paint.setColor(Color.WHITE);
                        paint.setStrokeWidth(MARGIN_INNER_RING);
                        paint.setStyle(Paint.Style.STROKE);
                        canvas.drawCircle(centerX, centerY, ringRadius, paint);
                    }

                    if (mode == Mode.GRADATION && !limitBreak) {
                        color = colors[(phase / colorStep) % colors.length];
                    }

                    // ring
                    if (isLandscape) {
                        rectF.set(centerX - ringRadius - ringStrokeWidth / 2, ringStrokeWidth / 2, centerX + ringRadius + ringStrokeWidth / 2, canvas.getHeight() - ringStrokeWidth / 2);
                    } else {
                        rectF.set(ringStrokeWidth / 2, centerY - ringRadius - ringStrokeWidth / 2, canvas.getWidth() - ringStrokeWidth / 2, centerY + ringRadius + ringStrokeWidth / 2);
                    }
                    if (limitBreak) {
//                        paint.setColor(COLOR_SECOND_LOOP_BASE_RING);
                        paint.setColor(color);
                    } else {
                        paint.setColor(color);
                    }
                    paint.setStrokeWidth(innerRingStrokeWidth);
                    paint.setStyle(Paint.Style.STROKE);
                    canvas.drawArc(rectF, -90, 360 * (Math.min(phase, 100) / 100f), false, paint);

                    // second loop ring
                    if (limitBreak) {
                        if (phase > 100) {
//                            paint.setColor(COLOR_SECOND_LOOP);
                            paint.setColor(color);
                            canvas.drawArc(rectF, -90, 360 * (Math.min(phase - 100, 100) / 100f), false, paint);
                        }

                        // text color
                        paint.setColor(Color.WHITE);
                    }

                    // value
                    paint.setTypeface(Typeface.DEFAULT_BOLD);
                    paint.setStyle(Paint.Style.FILL);
                    paint.setTextSize(textSize);
                    paint.getTextBounds(text, 0, text.length(), labelRect);
                    canvas.drawText(String.valueOf(phase), centerX, centerY + labelRect.height() / 3, paint);
                    break;
                }
            }

            // unit
            paint.setTypeface(Typeface.DEFAULT);
            paint.setColor(COLOR_UNIT_TEXT);
//            paint.setColor(COLOR_BASE_RING_INNER);
            paint.setTextSize(unitSize);
            paint.getTextBounds(unit, 0, unit.length(), labelRect);
            canvas.drawText(unit, centerX, centerY + ringRadius - MARGIN_UNIT, paint);

            // split line
            paint.setColor(COLOR_SPLIT_LINE);
            paint.setStrokeWidth(density);
            for (int i = 0; i < 360; i += 36) {
                float startX = centerX + (ringRadius * (float) Math.sin(Math.toRadians(i)));
                float stopX = centerX + ((ringRadius + ringStrokeWidth) * (float) Math.sin(Math.toRadians(i)));
                float startY = centerY + (ringRadius * (float) Math.cos(Math.toRadians(i)));
                float stopY = centerY + ((ringRadius + ringStrokeWidth) * (float) Math.cos(Math.toRadians(i)));

                canvas.drawLine(startX, startY, stopX, stopY, paint);
            }

            // current point
            paint.setColor(COLOR_CURRENT_POINT);
            if (currentPointPath == null) {
                currentPointPath = new Path();
                float distance = (ringStrokeWidth - innerRingStrokeWidth) / 4f;
                float pointCenterX = centerX + ((ringRadius + ringStrokeWidth - distance) * (float) Math.cos(Math.toRadians(current)));
                float pointCenterY = centerY + ((ringRadius + ringStrokeWidth - distance) * (float) Math.sin(Math.toRadians(current)));
                float pointTop1X = pointCenterX + (distance * (float) Math.cos(Math.toRadians(current + 60)));
                float pointTop1Y = pointCenterY + (distance * (float) Math.sin(Math.toRadians(current + 60)));
                float pointTop2X = pointCenterX + (distance * (float) Math.cos(Math.toRadians(current + 180)));
                float pointTop2Y = pointCenterY + (distance * (float) Math.sin(Math.toRadians(current + 180)));
                float pointTop3X = pointCenterX + (distance * (float) Math.cos(Math.toRadians(current + 300)));
                float pointTop3Y = pointCenterY + (distance * (float) Math.sin(Math.toRadians(current + 300)));
                currentPointPath.moveTo(pointTop1X, pointTop1Y);
                currentPointPath.lineTo(pointTop2X, pointTop2Y);
                currentPointPath.lineTo(pointTop3X, pointTop3Y);
                currentPointPath.close();
            }
            canvas.drawPath(currentPointPath, paint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (animate) {
            startAnimation(0);
        }
        return super.onTouchEvent(event);
    }

    private void startAnimation(int startDelay) {
        if(this.animator != null && this.animator.isRunning()){
            this.animator.end();
            this.animator.cancel();
            clearAnimation();
            this.animator = null;
        }
        this.animator = ObjectAnimator.ofInt(this, "phase", 0, this.value).setDuration(Math.min(Math.max(this.value, 20), 200) * 10);
        this.animator.setInterpolator(new AccelerateDecelerateInterpolator());
        this.animator.setStartDelay(startDelay);
        this.animator.start();
    }

    public void setData(int value, String unit, @ColorInt int color, int current, boolean animate) {
        this.value = value;
        this.text = String.valueOf(value);
        this.textSize = 0;
        this.unitSize = 0;
        this.unit = TextUtils.isEmpty(unit) ? "" : unit;
        this.color = color;
        this.current = current - 90;
        this.animate = animate;
        this.limitBreak = value >= 100;
        mode = Mode.SINGLE_COLOR;

        initialized = true;

        if (animate) {
            startAnimation(500);
        } else {
            phase = value;
            invalidate();
        }
    }

    public void setData(int value, String unit, @ColorInt int[] colors, int colorStep, int current, boolean animate) {
        this.value = value;
        text = String.valueOf(value);
        textSize = 0;
        unitSize = 0;
        this.colors = colors;
        this.colorStep = colorStep;
        this.unit = TextUtils.isEmpty(unit) ? "" : unit;
        this.current = current - 90;
        this.animate = animate;
        this.limitBreak = value >= 100;
        mode = Mode.GRADATION;

        initialized = true;

        if (animate) {
            startAnimation(500);
        } else {
            phase = value;
            invalidate();
        }
    }

    public void setData(String text, String unit, @ColorInt int[] colors, float[] positions) {
        this.text = TextUtils.isEmpty(text) ? "" : text;
        textSize = 0;
        unitSize = 0;
        this.unit = TextUtils.isEmpty(unit) ? "" : unit;
        this.current = current - 90;
        this.colors = colors;
        this.positions = positions;
        animate = false;
        mode = Mode.RAINBOW;

        initialized = true;
        invalidate();
    }

    private DisplayMetrics displayMetrics(Context context) {
        DisplayMetrics displayMetrics = new DisplayMetrics();

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        wm.getDefaultDisplay().getMetrics(displayMetrics);

        return displayMetrics;
    }

}
