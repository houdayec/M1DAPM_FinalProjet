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

    private String duration;
    private int sample=1;
    protected LinearLayout llay;
    protected ProgressDialog effectProgressDialog;
    public FFmpegMediaMetadataRetriever mediaMetadataRetriever;
    public double intervalRefresh;
   // protected List<Bitmap> listFrames = new ArrayList<>();
    protected  Bitmap finalBmp;
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

    public void createAnamorphose(Bitmap currentBmp,int[] pixels,int index){
        Log.e("coucouc","coucocucou");
        int height=currentBmp.getHeight();
        int width=currentBmp.getWidth();
        int currentPixel[]=new int[height*width];
        currentBmp.getPixels(currentPixel, 0, width, 0, 0, width, height);
        if(index<height) {//if choose TOP
            for (int j = 0; j < sample * width; j++) {
                if(index*width+j<width*height)
                    pixels[index * width + j] = currentPixel[index * width + j];
            }
        }
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

            int height = 0;
            int width = 0;
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


                Bitmap bmFirst = mediaMetadataRetriever.getFrameAtTime(0,FFmpegMediaMetadataRetriever.OPTION_CLOSEST);
                height=bmFirst.getHeight();
                width=bmFirst.getWidth();

                int size=height;
                sample = size/(Integer.valueOf(duration)*numberFrames);


                mProgressDialog.setTitle("test");
                mProgressDialog.setMessage("test");
                mProgressDialog.setIndeterminate(false);
                //mProgressDialog.show();
            }

            int currentTime = 0;
            System.out.println("duration " + duration);

            final int[] pixelsFinal=new int[width*height];
            int index=0;


            if(duration != null){
                duration = String.valueOf(Integer.valueOf(duration) * 1000);
                System.out.println("duration : " + duration);
                System.out.println("duration integer " + Integer.valueOf(duration));
                System.out.println("Interval refresh " + intervalRefresh);
                while(currentTime < Integer.valueOf(duration)){
                    final int finalCurrentTime = currentTime;
                    final  int finalIndex = index;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            if(mediaMetadataRetriever.getFrameAtTime(finalCurrentTime) != null){
                                Bitmap bmFrame = mediaMetadataRetriever.getFrameAtTime(finalCurrentTime,FFmpegMediaMetadataRetriever.OPTION_CLOSEST); //unit in microsecond
                                if(bmFrame!=null)
                                {
                                    createAnamorphose(bmFrame,pixelsFinal,finalIndex);
                                    //index+=sample;
                                   // int [] valeur={bmFrame.getWidth(),bmFrame.getHeight()};
                                   // publishProgress(pixelsFinal,valeur);
                                }
                            }
                        }
                    }).start();
                    index += sample;
                    currentTime = finalCurrentTime;
                    currentTime += intervalRefresh;
                    System.out.println(currentTime);
                }

                finalBmp=Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                finalBmp.setPixels(pixelsFinal, 0, width, 0, 0, width, height);

                //listFrames.add(finalBmp);

            }



            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {

            //mProgressDialog.dismiss();

           /* ImageView imgView=findViewById(R.id.imageView);
            imgView.setImageBitmap(finalBmp);*/

            Log.e("fin","fin");
            Toast.makeText(TypeActivity.this, "Traitement fini", Toast.LENGTH_SHORT).show();

        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected void onProgressUpdate(Void... values) {
           /* int[] pixelStart=values[0];
            int[] valeur=values[1];
            int w=valeur[0];
            int h=valeur[1];
            Bitmap finalBmp=Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            finalBmp.setPixels(pixelStart, 0, w, 0, 0,w, h);
            ImageView imgView=findViewById(R.id.imageView);
            imgView.setImageBitmap(finalBmp);*/
        }
    }


}
