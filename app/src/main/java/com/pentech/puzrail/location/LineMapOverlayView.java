package com.pentech.puzrail.location;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.TextView;

import com.pentech.puzrail.R;
import com.pentech.puzrail.database.Line;
import com.pentech.puzrail.piecegarally.PieceGarallyActivity;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.Context.VIBRATOR_SERVICE;

/**
 * Created by takashi on 2016/11/12.
 */
public class LineMapOverlayView extends android.support.v7.widget.AppCompatImageView {

    private static final boolean DEBUG = true;	// TODO for debugging
    private static String TAG = "RailwayLineImageView";
    private static final float EPS = 0.1f;
    private static final int MOVE_LIMIT = 100; //limit value to prevent the image disappearing from the view when moving
    private static final float DEFAULT_MAX_SCALE = 8.f;
    private static final float DEFAULT_MIN_SCALE = 0.1f;

    protected Matrix mImageMatrix = new Matrix();
    private final RectF mImageRect = new RectF();   // 路線図の矩形領域
    private final RectF mLimitRect = new RectF();
    private final LineSegment[] mLimitSegments = new LineSegment[4];

    private float mMaxScale = DEFAULT_MAX_SCALE;
    private float mMinScale = DEFAULT_MIN_SCALE;

    private ScaleGestureDetector mScaleDetector;
    private GestureDetector mGestureDetector;

    private boolean mScalingMode = false;
    private boolean mScrolling = false;
    private boolean mDragging = false;

    private OnLineScrollEndListener listener;
    private Context context;
    private float density;
    private boolean initState = false;

    public LineMapOverlayView(Context context) {
        super(context);
        this.context = context;
        createListener();
        this.density = context.getResources().getDisplayMetrics().density;
        if(DEBUG) Log.d(TAG,"constructor1");
    }

