package base;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.openqa.selenium.WebDriver;
import utils.ConfigReader;
import utils.DriverManager;

/**
 * Base test class that provides common setup and teardown methods for all test classes.
 * Manages WebDriver lifecycle and provides common configuration.
 */
public class BaseTest {
    protected WebDriver driver;
    protected static final long DEFAULT_TIMEOUT_SECONDS = 20;

    /**
     * Setup method that runs before each test method.
     * Initializes the WebDriver and navigates to the base URL.
     */
    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        // Get browser from system property or default to chrome
        String browser = System.getProperty("browser", "chrome");
        
        // Initialize WebDriver using DriverManager
        driver = DriverManager.initDriver(browser);
        
        // Navigate to base URL
        String baseUrl = ConfigReader.get("base.url");
        driver.get(baseUrl);
    }

    /**
     * Teardown method that runs after each test method.
     * Quits the WebDriver and performs cleanup.
     */
    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        DriverManager.quitDriver();
    }
}
