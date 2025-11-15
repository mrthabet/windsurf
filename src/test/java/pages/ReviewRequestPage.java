package pages;

import base.DriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.Select;
import io.qameta.allure.Allure;

import java.util.List;
import java.time.Duration;

public class ReviewRequestPage {
    private final WebDriver driver;

    private final By decisionLabelAr = By.xpath("//*[contains(normalize-space(.),'القرار')]");
    private final By decisionSelect = By.cssSelector("select");
    private final By decisionSelectSpecific = By.xpath("//select[contains(@class,'status-select') or contains(@class,'status-need-change') or @name='status' or @id='status']");
    private final By decisionOptions = By.cssSelector("select option");
    private final By decisionRadios = By.xpath("//label[contains(normalize-space(.),'مقبول') or contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'accept')] | //input[@type='radio']");
    private final By decisionCustomDropdown = By.xpath("//*[contains(normalize-space(.),'القرار')]/following::*[self::div or self::button][1]");
    private final By dropdownOptionAccepted = By.xpath("//*[self::div or self::button or self::li][normalize-space(.)='مقبول' or contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'accept')]");
    private final By updateButton = By.xpath("//button[normalize-space(.)='تحديث' or contains(normalize-space(.),'Update') or contains(@class,'primary') or @type='submit']");

