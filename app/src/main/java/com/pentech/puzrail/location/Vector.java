package com.pentech.puzrail.location;

/**
 * Created by takashi on 2016/11/13.
 */

public class Vector {
    public float x, y;

    public Vector() {
    }

/*		public Vector(Vector src) {
			set(src);
		} */

    public Vector(final float x, final float y) {
        set(x, y);
    }

    public Vector set(final float x, final float y) {
        this.x = x;
        this.y = y;
        return this;
    }

/*		public Vector set(final Vector other) {
			x = other.x;
			y = other.y;
			return this;
		} */

/*		public Vector add(final Vector other) {
			return new Vector(x + other.x, y + other.y);
		} */

/*		public Vector add(final float x, final float y) {
			return new Vector(this.x + x, this.y + y);
		} */

/*		public Vector inc(final Vector other) {
			x += other.x;
			y += other.y;
			return this;
		} */

/*		public Vector inc(final float x, final float y) {
			this.x += x;
			this.y += y;
			return this;
		} */

    public Vector sub(final Vector other) {
        return new Vector(x - other.x, y - other.y);
    }

/*		public Vector sub(final float x, final float y) {
			return new Vector(this.x - x, this.y - y);
		} */

/*		public Vector dec(final Vector other) {
			x -= other.x;
			y -= other.y;
			return this;
		} */

    public Vector dec(final float x, final float y) {
        this.x -= x;
        this.y -= y;
        return this;
    }

    public static final float dotProduct(final float x0, final float y0, final float x1, final float y1) {
        return x0 * x1 + y0 * y1;
    }

    public static final float crossProduct(final float x0, final float y0, final float x1, final float y1) {
        return x0 * y1 - x1 * y0;
    }

    public static final float crossProduct(final Vector v1, final Vector v2) {
        return v1.x * v2.y - v2.x * v1.y;
    }

}
