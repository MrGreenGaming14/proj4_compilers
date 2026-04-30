package CodeGen;

import java.util.ArrayList;
import java.util.List;

/**
 * Type system for the IR.
 *
 * Each IRExpr carries a type. This tells the backend what types 
 * to use in the generated C code.
 */
enum GOTOType {
    INT, STRING, INTARRAY, STRUCT, UNION;

    @Override
    public String toString() {
        return switch (this) {
            case INT -> "int";
            case STRING -> "char*";
            case INTARRAY -> "int*";
            case STRUCT -> "struct";
            case UNION -> "union";
        };
    }
}

/**
 * Base class for all IR nodes.
 */
abstract class GOTONode {
    public <T> T accept(GOTOVisitor<T> v) {
        return v.visitGOTO(this);
    }
}

/**
 * Expressions in IR.
 */
abstract class IRExpr extends GOTONode {
    public GOTOType type;
}

/**
 * Statements in IR.
 *
 * Anything that performs an action but does not compute a value directly.
 */
abstract class IRStmt extends GOTONode {}

/**
 * Builtin operations.
 *
 * This class represents "Builtin" functions. IE functions that come
 * prepackaged with Geaux. 
 *
 * The idea is that when encournting:
 *            var int x = 9;
 *            printf(x);
 *
 * The compiler can insert a builtin to handle printf(). See printf()
 * for a better idea on how this works.
 * In order to make this work, you will need to make special cases
 * for the "builtin" functions in the type checker. Otherwise the typechecker
 * will complain about undefined functions and mismatching types.
 *
 */
class Builtin extends IRStmt {}

/**
 * The top level class that makes up a GOTO program.
 * This will be populated by functions. Each function
 * represents a C function in the generated code.
 * Not necessarily a Geaux function. For example
 * Geaux has nested functions, but C does not.
 * This is something that must be dealt with.

 * Program also provides two unique name generators.
 * This is how students can generate unique names for their
 * variables and labels (which will be important).

 * IMPORTANT: GOTO Programs declare ALL variables as globals
 * Every single variable you plan on using in the entire program,
 * whether it is function local or not, must be in the globals
 * member below.

 * ALSO IMPORTANT: A reasonable question would be:
 * "Wouldn't all variables be in the same scope?
 * Wouldn't we get name clashing if multiple variables have the same name?"
 * The answer is: Yes! and that is a problem that must be solved. This is
 * why one of the suggested passes is renaming variables that shadow each other.
 * It prevents name clashing here.

 * More explanation of this problem can be found in the Var comment.
 */
class Program {
    private int unique_name_counter;
    private int unique_label_counter;

    public ArrayList<Var> globals;
    public ArrayList<Var> stackPtrs;
    public ArrayList<IRStmt> varDecls;
    public boolean writeToFile;
    public boolean readFromFile;
    public ArrayList<Function> funcs;
    public ArrayList<StructTypeDef> structs;
    public ArrayList<UnionTypeDef> unions;

    public Program() {
        this.unique_name_counter = 0;
        this.unique_label_counter = 0;
        this.globals = new ArrayList<>();
        this.stackPtrs = new ArrayList<>();
        this.varDecls = new ArrayList<>();
        writeToFile = false;
        readFromFile = false;
        this.funcs = new ArrayList<>();
        this.structs = new ArrayList<>();
        this.unions = new ArrayList<>();
    }

    public String getUniqueVarName() {
        return "_x"+(++this.unique_name_counter);
    }

    public String getUniqueLabelName() {
        return "LABEL"+(++this.unique_label_counter);
    }
}

/**
 * Represents a function in IR.
 *
 * Holds a list of IR instructions.
 *
 * Example emitted C:
 * int main() {
 *     int x;
 *     x = 5;
 *     return x;
 * }
 */
class Function {
    public ArrayList<GOTONode> instr;
    public String name;
    public String returntype;

    public Function(String name, String ret) {
        this.name = name;
        this.instr = new ArrayList<>();
        this.returntype = ret;
    }
}

/**
 * Printf statement.
 *
 * Emitted C:
 * printf("xi = %d\n", xi);
 *
 * This is mainly for debugging, but it's also cool to write programs
 * that actually, ya know, do something! Putting in these Builtins
 * turns Geuax into an actual usable language.
 */
class Printf extends Builtin {
    public final String format;       
    public final List<IRExpr> args;  

    public Printf(String format, List<IRExpr> args) {
        this.format = format;
        this.args = args;
    }

    @Override
    public <T> T accept(GOTOVisitor<T> v) {
        return v.visitPrintf(this);
    }
}

