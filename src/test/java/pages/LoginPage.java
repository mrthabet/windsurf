package pages;

import base.DriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.time.Duration;
import java.util.List;

public class LoginPage {
    private final WebDriver driver;

    // Flexible locators to adapt to common login forms
    private final By emailInputCss = By.cssSelector("input[type='email'], input[name='email'], input[name='username'], input[id*='email' i], input[id*='user' i]");
    private final By passwordInputCss = By.cssSelector("input[type='password'], input[name='password'], input[id*='pass' i]");
    private final By submitButtonCss = By.cssSelector("button[type='submit'], button[id*='login' i], button[class*='login' i]");
    private final By submitButtonTextXpath = By.xpath("//button[normalize-space(.)='Login' or normalize-space(.)='Sign in' or normalize-space(.)='Sign In']");
    private final By errorAlert = By.cssSelector("[role='alert'], .error, .alert-error");

    public LoginPage() {
        this.driver = DriverManager.getDriver();
    }

    private WebElement waitForVisible(By locator) {
        return DriverManager.getWait().until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    private WebElement findFirst(By... locators) {
        for (By by : locators) {
            List<WebElement> found = driver.findElements(by);
            if (!found.isEmpty()) return found.get(0);
        }
        return null;
    }

    public void enterEmail(String email) {
        WebElement input = findFirst(emailInputCss);
        if (input == null) input = waitForVisible(emailInputCss);
        input.clear();
        input.sendKeys(email);
    }

    public void enterPassword(String password) {
        WebElement input = findFirst(passwordInputCss);
        if (input == null) input = waitForVisible(passwordInputCss);
        input.clear();
        input.sendKeys(password);
    }

    public void submit() {
        try {
            WebElement btn = findFirst(submitButtonCss, submitButtonTextXpath);
            if (btn != null && btn.isDisplayed() && btn.isEnabled()) {
                try {
                    DriverManager.getWait().until(ExpectedConditions.elementToBeClickable(btn)).click();
                    return;
                } catch (TimeoutException | ElementClickInterceptedException ignored) {}
            }
        } catch (Exception ignored) {}

        // Fallbacks: press ENTER on password, then on email
        try {
            WebElement pwd = findFirst(passwordInputCss);
            if (pwd == null) pwd = waitForVisible(passwordInputCss);
            pwd.sendKeys(Keys.ENTER);
            return;
        } catch (Exception ignored) {}

        try {
            WebElement email = findFirst(emailInputCss);
            if (email == null) email = waitForVisible(emailInputCss);
            email.sendKeys(Keys.ENTER);
        } catch (Exception ignored) {}
    }

    public void login(String email, String password) {
        enterEmail(email);
        enterPassword(password);
        submit();
        // Wait for either success (URL no longer contains /login) or error alert appears
        DriverManager.getWait().until(d -> {
            String url = driver.getCurrentUrl().toLowerCase();
            boolean leftLogin = !url.contains("/login");
            boolean hasError = !driver.findElements(errorAlert).isEmpty();
            return leftLogin || hasError;
        });
    }

    public void attemptLogin(String email, String password) {
        enterEmail(email);
        enterPassword(password);
        submit();
        // No wait here; caller will assert expected outcome (stay on /login or error shown)
    }

    public boolean hasError() {
        return !driver.findElements(errorAlert).isEmpty();
    }

    public String getErrorText() {
        List<WebElement> els = driver.findElements(errorAlert);
        return els.isEmpty() ? "" : els.get(0).getText();
    }
}
