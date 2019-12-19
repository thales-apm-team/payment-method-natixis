package com.payline.payment.natixis.integration;

import com.payline.payment.natixis.service.impl.PaymentServiceImpl;
import com.payline.payment.natixis.service.impl.PaymentWithRedirectionServiceImpl;
import com.payline.pmapi.bean.payment.request.PaymentRequest;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * This is an integration test class to validate the full payment process, via the partner API, using "Societe Generale" as ASPSP.
 * It must be run with several system property set on the JVM :
 * @see {@link com.payline.payment.natixis.integration.NatixisIT} class documentation
 *
 * It is not fully automated. At some point, the user must enter the SMS code manually in the web browser then click on
 * "Validate" button.
 */
public class SocieteGeneraleIT extends NatixisIT {

    private static final String SOGE_SANDBOX_SUCCESS = "https://particuliers.sandbox.societegenerale.fr/app/auth/icd/obu/index-authsec.html#obu/success";
    private static final String SOGE_SANDBOX_FAILURE = "https://particuliers.sandbox.societegenerale.fr/app/auth/icd/obu/index-authsec.html#obu/unsuccess";

    @Test
    @Override
    protected void run() {
        PaymentRequest paymentRequest = this.generatePaymentRequest();
        assertNotNull( paymentRequest );
        this.fullRedirectionPayment( paymentRequest, new PaymentServiceImpl(), new PaymentWithRedirectionServiceImpl() );
    }

    @Override
    protected String getDebtorAgent() {
        return "SOGEFRPPXXX";
    }

    @Override
    protected String payOnPartnerWebsite(String url) {
        // Start browser
        WebDriver driver = new ChromeDriver();
        driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);

        try {
            // Go to partner's website
            driver.get(url);

            // Fill the user id
            driver.findElement(By.id("user_id")).sendKeys("55000001");
            driver.findElement(By.id("btn-validate")).click();;

            // Wait for the user to enter SMS code (cannot be automated)
            WebDriverWait wait = new WebDriverWait(driver, 60);
            wait.until(ExpectedConditions.elementToBeClickable(By.id("validateBtn")));

            // Confirm payment
            driver.findElement(By.id("validateBtn")).click();
            wait.until(ExpectedConditions.elementToBeClickable(By.id("oob-btn-droite")));
            driver.findElement(By.id("oob-btn-droite")).click();

            // Wait for the result page to be displayed
            wait.until(ExpectedConditions.or(ExpectedConditions.urlToBe(SOGE_SANDBOX_SUCCESS), ExpectedConditions.urlToBe(SOGE_SANDBOX_FAILURE)));

            // The current sandbox does not perform the redirection so we mock it here
            return SOGE_SANDBOX_SUCCESS.equals(driver.getCurrentUrl()) ? SUCCESS_URL : CANCEL_URL;
        }
        finally {
            driver.quit();
        }
    }
}
