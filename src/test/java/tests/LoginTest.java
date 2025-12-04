package tests;

import base.BaseTest;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.LoginPage;
import utils.ConfigReader;
import pages.SendOtpPage;
import pages.DashboardPage;

import java.time.Duration;

public class LoginTest extends BaseTest {

    @Test(enabled = false)
    public void testSuccessfulLogin() {
        String email = ConfigReader.get("username");
        String password = ConfigReader.get("password");
        String otp = ConfigReader.get("otp");

        LoginPage login = new LoginPage(driver);
        login.login(email, password);
        // After successful login, app redirects to send-otp
        new WebDriverWait(driver, Duration.ofSeconds(BaseTest.DEFAULT_TIMEOUT_SECONDS))
                .until(ExpectedConditions.urlContains("/send-otp"));
        // Complete OTP step
        SendOtpPage otpPage = new SendOtpPage(driver);
        otpPage.completeOtp(otp);
        // Verify dashboard
        DashboardPage dashboard = new DashboardPage(driver);
        dashboard.waitUntilLoaded();
        Assert.assertTrue(dashboard.isLoaded(), "Expected Dashboard to be loaded after OTP.");
    }
}
