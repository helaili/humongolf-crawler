package com.humongolf.crawler;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	com.humongolf.crawler.TheGolfWarehouseMenBallsTest.class, 
	com.humongolf.crawler.GolfsmithMenBallsTest.class,  
	com.humongolf.crawler.BallCollectionInitializerTest.class,
	com.humongolf.crawler.BallBrandInitializerTest.class,
	com.humongolf.crawler.BallPriceInitializerTest.class})
//@SuiteClasses({com.humongolf.crawler.TheGolfWarehouseMenBallsTest.class, com.humongolf.crawler.GolfsmithMenBallsTest.class})
//@SuiteClasses({com.humongolf.crawler.BallBrandInitializerTest.class})
public class BallCrawlers {

}
