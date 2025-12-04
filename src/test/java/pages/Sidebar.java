package pages;

import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

public class Sidebar {
    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By sidebarRoot = By.cssSelector("aside, nav[aria-label*='sidebar' i], [class*='sidebar' i]");

    public Sidebar(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        this.wait.until(d -> !driver.findElements(sidebarRoot).isEmpty());
    }

    public void clickMenuByText(String... texts) {
        List<String> candidates = Arrays.asList(texts);
        // Search inside sidebar for any clickable element with matching text
        for (String t : candidates) {
            By item = By.xpath("//aside//*[self::a or self::button or self::div or self::span][contains(normalize-space(.), '" + t + "')] | " +
                    "//nav//*[self::a or self::button or self::div or self::span][contains(normalize-space(.), '" + t + "')]");

            List<WebElement> els = driver.findElements(item);
            if (!els.isEmpty()) {
                WebElement target = els.get(0);
                try { new Actions(driver).moveToElement(target).perform(); } catch (Exception ignored) {}
                wait.until(ExpectedConditions.elementToBeClickable(target)).click();
                return;
            }
        }
        throw new NoSuchElementException("Sidebar menu item not found for texts: " + String.join(",", candidates));
    }
}
