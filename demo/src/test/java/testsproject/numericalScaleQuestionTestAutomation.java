package testsproject;

import org.junit.Test;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import java.time.Duration;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.junit.Assert;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.JavascriptExecutor;

public class numericalScaleQuestionTestAutomation {

    private static EdgeDriver driver;
    private static WebDriverWait wait;

    private static WebElement waitForElementVisibility(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    private static WebElement scrollUntilElementVisible(By locator, String direction) {
        WebElement element = waitForElementVisibility(locator);
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(" + direction + ");", element);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } 
        return element;
    }

@BeforeClass
    public static void setUp() {
        driver =  new EdgeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        
        // Opens instructor's main page and login 
        driver.get("http://localhost:8080/web/instructor");
        driver.findElement(By.xpath("//*[@id='btn-login']")).click();

        // Opens sessions' page and creates a new one to start testing 
        driver.get("http://localhost:8080/web/instructor/sessions");
        waitForElementVisibility(By.xpath("//*[@id='btn-add-session']")).click();
        waitForElementVisibility(By.xpath("//*[@id='session-type']")).click();
        waitForElementVisibility(By.xpath("//*[@id='session-type']/option[3]")).click();
        waitForElementVisibility(By.xpath("//*[@id='add-session-name']")).click();
        waitForElementVisibility(By.xpath("//*[@id='add-session-name']")).sendKeys("Sessão teste");
        waitForElementVisibility(By.xpath("//*[@id='btn-create-session']")).click();
        waitForElementVisibility(By.xpath("/html/body/tm-root/tm-instructor-page/tm-page/tm-toast/ngb-toast/div/button")).click();
        
  }

@AfterClass
    public static void tearDown() {
        // Deletes the session that was created for the tests
        scrollUntilElementVisible(By.xpath("//*[@id='btn-fs-delete']"), "false").click();
        waitForElementVisibility(By.xpath("/html/body/ngb-modal-window/div/div/tm-confirmation-modal/div[4]/button[2]")).click();
        scrollUntilElementVisible(By.xpath("//*[@id='btn-delete-all']"), "true").click();
        waitForElementVisibility(By.xpath("/html/body/ngb-modal-window/div/div/tm-sessions-permanent-deletion-confirm-modal/div[3]/button[2]")).click();
        driver.quit();
  }

    private String createNumScaleQuestion(String brief, String description, long min, float increment, long max){
        
        waitForElementVisibility(By.xpath("//*[@id='btn-new-question']")).click();
        waitForElementVisibility(By.xpath("//*[@id='new-question-dropdown']/div[5]/div/button")).click();

        WebElement questionBrief = waitForElementVisibility(By.xpath("//*[@id='question-brief']"));
        questionBrief.click();
        questionBrief.sendKeys(brief);

        driver.switchTo().frame(1);
        WebElement questionDescription = waitForElementVisibility(By.xpath("//*[@id='tinymce']"));
        questionDescription.click();
        questionDescription.sendKeys(description);
        driver.switchTo().defaultContent();

        WebElement questionMinValue = waitForElementVisibility(By.xpath("//*[@id='min-value']"));
        questionMinValue.clear();
        questionMinValue.click();
        questionMinValue.sendKeys(Long.toString(min));
        
        WebElement questionIncrementValue = waitForElementVisibility(By.xpath("//*[@id='increment-value']"));
        questionIncrementValue.clear();
        questionIncrementValue.click();
        questionIncrementValue.sendKeys(Float.toString(increment));

        WebElement questionMaxValue = waitForElementVisibility(By.xpath("//*[@id='max-value']"));
        questionMaxValue.clear();
        questionMaxValue.click();
        questionMaxValue.sendKeys(Long.toString(max));

        waitForElementVisibility(By.xpath("//*[@id='question-form-1']/div/div/div[2]/div/button[2]")).click();
        String output = waitForElementVisibility(By.xpath("/html/body/tm-root/tm-instructor-page/tm-page/tm-toast/ngb-toast/div")).getText();
        waitForElementVisibility(By.xpath("/html/body/tm-root/tm-instructor-page/tm-page/tm-toast/ngb-toast/div/button")).click();
        
        
        if (output.equals("The question has been added to this feedback session.")) {
            scrollUntilElementVisible(By.xpath("//*[@id='btn-delete-question']"), "false").click();
            waitForElementVisibility(By.xpath("/html/body/ngb-modal-window/div/div/tm-confirmation-modal/div[4]/button[2]")).click();
        } else {
            scrollUntilElementVisible(By.xpath("//*[@id='question-form-1']/button/div[2]/div[2]/div/button"), "false").click();
            waitForElementVisibility(By.xpath("/html/body/ngb-modal-window/div/div/tm-confirmation-modal/div[4]/button[2]")).click();
        }
        
        return output;
    }

    @Test
    public void validValuesDescription() {
        String output = createNumScaleQuestion("Pergunta 1", "Descrição da pergunta", -2147483648, 1, 2147483647);
        Assert.assertEquals("The question has been added to this feedback session.", output);
    }
    
    @Test
    public void validValuesNoDescription() {
        String output = createNumScaleQuestion("Pergunta 2", "", -2147483648, 1, 2147483647);
        Assert.assertEquals("The question has been added to this feedback session.", output);
    }

    @Test
    public void invalidMinValue() {
        String output = createNumScaleQuestion("Pergunta 3", "Descrição da pergunta", -2147483649L, 1, 2147483647);
        Assert.assertEquals("The server encountered an error when processing your request.", output);
    }

    @Test
    public void invalidIncrementValue() {
        String output = createNumScaleQuestion("Pergunta 4", "Descrição da pergunta", -2147483648, 0, 2147483647);
        Assert.assertEquals("[Step value must be > 0 for Numerical-scale question.]", output);
    }

    @Test
    public void invalidMaxValue() {
        String output = createNumScaleQuestion("Pergunta 5", "Descrição da pergunta", -2147483648, 1, 2147483648L);
        Assert.assertEquals("The server encountered an error when processing your request.", output);
    }

    @Test
    public void minEqualsMax() {
        String output = createNumScaleQuestion("Pergunta 6", "Descrição da pergunta", 5, 1, 5);
        Assert.assertEquals("[Minimum value must be < maximum value for Numerical-scale question.]", output);
    }

    @Test
    public void emptyBrief() {
        String output = createNumScaleQuestion("", "Descrição da pergunta", -2147483648, 1, 2147483647);
        Assert.assertEquals("Question brief cannot be empty", output);
    }
    
}
