package dapm.g1.final_project.activities;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

import butterknife.BindView;
import butterknife.OnClick;
import dapm.g1.final_project.R;

public class TypeActivity extends AppCompatActivity {

    @BindView(R.id.load_video_button)
    ImageView mLoadVideoButton;

    @BindView(R.id.record_video_button)
    ImageView mRecordVideoButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_type);

    }

    @OnClick
    public void loadVideoButtonClicked(){

    }

    @OnClick
    public void recordVideoButtonClicked(){

    }

}
