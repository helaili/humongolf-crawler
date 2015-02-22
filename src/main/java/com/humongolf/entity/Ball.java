package com.humongolf.entity;

import java.util.ArrayList;


public class Ball {
	private String _id = null;
	private String name = null;
	private String brand = null;
	private String fullname = null;
	private String source = null;
	private String productUrl = null;
	private String smallImageUrl = null;
	private String largeImageUrl = null;
	private boolean highNumber = false;
	private boolean customizable = false;
	private ArrayList<String> colors = null;
	private double price = -1;
	private boolean upserted = false;
	
	
	public Ball() {
		super();
	}
	
	public Ball(String name, String brand, String source, String productUrl, String imageUrl) {
		super();
		this.name = name;
		this.brand = brand;
		this.source = source;
		this.productUrl = productUrl;
		this.smallImageUrl = imageUrl;
	}
	
	public Ball(String fullname, String source, String productUrl, String imageUrl) {
		super();
		this.fullname = fullname;
		this.source = source;
		this.productUrl = productUrl;
		this.smallImageUrl = imageUrl;
	}
	
	
	
	public String get_id() {
		return _id;
	}
	public void set_id(String _id) {
		this._id = _id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getBrand() {
		return brand;
	}
	public void setBrand(String brand) {
		this.brand = brand;
	}
	public String getFullname() {
		if(fullname == null) {
			return brand + " " + name;
		} else {
			return fullname;
		}
	}
	public void setFullname(String fullname) {
		this.fullname = fullname;
	}
	
	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getProductUrl() {
		return productUrl;
	}
	public void setProductUrl(String productUrl) {
		this.productUrl = productUrl;
	}
	public String getSmallImageUrl() {
		return smallImageUrl;
	}
	public void setSmallImageUrl(String imageUrl) {
		this.smallImageUrl = imageUrl;
	}
	
	public String getLargeImageUrl() {
		return largeImageUrl;
	}

	public void setLargeImageUrl(String largeImageUrl) {
		this.largeImageUrl = largeImageUrl;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}
	
	public boolean isHighNumber() {
		return highNumber;
	}

	public void setHighNumber(boolean highNumber) {
		this.highNumber = highNumber;
	}

	public boolean isCustomizable() {
		return customizable;
	}

	public void setCustomizable(boolean customizable) {
		this.customizable = customizable;
	}

	public ArrayList<String> getColors() {
		return colors;
	}

	public void setColors(ArrayList<String> colors) {
		this.colors = colors;
	}

	public void addColor(String color) {
		if(colors == null) {
			colors = new ArrayList<String>();
		}
		colors.add(color);
	}

	public boolean isUpserted() {
		return upserted;
	}

	public void setUpserted(boolean upserted) {
		this.upserted = upserted;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Brand: ").append(brand).append(", name: ").append(name).append(", fullname: ").append(getFullname()).append(", product URL: ").append(productUrl);
		return sb.toString();
	}
	
}
