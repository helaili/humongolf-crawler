package com.humongolf.crawler;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.humongolf.entity.Ball;
import com.humongolf.entity.Benchmark;

@RunWith(BlockJUnit4ClassRunner.class)
public class TheGolfWarehouseMenBallsTest extends Crawler {
	private static String baseUrl = "http://www.tgw.com/customer/search2.jsp?scid=1061&a1146=mens&page=A&sortmfr=N";
	private static String cartUrl = "http://www.tgw.com/customer/cart.jsp";
	private static String webSource = "tgw";

	private LinkedList<Ball> balls = new LinkedList<Ball>();
	private LinkedHashMap<String, CartBall> cartBalls = new LinkedHashMap<String, CartBall>();

	@Test
	public void testCrawler() throws InterruptedException {
		crawl();
	}

	public void crawl() throws InterruptedException {
		getDriver().get(baseUrl);
		parseCurrentPage(baseUrl);
		processBalls();
		processShoppingCart();
	}

	public void parseCurrentPage(String url) {
		int counter = -1;
		Ball ball = null;

		Pattern dozenPattern = Pattern.compile("(\\d) (dozen|dz)");
		Pattern getFreePattern = Pattern.compile("Get (\\d) Free");
		Pattern nowPattern = Pattern.compile("Now.*\\$(\\d+\\.?\\d*)");
		Pattern buyXforPattern = Pattern.compile("Buy (\\d) (?:dozen|dz) for \\$(\\d+\\.?\\d*)");
		Pattern pricePattern = Pattern.compile("Price: \\$(\\d+\\.?\\d*)");

		List<WebElement> productCols = getDriver()
				.findElements(
						By.xpath("//*[@id='mainTable']/tbody/tr[2]/td/table/tbody/tr[2]/td/table/tbody/tr[3]/td[3]/table[2]/tbody/tr/td"));

		for (WebElement productCol : productCols) {

			counter++;
			if ((counter + 1) % 7 == 0 && counter > 0) {
				continue;
			} else if (counter % 7 % 2 == 0) {
				// This is the image
				if (productCol.getText().equals(" ")) {
					break;
				} else {
					String imageUrl = productCol.findElement(By.tagName("img")).getAttribute("src");
					ball = new Ball();
					ball.setSmallImageUrl(imageUrl);
				}
			} else if (counter % 7 % 2 == 1) {
				List<WebElement> productLinkElements = productCol.findElements(By.className("textlinks"));

				if (productLinkElements.size() != 0) {
					WebElement productLinkElement = productLinkElements.get(0);

					String productUrl = productLinkElement.getAttribute("href");
					ball.setProductUrl(productUrl);

					String fullname = productLinkElement.getAttribute("title");
					ball.setFullname(fullname);

					ball.setSource(webSource);
					balls.add(ball);

					boolean priceFound = false;

					try {
						// Buy 2 dozen Get 1 Free
						String priceText = productCol.findElement(By.xpath("p[1]/font[2]/b")).getText();

						Matcher m = buyXforPattern.matcher(priceText);
						if (m.find()) {
							int qtyForCart = Integer.parseInt(m.group(1));
							double priceForCart = Double.parseDouble(m.group(2));

							ball.setPrice(priceForCart / qtyForCart);
							priceFound = true;
						}

					} catch (Exception e) {
					}

					if (!priceFound) {
						try {
							// Buy 2 dozen Get 1 Free
							String priceText = productCol.findElement(By.xpath("p[1]/font[2]/b")).getText();
							int qtyForCart = 0;

							Matcher buyMatcher = dozenPattern.matcher(priceText);
							if (buyMatcher.find()) {
								qtyForCart = Integer.parseInt(buyMatcher.group(1));
							}

							Matcher freeMatcher = getFreePattern.matcher(priceText);
							if (freeMatcher.find()) {
								qtyForCart += Integer.parseInt(freeMatcher.group(1));
							}

							if (qtyForCart > 0) {
								cartBalls.put(fullname, new CartBall(ball, qtyForCart));
								priceFound = true;
							}
						} catch (Exception e) {
						}

					}

					if (!priceFound) {
						try {
							// Sale Now $19.98 Was $34.98
							String priceText = productCol.findElement(By.xpath("p[1]/font[2]/b")).getText();

							Matcher m = nowPattern.matcher(priceText);
							if (m.find()) {
								ball.setPrice(Double.parseDouble(m.group(1)));
								priceFound = true;
							}

						} catch (Exception e) {
						}
					}

					if (!priceFound) {
						try {
							// Price: $48.95 - $51.95
							String priceText = productCol.findElement(By.xpath("p[1]/font[2]/b/font/b")).getText();

							Matcher m = pricePattern.matcher(priceText);
							if (m.find()) {
								ball.setPrice(Double.parseDouble(m.group(1)));
								priceFound = true;
							}
						} catch (Exception e) {
						}
					}

					if (!priceFound) {
						System.out.println("******** Price not found ************");
						System.out.println(productCol.getText());
					}

				} else {
					break;
				}

			}

		}

	}

