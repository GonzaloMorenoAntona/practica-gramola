package edu.uclm.esi.gramola;

import static org.junit.jupiter.api.Assertions.assertTrue;


import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions; 
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import edu.uclm.esi.gramola.dao.StripeTransactionDao;
import edu.uclm.esi.gramola.dao.UserDao;
import edu.uclm.esi.gramola.model.StripeTransaction;
import edu.uclm.esi.gramola.model.Token;
import edu.uclm.esi.gramola.model.User;
import edu.uclm.esi.gramola.services.PasswordUtil;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestMethodOrder(OrderAnnotation.class)
public class GramolaSeleniumTest {

    @Autowired
    private StripeTransactionDao transactionDao;
    
    @Autowired
    private UserDao userDao;

    private WebDriver driver;
    private WebDriverWait wait;

    // datos para un usuraio de prueba
    private final String USUARIO = "gonza578.gm@gmail.com";
    private final String PASSWORD = "12345678"; 
    
    private final String REAL_CLIENT_ID = "71ac06b8a29a4de696a60e7b1569f2b0"; 
    private final String REAL_CLIENT_SECRET = "d880ef54bf0746d2b1870f0056a4a2cd";

    // Si deja de funcionar, hay que actualizar este valor desde el navegador, cookie (sp_dc)
    private final String SPOTIFY_COOKIE_VALUE = "AQBSb0PwE-oMa-68X47tmMYZ96otXznzdhgt5pSWydFwV5BCby6YMJS5hxvPQx6D0o0kv7-_YapSTeFbeT6RQIBZC5Vr6-cJJrOYGpKHdPgdETdpwsv1Avi_NAv6YOKAkf9XBi2kR8IYBWAlRhZ8UFIK7YJbWBBxFU8zzoTerS07u4nKrp5qvnaHXvQO-oRV3r12O70qcMFAQAiy2w";

    @BeforeEach
    //configura el entorno antes de cada test
    public void setUp() {
        if(userDao.existsById(USUARIO)) {
            userDao.deleteById(USUARIO);
        }

        User gonza = new User();
        gonza.setEmail(USUARIO);
        gonza.setPwd(PasswordUtil.hash(PASSWORD)); 
        gonza.setBarName("Bar Selenium");
        gonza.setClientId(REAL_CLIENT_ID);
        gonza.setClientSecret(REAL_CLIENT_SECRET);
        Token token = new Token();
        token.use(); 
        gonza.setCreationToken(token);
        userDao.save(gonza);

        driver = new ChromeDriver();
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, Duration.ofSeconds(30));

        driver.get("https://accounts.spotify.com/authorize"); 

        Cookie cookie = new Cookie.Builder("sp_dc", SPOTIFY_COOKIE_VALUE)
                .domain(".spotify.com")
                .path("/")
                .isSecure(true)
                .build();
        driver.manage().addCookie(cookie);
        
        driver.get("about:blank");
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    void testCompraCancionCorrecta() {
        System.out.println("primer test, compra correcta");
        
        realizarFlujoLoginYBusqueda(); 

        rellenarStripe("4242424242424242", "1228", "123", "13071");

        WebElement btnPagar = wait.until(ExpectedConditions.elementToBeClickable(By.id("submit-btn")));
        btnPagar.click();
        
        wait.until(ExpectedConditions.invisibilityOf(btnPagar));
        
        try { Thread.sleep(2000); } catch (InterruptedException e) {}
        List<StripeTransaction> pagos = transactionDao.findAll();
        assertTrue(pagos.size() > 0, "Fallo: No se guardó el pago.");
        System.out.println("TEST 1, el pago ha sido realizado.");
    }

   @Test
    @Order(2)
    public void testCompraCancionFallida() {
        System.out.println("test 2, compra fallida");

        realizarFlujoLoginYBusqueda(); 

        // Rellenamos con datos inválidos
        rellenarStripe("4000 0000 0000 0000", "1228", "123", "13071");

        WebElement btnPagar = wait.until(ExpectedConditions.elementToBeClickable(By.id("submit-btn")));
        btnPagar.click();
        

        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.id("card-error"), "no es válido"));
        
        WebElement errorMsg = driver.findElement(By.id("card-error"));
        
        System.out.println("Error encontrado: " + errorMsg.getText()); 
        
        assertTrue(errorMsg.isDisplayed());
    }

    private void realizarFlujoLoginYBusqueda() {
        System.out.println("Entrando en Gramola.");
        driver.get("http://127.0.0.1:4200/login");
        
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email"))).sendKeys(USUARIO);
        driver.findElement(By.id("password")).sendKeys(PASSWORD);
        driver.findElement(By.cssSelector("button[type='submit']")).click(); 

        wait.until(ExpectedConditions.urlContains("/music"));
        WebElement btnAbrir = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//button[contains(normalize-space(), 'Abrir Gramola')]")
        ));
        btnAbrir.click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("input[placeholder*='Escribe']")
        ));

        buscarCancion("Yellow"); 

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("table")));
        
        WebElement btnComprar = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//button[contains(text(),'Comprar')]")
        ));
        btnComprar.click();

        // Esperamos a que aparezca el título H3 dentro del form de pago 
        wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//h3[contains(text(),'Pagar Canción')]")
        ));
    }

    private void buscarCancion(String termino) {
        WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("input[placeholder*='título']")
        ));
        input.clear();
        input.sendKeys(termino);
        
        WebElement btnBuscar = driver.findElement(By.xpath("//button[contains(text(),'Buscar')]"));
        btnBuscar.click();
        
        try { Thread.sleep(1500); } catch (InterruptedException e) {}
    }

    // metodo para rellenar los datos de Stripe entrando en el iframe
    private void rellenarStripe(String tarjeta, String fecha, String cvc, String cp) {
        // esperar al iframe
        WebElement iframe = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("iframe[name^='__privateStripeFrame']")));
        // cambiar el contexto al iframe
        driver.switchTo().frame(iframe);
        
        // Inicializar Actions
        Actions actions = new Actions(driver);

        WebElement cardInput = wait.until(ExpectedConditions.elementToBeClickable(By.name("cardnumber")));
        // este es el click + SendKeys en la misma acción para forzar el foco
        actions.moveToElement(cardInput).click().sendKeys(tarjeta).perform();
        try { Thread.sleep(200); } catch (InterruptedException e) {}

        // fecha de caducidad
        WebElement dateInput = driver.findElement(By.name("exp-date"));
        actions.moveToElement(dateInput).click().sendKeys(fecha).perform();
        try { Thread.sleep(200); } catch (InterruptedException e) {}

        // cvc
        WebElement cvcInput = driver.findElement(By.name("cvc"));
        actions.moveToElement(cvcInput).click().sendKeys(cvc).perform();
        try { Thread.sleep(200); } catch (InterruptedException e) {}

        driver.switchTo().defaultContent();
    }
}