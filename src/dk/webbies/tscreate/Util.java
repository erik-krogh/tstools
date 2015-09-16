package dk.webbies.tscreate;


import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;

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
}
