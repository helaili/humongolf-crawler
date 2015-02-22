package com.humongolf.crawler;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.humongolf.entity.Ball;
import com.humongolf.entity.Benchmark;

@RunWith(BlockJUnit4ClassRunner.class)
public class GolfsmithMenBallsTest extends Crawler {
	private static String baseUrl = "http://www.golfsmith.com/search/mens-golf-balls";
	private static String cartUrl = "https://www.golfsmith.com/checkout/cart";
	private static String webSource  = "golfsmith";
	
	private LinkedList<Ball> balls = new LinkedList<Ball>();

	@Test
	public void testCrawler() throws InterruptedException {
		crawl();
	}
		
	
	public void crawl() throws InterruptedException {
		getDriver().get(baseUrl);
		LinkedHashMap<Integer, String> nextPages = new LinkedHashMap<Integer, String>();

		List<WebElement> pages = getDriver().findElements(By.className("page-number"));

		for (WebElement page : pages) {
			try {
				nextPages.put(Integer.parseInt(page.getText()), page.getAttribute("href"));
			} catch (Exception e) {
				// ignore this exception
			}
		}

		parseCurrentPage(baseUrl);

		for (String nextPage : nextPages.values()) {
			getDriver().get(nextPage);
			parseCurrentPage(nextPage);
		}
		
		processBalls();
	}

	public void parseCurrentPage(String url) {
		//Closing the overlay commercial if displayed
		waitAndClosePopup();
		
		
		List<WebElement> products = getDriver().findElements(By.xpath("//*[@id='srp-products']//ul//li"));
		for (WebElement product : products) {
			Ball ball = null;
			try {
				String productUrl = product.findElement(By.tagName("a")).getAttribute("href");
				String imageUrl = product.findElements(By.tagName("img")).get(0).getAttribute("src");
				String brand = product.findElement(By.className("brand-name")).getText();
				String name = product.findElement(By.className("brand-description")).getText();
				
				ball = new Ball(name, brand, webSource, productUrl, imageUrl);
				
				String priceText = product.findElement(By.className("now")).getText();
				try {
					ball.setPrice(Double.parseDouble(priceText.substring(priceText.indexOf('$') + 1)));
					saveBenchmark(new Benchmark(ball.getFullname(), ball.getSource(), ball.getPrice()));
				} catch (Exception e) {
					System.out.println("No price for " + brand + " " + name);
				}
				
				saveBall(ball);
				
				if(ball.isUpserted() || ball.getPrice() == -1) {
					balls.add(ball);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}
	
	private void processBalls() {
		if(balls.size() != 0) {
			try {
				for(Ball ball : balls) {
					getDriver().get(ball.getProductUrl());
					waitAndClosePopup();
					
					if(ball.isUpserted()) {
						try {
							String largeImage = getDriver().findElement(By.xpath("//*[@id='Zoomer']")).getAttribute("href");
							ball.setLargeImageUrl(largeImage);
							addImage(ball, "large");
						} catch (Exception e) {
							if(getDriver().findElement(By.xpath("//*[@id='error_401']")) != null) {
								continue;
							}
						}
					}
					
					if(ball.getPrice() == -1) {
						getDriver().findElement(By.xpath("//*[@id='add2cart_btn']")).click();
					}	
				}
				
				getDriver().get(cartUrl);
				waitAndClosePopup();
				
				List<WebElement> cartItems = getDriver().findElements(By.xpath("//*[@id='items_list']//li"));
				for(WebElement cartItem : cartItems) {
					String brand = cartItem.findElement(By.xpath("//table/tbody/tr[1]/td[2]/h6/strong")).getText();
					String name = cartItem.findElement(By.xpath("//table/tbody/tr[1]/td[2]/h6")).getText();
					name = name.substring(name.indexOf('\n')+1);
					String priceText = cartItem.findElement(By.xpath("//table/tbody/tr[1]/td[4]/h6")).getText();
				
					double price = -1;
					try {
						price = Double.parseDouble(priceText.substring(priceText.indexOf('$') + 1));
						saveBenchmark(new Benchmark(brand + " " + name, webSource, price));
						updateBallPrice(brand + " " + name, webSource, price);
					} catch (Exception e) {
						System.out.println("No price for " + brand + " " + name);
					}
					
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void waitAndClosePopup() {
		try {
			WebElement closeButton = (new WebDriverWait(getDriver(), 10)).until(ExpectedConditions.presenceOfElementLocated(By.className("bcx_close_overlay")));
			closeButton.click();
		} catch(Exception e) {
			//don't care
		}
	}

	
}
