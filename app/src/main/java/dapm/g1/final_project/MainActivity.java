package dapm.g1.final_project;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dapm.g1.final_project.activities.PreviewVideoActivity;
import dapm.g1.final_project.activities.RecordVideoActivity;
import dapm.g1.final_project.activities.TypeActivity;
import wseemann.media.FFmpegMediaMetadataRetriever;

public class MainActivity extends AppCompatActivity {

    /**
     * VIEW ELEMENTS USING BUTTERKNIFE
     */
    @BindView(R.id.load_video_button)
    Button loadVideoButton;

    @BindView(R.id.record_video_button)
    Button recordVideoButton;

    private String fileManager;
    private String duration;
    private ProgressDialog effectProgressDialog;
    private FFmpegMediaMetadataRetriever mediaMetadataRetriever;
    private double intervalRefresh;

    static final int REQUEST_VIDEO_CAPTURE = 1;
    static final int REQUEST_READ_EXTERNAL_STORAGE = 100;
    static final int REQUEST_TAKE_GALLERY_VIDEO = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Changing transitions animations between activities
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);

        //Binding of ButterKnife to get view elements
        ButterKnife.bind(this);

        if ( ContextCompat.checkSelfPermission( this, Manifest.permission.CAMERA ) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, REQUEST_VIDEO_CAPTURE);
        }

        if ( ContextCompat.checkSelfPermission( this, Manifest.permission.READ_EXTERNAL_STORAGE ) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_EXTERNAL_STORAGE);
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

    public void switchToNextActivity(){
        Intent nextActivityIntent = new Intent(this, TypeActivity.class);
        startActivity(nextActivityIntent);
    }

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
        if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            Intent intentPreview = new Intent(this, PreviewVideoActivity.class);
            Bundle bundleArgs = new Bundle();
            Uri videoUri = intent.getData();
            bundleArgs.putString("uri_video", videoUri.toString());
            intentPreview.putExtras(bundleArgs);
            startActivity(intentPreview);
        }
        if (requestCode == REQUEST_TAKE_GALLERY_VIDEO && resultCode == RESULT_OK) {
            Toast.makeText(this, "RESULT", Toast.LENGTH_SHORT).show();

            fileManager = PathUtil.getPath(this, intent.getData());
            System.out.println(fileManager);
            //Log.e("path", pathSelectedVideo);
            Toast.makeText(this, fileManager, Toast.LENGTH_SHORT).show();
            if (fileManager != null) {
                //TODO
                mediaMetadataRetriever = new FFmpegMediaMetadataRetriever();
                mediaMetadataRetriever.setDataSource(fileManager);

                duration = mediaMetadataRetriever.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_DURATION);
                Log.e("duration", duration);

                // Create a Linear Layout for each contact?
                //llay = new LinearLayout(this);
                //llay.setOrientation(LinearLayout.HORIZONTAL);

                int numberFrames = getFrameRateVideo(fileManager);
                System.out.println("The video has a " + numberFrames + " frames / second");

                intervalRefresh = 1000000/numberFrames;

                //new FramesExtraction().execute();

                Intent intentPreview = new Intent(this, PreviewVideoActivity.class);
                Bundle bundleArgs = new Bundle();
                bundleArgs.putString("uri_video", fileManager);
                intentPreview.putExtras(bundleArgs);
                startActivity(intentPreview);

            }


        }
    }

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
        }
    }

    public int getFrameRateVideo(String path){
        MediaExtractor extractor = new MediaExtractor();
        int frameRate = 24; //may be default
        try {
            //Adjust data source as per the requirement if file, URI, etc.
            extractor.setDataSource(path);
            int numTracks = extractor.getTrackCount();
            System.out.println("Number of frames in video : " + numTracks);
            for (int i = 0; i < numTracks; ++i) {
                MediaFormat format = extractor.getTrackFormat(i);
                String mime = format.getString(MediaFormat.KEY_MIME);
                if (mime.startsWith("video/")) {
                    if (format.containsKey(MediaFormat.KEY_FRAME_RATE)) {
                        frameRate = format.getInteger(MediaFormat.KEY_FRAME_RATE);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            //Release stuff
            extractor.release();
        }
        return frameRate;
    }
}
