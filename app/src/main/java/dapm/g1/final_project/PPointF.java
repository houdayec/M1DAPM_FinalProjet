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
        return norm(x-p.x,y-p.y);
    }

    public static float distanceBetween(PPointF p1, PPointF p2) {
        return norm(p1.x-p2.x,p1.y-p2.y);
    }

    private static float norm(float dx, float dy){
        return (float)Math.sqrt(Math.pow(dx,2)+Math.pow(dy,2));
    }

    public static float angle(PPointF p1, PPointF pm, PPointF p2){
        float dxU = p1.x-pm.x;
        float dyU = p1.y-pm.y;
        float dxV = pm.x-p2.x;
        float dyV = pm.y-p2.y;
        return (float)Math.toDegrees(Math.acos((dxU*dxV+dyU*dyV)/(norm(dxU,dyU)*norm(dxV,dyV))));
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
