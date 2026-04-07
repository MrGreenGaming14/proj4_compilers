//Rule 1: Numbers and strings are different values
//IMPLEMENTED
//var int r1_int = "test"; //fails
//var string r1_str = 2; //fails

//Rule 2: Any number can be assigned to a pointer of any type
//IMPLEMENTED
//var int r2_int1 = "Hello World"; //Throws Error
//var string r2_str = 0; // Throws Error
var int r2_int1 = 0;
var string* r2_strPtr = 8; // Passes
var int r2_int2 = r2_strPtr; // Passes
var int r2_int3 = r2_strPtr + r2_int1; // Passes

//Rule 3: If an Array is initialized, must be initialized with a list and must be correct length
//IMPLEMENTED
//var int[1][2] r3_test = 0; //throws error
var int[3][2] r3_arr1 = {{1,2},{1,2},{1,2}}; // Passes
//var int[2][2] r3_arr2 = {{1,2},{1,2,3}}; // Throws Error

//Rule 4: Structs can be initialized with a list
//IMPLEMENTED
struct r4_struct{
    int[][] x;
    int y;
}
var r4_struct r4_struct1 = {{{1,2},{1,2}},1}; //shouldn't throw error
//var r4_struct r4_struct2 = {1,2}; //should throw error

//Rule 5: Unions are not initialized with a list. They are initialized with a value that type
//checks for one of its members.

//Rule 6: Unions can be assigned to any value that type checks with one of it’s members.
//IMPLEMENTED
union r6_union {
    int[][] x;
    int y;
}
var r6_union r6_ux = {{1,2},{1,2}}; //no error
var r6_union r6_uy = 1; //no error
//var r6_union ufails = {{{1,2},{1,2}},1}; //error

//Rule 7: For Arrays without expressions, there must be some dimensions such that the initializer matches int[x_1][x_2]
//the dimensions must match: ex: int[2][2], int[3][3]
//IMPLEMENTED
var int[][] r7_arr1 = {{1,2},{1,2}}; //passes
//var int[][] r7_arr2 = {{1,2,3},{1,2}}; //fails

//Rule 8: Math operations only accept numbers (there is no string concatenation)
//IMPLEMENTED
var int r8_int1 = 1; //pass
var int r8_int2 = 2; //pass
var int r8_passes = r8_int1 + r8_int2; //pass
//var int r8_fails = "Hello" + "World"; //fail
//var string r8_alsofails = "Hello" + "World"; //fail

//Rule 9: Function application must match parameter types, and evaluates to expression of the return type
//IMPLEMENTED
//fun int r9_example1(int a, string b, int[2][2] arr){} //fails
fun int r9_example2(int a, string b, int[2][2] arr) {
    return 0;
}
fun int r9_main () {
    var int[][] r9_x1 = {{2,2},{2,2}};
    var int[2][3] r9_x2 = {{2,2,2},{2,2,2}};
    var int r9_y1 = r9_example2(1,"2",r9_x1); //passes
    //var int r9_y2 = r9_example2(1,"2",r9_x2); //fails
    //var int r9_y3 = r9_example2(1,"2"); //fails
    return 0;
}

//Rule 10: Func decls must check all return statements within the function return a value with type matching
// nested function declarations are supported
// if return type void, any returns cause error
//IMPLEMENTED
fun int r10_fun1() {
    fun string r10_fun2() {
        // Returns string. fun2 passes
        return "passes";
    }
    return 0;
}
fun void r10_fun2(){} //passes
//fun void r10_fun3() { return null; } //fails

//Rule 11: A variable and a function cannot share the same name in the same scope
//IMPLEMENTED
var int r11_func;
//fun void r11_func(int a, int b){} //throws error
//var int r11_func; //throws error

//Rule 12: Any type T used in the program must be a valid type in the scope
//IMPLEMENTED
//var r12_myStruct r12_myStruct1; //fails, r12_myStruct not defined

//Rule 13: The conditional in if statements and while statements must be numbers
//IMPLEMENTED
fun int r13_main() {
    //if ("fails") {} //fails
    //if(1+"alsofails"){} //fails
    if(r10_fun1()){} //passes
    return 0;
}

//Rule 14: Unary operations all take numbers and return numbers except for two
// *p evaluates to a term that matches the underlying type of the pointer
// & takes any term and evaluates to a pointer of that term's type
//IMPLEMENTED
var string* r14_p = 100;
var string r14_pstring = *r14_p; //passes
//var string r14_pstring2 = **r14_p //fails
var string r14_s = "Example";
var int r14_x = &r14_s; //passes
var string* r14_y = &r14_s; //passes

//Original test.g:
//var int**[3][3] b = {{1,2,3},{1,2,3}}; // Bugged Error Message
//union ms { string x; int[] y; }

//typedef int A;
//var ms n = "1"; 
//var ms m = {1,1}; 
//var A x = 2;

//fun ms main(ms n) {
//   var int z;
//   return x;
//} 


