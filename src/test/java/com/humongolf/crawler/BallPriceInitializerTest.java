package com.humongolf.crawler;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import com.mongodb.AggregationOutput;
import com.mongodb.BasicDBObject;
import com.mongodb.BulkWriteOperation;
import com.mongodb.BulkWriteResult;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

public class BallPriceInitializerTest {
	private DB db;

	public BallPriceInitializerTest() {
		try {
			db = new MongoClient("localhost:27017").getDB("humongolf-dev");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void importBalls() {
		DBCollection importedBalls = db.getCollection("importedBalls");
		DBCollection balls = db.getCollection("balls");

		Calendar cal = Calendar.getInstance();
		Date now = cal.getTime();
		
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		Date today = cal.getTime();
		
	
		
		
		List<DBObject> pipeline = new ArrayList<DBObject>();
		pipeline.add(new BasicDBObject("$match", new BasicDBObject("updated", new BasicDBObject("$gte", today))));
		pipeline.add(new BasicDBObject("$group", new BasicDBObject("_id", "$published_ball")
															.append("balls", new BasicDBObject("$push", "$_id"))
															.append("urls", new BasicDBObject("$push", new BasicDBObject("source", "$source").append("url", "$url")))
															.append("min_price", new BasicDBObject("$min", "$price"))
															.append("avg_price", new BasicDBObject("$avg", "$price"))
															.append("max_price", new BasicDBObject("$max", "$price"))
															.append("benchmarks", new BasicDBObject("$push", new BasicDBObject("source", "$source").append("price", "$price")))));
		pipeline.add(new BasicDBObject("$project", new BasicDBObject("price.min", "$min_price")
																.append("price.avg", "$avg_price")
																.append("price.max", "$max_price")
																.append("price.benchmarks", "$benchmarks")
																.append("balls", "$balls")
																.append("urls", "$urls")));
		
		System.out.println(pipeline);
		
		AggregationOutput ballDataSet = importedBalls.aggregate(pipeline);
		
		BulkWriteOperation ballsBulkUpdate = balls.initializeUnorderedBulkOperation();
		
		for(DBObject ballData : ballDataSet.results()) {
			ballsBulkUpdate.find(new BasicDBObject("_id", ballData.get("_id"))).updateOne(new BasicDBObject("$set", new BasicDBObject("price", ballData.get("price"))
																														.append("balls", ballData.get("balls"))
																														.append("urls", ballData.get("urls"))
																														.append("updated", now)));
		}
		
		DBCursor nonUpdatedBalls = importedBalls.find(new BasicDBObject("updated", new BasicDBObject("$lt", today)));
		for(DBObject ball : nonUpdatedBalls) {
			ballsBulkUpdate.find(new BasicDBObject("_id", ball.get("published_ball"))).updateOne(new BasicDBObject("$set", new BasicDBObject("price", null)
																																.append("balls", null)
																																.append("urls", null)
																																.append("published", false)
																																.append("updated", now)));
		}
		
		
		BulkWriteResult result = ballsBulkUpdate.execute();
		System.out.println(result);
		
	}
}
