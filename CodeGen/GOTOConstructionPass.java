package CodeGen;
import Typecheck.Types.*;
import Typecheck.TypeCheckException;
import Absyn.*;
import java.util.ArrayList;
import java.util.List;

public class GOTOConstructionPass extends Pass<IRExpr> {

   protected IRExpr defaultReturn = null;

   public Program GOTOprog;
   public Function mainFunction;
   protected Function currentFunction;

   public GOTOConstructionPass(Program GOTOprog) {
      this.GOTOprog = GOTOprog;
      this.mainFunction = new Function("main","void");
      this.currentFunction = mainFunction;
   }

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
         System.out.println(varType.getClass());
         throw new TypeCheckException("CodeGen only accepts variables of int, string, or int array");
      }
      return gotoVarType;
   }

   public int initializeArray(Var arrVar, ArrayExpr arrExpr, int i){
      int index = i;
      for(IRExpr elem : arrExpr.initElems){
         if(elem instanceof ArrayExpr){ //must recurse
            index = initializeArray(arrVar, (ArrayExpr)elem, index);
         }
         else{ //must be int
            currentFunction.instr.add(new ArrayStore(arrVar, new GOTOLiteral(index, GOTOType.INT), elem));
            index++;
         }
      }
      return index;
   }

   @Override
   public IRExpr visitVarDecl(VarDecl node){
      if(node.name.equals("_x3")){
         System.out.println(node.print(0));
      }
      visit(node.type);
      IRExpr init = visit(node.init);
      Typecheck.Types.TypecheckType varType = node.type.typeAnnotation;
      GOTOType gotoVarType = typecheckTypeToGOTO(varType);
      if(init != null){
         if(gotoVarType == GOTOType.INTARRAY){
            System.out.println(node.print(0));
            Var arrVar = new Var(node.name, GOTOType.INT);
            ArrayExpr arrExpr = (ArrayExpr)init;
            currentFunction.instr.add(new ArrayAlloc(arrVar, new GOTOLiteral(arrExpr.size, GOTOType.INT)));
            initializeArray(arrVar, arrExpr, 0);
         }
         else{
            Assign assign = new Assign(new Var(node.name, gotoVarType), init);
            currentFunction.instr.add(assign);
         }
      }
      else{
         Assign assign = new Assign(new Var(node.name, gotoVarType), null);
         currentFunction.instr.add(assign);
      }
      return defaultReturn;
   }

   @Override
   public IRExpr visitFunDecl(FunDecl node){
      TypecheckType tcType;
      String retType = "";

      visit(node.type);
      tcType = node.type.typeAnnotation;
      retType = typecheckTypeToC(tcType);

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
   public IRExpr visitDecLit(DecLit node){
      GOTOType gotoType = typecheckTypeToGOTO(node.typeAnnotation);
      return new GOTOLiteral(node.value, gotoType);
   }

   @Override
   public IRExpr visitStrLit(StrLit node){
      GOTOType gotoType = typecheckTypeToGOTO(node.typeAnnotation);
      return new GOTOLiteral(node.value, gotoType);
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
      //System.out.println(node.print(0));
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

   @Override
   public IRExpr visitExprStmt(ExprStmt node){
      IRExpr expr = visit(node.expression);
      if(expr instanceof GOTOBinOp){
         GOTOBinOp binOpExpr = (GOTOBinOp)expr;
         Var leftVar = (Var)binOpExpr.left;
         IRExpr rightExpr = (IRExpr)binOpExpr.right;
         currentFunction.instr.add(new Assign(leftVar, rightExpr));
      }
      else if(expr instanceof UnaryOp){
         UnaryOp unOpExpr = (UnaryOp)expr;
         currentFunction.instr.add(unOpExpr);
      }
      else if(expr instanceof Call){
         Call call = (Call)expr;
         currentFunction.instr.add(call);
      }
      return defaultReturn;
   }

   @Override
   public IRExpr visitFunExp(FunExp node){
      TypecheckType tcType;
      GOTOType gotoType;
      ID funcName = (ID)node.name;

      if(funcName.value.equals("printf")){
         List<IRExpr> args = new ArrayList<IRExpr>();
         for(Exp exp : node.params.list){
            args.add(visit(exp));
         }
         GOTOLiteral formatGOTO = (GOTOLiteral)args.get(0);
         String format = (String)formatGOTO.value;
         args.remove(0);
         currentFunction.instr.add(new Printf(format, args));
         return defaultReturn;
      }

      tcType = node.typeAnnotation;
      gotoType = typecheckTypeToGOTO(tcType);


      return new Call(funcName.value, gotoType);
   }

   @Override
   public IRExpr visitStructDecl(StructDecl node){
      DeclList absynFields = node.body;
      ArrayList<StructField> fields = new ArrayList<StructField>();
      StructMember sm;
      for(Decl decl : absynFields.list){
         sm = (StructMember)decl;
         fields.add(new StructField(sm.name, typecheckTypeToGOTO(sm.type.typeAnnotation)));
      }
      StructTypeDef sf = new StructTypeDef(node.name, fields);
      GOTOprog.structs.add(sf);
      return defaultReturn;
   }

   @Override
   public IRExpr visitUnionDecl(UnionDecl node){
      DeclList absynFields = node.body;
      ArrayList<StructField> variants = new ArrayList<StructField>();
      UnionMember sm;
      for(Decl decl : absynFields.list){
         sm = (UnionMember)decl;
         variants.add(new StructField(sm.name, typecheckTypeToGOTO(sm.type.typeAnnotation)));
      }
      UnionTypeDef sf = new UnionTypeDef(node.name, variants);
      GOTOprog.unions.add(sf);
      return defaultReturn;
   }

   @Override
   public IRExpr visitType(Type node){
      return defaultReturn;
   }

   @Override
   public IRExpr visitExpList(ExpList node){
      ArrayList<IRExpr> arrList = new ArrayList<IRExpr>();
      for(Exp exp : node.list){
         arrList.add(visit(exp));
      }
      ArrayExpr result = new ArrayExpr(arrList);
      if(arrList.get(0) instanceof ArrayExpr){
         ArrayExpr innerArr = (ArrayExpr)arrList.get(0);
         result.size = arrList.size() * innerArr.size;
      }
      else{
         result.size = arrList.size();
      }
      return result;
   }
   
}
