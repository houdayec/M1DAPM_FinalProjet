package dapm.g1.final_project.CustomView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.view.MotionEvent;
import android.view.View;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import dapm.g1.final_project.Line;
import dapm.g1.final_project.PPointF;

/**
 * Created by mickael alos on 30/05/2018.
 */
public class DrawingView extends View {
    private boolean eventEnabled;

    private int width;
    private int height;
    private int realWidth;
    private int realHeight;
    private Bitmap mBitmap;
    private Paint mBitmapPaint;
    private Canvas mCanvas;

    private Path mPath;
    private Paint mPaint;

    private Path mPointsPath;
    private Paint mPointsPaint;

    private Path mCursorPath;
    private Paint mCursorPaint;

    private Path mCirclePath;
    private Paint mCirclePaint;

    private static final float FRAME_RATIO = 0.90f;
    private PPointF[] frame;
    private Path mFramePath;
    private Paint mFramePaint;

    private List<PPointF> listPoints;
    private ArrayList<PPointF> path;
    private PPointF[] diagbase;
    private PPointF[] diagtemp;
    private boolean cap;
    private int startcap;
    private int endcap;
    private static final float TOUCH_TOLERANCE = 50;

    private int step;

    public DrawingView(Context c,int step,int rw,int rh) {
        super(c);
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

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        System.out.println("size changed "+w+" "+h+" "+oldw+" "+oldh);
        width = w;
        height = h;
        frame = new PPointF[]{
                new PPointF(0, height * FRAME_RATIO), new PPointF(width, height * FRAME_RATIO),
                new PPointF(0, height * (1 - FRAME_RATIO)), new PPointF(width, height * (1 - FRAME_RATIO)),
                new PPointF(width * FRAME_RATIO, 0), new PPointF(width * FRAME_RATIO, height),
                new PPointF(width * (1 - FRAME_RATIO), 0), new PPointF(Math.round(width * (1 - FRAME_RATIO)), height)
        };
        for (int i = 0; i < frame.length; i++) {
            mFramePath.moveTo(frame[i].x, frame[i].y);
            i++;
            mFramePath.lineTo(frame[i].x, frame[i].y);
        }
        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
        canvas.drawPath(mFramePath, mFramePaint);
        canvas.drawPath(mPath, mPaint);
        canvas.drawPath(mPointsPath, mPointsPaint);
        canvas.drawPath(mCursorPath, mCursorPaint);
        canvas.drawPath(mCirclePath, mCirclePaint);
    }

    private void touch_start(float x, float y) {
        /*clean*/
        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        capture(true);
        addPoint(new PPointF(x,y));
    }

