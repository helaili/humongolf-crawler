db.ballbenchmark.aggregate([{$unwind : '$images.small'}, {$project : {name : 1, brand : 1, minPrice : 1, maxPrice : 1, avgPrice : {$divide : ['$sumPrice', '$countPrice']}, image_small : '$images.small', created : 1}}, {$out : 'balls'}])


db.ballbenchmark.aggregate([{$unwind : '$images.small'}, {$project : {name : 1, brand : 1, minPrice : 1, maxPrice : 1, avgPrice : {$divide : ['$sumPrice', '$countPrice']}, image_small : '$images.small', created : 1}}])



db.balls.update({image_small : {$exists : false}}, {$set: {image_small_selected : 'images.small.$'}}, {multi : true})


db.importedBalls.aggregate([
	{$unwind : colors},
	{$group : {
		_id : {fullname : '$fullname', color : '$colors'}, 
		brand: {$first: '$brand'}, 
		name: { $first: "$name" }, 
		balls: {$addToSet : '$_id'},
		highNumber : {$first : '$highNumber'}, 
		customizable : {$first : '$customizable'}, 
		urls : {$addToSet : {source : '$source', url :'$url'}}, 
		image_small : {$first : '$images.small'}, 
		image_large : {$first : '$images.large'},
		price_min : {$min : '$price'},
		price_max : {$max : '$price'},
		price_avg : {$avg : '$price'},
		price_bench : {$push : {source : '$source', price :'$price'}}
	}},
	{$project : {
		fullname : '$_id.fullname',
		color : '$_id.color',
		name: 1, 
		balls: 1,
		highNumber : 1, 
		customizable : 1, 
		urls : 1, 
		published : 1,
		created : {$add : [new Date()]},
		updated : {$add : [new Date()]},
		images : {
			small : '$image_small',
			large : '$image_large'
		},
		price : {
			min : '$price_min',
			avg : '$price_avg',
			max : '$price_max',
			bench : '$price_bench' 
		},
		_id : 0
	}}
	])
	
	