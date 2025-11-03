package tests;

import base.BaseTest;
import base.DriverManager;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import pages.LoginPage;
import pages.SendOtpPage;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class AuthNegativeTests extends BaseTest {

    @DataProvider(name = "invalidLoginData")
    public Object[][] invalidLoginData() {
        return new Object[][]{
                {"", "" , "email", "required"},
                {"fp_user_1@sa.com", "wrongPass", "error", "invalid"},
                {"not-an-email", "secret@123", "email", "invalid"},
        };
    }

    @Test(dataProvider = "invalidLoginData")
    public void loginNegativeCases(String email, String password, String expectType, String note) {
        LoginPage page = new LoginPage();
        page.attemptLogin(email, password);
        try {
            DriverManager.getWait().until(d -> {
                String u = DriverManager.getDriver().getCurrentUrl().toLowerCase();
                return u.contains("/login") || page.hasError();
            });
        } catch (Exception ignored) {}
        String url = DriverManager.getDriver().getCurrentUrl().toLowerCase();
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

    @Test(dataProvider = "invalidOtpData")
    public void otpNegativeCases(String otp) {
        // Precondition: navigate to send-otp by logging in with correct creds
        // We call LoginTest logic lightly here for brevity
        // Note: BaseTest already navigates to base.url (login)
        LoginPage login = new LoginPage();
        login.login(utils.ConfigReader.get("username"), utils.ConfigReader.get("password"));
        // On /send-otp now
        SendOtpPage otpPage = new SendOtpPage();
        otpPage.enterOtp(otp);
        otpPage.submit();
        String url = DriverManager.getDriver().getCurrentUrl().toLowerCase();
        // Expect to remain on /send-otp when OTP invalid
        Assert.assertTrue(url.contains("/send-otp"), "Expected to remain on /send-otp for invalid OTP. url=" + url);
    }
}
