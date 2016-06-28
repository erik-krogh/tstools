package dk.webbies.tscreate.util;

/**
 * Created by Erik Krogh Kristensen on 21-10-2015.
 */
public class Tuple3<A, B, C> {
    public final A a;
    public final B b;
    public final C c;

    public Tuple3(A a, B b, C c) {
        this.a = a;
        this.b = b;
        this.c = c;
    }

    public A getA() {
        return a;
    }

    public B getB() {
        return b;
    }

    public C getC() {
        return c;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Tuple3<?, ?, ?> tuple3 = (Tuple3<?, ?, ?>) o;

        if (a != null ? !a.equals(tuple3.a) : tuple3.a != null) return false;
        if (b != null ? !b.equals(tuple3.b) : tuple3.b != null) return false;
        return c != null ? c.equals(tuple3.c) : tuple3.c == null;

    }

    @Override
    public int hashCode() {
        int result = a != null ? a.hashCode() : 0;
        result = 31 * result + (b != null ? b.hashCode() : 0);
        result = 31 * result + (c != null ? c.hashCode() : 0);
        return result;
    }
}