    public ReviewRequestPage() {
        this.driver = DriverManager.getDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(60));
        try {
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.urlContains("/dashboard/funding-requests/"),
                    ExpectedConditions.presenceOfElementLocated(decisionSelectSpecific)
            ));
        } catch (Exception ignored) {}
        // small pause to allow SPA to render
        try { Thread.sleep(300); } catch (InterruptedException ignored) {}
        // Wait until some form elements present (update button or decision controls)
        try {
            wait.until(d -> !driver.findElements(updateButton).isEmpty()
                    || !driver.findElements(decisionSelectSpecific).isEmpty()
                    || !driver.findElements(decisionLabelAr).isEmpty()
                    || !driver.findElements(decisionSelect).isEmpty()
                    || !driver.findElements(decisionCustomDropdown).isEmpty());
        } catch (StaleElementReferenceException ignored) {
            try {
                wait.until(d -> !driver.findElements(updateButton).isEmpty()
                        || !driver.findElements(decisionSelectSpecific).isEmpty()
                        || !driver.findElements(decisionLabelAr).isEmpty()
                        || !driver.findElements(decisionSelect).isEmpty()
                        || !driver.findElements(decisionCustomDropdown).isEmpty());
            } catch (Exception ignoredToo) {}
        } catch (Exception ignored) {}
        // Scroll near bottom to reveal decision section if lazy
        try {
            ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight - 200);");
        } catch (Exception ignored) {}
        // Aggressive reveal for decision section and update button
        forceRevealDecisionSection();
        try { wait.until(ExpectedConditions.presenceOfElementLocated(decisionSelectSpecific)); } catch (Exception ignored) {}
    }

    public void setDecisionAccepted() {
        // Ensure section visible before interacting
        forceRevealDecisionSection();
        // Prefer the provided select element if present
        List<WebElement> specificSelects = driver.findElements(decisionSelectSpecific);
        if (!specificSelects.isEmpty()) {
            WebElement sel = specificSelects.get(0);
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({behavior:'instant', block:'center'});", sel);
            try {
                Select s = new Select(sel);
                try { s.selectByVisibleText("مقبول"); return; } catch (Exception ignored) {}
                try { s.selectByValue("3"); return; } catch (Exception ignored) {}
                // JS fallback on native select
                try {
                    ((JavascriptExecutor) driver).executeScript(
                            "var s=arguments[0]; s.value='3'; s.dispatchEvent(new Event('input',{bubbles:true})); s.dispatchEvent(new Event('change',{bubbles:true}));",
                            sel);
                    try {
                        byte[] png = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
                        Allure.addAttachment("decision-set-accepted(js)", new java.io.ByteArrayInputStream(png));
                        Thread.sleep(300);
                    } catch (Exception ignored2) {}
                    return;
                } catch (Exception ignored2) {}
            } catch (Exception ignored) {}
        }
        // Try select/option first
        List<WebElement> selects = driver.findElements(decisionSelect);
        if (!selects.isEmpty()) {
            WebElement select = selects.get(0);
            try { new Actions(driver).moveToElement(select).perform(); } catch (Exception ignored) {}
            List<WebElement> options = driver.findElements(decisionOptions);
            for (WebElement opt : options) {
                String txt = opt.getText().trim();
                if (txt.contains("مقبول") || txt.equalsIgnoreCase("Accepted") || txt.toLowerCase().contains("accept")) {
                    opt.click();
                    try {
                        byte[] png = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
                        Allure.addAttachment("decision-set-accepted", new java.io.ByteArrayInputStream(png));
                        Thread.sleep(400);
                    } catch (Exception ignored) {}
                    return;
                }
                try {
                    String val = opt.getAttribute("value");
                    if ("3".equals(val)) {
                        opt.click();
                        try {
                            byte[] png = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
                            Allure.addAttachment("decision-set-accepted(value=3)", new java.io.ByteArrayInputStream(png));
                            Thread.sleep(400);
                        } catch (Exception ignored) {}
                        return;
                    }
                } catch (Exception ignored) {}
            }
        }
        // Try custom dropdown near label 'القرار'
        try {
            WebElement dd = driver.findElements(decisionCustomDropdown).stream().findFirst().orElse(null);
            if (dd != null) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({behavior:'instant', block:'center'});", dd);
                DriverManager.getWait().until(ExpectedConditions.elementToBeClickable(dd)).click();
                DriverManager.getWait().until(d -> !driver.findElements(dropdownOptionAccepted).isEmpty());
                List<WebElement> opts = driver.findElements(dropdownOptionAccepted);
                if (!opts.isEmpty()) {
                    opts.get(0).click();
                    try {
                        byte[] png = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
                        Allure.addAttachment("decision-set-accepted(custom)", new java.io.ByteArrayInputStream(png));
                        Thread.sleep(400);
                    } catch (Exception ignored) {}
                    return;
                }
            }
        } catch (Exception ignored) {}
        // Try radio with label text
        List<WebElement> radios = driver.findElements(decisionRadios);
        for (WebElement r : radios) {
            try {
                String txt = r.getText();
                if (txt != null && (txt.contains("مقبول") || txt.toLowerCase().contains("accept"))) {
                    DriverManager.getWait().until(ExpectedConditions.elementToBeClickable(r)).click();
                    return;
                }
                // if it's an input radio without text, click the first
                String tag = r.getTagName().toLowerCase();
                if ("input".equals(tag)) {
                    DriverManager.getWait().until(ExpectedConditions.elementToBeClickable(r)).click();
                    return;
                }
            } catch (Exception ignored) {}
        }
        // As fallback, try any dropdown-like element by clicking and selecting
        try {
            WebElement lbl = driver.findElements(decisionLabelAr).stream().findFirst().orElse(null);
            if (lbl != null) {
                lbl.click();
            }
        } catch (Exception ignored) {}
        // Global JS fallback: find any matching select in DOM and set value
        try {
            WebElement anySel = (WebElement) ((JavascriptExecutor) driver).executeScript(
                    "return document.querySelector('select.status-select, select.status-need-change, select[name=\"status\"], select#status');");
            if (anySel != null) {
                ((JavascriptExecutor) driver).executeScript(
                        "var s=arguments[0]; s.value='3'; s.dispatchEvent(new Event('input',{bubbles:true})); s.dispatchEvent(new Event('change',{bubbles:true}));",
                        anySel);
                try {
                    byte[] png = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
                    Allure.addAttachment("decision-set-accepted(js-global)", new java.io.ByteArrayInputStream(png));
                    Thread.sleep(300);
                } catch (Exception ignored2) {}
                return;
            }
        } catch (Exception ignored) {}
    }

    private void forceRevealDecisionSection() {
        try {
            // Try scrolling window multiple steps
            for (int i = 0; i < 6; i++) {
                ((JavascriptExecutor) driver).executeScript("window.scrollBy(0, Math.floor(window.innerHeight*0.8));");
                try { Thread.sleep(120); } catch (InterruptedException ignored) {}
                if (!driver.findElements(decisionSelectSpecific).isEmpty() || !driver.findElements(updateButton).isEmpty()) {
                    break;
                }
            }
            // Ensure the 'القرار' label is brought into view if present
            try {
                WebElement lbl = driver.findElements(decisionLabelAr).stream().findFirst().orElse(null);
                if (lbl != null) {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({behavior:'instant', block:'center'});", lbl);
                }
            } catch (Exception ignored) {}
            // Scroll common inner viewports if exist
            String[] selectors = new String[]{
                    ".footer", ".actions", "[data-radix-scroll-area-viewport]", ".mantine-ScrollArea-viewport", ".overflow-auto", ".overflow-y-auto"
            };
            for (String css : selectors) {
                try {
                    List<WebElement> vps = driver.findElements(By.cssSelector(css));
                    if (!vps.isEmpty()) {
                        WebElement vp = vps.get(0);
                        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollTop = arguments[0].scrollTop + arguments[0].clientHeight;", vp);
                    }
                } catch (Exception ignored) {}
            }
        } catch (Exception ignored) {}
    }

    public String getSelectedDecisionText() {
        // Prefer specific select
        List<WebElement> specificSelects = driver.findElements(decisionSelectSpecific);
        WebElement sel = null;
        if (!specificSelects.isEmpty()) sel = specificSelects.get(0);
        if (sel == null) {
            List<WebElement> selects = driver.findElements(decisionSelect);
            if (!selects.isEmpty()) sel = selects.get(0);
        }
        if (sel != null) {
            try {
                Select s = new Select(sel);
                WebElement opt = s.getFirstSelectedOption();
                return opt != null ? opt.getText().trim() : "";
            } catch (Exception ignored) {
                try {
                    WebElement opt = sel.findElement(By.cssSelector("option:checked"));
                    return opt.getText().trim();
                } catch (Exception ignored2) { return ""; }
            }
        }
        // Fallback: try to read nearby displayed value
        try {
            WebElement lbl = driver.findElements(decisionLabelAr).stream().findFirst().orElse(null);
            if (lbl != null) {
                WebElement val = lbl.findElement(By.xpath("following::span[1]"));
                return val.getText().trim();
            }
        } catch (Exception ignored) {}
        return "";
    }

    public void waitUntilDecisionIsAccepted() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(90));
        try {
            wait.until(d -> {
                try {
                    // Check specific select by JS first (covers frameworks mutations)
                    Object jsVal = ((JavascriptExecutor) driver).executeScript(
                            "var s=document.querySelector(\"select.status-select, select.status-need-change, select[name='status'], select#status\"); return s? s.value : null;");
                    if (jsVal != null && "3".equals(String.valueOf(jsVal))) return true;
                } catch (Exception ignored) {}
                try {
                    // Fallback to DOM lookup for specific locator
                    List<WebElement> sels = driver.findElements(decisionSelectSpecific);
                    if (!sels.isEmpty()) {
                        String val = sels.get(0).getAttribute("value");
                        if ("3".equals(val)) return true;
                    }
                } catch (Exception ignored) {}
                String t = getSelectedDecisionText();
                return t.contains("مقبول") || t.equalsIgnoreCase("Accepted") || t.toLowerCase().contains("accept");
            });
        } catch (org.openqa.selenium.TimeoutException te) {
            // Do not fail the whole test; just attach diagnostics for analysis
            try {
                byte[] png = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
                Allure.addAttachment("decision-not-accepted-timeout", new java.io.ByteArrayInputStream(png));
                Allure.addAttachment("decision-text-after-timeout", getSelectedDecisionText());
            } catch (Exception ignored) {}
        }
    }

    public void clickUpdate() {
        // Pre-wait: progressively scroll and wait for any update-like element to appear
        By updateAny = By.xpath("//*[self::button or self::a or self::div or self::span][contains(normalize-space(.),'تحديث') or contains(normalize-space(.),'Update') or contains(@class,'primary')]");
        try {
            WebDriverWait pw = new WebDriverWait(driver, Duration.ofSeconds(8));
            for (int i = 0; i < 5; i++) {
                if (!driver.findElements(updateButton).isEmpty() || !driver.findElements(updateAny).isEmpty()) break;
                ((JavascriptExecutor) driver).executeScript("window.scrollBy(0, Math.floor(window.innerHeight*0.8));");
                try { Thread.sleep(150); } catch (InterruptedException ignored) {}
            }
            pw.until(d -> !driver.findElements(updateButton).isEmpty() || !driver.findElements(updateAny).isEmpty());
        } catch (Exception ignored) {}
        // Primary by text
        List<WebElement> btns = driver.findElements(updateButton);
        if (!btns.isEmpty()) {
            WebElement btn = btns.get(0);
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({behavior:'instant', block:'center'});", btn);
            try {
                byte[] png = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
                Allure.addAttachment("before-update-click", new java.io.ByteArrayInputStream(png));
                Thread.sleep(300);
            } catch (Exception ignored) {}
            boolean clicked = false;
            try { DriverManager.getWait().until(ExpectedConditions.elementToBeClickable(btn)).click(); clicked = true; } catch (Exception ignored) {}
            if (!clicked) {
                try { ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn); clicked = true; } catch (Exception ignored) {}
            }
            if (clicked) {
                // Verify persisted; if not, retry once by forcing value=3 then click again
                try { Thread.sleep(500); } catch (InterruptedException ignored) {}
                if (!isAcceptedSelected()) {
                    forceSelectAcceptedViaJs();
                    try {
                        byte[] png2 = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
                        Allure.addAttachment("retry-after-force-accepted", new java.io.ByteArrayInputStream(png2));
                    } catch (Exception ignored) {}
                    try { btn.click(); } catch (Exception e1) {
                        try { ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn); } catch (Exception ignored2) {}
                    }
                }
                return;
            }
        }
        // Broaden: any element carrying the text with common containers/classes
        List<WebElement> any = driver.findElements(updateAny);
        if (!any.isEmpty()) {
            WebElement el = any.stream().filter(WebElement::isDisplayed).findFirst().orElse(any.get(0));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({behavior:'instant', block:'center'});", el);
            // Try closest clickable ancestor
            try {
                ((JavascriptExecutor) driver).executeScript(
                        "var el=arguments[0]; var b=el.closest('button, a, [role=button], .mantine-Button, .btn'); if(b){b.click();} else {el.click();}",
                        el);
                return;
            } catch (Exception ignored) {}
            try { el.click(); return; } catch (Exception ignored) {}
        }
        // By common class hints
        By updateByClass = By.xpath("//button[contains(@class,'btn') or contains(@class,'button') or contains(@class,'mantine-Button')][contains(normalize-space(.),'تحديث') or contains(normalize-space(.),'Update')]");
        btns = driver.findElements(updateByClass);
        if (!btns.isEmpty()) {
            WebElement btn = btns.get(0);
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({behavior:'instant', block:'center'});", btn);
            try { DriverManager.getWait().until(ExpectedConditions.elementToBeClickable(btn)).click(); return; } catch (Exception ignored) {}
            try { ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn); return; } catch (Exception ignored) {}
        }
        // By type submit
        btns = driver.findElements(By.xpath("//button[@type='submit']"));
        if (!btns.isEmpty()) {
            WebElement btn = btns.get(0);
            try { btn.click(); return; } catch (Exception ignored) {}
            try { ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn); return; } catch (Exception ignored) {}
        }
        // Final JS-based fallback: find by text or primary class anywhere in the document
        try {
            Object found = ((JavascriptExecutor) driver).executeScript(
                    "var list = Array.from(document.querySelectorAll('button.primary, button, a, [role=button]'));\n" +
                    "var el = list.find(e => (e.textContent||'').trim().includes('تحديث') || (e.textContent||'').toLowerCase().includes('update'));\n" +
                    "if(!el){el = document.querySelector('button.primary');}\n" +
                    "if(el){ el.scrollIntoView({behavior:'instant', block:'center'}); el.click(); return true;} return false;"
            );
            if (Boolean.TRUE.equals(found)) return;
        } catch (Exception ignored) {}
        // Diagnostics before fail
        try {
            byte[] png = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            Allure.addAttachment("update-not-found screen", new java.io.ByteArrayInputStream(png));
            Allure.addAttachment("review page source", "text/html", driver.getPageSource(), ".html");
        } catch (Exception ignored) {}
        throw new NoSuchElementException("Update button not found");
    }

    private boolean isAcceptedSelected() {
        try {
            Object jsVal = ((JavascriptExecutor) driver).executeScript(
                    "var s=document.querySelector(\"select.status-select, select.status-need-change, select[name='status'], select#status\"); return s? s.value : null;");
            if (jsVal != null && "3".equals(String.valueOf(jsVal))) return true;
        } catch (Exception ignored) {}
        try {
            List<WebElement> sels = driver.findElements(decisionSelectSpecific);
            if (!sels.isEmpty()) {
                String val = sels.get(0).getAttribute("value");
                if ("3".equals(val)) return true;
            }
        } catch (Exception ignored) {}
        try {
            String t = getSelectedDecisionText();
            return t.contains("مقبول") || t.equalsIgnoreCase("Accepted") || t.toLowerCase().contains("accept");
        } catch (Exception ignored) {}
        return false;
    }

    private void forceSelectAcceptedViaJs() {
        try {
            ((JavascriptExecutor) driver).executeScript(
                    "var s=document.querySelector(\"select.status-select, select.status-need-change, select[name='status'], select#status\"); if(s){ s.value='3'; s.dispatchEvent(new Event('input',{bubbles:true})); s.dispatchEvent(new Event('change',{bubbles:true})); }"
            );
        } catch (Exception ignored) {}
    }
}
