package dapm.g1.final_project.activities;

import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dapm.g1.final_project.R;

public class TypeActivity extends AppCompatActivity {

    /**
     * INTERN VARIABLES
     */
    @BindView(R.id.previousActivityButton)
    ImageButton mPreviousActivityButton;

    @BindView(R.id.nextActivityButton)
    ImageButton mNextActivityButton;

    static final int REQUEST_VIDEO_CAPTURE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_type);

        ButterKnife.bind(this);
    }

    /**
     * Method called to go back to the previous activity
     */
    @OnClick(R.id.previousActivityButton)
    void goToPreviousActivity(){

    }

    /**
     * Method called to go back to the next activity
     */
    @OnClick(R.id.nextActivityButton)
    void goToNextActivity(){

    }

}
