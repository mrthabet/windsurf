package base;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;

public class DriverManager {
    private static final ThreadLocal<WebDriver> DRIVER = new ThreadLocal<>();
    private static final ThreadLocal<WebDriverWait> WAIT = new ThreadLocal<>();

    private static final long DEFAULT_EXPLICIT_WAIT_SECONDS = 10L;

    public static void initDriver() {
        if (DRIVER.get() == null) {
            String browser = System.getProperty("browser", "chrome").toLowerCase();
            boolean headless = Boolean.parseBoolean(System.getProperty("headless", "false"));
            WebDriver driver;
            switch (browser) {
                case "firefox":
                    WebDriverManager.firefoxdriver().setup();
                    FirefoxOptions ff = new FirefoxOptions();
                    if (headless) ff.addArguments("-headless");
                    driver = new FirefoxDriver(ff);
                    break;
                case "edge":
                    WebDriverManager.edgedriver().setup();
                    EdgeOptions edge = new EdgeOptions();
                    if (headless) edge.addArguments("--headless=new", "--window-size=1440,900");
                    driver = new EdgeDriver(edge);
                    break;
                case "chrome":
                default:
                    WebDriverManager.chromedriver().setup();
                    ChromeOptions options = new ChromeOptions();
                    // Stability flags
                    options.addArguments("--no-sandbox");
                    options.addArguments("--disable-blink-features=AutomationControlled");
                    options.addArguments("--disable-infobars");
                    options.addArguments("--disable-dev-shm-usage");
                    // Set a common user-agent to reduce bot detection
                    options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");

                    boolean ci = "true".equalsIgnoreCase(System.getenv("CI"));
                    if (headless || ci) {
                        options.addArguments("--headless=new");
                        options.addArguments("--window-size=1920,1080");
                    } else {
                        options.addArguments("--start-maximized");
                    }

                    // Exclude the automation switch and disable the automation extension
                    Map<String, Object> prefs = new HashMap<>();
                    options.setExperimentalOption("excludeSwitches", Arrays.asList("enable-automation"));
                    options.setExperimentalOption("useAutomationExtension", false);
                    options.setExperimentalOption("prefs", prefs);

                    // Enable browser console logs
                    LoggingPreferences logPrefs = new LoggingPreferences();
                    logPrefs.enable(LogType.BROWSER, Level.ALL);
                    options.setCapability("goog:loggingPrefs", logPrefs);

                    driver = new ChromeDriver(options);
                    break;
            }
            DRIVER.set(driver);
            WAIT.set(new WebDriverWait(driver, Duration.ofSeconds(DEFAULT_EXPLICIT_WAIT_SECONDS)));
        }
    }

    public static WebDriver getDriver() {
        if (DRIVER.get() == null) {
            initDriver();
        }
        return DRIVER.get();
    }

    public static WebDriverWait getWait() {
        if (WAIT.get() == null) {
            initDriver();
        }
        return WAIT.get();
    }

    public static void quitDriver() {
        WebDriver driver = DRIVER.get();
        if (driver != null) {
            driver.quit();
            DRIVER.remove();
            WAIT.remove();
        }
    }

    public static String getBrowserConsoleLogs() {
        try {
            LogEntries entries = getDriver().manage().logs().get(LogType.BROWSER);
            StringBuilder sb = new StringBuilder();
            entries.forEach(e -> sb.append("[" + e.getLevel() + "] " + e.getMessage()).append(System.lineSeparator()));
            return sb.toString();
        } catch (Exception e) {
            return "No browser logs available: " + e.getMessage();
        }
    }
}
