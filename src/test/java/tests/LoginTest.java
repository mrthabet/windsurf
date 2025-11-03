package tests;

import base.BaseTest;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.LoginPage;
import utils.ConfigReader;
import base.DriverManager;
import org.openqa.selenium.support.ui.ExpectedConditions;
import pages.SendOtpPage;
import pages.DashboardPage;

public class LoginTest extends BaseTest {

    @Test
    public void testSuccessfulLogin() {
        String email = ConfigReader.get("username");
        String password = ConfigReader.get("password");
        String otp = ConfigReader.get("otp");

        LoginPage login = new LoginPage();
        login.login(email, password);
        // After successful login, app redirects to send-otp
        DriverManager.getWait().until(ExpectedConditions.urlContains("/send-otp"));
        // Complete OTP step
        SendOtpPage otpPage = new SendOtpPage();
        otpPage.completeOtp(otp);
        // Verify dashboard
        DashboardPage dashboard = new DashboardPage();
        dashboard.waitUntilLoaded();
        Assert.assertTrue(dashboard.isLoaded(), "Expected Dashboard to be loaded after OTP.");
    }
}
