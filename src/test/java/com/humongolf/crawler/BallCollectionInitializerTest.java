package com.humongolf.crawler;

import java.net.UnknownHostException;
import java.util.Date;

import org.junit.Test;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

public class BallCollectionInitializerTest {
	private DB db;

	public BallCollectionInitializerTest() {
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

		DBCursor ballFound = importedBalls.find(new BasicDBObject("published_ball", null));

		for (DBObject importedBall : ballFound) {
			// This ball is new in the system
			BasicDBObject whereClause = new BasicDBObject("fullname", importedBall.get("fullname"));
			
			BasicDBObject setExpression = new BasicDBObject("$set", new BasicDBObject("updated", new Date()));
			
			BasicDBObject addToSetExpression = new BasicDBObject("balls", importedBall.get("_id"))
																.append("urls", new BasicDBObject("source", importedBall.get("source"))
																							.append("url", importedBall.get("url")));

			BasicDBObject setOnInsertExpression = new BasicDBObject("created", new Date())
					.append("brand", importedBall.get("brand"))
					.append("name", importedBall.get("name"))
					.append("highNumber", importedBall.get("highNumber"))
					.append("customizable", importedBall.get("customizable"))
					.append("published", false)
					.append("images", importedBall.get("images"));
			
			String color = null;
			
			if (importedBall.get("color") != null) {
				color = (String)importedBall.get("color");
			} else {
				String fullname = (String)importedBall.get("fullname");
				
				if (fullname.indexOf("White") >= 0) {
					color = "White";
				} else if (fullname.indexOf("Yellow") >= 0) {
					color = "Yellow";
				} else if (fullname.indexOf("Orange") >= 0) {
					color = "Orange";
				} else if (fullname.indexOf("Pink") >= 0) {
					color = "Pink";
				} else if (fullname.indexOf("Volt") >= 0) {
					color = "Yellow";
				} else if (fullname.indexOf("Multi") >= 0 || fullname.indexOf("Assorted") >= 0) {
					color = "Multi-Color";
				} else {
					color = "White";
				}
			}

			//setOnInsertExpression.append("color", color);
			whereClause.append("color", color);

			DBObject ball = balls.findAndModify(whereClause, new BasicDBObject("_id", 1), null, false,
					setExpression.append("$addToSet", addToSetExpression).append("$setOnInsert", setOnInsertExpression), true, true);

			BasicDBObject ballIdRef = new BasicDBObject("$set", new BasicDBObject("published_ball", ball.get("_id")));
			importedBalls.update(new BasicDBObject("_id", importedBall.get("_id")), ballIdRef);
		}

	}
}
