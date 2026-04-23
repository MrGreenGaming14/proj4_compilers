package CodeGen;
import Typecheck.Types.*;
import Typecheck.TypeCheckException;
import Absyn.*;
import java.util.ArrayList;

public class GOTOConstructionPass extends Pass<IRExpr> {

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

   protected IRExpr defaultReturn = null;

   public Program GOTOprog;
   public Function mainFunction;
   protected Function currentFunction;

   public GOTOConstructionPass(Program GOTOprog) {
      this.GOTOprog = GOTOprog;
      this.mainFunction = new Function("main","void");
      this.currentFunction = mainFunction;
   }

   @Override
   public IRExpr visitVarDecl(VarDecl node){
      visit(node.type);
      IRExpr init = visit(node.init);
      Typecheck.Types.TypecheckType varType = node.type.typeAnnotation;
      GOTOType gotoVarType = typecheckTypeToGOTO(varType);
      Assign assign;
      if(init != null){
         assign = new Assign(new Var(node.name, gotoVarType), init);
      }
      else{
         assign = new Assign(new Var(node.name, gotoVarType), null);
      }
      currentFunction.instr.add(assign);
      return defaultReturn;
   }

   @Override
   public IRExpr visitFunDecl(FunDecl node){
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
   public IRExpr visitBinOp(BinOp node){
      //System.out.println(node.print(0));
      IRExpr left = visit(node.left);
      IRExpr right = visit(node.right);
      return new GOTOBinOp(node.oper, left, right, left.type);
   }

   @Override
   public IRExpr visitUnaryExp(UnaryExp node){
      IRExpr expr = visit(node.exp);
      return new UnaryOp(node.prefix,expr,expr.type);
   }

   @Override
   public IRExpr visitLiteral(Literal node){
      GOTOType gotoType = typecheckTypeToGOTO(node.typeAnnotation);
      return new GOTOLiteral(node.value, gotoType);
   }

   @Override
   public IRExpr visitID(ID node){
      GOTOType gotoType = typecheckTypeToGOTO(node.typeAnnotation);
      return new Var(node.value, gotoType);
   }

   @Override
   public IRExpr visitReturnStmt(ReturnStmt node){
      IRExpr expr = visit(node.expression);
      GOTOReturnStmt retStmt = new GOTOReturnStmt(expr);
      currentFunction.instr.add(retStmt);
      return defaultReturn;
   }

   @Override
   public IRExpr visitIfStmt(IfStmt node){
      String labelTrue;
      String labelFalse;
      String labelFinish;
      IRExpr expr = visit(node.expression);
      if(node.else_statement instanceof EmptyStmt){ //only if
         labelTrue = GOTOprog.getUniqueLabelName();
         labelFinish = GOTOprog.getUniqueLabelName();
         currentFunction.instr.add(new GOTOIfStmt(expr,labelTrue,labelFinish));
         currentFunction.instr.add(new Label(labelTrue));
         visit(node.if_statement);
         currentFunction.instr.add(new Label(labelFinish));
      }
      else{ //if + else
         labelTrue = GOTOprog.getUniqueLabelName();
         labelFalse = GOTOprog.getUniqueLabelName();
         labelFinish = GOTOprog.getUniqueLabelName();
         currentFunction.instr.add(new GOTOIfStmt(expr,labelTrue,labelFalse));
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
   public IRExpr visitAssignExp(AssignExp node){
      IRExpr left = visit(node.left);
      IRExpr right = visit(node.right);
      return new GOTOBinOp("==",left,right,left.type);
   }

   @Override
   public IRExpr visitWhileStmt(WhileStmt node){
      String labelStart;
      String labelFinish;
      IRExpr expr = visit(node.expression);
      labelStart = GOTOprog.getUniqueLabelName();
      labelFinish = GOTOprog.getUniqueLabelName();
      currentFunction.instr.add(new GOTOIfStmt(expr,labelStart,labelFinish));
      currentFunction.instr.add(new Label(labelStart));
      visit(node.statement);
      currentFunction.instr.add(new GOTOIfStmt(expr,labelStart,labelFinish));
      currentFunction.instr.add(new Label(labelFinish));
      return defaultReturn;
   }
}
