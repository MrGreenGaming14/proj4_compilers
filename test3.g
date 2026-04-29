var int a_1 = 1;
var int a_2 = 2;
var int a_3 = 3;

fun int function(int a, int b, int c){
    return a+b+c;
}

fun int main() {
    var int a = 1;
    var int b = 2;
    var int c = 3;
    a = function(a, b, c);
    return a;
}