/**
 * Some Additional Builtin's to implement. Read and Write to/from file
 * are pretty self explanatory, but Input is a builtin for getting user
 * input. These are not implemented here or in Emitter. You will need
 * implement the code in Emitter too.
 *
 * Remember, the idea is that Geaux should have a simple "readfromfile("file")"
 * function, and the Emitter turns that into C that actually reads from the file.
 */
class ReadFromFile extends IRExpr {
    public final IRExpr path;

    public ReadFromFile(IRExpr path){
        this.path = path;
    }

    @Override
    public <T> T accept(GOTOVisitor<T> v){
        return v.visitReadFromFile(this);
    }
}

class WriteToFile extends Builtin {
    public final IRExpr path;
    public final IRExpr content;

    public WriteToFile(IRExpr path, IRExpr content){
        this.path = path;
        this.content = content;
    }

    @Override
    public <T> T accept(GOTOVisitor<T> v){
        return v.visitWriteToFile(this);
    }
}

class Input extends Builtin {
    public final IRExpr arg;

    public Input(IRExpr arg){
        this.arg = arg;
    }

    @Override
    public <T> T accept(GOTOVisitor<T> v){
        return v.visitInput(this);
    }
}

/**
 * Variable reference.
 *
 * Var looks simple, but its probably the hardest instruction to work with.
 * When lowering to GOTO, you will run into the problem that two variables
 * in the Geaux program have the same name. Example:
                     var int x = 0;
                     fun int func(int x) {
                     ...
                     }
 * If the final C file declares all variables as global, then this is going
 * to cause an error when we try to run gcc. If we do not intervene and rename
 * one of these variables, the following code will be emitted:

                    int x;
                    int x;
                    int func() {
                    ...
                    }

 * The solution is to rename ALL variables with unique names. Inside the Scope object,
 * assign each var entry a unique name. Change every reference to that variable to the new
 * name. This ensure no name clashing.
 */
class Var extends IRExpr {
    public final String name;
    public String typeName;

    public Var(String name, GOTOType type) {
        this.name = name;
        this.type = type;
        this.typeName = null;
    }

    @Override
    public <T> T accept(GOTOVisitor<T> v) {
        return v.visitVar(this);
    }
}

/**
 * Literal constant.
 *
 * Represents either a hardcoded int or string.
 */
class GOTOLiteral extends IRExpr {
    public final Object value;  

    public GOTOLiteral(Object value, GOTOType type) {
        this.value = value;
        this.type = type;
    }

    @Override
    public <T> T accept(GOTOVisitor<T> v) {
        return v.visitLiteral(this);
    }
}

/**
 * Binary operation.
 *
 * Represents operations like +, *, <, etc.
 *
 * Example emitted C:
 * y + z
 * x < 5
 */
class GOTOBinOp extends IRExpr {
    public final String op;
    public final IRExpr left, right;

    public GOTOBinOp(String op, IRExpr left, IRExpr right, GOTOType type) {
        this.op = op;
        this.left = left;
        this.right = right;
        this.type = type;
    }

    @Override
    public <T> T accept(GOTOVisitor<T> v) {
        return v.visitGOTOBinOp(this);
    }
}

/**
 * Unary operation.
 *
 * Example emitted C:
 * +y
 * *p
 */
class UnaryOp extends IRExpr {
    public final String op;
    public final IRExpr expr;

    public UnaryOp(String op, IRExpr expr, GOTOType type) {
        this.op = op;
        this.expr = expr;
        this.type = type;
    }

    @Override
    public <T> T accept(GOTOVisitor<T> v) {
        return v.visitUnaryOp(this);
    }
}

/**
 * Function call.
 *
 * Example emitted C:
 * f()

 * Functions do not need arguments, because all
 * variables are global in the final C file.
 */
class Call extends IRExpr {
    public final String func;

    public Call(String func, GOTOType rettype) {
        this.func = func;
        this.type = rettype;
    }

    @Override
    public <T> T accept(GOTOVisitor<T> v) {
        return v.visitCall(this);
    }
}

/**
 * Array load: reading from an array.
 *
 * Example emitted C:
 * (*arr + i)
 */
class ArrayLoad extends IRExpr {
    public final Var array;
    public final IRExpr index;

    public ArrayLoad(Var array, IRExpr index, GOTOType type) {
        this.array = array;
        this.index = index;
        this.type = type; 
    }

    @Override
    public <T> T accept(GOTOVisitor<T> v) {
        return v.visitArrayLoad(this);
    }
}

/**
 * Assignment.
 *
 * Example emitted C:
 * x = 5;
 */