    public LineMapOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        createListener();
        this.density = context.getResources().getDisplayMetrics().density;
        if(DEBUG) Log.d(TAG,"constructor2");
    }

    public LineMapOverlayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        createListener();
        this.density = context.getResources().getDisplayMetrics().density;
        if(DEBUG) Log.d(TAG,"constructor3");
    }

    public void setOnScrollEndListener(OnLineScrollEndListener listener){
        this.listener = listener;
    }

    public RectF getCurrentImageRect(){

        float values[] = new float[8];
        // calculate the corner coordinates of image applied matrix
        // [(left,top),(right,top),(right,bottom),(left.bottom)]
        values[0] = values[6] = mImageRect.left;
        values[1] = values[3] = mImageRect.top;
        values[5] = values[7] = mImageRect.bottom;
        values[2] = values[4] = mImageRect.right;
        mImageMatrix.mapPoints(values);

        float left = values[0];
        float top = values[1];
        float right = values[2];
        float bottom = values[5];

        return new RectF(left,top,right,bottom);
    }

    public void setImageDrawable(){
        super.setImageDrawable(ResourcesCompat.getDrawable(getResources(), this.line.getDrawableResourceId(), null));
    }
    public void resetImageDrawable(){
        this.initState = false;
        super.setImageDrawable(null);
    }
    // pinch in/out操作のイベントハンドラ
    private class RailwayLineViewScaleGestureDetector
            implements ScaleGestureDetector.OnScaleGestureListener{
        public RailwayLineViewScaleGestureDetector(){

        }
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            // ピンチイン・アウト中に継続して呼び出される
            // getScaleFactor()は
            // 『今回の2点タッチの距離/前回の2点タッチの距離』を返す
            if(DEBUG) Log.d(TAG, String.format("onScale factor:%f",detector.getScaleFactor()));
            // 表示倍率の計算
            if(mScalingMode){
                if(processZoom(detector.getScaleFactor())){
                    invalidate();
                }
            }
            return true;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            if(DEBUG) Log.d(TAG, "onScaleBegin");
            mScalingMode = true;
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            if(DEBUG) Log.d(TAG, "onScaleEnd");
            mScalingMode = false;
        }
    }

    // scroll1操作、single tap、double tap、long tap操作のイベントハンドラ
    private class RailwayLineViewGestureListener
            implements GestureDetector.OnGestureListener,GestureDetector.OnDoubleTapListener{

        public RailwayLineViewGestureListener(){

        }
        @Override
        public boolean onDown(MotionEvent e) {
//            if(DEBUG) Log.d(TAG, "onDown");
            return true;
        }

        @Override
        public void onShowPress(MotionEvent e) {
//            if(DEBUG) Log.d(TAG, "onShowPress");
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if(DEBUG) Log.d(TAG, "onSingleTapUp");
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
//            if(DEBUG) Log.d(TAG, "onScroll");
            if(!mScalingMode){
                mScrolling = true;

//                Log.d(TAG,String.format("positionLock = %b, positionLocked = %b",
//                        LineMapOverlayView.this.positionLock,LineMapOverlayView.this.positionLocked));
                float dy,dx;
                if(LineMapOverlayView.this.positionLock){
                    if(!LineMapOverlayView.this.positionLocked ) {
                        processZoom(1.0f/computeScaleError());
                        LatLng point1, point2;
                        Point screenPoint1, screenPoint2;
                        point1 = new LatLng(LineMapOverlayView.this.line.getCorrectTopLat(), LineMapOverlayView.this.line.getCorrectLeftLng());
                        point2 = new LatLng(LineMapOverlayView.this.line.getCorrectBottomLat(), LineMapOverlayView.this.line.getCorrectRightLng());
                        screenPoint1 = map.getProjection().toScreenLocation(point1);
                        screenPoint2 = map.getProjection().toScreenLocation(point2);
                        RectF railwayImageRect = getCurrentImageRect();
                        dx = Math.min((screenPoint1.x - railwayImageRect.left), (screenPoint2.x - railwayImageRect.right));
                        dy = Math.min((screenPoint1.y - railwayImageRect.top), (screenPoint2.y - railwayImageRect.bottom));
                        if (processDrag(dx, dy)) {
                            invalidate();
                        }
                        LineMapOverlayView.this.positionLocked = true;
                        // ロックバイブレーション
                        if(LineMapOverlayView.this.vibration_mode){
                            Vibrator vib =  (Vibrator)LineMapOverlayView.this.context.getSystemService(VIBRATOR_SERVICE);
                            vib.vibrate(50); // 10msec 1shotでバイブレーション
                        }
                    }
                }
                else{
                    dx = -1.0f*distanceX;
                    dy = -1.0f*distanceY;
                    if(processDrag(dx,dy)){
                        invalidate();
                    }
                }

            }
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
//            if(DEBUG) Log.d(TAG, "onLongPress");
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if(DEBUG) Log.d(TAG,"onFling");
            return false;
        }

        @Override
        public boolean onDoubleTap(MotionEvent motionEvent) {
            if(DEBUG) Log.d(TAG,"onDoubleTap");
            return false;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent motionEvent) {
            if(DEBUG) Log.d(TAG,"onDoubleTapEvent");
            return false;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
            if(DEBUG) Log.d(TAG,"onSingleTapConfirmed");
            return false;
        }
    }

    private void createListener(){
        setLongClickable(true);
        mScaleDetector = new ScaleGestureDetector(this.context,new RailwayLineViewScaleGestureDetector());
        mGestureDetector = new GestureDetector(this.context,new RailwayLineViewGestureListener() );
    }

    protected void onLayout(final boolean changed, final int left, final int top, final int right, final int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        // if view size(width|height) is zero(the view size not decided yet)
        // or no image assigned, skip initialization
        if (getWidth() == 0 || getHeight() == 0 || getDrawable() == null) return;
        if(DEBUG) Log.d(TAG,String.format("onLayout : changed = %b,left %d, top=%d, right=%d, bottom=%d",changed,left,top,right,bottom));
        if(!this.initState){
            init();
        }
    }

    private void init(){

        super.setScaleType(ScaleType.CENTER_INSIDE);
        setFrame(getLeft(), getTop(), getRight(), getBottom());

        // 路線図の描画領域の境界を示す矩形データを取得する
        final Drawable dr = getDrawable();
        if (dr != null) {
            mImageRect.set(dr.getBounds());
        } else {
            mImageRect.setEmpty();
        }

        // スケーリングの最大、最小を求める。
        mMaxScale = Math.min(
                Math.abs((getRight()-getLeft()))/Math.abs((mImageRect.right-mImageRect.left)),
                Math.abs((getBottom()-getTop()))/Math.abs((mImageRect.bottom-mImageRect.top)));
        mMinScale = 1.0f;

        // set limit rectangle that the image can move
        final Rect tmp = new Rect();
        getDrawingRect(tmp);
        mLimitRect.set(tmp);
        mLimitRect.inset(MOVE_LIMIT, MOVE_LIMIT);
        mLimitSegments[0] = null;

        // set the scale type to ScaleType.MATRIX
        mImageMatrix = getImageMatrix();
        // set the scale type to ScaleType.MATRIX
        super.setScaleType(ScaleType.MATRIX);
        // apply matrix"
        super.setImageMatrix(mImageMatrix);

        // debug
        float[] values = new float[9];
        mImageMatrix.getValues(values);
        if(DEBUG) Log.v(TAG,String.format("mImageMatrix %f,%f,%f,%f,%f,%f,%f,%f,%f",
                values[0],values[1],values[2],
                values[3],values[4],values[5],
                values[6],values[7],values[8]));
        this.initState=true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        float x = event.getX();
        float y = event.getY();

        RectF imageRectF = getCurrentImageRect();

//        if(DEBUG) Log.d(TAG,"onTouchEvent : "
//                + " event.x =" + x + ", event.y =" + y
//                + ", left = " + imageRectF.left + ", top  = " + imageRectF.top + ", bottom = " + imageRectF.bottom  + ", right = " + imageRectF.right);

        if( imageRectF.left < x && x < imageRectF.right && imageRectF.top < y && y < imageRectF.bottom && getDrawable()!=null){
            super.onTouchEvent(event);
            mDragging = true;
            boolean a = mGestureDetector.onTouchEvent(event);
            boolean b = mScaleDetector.onTouchEvent(event);
            if(event.getAction()==MotionEvent.ACTION_UP){
                mDragging = false;
                if(mScrolling){
                    Log.d(TAG,"ピース移動完了");
                    if(this.listener!=null) this.listener.onScrollEnd();
                    mScrolling = false;
                }
            }
            return a|b;
        }
        else{
            mScrolling = false;
            mDragging = false;
            return false;
        }
    }

    private final boolean processDrag(final float dist_x,final float dist_y) {

        float[] mTrans = new float[8];
        float dx = dist_x;
        float dy = dist_y;

        // calculate the corner coordinates of image applied matrix
        // [(left,top),(right,top),(right,bottom),(left.bottom)]
        mTrans[0] = mTrans[6] = mImageRect.left;
        mTrans[1] = mTrans[3] = mImageRect.top;
        mTrans[5] = mTrans[7] = mImageRect.bottom;
        mTrans[2] = mTrans[4] = mImageRect.right;
        mImageMatrix.mapPoints(mTrans);
        for (int i = 0; i < 8; i += 2) {
            mTrans[i] += dx;
            mTrans[i+1] += dy;
        }

        // check whether the image can move
        // if we can ignore rotating, the limit check is more easy...
        boolean canMove
                // check whether at lease one corner of image bounds is in the limitRect
                = mLimitRect.contains(mTrans[0], mTrans[1])
                || mLimitRect.contains(mTrans[2], mTrans[3])
                || mLimitRect.contains(mTrans[4], mTrans[5])
                || mLimitRect.contains(mTrans[6], mTrans[7])
                // check whether at least one corner of limitRect is in the image bounds
                || ptInPoly(mLimitRect.left, mLimitRect.top, mTrans)
                || ptInPoly(mLimitRect.right, mLimitRect.top, mTrans)
                || ptInPoly(mLimitRect.right, mLimitRect.bottom, mTrans)
                || ptInPoly(mLimitRect.left, mLimitRect.bottom, mTrans);
        if (!canMove) {
            // when no corner is in, we need additional check whether at least
            // one side of image bounds intersect with the limit rectangle
            if (mLimitSegments[0] == null) {
                mLimitSegments[0] = new LineSegment(mLimitRect.left, mLimitRect.top, mLimitRect.right, mLimitRect.top);
                mLimitSegments[1] = new LineSegment(mLimitRect.right, mLimitRect.top, mLimitRect.right, mLimitRect.bottom);
                mLimitSegments[2] = new LineSegment(mLimitRect.right, mLimitRect.bottom, mLimitRect.left, mLimitRect.bottom);
                mLimitSegments[3] = new LineSegment(mLimitRect.left, mLimitRect.bottom, mLimitRect.left, mLimitRect.top);
            }
            final LineSegment side = new LineSegment(mTrans[0], mTrans[1], mTrans[2], mTrans[3]);
            canMove = LineSegment.checkIntersect(side, mLimitSegments);
            if (!canMove) {
                side.set(mTrans[2], mTrans[3], mTrans[4], mTrans[5]);
                canMove = LineSegment.checkIntersect(side, mLimitSegments);
                if (!canMove) {
                    side.set(mTrans[4], mTrans[5], mTrans[6], mTrans[7]);
                    canMove = LineSegment.checkIntersect(side, mLimitSegments);
                    if (!canMove) {
                        side.set(mTrans[6], mTrans[7], mTrans[0], mTrans[1]);
                        canMove = LineSegment.checkIntersect(side, mLimitSegments);
                    }
                }
            }
        }
        if (canMove) {
            // TODO we need adjust dx/dy not to penetrate into the limit rectangle
            // otherwise the image can not move when one side is on the border of limit rectangle.
            // only calculate without rotation now because its calculation is to heavy when rotation applied.
//            if (!mIsRotating) {
            final float left = Math.min(Math.min(mTrans[0], mTrans[2]), Math.min(mTrans[4], mTrans[6]));
            final float right = Math.max(Math.max(mTrans[0], mTrans[2]), Math.max(mTrans[4], mTrans[6]));
            final float top = Math.min(Math.min(mTrans[1], mTrans[3]), Math.min(mTrans[5], mTrans[7]));
            final float bottom = Math.max(Math.max(mTrans[1], mTrans[3]), Math.max(mTrans[5], mTrans[7]));

            if (right < mLimitRect.left) {
                dx = mLimitRect.left - right;
            } else if (left + EPS > mLimitRect.right) {
                dx = mLimitRect.right - left - EPS;
            }
            if (bottom < mLimitRect.top) {
                dy = mLimitRect.top - bottom;
            } else if (top + EPS > mLimitRect.bottom) {
                dy = mLimitRect.bottom - top - EPS;
            }
//            }
            if ((dx != 0) || (dy != 0)) {
//                if(DEBUG) Log.v(TAG, String.format("Applied processDrag:dx=%f,dy=%f", dx, dy));
                // apply move
                if (mImageMatrix.postTranslate(dx, dy)) {
                    // when image is really moved?
//                    mImageMatrixChanged = true;
                    // apply to super class
                    super.setImageMatrix(mImageMatrix);
                }
            }
            else{
//                if(DEBUG) Log.v(TAG, String.format("Not Applied processDrag:dx=%f,dy=%f", dx, dy));
            }
        }
        return canMove;
    }

    private static final Vector sPtInPoly_v1 = new Vector();
    private static final Vector sPtInPoly_v2 = new Vector();
    /**
     * check whether the point is in the clockwise 2D polygon
     * @param x
     * @param y
     * @param poly: the array of polygon coordinates(x,y pairs)
     * @return
     */
    private static final boolean ptInPoly(final float x, final float y, final float[] poly) {

        final int n = poly.length & 0x7fffffff;
        // minimum 3 points(3 pair of x/y coordinates) need to calculate >> length >= 6
        if (n < 6) return false;
        boolean result = true;
        for (int i = 0; i < n; i += 2) {
            sPtInPoly_v1.set(x, y).dec(poly[i], poly[i + 1]);
            if (i + 2 < n) sPtInPoly_v2.set(poly[i + 2], poly[i + 3]);
            else sPtInPoly_v2.set(poly[0], poly[1]);
            sPtInPoly_v2.dec(poly[i], poly[i + 1]);
            if (Vector.crossProduct(sPtInPoly_v1, sPtInPoly_v2) > 0) {
                if(DEBUG) Log.v(TAG, "pt is outside of a polygon:");
                result = false;
                break;
            }
        }
        return result;
    }

    /**
     * zooming
     */
    private final boolean processZoom(float scale) {

        // スケーリングの中心座標を求める。
        // 最初に現在の表示領域の矩形頂点座標を求める
        float[] points = new float[9];
        // calculate the corner coordinates of image applied matrix
        // [(left,top),(right,top),(right,bottom),(left.bottom)]
        points[0] = points[6] = mImageRect.left;
        points[1] = points[3] = mImageRect.top;
        points[5] = points[7] = mImageRect.bottom;
        points[2] = points[4] = mImageRect.right;
        mImageMatrix.mapPoints(points);

        float left = points[0];
        float top = points[1];
        float right = points[2];
        float bottom = points[5];

        //次にX,Yの中心を求め、スケーリングの中心座標とする。
        float pivotX = (left+right)/2.0f;
        float pivotY = (top+bottom)/2.0f;

        // restore the Matrix
        float[] values = new float[9];
        mImageMatrix.getValues(values);
        // get current zooming scale
        final float currentScale = Math.min(values[Matrix.MSCALE_X],values[Matrix.MSCALE_Y]);
        // calculate the zooming scale from the distance between touched positions
//        final float scale = detector.getScaleFactor();
        // calculate the applied zooming scale
        final float tmpScale = scale * currentScale*1.1f;
        if (tmpScale < mMinScale) {
            // skip if the applied scale is smaller than minimum scale
            return false;
        } else if (tmpScale > mMaxScale) {
            // skip if the applied scale is bigger than maximum scale
            return false;
        }

        // change scale with scale value and pivot point
        if (mImageMatrix.postScale(scale, scale, pivotX, pivotY)){
            // when Matrix is changed
//            mImageMatrixChanged = true;
            // apply to super class
            super.setImageMatrix(mImageMatrix);
        }
        return true;
    }

    // 正解座標近辺での色変更

    /**
     * ColorMatrix data for reversing image
     */

    private static final float[] TRANSPEARENT = {
            1.0f,   0.0f,   0.0f,  0.0f,  0.0f,
            0.0f,   1.0f,   0.0f,  0.0f,  0.0f,
            0.0f,   0.0f,   1.0f,  0.0f,  0.0f,
            1.0f,   0.0f,   0.0f,  0.0f,  0.0f,
    };
    private static final float[] REVERSE = {
            1.0f,   0.0f,   0.0f,  0.0f,    0.0f,
            0.0f,   1.0f,   0.0f,  0.0f,    0.0f,
           -1.0f,   0.0f,   1.0f,  0.0f,  255.0f,
            0.0f,   0.0f,   0.0f,  1.0f,    0.0f,
    };
    private static final float[] NORMAL = {
            1.0f,   0.0f,   0.0f,  0.0f,  0.0f,
            0.0f,   1.0f,   0.0f,  0.0f,  0.0f,
            0.0f,   0.0f,   1.0f,  0.0f,  0.0f,
            0.0f,   0.0f,   0.0f,  1.0f,  0.0f,
    };

    private static final float[] TRANSPEARENT_DRAGGING = {
            0.0f,   0.0f,   0.0f,  0.0f,  0.0f,
            1.0f,   0.0f,   0.0f,  0.0f,  0.0f, //　赤を緑に変換するだけ
            0.0f,   0.0f,   0.0f,  0.0f,  0.0f,
            1.0f,   0.0f,   0.0f,  0.0f,  0.0f,
    };
    private static final float[] REVERSE_DRAGGING = {
            0.0f,   0.0f,   0.0f,  0.0f,    0.0f, // 赤を消す
            1.0f,   0.0f,   0.0f,  0.0f,    0.0f, // 赤→緑に変換
           -1.0f,   0.0f,   0.0f,  0.0f,  255.0f, // 黒→赤を消して青をセット
            0.0f,   0.0f,   0.0f,  1.0f,    0.0f,
    };
    private static final float[] NORMAL_DRAGGING = {
            0.0f,   0.0f,   0.0f,  0.0f,  0.0f,
            1.0f,   0.0f,   0.0f,  0.0f,  0.0f,
            0.0f,   0.0f,   0.0f,  0.0f,  0.0f,
            0.0f,   0.0f,   0.0f,  1.0f,  0.0f,
    };

    // 難易度設定
    public final static int DIFFICULTY_PROFESSIONAL = 0;
    public final static int DIFFICULTY_AMATEUR = 1;
    public final static int DIFFICULTY_BEGINNER = 2;
    //　位置誤差のレベル番号
    public final static int ERR_LEVEL0 = 0;
    public final static int ERR_LEVEL1 = 1;
    public final static int ERR_LEVEL2 = 2;
    public final static int ERR_LEVEL3 = 3;

    // 難易度名称
    private final static String difficulty_name[] = {
            "Professional",
            "Amateur",
            "Beginner"
    };
    // 正誤判定誤差(表示dpでの位置誤差）
    public final static int ERR_RANGE[][] = { // [難易度][誤差レベル]
            { 2, 5,10,20}, // PROFESSIONAL
            {10,15,25,40}, // AMATEUR
            {25,35,50,70}  // BEGINNER
    };
    // 正解ポジションロック時間
    private final static int PositionLockTime[] = new int[] {0,30,50,75};
    // 滅灯デューティ比
    private final int onTime[]  = new int[] {5,30,80,1000}; // [誤差レベル]
    private final int offTime[] = new int[] {5,10,20,   0}; // [誤差レベル]

    private Line line;
    private GoogleMap map;
    private boolean lightingSw = true;
    private int colorCount = 0;
    private int difficulty_mode = DIFFICULTY_AMATEUR;
    private boolean vibration_mode= true;

    public void setLine(Line line){
        this.line = line;
    }
    public void setMap(GoogleMap map){
        this.map = map;
    }
    public void setDifficulty_mode(int mode){ this.difficulty_mode = mode; }
    public void setVibrationMode(boolean mode){ this.vibration_mode = mode; }

    public void displayCorrectCoordinate(String tag){
        RectF railwayImageRect = getCurrentImageRect();
        Point scpt1 = new Point((int)railwayImageRect.left,(int)railwayImageRect.top);
        Point scpt2 = new Point((int)railwayImageRect.right,(int)railwayImageRect.bottom);
        LatLng coordinate1 = map.getProjection().fromScreenLocation(scpt1);
        LatLng coordinate2 = map.getProjection().fromScreenLocation(scpt2);
        Log.d(tag,String.format("point1 (lat,lng) = (%f,%f), point2 (lat,lng) = (%f,%f)",
                coordinate1.latitude,coordinate1.longitude,coordinate2.latitude,coordinate2.longitude));
    }

    private double[] computePositionError(){
        RectF railwayImageRect = getCurrentImageRect();
        // 路線ピースの表示中心座標
        Point imageCenter = new Point(
                (int)((railwayImageRect.left + railwayImageRect.right) / 2.0),
                (int)((railwayImageRect.top + railwayImageRect.bottom) / 2.0)
        );
        // 路線ピース表示中心の緯度・経度現在値
        LatLng pieceCenterCoordinate = map.getProjection().fromScreenLocation(imageCenter);
        // 路線の中心緯度・経度
        LatLng lineCenterCoordinate  = new LatLng(
                (this.line.getCorrectTopLat()+this.line.getCorrectBottomLat())/2.0,
                (this.line.getCorrectRightLng() + this.line.getCorrectLeftLng())/2.0
        );
        //　誤差表示
        double error[] = new double[2];
        error[0] = Math.abs(pieceCenterCoordinate.latitude-lineCenterCoordinate.latitude);
        error[1] = Math.abs(pieceCenterCoordinate.longitude-lineCenterCoordinate.longitude);
        return error;
    }

    private float computeScaleError(){
        LatLng point1,point2;
        Point screenPoint1,screenPoint2;

        // 路線の表示スケール（px)
        point1 = new LatLng(this.line.getCorrectTopLat(),this.line.getCorrectLeftLng());
        point2 = new LatLng(this.line.getCorrectBottomLat(),this.line.getCorrectRightLng());
        screenPoint1 = map.getProjection().toScreenLocation(point1);
        screenPoint2 = map.getProjection().toScreenLocation(point2);
        int lineScaleWidth = Math.abs(screenPoint1.x - screenPoint2.x);
        int lineScaleHeight = Math.abs(screenPoint1.y - screenPoint2.y);
        int lineScale = lineScaleWidth > lineScaleHeight ? lineScaleWidth : lineScaleHeight;
        // ピースの表示スケール(px)
        RectF railwayImageRect = getCurrentImageRect();
        float pieceScaleHeight = Math.abs(railwayImageRect.top - railwayImageRect.bottom);
        float pieceScaleWidth = Math.abs(railwayImageRect.right - railwayImageRect.left);
        float pieceScale = pieceScaleHeight > pieceScaleWidth ? pieceScaleHeight : pieceScaleWidth;
        // 表示スケールの比率
        float error = pieceScale/(float)lineScale;
        return(error);
    }

    public int computeLocationError(){
        LatLng point1,point2;
        Point screenPoint1,screenPoint2;

        RectF railwayImageRect = getCurrentImageRect();
        point1 = new LatLng(this.line.getCorrectTopLat(),this.line.getCorrectLeftLng());
        point2 = new LatLng(this.line.getCorrectBottomLat(),this.line.getCorrectRightLng());
        screenPoint1 = map.getProjection().toScreenLocation(point1);
        screenPoint2 = map.getProjection().toScreenLocation(point2);

//        Log.d(TAG,String.format("screenPoint1 = (%f,%f) , screenPoint2 = (%f,%f)",
//                (double)screenPoint1.x,(double)screenPoint1.y,(double)screenPoint2.x,(double)screenPoint2.y));
//        Log.d(TAG,String.format("ImageRect top = %f, left = %f, bottom = %f, right = %f",
//                railwayImageRect.left,railwayImageRect.top,railwayImageRect.right,railwayImageRect.bottom));

        double distance[]  = new double[2];
        distance[0] = Math.sqrt( Math.pow((railwayImageRect.top    - (double)screenPoint1.y), 2.0) +
                                 Math.pow((railwayImageRect.left   - (double)screenPoint1.x),2.0));
        distance[1] = Math.sqrt( Math.pow((railwayImageRect.bottom - (double)screenPoint2.y), 2.0) +
                                 Math.pow((railwayImageRect.right  - (double)screenPoint2.x),2.0));
//        Log.d(TAG,String.format("distance = %f,%f density = %f",distance[0],distance[1],this.density));

        int err[] = new int[2];
        err[0] = (int)(distance[0] / this.density);
        err[1] = (int)(distance[1] / this.density);
        int error = err[0]+err[1];
//        Log.d(TAG,String.format("err(dp) = %d, %d, error = %d",err[0],err[1],error));

        return (error);
    }

    private ColorMatrix getColorMatrix(int errLevel){
        ColorMatrix clm = new ColorMatrix(REVERSE);
        if(mDragging){
            clm = new ColorMatrix(REVERSE_DRAGGING);
        }
        int onCnt = onTime[errLevel];
        int offCnt = offTime[errLevel];
        if(lightingSw){
            colorCount++;
            if( onCnt <= colorCount ){
                lightingSw = false;
                colorCount = 0;
            }
        }
        else{
            colorCount++;
            if( offCnt <= colorCount ){
                lightingSw = true;
                colorCount = 0;
            }
            else{
                if(mDragging){
                    clm = new ColorMatrix(TRANSPEARENT_DRAGGING);
                }else{
                    clm = new ColorMatrix(TRANSPEARENT);
                }
            }
        }
        return clm;
    }

    private boolean positionLock = false;
    private boolean positionLocked = false;
    private int positionLockTimeCnt = 0;

    @Override
    protected void onDraw(Canvas canvas) {
//       final double positionError[] = computePositionError();
        float scaleError = computeScaleError();
        int err = computeLocationError();

        if( err < ERR_RANGE[this.difficulty_mode][ERR_LEVEL0] ){
            if(!this.positionLock && !this.positionLocked){ // 未固定の場合
                // 正解の位置に一定時間固定
                this.positionLock = true;
                this.positionLockTimeCnt = 0;
            }
            else{
                //  固定時間のカウントアップ
                if(this.positionLockTimeCnt < this.PositionLockTime[this.difficulty_mode]){ // 固定時間経過
                    this.positionLockTimeCnt++;
                    Log.d(TAG,String.format("positionLockTimeCnt = %d",this.positionLockTimeCnt));
                    super.setColorFilter(new ColorMatrixColorFilter(getColorMatrix(ERR_LEVEL0)));
                }
                else{
                    this.positionLock = false;
                    if(mDragging) {
                        super.setColorFilter(new ColorMatrixColorFilter(new ColorMatrix(REVERSE_DRAGGING)));
                    }
                    else{
                        super.setColorFilter(new ColorMatrixColorFilter(new ColorMatrix(REVERSE)));
                    }
                }
            }
        }
        else{
            this.positionLock = false;
            this.positionLocked = false;
            if( err < ERR_RANGE[this.difficulty_mode][ERR_LEVEL1] ){
                super.setColorFilter(new ColorMatrixColorFilter(getColorMatrix(ERR_LEVEL1)));
            }
            else if( err < ERR_RANGE[this.difficulty_mode][ERR_LEVEL2] ) {
                super.setColorFilter(new ColorMatrixColorFilter(getColorMatrix(ERR_LEVEL2)));
            }
            else if(err < ERR_RANGE[this.difficulty_mode][ERR_LEVEL3] ){
                super.setColorFilter(new ColorMatrixColorFilter(getColorMatrix(ERR_LEVEL3)));
            }
            else{
                if(mDragging) {
                    super.setColorFilter(new ColorMatrixColorFilter(new ColorMatrix(NORMAL_DRAGGING)));
                }
                else{
                    super.setColorFilter(new ColorMatrixColorFilter(new ColorMatrix(NORMAL)));
                }
                colorCount =0;
                lightingSw = true;
            }
        }

        if(!this.line.isLocationCompleted()){
            int margin = 5;
            Paint paint = new Paint();

            paint.setTextLocale(Locale.JAPANESE);
            paint.setTypeface(Typeface.DEFAULT_BOLD);
            paint.setStyle(Paint.Style.FILL);
//        paint.setColor(Color.parseColor("#142d81"));
            paint.setTextSize(12*this.density); // 12sp*density
            Paint.FontMetrics fm = new Paint.FontMetrics();
            paint.getFontMetrics(fm);

            String positionErr = String.format("【%s】（位置ズレ,縮尺ズレ）= (%d,%.2f)",difficulty_name[this.difficulty_mode],err,scaleError);
            paint.setColor(ContextCompat.getColor(this.context, R.color.color_bkGroundREct));
            Rect rect = new Rect();
            paint.getTextBounds(positionErr,0,positionErr.length(),rect);
            RectF bkGroundRect = new RectF(
                    (float)rect.left,
                    (float)rect.top   + 14*this.density,
                    (float)rect.right +  2*this.density,
                    (float)rect.bottom+ 15*this.density);
            canvas.drawRoundRect(bkGroundRect,2,2,paint);
            paint.setColor(ContextCompat.getColor(this.context, R.color.color_WHITE));
            canvas.drawText(positionErr, 1*this.density, 15*this.density, paint);
        }
        super.onDraw(canvas);
    }
}
