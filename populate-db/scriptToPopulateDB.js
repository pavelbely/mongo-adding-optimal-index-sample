let http = require('http');
let Chance = require('chance');

let chance = new Chance();

const PRODUCTS = ["Mushroom soup", "Beetrot cold soup", "Pureed soup", "Stewed cabbage in a pot",
  "Chicken livers", "Ceasar salad", "Greek salad", "Meatballs", "Tea", "Buttermilk",
  "Coffee", "Mojito", "A loaf of bread"];

const CUSTOMERS_COUNT = 2000;

const OPTIONS = {
  hostname: 'localhost',
  port: 8080,
  path: '/order',
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  }
};

function makeOrder(order) {
  let req = http.request(OPTIONS, (res) => {
    if (('' + req.statusCode).match(/^5\d\d$/)) {
      console.log('Server error ' + req.statusCode);
    }
  });

  req.on('error', (e) => {
    console.log('problem with request: ${e.message}');
  });

  // write data to request body
  req.write(order);
  req.end();
}

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

function addCustomerOrders() {
  let customer = chance.first() + "_" + chance.last();
  for (var i = 0; i < chance.natural({min: 3, max: 10}); i++) {
    makeOrder(JSON.stringify(randomOrder(customer)));
  }
}

function populateComradeDB() {
  for (var i = 0; i < CUSTOMERS_COUNT; i++) {
    addCustomerOrders();
  }
}

populateComradeDB();
