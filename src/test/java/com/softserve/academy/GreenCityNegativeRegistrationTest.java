package com.softserve.academy;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import org.openqa.selenium.chrome.ChromeOptions;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GreenCityNegativeRegistrationTest {
    private static WebDriver driver;

    @BeforeAll
    static void setUp() {
        ChromeOptions options = new ChromeOptions();
        // Check if we are running in CI (GitHub Actions)
        if (System.getenv("GITHUB_ACTIONS") != null) {
            options.addArguments("--headless=new");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--window-size=1920,1080");
        }
        
        driver = WebDriverManager.chromedriver().capabilities(options).create();
        driver.manage().window().maximize();
        // At this stage, we are not using complex waits, so we just maximize the window
    }

    @BeforeEach
    void openRegistrationForm() throws InterruptedException {
        // 1. Open the main page
        driver.navigate().to("https://www.greencity.cx.ua/#/greenCity");
        
        // Bad practice: using a delay to allow the page to load completely.
        // This is necessary because the site may load slowly.
        Thread.sleep(5000);

        // 2. Click the "Sign Up" button to open the modal window
        driver.findElement(By.cssSelector(".header_sign-up-btn > span")).click();

        // Bad practice: using a delay to allow the modal window to open.
        Thread.sleep(2000);
    }

    // --- TESTS ---

    @Test
    @DisplayName("Invalid email format (without @) → email error")
    void shouldShowErrorForInvalidEmail() throws InterruptedException {
        // One test = one reason for failure. Other fields must be valid.
        typeEmail("invalid-email");
        typeUsername("ValidUsername");
        typePassword("ValidPass123!");
        typeConfirm("ValidPass123!");

        // Give the system some time to validate and display the error
        Thread.sleep(1000);

        // Check that the error for email appeared
        assertEmailErrorVisible(getEmailErrorMessageElement());
        // Check that the registration button is disabled (or registration did not occur)
        assertSignUpButtonDisabled();
    }

    @Test
    @DisplayName("All fields empty → required errors shown")
    void shouldShowErrorsForAllEmptyFields() throws InterruptedException {
        typeEmail("");
        typeUsername("");
        typePassword("");
        typeConfirm("");

        clickSignUp();
        Thread.sleep(1000);

        assertRequiredError(getEmailErrorMessageElement());
        assertRequiredError(getUserNameErrorMessageElement());
        assertPasswordErrorVisible(getPasswordErrorMessageElement());
        assertRequiredError(getConfirmPasswordErrorMessageElement());

        assertSignUpButtonDisabled();
    }

    @Test
    @DisplayName("Empty username → username required")
    void shouldShowErrorForEmptyUsername() throws InterruptedException {
        typeEmail("test@gmail.com");
        typeUsername("");
        typePassword("ValidPass123!");
        typeConfirm("ValidPass123!");

        clickSignUp();
        Thread.sleep(1000);

        assertRequiredError(getUserNameErrorMessageElement());
        assertSignUpButtonDisabled();
    }

    @Test
    @DisplayName("Short password (<8) → password rule error")
    void shouldShowErrorForShortPassword() throws InterruptedException {
        typeEmail("test@gmail.com");
        typeUsername("ValidUsername");
        typePassword("1234567");
        typeConfirm("1234567");

        Thread.sleep(1000);

        assertPasswordErrorVisible(getPasswordErrorMessageElement());
        assertSignUpButtonDisabled();
    }

    @Test
    @DisplayName("Password with space → password rule error")
    void shouldShowErrorForPasswordWithSpace() throws InterruptedException {
        typeEmail("test@gmail.com");
        typeUsername("ValidUsername");
        typePassword("Pass 1234");
        typeConfirm("Pass 1234");

        Thread.sleep(1000);

        assertPasswordErrorVisible(getPasswordErrorMessageElement());
        assertSignUpButtonDisabled();
    }

    @Test
    @DisplayName("Confirm password mismatch → confirm error")
    void shouldShowErrorForPasswordMismatch() throws InterruptedException {
        typeEmail("test@gmail.com");
        typeUsername("ValidUsername");
        typePassword("ValidPass123!");
        typeConfirm("DifferentPass123!");

        Thread.sleep(1000);

        clickSignUp();

        assertConfirmPasswordErrorVisible();
        assertSignUpButtonDisabled();
    }

    // --- HELPERS (Helper methods) ---
    // This is the first step towards structuring code before learning Page Object

    private void typeEmail(String value) {
        WebElement field = driver.findElement(By.id("email"));
        field.clear();
        field.sendKeys(value);
    }

    private void typeUsername(String value) {
        WebElement field = driver.findElement(By.id("firstName"));
        field.clear();
        field.sendKeys(value);
    }

    private void typePassword(String value) {
        WebElement field = driver.findElement(By.id("password"));
        field.clear();
        field.sendKeys(value);
    }

    private void typeConfirm(String value) {
        WebElement field = driver.findElement(By.id("repeatPassword"));
        field.clear();
        field.sendKeys(value);
    }

    private void clickSignUp() {
        driver.findElement(By.cssSelector("button[type='submit'].greenStyle")).click();
    }

    private WebElement getEmailErrorMessageElement() { return driver.findElement(By.id("email-err-msg")); }

    private WebElement getUserNameErrorMessageElement() { return driver.findElement(By.id("firstname-err-msg")); }

    private WebElement getPasswordErrorMessageElement() { return driver.findElement(By.cssSelector("p.password-not-valid")); }

    private WebElement getConfirmPasswordErrorMessageElement() { return driver.findElement(By.id("confirm-err-msg")); }

    private void assertEmailErrorVisible(WebElement errorElement) {
        assertTrue(errorElement.isDisplayed(), "Email error message should be visible");
        // contains("required") or other text to avoid dependency on the full phrase
        assertTrue(getEmailErrorMessageElement().getText().toLowerCase().contains("check") ||
                getEmailErrorMessageElement().getText().toLowerCase().contains("correctly"));
    }

    private void assertRequiredError(WebElement errorElement) {
        assertTrue(errorElement.isDisplayed(), "Email error message should be visible");
        assertTrue(errorElement.getText().toLowerCase().contains("required"));
    }

    private void assertPasswordErrorVisible(WebElement errorElement) {
        assertTrue(errorElement.isDisplayed(), "Password error should be visible");

        String text = errorElement.getText().toLowerCase();

        assertTrue(
                text.contains("8") || text.contains("characters") || text.contains("uppercase"),
                "Unexpected password error message: " + text
        );
    }

    private void assertConfirmPasswordErrorVisible() {
        assertTrue(getConfirmPasswordErrorMessageElement().isDisplayed(), "Email error message should be visible");
        assertTrue(getConfirmPasswordErrorMessageElement().getText().toLowerCase().contains("do not match"));
    }

    private void assertSignUpButtonDisabled() {
        WebElement btn = driver.findElement(By.cssSelector("button[type='submit'].greenStyle"));
        assertFalse(btn.isEnabled(), "The 'Sign Up' button should be disabled with invalid data");
    }

    @AfterAll
    static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
