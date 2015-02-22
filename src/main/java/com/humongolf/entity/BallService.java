package com.humongolf.entity;

import java.util.Date;

import org.apache.commons.lang3.text.WordUtils;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.WriteResult;

public class BallService extends Service {
	private static BallService instance = null;
	private DBCollection importedBallCollection = null;
	
	
	protected BallService() {
		super();
		importedBallCollection = getDb().getCollection("importedBalls");
	}

	public static BallService getInstance() {
		if (instance == null) {
			instance = new BallService();
		}
		return instance;
	}

	public void save(Ball ball) {
		if(ball.getColors() == null) {
			save(ball, null);
		} else {
			for(String color : ball.getColors()) {
				save(ball, color);
			}
		}
	
	}
		
	public void save(Ball ball, String color) {
		BasicDBObject query = new BasicDBObject("fullname", WordUtils.capitalizeFully(ball.getFullname())).append("source", ball.getSource()).append("color", color);
		BasicDBObject setValues = new BasicDBObject("name", WordUtils.capitalizeFully(ball.getName())).
				append("brand", WordUtils.capitalizeFully(ball.getBrand())).
				append("updated", new Date()).
				append("url", ball.getProductUrl()).
				append("highNumber", ball.isHighNumber()).
				append("customizable", ball.isCustomizable());
		
		
		BasicDBObject values = new BasicDBObject("$set", setValues);
		
		if(ball.getPrice() >= 0) {
			setValues.append("price", ball.getPrice());
		} else {
			values.append("$unset", new BasicDBObject("price", ""));
		}
		
		values.append("$setOnInsert", new BasicDBObject("created", new Date()).append("published", false));
		WriteResult wr =  importedBallCollection.update(query, values, true, false);
		ball.setUpserted(wr.getUpsertedId() != null);
	}
	
	
	public void updateBallPrice(String fullname, String source, double price) {
		BasicDBObject query = new BasicDBObject("fullname", WordUtils.capitalizeFully(fullname)).append("source", source);
		BasicDBObject values = new BasicDBObject("$set", new BasicDBObject("price", price));
		
		importedBallCollection.update(query, values, false, true);
	}
	
	
	public WriteResult addImage(String fullname, String type, String destFilename) {
		BasicDBObject query = new BasicDBObject("fullname", WordUtils.capitalizeFully(fullname));
		BasicDBObject values = new BasicDBObject("$set", new BasicDBObject("images."+type, destFilename));
		return importedBallCollection.update(query, values, false, true);
	}
}
