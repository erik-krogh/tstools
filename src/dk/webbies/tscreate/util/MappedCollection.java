package dk.webbies.tscreate.util;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.function.Function;

/**
 * Created by Erik Krogh Kristensen on 21-10-2015.
 */
public class MappedCollection<T, R> extends AbstractCollection<R> {
    private final Collection<? extends T> in;
    private final Function<T, R> func;

    public MappedCollection(Collection<? extends T> in, Function<T, R> func) {
        this.in = in;
        this.func = func;
    }

    @Override
    public Iterator<R> iterator() {
        return in.stream().map(this.func).iterator();
    }

    @Override
    public int size() {
        return in.size();
    }
}
