package dapm.g1.final_project.custom_classes;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mickael alos on 30/05/2018.
 */
public class DrawingView extends View {

    /**
     * INTERN VARIABLES
     */
    private static final float TOUCH_TOLERANCE = 20;
    private static final float FRAME_RATIO = 0.90f;

    private boolean eventEnabled;

    private int width, height, realWidth, realHeight;
    private int step, startcap, endcap;

    private boolean cap;

    private Bitmap mBitmap;
    private Paint mBitmapPaint;
    private Canvas mCanvas;

    private RectF mRect;
    private Path mPath, mPointsPath, mCursorPath, mCirclePath, mFramePath;
    private Paint mPaint, mPointsPaint, mCursorPaint, mCirclePaint, mFramePaint, mRectPaint;

    private List<CustomPointF> listPoints;
    private ArrayList<CustomPointF> path;
    private CustomPointF[] frame, diagBase, diagTemp;

    /**
     * CUSTOM CONSTRUCTOR
     * @param c
     * @param step
     * @param rw
     * @param rh
     */
    public DrawingView(Context c,int step,int rw,int rh) {
        super(c);
        System.out.println("Step : "+step);
        realHeight = rh;
        realWidth = rw;
        eventEnabled = false;
        listPoints = new ArrayList<>();
        mBitmapPaint = new Paint(Paint.DITHER_FLAG);
        mPaint = customPaint(Color.BLUE, 10);
        mPath = new Path();
        mPointsPaint = customPaint(Color.RED,3);
        mPointsPath = new Path();
        mCursorPaint = customPaint(Color.rgb(255,0,150),5);
        mCursorPath = new Path();
        mCirclePaint = customPaint(Color.BLACK, 5);
        mCirclePath = new Path();
        mFramePaint = customPaint(Color.rgb(46,204,113), 5);
        mFramePath = new Path();
        mRectPaint = new Paint();
        mRectPaint.setColor(Color.argb(127,255,240,0));
        this.step = step;
    }

