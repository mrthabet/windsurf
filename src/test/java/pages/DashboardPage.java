package pages;

import base.BasePage;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

public class DashboardPage extends BasePage {

    private final By arabicHeading = By.xpath("//*[self::h1 or self::h2][contains(normalize-space(.),'لوحة التحكم')]");
    private final By englishHeading = By.xpath("//*[self::h1 or self::h2][contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'dashboard')]");
    private final By sidebar = By.cssSelector("aside, nav[aria-label*='sidebar' i]");
    private final By brandLogo = By.cssSelector("img[alt*='tarh' i], a[href*='dashboard' i]");

    public DashboardPage(WebDriver driver) {
        super(driver, 20L);
    }

    public void waitUntilLoaded() {
        // URL and at least one meaningful UI element of dashboard
        wait.until(ExpectedConditions.urlContains("/dashboard"));
        wait.until(d ->
                !driver.findElements(arabicHeading).isEmpty() ||
                !driver.findElements(englishHeading).isEmpty() ||
                !driver.findElements(sidebar).isEmpty() ||
                !driver.findElements(brandLogo).isEmpty()
        );
    }

    public boolean isLoaded() {
        String url = driver.getCurrentUrl().toLowerCase();
        boolean urlOk = url.contains("/dashboard");
        boolean uiOk = !driver.findElements(arabicHeading).isEmpty() ||
                !driver.findElements(englishHeading).isEmpty() ||
                !driver.findElements(sidebar).isEmpty() ||
                !driver.findElements(brandLogo).isEmpty();
        return urlOk && uiOk;
    }
}
