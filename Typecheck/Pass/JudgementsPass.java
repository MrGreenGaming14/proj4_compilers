package Typecheck.Pass;
import Typecheck.Types.*;
import Typecheck.SymbolTable.*;
import Absyn.Exp;
import Typecheck.TypeCheckException;
import java.util.ArrayList;

// This pass implements the type rules.
// Some of the logic has been implemented for you in the Types.
// Check out the "canAccept" functions.
public class JudgementsPass extends ScopePass<Type> {

   protected Type defaultReturn = null;

   public JudgementsPass(Scope s) {
      super(s);
   }
   
   @Override
   public Type visitVarDecl(Absyn.VarDecl node){
      //System.out.println(node.print(0));
      Type varType = node.type.typeAnnotation;
      //System.out.println("varType = "+varType.toString()+"\n\n");
      Type expType = visit(node.init);
      if(expType != null){
         if(!varType.canAccept(expType)){
            throw new TypeCheckException("Tried to initialize var ("+node.name+") of type "+varType.toString()+" as incompatible type "+expType.toString());
         }
         if (varType instanceof ARRAY && expType instanceof LIST) {
             LIST list = (LIST) expType;
             int expectedSize = ((LIST) list.typelist.get(0)).typelist.size();
             for (int i = 1; i < list.typelist.size(); i++) {
                 int actualSize = ((LIST) list.typelist.get(i)).typelist.size();
                 if (actualSize != expectedSize) {
                     throw new TypeCheckException("Inconsistent array dimensions in " + node.name);
                 }
             }
         }
      }

      return defaultReturn;
   }

   //Rule 10:
   @Override
   public Type visitFunDecl(Absyn.FunDecl node){
      //System.out.println(node.print(0));
      Scope originalscope = currentscope;
		currentscope = node.scope;
      visit(node.type);
		visit(node.params);
		Type bodyType = visit(node.body);
      Type funType = node.type.typeAnnotation;
      /*
      System.out.println(node.name+":");
      if(bodyType != null){ System.out.println("bodyType: "+bodyType.getClass()); }
      else{System.out.println("bodyType: null");}
      System.out.println("funType: "+funType.getClass());
      */
      if(funType instanceof VOID){
         if(bodyType != null){
            throw new TypeCheckException("Function "+node.name+" of return type void should not return a value.");
         }
      }
      else{
         if(bodyType == null){
            throw new TypeCheckException("Function "+node.name+" of return type "+funType.toString()+" must return a value.");
         }
         else if((bodyType.getClass() != funType.getClass())){
            throw new TypeCheckException("Function "+node.name+" expecting return type "+funType.toString()+" but got "+bodyType.getClass());
         }
      }
		node.scope = currentscope;
		currentscope = originalscope;
      return defaultReturn;
   }

   @Override
	public Type visitCompStmt(Absyn.CompStmt node) {
      Type returnType = visit(node.decl_list);
      if(returnType == null){
         returnType = visit(node.stmt_list);
      }
      else{
         visit(node.stmt_list);
      }
		return returnType;
	}

   @Override
   public Type visitDeclList(Absyn.DeclList node) {
      Type returnType = null;
      for (Absyn.Decl d : node.list) {
         Type type = visit(d);
         if(returnType == null && type != null){
            returnType = type;
         }
         if(type != null && !(type instanceof VOID)){
            System.out.println(type.toString());
            returnType = type;
         }
		}
      return returnType;
	}

   @Override
	public Type visitStmtList(Absyn.StmtList node) {
      Type returnType = null;
      for (Absyn.Stmt d : node.list) {
         Type type = visit(d);
         if(returnType == null && type != null){
            returnType = type;
         }
         if(type != null && !(type instanceof VOID)){
            returnType = type;
         }
		}
      return returnType;
	}

   @Override
   public Type visitIfStmt(Absyn.IfStmt node){
      Scope originalscope = currentscope;
		currentscope = node.scope;
      Type checkType = visit(node.expression);
		Type type1 = visit(node.if_statement);
		Type type2 = visit(node.else_statement);
      //Rule 13:
      if(checkType != null && !(checkType instanceof INT)){
         if(checkType != null){
            throw new TypeCheckException("If statement must contain numerical conditional, contains type "+checkType.toString());
         }
      }
      //Back to Rule 10:
      if((type1 != null) && (type2 != null)){
         if(type1.getClass() != type2.getClass()){
            throw new TypeCheckException("Mismatch return type");
         }
      }
      else{
         return null;
      }
		node.scope = currentscope;
		currentscope = originalscope;
		return type1;
   }

   @Override
   public Type visitReturnStmt(Absyn.ReturnStmt node){
		return visit(node.expression);
   }

   @Override
   public Type visitDecLit(Absyn.DecLit node){
      return new INT();
   }

   @Override
   public Type visitStrLit(Absyn.StrLit node){
      return new STRING();
   }

   @Override
   public Type visitID(Absyn.ID node){
      if(node.value.equals("null")){
         return new VOID();
      }
      else{
         return this.currentscope.getVar(node.value).type;
      }
   }

   @Override
   public Type visitType(Absyn.Type node){
      return node.typeAnnotation;
   }

   //Many rules need these to get the correct type out of expressions:
   @Override
   public Type visitBinOp(Absyn.BinOp node){
      Type type1 = visit(node.left);
      Type type2 = visit(node.right);
      if((type1.getClass() != type2.getClass()) &&
         !((type1 instanceof POINTER) && (type2 instanceof INT)) &&
         !(((type1 instanceof INT) && (type2 instanceof POINTER)))){
         throw new TypeCheckException("BinOp expression found to have conflicting types: "+type1.toString()+" and "+type2.toString());
      }

      // Should fix rule 8 problems
      if(type1 instanceof STRING || type2 instanceof STRING) {
        throw new TypeCheckException("Math operations only accept numbers. No string concatenation allowed.");
      }
		return type1;
   }

   //Rule 9:
   @Override
   public Type visitFunExp(Absyn.FunExp node){
      //System.out.println(node.print(0));
      Type expType = visit(node.params);
      Absyn.ID funcId = (Absyn.ID)node.name;
      FunSymbol funSym = this.currentscope.getFun(funcId.value);
      if(expType == null){
         if(funSym.params.typelist.size() != 0){
            throw new TypeCheckException("Function "+funcId.value+" takes parameters but initialized with none");
         }
      }
      else if(!funSym.params.canAccept(expType)){
         throw new TypeCheckException("Function "+funcId.value+" given invalid parameters");
      }
      return this.currentscope.getFun(funcId.value).returnType;
   }

   @Override
   public Type visitUnaryExp(Absyn.UnaryExp node){
      Type type = visit(node.exp);
      if(node.prefix.equals("*")){ //dereference
         if(type instanceof POINTER){
            POINTER ptr = (POINTER)type;
            return ptr.type;
         }
         else{
            throw new TypeCheckException("Tried to dereference non-pointer type "+type.toString());
         }
      }
      else if(node.prefix.equals("&")){ //reference
         return new POINTER(type);
      }
		return defaultReturn;
   }

   @Override
   public Type visitExpList(Absyn.ExpList node){
      Type returnType = defaultReturn;
      ArrayList<Type> typelist = new ArrayList<Type>();
      for (Exp e : node.list) {
         typelist.add(visit(e));
		}
      if(typelist.size() == 0){ //empty
         return defaultReturn;
      }
      if(typelist.size() == 1){ //array
         returnType = new ARRAY(typelist.get(0));
      }
      else{
         returnType = new LIST(typelist);
      }
      return returnType;
   }

}