	private void processBalls() {
		for (Ball ball : balls) {
			try {
				getDriver().get(ball.getProductUrl());
				String largeImageUrl = getDriver().findElement(By.xpath("//*[@id='mprodMainImage']")).getAttribute("src");
				ball.setLargeImageUrl(largeImageUrl);
				
				(new WebDriverWait(getDriver(), 10)).until(ExpectedConditions.presenceOfElementLocated(By
						.id("mprodQtySpan")));
				Thread.sleep(2000);

				try {
					
					
					WebElement attributeWebElement = getDriver().findElement(
							By.xpath("//*[@id='radioProductSelection']/div[1]/div[1]"));
					
					String attribute = attributeWebElement.getText();
					
					if (attribute.equals("Color")) {
						List<WebElement> colorWebElements = getDriver().findElements(
								By.xpath("//*[@id='radioProductSelection']/div[1]/div[2]//label/input"));

						for (WebElement color : colorWebElements) {
							String colorStr = color.getAttribute("value");
							
							if (colorStr.indexOf("White") >= 0) {
								ball.addColor("White");
							} else if (colorStr.indexOf("Yellow") >= 0) {
								ball.addColor("Yellow");
							} else if (colorStr.indexOf("Pink") >= 0) {
								ball.addColor("Pink");
							} else if (colorStr.indexOf("Orange") >= 0) {
								ball.addColor("Orange");
							} else if (colorStr.indexOf("Volt") >= 0) {
								ball.addColor("Yellow");
							} else if (colorStr.indexOf("Multi") >= 0 || colorStr.indexOf("Assorted") >= 0) {
								ball.addColor("Multi-Color");
							} else {
								ball.addColor(colorStr);
							}
						}
					} else if (attribute.equals("Number")) {
						ball.setHighNumber(true);
					} else if (attribute.equals("Quantity")  || attribute.equals("Item")) {
						// Ignore these
					} else if (attribute.equals("Series") || attribute.equals("Model")) {
						ball.setCustomizable(true);
					} 
				} catch (Exception e) {
				}
				
				saveBall(ball);
				if (ball.getPrice() != -1) {
					saveBenchmark(new Benchmark(ball.getFullname(), ball.getSource(), ball.getPrice()));
				}
			} catch (Exception e) {

			}
		}
	}

	private void processShoppingCart() {
		for (String ballName : cartBalls.keySet()) {
			try {
				Ball ball = cartBalls.get(ballName).getBall();
				getDriver().get(ball.getProductUrl());

				Thread.sleep(2000);

				(new WebDriverWait(getDriver(), 10)).until(ExpectedConditions.presenceOfElementLocated(By
						.id("mprodQtySpan")));
				getDriver()
						.findElement(
								By.xpath("//*[@id='mprodQtySpan']/select/option["
										+ cartBalls.get(ballName).getQuantity() + "]")).click();

				try {
					List<WebElement> options = getDriver().findElements(By.xpath("//*[@id='swatchInput12ballpack']"));
					options.get(0).click();
					Thread.sleep(1000);
				} catch (Exception e) {
				}

				if (getDriver() instanceof JavascriptExecutor) {
					((JavascriptExecutor) getDriver()).executeScript("mainProd.addToCart()");
				}

				Thread.sleep(1000);

				try {
					Alert a = getDriver().switchTo().alert();
					if (a != null) {
						a.accept();
						continue;
					}
				} catch (Exception e1) {
				}

				getDriver().get(cartUrl);
				(new WebDriverWait(getDriver(), 10)).until(ExpectedConditions.presenceOfElementLocated(By
						.id("floatCartSummaryDiv")));

				String cartText = getDriver().findElement(By.id("floatCartSummaryDiv")).getText();
				Pattern MY_PATTERN = Pattern.compile("Cart: \\$(\\d+\\.?\\d*)");
				Matcher m = MY_PATTERN.matcher(cartText);

				if (m.find()) {
					double price = Double.parseDouble(m.group(1)) / cartBalls.get(ballName).getQuantity();
					saveBenchmark(new Benchmark(ball.getFullname(), ball.getSource(), price));
					updateBallPrice(ball.getFullname(), ball.getSource(), price);
				}
			} catch (Exception e) {
				// e.printStackTrace();
			}

			getDriver().manage().deleteAllCookies();
		}
	}

	class CartBall {
		private Ball ball;
		private int quantity;

		public Ball getBall() {
			return ball;
		}

		public CartBall(Ball ball, int quantity) {
			super();
			this.ball = ball;
			this.quantity = quantity;
		}

		public void setBall(Ball ball) {
			this.ball = ball;
		}

		public int getQuantity() {
			return quantity;
		}

		public void setQuantity(int quantity) {
			this.quantity = quantity;
		}
	}

}
