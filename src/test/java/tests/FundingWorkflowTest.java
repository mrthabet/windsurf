package tests;

import base.BaseTest;
import base.DriverManager;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.*;
import utils.ConfigReader;

public class FundingWorkflowTest extends BaseTest {

    @Test
    public void approveFirstFundingRequest() {
        // Login + OTP to Dashboard
        String email = ConfigReader.get("username");
        String password = ConfigReader.get("password");
        String otp = ConfigReader.get("otp");

        LoginPage login = new LoginPage();
        login.login(email, password);
        DriverManager.getWait().until(ExpectedConditions.urlContains("/send-otp"));
        SendOtpPage otpPage = new SendOtpPage();
        otpPage.completeOtp(otp);

        DashboardPage dashboard = new DashboardPage();
        dashboard.waitUntilLoaded();
        Assert.assertTrue(dashboard.isLoaded(), "Dashboard should be loaded before proceeding.");

        // Navigate to Funding Requests
        FundingRequestsPage funding = new FundingRequestsPage();
        funding.openFromSidebar();
        funding.openFirstRequestReview();

        // On Review page: set decision to Accepted and Update
        ReviewRequestPage review = new ReviewRequestPage();
        review.setDecisionAccepted();
        // Proceed to update directly; verify after update to avoid pre-update flakiness
        review.clickUpdate();
        // remain on details route and selection persists
        DriverManager.getWait().until(ExpectedConditions.urlContains("/dashboard/funding-requests/"));
        // Wait until accepted by value/text
        review.waitUntilDecisionIsAccepted();
        String after = review.getSelectedDecisionText();
        Assert.assertTrue(after.contains("مقبول") || after.equalsIgnoreCase("Accepted") || after.toLowerCase().contains("accept"),
                "Decision should be Accepted after update. Selected=" + after);

        // Basic post-condition: still authenticated and not on login/send-otp
        String url = DriverManager.getDriver().getCurrentUrl().toLowerCase();
        Assert.assertFalse(url.contains("/login") || url.contains("/send-otp"),
                "Should remain authenticated after update. Current url=" + url);
    }
}
