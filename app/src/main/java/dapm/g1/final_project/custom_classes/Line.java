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

    /**
     * Get the point position in relation to the line
     * @param m a point
     * @return a positive number if the point is in his positive zone and a negative number else
     */
    public float position(CustomPointF m) {
        return position(p1,p2,m);
    }

    /**
     * Get the point position in relation to the line
     * @param p1 first point of the line
     * @param p2 last point of the line
     * @param m a point
     * @return a positive number if the point is in his positive zone and a negative number else
     */
    public static float position(CustomPointF p1, CustomPointF p2, CustomPointF m) {
        return (p2.x - p1.x) * (m.y - p1.y) - (p2.y - p1.y) * (m.x - p1.x);
    }


    //METHODS

    /**
     * x value calculation at y position
     * @param y
     * @return x value
     */
    public float calcX(float y) {
        return calcX(y,a,b,at);
    }

    /**
     * y value calculation at x position
     * @param x
     * @return y value
     */
    public float calcY(float x) {
        return calcY(x,a,b,at);
    }

    /**
     * x value calculation at y position
     * @param y position
     * @param a x shift
     * @param b y shift
     * @param at slope
     * @return x value
     */
    public static float calcX(float y, float a, float b, float at) {
        return a + ((y - b) / at);
    }

    /**
     * y value calculation at x position
     * @param x position
     * @param a x shift
     * @param b y shift
     * @param at slope
     * @return y value
     */
    public static float calcY(float x,float a,float b,float at) {
        return at * (x - a) + b;
    }

    /**
     * Get a copy of the line
     * @return copy
     */
    public Line copy(){
        return new Line(at, a, b, p1.copy(), p2.copy());
    }

    /**
     * slope getter
     * @return self slope
     */
    public float getAt() {
        return at;
    }

    /**
     * x shift getter
     * @return self x shift
     */
    public float getA() {
        return a;
    }

    /**
     * y shift getter
     * @return self y shift
     */
    public float getB() {
        return b;
    }

    /**
     * Get the first point of the line
     * @return self first point
     */
    public CustomPointF getP1() {
        return p1;
    }

    /**
     * Get the last point of the line
     * @return self last point
     */
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
