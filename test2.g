var int r1_int = 2;
var string r1_str = "string";
fun int test_func () {
    var int a = 1;
    fun int test_func2 (){
        var int a = 2;
        var int b = 3;
        fun int test_func3(){
            var int a = 3;
            var int b = 2;
            var int c = a + b;
            if (a = b) {
                if(b = c){
                    var int d = 0;
                }
            }
            else{
                if(b = c){
                    var int e = 1;
                }
            }
            return a;
        }
        while (a = b) {
            var int c = 2;
        }
        return a;
    }
    return a;
}
var int f = 0;
var int g = 1;
fun string*** test_func4(){
    var string str = "";
    var string*** str_ptr = &(&(&str));
    return str_ptr;
}