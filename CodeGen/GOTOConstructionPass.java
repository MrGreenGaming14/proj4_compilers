package CodeGen;
import Typecheck.Types.*;
import Typecheck.TypeCheckException;
import Absyn.*;
import java.util.ArrayList;

public class GOTOConstructionPass extends Pass<ArrayList<GOTO>> {

   protected GOTO defaultReturn = null;

   public Program GOTOprog;

   public GOTOConstructionPass(Program GOTOprog) {
      this.GOTOprog = GOTOprog;
   }

   @Override
   public ArrayList<GOTO> visitVarDecl(VarDecl node){
      ArrayList<GOTO> result = new ArrayList<GOTO>();
      Typecheck.Types.Type varType = node.type.typeAnnotation;
      GOTOType gotoVarType;
      if(varType instanceof INT){
         gotoVarType = GOTOType.INT;
      }
      else if(varType instanceof STRING){
         gotoVarType = GOTOType.STRING;
      }
      else if(varType instanceof LIST || varType instanceof ARRAY){
         gotoVarType = GOTOType.INTARRAY;
      }
      else{
         throw new TypeCheckException("GOTO only accepts variables of int, string, or int array");
      }
      result.add(new Var(node.name, gotoVarType));
      return result;
   }

   @Override
   public ArrayList<GOTO> visitFunDecl(FunDecl node){
      ArrayList<GOTO> result = new ArrayList<GOTO>();



      return result;
   }

}
