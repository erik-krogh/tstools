package dk.webbies.tscreate.util;

import java.util.*;

/**
 * Created by Erik Krogh Kristensen on 26-11-2015.
 */
public class IdentityHashSet<T> implements Set<T> {
    private IdentityHashMap<T, Object> map = new IdentityHashMap<>();

    private Object value = new Object();

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return map.containsKey(o);
    }

    @Override
    public Iterator<T> iterator() {
        return map.keySet().iterator();
    }

    @Override
    public Object[] toArray() {
        return map.keySet().toArray();
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
        return map.keySet().toArray(a);
    }

    @Override
    public boolean add(T t) {
        return map.put(t, value) == null;
    }

    @Override
    public boolean remove(Object o) {
        return map.remove(o) != null;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return map.keySet().containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        boolean result = false;
        for (T t : c) {
            result |= this.add(t);
        }
        return result;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        HashSet<T> toRemove = new HashSet<>(this.map.keySet());
        //noinspection SuspiciousMethodCalls
        toRemove.removeAll(c);
        boolean result = false;
        for (T t : toRemove) {
            result |= this.remove(t);
        }
        return result;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean result = false;
        for (Object o : c) {
            result |= this.remove(o);
        }
        return result;
    }

    @Override
    public void clear() {
        map.clear();
    }
}
