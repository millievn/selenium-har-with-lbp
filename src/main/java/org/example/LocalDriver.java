package org.example;

import io.github.qiwang97.LightBrowserProxyServer;
import io.reactivex.rxjava3.disposables.Disposable;
import net.lightbody.bmp.proxy.CaptureType;
import org.openqa.selenium.By;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;


public class LocalDriver {

	private static final Logger log = LoggerFactory.getLogger(LocalDriver.class);


	public static void setup(Logger logger, String type) throws InterruptedException, MalformedURLException {
		WebDriver driver = null;
		LightBrowserProxyServer browserMobProxy = null;
		Disposable listener = null;
		try {

			browserMobProxy = new LightBrowserProxyServer();

			// enable more detailed HAR capture, if desired (see CaptureType for the complete list)
			browserMobProxy.enableHarCaptureTypes(CaptureType.REQUEST_CONTENT, CaptureType.RESPONSE_CONTENT, CaptureType.RESPONSE_BINARY_CONTENT, CaptureType.REQUEST_HEADERS);
			browserMobProxy.start();

			// setup proxy to listen network
			Proxy proxy = new Proxy();
			proxy.setProxyType(Proxy.ProxyType.MANUAL);
			String proxyStr;

			if ("remote".equals(type)) {
				// NOTE: this address is only work for Mac
				// more detail please see https://medium.com/@TimvanBaarsen/how-to-connect-to-the-docker-host-from-inside-a-docker-container-112b4c71bc66
				proxyStr = "http://docker.for.mac.host.internal:" + browserMobProxy.getPort();
			} else {
				proxyStr = "http://localhost:" + browserMobProxy.getPort();
			}

			proxy.setHttpProxy(proxyStr);
			proxy.setSslProxy(proxyStr);

			// set args to control chrome
			ChromeOptions options = new ChromeOptions();
			options.setCapability("proxy", proxy);
			options.addArguments("headless");
			options.addArguments("--no-sandbox");
			options.addArguments("--disable-gpu");
			options.addArguments("--allow-insecure-localhost");
			options.setAcceptInsecureCerts(true);

			if ("remote".equals(type)) {
				driver = new RemoteWebDriver(new URL("http://localhost:4444"), options);
			} else {
				driver = new ChromeDriver(options);
			}

			// listen stream harEntry
			listener = browserMobProxy.$entries.subscribe(entry -> {
						logger.info("receive entry size {}", entry.getResponse().getBodySize());
					},
					error -> {
						logger.info("catch error", error);
					}, () -> {
						logger.info("job done");
					});

			browserMobProxy.newHar("github");

			// open yahoo.com
			driver.get("https://www.github.com");

			// wait page load
			new WebDriverWait(driver, Duration.ofSeconds(3))
					.until(dv -> dv.findElement(By.cssSelector("header.Header")));
		} finally {
			if (listener != null) {
				listener.dispose();
			}

			if (browserMobProxy != null) {
				browserMobProxy.stop();
			}

			if (driver != null) {
				driver.quit();
			}
		}
	}


	public static void main(String[] args) throws InterruptedException, MalformedURLException {
		System.setProperty("webdriver.chrome.driver", "path/to/driver");
		setup(log, "local");
	}
}