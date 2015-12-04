package dk.webbies.tscreate.util;

import java.util.Objects;

/**
 * Created by Erik Krogh Kristensen on 21-10-2015.
 */
public class Pair<A, B> {
    public final A first;
    public final B second;

    public Pair(A left, B right) {
        this.first = left;
        this.second = right;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pair<?, ?> pair = (Pair<?, ?>) o;
        return Objects.equals(first, pair.first) &&
                Objects.equals(second, pair.second);
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second);
    }

    @Override
    public String toString() {
        return "Pair{" +
                "left=" + first +
                ", right=" + second +
                '}';
    }
}
