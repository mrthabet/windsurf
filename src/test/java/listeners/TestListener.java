package listeners;

import base.DriverManager;
import io.qameta.allure.Allure;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

public class TestListener implements ITestListener {

    @Override
    public void onTestFailure(ITestResult result) {
        try {
            // Screenshot
            byte[] screenshot = ((TakesScreenshot) DriverManager.getDriver()).getScreenshotAs(OutputType.BYTES);
            Allure.addAttachment("Screenshot (failure)", new ByteArrayInputStream(screenshot));
        } catch (Exception ignored) {}

        try {
            // Page source
            String html = DriverManager.getDriver().getPageSource();
            Allure.addAttachment("Page Source", "text/html", html, ".html");
        } catch (Exception ignored) {}

        try {
            // Browser console logs (Chrome)
            String logs = DriverManager.getBrowserConsoleLogs();
            Allure.addAttachment("Browser Console Logs", "text/plain",
                    new ByteArrayInputStream(logs.getBytes(StandardCharsets.UTF_8)), ".log");
        } catch (Exception ignored) {}
    }

    @Override
    public void onFinish(ITestContext context) {
        // Place to aggregate or finalize if needed
    }
}
