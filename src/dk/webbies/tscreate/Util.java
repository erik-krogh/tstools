package dk.webbies.tscreate;


import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.function.BiFunction;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Created by erik1 on 01-09-2015.
 */
public class Util {
    public static String runNodeScript(String args) throws IOException {
        Process process = Runtime.getRuntime().exec("node " + args);

        CountDownLatch latch = new CountDownLatch(2);
        StreamGobbler inputGobbler = new StreamGobbler(process.getInputStream(), latch);
        StreamGobbler errGobbler = new StreamGobbler(process.getErrorStream(), latch);

        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        if (!errGobbler.getResult().isEmpty()) {
            throw new RuntimeException("Got an error while creating the Snapshot of the library: " + errGobbler.getResult());
        }

        return inputGobbler.getResult();
    }

    private static class StreamGobbler extends Thread {
        BufferedInputStream is;
        private CountDownLatch latch;
        private String result;

        private StreamGobbler(InputStream is, CountDownLatch latch) {
            this.is = new BufferedInputStream(is);
            this.latch = latch;
            this.start();
        }

        public String getResult() {
            return result;
        }

        @Override
        public void run() {
            try {
                result = IOUtils.toString(is);
                is.close();
                latch.countDown();
            }
            catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    // I would really like to force ColT and ColS to be the same subtype of Collection. But I don't think Java generics can handle that.
    public static <T, S, ColT extends Collection<T>, ColS extends Collection<S>> ColS cast(Class<S> clazz, ColT list) {
        for (T t : list) {
            if (!clazz.isInstance(t)) {
                throw new ClassCastException("Cannot cast : " + t + " to class " + clazz.getName());
            }
        }
        return (ColS)list;
    }

    public static String readFile(String path) throws IOException {
        FileReader reader = new FileReader(new File(path));
        String result = IOUtils.toString(reader);
        reader.close();
        return result;
    }

    // http://stackoverflow.com/questions/17640754/zipping-streams-using-jdk8-with-lambda-java-util-stream-streams-zip#answer-23529010
    public static<A, B, C> Stream<C> zip(Stream<? extends A> a,
                                         Stream<? extends B> b,
                                         BiFunction<? super A, ? super B, ? extends C> zipper) {
        Objects.requireNonNull(zipper);
        @SuppressWarnings("unchecked")
        Spliterator<A> aSpliterator = (Spliterator<A>) Objects.requireNonNull(a).spliterator();
        @SuppressWarnings("unchecked")
        Spliterator<B> bSpliterator = (Spliterator<B>) Objects.requireNonNull(b).spliterator();

        // Zipping looses DISTINCT and SORTED characteristics
        int both = aSpliterator.characteristics() & bSpliterator.characteristics() &
                ~(Spliterator.DISTINCT | Spliterator.SORTED);
        int characteristics = both;

        long zipSize = ((characteristics & Spliterator.SIZED) != 0)
                ? Math.min(aSpliterator.getExactSizeIfKnown(), bSpliterator.getExactSizeIfKnown())
                : -1;

        Iterator<A> aIterator = Spliterators.iterator(aSpliterator);
        Iterator<B> bIterator = Spliterators.iterator(bSpliterator);
        Iterator<C> cIterator = new Iterator<C>() {
            @Override
            public boolean hasNext() {
                return aIterator.hasNext() && bIterator.hasNext();
            }

            @Override
            public C next() {
                return zipper.apply(aIterator.next(), bIterator.next());
            }
        };

        Spliterator<C> split = Spliterators.spliterator(cIterator, zipSize, characteristics);
        return (a.isParallel() || b.isParallel())
                ? StreamSupport.stream(split, true)
                : StreamSupport.stream(split, false);
    }

    public static final class Pair<A, B> {
        public final A left;
        public final B right;

        public Pair(A left, B right) {
            this.left = left;
            this.right = right;
        }
    }

    public static<A, B> Stream<Pair<A, B>> zip(Stream<? extends A> a,
                                         Stream<? extends B> b) {
        return zip(a, b, Pair::new);
    }
}
