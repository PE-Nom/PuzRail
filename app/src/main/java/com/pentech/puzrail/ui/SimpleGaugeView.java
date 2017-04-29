package com.pentech.puzrail.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.graphics.Typeface;
import android.support.annotation.ColorInt;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;

/**
 * Created by c0932 on 2017/03/17.
 */

public class SimpleGaugeView extends View {

    private float MARGIN_UNIT = 5f;
    private float MARGIN_INNER_RING = 2f;

    private static final float RING_STROKE_PERCENTAGE = 0.15f;

    private static final int COLOR_RAINBOW_TEXT = Color.rgb(158, 158, 158);
    private static final int COLOR_BASE_RING = Color.rgb(230, 230, 230);
//    private static final int COLOR_SECOND_LOOP_BASE_RING = Color.rgb(242, 89, 12);
//    private static final int COLOR_SECOND_LOOP = Color.rgb(245, 139, 85);
    private static final int COLOR_SECOND_LOOP_BASE_RING = Color.rgb(0, 85, 255);
    private static final int COLOR_SECOND_LOOP = Color.rgb(0, 85, 255);

    private float density;

    // data
    private boolean initialized;
    private int value;
    private String text;
    private float textSize;
    private float unitSize;
    private String unit;
    private int color;
    private boolean limitBreak;

    // rainbow mode
    private boolean rainbow;
    @ColorInt
    private int[] colors;
    private float[] positions;
    private Shader arcShader;

    // for draw
    private Paint paint;
    private Rect labelRect;
    private RectF rectF;

    public SimpleGaugeView(Context context) {
        this(context, null);
    }

    public SimpleGaugeView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SimpleGaugeView(Context context, AttributeSet attrs, int defStyleAttr) {
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

        initialized = false;
        rainbow = false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (initialized) {
            // center
            float centerX = canvas.getWidth() / 2;
            float centerY = canvas.getHeight() / 2;

            boolean isLandscape = canvas.getWidth() > canvas.getHeight();

            float ringStrokeWidth;
            float ringRadius;
            if (isLandscape) {
                ringStrokeWidth = canvas.getHeight() * RING_STROKE_PERCENTAGE;
                ringRadius = (canvas.getHeight() / 2) - ringStrokeWidth;
                centerX = canvas.getWidth()/20+ringRadius+ringStrokeWidth;
            } else {
                ringStrokeWidth = canvas.getWidth() * RING_STROKE_PERCENTAGE;
                ringRadius = (canvas.getWidth() / 2) - ringStrokeWidth;
            }

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

            if (rainbow) {
                if (arcShader == null) {
                    arcShader = new SweepGradient(centerX, centerY, colors, positions);
                }
                // ring
                paint.setShader(arcShader);
                paint.setStrokeWidth(ringStrokeWidth);
                paint.setStyle(Paint.Style.STROKE);
                canvas.drawCircle(centerX, centerY, ringRadius + ringStrokeWidth / 2, paint);

                // text
                paint.setShader(null);
                paint.setColor(COLOR_RAINBOW_TEXT);
            } else {
                paint.setShader(null);

                if (limitBreak) {
                    // base ring
                    paint.setColor(COLOR_SECOND_LOOP_BASE_RING);
                    paint.setStrokeWidth(ringStrokeWidth);
                    paint.setStyle(Paint.Style.STROKE);
                    canvas.drawCircle(centerX, centerY, ringRadius + ringStrokeWidth / 2, paint);

                    // inner fill
                    paint.setColor(COLOR_SECOND_LOOP_BASE_RING);
                    paint.setStyle(Paint.Style.FILL);
                    canvas.drawCircle(centerX, centerY, ringRadius - MARGIN_INNER_RING, paint);

                    // ring
                    paint.setColor(COLOR_SECOND_LOOP);
                    paint.setStrokeWidth(ringStrokeWidth);
                    paint.setStyle(Paint.Style.STROKE);
                    canvas.drawArc(rectF, -90, 360 * ((value - 100) / 100f), false, paint);

                    // text color
                    paint.setColor(Color.WHITE);
                } else {
                    // base ring
                    paint.setColor(COLOR_BASE_RING);
                    paint.setStrokeWidth(ringStrokeWidth);
                    paint.setStyle(Paint.Style.STROKE);
                    canvas.drawCircle(centerX, centerY, ringRadius + ringStrokeWidth / 2, paint);

                    // ring
                    paint.setColor(color);
                    paint.setStrokeWidth(ringStrokeWidth);
                    paint.setStyle(Paint.Style.STROKE);
                    canvas.drawArc(rectF, -90, 360 * (value / 100f), false, paint);
                }
            }

            // value
            if (!TextUtils.isEmpty(text)) {
                paint.setTypeface(Typeface.DEFAULT_BOLD);
                paint.setStyle(Paint.Style.FILL);
                paint.setTextSize(textSize);
                paint.getTextBounds(text, 0, text.length(), labelRect);
                canvas.drawText(text, centerX, centerY + labelRect.height() / 4, paint);
            }

            // unit
            if (!TextUtils.isEmpty(unit)) {
                paint.setTypeface(Typeface.DEFAULT);
                paint.setStyle(Paint.Style.FILL);
                paint.setTextSize(unitSize);
                paint.getTextBounds(unit, 0, unit.length(), labelRect);
                canvas.drawText(unit, centerX, centerY + ringRadius - MARGIN_UNIT, paint);
            }
        }

        super.onDraw(canvas);
    }

    public void setData(int value, String unit, int color) {
        this.value = value;
        text = String.valueOf(value);
        textSize = 0;
        unitSize = 0;
        this.unit = TextUtils.isEmpty(unit) ? "" : unit;
        this.color = color;
        this.limitBreak = value >= 100;
        rainbow = false;

        initialized = true;
        invalidate();
    }

    public void setData(String text, String unit, @ColorInt int[] colors, float[] positions) {
        this.text = TextUtils.isEmpty(text) ? "" : text;
        textSize = 0;
        unitSize = 0;
        this.unit = TextUtils.isEmpty(unit) ? "" : unit;
        this.colors = colors;
        this.positions = positions;
        rainbow = true;

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
