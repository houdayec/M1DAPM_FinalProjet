package dapm.g1.final_project.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dapm.g1.final_project.MainActivity;
import dapm.g1.final_project.PathUtil;
import dapm.g1.final_project.R;
import dapm.g1.final_project.VideoUtils;
import wseemann.media.FFmpegMediaMetadataRetriever;

public class Old_FinalRenderActivity extends AppCompatActivity {

    /**
     * Binding view elements
     */
    @BindView(R.id.imageViewAnamorphosis)
    ImageView mImageViewAnamorphosis;
    private int cpt=0;

    @BindView(R.id.downloadAnamorphosisButton)
    ImageButton mDownloadAnamorphosisButton;

    @BindView(R.id.shareAnamorphosisButton)
    ImageButton mShareAnamorphosisButton;

    @BindView(R.id.backToMenuButton)
    Button mBackToMenuButton;

    /**
     * Intern state
     */
    private String duration;
    public FFmpegMediaMetadataRetriever mediaMetadataRetriever;
    public double intervalRefresh;
    protected  Bitmap finalBmp;
    public String fileManager;
    private int sample = 1;
    private Uri uriData;
    private String direction;

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

        // Getting data from the previous activity

        uriData = Uri.parse(getIntent().getStringExtra("uri_video"));

        direction = getIntent().getStringExtra("direction");
        Log.e("direction",direction);
        fileManager = PathUtil.getPath(this, uriData);

        mediaMetadataRetriever = new FFmpegMediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(fileManager);

        int numberFrames = VideoUtils.getFrameRateVideo(fileManager);
        System.out.println("The video has a " + numberFrames + " frames / second");

        // Retrieving the first frame to set up the right settings

        Bitmap bmFrame = mediaMetadataRetriever.getFrameAtTime(0,FFmpegMediaMetadataRetriever.OPTION_CLOSEST); //unit in microsecond
        int stackPixels=1;
        if(direction.equals("Top") || direction.equals("Bottom"))
            stackPixels = bmFrame.getHeight();
        else if(direction.equals("Left") || direction.equals("Right"))
            stackPixels = bmFrame.getWidth();
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
     * Allows the user to save a completed anamorphosis
     * Butterknife OnClick methods
     */

    @OnClick(R.id.downloadAnamorphosisButton)
    void downloadAnamorphosis(){
        String storage = Environment.getExternalStorageDirectory().toString();
        File fileRepo = new File(storage + "/saved_anamorphosis");
        fileRepo.mkdirs();

        // Genere un entier aleatoire pour la sauvegarde
        Random fileId = new Random();
        int n = 1;
        n = fileId.nextInt();

        String fileName = "Anamorphosis-" + n + ".jpg";
        File newImg = new File(fileRepo, fileName);

        try{
            FileOutputStream fout = new FileOutputStream(newImg);
            finalBmp.compress(Bitmap.CompressFormat.JPEG, 90, fout);
            fout.flush();
            fout.close();
        }
        catch (Exception e){
            Log.e("Saving", "Saving anamophosis failed");
        }
        Toast.makeText(Old_FinalRenderActivity.this, "File saved:" + newImg.getAbsolutePath(), Toast.LENGTH_LONG).show();
    }

    @OnClick(R.id.shareAnamorphosisButton)
    void shareAnamorphosis(){
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/jpg");

        String bitmapPath = MediaStore.Images.Media.insertImage(getContentResolver(), finalBmp, "sharedAnamorphosis", null);
        Uri bitmapUri = Uri.parse(bitmapPath);

        // Will show every communication app that can share the picture
        intent.putExtra(Intent.EXTRA_STREAM, bitmapUri);
        startActivity(Intent.createChooser(intent, "Share !"));
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
    private class FramesExtraction extends AsyncTask<String, int[], String>{

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
                            //Bitmap bmpToWorkOn = bitmapInterpolation(bmFrame, bmpEnd, i);

                           // createAnamorphosis(bmpToWorkOn, pixelsArrayTemp, indice);
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
                }
                currentTime += intervalRefresh;
                System.out.println("currentTime : " + currentTime);
         }
         if(bmpStart != null) {
             finalBmp.setPixels(pixelsArrayTemp, 0, bmpStart.getWidth(), 0, 0, bmpStart.getWidth(), bmpStart.getHeight());
         }
            return "Executed";
        }




        @Override
        protected void onPostExecute(String result) {

            //mProgressDialog.dismiss();

            mImageViewAnamorphosis.setImageBitmap(finalBmp);

            Log.e("onPostExecute","reached");

            Toast.makeText(Old_FinalRenderActivity.this, "Traitement fini", Toast.LENGTH_SHORT).show();

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
                    cpt++;
                    Log.e("compteur",String.valueOf(cpt));
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

    /**
     * Method to generate new bitmaps if needed
     * @param firstBmp
     * @param lastBmp
     * @param bmpToCreate
     */

    public static Bitmap bitmapInterpolation(Bitmap firstBmp, Bitmap lastBmp, float bmpToCreate,int sample,int index){

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
        Log.e("sample" , String.valueOf(sample));
        Log.e("index" , String.valueOf(index));


        if(index < height ) {
            for (int k = 0; k < sample * width; k++) {
                if ((index * width) + k < width * height) {
                    pixelColorStart[0] = Color.red(initialPixels[index * width + k]);
                    pixelColorStart[1] = Color.green(initialPixels[index * width + k]);
                    pixelColorStart[2] = Color.blue(initialPixels[index * width + k]);

                    pixelColorEnd[0] = Color.red(finalPixels[index * width + k]);
                    pixelColorEnd[1] = Color.green(finalPixels[index * width + k]);
                    pixelColorEnd[2] = Color.blue(finalPixels[index * width + k]);


                    newColor = Color.rgb((int) ((1 - bmpToCreate) * pixelColorStart[0] + (bmpToCreate * pixelColorEnd[0])),
                            (int) ((1 - bmpToCreate) * pixelColorStart[1] + (bmpToCreate * pixelColorEnd[1])),
                            (int) ((1 - bmpToCreate) * pixelColorStart[2] + (bmpToCreate * pixelColorEnd[2])));


                    newPixels[index * width + k] = newColor;
                }
            }
        }

        Bitmap newBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        newBitmap.setPixels(newPixels, 0, width, 0, 0, width, height);
        return newBitmap;
    }

}
