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

    // --- TUS DATOS ---
    private final String USUARIO = "gonza578.gm@gmail.com";
    private final String PASSWORD = "12345678"; 
    
    private final String REAL_CLIENT_ID = "71ac06b8a29a4de696a60e7b1569f2b0"; 
    private final String REAL_CLIENT_SECRET = "d880ef54bf0746d2b1870f0056a4a2cd";

    // Si deja de funcionar,  hay que actualizar este valor desde el navegador, cookie (sp_dc)
    private final String SPOTIFY_COOKIE_VALUE = "AQBSb0PwE-oMa-68X47tmMYZ96otXznzdhgt5pSWydFwV5BCby6YMJS5hxvPQx6D0o0kv7-_YapSTeFbeT6RQIBZC5Vr6-cJJrOYGpKHdPgdETdpwsv1Avi_NAv6YOKAkf9XBi2kR8IYBWAlRhZ8UFIK7YJbWBBxFU8zzoTerS07u4nKrp5qvnaHXvQO-oRV3r12O70qcMFAQAiy2w";

    @BeforeEach
    public void setUp() {
        if(userDao.existsById(USUARIO)) { //si existe ya un usuario con es ecorreo, lo eliminamos y creamos uno nuevo en la base de datos
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

        driver.get("https://accounts.spotify.com/en/login"); 

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
        // se cierra el navegador al acabar cada test
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    void testCompraCancionCorrecta() {
        System.out.println("primer test, compra correcta");
        
        realizarFlujoLoginYBusqueda(); //para loguearse y buscar la cancion

        rellenarStripe("4242424242424242", "1228", "123", "13071");

        WebElement btnPagar = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(),'Pagar Ahora')]")));
        btnPagar.click();
        
        wait.until(ExpectedConditions.invisibilityOf(btnPagar));
        
        // para asegurarnos de que el pago se ha realizado, buscamos en la base de datos
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

    // se rellenan datos inválidos
    rellenarStripe("4000 0000 0000 0000", "1228", "123", "13071");

    WebElement btnPagar = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(),'Pagar Ahora')]")));
    btnPagar.click();

    // buscamos por el TEXTO visible.
    WebElement errorMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(
        By.xpath("//*[contains(text(), 'no es válido')]")
    ));
    
    // Si pasa de la línea anterior, es que ha encontrado el mensaje.
    System.out.println("Error encontrado: " + errorMsg.getText()); //por lo tanto el test habria funcionado
    
    assertTrue(errorMsg.isDisplayed());//validamos que el mensaje se vea 
}

    private void realizarFlujoLoginYBusqueda() { //metodos de apoyo para ambos test
        // LOGIN
        System.out.println("Entrando en Gramola.");
        driver.get("http://127.0.0.1:4200/login");
        
        WebElement email = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email")));
        email.sendKeys(USUARIO);
        driver.findElement(By.id("password")).sendKeys(PASSWORD);
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        gestionarPermisosSpotifySiAparecen(); //sirve para gestionar los permisos de spotify si aparecen

        wait.until(ExpectedConditions.urlContains("/music"));
        System.out.println("✅ Estamos en /music.");

        try { Thread.sleep(2000); } catch (InterruptedException e) {}

        buscarCancion("Rock");//busca la cancion rock

        // se abre la tabla de resultados y se compra la primera cancion
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("table")));
        WebElement btnComprar = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//button[contains(text(),'Comprar')]")
        ));
        btnComprar.click();

        // se espera a que aparezca la pantalla de pago
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h3[contains(text(),'Confirmar Pago')]")));
    }

    private void gestionarPermisosSpotifySiAparecen() { //metodo para gestionar los permisos de spotify si aparecen
        try { Thread.sleep(1500); } catch (InterruptedException e) {} 
        if (driver.getCurrentUrl().contains("spotify")) {
            try {
                List<WebElement> btns = driver.findElements(By.xpath("//button[@data-testid='auth-accept' or contains(., 'Aceptar') or contains(., 'Agree')]"));
                if (!btns.isEmpty()) btns.get(0).click();
            } catch (Exception e) {}
        }
    }

    private void buscarCancion(String termino) {
        if (driver.getCurrentUrl().contains("spotify")) throw new RuntimeException("ERROR: Seguimos en Spotify.");

        WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("input[placeholder*='título']")
        ));
        input.clear();
        input.sendKeys(termino);
        
        WebElement btnBuscar = driver.findElement(By.xpath("//button[contains(text(),'Buscar')]"));
        btnBuscar.click();
        
        try { Thread.sleep(1500); } catch (InterruptedException e) {}
    }

    private void rellenarStripe(String tarjeta, String fecha, String cvc, String cp) {
        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.cssSelector("iframe[name^='__privateStripeFrame']")));
        
        WebElement card = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("cardnumber")));
        card.click();
        card.clear();
        try { Thread.sleep(500); } catch (InterruptedException e) {}
        
        for(char c : tarjeta.toCharArray()) {
            card.sendKeys(String.valueOf(c));
            try { Thread.sleep(50); } catch (InterruptedException e) {}
        }
        
        WebElement dateInput = driver.findElement(By.name("exp-date"));
        for(char c : fecha.toCharArray()) {
            dateInput.sendKeys(String.valueOf(c));
            try { Thread.sleep(50); } catch (InterruptedException e) {}
        }

        driver.findElement(By.name("cvc")).sendKeys(cvc);
        
        try {
            driver.findElement(By.name("postal")).sendKeys(cp);
        } catch (Exception e) {}
        
        driver.switchTo().defaultContent();
    }
}