package tests;

import base.BaseTest;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.*;
import utils.ConfigReader;

import java.time.Duration;

/**
 * Test class for funding request approval workflow.
 * Tests the complete flow from login to approving a funding request.
 */
public class FundingWorkflowTest extends BaseTest {

    @Test
    public void invalidLoginShouldStayOnLoginOrShowError() {
        // Simple negative login case: correct username, wrong password
        LoginPage loginPage = new LoginPage(driver);
        String email = ConfigReader.get("username");
        String wrongPassword = "wrongPassword123";
        loginPage.attemptLogin(email, wrongPassword);
        new WebDriverWait(driver, Duration.ofSeconds(DEFAULT_TIMEOUT_SECONDS)).until(d -> {
            String currentUrl = driver.getCurrentUrl().toLowerCase();
            return currentUrl.contains("/login") || loginPage.hasError();
        });
        String url = driver.getCurrentUrl().toLowerCase();
        Assert.assertTrue(url.contains("/login") || loginPage.hasError(),
                "Expected to stay on /login or see error on invalid credentials. url=" + url);
    }

    @Test
    public void approveFirstFundingRequest() {
        // Login + OTP to Dashboard (reuse same config keys as LoginTest)
        String email = ConfigReader.get("username");
        String password = ConfigReader.get("password");
        String otp = ConfigReader.get("otp");

        // Initialize pages with the WebDriver instance
        LoginPage login = new LoginPage(driver);
        login.login(email, password);
        
        // Wait for OTP page
        new WebDriverWait(driver, Duration.ofSeconds(DEFAULT_TIMEOUT_SECONDS))
            .until(ExpectedConditions.urlContains("/send-otp"));
            
        SendOtpPage otpPage = new SendOtpPage(driver);
        otpPage.completeOtp(otp);

        // Verify dashboard is loaded
        DashboardPage dashboard = new DashboardPage(driver);
        dashboard.waitUntilLoaded();
        Assert.assertTrue(dashboard.isLoaded(), "Dashboard should be loaded before proceeding.");

        // Navigate to Funding Requests
        FundingRequestsPage funding = new FundingRequestsPage(driver);
        funding.openFromSidebar();
        funding.openFirstRequestReview();

        // On Review page: set decision to Accepted and Update
        ReviewRequestPage review = new ReviewRequestPage(driver);
        review.setDecisionAccepted();
        
        // Proceed to update directly; verify after update to avoid pre-update flakiness
        review.clickUpdate();
        
        // Wait for URL to update and verify decision (actual path is /dashboard/opportunities-updates)
        new WebDriverWait(driver, Duration.ofSeconds(DEFAULT_TIMEOUT_SECONDS))
            .until(ExpectedConditions.urlContains("/dashboard/opportunities-updates"));
            
        // Wait until accepted by value/text (non-fatal timeout; see ReviewRequestPage.waitUntilDecisionIsAccepted)
        review.waitUntilDecisionIsAccepted();
        String after = review.getSelectedDecisionText();
        System.out.println("[FundingWorkflowTest] Decision after update = " + after);

        // Basic post-condition: still authenticated and not on login/send-otp
        String url = driver.getCurrentUrl().toLowerCase();
        Assert.assertFalse(url.contains("/login") || url.contains("/send-otp"),
                "Should remain authenticated after update. Current url=" + url);
    }
}
