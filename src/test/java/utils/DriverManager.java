package utils;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Utility class for managing WebDriver instances and waits.
 * This class provides thread-safe WebDriver management and utility methods.
 */
public class DriverManager {
    private static final ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();
    private static final long DEFAULT_WAIT_SECONDS = 20;
    private static boolean isHeadless = false;

    // Private constructor to prevent instantiation
    private DriverManager() {
    }

    /**
     * Initializes the WebDriver with the specified browser type.
     * If no browser is specified, defaults to Chrome.
     *
     * @param browser The browser to use (chrome, firefox, edge).
     * @return The initialized WebDriver instance.
     */
    public static WebDriver initDriver(String browser) {
        WebDriver existing = driverThreadLocal.get();
        if (existing != null) {
            return existing;
        }

        WebDriver driver;
        browser = (browser != null) ? browser.toLowerCase() : "chrome";

        switch (browser) {
            case "firefox":
                FirefoxOptions firefoxOptions = createFirefoxOptions();
                driver = new FirefoxDriver(firefoxOptions);
                break;

            case "edge":
                EdgeOptions edgeOptions = createEdgeOptions();
                driver = new EdgeDriver(edgeOptions);
                break;

            case "chrome":
            default:
                ChromeOptions chromeOptions = createChromeOptions();
                driver = new ChromeDriver(chromeOptions);
                break;
        }

        // Set default timeouts (explicit waits are preferred over implicit waits)
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
        driver.manage().window().maximize();

        driverThreadLocal.set(driver);
        return driver;
    }

    /**
     * Gets the current WebDriver instance.
     * If no instance exists, initializes a new one with default settings.
     *
     * @return The current WebDriver instance.
     */
    public static WebDriver getDriver() {
        WebDriver driver = driverThreadLocal.get();
        if (driver == null) {
            throw new IllegalStateException("WebDriver has not been initialized. Call DriverManager.initDriver(...) before getDriver().");
        }
        return driver;
    }

    /**
     * Quits the current WebDriver instance and removes it from the thread local storage.
     */
    public static void quitDriver() {
        WebDriver driver = driverThreadLocal.get();
        if (driver != null) {
            try {
                driver.quit();
            } finally {
                driverThreadLocal.remove();
            }
        }
    }

    /**
     * Creates a new WebDriverWait instance with the default timeout.
     *
     * @return A new WebDriverWait instance.
     */
    public static WebDriverWait getWait() {
        return new WebDriverWait(getDriver(), Duration.ofSeconds(DEFAULT_WAIT_SECONDS));
    }

    /**
     * Creates a new WebDriverWait instance with a custom timeout.
     *
     * @param seconds The timeout in seconds.
     * @return A new WebDriverWait instance with the specified timeout.
     */
    public static WebDriverWait getWait(long seconds) {
        return new WebDriverWait(getDriver(), Duration.ofSeconds(seconds));
    }

    private static ChromeOptions createChromeOptions() {
        ChromeOptions chromeOptions = new ChromeOptions();
        if (isHeadless) {
            chromeOptions.addArguments("--headless=new");
            chromeOptions.addArguments("--no-sandbox");
            chromeOptions.addArguments("--disable-dev-shm-usage");
            chromeOptions.addArguments("--window-size=1920,1080");
        }
        return chromeOptions;
    }

    private static FirefoxOptions createFirefoxOptions() {
        FirefoxOptions firefoxOptions = new FirefoxOptions();
        if (isHeadless) {
            firefoxOptions.addArguments("--headless");
            firefoxOptions.addArguments("--window-size=1920,1080");
        }
        return firefoxOptions;
    }

    private static EdgeOptions createEdgeOptions() {
        EdgeOptions edgeOptions = new EdgeOptions();
        if (isHeadless) {
            edgeOptions.addArguments("--headless=new");
            edgeOptions.addArguments("--window-size=1920,1080");
        }
        return edgeOptions;
    }

    /**
     * Enables or disables headless mode for WebDriver instances.
     * Must be called before initializing the WebDriver.
     *
     * @param headless Whether to enable headless mode.
     */
    public static void setHeadless(boolean headless) {
        isHeadless = headless;
    }

    /**
     * Checks if headless mode is enabled.
     *
     * @return true if headless mode is enabled, false otherwise.
     */
    public static boolean isHeadless() {
        return isHeadless;
    }
}
