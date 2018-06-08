package dapm.g1.final_project.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.IOException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dapm.g1.final_project.custom_classes.CustomPointF;
import dapm.g1.final_project.custom_classes.DrawingView;
import dapm.g1.final_project.utils.PathUtils;
import dapm.g1.final_project.R;
import dapm.g1.final_project.custom_classes.CustomMediaExtractor;

public class TypeActivity extends AppCompatActivity {

    /**
     * Binding View
     */
    @BindView(R.id.generateAnamorphosis)
    Button mGenerateAnamorphosisButton;

    @BindView((R.id.spinner))
    Spinner mSpinnerDirection;

    @BindView(R.id.validCustom)
    ToggleButton validCustom;

    @BindView(R.id.layoutDrawingView)
    LinearLayout layoutDrawingView;

    /**
     * INTERN VARIABLES
     */
    private Uri uriData;
    private DrawingView dv;

    /**
     *
     * @param savedInstanceState
     */
    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_type);

        // Binding view
        ButterKnife.bind(this);

        uriData = Uri.parse(getIntent().getStringExtra("uri_video"));

        try {
            CustomMediaExtractor mediaExtractor = new CustomMediaExtractor(PathUtils.getPath(this, uriData));
            mediaExtractor.selectTrack(mediaExtractor.getTrackVideoIndex());

            int w = mediaExtractor.getVideoWidth();
            int h = mediaExtractor.getVideoHeight();
            float f = mediaExtractor.getVideoFrameRate()*(mediaExtractor.getVideoDuration()/1000000f);
            System.out.println("video size w:"+w+" h:"+h+" nbFrame"+f);

            dv = new DrawingView(this,(int)Math.ceil(f)+1,w,h);

            dv.setBackgroundColor(Color.rgb(240,240,255));
            layoutDrawingView.addView(dv);
        } catch (IOException | CustomMediaExtractor.NoTrackSelectedException e) {
            finish();
        }

        // active the canvas to draw when custom item is chosen
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

        // actived the canvas according to the padlock
        validCustom.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean drawing) {
                dv.setEventEnabled(drawing);
                if (drawing) {
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
        Intent intentFinalRender = new Intent(this, FinalRenderActivity.class);
        Bundle bundleArgs = new Bundle();
        if(mSpinnerDirection.getSelectedItem().toString().equals("Custom"))
        {
            ArrayList<CustomPointF> spath = dv.getPath();
            // add to bundle the curve personalized
            if (!validCustom.isChecked() && spath!=null) {
                bundleArgs.putParcelableArrayList("drawing", spath);
                bundleArgs.putInt("start",dv.getStartcap());
                bundleArgs.putInt("end",dv.getEndcap());
            }
            else {
                Toast.makeText(TypeActivity.this, "Please valid your drawing", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        bundleArgs.putString("uri_video", uriData.toString());
        bundleArgs.putString("direction", mSpinnerDirection.getSelectedItem().toString());
        intentFinalRender.putExtras(bundleArgs);
        startActivity(intentFinalRender);
    }
}