package com.humongolf.crawler;

import java.net.UnknownHostException;
import java.util.List;

import org.junit.Test;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.BulkWriteOperation;
import com.mongodb.BulkWriteResult;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

public class BallBrandInitializerTest {
	private DB db;

	public BallBrandInitializerTest() {
		try {
			db = new MongoClient("localhost:27017").getDB("humongolf-dev");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
	
	
	@Test
	public void updateNames() {
		DBCollection ballsCollection = db.getCollection("importedBalls");
		
		List<String> brands = ballsCollection.distinct("brand");
		
		
		BasicDBList orList = new BasicDBList();
		orList.add(new BasicDBObject("name", null));
		orList.add(new BasicDBObject("name", new BasicDBObject("$exists", false)));
		BasicDBObject query = new BasicDBObject("$or", orList);
		
		DBCursor cursor = ballsCollection.find(query);
		
		
		BulkWriteOperation builder = ballsCollection.initializeUnorderedBulkOperation();
		
		boolean executeBulk = false;
		
		while(cursor.hasNext()) {
			DBObject ball = cursor.next();
			String fullname = ((BasicDBObject)ball).getString("fullname");
			boolean foundBrand = false;
			for(String brand : brands) {
				if(brand == null) {
					continue;
				}
				if(fullname.startsWith(brand)) {
					String name = fullname.substring(brand.length()+1);
					BasicDBObject ballUpdates = new BasicDBObject("$set", new BasicDBObject("brand", brand).append("name", name));
					builder.find(new BasicDBObject("_id", ball.get("_id"))).updateOne(ballUpdates);
					executeBulk = true;
					foundBrand = true;
					break;
				}
			}
			if(!foundBrand) {
				System.out.println("Brand not found. Fullname: " + fullname);
			}
		}		
		
		if(executeBulk) {
			BulkWriteResult result = builder.execute();
			System.out.println(result);
		}
	}
}
