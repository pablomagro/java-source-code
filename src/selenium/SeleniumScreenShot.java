import java.io.File;
import java.net.URL;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import org.apache.commons.io.FileUtils;

/**
 * @see https://seleniumhq.github.io/selenium/docs/api/java/org/openqa/selenium/TakesScreenshot.html
 * @see http://docs.seleniumhq.org/docs/04_webdriver_advanced.jsp#taking-a-screenshot
 * @see https://github.com/detro/ghostdriver
 * 
 * @see http://tejasvijava.blogspot.co.nz/2013/01/screen-capture-using-phantomjs.html
 * 
 * Phantom JS options
 * https://github.com/ariya/phantomjs/wiki/API-Reference
 * 
 * Tips about using selenium Grid
 * http://code.google.com/p/robotframework-seleniumlibrary/wiki/UseSeleniumGRIDwithRobotFramework
 * 
 * Tip about setting a zoom factor
 * http://ariya.ofilabs.com/2012/10/web-page-screenshot-with-phantomjs.html
 */

public class SeleniumScreenShot {
   
   public File getScreenshot(String targetPath, WebDriver driver) throws Exception {
      String path;
      try {
         /*WebDriver driver = new RemoteWebDriver(
                  new URL("http://localhost:4444/wd/hub"),
                  DesiredCapabilities.firefox());
   
         driver.get("http://www.google.com");*/
   
         // RemoteWebDriver does not implement the TakesScreenshot class
         // if the driver does have the Capabilities to take a screenshot
         // then Augmenter will add the TakesScreenshot methods to the instance

         WebDriver augmentedDriver = new Augmenter().augment(driver);
         //driver = new Augmenter().augment(driver);
         File source = ((TakesScreenshot)augmentedDriver).getScreenshotAs(OutputType.FILE);
         path = targetPath + source.getName();
         System.out.println("File:" + path);
         FileUtils.copyFile(source, new File(path)); 
      }
      catch(IOException e) {
         path = "Failed to capture screenshot: " + e.getMessage();
      }
      return path;
   }
}
