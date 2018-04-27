package dapm.g1.final_project;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dapm.g1.final_project.activities.TypeActivity;

public class MainActivity extends AppCompatActivity {

    /**
     * VIEW ELEMENTS USING BUTTERKNIFE
     */
    @BindView(R.id.load_video_button)
    Button loadVideoButton;

    @BindView(R.id.record_video_button)
    Button recordVideoButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Changing transitions animations between activities
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);

        //Binding of ButterKnife to get view elements
        ButterKnife.bind(this);
    }

    /**
     * ON CLICKS METHODS (BUTTERKNIFE)
     */
    @OnClick(R.id.load_video_button)
    public void loadVideoButtonClicked(){

    }

    @OnClick(R.id.record_video_button)
    public void recordVideoButtonClicked(){
        switchToNextActivity();
    }

    public void switchToNextActivity(){
        Intent nextActivityIntent = new Intent(this, TypeActivity.class);
        startActivity(nextActivityIntent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
