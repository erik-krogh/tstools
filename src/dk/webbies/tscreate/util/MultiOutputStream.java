package dk.webbies.tscreate.util;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Erik Krogh Kristensen on 27-10-2015.
 */
public class MultiOutputStream extends OutputStream {
    private final List<OutputStream> streams;

    public MultiOutputStream(OutputStream... streams) {
        this.streams = new ArrayList<>(Arrays.asList(streams));
    }

    @Override
    public synchronized void write(byte[] b) throws IOException {
        for (OutputStream stream : streams) {
            stream.write(b);
        }
    }

    @Override
    public synchronized void write(byte[] b, int off, int len) throws IOException {
        for (OutputStream stream : streams) {
            stream.write(b, off, len);
        }
    }

    @Override
    public synchronized void write(int b) throws IOException {
        for (OutputStream stream : streams) {
            stream.write(b);
        }
    }

    @Override
    public void flush() throws IOException {
        for (OutputStream stream : streams) {
            stream.flush();
        }
    }

    @Override
    public void close() throws IOException {
        IOException exception = null;
        for (OutputStream stream : streams) {
            try {
                stream.close();
            } catch (IOException ioExp) {
                exception = ioExp;
                // Then go on.
            } catch (Throwable throwable) {
                // Go on..
            }
        }
        if (exception != null) {
            throw exception;
        }
    }

}
