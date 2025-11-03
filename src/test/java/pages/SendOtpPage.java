package pages;

import base.DriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.ArrayList;
import java.util.List;

public class SendOtpPage {
    private final WebDriver driver;

    // Flexible locators for OTP inputs and verify/continue button
    private final By singleOtpInput = By.cssSelector("input[name*='otp' i], input[id*='otp' i], input[autocomplete='one-time-code'], input[type='tel']");
    private final By otpDigitInputs = By.cssSelector("input[maxlength='1'], input[aria-label*='digit' i]");
    private final By verifyButtonCss = By.cssSelector("button[type='submit'], button[class*='verify' i], button[class*='continue' i]");
    private final By verifyButtonTextXpath = By.xpath("//button[normalize-space(.)='Verify' or normalize-space(.)='Continue' or normalize-space(.)='Submit']");

    public SendOtpPage() {
        this.driver = DriverManager.getDriver();
        // Ensure we are on /send-otp route before interacting
        DriverManager.getWait().until(ExpectedConditions.urlContains("/send-otp"));
    }

    private WebElement waitVisible(By locator) {
        return DriverManager.getWait().until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    public void enterOtp(String otp) {
        List<WebElement> digits = new ArrayList<>();
        // Try multiple inputs style
        List<WebElement> candidates = driver.findElements(otpDigitInputs);
        for (WebElement el : candidates) {
            try {
                if (el.isDisplayed() && el.isEnabled()) digits.add(el);
            } catch (StaleElementReferenceException ignored) {}
        }
        if (digits.size() >= otp.length()) {
            for (int i = 0; i < otp.length(); i++) {
                WebElement el = digits.get(i);
                el.clear();
                el.sendKeys(String.valueOf(otp.charAt(i)));
            }
            return;
        }
        // Fallback: single input
        WebElement input = driver.findElements(singleOtpInput).stream()
                .filter(e -> e.isDisplayed() && e.isEnabled())
                .findFirst()
                .orElse(waitVisible(singleOtpInput));
        input.clear();
        input.sendKeys(otp);
    }

    public void submit() {
        WebElement btn = driver.findElements(verifyButtonCss).stream()
                .filter(e -> e.isDisplayed() && e.isEnabled())
                .findFirst()
                .orElseGet(() -> driver.findElements(verifyButtonTextXpath).stream()
                        .filter(e -> e.isDisplayed() && e.isEnabled())
                        .findFirst().orElse(null));
        if (btn != null) {
            DriverManager.getWait().until(ExpectedConditions.elementToBeClickable(btn)).click();
        } else {
            // Press Enter as fallback
            try {
                WebElement input = waitVisible(singleOtpInput);
                input.sendKeys(Keys.ENTER);
            } catch (Exception ignored) {}
        }
    }

    public void completeOtp(String otp) {
        enterOtp(otp);
        submit();
        // Wait until we leave /send-otp
        DriverManager.getWait().until(d -> !driver.getCurrentUrl().toLowerCase().contains("/send-otp"));
    }
}
