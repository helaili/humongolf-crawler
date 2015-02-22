package com.humongolf.entity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang3.text.WordUtils;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.WriteResult;

public class BenchmarkService extends Service {
	private static BenchmarkService instance = null;
	private DBCollection benchmarkCollection = null;
	
	
	protected BenchmarkService() {
		super();
		benchmarkCollection = getDb().getCollection("ballbenchmarks");
	}

	public static BenchmarkService getInstance() {
		if (instance == null) {
			instance = new BenchmarkService();
		}
		return instance;
	}

	public WriteResult save(Benchmark benchmark) {
		String month = new SimpleDateFormat("yyyy-MM").format(Calendar.getInstance().getTime());
		String date  = new SimpleDateFormat("dd").format(Calendar.getInstance().getTime());
		
		BasicDBObject query = new BasicDBObject("fullname", WordUtils.capitalizeFully(benchmark.getFullname())).append("month", month);
		BasicDBObject values = new BasicDBObject("$push", new BasicDBObject("prices."+date+".benchmarks", new BasicDBObject("source", benchmark.getSource()).append("price", benchmark.getPrice())))
								.append("$min", new BasicDBObject("minPrice", benchmark.getPrice()).append("prices."+date+".min", benchmark.getPrice()))
								.append("$max", new BasicDBObject("maxPrice", benchmark.getPrice()).append("prices."+date+".max", benchmark.getPrice()))
								.append("$inc", new BasicDBObject("sumPrice", benchmark.getPrice()).append("countPrice", 1).append("prices."+date+".sumPrice", benchmark.getPrice()).append("prices."+date+".countPrice", 1))
								.append("$set", new BasicDBObject("updated", new Date()).append("fullname", WordUtils.capitalizeFully(benchmark.getFullname())))
								.append("$setOnInsert", new BasicDBObject("created", new Date()));
		
		return benchmarkCollection.update(query, values, true, false);
	}
}
