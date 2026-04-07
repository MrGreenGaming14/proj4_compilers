package Typecheck.Pass;
import Absyn.*;
import Typecheck.Types.Type;
import Typecheck.Types.LIST;
import Typecheck.SymbolTable.*;
import Typecheck.TypeCheckException;
import java.util.ArrayList;

public class FunAndVarScopePass extends ScopePass<Void> {

   public FunAndVarScopePass(Scope s) {
      super(s);
   }
   private void enforceRule11(String name) {
      if (this.currentscope.hasLocalVar(name) || this.currentscope.hasLocalFun(name)) {
         throw new TypeCheckException("Name '" + name + "' is already used in this scope (variable/function conflict)");
      }
   }
// Hint: Parameters behave like variables inside the function scope.
// 1. Ensure no function with this name already exists in the current scope.
// 2. Add the parameter as a variable symbol.
// 3. Use the parameter's typeAnnotation as its type.
   @Override
	public Void visitParameter(Parameter node) {
      enforceRule11(node.name);
      // Here is some code I used. You might find it useful:
      if (this.currentscope.hasLocalFun(node.name)) {
         throw new TypeCheckException("Tried to define var ("+node.name+") but fun with same name already exists");
      }
      VarSymbol vs = new VarSymbol(node.name, node.type.typeAnnotation);
      this.currentscope.addVar(node.name, vs);
      /*
      System.out.println("visitParameter:");
      System.out.println("name: "+node.name);
      System.out.println(node.type.typeAnnotation.toString());
      System.out.println("get from symbol table");
      System.out.println(this.currentscope.getVar(node.name).type.toString());
      System.out.println("--------------------------");
      */
      return defaultReturn;
	}
// Hint: Functions must be registered in the current scope before visiting their body.
// 1. Ensure no variable with the same name exists in the current scope.
// 2. Collect the types of all parameters.
// 3. Construct the function type (parameter types → return type).
// 4. Add the function symbol to the current scope.
// 5. Enter the function’s scope and visit its contents.
   @Override
   public Void visitFunDecl(FunDecl node) {
      enforceRule11(node.name);
      if(this.currentscope.hasLocalVar(node.name)){
         throw new TypeCheckException("Tried to define fun ("+node.name+") but var with same name already exists");
      }
      ArrayList<Type> arrlist = new ArrayList<>();
      Parameter param;
      for(int i = 0; i < node.params.list.size(); i++){
         param = (Parameter)node.params.list.get(i);
         arrlist.add(param.type.typeAnnotation);
      }
      FunSymbol fs = new FunSymbol(node.name, new LIST(arrlist), node.type.typeAnnotation);
      this.currentscope.addFun(node.name, fs);
      Scope originalscope = this.currentscope;
      this.currentscope = node.scope;
      visit(node.type);
      visit(node.params);
      visit(node.body);
      this.currentscope = originalscope;
      /*
      System.out.println("visitFunDecl:");
      System.out.println("name: "+node.name);
      System.out.println("get from symbol table");
      System.out.println("params: "+this.currentscope.getFun(node.name).params.toString());
      System.out.println("return type: "+this.currentscope.getFun(node.name).returnType.toString());
      System.out.println("--------------------------");
      */
      return defaultReturn;
   }
// Hint: Struct members are variables within the struct's scope.
// 1. Ensure no function with this name exists in the current scope.
// 2. Add the member as a variable symbol using its annotated type.
   @Override
   public Void visitStructMember(StructMember node) {
      enforceRule11(node.name);
      if(this.currentscope.hasLocalFun(node.name)){
         throw new TypeCheckException("Tried to define fun ("+node.name+") but fun with same name already exists");
      }
      VarSymbol vs = new VarSymbol(node.name, node.type.typeAnnotation);
      this.currentscope.addVar(node.name, vs);
      /*
      System.out.println("visitStructDecl:");
      System.out.println("name: "+node.name);
      System.out.println("get from symbol table");
      System.out.println("type: "+this.currentscope.getVar(node.name).type.toString());
      System.out.println("--------------------------");
      */
      return defaultReturn;
   }
// Hint: Union members behave like variables within the union scope.
// 1. Ensure no function with this name exists in the current scope.
// 2. Add the member as a variable symbol using its annotated type.

    // Note: This is only true for now. Union's will get special treatement
    // later, but for now we treat them as the same as structs. 
   @Override
   public Void visitUnionMember(UnionMember node) {
      enforceRule11(node.name);
      if(this.currentscope.hasLocalFun(node.name)){
         throw new TypeCheckException("Tried to define fun ("+node.name+") but fun with same name already exists");
      }
      VarSymbol vs = new VarSymbol(node.name, node.type.typeAnnotation);
      this.currentscope.addVar(node.name, vs);
      /*
      System.out.println("visitUnionDecl:");
      System.out.println("name: "+node.name);
      System.out.println("get from symbol table");
      System.out.println("type: "+this.currentscope.getVar(node.name).type.toString());
      System.out.println("--------------------------");
      */
      return defaultReturn;
   }
// Hint: Variable declarations introduce a new variable in the current scope.
// 1. Ensure no function with this name exists in the current scope.
// 2. Add the variable symbol using its annotated type.
   @Override
   public Void visitVarDecl(VarDecl node){
      enforceRule11(node.name);
      if(this.currentscope.hasLocalFun(node.name)){
         throw new TypeCheckException("Tried to define fun ("+node.name+") but fun with same name already exists");
      }
      VarSymbol vs = new VarSymbol(node.name, node.type.typeAnnotation);
      this.currentscope.addVar(node.name, vs);
      /*
      System.out.println("visitVarDecl:");
      System.out.println("name: "+node.name);
      System.out.println("get from symbol table");
      System.out.println("type: "+this.currentscope.getVar(node.name).type.toString());
      System.out.println("--------------------------");
      */
      return defaultReturn;
   }
}
