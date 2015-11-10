package dk.webbies.tscreate.jsnap;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.util.Arrays;
import java.util.logging.Level;


/**
 * Created by Erik Krogh Kristensen on 10-11-2015.
 */
public class SeleniumDriver {
    public static void main(String[] args) {
        System.out.println(executeScript("console.log(\"\\\"3\\\"\")"));
    }

    private static String getEmptyPageUrl() {
        boolean isWindows = System.getProperty("os.name").toLowerCase().contains("windows");
        String workingDir = System.getProperty("user.dir");
        if (isWindows) {
            return "file:///" + workingDir + "\\lib\\selenium\\empty.html";
        } else {
            return "file:///" + workingDir + "/lib/selenium/empty.html";
        }
    }

    public static String executeScript(String script) {
        setDriverPath();

        ChromeDriver driver = new ChromeDriver(buldCapabilities());

        driver.get(getEmptyPageUrl());

        driver.executeScript(script);

        LogEntries logEntries = driver.manage().logs().get(LogType.BROWSER);

        driver.close();

        LogEntry jsnapEntry = findJsnapEntry(logEntries);

        String message = jsnapEntry.getMessage();
        if (!message.substring(0, 12).equals("console-api ")) {
            throw new RuntimeException("I don't even know!");
        }

        message = message.substring(12, message.length());

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
        }  else {
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
