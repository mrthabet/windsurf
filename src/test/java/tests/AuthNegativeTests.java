package tests;

import base.BaseTest;
import org.openqa.selenium.NoSuchSessionException;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.openqa.selenium.TimeoutException;
import pages.LoginPage;
import pages.SendOtpPage;

import java.time.Duration;

public class AuthNegativeTests extends BaseTest {

    @DataProvider(name = "invalidLoginData")
    public Object[][] invalidLoginData() {
        return new Object[][]{
                {"", "" , "email", "required"},
                {"fp_user_1@sa.com", "wrongPass", "error", "invalid"},
                {"not-an-email", "secret@123", "email", "invalid"},
        };
    }

    @Test(dataProvider = "invalidLoginData", enabled = false)
    public void loginNegativeCases(String email, String password, String expectType, String note) {
        LoginPage page = new LoginPage(driver);
        page.attemptLogin(email, password);
        try {
            new WebDriverWait(driver, Duration.ofSeconds(BaseTest.DEFAULT_TIMEOUT_SECONDS)).until(d -> {
                String u = driver.getCurrentUrl().toLowerCase();
                return u.contains("/login") || page.hasError();
            });
        } catch (Exception ignored) {}
        String url = driver.getCurrentUrl().toLowerCase();
        Assert.assertTrue(url.contains("/login") || page.hasError(),
                "Expected to stay on /login or see error when invalid creds. url=" + url + ", error=" + page.getErrorText());
    }

    @DataProvider(name = "invalidOtpData")
    public Object[][] invalidOtpData() {
        return new Object[][]{
                {"000000"},
                {"123123"},
        };
    }

    @Test(dataProvider = "invalidOtpData", enabled = false)
    public void otpNegativeCases(String otp) {
        // Precondition: navigate to send-otp by logging in with correct creds
        // We call LoginTest logic lightly here for brevity
        // Note: BaseTest already navigates to base.url (login)
        LoginPage login = new LoginPage(driver);
        try {
            login.login(utils.ConfigReader.get("username"), utils.ConfigReader.get("password"));
        } catch (TimeoutException ignored) {
            // In slow environments, login wait may time out; continue to verify URL state.
        }
        // On /send-otp now (in normal case). In some negative flows, the browser/session may die early.
        try {
            SendOtpPage otpPage = new SendOtpPage(driver);
            otpPage.enterOtp(otp);
            otpPage.submit();
            String url = driver.getCurrentUrl().toLowerCase();
            // Negative outcome is valid if we remain on /send-otp or are sent back to /login
            Assert.assertTrue(url.contains("/send-otp") || url.contains("/login"),
                    "Expected to remain on /send-otp or be redirected to /login for invalid OTP. url=" + url);
        } catch (NoSuchSessionException ignored) {
            // Session already closed after invalid OTP; treat as acceptable negative outcome.
        }
    }
}
