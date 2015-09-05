package com.google.javascript.jscomp.parsing;

import java.util.Set;

/**
 * Gives access to com.google.javascript.jscomp.parsing.Config.
 */
public class ConfigExposer {
    public static Config createConfig(Set<String> annotationWhitelist, Set<String> suppressionNames,
                                      boolean isIdeMode, boolean parseJSDoc, Config.LanguageMode languageMode) {
        return new Config(annotationWhitelist, suppressionNames, isIdeMode, parseJSDoc, languageMode);
    }
}
