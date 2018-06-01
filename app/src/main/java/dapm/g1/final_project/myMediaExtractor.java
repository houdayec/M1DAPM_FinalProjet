package dapm.g1.final_project;

import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by mickael alos on 31/05/2018.
 */
public class myMediaExtractor {
    private final MediaExtractor extractor;
    private MediaFormat format=null;
    private boolean VERBOSE=false;
    private String TAG=this.getClass().getSimpleName();

    public myMediaExtractor(final String FILE_NAME) throws IOException {
        extractor = new MediaExtractor();
        extractor.setDataSource(FILE_NAME);
    }

    public myMediaExtractor setVERBOSE(boolean newstate){
        VERBOSE = newstate;
        return this;
    }

    public myMediaExtractor setTAG(String newtag){
        TAG = newtag;
        return this;
    }

    public void selectTrack(int trackIndex){
        if (trackIndex!=-1) {
            extractor.selectTrack(trackIndex);
            format = extractor.getTrackFormat(trackIndex);
        } else {
            format = null;
            Log.e(TAG,"Invalide track index");
        }
    }

    public int getVideoWidth() throws NoTrackSelectedException {
        if (format!=null) return format.getInteger(MediaFormat.KEY_WIDTH);
        throw new NoTrackSelectedException();
    }

    public int getVideoHeight() throws NoTrackSelectedException {
        if (format!=null) return format.getInteger(MediaFormat.KEY_HEIGHT);
        throw new NoTrackSelectedException();
    }

    public long getVideoDuration() throws NoTrackSelectedException {
        if (format!=null) return format.getLong(MediaFormat.KEY_DURATION);
        throw new NoTrackSelectedException();
    }

    public int getVideoFrameRate() throws NoTrackSelectedException {
        if (format!=null) return format.getInteger(MediaFormat.KEY_FRAME_RATE);
        throw new NoTrackSelectedException();
    }

    public String getVideoMine() throws NoTrackSelectedException {
        if (format!=null) return format.getString(MediaFormat.KEY_MIME);
        throw new NoTrackSelectedException();
    }

    public MediaFormat getFormat() throws NoTrackSelectedException {
        if (format!=null) return format;
        throw new NoTrackSelectedException();
    }

    public int readSampleData(ByteBuffer var1, int var2){
        return extractor.readSampleData(var1, var2);
    }

    public int getSampleTrackIndex(){
        return extractor.getSampleTrackIndex();
    }

    public long getSampleTime(){
        return extractor.getSampleTime();
    }

    public boolean advance() {
        return extractor.advance();
    }

    public void release(){
        extractor.release();
    }

    public int getTrackVideoIndex() {
        // Select the first video track we find, ignore the rest.
        int numTracks = extractor.getTrackCount();
        for (int i = 0; i < numTracks; i++) {
            MediaFormat format = extractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);
            if (mime.startsWith("video/")) {
                if (VERBOSE) {
                    Log.d(TAG, "Extractor selected track " + i + " (" + mime + "): " + format);
                }
                return i;
            }
        }
        return -1;
    }

    public class NoTrackSelectedException extends Exception {
        private Exception exception;

        public NoTrackSelectedException() {
            super("No track selected");
            Log.e(TAG,"Select a track before");
        }

        public NoTrackSelectedException(String message) {
            super(message);
            Log.e(TAG,"Select a track before");
        }

        public NoTrackSelectedException(Exception exception) {
            this.exception = exception;
            Log.e(TAG,"Select a track before");
        }

        public Exception getException() {
            return exception;
        }

        @Override
        public String getMessage() {
            return (exception!=null) ? exception.getMessage() : super.getMessage();
        }
    }
}
