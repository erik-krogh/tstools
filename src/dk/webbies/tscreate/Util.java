package dk.webbies.tscreate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;

/**
 * Created by erik1 on 01-09-2015.
 */
public class Util {
    public static String runNodeScript(String args) throws IOException {
        Process p = Runtime.getRuntime().exec("node " + args);
        String line;
        StringBuilder builder = new StringBuilder();
        BufferedReader input =
                new BufferedReader
                        (new InputStreamReader(p.getInputStream()));
        while ((line = input.readLine()) != null) {
            builder.append(line);
        }
        input.close();
        return builder.toString();
    }
}
