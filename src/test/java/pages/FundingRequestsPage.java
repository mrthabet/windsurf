package pages;

import base.BasePage;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import io.qameta.allure.Allure;

import java.util.List;
import java.time.Duration;

public class FundingRequestsPage extends BasePage {

    // Common locators
    private final By tableSelector = By.cssSelector("table, [role='table'], .mantine-Table-root, .mantine-DataTable-root");
    private final By rowsSelector = By.cssSelector("tbody tr, [role='rowgroup'] [role='row'], .table [class*='row'], .mantine-DataTable-row, [class*='DataTable-row'], [data-row-index], .mantine-Table-tbody tr");
    private final By reviewButtonText = By.xpath("//button[normalize-space(.)='مراجعة' or contains(normalize-space(.), 'Review') or contains(normalize-space(.), 'مراجعه') or normalize-space(.)='عرض'] | //a[normalize-space(.)='مراجعة' or normalize-space(.)='عرض']");
    private final By actionsCellButton = By.xpath("(//tbody/tr[1]//*[self::button or self::a][.//*[name()='svg'] or contains(., '...') or contains(@aria-label,'actions') or contains(@aria-label,'إجراءات') or contains(@class,'menu') or contains(@class,'actions')])[1] | (//*[@role='rowgroup']//*[@role='row'][1]//*[self::button or self::a][.//*[name()='svg'] or contains(., '...') or contains(@aria-label,'actions') or contains(@aria-label,'إجراءات') or contains(@class,'menu') or contains(@class,'actions')])[1] | (//*[@data-row-index][1]//*[self::button or self::a][.//*[name()='svg'] or contains(., '...') or contains(@aria-label,'actions') or contains(@aria-label,'إجراءات') or contains(@class,'menu') or contains(@class,'actions')])[1]");
    private final By actionsMenuTrigger = By.xpath("//button[contains(@aria-label,'actions') or contains(@aria-label,'إجراءات') or contains(@class,'menu') or contains(@class,'actions') or .='⋯' or .='...']");
    private final By firstRowLastCellButton = By.cssSelector("tbody tr:first-child td:last-child button, [role='rowgroup'] [role='row']:first-child [role='cell']:last-child button");
    private final By menuItemReview = By.xpath("(//*[self::button or self::a][normalize-space(.)='مراجعة' or contains(normalize-space(.),'Review')] | //button[@class='dropdown-item' and (normalize-space(.)='مراجعة' or contains(normalize-space(.),'Review'))] | //button[contains(@class,'dropdown-item') and (normalize-space(.)='مراجعة' or contains(normalize-space(.),'Review'))])");
    private final By headingFundRequests = By.xpath("//*[self::h1 or self::h2 or self::h3][contains(normalize-space(.),'طلبات التمويل')]");
    private final By reviewDecisionSelect = By.xpath("//select[contains(@class,'status-select') or contains(@class,'status-need-change') or @name='status' or @id='status']");

    public FundingRequestsPage(WebDriver driver) {
        super(driver);
    }

