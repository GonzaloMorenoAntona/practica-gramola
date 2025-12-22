package edu.uclm.esi.gramola;



import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.devtools.idealized.Javascript;





public class Scrapping {
    public static void main(String[] args) throws InterruptedException {
        WebDriver driver = new ChromeDriver();
        JavascriptExecutor js = (JavascriptExecutor) driver;
        driver.get("https://mercadona.es/");
        driver.manage().window().maximize();

   

        WebElement btnCookies = driver.findElement(By.xpath( "/html/body/div[2]/div/div/div/button[3]"));
        btnCookies.click();

        WebElement cajaCodigoPostal = driver.findElement(By.xpath( "/html/body/div[2]/header/div/div/form/div/input"));
        cajaCodigoPostal.sendKeys("13001");

        WebElement btnAceptar = driver.findElement(By.xpath( "/html/body/div[2]/header/div/div/form/input"));
        btnAceptar.click();
        Thread.sleep(2000);

        List<WebElement> pcc = driver.findElements(By.className("product-cell-container"));

        for (WebElement pc : pcc) {
            WebElement h4 = pc.findElement(By.tagName("h4"));
            System.out.println(h4.getText());
            
            
        }


    }
}
