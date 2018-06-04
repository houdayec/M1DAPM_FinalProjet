package dapm.g1.final_project;

import android.graphics.Point;
import android.graphics.PointF;

import java.io.Serializable;

/**
 * Created by mickael alos on 30/05/2018.
 */
public class PPointF extends PointF implements Serializable{
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

    public PPointF getInvert() {
        return new PPointF(y,x);
    }

    public PPointF copy(){
        return new PPointF(x,y);
    }

    @Override
    public String toString() {
        return "PPointF{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}