    public void openFromSidebar() {
        new Sidebar(driver).clickMenuByText("طلبات التمويل", "التمويل", "Requests", "Funding");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(40));
        // Wait for navigation to list (both variants)
        wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("/dashboard/fund-requests"),
                ExpectedConditions.urlContains("/dashboard/funding-requests")
        ));
        // Nudge scroll to ensure lazy content loads (page + inner viewport)
        try {
            ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, 0);");
            By viewport = By.cssSelector("[data-radix-scroll-area-viewport], .mantine-ScrollArea-viewport, .overflow-auto, .overflow-y-auto, .table-container, .scroll, .scrollbar");
            List<WebElement> vps = driver.findElements(viewport);
            if (!vps.isEmpty()) {
                WebElement vp = vps.get(0);
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollTop = 0;", vp);
            }
            // Explicit visible scroll down then up
            for (int i = 0; i < 3; i++) {
                ((JavascriptExecutor) driver).executeScript("window.scrollBy(0, Math.floor(window.innerHeight*0.9));");
                try { Thread.sleep(90); } catch (InterruptedException ignored) {}
            }
            ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, 0);");
            try {
                byte[] png = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
                Allure.addAttachment("fund-requests after scroll", new java.io.ByteArrayInputStream(png));
            } catch (Exception ignored) {}
        } catch (Exception ignored) {}
        // Wait for either rows, table, review buttons, or any row actions button
        By firstRowAnyButton = By.xpath("(//table//tbody//tr//button)[1] | (//*[@role='rowgroup']//*[@role='row']//button)[1] | (//*[@data-row-index][1]//button)[1]");
        wait.until(d -> !driver.findElements(rowsSelector).isEmpty()
                || !driver.findElements(tableSelector).isEmpty()
                || !driver.findElements(reviewButtonText).isEmpty()
                || !driver.findElements(firstRowAnyButton).isEmpty());
    }

    public void openFirstRequestReview() {
        // Pre-scan: scroll window and possible inner viewport to allow lazy content to render
        try {
            for (int i = 0; i < 4; i++) {
                ((JavascriptExecutor) driver).executeScript("window.scrollBy(0, Math.floor(window.innerHeight*0.7));");
                try { Thread.sleep(120); } catch (InterruptedException ignored) {}
                if (!driver.findElements(rowsSelector).isEmpty() || !driver.findElements(reviewButtonText).isEmpty()) break;
                By viewport = By.cssSelector("[data-radix-scroll-area-viewport], .mantine-ScrollArea-viewport, .overflow-auto, .overflow-y-auto, .table-container, .scroll, .scrollbar");
                List<WebElement> vps = driver.findElements(viewport);
                if (!vps.isEmpty()) {
                    WebElement vp = vps.get(0);
                    ((JavascriptExecutor) driver).executeScript("arguments[0].scrollTop = arguments[0].scrollTop + arguments[0].clientHeight;", vp);
                }
            }
        } catch (Exception ignored) {}
        // Try direct review button
        // Global text-based fallback first
        try {
            List<WebElement> globalActions = driver.findElements(By.xpath("//a[normalize-space(.)='عرض' or normalize-space(.)='مراجعة' or contains(normalize-space(.),'Review')] | //button[normalize-space(.)='عرض' or normalize-space(.)='مراجعة' or contains(normalize-space(.),'Review')]"));
            if (!globalActions.isEmpty()) {
                WebElement act = globalActions.stream().filter(WebElement::isDisplayed).findFirst().orElse(globalActions.get(0));
                scrollIntoViewCenter(act);
                waitAndClick(act, 20);
                try {
                    new WebDriverWait(driver, Duration.ofSeconds(10)).until(ExpectedConditions.or(
                            ExpectedConditions.presenceOfElementLocated(reviewDecisionSelect),
                            ExpectedConditions.urlContains("/dashboard/funding-requests/")
                    ));
                } catch (Exception ignored) {}
                return;
            }
        } catch (Exception ignored) {}
        // Try direct review button
        List<WebElement> reviews = driver.findElements(reviewButtonText);
        if (!reviews.isEmpty()) {
            waitAndClick(reviews.get(0), 20);
            try {
                new WebDriverWait(driver, Duration.ofSeconds(10)).until(ExpectedConditions.or(
                        ExpectedConditions.presenceOfElementLocated(reviewDecisionSelect),
                        ExpectedConditions.urlContains("/dashboard/funding-requests/")
                ));
            } catch (Exception ignored) {}
            return;
        }
        // Try explicit last-cell button in first row (ellipsis/actions)
        try {
            List<WebElement> lastCellBtns = driver.findElements(firstRowLastCellButton);
            if (!lastCellBtns.isEmpty()) {
                WebElement b = lastCellBtns.get(0);
                scrollIntoViewCenter(b);
                waitAndClick(b, 20);
                try { Thread.sleep(250); } catch (InterruptedException ignored) {}
                List<WebElement> menuReview = driver.findElements(menuItemReview);
                if (!menuReview.isEmpty()) {
                    waitAndClick(menuReview.get(0), 20);
                    try {
                        new WebDriverWait(driver, Duration.ofSeconds(10)).until(ExpectedConditions.or(
                                ExpectedConditions.presenceOfElementLocated(reviewDecisionSelect),
                                ExpectedConditions.urlContains("/dashboard/funding-requests/")
                        ));
                    } catch (Exception ignored) {}
                    return;
                }
                // Fallback: click first dropdown-item if text not matched
                List<WebElement> anyDropdownItem = driver.findElements(By.xpath("(//button[@class='dropdown-item' or contains(@class,'dropdown-item')])[1]"));
                if (!anyDropdownItem.isEmpty()) {
                    waitAndClick(anyDropdownItem.get(0), 20);
                    try {
                        new WebDriverWait(driver, Duration.ofSeconds(10)).until(ExpectedConditions.or(
                                ExpectedConditions.presenceOfElementLocated(reviewDecisionSelect),
                                ExpectedConditions.urlContains("/dashboard/funding-requests/")
                        ));
                    } catch (Exception ignored) {}
                    return;
                }
            }
        } catch (Exception ignored) {}
        // Try locating first row under the 'طلبات التمويل' heading and hover it to reveal actions
        try {
            WebElement heading = driver.findElements(headingFundRequests).stream().findFirst().orElse(null);
            if (heading != null) {
                WebElement firstRow = null;
                try {
                    firstRow = heading.findElement(By.xpath("following::*[self::table or @role='table'][1]//tbody/tr[1]"));
                } catch (Exception ignored) {}
                if (firstRow == null) {
                    firstRow = heading.findElement(By.xpath("following::*[@role='rowgroup'][1]//*[@role='row'][1]"));
                }
                if (firstRow != null) {
                    scrollIntoViewCenter(firstRow);
                    try { new org.openqa.selenium.interactions.Actions(driver).moveToElement(firstRow).perform(); } catch (Exception ignored) {}
                    List<WebElement> rowMenus = firstRow.findElements(By.xpath(".//*[self::button or self::a][.//*[name()='svg'] or contains(., '...') or contains(@aria-label,'actions') or contains(@aria-label,'إجراءات') or contains(@class,'menu') or contains(@class,'actions')]")).
                            stream().filter(e -> e.isDisplayed() && e.isEnabled()).toList();
                    if (!rowMenus.isEmpty()) {
                        WebElement rm = rowMenus.get(0);
                        waitAndClick(rm, 20);
                        try { Thread.sleep(250); } catch (InterruptedException ignored) {}
                        List<WebElement> menuReview = driver.findElements(menuItemReview);
                        if (!menuReview.isEmpty()) {
                            waitAndClick(menuReview.get(0), 20);
                            try {
                                new WebDriverWait(driver, Duration.ofSeconds(10)).until(ExpectedConditions.or(
                                        ExpectedConditions.presenceOfElementLocated(reviewDecisionSelect),
                                        ExpectedConditions.urlContains("/dashboard/funding-requests/")
                                ));
                            } catch (Exception ignored) {}
                            return;
                        }
                        List<WebElement> anyDropdownItem = driver.findElements(By.xpath("(//button[@class='dropdown-item' or contains(@class,'dropdown-item')])[1]"));
                        if (!anyDropdownItem.isEmpty()) {
                            waitAndClick(anyDropdownItem.get(0), 20);
                            try {
                                new WebDriverWait(driver, Duration.ofSeconds(10)).until(ExpectedConditions.or(
                                        ExpectedConditions.presenceOfElementLocated(reviewDecisionSelect),
                                        ExpectedConditions.urlContains("/dashboard/funding-requests/")
                                ));
                            } catch (Exception ignored) {}
                            return;
                        }
                    }
                }
            }
        } catch (Exception ignored) {}
        // Fallback: click actions menu in first row and choose Review
        WebElement btn = driver.findElements(actionsCellButton).stream()
                .filter(e -> e.isDisplayed() && e.isEnabled())
                .findFirst().orElse(null);
        if (btn != null) {
            scrollIntoViewCenter(btn);
            waitAndClick(btn, 20);
            try { Thread.sleep(250); } catch (InterruptedException ignored) {}
            List<WebElement> menuReview = driver.findElements(menuItemReview);
            if (!menuReview.isEmpty()) {
                new WebDriverWait(driver, Duration.ofSeconds(20)).until(ExpectedConditions.elementToBeClickable(menuReview.get(0))).click();
                try {
                    new WebDriverWait(driver, Duration.ofSeconds(10)).until(ExpectedConditions.or(
                            ExpectedConditions.presenceOfElementLocated(reviewDecisionSelect),
                            ExpectedConditions.urlContains("/dashboard/funding-requests/")
                    ));
                } catch (Exception ignored) {}
                return;
            }
            List<WebElement> anyDropdownItem = driver.findElements(By.xpath("(//button[@class='dropdown-item' or contains(@class,'dropdown-item')])[1]"));
            if (!anyDropdownItem.isEmpty()) {
                new WebDriverWait(driver, Duration.ofSeconds(20)).until(ExpectedConditions.elementToBeClickable(anyDropdownItem.get(0))).click();
                try {
                    new WebDriverWait(driver, Duration.ofSeconds(10)).until(ExpectedConditions.or(
                            ExpectedConditions.presenceOfElementLocated(reviewDecisionSelect),
                            ExpectedConditions.urlContains("/dashboard/funding-requests/")
                    ));
                } catch (Exception ignored) {}
                return;
            }
        }
        // As alternative, click generic button in first row
        btn = driver.findElements(actionsMenuTrigger).stream()
                .filter(e -> e.isDisplayed() && e.isEnabled())
                .findFirst().orElse(null);
        if (btn != null) {
            scrollIntoViewCenter(btn);
            waitAndClick(btn, 20);
            try { Thread.sleep(250); } catch (InterruptedException ignored) {}
            List<WebElement> menuReview = driver.findElements(menuItemReview);
            if (!menuReview.isEmpty()) {
                new WebDriverWait(driver, Duration.ofSeconds(20)).until(ExpectedConditions.elementToBeClickable(menuReview.get(0))).click();
                new WebDriverWait(driver, Duration.ofSeconds(20)).until(ExpectedConditions.urlContains("/dashboard/funding-requests/"));
                return;
            }
            return;
        }
        // Last-ditch JS fallbacks
        try {
            String script = "var b = document.querySelector(\"tbody tr button, [role='rowgroup'] [role='row'] button\"); if (b){b.scrollIntoView({behavior:'instant', block:'center'}); b.click(); true;} else {false;}";
            boolean clicked = Boolean.TRUE.equals(((JavascriptExecutor) driver).executeScript(script));
            if (clicked) {
                // Small wait for menu then click review
                WebDriverWait w = new WebDriverWait(driver, Duration.ofSeconds(5));
                w.until(d -> !driver.findElements(menuItemReview).isEmpty() || !driver.findElements(By.xpath("//button[@class='dropdown-item' or contains(@class,'dropdown-item')]"))
                        .isEmpty());
                List<WebElement> menuReview = driver.findElements(menuItemReview);
                if (!menuReview.isEmpty()) {
                    menuReview.get(0).click();
                    try {
                        new WebDriverWait(driver, Duration.ofSeconds(10)).until(ExpectedConditions.or(
                                ExpectedConditions.presenceOfElementLocated(reviewDecisionSelect),
                                ExpectedConditions.urlContains("/dashboard/funding-requests/")
                        ));
                    } catch (Exception ignored) {}
                    return;
                }
                List<WebElement> anyDropdownItem = driver.findElements(By.xpath("(//button[@class='dropdown-item' or contains(@class,'dropdown-item')])[1]"));
                if (!anyDropdownItem.isEmpty()) {
                    anyDropdownItem.get(0).click();
                    try {
                        new WebDriverWait(driver, Duration.ofSeconds(10)).until(ExpectedConditions.or(
                                ExpectedConditions.presenceOfElementLocated(reviewDecisionSelect),
                                ExpectedConditions.urlContains("/dashboard/funding-requests/")
                        ));
                    } catch (Exception ignored) {}
                    return;
                }
            }
        } catch (Exception ignored) {}
        // As last resort A: click first details link
        try {
            By firstDetailsLink = By.xpath("(//a[contains(@href,'/dashboard/funding-requests/') or normalize-space(.)='عرض'])[1]");
            List<WebElement> links = driver.findElements(firstDetailsLink);
            if (!links.isEmpty()) {
                WebElement link = links.get(0);
                scrollIntoViewCenter(link);
                waitAndClick(link, 20);
                new WebDriverWait(driver, Duration.ofSeconds(20)).until(ExpectedConditions.urlContains("/dashboard/funding-requests/"));
                return;
            }
        } catch (Exception ignored) {}
        // As last resort B: behave like a human on first row (hover -> click -> doubleClick)
        List<WebElement> rows = driver.findElements(rowsSelector);
        if (!rows.isEmpty()) {
            WebElement first = rows.get(0);
            scrollIntoViewCenter(first);
            try { new org.openqa.selenium.interactions.Actions(driver).moveToElement(first).perform(); } catch (Exception ignored) {}
            try {
                new WebDriverWait(driver, Duration.ofSeconds(20)).until(ExpectedConditions.elementToBeClickable(first)).click();
                // small pause, then double click to open details if supported
                try { Thread.sleep(200); } catch (InterruptedException ignored) {}
                new org.openqa.selenium.interactions.Actions(driver).doubleClick(first).perform();
            } catch (Exception ignored) {}
            try {
                new WebDriverWait(driver, Duration.ofSeconds(20)).until(ExpectedConditions.urlContains("/dashboard/funding-requests/"));
                return;
            } catch (Exception ignored) {}
        } else {
            // Global fallback: search whole page
            List<WebElement> anyReviews = driver.findElements(menuItemReview);
            if (!anyReviews.isEmpty()) {
                waitAndClick(anyReviews.get(0), 20);
                new WebDriverWait(driver, Duration.ofSeconds(20)).until(ExpectedConditions.urlContains("/dashboard/funding-requests/"));
                return;
            }
            List<WebElement> anyMenus = driver.findElements(actionsMenuTrigger);
            if (!anyMenus.isEmpty()) {
                WebElement m = anyMenus.get(0);
                scrollIntoViewCenter(m);
                waitAndClick(m, 20);
                try { Thread.sleep(250); } catch (InterruptedException ignored) {}
                List<WebElement> menuReview = driver.findElements(menuItemReview);
                if (!menuReview.isEmpty()) {
                    waitAndClick(menuReview.get(0), 20);
                    new WebDriverWait(driver, Duration.ofSeconds(20)).until(ExpectedConditions.urlContains("/dashboard/funding-requests/"));
                    return;
                }
            }
            try {
                byte[] png = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
                Allure.addAttachment("fund-requests screen (pre-fail)", new java.io.ByteArrayInputStream(png));
                Allure.addAttachment("fund-requests page source", "text/html", driver.getPageSource(), ".html");
                String url = driver.getCurrentUrl();
                Allure.addAttachment("current url", "text/plain", url);
            } catch (Exception ignored) {}
            throw new NoSuchElementException("No rows/actions/review found on funding requests page");
        }
    }

    private void waitAndClick(WebElement element, long timeoutSeconds) {
        try {
            waitUntilClickable(element, timeoutSeconds).click();
        } catch (Exception ignored) {}
    }
}
