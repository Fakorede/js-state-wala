var name = "John Doe";
var year = 1997;
var isAdult = null;
var age = null;

function calculateAge(year) {
    return 2023 - year;
}

age = calculateAge(1997);
isAdult = age > 18 ? true : false;

var message = "The candidate " + name + " is " + age + " years old and is";
message = isAdult ? message + " eligible" : message + " not eligible"