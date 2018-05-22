package dapm.g1.final_project.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
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
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dapm.g1.final_project.PathUtil;
import dapm.g1.final_project.R;
import dapm.g1.final_project.VideoUtils;
import wseemann.media.FFmpegMediaMetadataRetriever;

public class TypeActivity extends AppCompatActivity {

    /**
     * INTERN VARIABLES
     */
    @BindView(R.id.generateAnamorphosis)
    ImageButton mGenerateAnamorphosisButton;

    static final int REQUEST_VIDEO_CAPTURE = 1;

    private Uri uriData;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_type);

        // Binding view
        ButterKnife.bind(this);

        uriData = Uri.parse(getIntent().getStringExtra("uri_video"));

    }

    /**
     * Method called when user clicks the generate button
     */
    @OnClick(R.id.generateAnamorphosis)
    void generateAnamorphosis(){
        Intent intentFinalRender = new Intent(this, FinalRenderActivity.class);
        Bundle bundleArgs = new Bundle();
        bundleArgs.putString("uri_video", uriData.toString());
        intentFinalRender.putExtras(bundleArgs);
        startActivity(intentFinalRender);
    }



}
