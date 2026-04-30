fun int function(int a, int b, int c){
    var int d = 0;
    a = a + b;
    c = c - 1;
    if(c = 0){
        return a;
    }
    d = function(a, b, c);
    return d;
}

fun int main() {
    var int a = 1;
    var int b = 2;
    var int c = 3;
    a = function(a, b, c);
    return a;
}