class Assign extends IRStmt {
    public final Var target;
    public final IRExpr value;

    public Assign(Var target, IRExpr value) {
        this.target = target;
        this.value = value;
    }

    @Override
    public <T> T accept(GOTOVisitor<T> v) {
        return v.visitAssign(this);
    }
}

/**
 * Array store: writing to an array.
 *
 * Example emitted C:
 * (*arr + i) = x;
 */
class ArrayStore extends IRStmt {
    public final Var array;
    public final IRExpr index;
    public final IRExpr value;

    public ArrayStore(Var array, IRExpr index, IRExpr value) {
        this.array = array;
        this.index = index;
        this.value = value; 
    }

    @Override
    public <T> T accept(GOTOVisitor<T> v) {
        return v.visitArrayStore(this);
    }
}

/**
 * Array allocation.
 *
 * Example emitted C:
 * arr = realloc(&arr, size * sizeof({this.array.type}));
 *
 * Note: The type in the sizeof call is the type of the Var array member.
 *
 * Be mindful that Geaux Arrays are actually dynamic lists. If a user tries to store
 * an out of bounds index, YOU need to resize the array by calling ArrayAlloc to make that work
 * If they try to read from an out of bounds index, then the program should throw an
 * error.
 */
class ArrayAlloc extends IRStmt {
    public final Var array;
    public final IRExpr size;

    public ArrayAlloc(Var array, IRExpr size) {
        this.array = array;
        this.size = size;
    }

    @Override
    public <T> T accept(GOTOVisitor<T> v) {
        return v.visitArrayAlloc(this);
    }
}

/**
 * If statement implemented with goto statements
 *
 * Example emitted C:
 * if (x < 5) goto LABEL_TRUE;
 * goto LABEL_FALSE;

 * Your program is responsible for placing the true and false labels in the proper
 * locations. That does not happen automatically. 
 */
class GOTOIfStmt extends IRStmt {
    public final IRExpr cond;
    public final String trueLabel;
    public final String falseLabel;

    public GOTOIfStmt(IRExpr cond, String trueLabel, String falseLabel) {
        this.cond = cond;
        this.trueLabel = trueLabel;
        this.falseLabel = falseLabel;
    }

    @Override
    public <T> T accept(GOTOVisitor<T> v) {
        return v.visitGOTOIfStmt(this);
    }
}

/**
 * Goto statement.
 *
 * Example emitted C:
 * goto LABEL;
 */
class Goto extends IRStmt {
    public final String label;

    public Goto(String label) { 
        this.label = label; 
    }

    @Override
    public <T> T accept(GOTOVisitor<T> v) {
        return v.visitGoto(this);
    }
}

/**
 * Label definition.
 *
 * Example emitted C:
 * LABEL:
 */
class Label extends IRStmt {
    public final String name;

    public Label(String name) { 
        this.name = name; 
    }

    @Override
    public <T> T accept(GOTOVisitor<T> v) {
        return v.visitLabel(this);
    }
}

/**
 * Return statement.
 *
 * Example emitted C:
 * return x;
 */
class GOTOReturnStmt extends IRStmt {
    public final IRExpr value;

    public GOTOReturnStmt(IRExpr value) { 
        this.value = value; 
    }

    @Override
    public <T> T accept(GOTOVisitor<T> v) {
        return v.visitGOTOReturnStmt(this);
    }
}
// ─── ARRAYS ──────────────────────────────────────────────────────────────────


class ArrayExpr extends IRExpr {
    public final ArrayList<IRExpr> initElems;
    public int size = 0;
    public ArrayExpr(ArrayList<IRExpr> initElems){
        this.initElems = initElems;
    }
}

// ─── STRUCTS ─────────────────────────────────────────────────────────────────

/**
 * A single field inside a struct or union definition.
 * Not a GOTO node itself — just a data carrier used by StructTypeDef/UnionTypeDef.
 *
 * Example:  int x;   or   char* name;
 */
class StructField {
    public final String name;
    public final GOTOType gotoType;

    public StructField(String name, GOTOType gotoType) {
        this.name     = name;
        this.gotoType = gotoType;
    }
}

/**
 * Struct type definition — emitted once at the top of the C file.
 *
 * Example emitted C:
 * typedef struct {
 *     int x;
 *     char* name;
 * } Point;
 */
class StructTypeDef extends GOTONode {
    public final String name;
    public final ArrayList<StructField> fields;

    public StructTypeDef(String name, ArrayList<StructField> fields) {
        this.name   = name;
        this.fields = fields;
    }

