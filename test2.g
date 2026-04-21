var int r1_int = 2;
var string r1_str = "string";
fun int test_func () {
    var int a = 1;
    fun int test_func2 (){
        var int a = 2;
        fun int test_func3(){
            var int a = 3;
            var int a = 4;
            return a;
        }
        var int a = 3;
        return a;
    }
    var int a = 2;
    return a;
}