package CodeGen;
import Absyn.*;
import Typecheck.TypeCheckException;
import Typecheck.Types.*;


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
   public Void visitID(ID node){
      node.value = this.currentscope.getVar(node.value).new_name;
      return defaultReturn;
   }

}
