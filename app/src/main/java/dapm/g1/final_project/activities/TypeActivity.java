package dapm.g1.final_project.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dapm.g1.final_project.CustomView.DrawingView;
import dapm.g1.final_project.PPointF;
import dapm.g1.final_project.PathUtil;
import dapm.g1.final_project.R;
import dapm.g1.final_project.myMediaExtractor;

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
            int w = mediaExtractor.getVideoWidth();
            int h = mediaExtractor.getVideoHeight();
            System.out.println("video size w:"+w+" h:"+h);
            dv = new DrawingView(this,(int)Math.ceil(mediaExtractor.getVideoFrameRate()*(mediaExtractor.getVideoDuration()/1000000f)),w,h);
            dv.setBackgroundColor(Color.rgb(240,240,255));
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
        Intent intentFinalRender = new Intent(this, FinalRenderActivity.class);
        Bundle bundleArgs = new Bundle();
        if(mSpinnerDirection.getSelectedItem().toString().equals("Custom"))
        {
            ArrayList<PPointF> spath = dv.getPath();
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