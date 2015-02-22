package com.humongolf;

import java.util.ResourceBundle;

public class CrawlerContext {
	private static CrawlerContext instance = null;
	private static ResourceBundle rs = ResourceBundle.getBundle("Crawler");
	
	protected CrawlerContext() {
		super();
	}

	public static CrawlerContext getInstance() {
		if (instance == null) {
			instance = new CrawlerContext();
		}
		return instance;
	}
	
	public static String getString(String name) {
		return rs.getString(name);
	}
	
	public static boolean loadImages() 	{
		return Boolean.parseBoolean(getString("LOAD_IMAGES"));
	}
	
	public static String getImagePath() 	{
		return getString("IMAGE_DIR");
	}
}
