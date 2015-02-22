package com.humongolf.entity;

import java.net.UnknownHostException;

import com.mongodb.DB;
import com.mongodb.MongoClient;

public abstract class Service {
	private DB db;

	protected Service() {
		try {
			db = new MongoClient("localhost:27017").getDB("humongolf-dev");
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected DB getDb() {
		return db;
	}
	

}
