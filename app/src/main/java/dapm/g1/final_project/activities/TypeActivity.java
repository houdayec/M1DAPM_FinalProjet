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

    public String duration;
    protected LinearLayout llay;
    protected ProgressDialog effectProgressDialog;
    public FFmpegMediaMetadataRetriever mediaMetadataRetriever;
    public double intervalRefresh;
    protected List<Bitmap> listFrames = new ArrayList<>();
    protected ProgressDialog mProgressDialog;
    public String fileManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_type);

        // Binding view
        ButterKnife.bind(this);

        // Setting up the view
        mProgressDialog = new ProgressDialog(getApplicationContext());

        // Starting async task (another thread)
        new FramesExtraction().execute();

    }

    /**
     * Method called when user clicks the generate button
     */
    @OnClick(R.id.generateAnamorphosis)
    void generateAnamorphosis(){
        new FramesExtraction().execute();
    }

    /**
     * Inner class (Asynchronous task) to get all the frames from a video //TODO MICKAEL ALGO IMP ?
     */
    private class FramesExtraction extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            Uri uriData = Uri.parse(getIntent().getStringExtra("uri_video"));

            fileManager = PathUtil.getPath(getApplicationContext(), uriData);

            System.out.println(fileManager);
            //Log.e("path", pathSelectedVideo);
            if (fileManager != null) {
                //TODO
                mediaMetadataRetriever = new FFmpegMediaMetadataRetriever();
                mediaMetadataRetriever.setDataSource(fileManager);

                duration = mediaMetadataRetriever.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_DURATION);
                Log.e("duration", duration);

                // Create a Linear Layout for each contact?
                llay = new LinearLayout(getApplicationContext());
                llay.setOrientation(LinearLayout.HORIZONTAL);

                int numberFrames = VideoUtils.getFrameRateVideo(fileManager);
                System.out.println("The video has a " + numberFrames + " frames / second");

                intervalRefresh = 1000000/numberFrames;


                mProgressDialog.setTitle("test");
                mProgressDialog.setMessage("test");
                mProgressDialog.setIndeterminate(false);
                mProgressDialog.show();
            }

            int currentTime = 0;
            System.out.println("duration " + duration);

            if(duration != null){
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

        }

        @Override
        protected void onProgressUpdate(Void... values) {}
    }


}
