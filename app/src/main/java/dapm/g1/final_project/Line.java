package dapm.g1.final_project;

/**
 * Created by mickael alos on 03/06/2018.
 */
public class Line {
    private float at;
    private float a;
    private float b;
    private PPointF p1;
    private PPointF p2;

    public Line(float at, float a, float b, PPointF p1, PPointF p2) {
        this.at = at;
        this.a = a;
        this.b = b;
        this.p1 = p1;
        this.p2 = p2;
    }

    public float calcX(float y) {
        return calcX(y,a,b,at);
    }

    public float calcY(float x) {
        return calcY(x,a,b,at);
    }

    public static float calcX(float y, float a, float b, float at) {
        return a + ((y - b) / at);
    }

    public static float calcY(float x,float a,float b,float at) {
        return at * (x - a) + b;
    }

    public Line copy(){
        return new Line(at, a, b, p1.copy(), p2.copy());
    }

    public float getAt() {
        return at;
    }

    public void setAt(float at) {
        this.at = at;
    }

    public float getA() {
        return a;
    }

    public void setA(float a) {
        this.a = a;
    }

    public float getB() {
        return b;
    }

    public void setB(float b) {
        this.b = b;
    }

    public PPointF getP1() {
        return p1;
    }

    public void setP1(PPointF p1) {
        this.p1 = p1;
    }

    public PPointF getP2() {
        return p2;
    }

    public void setP2(PPointF p2) {
        this.p2 = p2;
    }

    @Override
    public String toString() {
        return "Line{" +
                "at=" + at +
                ", a=" + a +
                ", b=" + b +
                ", p1=" + p1 +
                ", p2=" + p2 +
                '}';
    }
}
