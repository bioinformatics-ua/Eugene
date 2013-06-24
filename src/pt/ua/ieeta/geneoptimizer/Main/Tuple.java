package pt.ua.ieeta.geneoptimizer.Main;

/**
 *
 * @author Nuno Silva <nuno.mogas@ua.pt>
 */
public class Tuple<X, Y> {

    private X x;
    private Y y;

    public Tuple(X x, Y y) {
        this.x = x;
        this.y = y;
    }

    public X getX() {
        return x;
    }

    public Y getY() {
        return y;
    }
}