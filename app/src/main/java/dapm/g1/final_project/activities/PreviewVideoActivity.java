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
import android.widget.VideoView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_video);

        ButterKnife.bind(this);

        Uri uriData = Uri.parse(getIntent().getStringExtra("uri_video"));
        mVideoView.setVideoURI(uriData);

        mVideoView.start();
    }

    @OnClick(R.id.cancelVideo)
    void cancelChoosenVideo(){

    }

    @OnClick(R.id.controlVideoButton)
    void controlVideo(){
        if(mVideoView.isPlaying())
            mVideoView.pause();
        else
            mVideoView.start();
    }

    @OnClick(R.id.validVideo)
    void validVideo(){
        Intent nextActivity = new Intent(this, TypeActivity.class);
        startActivity(nextActivity);
    }

}
