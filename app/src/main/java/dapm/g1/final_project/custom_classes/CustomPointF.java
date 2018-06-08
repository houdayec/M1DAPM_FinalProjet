package dapm.g1.final_project.custom_classes;

import android.graphics.Point;
import android.graphics.PointF;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by mickael alos on 30/05/2018.
 */
public class CustomPointF extends PointF implements Parcelable{

    public CustomPointF() {
        super();
    }
    public CustomPointF(float x, float y) {
        super(x,y);
    }
    public CustomPointF(Point p) {
        super(p);
    }
    public CustomPointF(PointF p) {
        super(p.x,p.y);
    }
    public float get(int axe){
        return (axe>0)? y : x;
    }

    public float distanceBetween(CustomPointF p){
        return norm(x-p.x,y-p.y);
    }

    public static float distanceBetween(CustomPointF p1, CustomPointF p2) {
        return norm(p1.x-p2.x,p1.y-p2.y);
    }

    private static float norm(float dx, float dy){
        return (float)Math.sqrt(Math.pow(dx,2)+Math.pow(dy,2));
    }

    public static float angle(CustomPointF p1, CustomPointF pm, CustomPointF p2){
        float dxU = p1.x-pm.x;
        float dyU = p1.y-pm.y;
        float dxV = pm.x-p2.x;
        float dyV = pm.y-p2.y;
        return (float)Math.toDegrees(Math.acos((dxU*dxV+dyU*dyV)/(norm(dxU,dyU)*norm(dxV,dyV))));
    }

    public CustomPointF getInvert() {
        return new CustomPointF(y,x);
    }

    public CustomPointF copy(){
        return new CustomPointF(x,y);
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
        return "CustomPointF{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }

    public static final Parcelable.Creator<CustomPointF> CREATOR = new Parcelable.Creator<CustomPointF>()
    {
        @Override
        public CustomPointF createFromParcel(Parcel source)
        {
            return new CustomPointF(source);
        }

        @Override
        public CustomPointF[] newArray(int size)
        {
            return new CustomPointF[size];
        }
    };

    private CustomPointF(Parcel in) {
        this.x = in.readFloat();
        this.y = in.readFloat();
    }
}
