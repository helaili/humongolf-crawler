package com.humongolf.crawler;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.humongolf.CrawlerContext;
import com.humongolf.entity.Ball;
import com.humongolf.entity.BallService;
import com.humongolf.entity.Benchmark;
import com.humongolf.entity.BenchmarkService;
import com.mongodb.WriteResult;

public abstract class Crawler {
	private static ChromeDriverService service;
	private static BallService ballService = BallService.getInstance();
	private static BenchmarkService benchmarkService = BenchmarkService.getInstance();
	private WebDriver driver;
	
	
	public static ChromeDriverService getService() {
		return service;
	}

	public WebDriver getDriver() {
		return driver;
	}

	@BeforeClass
	public static void createAndStartService() {
		service = new ChromeDriverService.Builder().usingDriverExecutable(new File("/Applications/chromedriver"))
				.usingAnyFreePort().build();
		
		try {
			service.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@AfterClass
	public static void createAndStopService() {
		service.stop();
	}

	@Before
	public void createDriver() {
		driver = new RemoteWebDriver(service.getUrl(), DesiredCapabilities.chrome());
	}

	@After
	public void quitDriver() {
		driver.quit();
	}
	
	
	protected void saveBall(Ball ball) {
		ballService.save(ball);
		
		if(ball.isUpserted() || CrawlerContext.loadImages()) {
			addImage(ball, "small");
			addImage(ball, "large");
		} 
	}
	
	
	protected void saveBenchmark(Benchmark benchmark) {
		benchmarkService.save(benchmark);
	}
	
	
	protected void updateBallPrice(String fullname, String source, double price) {
		ballService.updateBallPrice(fullname, source, price);
	}
	
	
	public void addImage(Ball ball, String size) {
		String url = null; 
		if(size.equals("small")) {
			url = ball.getSmallImageUrl();
		} else if(size.equals("large")) {
			url = ball.getLargeImageUrl();
		}  
		
		if(url == null) {
			return;
		}
		
		String extension = url.substring(url.lastIndexOf("."));
		if(extension.length() > 3) {
			extension = ".jpg";
		}
		String destFilename = ball.getFullname().replace(' ', '_').replace('\\', '_').replace('/', '_').replace('+', '_')  + "_" + size + extension;
	
		processImage(url, CrawlerContext.getImagePath() + "/" + ball.getSource() + "/" + destFilename);
		ballService.addImage(ball.getFullname(), size, ball.getSource() + "/" + destFilename);
	}
	
	protected void processImage(String imageUrl, String destFilename) {
		try {
			URL imgUrl = new URL(imageUrl);
			File imgDest = new File(destFilename);

			if (!imgDest.exists()) {
				org.apache.commons.io.FileUtils.copyURLToFile(imgUrl, imgDest);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
}
