package dapm.g1.final_project.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageButton;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dapm.g1.final_project.MainActivity;
import dapm.g1.final_project.R;

public class FinalRenderActivity extends AppCompatActivity {

    /**
     * Binding view elements
     */
    @BindView(R.id.downloadAnamorphosisButton)
    ImageButton mDownloadAnamorphosisButton;

    @BindView(R.id.shareAnamorphosisButton)
    ImageButton mShareAnamorphosisButton;

    @BindView(R.id.backToMenuButton)
    ImageButton mBackToMenuButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_final_render);

        // Binding the view
        ButterKnife.bind(this);

    }

    /**
     * Butterknife OnClick methods
     */

    @OnClick(R.id.downloadAnamorphosisButton)
    void downloadAnamorphosis(){
        //TODO
    }

    @OnClick(R.id.shareAnamorphosisButton)
    void shareAnamorphosis(){
        //TODO
    }

    @OnClick(R.id.backToMenuButton)
    void backToMenu(){
        Intent goToMainMenuIntent = new Intent(this, MainActivity.class);
        startActivity(goToMainMenuIntent);
    }
}
