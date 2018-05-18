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
    // protected List<Bitmap> listFrames = new ArrayList<>();
    protected  Bitmap finalBmp;
    protected ProgressDialog mProgressDialog;
    public String fileManager;
    private int sample = 1;
    private Uri uriData;

    int scale=1;

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

        int [] pixelStart=null;
        Bitmap bmpStart=null;

        @Override
        protected String doInBackground(String... params) {int currentTime = 0;
            Bitmap bmpStart=null;
            int [] pixelStart=null;
            int indice=0;
            duration = mediaMetadataRetriever.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_DURATION);
            duration = String.valueOf(Integer.valueOf(duration) * 1000);
            while(currentTime < Integer.valueOf(duration)){
                Bitmap bmFrame = mediaMetadataRetriever.getFrameAtTime(currentTime,FFmpegMediaMetadataRetriever.OPTION_CLOSEST); //unit in microsecond
                if(currentTime==0)
                {
                    bmpStart=bmFrame;
                    pixelStart=new int[bmpStart.getWidth()*bmpStart.getHeight()];
                    //bmpStart.getPixels(pixelStart, 0, bmpStart.getWidth(), 0, 0,bmpStart.getWidth(),bmpStart.getHeight());

                }
                if(bmFrame!=null)
                {
                    //testMaxime(pixelStart,bmFrame,indice);
                    indice+=scale;
                    int [] valeur={bmFrame.getWidth(),bmFrame.getHeight()};
                    publishProgress(pixelStart,valeur);
                }
                //listFrames.add(bmFrame);
                System.out.println("coucou");
                Log.e("yo","yo");
                currentTime += intervalRefresh;
                System.out.println(currentTime);
            }
            if(bmpStart !=null) {
                Bitmap finalBmp=Bitmap.createBitmap(bmpStart.getWidth(), bmpStart.getHeight(), Bitmap.Config.ARGB_8888);
                finalBmp.setPixels(pixelStart, 0, bmpStart.getWidth(), 0, 0, bmpStart.getWidth(), bmpStart.getHeight());

                //listFrames.add(finalBmp);
            }
            return "Executed";}



        @Override
        protected void onPostExecute(String result) {

            //mProgressDialog.dismiss();

           /* ImageView imgView=findViewById(R.id.imageView);
            imgView.setImageBitmap(finalBmp);*/

            Log.e("fin","fin");
            Toast.makeText(FinalRenderActivity.this, "Traitement fini", Toast.LENGTH_SHORT).show();

        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected void onProgressUpdate(int[]... values) {
            int[] pixelStart=values[0];
            int[] valeur=values[1];
            int w=valeur[0];
            int h=valeur[1];
            Bitmap finalBmp=Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            finalBmp.setPixels(pixelStart, 0, w, 0, 0,w, h);
            mImageViewAnamorphosis.setImageBitmap(finalBmp);
        }

    }



}