    private void touch_move(float x, float y) {
        if (x<0 || x>width || y<0 || y>height) return;
        float dx = x;
        float dy = y;
        if (listPoints.size()>0) {
            PPointF lastP = listPoints.get(listPoints.size()-1);
            dx = Math.abs(x - lastP.x);
            dy = Math.abs(y - lastP.y);
        }
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            addPoint(new PPointF(x,y));
        }
    }

    private void touch_up() {
        mCirclePath.reset();
        capture(false);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (eventEnabled) {
            float x = event.getX();
            float y = event.getY();
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    touch_start(x, y);
                    invalidate();
                    break;
                case MotionEvent.ACTION_MOVE:
                    touch_move(x, y);
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
        //mFramePath.reset();
        mCursorPath.reset();
        if (cap) {
            System.out.println("start capture");
            startcap = 0;
            endcap = 0;
            listPoints = new ArrayList<>();
            path = new ArrayList<>();
            diagbase = new PPointF[]{new PPointF(0f, 0f), new PPointF(0f, 0f)};
            diagtemp = new PPointF[]{new PPointF(0f, 0f), new PPointF(0f, 0f)};
            /*for (int i = 0; i < frame.length; i++) {
                mFramePath.moveTo(frame[i].x, frame[i].y);
                i++;
                mFramePath.lineTo(frame[i].x, frame[i].y);
            }*/
        } else if (listPoints.size() >= 2) {
            System.out.println("end capture");
            PPointF lastP = listPoints.get(listPoints.size()-1);
            int[] capIds = getCapIds(lastP);
            System.out.println("last point : startcap "+capIds[1]+" endcap "+capIds[0]);
            if (startcap == capIds[1] && endcap == capIds[0]) {
                System.out.println("last point ok");
                switch (startcap) {
                    case 1:
                        addExtremum(1, new PPointF(listPoints.get(0).x, height), new PPointF(lastP.x, 0));
                        break;
                    case 2:
                        addExtremum(1, new PPointF(listPoints.get(0).x, 0), new PPointF(lastP.x, height));
                        break;
                    case 3:
                        addExtremum(0, new PPointF(width, listPoints.get(0).y), new PPointF(0, lastP.y));
                        break;
                    case 4:
                        addExtremum(new PPointF(width, height), new PPointF(0, 0));
                        break;
                    case 5:
                        addExtremum(new PPointF(width, 0), new PPointF(0, height));
                        break;
                    case 6:
                        addExtremum(0, new PPointF(0, listPoints.get(0).y), new PPointF(width, lastP.y));
                        break;
                    case 7:
                        addExtremum(new PPointF(0, height), new PPointF(width, 0));
                        break;
                    case 8:
                        addExtremum(new PPointF(0, 0), new PPointF(width, height));
                        break;
                }
                System.out.println("extremum added");
                path = initBezier();
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

    private void addExtremum(int direction, PPointF p1, PPointF p2) {
        if (listPoints.get(0).get(direction) != p1.get(direction)) {
            System.out.println("addExtremum norm start");
            Path tempPath = new Path();
            tempPath.moveTo(p1.x,p1.y);
            tempPath.lineTo(listPoints.get(0).x,listPoints.get(0).y);
            tempPath.addPath(mPointsPath);
            mPointsPath.set(tempPath);
            listPoints.add(0, p1);
        }
        PPointF lastP = listPoints.get(listPoints.size()-1);
        if (lastP.get(direction) != p2.get(direction)){
            System.out.println("addExtremum norm end");
            mPointsPath.lineTo(p2.x,p2.y);
            listPoints.add(p2);
        }
    }

    private void addExtremum(PPointF p1, PPointF p2) {
        if (listPoints.get(0).x != p1.x || listPoints.get(0).y != p1.y) {
            System.out.println("addExtremum diag start");
            Path tempPath = new Path();
            tempPath.moveTo(p1.x,p1.y);
            tempPath.lineTo(listPoints.get(0).x,listPoints.get(0).y);
            tempPath.addPath(mPointsPath);
            mPointsPath.set(tempPath);
            listPoints.add(0, p1);
        }
        PPointF lastP = listPoints.get(listPoints.size()-1);
        if (lastP.x != p2.x || lastP.y != p2.y) {
            System.out.println("addExtremum diag end");
            mPointsPath.lineTo(p2.x,p2.y);
            listPoints.add(p2);
        }
    }

    private ArrayList<PPointF> initBezier(){
        if (listPoints.size()>0) {
            System.out.println("init bezier");
            mPath.reset();
            float ratioWidthHomothetie = (float)realWidth/width;
            float ratioHeightHomothetie = (float)realHeight/height;
            ArrayList<PPointF> B = new ArrayList<>();
            int n = listPoints.size()-1;
            float precision = 1f/step;
            if (n>0) {
                System.out.println("precision "+precision+" n "+n);
                float u = 0.0f;
                while (u <= 1f+precision) {
                    float x = 0.0f;
                    float y = 0.0f;
                    for(int i=0;i<n+1;i++) {
                        double b = (((factorialOf(n))/((factorialOf(i))*(factorialOf(n-i))))*(Math.pow(u,i))*(Math.pow((1-u),(n-i))));
                        PPointF p = listPoints.get(i);
                        x = (float)(x + b * p.x);
                        y = (float)(y + b * p.y);
                    }
                    if(B.size()==0) mPath.moveTo(x,y);
                    else mPath.lineTo(x,y);
                    B.add(new PPointF(x*ratioWidthHomothetie, y*ratioHeightHomothetie));
                    u += precision;
                }
            }
            return B;
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

    private void addPoint(PPointF p) {
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
                    diagbase[0].set(0, height);
                    diagbase[1].set(width, 0);
                    System.out.println(diagbase[0]+" "+diagbase[1]);
                    refreshDiag(p, true);
                }
                else if (startcap == 5 || startcap == 7) {
                    diagbase[0].set(0, 0);
                    diagbase[1].set(width, height);
                    System.out.println(diagbase[0]+" "+diagbase[1]);
                    refreshDiag(p, false);
                }
            }
            if (startcap != 0 && acceptPoint (p)) {
                refreshCursorCap(p);
                if (lentp > 0) {
                    PPointF lastP = listPoints.get(listPoints.size()-1);
                    mPointsPath.quadTo(lastP.x, lastP.y, (p.x + lastP.x) / 2, (p.y + lastP.y) / 2);
                }
                mCirclePath.reset();
                mCirclePath.addCircle(p.x, p.y, 30, Path.Direction.CW);
                listPoints.add(p);
            }
        }
    }

    private boolean acceptPoint(PPointF p) {
        System.out.println(p);
        if (listPoints.size() == 0)
            return true;
        PPointF lastP = listPoints.get(listPoints.size()-1);
        if (startcap == 1 && p.y<lastP.y)
            return true;
        else if (startcap ==2 && p.y>lastP.y)
            return true;
        else if (startcap ==3 && p.x<lastP.x)
            return true;
        else if (startcap ==4 && position_d_p(diagtemp[0], diagtemp[1], p)<0) {
            refreshDiag(p, true);
            return true;
        }
        else if (startcap ==5 && position_d_p (diagtemp[0], diagtemp[1], p)>0) {
            refreshDiag(p, false);
            return true;
        }
        else if (startcap ==6 && p.x>lastP.x)
            return true;
        else if (startcap ==7 && position_d_p (diagtemp[0], diagtemp[1], p)<0) {
            refreshDiag(p, false);
            return true;
        }
        else if (startcap ==8 && position_d_p (diagtemp[0], diagtemp[1], p)>0) {
            refreshDiag(p, true);
            return true;
        }
        return false;
    }

    private void refreshDiag(PPointF p,boolean invert){
        System.out.println("refresh Diag :"+p+" "+invert);
        float at = (float)height/width;
        System.out.println(at);
        if (invert)
            at*=-1;
        float xdiff = p.x-Line.calcX(p.y,0, diagbase[0].y,at);
        float ydiff = p.y-Line.calcY(p.x,0, diagbase[0].y,at);
        if (xdiff<0) {
            diagtemp[0].x = diagbase[0].x;
            diagtemp[0].y = diagbase[0].y + ydiff;
            diagtemp[1].x = diagbase[1].x + xdiff;
            diagtemp[1].y = diagbase[1].y;
        }
        else if (xdiff>0) {
            diagtemp[0].x = diagbase[0].x + xdiff;
            diagtemp[0].y = diagbase[0].y;
            diagtemp[1].x = diagbase[1].x;
            diagtemp[1].y = diagbase[1].y + ydiff;
        }
        System.out.println(diagtemp[0]+" "+diagtemp[1]);
    }

    private void refreshCursorCap(PPointF p) {
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
            mCursorPath.moveTo(diagtemp[0].x,diagtemp[0].y);
            mCursorPath.lineTo(diagtemp[1].x,diagtemp[1].y);
        }
    }

    private float position_d_p(PPointF p1,PPointF p2,PPointF m) {
        return (p2.x - p1.x) * (m.y - p1.y) - (p2.y - p1.y) * (m.x - p1.x);
    }

    public ArrayList<PPointF> getPath() {
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
