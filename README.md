db.getCollection('order').aggregate([
{$group : {"_id" : "$customer", "bonusPoints" : { $push :  "$bonusPoint" }}},
{$project : {"_id" : 1, "bonusPoints" : 1, "bonusPointsCount" : {$size : "$bonusPoints"}}},
{$match : {bonusPointsCount : {$gt : 4}}},
{$project : {"_id" : 1, "bonusPoints" : 1, "isInvalid" : {$allElementsTrue : "$bonusPoints"}}},
{$match : {"isInvalid" : true}}])

db.setProfilingLevel(2);
db.system.profile.find().sort({$natural:-1}).limit(5).pretty();

db.getCollection('order').find({"customer" : "Lora_Copeland", "bonusPoint" : true}).explain({"excutionStats" : 1})

db.order.getIndexes()

db.getCollection('order').createIndex({customer : 1})
