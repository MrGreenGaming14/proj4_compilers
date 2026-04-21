var int r1_int = 2;
var string r1_str = "string";
fun int test_func () {
    var int a = 1;
    fun int test_func2 (){
        var int a = 2;
        fun int test_func3(){
            var int a = 3;
            var int b = 2;
            var int c = a + b;
            return a;
        }
        return a;
    }
    return a;
}
fun string*** test_func4(){
    var string str = "";
    var string*** str_ptr = &(&(&str));
    return str_ptr;
}