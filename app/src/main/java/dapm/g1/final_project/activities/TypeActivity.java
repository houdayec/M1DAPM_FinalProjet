package dapm.g1.final_project.activities;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dapm.g1.final_project.CustomView.DrawingView;
import dapm.g1.final_project.PathUtil;
import dapm.g1.final_project.R;
import dapm.g1.final_project.VideoUtils;
import wseemann.media.FFmpegMediaMetadataRetriever;

public class TypeActivity extends AppCompatActivity implements View.OnClickListener {

    /**
     * INTERN VARIABLES
     */
    @BindView(R.id.generateAnamorphosis)
    Button mGenerateAnamorphosisButton;

    @BindView((R.id.spinner))
    Spinner mSpinnerDirection;

    @BindView(R.id.validVideoFP)
    ImageButton validVideo;

    @BindView(R.id.layoutDrawingView)
    LinearLayout layoutDrawingView;


    private Paint mPaint;
    private boolean didUserAlreadyDraw = false;

    float tempDx, tempDy = 0;

    private List<Point> listPoints = new ArrayList<>();

    public int width;
    public int height;
    private boolean validCanvas;
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Path mPath;
    private Paint mBitmapPaint;
    Context context;
    private Paint circlePaint;
    private Path circlePath;
    private Uri uriData;
    private DrawingView dv;
    public String fileManager;
    public FFmpegMediaMetadataRetriever mediaMetadataRetriever;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_type);

        // Binding view
        ButterKnife.bind(this);

        uriData = Uri.parse(getIntent().getStringExtra("uri_video"));

        dv = new DrawingView(this);
        dv.setBackgroundColor(getResources().getColor(R.color.white));

        layoutDrawingView.addView(dv);

        mSpinnerDirection.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (adapterView.getItemAtPosition(i).toString().equals("Custom")) {
                    validVideo.setEnabled(true);
                    validVideo.setBackgroundColor(getResources().getColor(R.color.colorPrimary));

                } else {
                    validVideo.setEnabled(false);
                    validVideo.setBackgroundColor(Color.GRAY);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                System.out.println("nothing");
            }
        });
        validVideo.setOnClickListener(this);

        validVideo.setEnabled(false);
        validVideo.setBackgroundColor(Color.GRAY);
    }

    /**
     * Method called when user clicks the generate button
     */
    @OnClick(R.id.generateAnamorphosis)
    void generateAnamorphosis() {
        Intent intentFinalRender = new Intent(this, Test_recup_frame.class);
        Bundle bundleArgs = new Bundle();
        bundleArgs.putString("uri_video", uriData.toString());
        bundleArgs.putString("direction", mSpinnerDirection.getSelectedItem().toString());
        if(mSpinnerDirection.getSelectedItem().toString().equals("Custom"))
        {
            bundleArgs.putSerializable("drawing", (Serializable) listPoints);
        }
        intentFinalRender.putExtras(bundleArgs);
        startActivity(intentFinalRender);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.validVideoFP:
                if(validCanvas == false) {
                    validCanvas = true;
                    Toast.makeText(TypeActivity.this, "Validated drawing", Toast.LENGTH_SHORT).show();
                    validVideo.setImageDrawable(getResources().getDrawable(R.drawable.ic_lock_white_24dp));

                }
                else {
                    validCanvas = false;
                    Toast.makeText(TypeActivity.this, "Activated change", Toast.LENGTH_SHORT).show();
                    validVideo.setImageDrawable(getResources().getDrawable(R.drawable.ic_lock_open_white_24dp));

                }
                break;

        }
    }

}