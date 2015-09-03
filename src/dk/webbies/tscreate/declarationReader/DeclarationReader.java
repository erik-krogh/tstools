package dk.webbies.tscreate.declarationReader;

import dk.webbies.tscreate.Util;
import dk.webbies.tscreate.declarationReader.tsTypeReader.SpecReader;

import java.io.IOException;

/**
 * Created by Erik Krogh Kristensen on 02-09-2015.
 */
public class DeclarationReader {
    public static void main(String[] args) throws IOException {
        String JSON = Util.runNodeScript("lib/ts-type-reader/src/CLI.js --env es5 tests/test.d.ts");
        SpecReader.Spec spec = SpecReader.read(JSON);
        System.out.println(JSON);
    }
}
