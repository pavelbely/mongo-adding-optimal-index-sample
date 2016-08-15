# Choosing optimal MongoDB index for a specific query

As you may known having an appropriate index can significantly improve query performance.
But what counts as appropriate index? I would like to share with you my humble experience of adding one.

We will profile the database of **Comrade** application - food ordering app for soviet-style restaurant of that name.

## Comrade application

Comrade is a RESTful web service build with Spring that uses MongoDB as data storage for customer orders. The orders are saved in a collection with the following schema:
```js
db.order.findOne()

{
    "customer" : "Natasha_Ivanova",
    "items" : [
        {
            "product" : "Stewed cabbage in a pot",
            "price" : 4.4,
            "quantity" : 1
        },
        {
            "product" : "A loaf of bread",
            "price" : 1.3,
            "quantity" : 3
        },
        {
            "product" : "Mojito",
            "price" : 3.9,
            "quantity" : 1
        }
    ],
    "bonusPoint" : false,
    "givenAsBonus" : false
}
```

One would argue that saving orders in SQL database is a better idea and probably it is. We're doing so just for demostration purposes.

Customers make orders by sending **POST** http requests to **Comrade** app.
```js
POST /order HTTP/1.1
Host: localhost:8080
Content-Type: application/json

{
    "customer" : "Natasha_Ivanova",
    "items" : [
        {
            "product" : "Cold beetrot soup",
            "price" : 1.5,
            "quantity" : 1
        },
        {
            "product" : "A loaf of bread",
            "price" : 0.05,
            "quantity" : 1
        },
        {
            "product" : "Buttermilk",
            "price" : 0.5,
            "quantity" : 1
        }]
}
```

This order is then being processed, and the bill is served as http response:
```js
{
  "orderId": "57adc478e6eb6c237791f5e1",
  "total": 5.05,
  "comment": "Bon appetite, comrade!",
  "bonusPointsCount": 1
}
```

Comrade restaurant runs bonus program - each 10th customer's meal is free.
If customer has enough bonus points they are being spent and her meal is served free:
```js
{
  "orderId": "57adc761e6eb6c237791f5e5",
  "total": 0,
  "comment": "Enjoy your FREE meal, comrade!",
  "bonusPointsCount": 0
}
```

## Adding MongoDB indexes

## Requirements
You need to have MongoDB installed and Comrade app running in order to follow through this tutorial.

### Start Comrade application
TODO

### Import data

First let's load some data into your database.
Please download [comrade.json](comrade.json) and import it using the following command
```
mongoimport --db comrade --collection order --file comrade.json
```
10000 documents should have been imported into **order** collection.

### Set MongoDB profiler on

