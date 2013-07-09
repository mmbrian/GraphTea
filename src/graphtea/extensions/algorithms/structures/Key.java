package graphtea.extensions.algorithms.structures;

/**
 *
 * @author ASUS
 */
public interface Key {

    public abstract double getValue();

    public abstract void setValue(double d);

    public abstract void swapValue(Key key);

    public abstract String getLabel();

    public abstract boolean lessThan(Object obj);

    public abstract boolean lessThan(Key key);

    public abstract boolean largerThan(Object obj);

    public abstract boolean largerThan(Key key);

    public abstract boolean equals(Object obj);

    public abstract boolean equals(Key key);
}
