var name = "John Doe";
var year = 1997;
var isAdult = null;
var age = null;
var message = null;

function calculateAge(name, year, isAdult, message) {
    age = 2023 - year;
    isAdult = age > 18 ? true : false;
    message = name + " is " + age + " years old";
}

function anotherFunction() {
    var b = 3
    var c = 5
    var a = f(b * c)
}