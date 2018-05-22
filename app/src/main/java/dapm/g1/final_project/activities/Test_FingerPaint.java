package dapm.g1.final_project.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.media.Image;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;

import java.util.ArrayList;
import java.util.List;

import dapm.g1.final_project.R;

public class Test_FingerPaint extends AppCompatActivity implements View.OnClickListener {

    DrawingView dv ;
    private Paint mPaint;
    private boolean didUserAlreadyDraw = false;
    private ImageButton cancelVideo;
    private ImageButton validVideo;
    float tempDx, tempDy = 0;

    private List<Point> listPoints = new ArrayList<>();

    public int width;
    public  int height;
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Path mPath;
    private Paint   mBitmapPaint;
    Context context;
    private Paint circlePaint;
    private Path circlePath;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dv = new DrawingView(this);
        setContentView(dv);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(Color.BLUE);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(10);

        validVideo = findViewById(R.id.validVideoFP);
        cancelVideo = findViewById(R.id.cancelVideoFP);

        validVideo.setOnClickListener(this);
        cancelVideo.setOnClickListener(this);

        validVideo.setVisibility(View.GONE);
        cancelVideo.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.validVideo:
                validVideo.setVisibility(View.GONE);
                cancelVideo.setVisibility(View.GONE);

                Intent intentFinalRender = new Intent(this, FinalRenderActivity.class);
                Bundle bundleArgs = new Bundle();
                //bundleArgs.putString("uri_video", uriData.toString());
                intentFinalRender.putExtras(bundleArgs);
                startActivity(intentFinalRender);
                break;
            case R.id.cancelVideo:
                validVideo.setVisibility(View.GONE);
                cancelVideo.setVisibility(View.GONE);
                tempDx = 0;
                mCanvas = new Canvas();
                listPoints = new ArrayList<>();
                break;
        }
    }

    public class DrawingView extends View {

        public DrawingView(Context c) {
            super(c);
            context=c;
            mPath = new Path();
            mBitmapPaint = new Paint(Paint.DITHER_FLAG);
            circlePaint = new Paint();
            circlePath = new Path();
            circlePaint.setAntiAlias(true);
            circlePaint.setColor(Color.BLUE);
            circlePaint.setStyle(Paint.Style.STROKE);
            circlePaint.setStrokeJoin(Paint.Join.MITER);
            circlePaint.setStrokeWidth(10);
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);

            mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(mBitmap);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            canvas.drawBitmap( mBitmap, 0, 0, mBitmapPaint);
            canvas.drawPath( mPath,  mPaint);
            canvas.drawPath( circlePath,  circlePaint);
        }

        private float mX, mY;
        private static final float TOUCH_TOLERANCE = 4;

        private void touch_start(float x, float y) {
            mPath.reset();
            mPath.moveTo(x, y);
            mX = x;
            mY = y;
            listPoints.add(new Point((int)x, (int)y));
        }

        private void touch_move(float x, float y) {
            float dx = Math.abs(x - mX);
            float dy = Math.abs(y - mY);
            System.out.println("coords : " + dx + " : " + dy);
            System.out.println("coords : " + x + " : " + y);
            if(tempDx < x){
                if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                    mPath.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);
                    mX = x;
                    mY = y;
                    circlePath.reset();
                    circlePath.addCircle(mX, mY, 30, Path.Direction.CW);
                    tempDx = x;
                    listPoints.add(new Point((int)x, (int)y));
                }
            }

        }

        private void touch_up() {
            mPath.lineTo(mX, mY);
            circlePath.reset();
            // commit the path to our offscreen
            mCanvas.drawPath(mPath,  mPaint);
            // kill this so we don't double draw
            mPath.reset();

            validVideo.setVisibility(VISIBLE);
            cancelVideo.setVisibility(VISIBLE);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
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
            return true;
        }
    }

}
