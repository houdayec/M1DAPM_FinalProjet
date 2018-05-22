package dapm.g1.final_project.activities;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dapm.g1.final_project.MainActivity;
import dapm.g1.final_project.PathUtil;
import dapm.g1.final_project.R;
import dapm.g1.final_project.VideoUtils;
import wseemann.media.FFmpegMediaMetadataRetriever;

public class FinalRenderActivity extends AppCompatActivity {

    /**
     * Binding view elements
     */
    @BindView(R.id.imageViewAnamorphosis)
    ImageView mImageViewAnamorphosis;

    @BindView(R.id.downloadAnamorphosisButton)
    ImageButton mDownloadAnamorphosisButton;

    @BindView(R.id.shareAnamorphosisButton)
    ImageButton mShareAnamorphosisButton;

    @BindView(R.id.backToMenuButton)
    Button mBackToMenuButton;

    private String duration;
    protected LinearLayout llay;
    protected ProgressDialog effectProgressDialog;
    public FFmpegMediaMetadataRetriever mediaMetadataRetriever;
    public double intervalRefresh;
    protected  Bitmap finalBmp;
    protected ProgressDialog mProgressDialog;
    public String fileManager;
    private int sample = 1;
    private Uri uriData;
    private String direction;


    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_final_render);

        // Binding the view
        ButterKnife.bind(this);



        uriData = Uri.parse(getIntent().getStringExtra("uri_video"));
        direction = getIntent().getStringExtra("direction");
        Log.e("direction",direction);
        fileManager = PathUtil.getPath(this, uriData);

        mediaMetadataRetriever = new FFmpegMediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(fileManager);

        int numberFrames = VideoUtils.getFrameRateVideo(fileManager);
        System.out.println("The video has a " + numberFrames + " frames / second");

        // TODO DIRECTION

        Bitmap bmFrame = mediaMetadataRetriever.getFrameAtTime(0,FFmpegMediaMetadataRetriever.OPTION_CLOSEST); //unit in microsecond
        int stackPixels=1;
        if(direction.equals("Top") || direction.equals("Bottom"))
            stackPixels = bmFrame.getHeight();
        else if(direction.equals("Left") || direction.equals("Right"))
            stackPixels = bmFrame.getWidth();
        duration = mediaMetadataRetriever.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_DURATION);

        System.out.println("current duration : " + (Integer.parseInt(duration)));

        sample = stackPixels/((Integer.parseInt(duration)/1000)*numberFrames);
        System.out.println("start scale "+ sample);
        intervalRefresh = 1000000/numberFrames;

        duration = String.valueOf(Integer.valueOf(duration) * 1000);


        // Starting anamorphosis
        new FramesExtraction().execute();

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

    /**
     * Inner class (Asynchronous task) to get all the frames from a video //TODO MICKAEL ALGO IMP ?
     */
    @SuppressLint("NewApi")
    private class FramesExtraction extends AsyncTask<String, int[], String> {

        @Override
        protected String doInBackground(String... params) {

            int currentTime = 0;
            Bitmap bmpStart=null;
            int [] pixelsArrayTemp=null;
            int indice=0;

            System.out.println("Current file duration : " + duration);

            while(currentTime < Integer.valueOf(duration)){
                Bitmap bmFrame = mediaMetadataRetriever.getFrameAtTime(currentTime,FFmpegMediaMetadataRetriever.OPTION_CLOSEST); //unit in microsecond
                if(currentTime==0)
                {
                    bmpStart=bmFrame;
                    pixelsArrayTemp=new int[bmpStart.getWidth()*bmpStart.getHeight()];
                    finalBmp=Bitmap.createBitmap(bmpStart.getWidth(), bmpStart.getHeight(), Bitmap.Config.ARGB_8888);
                }
                if(bmFrame!=null)
                {
                    createAnamorphosis(bmFrame,pixelsArrayTemp,indice);
                    System.out.println("scale : " + sample);
                    indice+=sample;
                    int [] valeur={bmFrame.getWidth(),bmFrame.getHeight()};
                    publishProgress(pixelsArrayTemp,valeur);
                }
                currentTime += intervalRefresh;
                System.out.println("currentTime : " + currentTime);
            }
            if(bmpStart != null) {
                finalBmp.setPixels(pixelsArrayTemp, 0, bmpStart.getWidth(), 0, 0, bmpStart.getWidth(), bmpStart.getHeight());
            }
            return "Executed";}



        @Override
        protected void onPostExecute(String result) {

            //mProgressDialog.dismiss();

            mImageViewAnamorphosis.setImageBitmap(finalBmp);

            Log.e("onPostExecute","reached");

            Toast.makeText(FinalRenderActivity.this, "Traitement fini", Toast.LENGTH_SHORT).show();

        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected void onProgressUpdate(int[]... values) {
            int[] pixelsArray=values[0];
            int[] valeur=values[1];
            int w=valeur[0];
            int h=valeur[1];
            //Bitmap finalBmp=Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            finalBmp.setPixels(pixelsArray, 0, w, 0, 0,w, h);
            mImageViewAnamorphosis.setImageBitmap(finalBmp);
        }

    }

    /**
     * Method to create an anamorphosis
     * @param currentBmp
     * @param pixels
     * @param index
     */
    public void createAnamorphosis(Bitmap currentBmp,int[] pixels,int index){
        int height=currentBmp.getHeight();
        int width=currentBmp.getWidth();
        int currentPixel[]=new int[height*width];
        currentBmp.getPixels(currentPixel, 0, width, 0, 0, width, height);
        switch (direction)
        {
            case "Top" :
                if(index<height) {
                    for (int j = 0; j < sample * width; j++) {
                        if((index*width)+j < width*height)
                            pixels[index * width + j] = currentPixel[index * width + j];
                    }
                }
                break;
            case "Bottom" :
                if(index<height) {
                    int indexTmp = (height-1) - index;
                    for (int j = (height-1); j >(height-1)-(sample * width); j--) {
                        if((indexTmp*width)+j < width*height){
                            pixels[indexTmp * width + j] = currentPixel[indexTmp * width + j];
                        }
                    }
                }
                break;
            case "Left" :
                if(index<width) {
                    for (int k = 0; k < sample; k++) {
                        for (int i = 0; i < height; i++) {
                            if ((i*width)+index+k < width * height)
                                pixels[i * width + (index+k)] = currentPixel[i * width + (index+k)];
                        }
                    }
                }
                break;

            case "Right" :
                if(index<width) {
                    int indexTmp = (width-1) - index;
                    for (int k = 0; k < sample; k++) {
                        for (int i = 0; i < height; i++) {
                            if ((i*width)+indexTmp-k > 0)
                                pixels[i * width + (indexTmp-k)] = currentPixel[i * width + (indexTmp-k)];
                        }
                    }
                }
                break;
            default:
                System.out.println("Error on the direction");
                Log.e("error","errror");
                break;
        }

    }



}
