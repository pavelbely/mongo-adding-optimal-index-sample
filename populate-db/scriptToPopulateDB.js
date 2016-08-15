let http = require('http');
let agent = new http.Agent( {maxSockets: 1} );
var rp = require('request-promise');
let Chance = require('chance');
let async = require('async');

let chance = new Chance();

const PRODUCTS = ["Mushroom soup", "Beetrot cold soup", "Pureed soup", "Stewed cabbage in a pot",
  "Chicken livers", "Ceasar salad", "Greek salad", "Meatballs", "Tea", "Buttermilk",
  "Coffee", "Mojito", "A loaf of bread"];

const CUSTOMERS_COUNT = 10000;

const OPTIONS = {
  hostname: 'localhost',
  port: 8080,
  path: '/order',
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  }
};

function randomItem() {
  return {
    product : chance.pickone(PRODUCTS),
    price : chance.floating({min: 0.5, max: 5, fixed: 1}),
    quantity : chance.natural({min: 1, max: 3})
  }
}

function randomItems() {
  let items = [];
  for (var i = 0; i < chance.natural({min: 3, max: 5}); i++) {
    items.push(randomItem());
  }
  return items;
}

function randomOrder(customer) {
  return {
    customer : customer,
    items : randomItems()
  }
}

function getCustomerOrders() {
  let customerOrders = [];
  let customer = chance.first() + "_" + chance.last();
  for (var i = 0; i < chance.natural({min: 7, max: 20}); i++) {
    customerOrders.push(randomOrder(customer));
  }
  return customerOrders;
}

function getOrders() {
  let orders = [];
  debugger;
  for (var i = 0; i < CUSTOMERS_COUNT; i++) {
    orders = orders.concat(getCustomerOrders());
  }
  return orders;
}

function makeOrder(order, callback) {
  const options = {
    url :  "http://localhost:8080/order",
    method: 'POST',
    body : order,
    json : true,
    agent : agent
  };

  rp(options,
    function(err, res, body) {
      callback(err, body);
    }
  );
}

let orders = getOrders();
console.log(orders.length);
async.map(orders, makeOrder, function (err, res){
  if (err) return console.log(err);
  // console.log(res);
});
