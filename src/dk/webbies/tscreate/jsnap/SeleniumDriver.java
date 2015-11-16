package dk.webbies.tscreate.jsnap;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import org.apache.commons.io.IOUtils;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.logging.Level;


/**
 * Created by Erik Krogh Kristensen on 10-11-2015.
 */
public class SeleniumDriver {
    public static void main(String[] args) {
        System.out.println(executeScript("console.log(\"{\\\"global\\\":1, this is test stuff\")"));
    }

    private static String getEmptyPageUrl(String scriptPath) {
        try {
            scriptPath = URLEncoder.encode(scriptPath, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        boolean isWindows = System.getProperty("os.name").toLowerCase().contains("windows");
        String workingDir = System.getProperty("user.dir");
        if (isWindows) {
            return "file:///" + workingDir + "\\lib\\selenium\\driver.html?script=" + scriptPath;
        } else {
            return "file:///" + workingDir + "/lib/selenium/driver.html?script=" + scriptPath;
        }
    }

    public static String executeScript(String script) {
        setDriverPath();

        ChromeDriver driver = new ChromeDriver(buldCapabilities());

        File scriptFile;
        String tmpFileSuffix = "tmpFileSeleniumDriverThing.js";
        try {
            scriptFile = File.createTempFile("script-", tmpFileSuffix);
            FileWriter out = new FileWriter(scriptFile);
            IOUtils.write(script.getBytes(), out);
            out.close();
        } catch (IOException e) {throw new RuntimeException();}

        driver.get(getEmptyPageUrl(scriptFile.getAbsolutePath()));

        LogEntries logEntries = driver.manage().logs().get(LogType.BROWSER);

        driver.close();

        LogEntry jsnapEntry = findJsnapEntry(logEntries);

        String message = jsnapEntry.getMessage();
        if (!message.contains(tmpFileSuffix)) {
            throw new RuntimeException("I don't even know");
        }

        message = message.substring(message.indexOf(tmpFileSuffix) + tmpFileSuffix.length() + 1, message.length());

        return message.substring(message.indexOf(" ") + 1, message.length());
    }

    private static LogEntry findJsnapEntry(LogEntries entries) {
        for (LogEntry entry : Lists.reverse(Arrays.asList(Iterators.toArray(entries.iterator(), LogEntry.class)))) {
            if (entry.getLevel() == Level.INFO && entry.getMessage().contains("{\"global\":1")) {
                return entry;
            }
        }
        throw new RuntimeException("Could not find a fitting logEntry");
    }

    private static void setDriverPath() {
        String operatingSystem = System.getProperty("os.name");
        if (operatingSystem.contains("Windows")) {
            System.setProperty("webdriver.chrome.driver", "./lib/selenium/chromedriver.exe");
        }else if (operatingSystem.contains("Linux")) {
            System.setProperty("webdriver.chrome.driver", "./lib/selenium/chromedriverLinux64");
        }
        else {
            throw new RuntimeException("Unknown operating system: " + operatingSystem);
        }
    }

    private static DesiredCapabilities buldCapabilities() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("window-size=400,400");

        DesiredCapabilities capabilities = DesiredCapabilities.chrome();
        LoggingPreferences loggingPreferences = new LoggingPreferences();
        loggingPreferences.enable(LogType.BROWSER, Level.ALL);
        capabilities.setCapability(CapabilityType.LOGGING_PREFS, loggingPreferences);
        capabilities.setCapability(ChromeOptions.CAPABILITY, options);
        return capabilities;
    }
}
