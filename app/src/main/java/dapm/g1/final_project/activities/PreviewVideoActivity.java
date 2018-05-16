package dapm.g1.final_project.activities;

import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.VideoView;

import java.lang.reflect.Type;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dapm.g1.final_project.MainActivity;
import dapm.g1.final_project.R;

public class PreviewVideoActivity extends AppCompatActivity {

    @BindView(R.id.videoView)
    VideoView mVideoView;

    @BindView(R.id.cancelVideo)
    FloatingActionButton mCancelVideoButton;

    @BindView(R.id.validVideo)
    FloatingActionButton mValidVideo;

    @BindView(R.id.controlVideoButton)
    FloatingActionButton mControlVideoButton;

    private Uri uriData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_video);

        // Binding view with butterknife
        ButterKnife.bind(this);

        // Setting up the VideoView
        uriData = Uri.parse(getIntent().getStringExtra("uri_video"));
        mVideoView.setVideoURI(uriData);

        mVideoView.start();
    }

    /**
     * Method called when user clicks on the back button
     */
    @OnClick(R.id.cancelVideo)
    void cancelChoosenVideo(){
        Intent goToMainScreenIntent = new Intent(this, MainActivity.class);
        startActivity(goToMainScreenIntent);
    }

    /**
     * Method called when user clicks play/pause control button
     */
    @OnClick(R.id.controlVideoButton)
    void controlVideo(){
        if(mVideoView.isPlaying())
            mVideoView.pause();
        else
            mVideoView.start();
    }

    /**
     * Method called when user clicks the valid button
     */
    @OnClick(R.id.validVideo)
    void validVideo(){
        Intent intentType = new Intent(this, TypeActivity.class);
        Bundle bundleArgs = new Bundle();
        bundleArgs.putString("uri_video", uriData.toString());
        Toast.makeText(this, "uri : " + uriData.toString(), Toast.LENGTH_LONG).show();
        intentType.putExtras(bundleArgs);
        startActivity(intentType);
    }

}
