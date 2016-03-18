package dk.webbies.tscreate.evaluation;

import dk.webbies.tscreate.evaluation.DeclarationEvaluator.EvaluationQueueElement;

import java.util.*;

/**
 * Created by Erik Krogh Kristensen on 16-12-2015.
 *
 * Is used to create a lot of sub-callbacks, and when all those are done, the main callback is executed.
 */
public class WhenAllDone {
    private final EvaluationQueueElement callback;
    private PriorityQueue<EvaluationQueueElement> queue;

    private int notDoneCounter = 0;
    boolean ran = false;

    public WhenAllDone(EvaluationQueueElement callback, PriorityQueue<EvaluationQueueElement> queue) {
        this.callback = callback;
        this.queue = queue;
    }

    public Runnable newSubCallback() {
        notDoneCounter++;
        return () -> {
            queue.add(new EvaluationQueueElement(callback.depth, () -> {
                if (ran) {
                    throw new RuntimeException("Has already run");
                }
                notDoneCounter--;
                if (notDoneCounter == 0) {
                    callback.runnable.run();
                    ran = true;
                }
            }));
        };
    }
}
