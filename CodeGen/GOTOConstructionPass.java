package CodeGen;
import Typecheck.Types.*;
import Typecheck.TypeCheckException;
import Absyn.*;
import java.util.ArrayList;

public class GOTOConstructionPass extends Pass<ArrayList<GOTO>> {

   public String typecheckTypeToC(TypecheckType tcType){
      String retType = "";
      String stars = "";
      if(tcType instanceof POINTER){
         POINTER ptrType = (POINTER)tcType;
         //System.out.println(ptrType.toString());
         stars = stars + "*";
         while(ptrType.type instanceof POINTER){
            ptrType = (POINTER)ptrType.type;
            stars = stars + "*";
         }
         tcType = ptrType.type;
         if(tcType instanceof STRING){
            retType = "char*";
         }
         else if(tcType instanceof INT){
            retType = "int";
         }
         else if(tcType instanceof ARRAY || tcType instanceof LIST){
            retType = "int*";
         }
      }
      else if(tcType instanceof STRING){
         retType = "char*";
      }
      else if(tcType instanceof INT){
         retType = "int";
      }
      else if(tcType instanceof ARRAY || tcType instanceof LIST){
         retType = "int*";
      }
      return retType + stars;
   }

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

   protected GOTO defaultReturn = null;

   public Program GOTOprog;

   public GOTOConstructionPass(Program GOTOprog) {
      this.GOTOprog = GOTOprog;
   }

   //construct Var objects in a different pass
   @Override
   public ArrayList<GOTO> visitVarDecl(VarDecl node){
      visit(node.type);
      visit(node.init);
      ArrayList<GOTO> result = new ArrayList<GOTO>();
      Typecheck.Types.TypecheckType varType = node.type.typeAnnotation;
      GOTOType gotoVarType = typecheckTypeToGOTO(varType);
      result.add(new Var(node.name, gotoVarType));
      return result;
   }

   @Override
   public ArrayList<GOTO> visitFunDecl(FunDecl node){
      ArrayList<GOTO> result = new ArrayList<GOTO>();
      ArrayList<GOTO> instrs;
      ArrayList<GOTO> ret;
      TypecheckType tcType;
      String retType = "";

      visit(node.type);
      tcType = node.type.typeAnnotation;
      retType = typecheckTypeToC(tcType);
      
      instrs = visit(node.params);
      ret = visit(node.body);
      if(instrs != null && ret != null){
         instrs.addAll(ret);
      }
      Function func = new Function(node.name, retType);
      func.instr = instrs;
      result.add(func);
      //System.out.println("retType = "+retType);
      return result;
   }

   @Override
   public ArrayList<GOTO> visitDeclList(DeclList node){
      //System.out.println(node.print(0));
      ArrayList<GOTO> result = new ArrayList<GOTO>();
      for(Decl d : node.list){
         ArrayList<GOTO> instr = visit(d);
         if(instr != null){
            result.addAll(instr);
         }
      }
      return result;
   }

   @Override
   public ArrayList<GOTO> visitAssignExp(AssignExp node){
      ArrayList<GOTO> result = new ArrayList<GOTO>();
      ArrayList<GOTO> left = visit(node.left);
      ArrayList<GOTO> right = visit(node.right);
      Assign assign = new Assign((Var)left.get(0), (IRExpr)right.get(0));
      result.add(assign);
      return result;
   }

   @Override
   public ArrayList<GOTO> visitBinOp(BinOp node){
      //System.out.println(node.print(0));
      ArrayList<GOTO> result = new ArrayList<GOTO>();
      ArrayList<GOTO> left = visit(node.left);
      ArrayList<GOTO> right = visit(node.right);
      IRExpr leftExpr = (IRExpr)left.get(0);
      IRExpr rightExpr = (IRExpr)right.get(0);
      GOTOBinOp gbo = new GOTOBinOp(node.oper, leftExpr, rightExpr, leftExpr.type);
      result.add(gbo);
      return result;
   }

   @Override
   public ArrayList<GOTO> visitLiteral(Literal node){
      ArrayList<GOTO> result = new ArrayList<GOTO>();
      GOTOType gotoType = typecheckTypeToGOTO(node.typeAnnotation);
      GOTOLiteral lit = new GOTOLiteral(node.value, gotoType);
      result.add(lit);
      return result;
   }

   @Override
   public ArrayList<GOTO> visitID(ID node){
      ArrayList<GOTO> result = new ArrayList<GOTO>();
      GOTOType gotoType = typecheckTypeToGOTO(node.typeAnnotation);
      Var var = new Var(node.value, gotoType);
      result.add(var);
      return result;
   }
}
