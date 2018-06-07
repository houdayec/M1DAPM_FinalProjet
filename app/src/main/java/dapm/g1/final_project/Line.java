package dapm.g1.final_project;

/**
 * Created by mickael alos on 03/06/2018.
 */
public class Line {

    private float at, a, b;

    private PPointF p1, p2;

    /**
     * CONSTRUCTORS
     */
    public Line(PPointF p1, PPointF p2){
        if (p1.x < p2.x){
            this.p1 = p1;
            this.p2 = p2;
        }
        else {
            this.p1 = p2;
            this.p2 = p1;
        }
        at = (this.p2.y-this.p1.y)/(this.p2.x-this.p1.x);
        a = this.p1.x;
        b = this.p1.y;
    }

    public Line(float at, float a, float b, PPointF p1, PPointF p2) {
        this.at = at;
        this.a = a;
        this.b = b;
        this.p1 = p1;
        this.p2 = p2;
    }

    /**
     * METHODS
     */
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

    public float getA() {
        return a;
    }

    public void setA(float a) {
        this.a = a;
    }

    public float getB() {
        return b;
    }

    public PPointF getP1() {
        return p1;
    }

    public PPointF getP2() {
        return p2;
    }

    /**
     * OVERRIDED METHODS
     */
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
