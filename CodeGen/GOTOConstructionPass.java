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

   protected ArrayList<GOTO> defaultReturn = null;

   public Program GOTOprog;
   public Function mainFunction;
   protected Function currentFunction;

   public GOTOConstructionPass(Program GOTOprog) {
      this.GOTOprog = GOTOprog;
      this.mainFunction = new Function("main","void");
      this.currentFunction = mainFunction;
   }

   @Override
   public ArrayList<GOTO> visitVarDecl(VarDecl node){
      visit(node.type);
      ArrayList<GOTO> init = visit(node.init);
      Typecheck.Types.TypecheckType varType = node.type.typeAnnotation;
      GOTOType gotoVarType = typecheckTypeToGOTO(varType);
      Assign assign;
      if(init != null){
         assign = new Assign(new Var(node.name, gotoVarType), (IRExpr)init.get(0));
      }
      else{
         assign = new Assign(new Var(node.name, gotoVarType), null);
      }
      currentFunction.instr.add(assign);
      return defaultReturn;
   }

   @Override
   public ArrayList<GOTO> visitFunDecl(FunDecl node){
      TypecheckType tcType;
      GOTOType gotoType;
      String retType = "";

      visit(node.type);
      tcType = node.type.typeAnnotation;
      retType = typecheckTypeToC(tcType);
      gotoType = typecheckTypeToGOTO(tcType);

      currentFunction.instr.add(new Call(node.name, gotoType));

      Function originalFunction = currentFunction;
      currentFunction = new Function(node.name, retType);

      //System.out.println(node.print(0));
      visit(node.body);

      GOTOprog.funcs.add(currentFunction);
      currentFunction = originalFunction;
      return defaultReturn;
   }

   @Override
   public ArrayList<GOTO> visitCompStmt(CompStmt node){
      ArrayList<GOTO> result = new ArrayList<GOTO>();
      ArrayList<GOTO> decl_list;
      ArrayList<GOTO> stmt_list;
      decl_list = visit(node.decl_list);
      stmt_list = visit(node.stmt_list);
      if(decl_list != null){
         result.addAll(decl_list);
      }
      if(stmt_list != null){
         result.addAll(stmt_list);
      }
      return result;
   }

   @Override
   public ArrayList<GOTO> visitDeclList(DeclList node){
      //System.out.println(node.print(0));
      for(Decl d : node.list){
         visit(d);
      }
      return defaultReturn;
   }

   /*
   @Override
   public ArrayList<GOTO> visitAssignExp(AssignExp node){
      ArrayList<GOTO> result = new ArrayList<GOTO>();
      ArrayList<GOTO> left = visit(node.left);
      ArrayList<GOTO> right = visit(node.right);
      Assign assign = new Assign((Var)left.get(0), (IRExpr)right.get(0));
      result.add(assign);
      if(assign != null){
         System.out.println(assign+" | "+(Var)left.get(0)+" | "+(IRExpr)right.get(0));
      }
      return result;
   }
   */

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

   @Override
   public ArrayList<GOTO> visitReturnStmt(ReturnStmt node){
      ArrayList<GOTO> expr = visit(node.expression);
      GOTOReturnStmt retStmt;
      retStmt = new GOTOReturnStmt((IRExpr)expr.get(0));
      currentFunction.instr.add(retStmt);
      return defaultReturn;
   }

   @Override
   public ArrayList<GOTO> visitIfStmt(IfStmt node){
      String labelTrue;
      String labelFalse;
      String labelFinish;
      ArrayList<GOTO> expr = visit(node.expression);
      if(node.else_statement instanceof EmptyStmt){ //only if
         labelTrue = GOTOprog.getUniqueLabelName();
         labelFinish = GOTOprog.getUniqueLabelName();
         currentFunction.instr.add(new GOTOIfStmt((IRExpr)expr.get(0),labelTrue,labelFinish));
         currentFunction.instr.add(new Label(labelTrue));
         visit(node.if_statement);
         currentFunction.instr.add(new Label(labelFinish));
      }
      else{ //if + else
         labelTrue = GOTOprog.getUniqueLabelName();
         labelFalse = GOTOprog.getUniqueLabelName();
         labelFinish = GOTOprog.getUniqueLabelName();
         currentFunction.instr.add(new GOTOIfStmt((IRExpr)expr.get(0),labelTrue,labelFalse));
         currentFunction.instr.add(new Label(labelTrue));
         visit(node.if_statement);
         currentFunction.instr.add(new Goto(labelFinish));
         currentFunction.instr.add(new Label(labelFalse));
         visit(node.else_statement);
         currentFunction.instr.add(new Label(labelFinish));
      }
      return defaultReturn;
   }

   @Override
   public ArrayList<GOTO> visitAssignExp(AssignExp node){
      ArrayList<GOTO> result = new ArrayList<GOTO>();
      ArrayList<GOTO> left = visit(node.left);
      ArrayList<GOTO> right = visit(node.right);
      IRExpr leftExpr = (IRExpr)left.get(0);
      IRExpr rightExpr = (IRExpr)right.get(0);
      result.add(new GOTOBinOp("==",leftExpr,rightExpr,leftExpr.type));
      return result;
   }
}
