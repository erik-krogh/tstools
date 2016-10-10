package dk.webbies.tscreate.analysis.unionFind;

import dk.webbies.tscreate.paser.AST.Expression;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by erik1 on 24-08-2016.
 */
@SuppressWarnings({"unchecked"})
public abstract class UnionFindCallback implements Runnable {
    private List objects = new ArrayList();

    public UnionFindCallback(List objects) {
        this.objects.addAll(objects);
    }

    public UnionFindCallback(Object first, List objects) {
        this.objects.add(first);
        this.objects.addAll(objects);
    }

    public UnionFindCallback(Object first, Object second, Object third, List objects) {
        this.objects.add(first);
        this.objects.add(second);
        this.objects.add(third);
        this.objects.addAll(objects);
    }

    public UnionFindCallback(Object first, Object second, Object third) {
        this.objects.add(first);
        this.objects.add(second);
        this.objects.add(third);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UnionFindCallback that = (UnionFindCallback) o;

        return objects.equals(that.objects);

    }

    @Override
    public int hashCode() {
        return objects.hashCode();
    }

    @Override
    public abstract void run();
}
