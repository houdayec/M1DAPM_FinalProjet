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
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;

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
import dapm.g1.final_project.myMediaExtractor;
import wseemann.media.FFmpegMediaMetadataRetriever;

public class TypeActivity extends AppCompatActivity {

    /**
     * INTERN VARIABLES
     */
    @BindView(R.id.generateAnamorphosis)
    Button mGenerateAnamorphosisButton;

    @BindView((R.id.spinner))
    Spinner mSpinnerDirection;

    @BindView(R.id.validCustom)
    ToggleButton validCustom;

    @BindView(R.id.layoutDrawingView)
    LinearLayout layoutDrawingView;

    private Uri uriData;
    private DrawingView dv;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_type);

        // Binding view
        ButterKnife.bind(this);

        uriData = Uri.parse(getIntent().getStringExtra("uri_video"));

        try {
            myMediaExtractor mediaExtractor = new myMediaExtractor(PathUtil.getPath(this, uriData));
            mediaExtractor.selectTrack(mediaExtractor.getTrackVideoIndex());
            dv = new DrawingView(this,(int)Math.ceil(mediaExtractor.getVideoFrameRate()*(mediaExtractor.getVideoDuration()/1000000f)));
            dv.setBackgroundColor(getResources().getColor(R.color.white));

            layoutDrawingView.addView(dv);
        } catch (IOException | myMediaExtractor.NoTrackSelectedException e) {
            finish();
        }



        mSpinnerDirection.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (adapterView.getItemAtPosition(i).toString().equals("Custom")) {
                    validCustom.setChecked(true);
                    validCustom.setEnabled(true);
                } else {
                    validCustom.setChecked(false);
                    validCustom.setEnabled(false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Toast.makeText(TypeActivity.this, "Please select a direction", Toast.LENGTH_SHORT).show();
            }
        });
        validCustom.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                dv.setEventEnabled(b);
                if (b) {
                    Toast.makeText(TypeActivity.this, "Activated change", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(TypeActivity.this, "Validated drawing", Toast.LENGTH_SHORT).show();
                }
            }
        });
        validCustom.setChecked(false);
        validCustom.setEnabled(false);
    }

    /**
     * Method called when user clicks the generate button
     */
    @OnClick(R.id.generateAnamorphosis)
    void generateAnamorphosis() {
        Intent intentFinalRender = new Intent(this,Test_Multi_Thread.class);
        Bundle bundleArgs = new Bundle();
        if(mSpinnerDirection.getSelectedItem().toString().equals("Custom"))
        {
            Serializable spath = dv.getPath();
            if (!validCustom.isChecked() && spath!=null)
                bundleArgs.putSerializable("drawing", spath);
            else {
                Toast.makeText(TypeActivity.this, "Please valid your drawing", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        bundleArgs.putString("uri_video", uriData.toString());
        bundleArgs.putString("direction", mSpinnerDirection.getSelectedItem().toString());
        intentFinalRender.putExtras(bundleArgs);
        System.out.println("started custom anamorphosis");
        startActivity(intentFinalRender);
    }
}