package dapm.g1.final_project;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dapm.g1.final_project.activities.PreviewVideoActivity;
import dapm.g1.final_project.utils.PathUtils;
import dapm.g1.final_project.utils.VideoUtils;
import wseemann.media.FFmpegMediaMetadataRetriever;

public class MainActivity extends AppCompatActivity {

    /**
     * VIEW ELEMENTS USING BUTTERKNIFE
     */
    @BindView(R.id.load_video_button)
    Button loadVideoButton;

    @BindView(R.id.record_video_button)
    Button recordVideoButton;

    /**
     * UTILS VARS
     */
    private String fileManager, duration;
    private FFmpegMediaMetadataRetriever mediaMetadataRetriever;
    private double intervalRefresh;

    /**
     * REQUEST VALUES
     */
    static final int REQUEST_VIDEO_CAPTURE = 1;
    static final int REQUEST_READ_EXTERNAL_STORAGE = 100;
    static final int REQUEST_TAKE_GALLERY_VIDEO = 200;
    static final int REQUEST_WRITE_EXTERNAL_STORAGE = 201;

    /**
     * METHOD TO INITIALIZE THE ACTIVITY
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Changing transitions animations between activities
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);

        // Binding of ButterKnife to get view elements
        ButterKnife.bind(this);

        // Checking needed permissions
        if ( ContextCompat.checkSelfPermission( this, Manifest.permission.CAMERA ) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, REQUEST_VIDEO_CAPTURE);
        }
    }

    /**
     * ON CLICKS METHODS (BUTTERKNIFE)
     */

    /**
     * Method called when user wants to load a video
     */
    @OnClick(R.id.load_video_button)
    public void loadVideoButtonClicked(){
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Video"),REQUEST_TAKE_GALLERY_VIDEO);
    }

    /**
     * Method called when user wants to record a video
     */
    @OnClick(R.id.record_video_button)
    public void recordVideoButtonClicked(){
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
        }
    }

    /**
     * Method called when user press return button
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    /**
     * Callback method to catch the results of launched activity
     * @param requestCode
     * @param resultCode
     * @param intent
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        // If the user wants to record a new video
        if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            Intent intentPreview = new Intent(this, PreviewVideoActivity.class);
            Bundle bundleArgs = new Bundle();
            Uri videoUri = intent.getData();
            bundleArgs.putString("uri_video", videoUri.toString());
            intentPreview.putExtras(bundleArgs);
            startActivity(intentPreview);
        }

        // If the user wants to use a video of his gallery
        if (requestCode == REQUEST_TAKE_GALLERY_VIDEO && resultCode == RESULT_OK) {
            fileManager = PathUtils.getPath(this, intent.getData());

            if (fileManager != null) {
                mediaMetadataRetriever = new FFmpegMediaMetadataRetriever();
                mediaMetadataRetriever.setDataSource(fileManager);

                // Video duration
                duration = mediaMetadataRetriever.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_DURATION);

                // Video frame per second
                int numberFrames = VideoUtils.getFrameRateVideo(fileManager);

                // Refresh rate
                intervalRefresh = 1000000/numberFrames;

                // Change activity to the preview of selected video
                Intent intentPreview = new Intent(this, PreviewVideoActivity.class);
                Bundle bundleArgs = new Bundle();
                bundleArgs.putString("uri_video", intent.getData().toString());
                intentPreview.putExtras(bundleArgs);
                startActivity(intentPreview);

            }
        }
    }

    /**
     * Callback method for permissions requests
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String permissions[],
            int[] grantResults) {
        switch (requestCode) {
            case REQUEST_VIDEO_CAPTURE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this, "Permission Granted!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Permission Denied!", Toast.LENGTH_SHORT).show();
                }
                if ( ContextCompat.checkSelfPermission( this, Manifest.permission.READ_EXTERNAL_STORAGE ) != PackageManager.PERMISSION_GRANTED ) {
                    ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_EXTERNAL_STORAGE);
                }
                break;
            case REQUEST_READ_EXTERNAL_STORAGE:
                if ( ContextCompat.checkSelfPermission( this, Manifest.permission.WRITE_EXTERNAL_STORAGE ) != PackageManager.PERMISSION_GRANTED ) {
                    ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE);
                }
                break;
        }
    }
}
