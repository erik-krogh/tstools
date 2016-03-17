package dk.webbies.tscreate.util;

import java.util.Objects;

/**
 * Created by Erik Krogh Kristensen on 21-10-2015.
 */
public class Pair<A, B> {
    public final A left;
    public final B right;

    public Pair(A left, B right) {
        this.left = left;
        this.right = right;
    }

    public A getLeft() {
        return left;
    }

    public B getRight() {
        return right;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pair<?, ?> pair = (Pair<?, ?>) o;
        return Objects.equals(left, pair.left) &&
                Objects.equals(right, pair.right);
    }

    @Override
    public int hashCode() {
        return Objects.hash(left, right);
    }

    @Override
    public String toString() {
        return "{" +
                "1st=" + left +
                ", 2nd=" + right +
                '}';
    }
}
