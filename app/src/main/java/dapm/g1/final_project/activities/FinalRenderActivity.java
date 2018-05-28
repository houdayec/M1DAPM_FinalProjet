package dapm.g1.final_project.activities;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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
    // protected List<Bitmap> listFrames = new ArrayList<>();
    protected  Bitmap finalBmp;
    protected ProgressDialog mProgressDialog;
    public String fileManager;
    private int sample = 1;
    private Uri uriData;

    // Interpolation utils
    private boolean interpolate = false;
    private int interpolationSample;


    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_final_render);

        // Binding the view
        ButterKnife.bind(this);

        uriData = Uri.parse(getIntent().getStringExtra("uri_video"));
        fileManager = PathUtil.getPath(this, uriData);

        mediaMetadataRetriever = new FFmpegMediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(fileManager);

        int numberFrames = VideoUtils.getFrameRateVideo(fileManager);
        System.out.println("The video has a " + numberFrames + " frames / second");

        // TODO DIRECTION

        Bitmap bmFrame = mediaMetadataRetriever.getFrameAtTime(0,FFmpegMediaMetadataRetriever.OPTION_CLOSEST); //unit in microsecond
        int stackPixels = bmFrame.getHeight();
        duration = mediaMetadataRetriever.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_DURATION);

        System.out.println("current duration : " + (Integer.parseInt(duration)));

        // 1920 / (30fps * 3sec) = scale
        // Si scale > 4 on interpole
        sample = stackPixels/((Integer.parseInt(duration)/1000)*numberFrames);
        // test if we need to interpolate
        if(sample > 4){
            interpolate = true;
            sample = 4;
            // Nombre d'images a créer entre chaque images
            interpolationSample = stackPixels/((Integer.parseInt(duration)/1000)*numberFrames*4) - 1;
        }

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
            // Second bitmap if we need to interpolate
            Bitmap bmpEnd = null;

            System.out.println("Current file duration : " + duration);

            while(currentTime < Integer.valueOf(duration)){
                Bitmap bmFrame = mediaMetadataRetriever.getFrameAtTime(currentTime,FFmpegMediaMetadataRetriever.OPTION_CLOSEST); //unit in microsecond

                if(currentTime==0)
                {
                    bmpStart=bmFrame;
                    pixelsArrayTemp=new int[bmpStart.getWidth()*bmpStart.getHeight()];
                    finalBmp=Bitmap.createBitmap(bmpStart.getWidth(), bmpStart.getHeight(), Bitmap.Config.ARGB_8888);
                }

                if(interpolate){
                    currentTime += intervalRefresh;
                    bmpEnd = mediaMetadataRetriever.getFrameAtTime(currentTime, FFmpegMediaMetadataRetriever.OPTION_CLOSEST);

                    createAnamorphosis(bmFrame, pixelsArrayTemp, indice);
                    indice += sample;
                    if(bmFrame != null) {
                        int[] valeur = {bmFrame.getWidth(), bmFrame.getHeight()};
                        publishProgress(pixelsArrayTemp, valeur);
                    }
                    // Calcul du coefficient à utiliser pour le calcul d'interpolation
                    float bmpCount = (float) (1.0/interpolationSample);
                    Log.e("bmpCount", String.valueOf(bmpCount));
                    // On créér le nombre d'image calculé précédement
                    for(float i = bmpCount; i < 1; i += bmpCount) {
                        if(bmpEnd != null) {
                            // Calcul d'une nouvelle image interpolée
                            Bitmap bmpToWorkOn = bitmapInterpolation(bmFrame, bmpEnd, i);

                            createAnamorphosis(bmpToWorkOn, pixelsArrayTemp, indice);
                            indice += sample;
                            int[] valeur = {bmpEnd.getWidth(), bmpEnd.getHeight()};
                            publishProgress(pixelsArrayTemp, valeur);

                            Log.e("Interpolate", String.valueOf(i));
                        }
                    }

                    if(bmpEnd != null) {
                        createAnamorphosis(bmpEnd, pixelsArrayTemp, indice);
                        int[] valeur = {bmpEnd.getWidth(), bmpEnd.getHeight()};
                        publishProgress(pixelsArrayTemp, valeur);
                    }
                    indice += sample;
                    Log.e("currentTime", String.valueOf(currentTime));
                }
                else {
                    if (bmFrame != null) {
                        createAnamorphosis(bmFrame, pixelsArrayTemp, indice);
                        System.out.println("scale : " + sample);
                        indice += sample;
                        int[] valeur = {bmFrame.getWidth(), bmFrame.getHeight()};
                        publishProgress(pixelsArrayTemp, valeur);
                    }
                    currentTime += intervalRefresh;
                    System.out.println("currentTime : " + currentTime);
                }
            }
            if(bmpStart !=null) {
                finalBmp.setPixels(pixelsArrayTemp, 0, bmpStart.getWidth(), 0, 0, bmpStart.getWidth(), bmpStart.getHeight());
            }
            return "Executed";
        }



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
        /*System.out.println(pixels.toString());
        System.out.println(pixels.length);*/
        if(index<height) {//if choose TOP
            for (int j = 0; j < sample * width; j++) {
                if(index*width+j<width*height)
                    pixels[index * width + j] = currentPixel[index * width + j];
            }
        }
    }

    /**
     * Method to generate new bitmaps if needed
     * @param firstBmp
     * @param lastBmp
     * @param bmpToCreate
     */
    public Bitmap bitmapInterpolation(Bitmap firstBmp, Bitmap lastBmp, float bmpToCreate){
        int height = firstBmp.getHeight();
        int width = firstBmp.getWidth();
        int initialPixels[] = new int[height * width];
        int finalPixels[] = new int[height * width];
        int newPixels[] = new int[height * width];
        int pixelColorStart[] = new int[3];
        int pixelColorEnd[] = new int[3];
        int newColor;

        firstBmp.getPixels(initialPixels, 0, width, 0, 0, width, height);
        lastBmp.getPixels(finalPixels, 0, width, 0, 0, width, height);

        Log.e("Interpolate", "Creating a bitmap ...");
        int k = 0;
        while(k < initialPixels.length){
            pixelColorStart[0] = Color.red(initialPixels[k]);
            pixelColorStart[1] = Color.green(initialPixels[k]);
            pixelColorStart[2] = Color.blue(initialPixels[k]);

            pixelColorEnd[0] = Color.red(finalPixels[k]);
            pixelColorEnd[1] = Color.green(finalPixels[k]);
            pixelColorEnd[2] = Color.blue(finalPixels[k]);



            newColor = Color.rgb((int)((1 - bmpToCreate)*pixelColorStart[0] + bmpToCreate*pixelColorEnd[0]),
                    (int)((1 - bmpToCreate)*pixelColorStart[1] + bmpToCreate*pixelColorEnd[1]),
                    (int)((1 - bmpToCreate)*pixelColorStart[2] + bmpToCreate*pixelColorEnd[2]));

            newPixels[k] = newColor;
            k++;
        }
        Bitmap newBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        newBitmap.setPixels(newPixels, 0, width, 0, 0, width, height);
        return newBitmap;
    }
}