    @Override
    public <T> T accept(GOTOVisitor<T> v) {
        return v.visitStructTypeDef(this);
    }
}

/**
 * Struct variable declaration with optional initializer.
 *
 * Example emitted C (no init):
 * Point p;
 *
 * Example emitted C (with init):
 * Point p = {1, "hello"};
 */
class StructInit extends IRStmt {
    public final Var var;
    public final String structName;
    public final ArrayList<IRExpr> initExprs;  // null if no initializer

    public StructInit(Var var, String structName, ArrayList<IRExpr> initExprs) {
        this.var        = var;
        this.structName = structName;
        this.initExprs  = initExprs;
    }

    @Override
    public <T> T accept(GOTOVisitor<T> v) {
        return v.visitStructInit(this);
    }
}

/**
 * Field read:  obj.field  or  ptr->field
 *
 * Example emitted C:
 * p.x
 * ptr->x
 */
class FieldLoad extends IRExpr {
    public final IRExpr base;
    public final String field;
    public final boolean isPointer;  // true = ->, false = .

    public FieldLoad(IRExpr base, String field, boolean isPointer, GOTOType type) {
        this.base      = base;
        this.field     = field;
        this.isPointer = isPointer;
        this.type      = type;
    }

    @Override
    public <T> T accept(GOTOVisitor<T> v) {
        return v.visitFieldLoad(this);
    }
}

/**
 * Field write:  obj.field = value  or  ptr->field = value
 *
 * Example emitted C:
 * p.x = 5;
 * ptr->x = 5;
 */
class FieldStore extends IRStmt {
    public final IRExpr base;
    public final String field;
    public final boolean isPointer;
    public final IRExpr value;

    public FieldStore(IRExpr base, String field, boolean isPointer, IRExpr value) {
        this.base      = base;
        this.field     = field;
        this.isPointer = isPointer;
        this.value     = value;
    }

    @Override
    public <T> T accept(GOTOVisitor<T> v) {
        return v.visitFieldStore(this);
    }
}

// ─── UNIONS ──────────────────────────────────────────────────────────────────

/**
 * Union type definition — emitted once at the top of the C file.
 *
 * Example emitted C:
 * typedef union {
 *     int i;
 *     char* s;
 * } MyUnion;
 */
class UnionTypeDef extends GOTONode {
    public final String name;
    public final ArrayList<StructField> variants;  // reuses StructField

    public UnionTypeDef(String name, ArrayList<StructField> variants) {
        this.name     = name;
        this.variants = variants;
    }

    @Override
    public <T> T accept(GOTOVisitor<T> v) {
        return v.visitUnionTypeDef(this);
    }
}

/**
 * Union variable declaration with optional initializer.
 *
 * Example emitted C (no init):
 * MyUnion u;
 *
 * Example emitted C (with init, setting the int variant):
 * MyUnion u;
 * u.i = 42;
 */
class UnionInit extends IRStmt {
    public final Var var;
    public final String unionName;
    public final String activeField;   // which variant is being initialized
    public final IRExpr initExpr;      // null if no initializer

    public UnionInit(Var var, String unionName, String activeField, IRExpr initExpr) {
        this.var         = var;
        this.unionName   = unionName;
        this.activeField = activeField;
        this.initExpr    = initExpr;
    }

    @Override
    public <T> T accept(GOTOVisitor<T> v) {
        return v.visitUnionInit(this);
    }
}

/**
 * This instruction is meant to only be used when adding a value to
 * a parameter's stack.
 * 
 * Example of emitted C:
 * _x1 = realloc(&_x1, (_x2+1) * sizeof(int);
 * _x1[_x2] = value;
 * _x2++;
 * 
 * Note: Parameters only work for parameters of the int type as of now
 */

class StackPushOp extends IRStmt {
    public final String stack;
    public final String stackPtr;
    public final IRExpr value;

    public StackPushOp(String stack, String stackPtr, IRExpr value){
        this.stack = stack;
        this.stackPtr = stackPtr;
        this.value = value;
    }

    @Override
    public <T> T accept(GOTOVisitor<T> v){
        return v.visitStackPushOp(this);
    }
}

/**
 * This instruction is meant to only be used when removing a value
 * from a parameter's stack.
 * 
 * Example of emitted C:
 * _x2--;
 */

class StackPopOp extends IRStmt {
    public final String stackPtr;

    public StackPopOp(String stackPtr){
        this.stackPtr = stackPtr;
    }

    @Override
    public <T> T accept(GOTOVisitor<T> v){
        return v.visitStackPopOp(this);
    }

}