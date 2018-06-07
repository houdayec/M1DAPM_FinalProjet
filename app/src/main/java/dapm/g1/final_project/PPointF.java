package dapm.g1.final_project;

import android.graphics.Point;
import android.graphics.PointF;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by mickael alos on 30/05/2018.
 */
public class PPointF extends PointF implements Parcelable{
    public PPointF() {
        super();
    }
    public PPointF(float x,float y) {
        super(x,y);
    }
    public PPointF(Point p) {
        super(p);
    }
    public PPointF(PointF p) {
        super(p.x,p.y);
    }
    public float get(int axe){
        return (axe>0)? y : x;
    }

    public float distanceBetween(PPointF p){
        return (float)Math.sqrt(Math.pow(x-p.x,2)+Math.pow(y-p.y,2));
    }

    public PPointF getInvert() {
        return new PPointF(y,x);
    }

    public PPointF copy(){
        return new PPointF(x,y);
    }

    @Override
    public int describeContents() {
        return super.describeContents();
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeFloat(x);
        out.writeFloat(y);
    }

    @Override
    public String toString() {
        return "PPointF{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }

    public static final Parcelable.Creator<PPointF> CREATOR = new Parcelable.Creator<PPointF>()
    {
        @Override
        public PPointF createFromParcel(Parcel source)
        {
            return new PPointF(source);
        }

        @Override
        public PPointF[] newArray(int size)
        {
            return new PPointF[size];
        }
    };

    private PPointF(Parcel in) {
        this.x = in.readFloat();
        this.y = in.readFloat();
    }
}
