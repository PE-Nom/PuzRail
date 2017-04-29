package com.pentech.puzrail.location;

/**
 * Created by takashi on 2016/11/13.
 */

public class LineSegment {

    private static final float EPS = 0.1f;

    public final Vector p1;
    public final Vector p2;

    public LineSegment(final float x0, final float y0, final float x1, final float y1) {
        p1 = new Vector(x0, y0);
        p2 = new Vector(x1, y1);
    }

    public LineSegment set(final float x0, final float y0, final float x1, final float y1) {
        p1.set(x0, y0);
        p2.set(x1, y1);
        return this;
    }

    /**
     * check whether Line segment(seg) intersects with at least one of Line segments in the array
     *
     * @param seg
     * @param segs array of segment
     * @return true if Line segment intersects with at least one of other Line segment.
     */
    public static final boolean checkIntersect(final LineSegment seg, final LineSegment[] segs) {

        boolean result = false;
        final int n = segs != null ? segs.length : 0;

        final Vector a = seg.p2.sub(seg.p1);
        Vector b, c, d;
        for (int i = 0; i < n; i++) {
            c = segs[i].p1.sub(seg.p1);
            d = segs[i].p2.sub(seg.p1);
            result = Vector.crossProduct(a, c) * Vector.crossProduct(a, d) < EPS;
            if (result) {
                b = segs[i].p2.sub(segs[i].p1);
                c = seg.p1.sub(segs[i].p1);
                d = seg.p2.sub(segs[i].p1);
                result = Vector.crossProduct(b, c) * Vector.crossProduct(b, d) < EPS;
                if (result) {
                    break;
                }
            }
        }
        return result;
    }
}