    public static Paint customPaint(int color, int strokeWidth) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setColor(color);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(strokeWidth);
        return paint;
    }

    /**
     * Method that manages event when size of view is changed
     * @param w
     * @param h
     * @param oldw
     * @param oldh
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        System.out.println("size changed "+w+" "+h+" "+oldw+" "+oldh);
        width = w;
        height = h;
        frame = new CustomPointF[]{
                new CustomPointF(0, height * FRAME_RATIO), new CustomPointF(width, height * FRAME_RATIO), //bas
                new CustomPointF(0, height * (1 - FRAME_RATIO)), new CustomPointF(width, height * (1 - FRAME_RATIO)), //haut
                new CustomPointF(width * FRAME_RATIO, 0), new CustomPointF(width * FRAME_RATIO, height), //droite
                new CustomPointF(width * (1 - FRAME_RATIO), 0), new CustomPointF(Math.round(width * (1 - FRAME_RATIO)), height) //gauche
        };
        for (int i = 0; i < frame.length; i++) {
            mFramePath.moveTo(frame[i].x, frame[i].y);
            i++;
            mFramePath.lineTo(frame[i].x, frame[i].y);
        }
        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
    }

    private void drawEndArea() {
        switch (startcap){
            case 1:
                mRect = new RectF(frame[6].x, frame[6].y, frame[4].x, frame[3].y);
                break;
            case 2:
                mRect = new RectF(frame[7].x, frame[0].y, frame[5].x, frame[5].y);
                break;
            case 3:
                mRect = new RectF(frame[2].x, frame[2].y, frame[7].x, frame[0].y);
                break;
            case 4:
                mRect = new RectF(frame[2].x, frame[6].y, frame[6].x, frame[2].y);
                break;
            case 5:
                mRect = new RectF(frame[0].x, frame[0].y, frame[7].x, frame[7].y);
                break;
            case 6:
                mRect = new RectF(frame[4].x, frame[3].y, frame[1].x, frame[1].y);
                break;
            case 7:
                mRect = new RectF(frame[4].x, frame[4].y, frame[3].x, frame[3].y);
                break;
            case 8:
                mRect = new RectF(frame[5].x, frame[1].y, frame[1].x, frame[5].y);
                break;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
        if (mRect != null) canvas.drawRect(mRect, mRectPaint);
        canvas.drawPath(mFramePath, mFramePaint);
        canvas.drawPath(mPath, mPaint);
        canvas.drawPath(mPointsPath, mPointsPaint);
        canvas.drawPath(mCursorPath, mCursorPaint);
        canvas.drawPath(mCirclePath, mCirclePaint);
    }

    /**
     * Custom methods to get touch events
     */
    private void touch_start(CustomPointF p) {
        /*clean*/
        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        capture(true);
        addPoint(p);
    }

    private void touch_move(CustomPointF p) {
        if (p.x<0 || p.x>width || p.y<0 || p.y>height) return;
        float d = 0;
        boolean add = false;
        int lsize = listPoints.size();
        if (lsize>0) {
            CustomPointF lastP = listPoints.get(lsize-1);
            d = lastP.distanceBetween(p);
            if (d >= TOUCH_TOLERANCE) {
                System.out.println("touch tolerance "+d+" s "+lsize);
                addPoint(p);
            } else System.out.println("d : " + d);
        }
    }

    private void touch_up() {
        mCirclePath.reset();
        mRect = null;
        capture(false);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (eventEnabled) {
            float x = event.getX();
            float y = event.getY();
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    touch_start(new CustomPointF(x, y));
                    invalidate();
                    break;
                case MotionEvent.ACTION_MOVE:
                    touch_move(new CustomPointF(x, y));
                    invalidate();
                    break;
                case MotionEvent.ACTION_UP:
                    touch_up();
                    invalidate();
                    break;
            }
        }
        return true;
    }

    private void capture(boolean cap) {
        this.cap = cap;
        mCursorPath.reset();
        if (cap) {
            System.out.println("start capture");
            startcap = 0;
            endcap = 0;
            mRect = null;
            listPoints = new ArrayList<>();
            path = new ArrayList<>();
            diagBase = new CustomPointF[]{new CustomPointF(0f, 0f), new CustomPointF(0f, 0f)};
            diagTemp = new CustomPointF[]{new CustomPointF(0f, 0f), new CustomPointF(0f, 0f)};
        } else if (listPoints.size() >= 2) {
            System.out.println("end capture");
            CustomPointF lastP = listPoints.get(listPoints.size()-1);
            int[] capIds = getCapIds(lastP);
            System.out.println("last point : startcap "+capIds[1]+" endcap "+capIds[0]);
            if (startcap == capIds[1] && endcap == capIds[0]) {
                System.out.println("last point ok");
                switch (startcap) {
                    case 1:
                        addExtremum(1, new CustomPointF(listPoints.get(0).x, height), new CustomPointF(lastP.x, 0));
                        break;
                    case 2:
                        addExtremum(1, new CustomPointF(listPoints.get(0).x, 0), new CustomPointF(lastP.x, height));
                        break;
                    case 3:
                        addExtremum(0, new CustomPointF(width, listPoints.get(0).y), new CustomPointF(0, lastP.y));
                        break;
                    case 4:
                        addExtremum(new CustomPointF(width, height), new CustomPointF(0, 0));
                        break;
                    case 5:
                        addExtremum(new CustomPointF(width, 0), new CustomPointF(0, height));
                        break;
                    case 6:
                        addExtremum(0, new CustomPointF(0, listPoints.get(0).y), new CustomPointF(width, lastP.y));
                        break;
                    case 7:
                        addExtremum(new CustomPointF(0, height), new CustomPointF(width, 0));
                        break;
                    case 8:
                        addExtremum(new CustomPointF(0, 0), new CustomPointF(width, height));
                        break;
                }
                System.out.println("extremum added");
                path = initBezier();
                if (path==null) {
                    System.out.println("error");
                    mPointsPath.reset();
                }
                System.out.println("trajectoire terminée");
            }
            else {
                System.out.println("wrong endcap");
                mPointsPath.reset();
            }
        }
    }

    private int[] getCapIds(PointF p) {
        int[] capIds = new int[]{0, 0};
        if (p.y > frame[0].y && p.y < height) {
            capIds[0] = 1;
            capIds[1] = 2;
        } else if (p.y < frame[2].y && p.y > 0) {
            capIds[0] = 2;
            capIds[1] = 1;
        }

        if (p.x > frame[4].x && p.x < width) {
            capIds[0] += 3;
            capIds[1] += 6;
        } else if (p.x < frame[6].x && p.x > 0) {
            capIds[0] += 6;
            capIds[1] += 3;
        }
        return capIds;
    }

    private void addExtremum(int direction, CustomPointF p1, CustomPointF p2) {
        if (listPoints.get(0).get(direction) != p1.get(direction)) {
            System.out.println("addExtremum norm start");
            Path tempPath = new Path();
            tempPath.moveTo(p1.x,p1.y);
            tempPath.lineTo(listPoints.get(0).x,listPoints.get(0).y);
            tempPath.addPath(mPointsPath);
            mPointsPath.set(tempPath);
            listPoints.add(0, p1);
        }
        CustomPointF lastP = listPoints.get(listPoints.size()-1);
        if (lastP.get(direction) != p2.get(direction)){
            System.out.println("addExtremum norm end");
            mPointsPath.lineTo(p2.x,p2.y);
            listPoints.add(p2);
        }
    }

    private void addExtremum(CustomPointF p1, CustomPointF p2) {
        if (listPoints.get(0).x != p1.x || listPoints.get(0).y != p1.y) {
            System.out.println("addExtremum diag start");
            Path tempPath = new Path();
            tempPath.moveTo(p1.x,p1.y);
            tempPath.lineTo(listPoints.get(0).x,listPoints.get(0).y);
            tempPath.addPath(mPointsPath);
            mPointsPath.set(tempPath);
            listPoints.add(0, p1);
        }
        CustomPointF lastP = listPoints.get(listPoints.size()-1);
        if (lastP.x != p2.x || lastP.y != p2.y) {
            System.out.println("addExtremum diag end");
            mPointsPath.lineTo(p2.x,p2.y);
            listPoints.add(p2);
        }
    }

    private List<CustomPointF> extractInterestingPoints(){
        System.out.println("extraction des points d'interets");
        List<CustomPointF> interestingPoints = new ArrayList<>();
        int lsize = listPoints.size();
        System.out.println("listPoints size : "+lsize);
        if (lsize>=3) {
            float cnt = 0;
            interestingPoints.add(listPoints.get(0));
            for(int i = 2; i<lsize-1;i++){
                CustomPointF p = listPoints.get(i);
                float a = CustomPointF.angle(listPoints.get(i-2),listPoints.get(i-1),p);
                System.out.println(a);
                if (a>4)
                    interestingPoints.add(p);
                else {
                    cnt+=a;
                    if (cnt>=30){
                        cnt = 0;
                        interestingPoints.add(p);
                    }
                }
            }
            interestingPoints.add(listPoints.get(lsize-1));
        }
        System.out.println(interestingPoints.size()+" points d'interet extrait");
        return interestingPoints;
    }

    private ArrayList<CustomPointF> initBezier(){
        if (listPoints.size()>0) {
            List<CustomPointF> interestingPoints = extractInterestingPoints();
            if (interestingPoints.size()>=2) {
                System.out.println("init bezier");
                mPath.reset();
                float ratioWidthHomothetie = (float) realWidth / width;
                float ratioHeightHomothetie = (float) realHeight / height;
                ArrayList<CustomPointF> B = new ArrayList<>();
                int n = interestingPoints.size() - 1;
                if (n > 0) {
                    float u;
                    for (int cnt = 0; cnt < step; cnt++) {
                        float x = 0.0f;
                        float y = 0.0f;
                        u = (float) cnt / step;
                        for (int i = 0; i < n + 1; i++) {
                            double b = (((factorialOf(n)) / ((factorialOf(i)) * (factorialOf(n - i)))) * (Math.pow(u, i)) * (Math.pow((1 - u), (n - i))));
                            CustomPointF p = interestingPoints.get(i);
                            x = (float) (x + b * p.x);
                            y = (float) (y + b * p.y);
                        }
                        if (B.size() == 0) mPath.moveTo(x, y);
                        else mPath.lineTo(x, y);
                        B.add(new CustomPointF(x * ratioWidthHomothetie, y * ratioHeightHomothetie));
                    }
                }
                System.out.println("result besier : " + B.size() + " points");
                return B;
            }
            return null;
        }
        return null;
    }

    private double factorialOf(int n) {
        double factorial = 1f;
        for (int i = 1; i <= n; i++) {
            factorial *= i;
        }
        return factorial;
    }

    private void addPoint(CustomPointF p) {
        if (cap) {
            int lentp = listPoints.size();
            if (lentp == 0) {
                System.out.println("first points capture");
                mPath.reset();
                mPointsPath.reset();
                mPointsPath.moveTo(p.x, p.y);
                int[] capIds = getCapIds(p);
                startcap = capIds[0];
                endcap = capIds[1];
                System.out.println("first points : startcap "+startcap+" endcap "+endcap);
                if (startcap == 4 || startcap == 8) {
                    diagBase[0].set(0, height);
                    diagBase[1].set(width, 0);
                    System.out.println(diagBase[0]+" "+ diagBase[1]);
                    refreshDiag(p, true);
                }
                else if (startcap == 5 || startcap == 7) {
                    diagBase[0].set(0, 0);
                    diagBase[1].set(width, height);
                    System.out.println(diagBase[0]+" "+ diagBase[1]);
                    refreshDiag(p, false);
                }
                drawEndArea();
            }
            if (startcap != 0 && acceptPoint (p)) {
                refreshCursorCap(p);
                if (lentp > 0) {
                    CustomPointF lastP = listPoints.get(listPoints.size()-1);
                    mPointsPath.quadTo(lastP.x, lastP.y, (p.x + lastP.x) / 2, (p.y + lastP.y) / 2);
                }
                mCirclePath.reset();
                mCirclePath.addCircle(p.x, p.y, 30, Path.Direction.CW);
                listPoints.add(p);
            }
        }
    }

    private boolean acceptPoint(CustomPointF p) {
        if (listPoints.size() == 0)
            return true;
        CustomPointF lastP = listPoints.get(listPoints.size()-1);
        if (startcap == 1 && p.y<lastP.y)
            return true;
        else if (startcap ==2 && p.y>lastP.y)
            return true;
        else if (startcap ==3 && p.x<lastP.x)
            return true;
        else if (startcap ==4 && Line.position(diagTemp[0], diagTemp[1], p)<0) {
            refreshDiag(p, true);
            return true;
        }
        else if (startcap ==5 && Line.position(diagTemp[0], diagTemp[1], p)>0) {
            refreshDiag(p, false);
            return true;
        }
        else if (startcap ==6 && p.x>lastP.x)
            return true;
        else if (startcap ==7 && Line.position(diagTemp[0], diagTemp[1], p)<0) {
            refreshDiag(p, false);
            return true;
        }
        else if (startcap ==8 && Line.position(diagTemp[0], diagTemp[1], p)>0) {
            refreshDiag(p, true);
            return true;
        }
        return false;
    }

    private void refreshDiag(CustomPointF p, boolean invert){
        System.out.println("refresh Diag :"+p+" "+invert);
        float at = (float)height/width;
        System.out.println(at);
        if (invert)
            at*=-1;
        float xdiff = p.x-Line.calcX(p.y,0, diagBase[0].y,at);
        float ydiff = p.y-Line.calcY(p.x,0, diagBase[0].y,at);
        if (xdiff<0) {
            diagTemp[0].x = diagBase[0].x;
            diagTemp[0].y = diagBase[0].y + ydiff;
            diagTemp[1].x = diagBase[1].x + xdiff;
            diagTemp[1].y = diagBase[1].y;
        }
        else if (xdiff>0) {
            diagTemp[0].x = diagBase[0].x + xdiff;
            diagTemp[0].y = diagBase[0].y;
            diagTemp[1].x = diagBase[1].x;
            diagTemp[1].y = diagBase[1].y + ydiff;
        }
        System.out.println(diagTemp[0]+" "+ diagTemp[1]);
    }

    private void refreshCursorCap(CustomPointF p) {
        mCursorPath.reset();
        if (startcap == 1 || startcap == 2) {
            mCursorPath.moveTo(0, p.y);
            mCursorPath.lineTo(width,p.y);
        }
        else if (startcap == 3 || startcap == 6) {
            mCursorPath.moveTo(p.x, 0);
            mCursorPath.lineTo(p.x,height);
        }
        else if (startcap==5 || startcap==7 || startcap==4 || startcap==8){
            mCursorPath.moveTo(diagTemp[0].x, diagTemp[0].y);
            mCursorPath.lineTo(diagTemp[1].x, diagTemp[1].y);
        }
    }

    public ArrayList<CustomPointF> getPath() {
        if (path==null) return null;
        System.out.println(path.size());
        if (path.size()==0) return null;
        return path;
    }

    public int getStartcap() {
        return startcap;
    }

    public int getEndcap() {
        return endcap;
    }

    public boolean isEventEnabled() {
        return eventEnabled;
    }

    public void setEventEnabled(boolean state) {
        eventEnabled = state;
    }
}