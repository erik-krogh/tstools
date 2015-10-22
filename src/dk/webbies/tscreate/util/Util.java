package dk.webbies.tscreate.util;


import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiFunction;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Created by erik1 on 01-09-2015.
 */
public class Util {
    private static String runNodeScript(String args) throws IOException {
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
            throw new RuntimeException("Got an error running a node script: " + errGobbler.getResult());
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

    public static String getCachedOrRun(String cachePath, File checkAgainst, String nodeArgs) throws IOException {
        cachePath = cachePath.replaceAll("/", "");

        if (checkAgainst != null && !checkAgainst.exists()) {
            throw new RuntimeException("I cannot check against something that doesn't exist.");
        }

        File cache = new File("cache/" + cachePath);

        boolean recreate = false;
        if (!cache.exists()) {
            recreate = true;
        } else {
            long jsnapLastModified = getLastModified(cache);
            long jsLastModified = getLastModified(checkAgainst);
            if (jsnapLastModified < jsLastModified) {
                recreate = true;
            }
        }

        if (recreate) {
            System.out.println("Creating " + cache.getPath() + " from scratch.");
            String jsnap = Util.runNodeScript(nodeArgs);
            BufferedWriter writer = new BufferedWriter(new FileWriter(cache));
            writer.write(jsnap);
            writer.close();
            return jsnap;
        } else {
            FileReader reader = new FileReader(cache);
            String result = IOUtils.toString(reader);
            reader.close();
            return result;
        }
    }

    // http://stackoverflow.com/questions/12249155/how-to-get-the-last-modified-date-and-time-of-a-directory-in-java#answer-12249411
    private static long getLastModified(File file) {
        if (file == null) {
            return 0;
        }
        if (!file.exists()) {
            return 0;
        }
        if (!file.isDirectory()) {
            return file.lastModified();
        } else {
            File[] files = file.listFiles();
            assert files != null;
            if (files.length == 0) return file.lastModified();
            Arrays.sort(files, (o1, o2) -> {
                return Long.valueOf(o2.lastModified()).compareTo(o1.lastModified()); //latest 1st
            });

            return files[0].lastModified();
        }
    }


    private static final ExecutorService threadPool = Executors.newCachedThreadPool();
    public static void runAll(Runnable... runs) throws Throwable {
        CountDownLatch latch = new CountDownLatch(runs.length);
        final Throwable[] exception = {null};
        for (Runnable run : runs) {
            threadPool.submit(() -> {
                try {
                    run.run();
                } catch (Throwable e) {
                    exception[0] = e;
                } finally {
                    latch.countDown();
                }
            });
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (exception[0] != null) {
            throw exception[0];
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
        return new String(Files.readAllBytes(Paths.get(path)));
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



    public static<A, B> Stream<Pair<A, B>> zip(Stream<? extends A> a,
                                         Stream<? extends B> b) {
        return zip(a, b, Pair::new);
    }

    public static<E> List<E> reduceList(List<E> acc, List<E> elem) {
        acc.addAll(elem);
        return acc;
    };

    public static<E> ArrayList<E> reduceList(ArrayList<E> acc, ArrayList<E> elem) {
        acc.addAll(elem);
        return acc;
    };

    public static<E> Set<E> reduceSet(Set<E> acc, Set<E> elem) {
        acc.addAll(elem);
        return acc;
    };


}
