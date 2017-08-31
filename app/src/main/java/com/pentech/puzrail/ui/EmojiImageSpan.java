package com.pentech.puzrail.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.style.ImageSpan;

import java.lang.ref.WeakReference;

/**
 * Created by c0932 on 2017/08/22.
 */

public class EmojiImageSpan extends ImageSpan {

    public EmojiImageSpan(Context c, int id) {
        super(c, id);
    }

    @Override
    public void draw(Canvas canvas, CharSequence text,
                     int start, int end, float x,
                     int top, int y, int bottom, Paint paint) {
        Drawable b = getCachedDrawable();
        canvas.save();

//        int transY = bottom - b.getBounds().bottom;
//        transY -= paint.getFontMetricsInt().descent;
        int transY = y - b.getBounds().bottom;
        transY += paint.getFontMetricsInt().descent;

        canvas.translate(x, transY);
        b.draw(canvas);
        canvas.restore();
    }

    private Drawable getCachedDrawable() {
        WeakReference<Drawable> wr = mDrawableRef;
        Drawable d = null;

        if (wr != null)
            d = wr.get();

        if (d == null) {
            d = getDrawable();
            mDrawableRef = new WeakReference<Drawable>(d);
        }

        return d;
    }

    private WeakReference<Drawable> mDrawableRef;
}
