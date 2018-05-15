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
import wseemann.media.FFmpegMediaMetadataRetriever;

public class TypeActivity extends AppCompatActivity {

    /**
     * INTERN VARIABLES
     */
    @BindView(R.id.generateAnamorphosis)
    ImageButton mGenerateAnamorphosisButton;

    static final int REQUEST_VIDEO_CAPTURE = 1;

    private String duration;
    private LinearLayout llay;
    private ProgressDialog effectProgressDialog;
    private FFmpegMediaMetadataRetriever mediaMetadataRetriever;
    private double intervalRefresh;
    private List<Bitmap> listFrames = new ArrayList<>();
    private ProgressDialog mProgressDialog;
    private String fileManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_type);

        ButterKnife.bind(this);

        Uri uriData = Uri.parse(getIntent().getStringExtra("uri_video"));

        System.out.println("uri video : " + uriData);

        fileManager = PathUtil.getPath(this, uriData);

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
            llay = new LinearLayout(this);
            llay.setOrientation(LinearLayout.HORIZONTAL);

            int numberFrames = getFrameRateVideo(fileManager);
            System.out.println("The video has a " + numberFrames + " frames / second");

            intervalRefresh = 1000000/numberFrames;

            new FramesExtraction().execute();


        }
    }

    @OnClick(R.id.generateAnamorphosis)
    void generateAnamorphosis(){
        new FramesExtraction().execute();
    }

    private class FramesExtraction extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            int currentTime = 0;
            duration = String.valueOf(Integer.valueOf(duration) * 1000);
            System.out.println("duration : " + duration);
            System.out.println("duration integer " + Integer.valueOf(duration));
            System.out.println("Interval refresh " + intervalRefresh);
            while(currentTime < Integer.valueOf(duration)){
                final int finalCurrentTime = currentTime;
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        if(mediaMetadataRetriever.getFrameAtTime(finalCurrentTime) != null){
                            Bitmap bmFrame = mediaMetadataRetriever.getFrameAtTime(finalCurrentTime); //unit in microsecond
                            listFrames.add(bmFrame);
                        }
                    }
                }).start();
                currentTime = finalCurrentTime;
                currentTime += intervalRefresh;
                System.out.println(currentTime);
            }
            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
            System.out.println(listFrames.size() + " results");
            for (Bitmap bm : listFrames) {
                System.out.println("Image found");
                ImageView imgView = new ImageView(TypeActivity.this);
                imgView.setImageBitmap(bm);
                llay.addView(imgView);
            }
            mProgressDialog.dismiss();
            //scrollView.addView(llay);

        }

        @Override
        protected void onPreExecute() {
            mProgressDialog = new ProgressDialog(TypeActivity.this);
            mProgressDialog.setTitle("test");
            mProgressDialog.setMessage("test");
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.show();
        }

        @Override
        protected void onProgressUpdate(Void... values) {}
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
