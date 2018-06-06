package dapm.g1.final_project.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.SurfaceTexture;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.net.Uri;
import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Surface;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dapm.g1.final_project.CustomView.DrawingView;
import dapm.g1.final_project.Line;
import dapm.g1.final_project.MainActivity;
import dapm.g1.final_project.PPointF;
import dapm.g1.final_project.PathUtil;
import dapm.g1.final_project.R;
import dapm.g1.final_project.myMediaExtractor;

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

    private static final float ZERO = 1e-15f;
    private Uri uriData;
    private String fileManager;
    static int idFrame = 0;

    private static final String TAG = "ExtractMpegFramesTest";
    private static final boolean VERBOSE = false;           // lots of logging

    // where to find files (note: requires WRITE_EXTERNAL_STORAGE permission)
    private static final File FILES_DIR = Environment.getExternalStorageDirectory();
    private static String INPUT_FILE = "source.mp4";
    private static final int MAX_FRAMES = 100000000;       // stop extracting after this many

    static String direction = "Top";
    static int sample = 1;
    public static int indexRangePixels = 0;

    private static int[] finalPixels;
    static int mWidth;
    static int mHeight;

    protected static Bitmap finalBmp;
    protected static Canvas canvas;
    static Bitmap bmp;
    static Bitmap bmp2 = null;
    static Bitmap interpolatedBmp;

    // Interpolation vars
    static boolean interpolate = false;
    static int interpolationSample;

    static boolean jumpFrame = false;
    static ArrayList<String> frameSelected = new ArrayList<>();

    private static PPointF[] tabPointsPath;
    private static Path pointsPath;
    private static Paint pointsPaint = DrawingView.customPaint(Color.GREEN,5);
    private static Path tangentesPath;
    private static Paint tangentesPaint = DrawingView.customPaint(Color.rgb(255,0,150),5);
    private static Path tangentes2Path;
    private static Paint tangentes2Paint = DrawingView.customPaint(Color.rgb(200,200,255),5);
    private static Path selectPath;
    private static Paint selectPaint = DrawingView.customPaint(Color.rgb(255,200,0),1);
    private static Path ptPath;
    private static Paint ptPaint = DrawingView.customPaint(Color.rgb(50,50,255),10);
    private static int cursor;
    private static Line saveLine;
    private static int start;
    private static int end;

    private int duration;
    int stackPixels;
    int frameRate;

    private FramesExtraction mFramesExtractionTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Supporting toolbar
        setContentView(R.layout.activity_final_render);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Binding view
        ButterKnife.bind(this);

        // Getting data from previous activity
        direction = getIntent().getStringExtra("direction");
        if(direction.equals("Custom")){
            ArrayList<PPointF> lpoints = getIntent().getParcelableArrayListExtra("drawing");
            tabPointsPath = new PPointF[lpoints.size()];
            lpoints.toArray(tabPointsPath);
            start = getIntent().getIntExtra("start",0);
            end = getIntent().getIntExtra("end",0);
            cursor = 0;
            saveLine = null;
            Log.d(TAG, "size path : " + tabPointsPath.length);
        }

        // Retrieving data and transform to uri
        uriData = Uri.parse(getIntent().getStringExtra("uri_video"));
        fileManager = PathUtil.getPath(this, uriData);
        INPUT_FILE = fileManager.toString();

        // Initializing variables
        stackPixels = 1;
        frameRate = 1;

        // Resetting variable to make new anamorphosis
        indexRangePixels = 0;

        // Setting up the view
        lockUI(true);

        // Starting extraction

        mFramesExtractionTask = new FramesExtraction();
        mFramesExtractionTask.execute();
    }

    /**
     * Allows the user to save a completed anamorphosis
     * Butterknife OnClick methods
     */

    @OnClick(R.id.downloadAnamorphosisButton)
    void downloadAnamorphosis() {
        String storage = Environment.getExternalStorageDirectory().toString();
        File fileRepo = new File(storage + "/saved_anamorphosis");
        fileRepo.mkdirs();

        // Genere un entier aleatoire pour la sauvegarde
        Random fileId = new Random();
        int n = 1;
        n = fileId.nextInt();

        String fileName = "Anamorphosis-" + n + ".jpg";
        File newImg = new File(fileRepo, fileName);

        try {
            FileOutputStream fout = new FileOutputStream(newImg);
            finalBmp.compress(Bitmap.CompressFormat.JPEG, 90, fout);
            fout.flush();
            fout.close();
        } catch (Exception e) {
            Log.e("Saving", "Saving anamophosis failed");
        }

        Toast.makeText(FinalRenderActivity.this, "File saved:" + newImg.getAbsolutePath(), Toast.LENGTH_LONG).show();
    }

    /**
     * Method to share the final anamorphosis
     */
    @OnClick(R.id.shareAnamorphosisButton)
    void shareAnamorphosis() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/jpg");

        String bitmapPath = MediaStore.Images.Media.insertImage(getContentResolver(), finalBmp, "sharedAnamorphosis", null);
        Uri bitmapUri = Uri.parse(bitmapPath);

        // Will show every communication app that can share the picture
        intent.putExtra(Intent.EXTRA_STREAM, bitmapUri);
        startActivity(Intent.createChooser(intent, "Share the anamorphosis !"));
    }

    /**
     * Method to go back to the main menu
     */
    @OnClick(R.id.backToMenuButton)
    void backToMenu() {
        Intent goToMainMenuIntent = new Intent(this, MainActivity.class);
        startActivity(goToMainMenuIntent);
    }

    /**
     * Inner class (Asynchronous task) to get all the frames from a video
     */
    @SuppressLint("NewApi")
    private class FramesExtraction extends AsyncTask<String, int[], String> {

        @Override
        protected String doInBackground(String... params) {

            try {
                extractMpegFrames(INPUT_FILE);
            } catch (IOException | myMediaExtractor.NoTrackSelectedException e) {
                e.printStackTrace();
            }

            return "Done";
        }


        @Override
        protected void onPostExecute(String result) {

            //mProgressDialog.dismiss();
            //finalBmp.setPixels(finalPixels, 0, mWidth, 0, 0,mWidth, mHeight);
            mImageViewAnamorphosis.setImageBitmap(finalBmp);

            Log.e("onPostExecute", "reached");

            Toast.makeText(FinalRenderActivity.this, "Traitement fini", Toast.LENGTH_SHORT).show();

            lockUI(false);
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected void onProgressUpdate(int[]... pixels) {
            //finalBmp.setPixels(pixels[0], 0, mWidth, 0, 0, mWidth, mHeight);
            if (direction.equals("Custom")) {
                /*canvas = new Canvas(finalBmp);
                canvas.drawColor(Color.WHITE);
                canvas.drawPath(selectPath, selectPaint);
                canvas.drawPath(tangentesPath, tangentesPaint);
                canvas.drawPath(tangentes2Path, tangentes2Paint);
                canvas.drawPath(pointsPath, pointsPaint);
                canvas.drawPath(ptPath,ptPaint);*/
                finalBmp.setPixels(pixels[0], 0, mWidth, 0, 0, mWidth, mHeight);
                canvas = new Canvas(finalBmp);
                canvas.drawPath(tangentesPath, tangentesPaint);
            }
            else {
                finalBmp.setPixels(pixels[0], 0, mWidth, 0, 0, mWidth, mHeight);
            }
            mImageViewAnamorphosis.setImageBitmap(finalBmp);
        }

        /**
         * CUSTOM METHODS
         */

        /**
         * Tests extraction from an MP4 to a series of PNG files.
         * <p>
         * We scale the video to 640x480 for the PNG just to demonstrate that we can scale the
         * video with the GPU.  If the input video has a different aspect ratio, we could preserve
         * it by adjusting the GL viewport to get letterboxing or pillarboxing, but generally if
         * you're extracting frames you don't want black bars.
         */
        private void extractMpegFrames(String INPUT_FILE) throws IOException, myMediaExtractor.NoTrackSelectedException {
            MediaCodec decoder = null;
            FinalRenderActivity.CodecOutputSurface outputSurface = null;
            myMediaExtractor extractor = null;
            //int saveWidth = mWidth; //640;
            //  int saveHeight = mHeight;//480;
            //  Log.e("largeur ",String.valueOf(mWidth));
            //  Log.e("hauteur ",String.valueOf(mHeight));

            try {
                File inputFile = new File(INPUT_FILE);   // must be an absolute path
                System.out.println("extractMpegFrames file : " + INPUT_FILE);
                // The MediaExtractor error messages aren't very useful.  Check to see if the input
                // file exists so we can throw a better one if it's not there.
                if (!inputFile.canRead()) {
                    throw new FileNotFoundException("Unable to read " + inputFile);
                }

                extractor = new myMediaExtractor(inputFile.toString());
                int trackIndex = extractor.getTrackVideoIndex();
                if (trackIndex < 0) {
                    throw new RuntimeException("No video track found in " + inputFile);
                }
                extractor.selectTrack(trackIndex);

                // Getting data from video source
                mWidth = extractor.getVideoWidth();
                mHeight = extractor.getVideoHeight();

                if (!direction.equals("Custom")) {
                    if (direction.equals("Top") || direction.equals("Bottom"))
                        stackPixels = mHeight;
                    else if (direction.equals("Left") || direction.equals("Right"))
                        stackPixels = mWidth;

                    duration = (int) extractor.getVideoDuration() / 1000000; //micro-second
                    frameRate = extractor.getVideoFrameRate();

                    Log.e(TAG, "Video size is " + mWidth + "x" + mHeight);
                    Log.e(TAG, "Duration is " + duration);
                    Log.e(TAG, "Frame rate is " + frameRate);

                    sample = (int) ((stackPixels) / ((duration) * frameRate));
                    Log.e(TAG, "Sample is " + sample);
                    //Need to interpolate
                    if (sample >= 3) {
                        interpolate = true;
                        // Nombre d'images a créer entre chaque images
                        interpolationSample = sample - 1;
                        sample = 1;

                        Log.e("Interpolate", "Interpolation needed");
                    }
                    //Need to jump frame
                    else if (sample < 1) {
                        Log.e("Jump", "jump frame needed");
                        jumpFrame = true;
                        sample = 1;
                        selectFrame(frameRate, duration, stackPixels);
                    }
                    System.out.println("start scale " + sample);
                }

                // Could use width/height from the MediaFormat to get full-size frames.
                outputSurface = new FinalRenderActivity.CodecOutputSurface(mWidth, mHeight);

                // Create a MediaCodec decoder, and configure it with the MediaFormat from the
                // extractor.  It's very important to use the format from the extractor because
                // it contains a copy of the CSD-0/CSD-1 codec-specific data chunks.
                String mime = extractor.getVideoMine();
                decoder = MediaCodec.createDecoderByType(mime);
                decoder.configure(extractor.getFormat(), outputSurface.getSurface(), null, 0);
                decoder.start();

                doExtract(extractor, trackIndex, decoder, outputSurface);
            } finally {
                // release everything we grabbed
                if (outputSurface != null) {
                    outputSurface.release();
                    outputSurface = null;
                }
                if (decoder != null) {
                    decoder.stop();
                    decoder.release();
                    decoder = null;
                }
                if (extractor != null) {
                    extractor.release();
                    extractor = null;
                }
            }
        }

        /**
         * Work loop.
         */
        void doExtract(myMediaExtractor extractor, int trackIndex, MediaCodec decoder,
                       FinalRenderActivity.CodecOutputSurface outputSurface) throws IOException {
            final int TIMEOUT_USEC = 10000;
            ByteBuffer[] decoderInputBuffers = decoder.getInputBuffers();
            MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
            int inputChunk = 0;
            int decodeCount = 0;
            long frameSaveTime = 0;

            boolean outputDone = false;
            boolean inputDone = false;
            while (!outputDone && !isCancelled()) {
                if (VERBOSE) Log.d(TAG, "loop");

                // Feed more data to the decoder.
                if (!inputDone) {
                    int inputBufIndex = decoder.dequeueInputBuffer(TIMEOUT_USEC);
                    if (inputBufIndex >= 0) {
                        ByteBuffer inputBuf = decoderInputBuffers[inputBufIndex];
                        // Read the sample data into the ByteBuffer.  This neither respects nor
                        // updates inputBuf's position, limit, etc.
                        int chunkSize = extractor.readSampleData(inputBuf, 0);
                        if (chunkSize < 0) {
                            // End of stream -- send empty frame with EOS flag set.
                            decoder.queueInputBuffer(inputBufIndex, 0, 0, 0L,
                                    MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                            inputDone = true;
                            if (VERBOSE) Log.d(TAG, "sent input EOS");
                        } else {
                            if (extractor.getSampleTrackIndex() != trackIndex) {
                                Log.w(TAG, "WEIRD: got sample from track " +
                                        extractor.getSampleTrackIndex() + ", expected " + trackIndex);
                            }
                            long presentationTimeUs = extractor.getSampleTime();
                            decoder.queueInputBuffer(inputBufIndex, 0, chunkSize,
                                    presentationTimeUs, 0 /*flags*/);
                            if (VERBOSE) {
                                Log.d(TAG, "submitted frame " + inputChunk + " to dec, size=" +
                                        chunkSize);
                            }
                            inputChunk++;
                            extractor.advance();
                        }
                    } else {
                        if (VERBOSE) Log.d(TAG, "input buffer not available");
                    }
                }

                if (!outputDone && !isCancelled()) {
                    int decoderStatus = decoder.dequeueOutputBuffer(info, TIMEOUT_USEC);
                    if (decoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                        // no output available yet
                        if (VERBOSE) Log.d(TAG, "no output from decoder available");
                    } else if (decoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                        // not important for us, since we're using Surface
                        if (VERBOSE) Log.d(TAG, "decoder output buffers changed");
                    } else if (decoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                        MediaFormat newFormat = decoder.getOutputFormat();
                        if (VERBOSE) Log.d(TAG, "decoder output format changed: " + newFormat);
                    } else { // decoderStatus >= 0
                        if (VERBOSE) Log.d(TAG, "surface decoder given buffer " + decoderStatus +
                                " (size=" + info.size + ")");
                        if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                            if (VERBOSE) Log.d(TAG, "output EOS");
                            outputDone = true;
                        }

                        boolean doRender = (info.size != 0);

                        // As soon as we call releaseOutputBuffer, the buffer will be forwarded
                        // to SurfaceTexture to convert to a texture.  The API doesn't guarantee
                        // that the texture will be available before the call returns, so we
                        // need to wait for the onFrameAvailable callback to fire.
                        decoder.releaseOutputBuffer(decoderStatus, doRender);
                        if (doRender) {
                            if (VERBOSE) Log.d(TAG, "awaiting decode of frame " + decodeCount);
                            outputSurface.awaitNewImage();
                            outputSurface.drawImage(true);

                            if (decodeCount < MAX_FRAMES) {
                                long startWhen = System.nanoTime();
                                if (direction.equals("Custom")) outputSurface.saveCustomFrame();
                                else outputSurface.saveFrame();
                                publishProgress(finalPixels);
                                //System.out.println("ISCANCELED "+isCancelled());
                                frameSaveTime += System.nanoTime() - startWhen;
                                /*try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }*/
                            }
                            decodeCount++;
                        }
                    }
                }
            }

            int numSaved = (MAX_FRAMES < decodeCount) ? MAX_FRAMES : decodeCount;
            Log.d(TAG, "Saving " + numSaved + " frames took " +
                    (frameSaveTime / numSaved / 1000) + " us per frame");
        }

    }

    /**
     * Method to lock/unlock the UI
     * @param isUIlocked
     */
    public void lockUI(boolean isUIlocked){
        if(isUIlocked){
            mDownloadAnamorphosisButton.setClickable(false);
            mDownloadAnamorphosisButton.setAlpha(.5f);
            mShareAnamorphosisButton.setClickable(false);
            mShareAnamorphosisButton.setAlpha(.5f);
            mBackToMenuButton.setClickable(false);
            mBackToMenuButton.setAlpha(.5f);
        }else{
            mDownloadAnamorphosisButton.setClickable(true);
            mDownloadAnamorphosisButton.setAlpha(1f);
            mShareAnamorphosisButton.setClickable(true);
            mShareAnamorphosisButton.setAlpha(1f);
            mBackToMenuButton.setClickable(true);
            mBackToMenuButton.setAlpha(1f);
        }
    }

    /**
     * Method to create a custom anamorphosis
     * @param currentBmp
     * @param pixels
     */
    private static void createCustomAnamorphosis(Bitmap currentBmp, int[] pixels){
        cursor++;
        if (VERBOSE){
            Log.d(TAG,"createCustomAnamorphosis");
            Log.d(TAG,"path size "+tabPointsPath.length);
            Log.d(TAG,"custom frame " + cursor);
        }
        PPointF pt;
        if (cursor == 1) {
            if (VERBOSE) {
                Log.d(TAG, "init cur");
            }
            pointsPath = new Path();
            tangentesPath = new Path();
            tangentes2Path = new Path();
            ptPath = new Path();
            selectPath = new Path();
            pt = tabPointsPath[0];
            pointsPath.moveTo(pt.x, pt.y);
        }
        if (cursor+1<tabPointsPath.length) {
            pt = tabPointsPath[cursor];
            pointsPath.lineTo(pt.x, pt.y);
            calcTangenteP(tabPointsPath[cursor - 1], tabPointsPath[cursor], tabPointsPath[cursor + 1], (start==1 || start==2),currentBmp,pixels);
        }
        else {
            calcSelectPixels(saveLine,null,currentBmp,pixels);
        }
    }

    private static void calcSelectPixels(Line line1, Line line2, Bitmap bmp, int[] pixels){
        tangentesPath.reset();
        tangentes2Path.reset();
        if (line2 != null) {
            tangentesPath.moveTo(line2.getP1().x, line2.getP1().y);
            tangentesPath.lineTo(line2.getP2().x, line2.getP2().y);
        }
        if (line1 != null) {
            tangentes2Path.moveTo(line1.getP1().x, line1.getP1().y);
            tangentes2Path.lineTo(line1.getP2().x, line1.getP2().y);
        }
        selectPath.reset();
        ptPath.reset();
        int[] currentPixel = new int[mHeight * mWidth];
        bmp.getPixels(currentPixel, 0, mWidth, 0, 0, mWidth, mHeight);
        System.out.println("SELECT "+cursor+" "+((line1!=null)?line1.getAt():"null") +" "+((line2!=null)?line2.getAt():"null"));
        int xmin = Math.round(getMinMax(line1,line2,false,0));
        int xmax = Math.round(getMinMax(line1,line2,true,0));
        int ymin = Math.round(getMinMax(line1,line2,false,1));
        int ymax = Math.round(getMinMax(line1,line2,true,1));
        float x1,x2,y1,y2;
        if (line1!=null && line2!=null) {
            ptPath.addCircle(line1.getP1().x, line1.getP1().y, 15, Path.Direction.CW);
            ptPath.addCircle(line2.getP1().x, line2.getP1().y, 15, Path.Direction.CW);
            if (line2.getAt()<=1 && line2.getAt()>=-1) {
                for (int x = xmin; x < xmax; x++) {
                    y1 = ((y1=line2.calcY(x))<0)? 0 : (y1>mHeight)? mHeight : y1;
                    y2 = ((y2=line1.calcY(x))<0)? 0 : (y2>mHeight)? mHeight : y2;
                    if (y2<y1) {
                        float ys = y1;
                        y1 = y2;
                        y2 = ys;
                    }
                    selectPath.moveTo(x, y1);
                    selectPath.lineTo(x, y2);
                    for (int y = Math.round(y1); y < y2; y++){
                        int id = y*mWidth+x;
                        if (pixels[id]==0)
                            pixels[id] = currentPixel[id];
                    }
                }
            } else {
                for (int y = ymin; y < ymax; y++) {
                    x1 = ((x1=line2.calcX(y))<0)? 0 : (x1>mWidth)? mWidth : x1;
                    x2 = ((x2=line1.calcX(y))<0)? 0 : (x2>mWidth)? mWidth : x2;
                    if (x2<x1) {
                        float xs = x1;
                        x1 = x2;
                        x2 = xs;
                    }
                    selectPath.moveTo(x1, y);
                    selectPath.lineTo(x2, y);
                    for (int x = Math.round(x1); x < x2; x++){
                        int id = y*mWidth+x;
                        if (pixels[id]==0)
                            pixels[id] = currentPixel[id];
                    }
                }
            }
        }
        else if(line1==null && line2!=null){
            ptPath.addCircle(line2.getP1().x, line2.getP1().y, 15, Path.Direction.CW);
            if (start >=6 && start <=8) {
                for (int y = ymin; y < ymax; y++) {
                    x1 = 0;
                    x2 = line2.calcX(y);
                    selectPath.moveTo(x1, y);
                    selectPath.lineTo(x2, y);
                    for (int x = Math.round(x1); x < x2; x++){
                        int id = y*mWidth+x;
                        if (pixels[id]==0)
                            pixels[id] = currentPixel[id];
                    }
                }
            } else if (start >=3 && start <=5) {
                for (int y = ymin; y < ymax; y++) {
                    x1 = line2.calcX(y);
                    x2 = mWidth;
                    selectPath.moveTo(x1, y);
                    selectPath.lineTo(x2, y);
                    for (int x = Math.round(x1); x < x2; x++){
                        int id = y*mWidth+x;
                        if (pixels[id]==0)
                            pixels[id] = currentPixel[id];
                    }
                }
            } else if (start ==1) {
                for (int x = xmin; x < xmax; x++) {
                    y1 = line2.calcY(x);
                    y2 = mHeight;
                    selectPath.moveTo(x, y1);
                    selectPath.lineTo(x, y2);
                    for (int y = Math.round(y1); y < y2; y++){
                        int id = y*mWidth+x;
                        if (pixels[id]==0)
                            pixels[id] = currentPixel[id];
                    }
                }
            } else if (start ==2) {
                for (int x = xmin; x < xmax; x++) {
                    y1 = 0;
                    y2 = line2.calcY(x);
                    selectPath.moveTo(x, y1);
                    selectPath.lineTo(x, y2);
                    for (int y = Math.round(y1); y < y2; y++){
                        int id = y*mWidth+x;
                        if (pixels[id]==0)
                            pixels[id] = currentPixel[id];
                    }
                }
            }
        }
        else if(line1!=null && line2==null){
            ptPath.addCircle(line1.getP1().x, line1.getP1().y, 15, Path.Direction.CW);
            if (end >=6 && end <=8) {
                for (int y = 0; y < mHeight; y++) {
                    x1 = 0;
                    x2 = line1.calcX(y);
                    selectPath.moveTo(x1, y);
                    selectPath.lineTo(x2, y);
                    for (int x = Math.round(x1); x < x2; x++){
                        int id = y*mWidth+x;
                        if (pixels[id]==0)
                            pixels[id] = currentPixel[id];
                    }
                }
            } else if (end >=3 && end <=5) {
                for (int y = 0; y < mHeight; y++) {
                    x1 = line1.calcX(y);
                    x2 = mWidth;
                    selectPath.moveTo(x1, y);
                    selectPath.lineTo(x2, y);
                    for (int x = Math.round(x1); x < x2; x++){
                        int id = y*mWidth+x;
                        if (pixels[id]==0)
                            pixels[id] = currentPixel[id];
                    }
                }
            } else if (end ==1) {
                for (int x = 0; x < mWidth; x++) {
                    y1 = line1.calcY(x);
                    y2 = mHeight;
                    selectPath.moveTo(x, y1);
                    selectPath.lineTo(x, y2);
                    for (int y = Math.round(y1); y < y2; y++){
                        int id = y*mWidth+x;
                        if (pixels[id]==0)
                            pixels[id] = currentPixel[id];
                    }
                }
            } else if (end ==2) {
                for (int x = 0; x < mWidth; x++) {
                    y1 = 0;
                    y2 = line1.calcY(x);
                    selectPath.moveTo(x, y1);
                    selectPath.lineTo(x, y2);
                    for (int y = Math.round(y1); y < y2; y++){
                        int id = y*mWidth+x;
                        if (pixels[id]==0)
                            pixels[id] = currentPixel[id];
                    }
                }
            }
        }
    }

    private static float getMinMax(Line line1, Line line2, boolean type, int axe) {
        if (type) {
            return Math.max((line1!=null)?Math.max(line1.getP1().get(axe),line1.getP2().get(axe)):0,(line2!=null)?Math.max(line2.getP1().get(axe),line2.getP2().get(axe)):0);
        }
        return Math.min((line1!=null)?Math.min(line1.getP1().get(axe),line1.getP2().get(axe)):Float.MAX_VALUE,(line2!=null)?Math.min(line2.getP1().get(axe),line2.getP2().get(axe)):Float.MAX_VALUE);
    }

    private static PPointF calcTangExtremum(float a, float b, float at, int yval, boolean invert) {
        float xmax = (invert)? mHeight : mWidth;
        float x = 0;
        float y = 0;

        if (at != 0) {
            x = Line.calcX(yval, a, b, at);
            if (x < 0) {
                x = 0;
                y = Line.calcY(x, a, b, at);
            } else if (x > xmax) {
                x = xmax;
                y = Line.calcY(x, a, b, at);
            } else
                y = yval;
        }
        else {
            x = (yval==0)? 0 : xmax;
            y = b;
        }
        return (invert)? new PPointF(y, x):new PPointF(x, y);
    }

    private static void calcTangenteP(PPointF p1,PPointF p2, PPointF p3, boolean invert, Bitmap currentBmp, int[] pixels) {
        if (!invert && ((int)Math.abs(p1.x-p2.x)==0 || (int)Math.abs(p1.x-p3.x)==0 || (int)Math.abs(p2.x-p3.x)==0)) {
            calcTangenteP(p1, p2, p3, true,currentBmp,pixels);
            return;
        }

        if (invert) {
            p1 = p1.getInvert();
            p2 = p2.getInvert();
            p3 = p3.getInvert();
        }

        float A1 = -(p1.x*p1.x)+p2.x*p2.x;
        float B1 = (-p1.x) + (p2.x);
        float C1 = (-p1.y) + (p2.y);

        float A2 = -(p2.x*p2.x)+p3.x*p3.x;
        float B2 = (-p2.x) + (p3.x);
        float C2 = (-p2.y) + (p3.y);

        float Bmul = -(B2 / B1);

        float A3 = Bmul * A1 + A2;
        float C3 = Bmul * C1 + C2;

        float a = C3 / A3;
        float b = (C1 - A1 * a) / B1;
        float c = p1.y - a * (p1.x*p1.x) - b * p1.x;

        if (VERBOSE) {
            Log.d(TAG, "f(x)=" + a + "x²+" + b + "x+" + c);
            Log.d(TAG,String.valueOf(p2));
            Log.d(TAG,"f'(x)=" + (a * 2) + "x+" + b);
        }

        float at = (2 * a * p2.x + b);
        if (VERBOSE)
            Log.d(TAG,"tangente:y="+at+"*(x-"+p2.x+")+"+p2.y);

        PPointF pt1 = calcTangExtremum(p2.x, p2.y, at, 0, invert);
        PPointF pt2 = calcTangExtremum(p2.x, p2.y, at, (invert)? mWidth : mHeight, invert);
        if (VERBOSE)
            Log.d(TAG,pt1+" "+pt2);

        float atp = (at!=0)? -(1 / at) : 0;

        if (VERBOSE)
            Log.d(TAG,"tangente perpen:y="+atp+"*(x)+"+p2.y);
        PPointF ptp1 = (atp != 0)? calcTangExtremum(p2.x, p2.y, atp, 0, invert) : ((invert)? new PPointF(0, p2.x) : new PPointF(p2.x, 0));
        PPointF ptp2 = (atp != 0)? calcTangExtremum(p2.x, p2.y, atp, (invert)? mWidth : mHeight, invert) : ((invert)? new PPointF(mWidth, p2.x) : new PPointF(p2.x, mHeight));
        if (VERBOSE)
            Log.d(TAG,ptp1+" "+ptp2);

        Line lastLine = new Line(ptp1,ptp2);
        calcSelectPixels(saveLine, lastLine, currentBmp, pixels);
        saveLine = lastLine.copy();
    }


    /**
     * Method to create an anamorphosis
     *
     * @param currentBmp
     * @param pixels
     * @param index
     */
    public static void createAnamorphosis(Bitmap currentBmp, int[] pixels, int index) {
        if (VERBOSE)
            Log.d(TAG,"createAnamorphosis");
        int height = mHeight;//currentBmp.getHeight();
        int width =mWidth ; //currentBmp.getWidth();
        int currentPixel[] = new int[height * width];
        currentBmp.getPixels(currentPixel, 0, width, 0, 0, width, height);
        switch (direction) {
            case "Top":
                if (index < height) {
                    for (int j = 0; j < sample * width; j++) {
                        if ((index * width) + j < width * height) {
                            pixels[index * width + j] = currentPixel[index * width + j];
                        }
                    }
                }
                break;
            case "Bottom":
                if (index < height) {
                    int indexTmp = (height - 1) - index;
                    for (int j = (height - 1); j > (height - 1) - (sample * width); j--) {
                        if ((indexTmp * width) + j < width * height) {
                            pixels[indexTmp * width + j] = currentPixel[indexTmp * width + j];
                        }
                    }
                }
                break;
            case "Left":
                if (index < width) {
                    for (int k = 0; k < sample; k++) {
                        for (int i = 0; i < height; i++) {
                            if ((i * width) + index + k < width * height)
                                pixels[i * width + (index + k)] = currentPixel[i * width + (index + k)];
                        }
                    }
                }
                break;

            case "Right":
                if (index < width) {
                    int indexTmp = (width - 1) - index;
                    for (int k = 0; k < sample; k++) {
                        for (int i = 0; i < height; i++) {
                            if ((i * width) + indexTmp - k > 0)
                                pixels[i * width + (indexTmp - k)] = currentPixel[i * width + (indexTmp - k)];
                        }
                    }
                }
                break;
            default:
                if (VERBOSE)
                    Log.e(TAG,"Error on the direction");
                Log.e("error", "errror");
                break;
        }

    }

    /**
     * Holds state associated with a Surface used for MediaCodec decoder output.
     * <p>
     * The constructor for this class will prepare GL, create a SurfaceTexture,
     * and then create a Surface for that SurfaceTexture.  The Surface can be passed to
     * MediaCodec.configure() to receive decoder output.  When a frame arrives, we latch the
     * texture with updateTexImage(), then render the texture with GL to a pbuffer.
     * <p>
     * By default, the Surface will be using a BufferQueue in asynchronous mode, so we
     * can potentially drop frames.
     */
    private static class CodecOutputSurface
            implements SurfaceTexture.OnFrameAvailableListener {
        private FinalRenderActivity.STextureRender mTextureRender;
        private SurfaceTexture mSurfaceTexture;
        private Surface mSurface;

        private EGLDisplay mEGLDisplay = EGL14.EGL_NO_DISPLAY;
        private EGLContext mEGLContext = EGL14.EGL_NO_CONTEXT;
        private EGLSurface mEGLSurface = EGL14.EGL_NO_SURFACE;


        private Object mFrameSyncObject = new Object();     // guards mFrameAvailable
        private boolean mFrameAvailable;

        private ByteBuffer mPixelBuf;                       // used by saveFrame()

        /**
         * Creates a CodecOutputSurface backed by a pbuffer with the specified dimensions.  The
         * new EGL context and surface will be made current.  Creates a Surface that can be passed
         * to MediaCodec.configure().
         */
        public CodecOutputSurface(int width, int height) {
            if (width <= 0 || height <= 0) {
                throw new IllegalArgumentException();
            }
            //   mWidth = width;
            //  mHeight = height;
            finalPixels = new int[mWidth * mHeight];
            finalBmp = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
            bmp = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);

            eglSetup();
            makeCurrent();
            setup();
        }

        /**
         * Creates interconnected instances of TextureRender, SurfaceTexture, and Surface.
         */
        private void setup() {
            mTextureRender = new FinalRenderActivity.STextureRender();
            mTextureRender.surfaceCreated();

            if (VERBOSE) Log.d(TAG, "textureID=" + mTextureRender.getTextureId());
            mSurfaceTexture = new SurfaceTexture(mTextureRender.getTextureId());

            // This doesn't work if this object is created on the thread that CTS started for
            // these test cases.
            //
            // The CTS-created thread has a Looper, and the SurfaceTexture constructor will
            // create a Handler that uses it.  The "frame available" message is delivered
            // there, but since we're not a Looper-based thread we'll never see it.  For
            // this to do anything useful, CodecOutputSurface must be created on a thread without
            // a Looper, so that SurfaceTexture uses the main application Looper instead.
            //
            // Java language note: passing "this" out of a constructor is generally unwise,
            // but we should be able to get away with it here.
            mSurfaceTexture.setOnFrameAvailableListener(this);

            mSurface = new Surface(mSurfaceTexture);

            mPixelBuf = ByteBuffer.allocateDirect(mWidth * mHeight * 4);
            mPixelBuf.order(ByteOrder.LITTLE_ENDIAN);
        }

        /**
         * Prepares EGL.  We want a GLES 2.0 context and a surface that supports pbuffer.
         */
        private void eglSetup() {
            mEGLDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
            if (mEGLDisplay == EGL14.EGL_NO_DISPLAY) {
                throw new RuntimeException("unable to get EGL14 display");
            }
            int[] version = new int[2];
            if (!EGL14.eglInitialize(mEGLDisplay, version, 0, version, 1)) {
                mEGLDisplay = null;
                throw new RuntimeException("unable to initialize EGL14");
            }

            // Configure EGL for pbuffer and OpenGL ES 2.0, 24-bit RGB.
            int[] attribList = {
                    EGL14.EGL_RED_SIZE, 8,
                    EGL14.EGL_GREEN_SIZE, 8,
                    EGL14.EGL_BLUE_SIZE, 8,
                    EGL14.EGL_ALPHA_SIZE, 8,
                    EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
                    EGL14.EGL_SURFACE_TYPE, EGL14.EGL_PBUFFER_BIT,
                    EGL14.EGL_NONE
            };
            EGLConfig[] configs = new EGLConfig[1];
            int[] numConfigs = new int[1];
            if (!EGL14.eglChooseConfig(mEGLDisplay, attribList, 0, configs, 0, configs.length,
                    numConfigs, 0)) {
                throw new RuntimeException("unable to find RGB888+recordable ES2 EGL config");
            }

            // Configure context for OpenGL ES 2.0.
            int[] attrib_list = {
                    EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
                    EGL14.EGL_NONE
            };
            mEGLContext = EGL14.eglCreateContext(mEGLDisplay, configs[0], EGL14.EGL_NO_CONTEXT,
                    attrib_list, 0);
            checkEglError("eglCreateContext");
            if (mEGLContext == null) {
                throw new RuntimeException("null context");
            }

            // Create a pbuffer surface.
            int[] surfaceAttribs = {
                    EGL14.EGL_WIDTH, mWidth,
                    EGL14.EGL_HEIGHT, mHeight,
                    EGL14.EGL_NONE
            };
            mEGLSurface = EGL14.eglCreatePbufferSurface(mEGLDisplay, configs[0], surfaceAttribs, 0);
            checkEglError("eglCreatePbufferSurface");
            if (mEGLSurface == null) {
                throw new RuntimeException("surface was null");
            }
        }

        /**
         * Discard all resources held by this class, notably the EGL context.
         */
        public void release() {
            if (mEGLDisplay != EGL14.EGL_NO_DISPLAY) {
                EGL14.eglDestroySurface(mEGLDisplay, mEGLSurface);
                EGL14.eglDestroyContext(mEGLDisplay, mEGLContext);
                EGL14.eglReleaseThread();
                EGL14.eglTerminate(mEGLDisplay);
            }
            mEGLDisplay = EGL14.EGL_NO_DISPLAY;
            mEGLContext = EGL14.EGL_NO_CONTEXT;
            mEGLSurface = EGL14.EGL_NO_SURFACE;

            mSurface.release();

            // this causes a bunch of warnings that appear harmless but might confuse someone:
            //  W BufferQueue: [unnamed-3997-2] cancelBuffer: BufferQueue has been abandoned!
            //mSurfaceTexture.release();

            mTextureRender = null;
            mSurface = null;
            mSurfaceTexture = null;
        }

        /**
         * Makes our EGL context and surface current.
         */
        public void makeCurrent() {
            if (!EGL14.eglMakeCurrent(mEGLDisplay, mEGLSurface, mEGLSurface, mEGLContext)) {
                throw new RuntimeException("eglMakeCurrent failed");
            }
        }

        /**
         * Returns the Surface.
         */
        public Surface getSurface() {
            return mSurface;
        }

        /**
         * Latches the next buffer into the texture.  Must be called from the thread that created
         * the CodecOutputSurface object.  (More specifically, it must be called on the thread
         * with the EGLContext that contains the GL texture object used by SurfaceTexture.)
         */
        public void awaitNewImage() {
            final int TIMEOUT_MS = 2500;

            synchronized (mFrameSyncObject) {
                while (!mFrameAvailable) {
                    try {
                        // Wait for onFrameAvailable() to signal us.  Use a timeout to avoid
                        // stalling the test if it doesn't arrive.
                        mFrameSyncObject.wait(TIMEOUT_MS);
                        if (!mFrameAvailable) {
                            // TODO: if "spurious wakeup", continue while loop
                            throw new RuntimeException("frame wait timed out");
                        }
                    } catch (InterruptedException ie) {
                        // shouldn't happen
                        throw new RuntimeException(ie);
                    }
                }
                mFrameAvailable = false;
            }

            // Latch the data.
            mTextureRender.checkGlError("before updateTexImage");
            mSurfaceTexture.updateTexImage();
        }

        /**
         * Draws the data from SurfaceTexture onto the current EGL surface.
         *
         * @param invert if set, render the image with Y inverted (0,0 in top left)
         */
        public void drawImage(boolean invert) {
            mTextureRender.drawFrame(mSurfaceTexture, invert);
        }

        // SurfaceTexture callback
        @Override
        public void onFrameAvailable(SurfaceTexture st) {
            if (VERBOSE) Log.d(TAG, "new frame available");
            synchronized (mFrameSyncObject) {
                if (mFrameAvailable) {
                    throw new RuntimeException("mFrameAvailable already set, frame could be dropped");
                }
                mFrameAvailable = true;
                mFrameSyncObject.notifyAll();
            }
        }

        public void saveCustomFrame() throws IOException {
            if (direction.equals("Custom")) {
                mPixelBuf.rewind();
                GLES20.glReadPixels(0, 0, mWidth, mHeight, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE,
                        mPixelBuf);
                mPixelBuf.rewind();

                bmp = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
                bmp.copyPixelsFromBuffer(mPixelBuf);

                createCustomAnamorphosis(bmp, finalPixels);

                bmp.recycle();
            }
        }


        /**
         * Saves the current frame to disk as a PNG image.
         */

        public void saveFrame() throws IOException {
            idFrame++;
            mPixelBuf.rewind();
            GLES20.glReadPixels(0, 0, mWidth, mHeight, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE,
                    mPixelBuf);
            mPixelBuf.rewind();

           /* Thread thread = new Thread() {
             @Override
             public void run() {
               try {
                 while(true) {*/
            bmp = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
            bmp.copyPixelsFromBuffer(mPixelBuf);
            if (jumpFrame) {
                if (frameSelected.contains(String.valueOf(idFrame))) {
                    createAnamorphosis(bmp, finalPixels, indexRangePixels);
                    indexRangePixels += sample;
                }
            } else {
                if (interpolate && bmp2 != null) {
                    Log.e("Interpolate", "interpolate");
                    float bmpCount = (float) (1.0 / interpolationSample);
                    Log.e("InterpolateSample", String.valueOf(interpolationSample));
                    Log.e("Interpolate bmp count", String.valueOf(bmpCount));
                    idFrame++;

                    for (float i = bmpCount; i <= 1; i += bmpCount) {
                        if (bmp2 != null) {
                            // Calcul d'une nouvelle image interpolée
                            interpolatedBmp = Old_FinalRenderActivity.bitmapInterpolation(bmp2, bmp, i, sample, indexRangePixels);

                            createAnamorphosis(interpolatedBmp, finalPixels, indexRangePixels);
                            indexRangePixels += sample;
                            interpolatedBmp.recycle();
                        }
                    }
                    bmp2.recycle();
                }
                createAnamorphosis(bmp, finalPixels, indexRangePixels);
                indexRangePixels += sample;

             bmp2 = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
             bmp2.createBitmap(bmp);
            }
            bmp.recycle();
          /*  }
                      } catch (Exception e) {
                        e.printStackTrace();
                  }
            }
            };

            thread.start();*/


            System.out.println("frame " + idFrame);

        }

        /**
         * Checks for EGL errors.
         */
        private void checkEglError(String msg) {
            int error;
            if ((error = EGL14.eglGetError()) != EGL14.EGL_SUCCESS) {
                throw new RuntimeException(msg + ": EGL error: 0x" + Integer.toHexString(error));
            }
        }
    }

    /**
     * Code for rendering a texture onto a surface using OpenGL ES 2.0.
     */
    private static class STextureRender {
        private static final int FLOAT_SIZE_BYTES = 4;
        private static final int TRIANGLE_VERTICES_DATA_STRIDE_BYTES = 5 * FLOAT_SIZE_BYTES;
        private static final int TRIANGLE_VERTICES_DATA_POS_OFFSET = 0;
        private static final int TRIANGLE_VERTICES_DATA_UV_OFFSET = 3;
        private final float[] mTriangleVerticesData = {
                // X, Y, Z, U, V
                -1.0f, -1.0f, 0, 0.f, 0.f,
                1.0f, -1.0f, 0, 1.f, 0.f,
                -1.0f, 1.0f, 0, 0.f, 1.f,
                1.0f, 1.0f, 0, 1.f, 1.f,
        };

        private FloatBuffer mTriangleVertices;

        private static final String VERTEX_SHADER =
                "uniform mat4 uMVPMatrix;\n" +
                        "uniform mat4 uSTMatrix;\n" +
                        "attribute vec4 aPosition;\n" +
                        "attribute vec4 aTextureCoord;\n" +
                        "varying vec2 vTextureCoord;\n" +
                        "void main() {\n" +
                        "    gl_Position = uMVPMatrix * aPosition;\n" +
                        "    vTextureCoord = (uSTMatrix * aTextureCoord).xy;\n" +
                        "}\n";

        private static final String FRAGMENT_SHADER =
                "#extension GL_OES_EGL_image_external : require\n" +
                        "precision mediump float;\n" +      // highp here doesn't seem to matter
                        "varying vec2 vTextureCoord;\n" +
                        "uniform samplerExternalOES sTexture;\n" +
                        "void main() {\n" +
                        "    gl_FragColor = texture2D(sTexture, vTextureCoord);\n" +
                        "}\n";

        private float[] mMVPMatrix = new float[16];
        private float[] mSTMatrix = new float[16];

        private int mProgram;
        private int mTextureID = -12345;
        private int muMVPMatrixHandle;
        private int muSTMatrixHandle;
        private int maPositionHandle;
        private int maTextureHandle;

        public STextureRender() {
            mTriangleVertices = ByteBuffer.allocateDirect(
                    mTriangleVerticesData.length * FLOAT_SIZE_BYTES)
                    .order(ByteOrder.nativeOrder()).asFloatBuffer();
            mTriangleVertices.put(mTriangleVerticesData).position(0);

            Matrix.setIdentityM(mSTMatrix, 0);
        }

        public int getTextureId() {
            return mTextureID;
        }

        /**
         * Draws the external texture in SurfaceTexture onto the current EGL surface.
         */
        public void drawFrame(SurfaceTexture st, boolean invert) {
            checkGlError("onDrawFrame start");
            st.getTransformMatrix(mSTMatrix);
            if (invert) {
                mSTMatrix[5] = -mSTMatrix[5];
                mSTMatrix[13] = 1.0f - mSTMatrix[13];
            }

            // (optional) clear to green so we can see if we're failing to set pixels
            GLES20.glClearColor(0.0f, 1.0f, 0.0f, 1.0f);
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

            GLES20.glUseProgram(mProgram);
            checkGlError("glUseProgram");

            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTextureID);

            mTriangleVertices.position(TRIANGLE_VERTICES_DATA_POS_OFFSET);
            GLES20.glVertexAttribPointer(maPositionHandle, 3, GLES20.GL_FLOAT, false,
                    TRIANGLE_VERTICES_DATA_STRIDE_BYTES, mTriangleVertices);
            checkGlError("glVertexAttribPointer maPosition");
            GLES20.glEnableVertexAttribArray(maPositionHandle);
            checkGlError("glEnableVertexAttribArray maPositionHandle");

            mTriangleVertices.position(TRIANGLE_VERTICES_DATA_UV_OFFSET);
            GLES20.glVertexAttribPointer(maTextureHandle, 2, GLES20.GL_FLOAT, false,
                    TRIANGLE_VERTICES_DATA_STRIDE_BYTES, mTriangleVertices);
            checkGlError("glVertexAttribPointer maTextureHandle");
            GLES20.glEnableVertexAttribArray(maTextureHandle);
            checkGlError("glEnableVertexAttribArray maTextureHandle");

            Matrix.setIdentityM(mMVPMatrix, 0);
            GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, mMVPMatrix, 0);
            GLES20.glUniformMatrix4fv(muSTMatrixHandle, 1, false, mSTMatrix, 0);

            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
            checkGlError("glDrawArrays");

            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
        }

        /**
         * Initializes GL state.  Call this after the EGL surface has been created and made current.
         */
        public void surfaceCreated() {
            mProgram = createProgram(VERTEX_SHADER, FRAGMENT_SHADER);
            if (mProgram == 0) {
                throw new RuntimeException("failed creating program");
            }

            maPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
            checkLocation(maPositionHandle, "aPosition");
            maTextureHandle = GLES20.glGetAttribLocation(mProgram, "aTextureCoord");
            checkLocation(maTextureHandle, "aTextureCoord");

            muMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
            checkLocation(muMVPMatrixHandle, "uMVPMatrix");
            muSTMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uSTMatrix");
            checkLocation(muSTMatrixHandle, "uSTMatrix");

            int[] textures = new int[1];
            GLES20.glGenTextures(1, textures, 0);

            mTextureID = textures[0];
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTextureID);
            checkGlError("glBindTexture mTextureID");

            GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER,
                    GLES20.GL_NEAREST);
            GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER,
                    GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S,
                    GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T,
                    GLES20.GL_CLAMP_TO_EDGE);
            checkGlError("glTexParameter");
        }

        /**
         * Replaces the fragment shader.  Pass in null to reset to default.
         */
        public void changeFragmentShader(String fragmentShader) {
            if (fragmentShader == null) {
                fragmentShader = FRAGMENT_SHADER;
            }
            GLES20.glDeleteProgram(mProgram);
            mProgram = createProgram(VERTEX_SHADER, fragmentShader);
            if (mProgram == 0) {
                throw new RuntimeException("failed creating program");
            }
        }

        private int loadShader(int shaderType, String source) {
            int shader = GLES20.glCreateShader(shaderType);
            checkGlError("glCreateShader type=" + shaderType);
            GLES20.glShaderSource(shader, source);
            GLES20.glCompileShader(shader);
            int[] compiled = new int[1];
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
            if (compiled[0] == 0) {
                Log.e(TAG, "Could not compile shader " + shaderType + ":");
                Log.e(TAG, " " + GLES20.glGetShaderInfoLog(shader));
                GLES20.glDeleteShader(shader);
                shader = 0;
            }
            return shader;
        }

        private int createProgram(String vertexSource, String fragmentSource) {
            int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
            if (vertexShader == 0) {
                return 0;
            }
            int pixelShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
            if (pixelShader == 0) {
                return 0;
            }

            int program = GLES20.glCreateProgram();
            if (program == 0) {
                Log.e(TAG, "Could not create program");
            }
            GLES20.glAttachShader(program, vertexShader);
            checkGlError("glAttachShader");
            GLES20.glAttachShader(program, pixelShader);
            checkGlError("glAttachShader");
            GLES20.glLinkProgram(program);
            int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
            if (linkStatus[0] != GLES20.GL_TRUE) {
                Log.e(TAG, "Could not link program: ");
                Log.e(TAG, GLES20.glGetProgramInfoLog(program));
                GLES20.glDeleteProgram(program);
                program = 0;
            }
            return program;
        }

        public void checkGlError(String op) {
            int error;
            while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
                Log.e(TAG, op + ": glError " + error);
                throw new RuntimeException(op + ": glError " + error);
            }
        }

        public static void checkLocation(int location, String label) {
            if (location < 0) {
                throw new RuntimeException("Unable to locate '" + label + "' in program");
            }
        }
    }

    public void selectFrame(int fps, int duration, int size) {
        Log.e("ici duration", String.valueOf(duration));
        int nbFrame = fps * duration;
        float step = nbFrame / size;
        Log.e("ici  nbFrame", String.valueOf(nbFrame));
        Log.e("ici step", String.valueOf(step));
        int vEntiere = (int) step;
        float vReste = (size * (step - vEntiere)) / nbFrame;
        int vEcompteur = vEntiere;
        float vRcompteur = vReste;
        for (int i = 0; i < nbFrame; i++) {
            if (vRcompteur >= 1) {
                vEcompteur += (int) vRcompteur;
                vRcompteur = vRcompteur - (int) vRcompteur;
            }
            if (vEcompteur == 1) {
                //traitement/
                frameSelected.add(String.valueOf(i));
                Log.e("ici", String.valueOf(i));
                vEcompteur = vEntiere;
            } else {
                vEcompteur--;
            }
            vRcompteur += vReste;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mFramesExtractionTask.cancel(true);
    }
}