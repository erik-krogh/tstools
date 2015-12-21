package dk.webbies.tscreate.evaluation;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Erik Krogh Kristensen on 16-12-2015.
 *
 * Is used to create a lot of sub-callbacks, and when all those are done, the main callback is executed.
 */
public class WhenAllDone {
    private final Runnable callback;
    private Collection<Runnable> queue;

    private int notDoneCounter = 0;
    boolean ran = false;

    public WhenAllDone(Runnable callback, Collection<Runnable> queue) {
        this.callback = callback;
        this.queue = queue;
        nonCompleted.add(this);
    }

    public static Set<WhenAllDone> nonCompleted = new HashSet<>();

    private Set<Integer> returnedCallbacks = new HashSet<>();
    private static int counter = 0;
    public Runnable newSubCallback(int i) {
        returnedCallbacks.add(counter++);
        notDoneCounter++;
        return () -> {
            queue.add(() -> {
                if (ran) {
                    throw new RuntimeException("Has already run");
                }
                nonCompleted.remove(this);
                notDoneCounter--;
                if (notDoneCounter == 0) {
                    callback.run();
                    ran = true;
                }
            });
        };
    }
}
