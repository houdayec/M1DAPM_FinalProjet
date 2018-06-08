package dapm.g1.final_project.custom_classes;

/**
 * Created by mickael alos on 03/06/2018.
 */
public class Line {

    private float at, a, b;

    private CustomPointF p1, p2;

    /**
     * CONSTRUCTORS
     */
    public Line(CustomPointF p1, CustomPointF p2){
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

    public Line(float at, float a, float b, CustomPointF p1, CustomPointF p2) {
        this.at = at;
        this.a = a;
        this.b = b;
        this.p1 = p1;
        this.p2 = p2;
    }

    public float position(CustomPointF m) {
        return position(p1,p2,m);
    }

    public static float position(CustomPointF p1, CustomPointF p2, CustomPointF m) {
        return (p2.x - p1.x) * (m.y - p1.y) - (p2.y - p1.y) * (m.x - p1.x);
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

    public float getB() {
        return b;
    }

    public CustomPointF getP1() {
        return p1;
    }

    public CustomPointF getP2() {
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
