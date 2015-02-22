package com.humongolf.entity;

public class Benchmark {
	private double price;
	private String fullname;
	private String source;
	
	public Benchmark(String fullname, String source) {
		super();
		this.price = -1;
		this.fullname = fullname;
		this.source = source;
	}

	public Benchmark(String fullname, String source, double price) {
		super();
		this.price = price;
		this.fullname = fullname;
		this.source = source;
	}

	public String getFullname() {
		return fullname;
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

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Fullname: ").append(fullname).append(", price: ").append(price);
		return sb.toString();
	}
}
