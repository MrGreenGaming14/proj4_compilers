package CodeGen;
import Absyn.*;
import Typecheck.TypeCheckException;
import Typecheck.Types.*;
import java.util.ArrayList;


public class GOTOVariableRenamingPass extends ScopePass<Void> {

   protected Void defaultReturn = null;

   public Scope globalscope;
   public Program GOTOprog;

   public GOTOType typecheckTypeToGOTO(TypecheckType varType){
      GOTOType gotoVarType;
      if(varType instanceof POINTER){
         gotoVarType = GOTOType.INT;
      }
      else if(varType instanceof INT){
         gotoVarType = GOTOType.INT;
      }
      else if(varType instanceof STRING){
         gotoVarType = GOTOType.STRING;
      }
      else if(varType instanceof LIST || varType instanceof ARRAY){
         gotoVarType = GOTOType.INTARRAY;
      }
      else{
         System.out.println(varType.getClass());
         throw new TypeCheckException("CodeGen only accepts variables of int, string, or int array");
      }
      return gotoVarType;
   }

   public GOTOVariableRenamingPass(Scope s) {
      super(s);
   }

   public GOTOVariableRenamingPass(Scope s, Program p){
      super(s);
      this.GOTOprog = p;
   }

   @Override
   public Void visitVarDecl(VarDecl node){
      visit(node.init);
      //System.out.println(node.init.print(0));
      String new_name = GOTOprog.getUniqueVarName();
      //System.out.println("Renaming var "+node.name+" to "+new_name);
      VarSymbol vs = new VarSymbol(node.name, new_name);
      this.currentscope.addVar(node.name, vs);
      node.name = new_name;
      GOTOType gotoType = typecheckTypeToGOTO(node.type.typeAnnotation);
      GOTOprog.globals.add(new Var(node.name, gotoType));
      return defaultReturn;
   }

   @Override
	public Void visitFunDecl(FunDecl node) {
		Scope originalscope = currentscope;
      visit(node.type);
		visit(node.params);
      DeclList paramList = (DeclList)node.params;
      ArrayList<Decl> paramListArr = paramList.list;
      ArrayList<ParamSymbol> paramSymList = new ArrayList<ParamSymbol>();
      for(Decl declParam : paramListArr){
         Parameter param = (Parameter)declParam;
         String stack = GOTOprog.getUniqueVarName();
         String stackPtr = GOTOprog.getUniqueVarName();
         VarSymbol vs = new VarSymbol(param.name, stack+"["+stackPtr+"-1]");
         this.currentscope.addVar(param.name, vs);
         GOTOprog.globals.add(new Var(stack, GOTOType.INTARRAY));
         GOTOprog.globals.add(new Var(stackPtr, GOTOType.INT));
         GOTOprog.stackPtrs.add(new Var(stackPtr, GOTOType.INT));
         ParamSymbol ps = new ParamSymbol(stack, stackPtr);
         paramSymList.add(ps);
      }
      FunSymbol fs = new FunSymbol(node.name, paramSymList);
      this.currentscope.addFun(node.name, fs);
      currentscope = node.codeGenScope;
		visit(node.body);
		node.codeGenScope = currentscope;
		currentscope = originalscope;
		return defaultReturn;
	}

   @Override
	public Void visitParameter(Parameter node) {
      visit(node.type);
		return defaultReturn;
	}

   @Override
   public Void visitID(ID node){
      if(this.currentscope.hasVar(node.value)){ //if var
         node.value = this.currentscope.getVar(node.value).new_name;
      }
      //otherwise is a function
      return defaultReturn;
   }

}
