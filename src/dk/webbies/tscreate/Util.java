package dk.webbies.tscreate;


import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

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
        InputStream is;
        private CountDownLatch latch;
        private String result;

        private StreamGobbler(InputStream is, CountDownLatch latch) {
            this.is = is;
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

    public static <T, S> List<S> cast(Class<S> clazz, List<T> list) {
        for (T t : list) {
            if (!clazz.isInstance(t)) {
                throw new ClassCastException("Cannot cast : " + t + " to class " + clazz.getName());
            }
        }
        return (List<S>) list;
    }
}