Now we need to set the profiler on for all the queries.
Please refer to [MongoDB documentation](https://docs.mongodb.com/manual/tutorial/manage-the-database-profiler) for more info on profiling options.
```js
use comrade;
db.setProfilingLevel(2);
```

### Make an order and see what happens at backend
Make a new order by sending http request as described above. If you are using Postman - please find Postman collection with order request attached.

Let's see what we have in profiler collection.
```js
db.system.profile.find().sort({$natural:-1}).pretty();
```

This command will return log entries in **system.profile** collection sorted by timestamp.
```js
/* 1 */
/* 1 */
{
    "op" : "insert",
    "ns" : "comrade.order",
    "query" : {
        "insert" : "order",
        "ordered" : true,
        "documents" : [
            {
                "_id" : ObjectId("57b1db55f829b7015d0a82d0"),
                "customer" : "Natasha_Ivanova",
                "items" : [
                    {
                        "product" : "Beetrot cold soup",
                        "price" : 1.3,
                        "quantity" : 3
                    },
                    {
                        "product" : "Buttermilk",
                        "price" : 3.4,
                        "quantity" : 1
                    }
                ],
                "bonusPoint" : true,
                "givenAsBonus" : false
            }
        ]
    },
    "ninserted" : 1,
    "keyUpdates" : 0,
    "writeConflicts" : 0,
    "numYield" : 0,
    "locks" : {
        "Global" : {
            "acquireCount" : {
                "r" : NumberLong(1),
                "w" : NumberLong(1)
            }
        },
        "Database" : {
            "acquireCount" : {
                "w" : NumberLong(1)
            }
        },
        "Collection" : {
            "acquireCount" : {
                "w" : NumberLong(1)
            }
        }
    },
    "responseLength" : 40,
    "protocol" : "op_query",
    "millis" : 0,
    "execStats" : {},
    "ts" : ISODate("2016-08-15T15:10:13.513Z"),
    "client" : "127.0.0.1",
    "allUsers" : [],
    "user" : ""
}

/* 2 */
{
    "op" : "command",
    "ns" : "comrade.order",
    "command" : {
        "count" : "order",
        "query" : {
            "customer" : "Natasha_Ivanova",
            "bonusPoint" : true
        }
    },
    "keyUpdates" : 0,
    "writeConflicts" : 0,
    "numYield" : 781,
    "locks" : {
        "Global" : {
            "acquireCount" : {
                "r" : NumberLong(1564)
            }
        },
        "Database" : {
            "acquireCount" : {
                "r" : NumberLong(782)
            }
        },
        "Collection" : {
            "acquireCount" : {
                "r" : NumberLong(782)
            }
        }
    },
    "responseLength" : 62,
    "protocol" : "op_query",
    "millis" : 53,
    "execStats" : {},
    "ts" : ISODate("2016-08-15T15:10:13.512Z"),
    "client" : "127.0.0.1",
    "allUsers" : [],
    "user" : ""
}
```

These queries were executed by **Comrade** when processing your order.
As you may guess the second query was executed when searching for customers bonus points.
```js
...
"query" : {
    "customer" : "Natasha_Ivanova",
    "bonusPoint" : true
}
...
```
And the first one when saving your processed order to db.

### Using explain plan to measure queries performance

Cool, now let's check how effective search query is using **MongoDB explain plan**.

```js
db.order.explain("executionStats").count({customer : "Natasha_Ivanova", bonusPoint : 1})

{
    "queryPlanner" : {
        "plannerVersion" : 1,
        "namespace" : "comrade.order",
        "indexFilterSet" : false,
        "parsedQuery" : {
            "$and" : [
                {
                    "bonusPoint" : {
                        "$eq" : 1.0
                    }
                },
                {
                    "customer" : {
                        "$eq" : "Natasha_Ivanova"
                    }
                }
            ]
        },
        "winningPlan" : {
            "stage" : "COUNT",
            "inputStage" : {
                "stage" : "COLLSCAN",
        ...
    "executionStats" : {
        "executionSuccess" : true,
        "nReturned" : 0,
        "executionTimeMillis" : 51,
        "totalKeysExamined" : 0,
        "totalDocsExamined" : 100001,
```

The output is pretty long but for now we are only concerned about **winningPlan** being **COLLSCAN** and **executionTimeMillis**=51 and **totalDocsExamined**=100001.
**COLLSCAN** means the whole collection (100001 documents) has been scanned (which is obvious since we haven't added any indexes yet) in order to find Natasha's bonus points and that took 51 milliseconds. You might say 51 ms is no big deal, but let's try to improve it a bit.

### Add an index and check whether it improves performance

Let's add compound index on **customer** and **bonusPoint** fields.
```js
db.order.createIndex({customer : 1, bonusPoint : 1});
```

And now run that explain query from the previous section again.
```js
db.order.explain("executionStats").count({customer : "Natasha_Ivanova", bonusPoint : 1})

{
    "queryPlanner" : {
        "plannerVersion" : 1,
        "namespace" : "comrade.order",
        "indexFilterSet" : false,
        "parsedQuery" : {
            "$and" : [
                {
                    "bonusPoint" : {
                        "$eq" : 1.0
                    }
                },
                {
                    "customer" : {
                        "$eq" : "Natasha_Ivanova"
                    }
                }
            ]
        },
        "winningPlan" : {
            "stage" : "COUNT",
            "inputStage" : {
                "stage" : "COUNT_SCAN",
                "keyPattern" : {
                    "customer" : 1.0,
                    "bonusPoint" : 1.0
                },
                "indexName" : "customer_1_bonusPoint_1",
                "isMultiKey" : false,
                "isUnique" : false,
                "isSparse" : false,
                "isPartial" : false,
                "indexVersion" : 1
            }
        },
        "rejectedPlans" : []
    },
    "executionStats" : {
        "executionSuccess" : true,
        "nReturned" : 0,
        "executionTimeMillis" : 2,
        "totalKeysExamined" : 1,
        "totalDocsExamined" : 0,
```

As you can see this index makes a difference.
Only 1 index key was examined during this query and that took 2 millis.
```js
"executionTimeMillis" : 2,
"totalKeysExamined" : 1,
"totalDocsExamined" : 0,
```

Congrats, comrade, you did a great job!
Even though adding of this index was pretty obvious since it included all the fields in the search query, it is good to make sure it actually was helpful.
