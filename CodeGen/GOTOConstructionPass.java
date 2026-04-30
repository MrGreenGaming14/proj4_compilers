package CodeGen;
import Typecheck.Types.*;
import Typecheck.TypeCheckException;
import Absyn.*;
import java.util.ArrayList;
import java.util.List;

public class GOTOConstructionPass extends ScopePass<IRExpr> {

   protected IRExpr defaultReturn = null;

   public Program GOTOprog;
   protected Function currentFunction;
   protected FunSymbol paramsToFree;

   public GOTOConstructionPass(Scope s, Program GOTOprog) {
      super(s);
      this.currentscope = s;
      this.GOTOprog = GOTOprog;
      this.currentFunction = null;
      this.paramsToFree = null;
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
      else if(tcType instanceof VOID){
         retType = "void";
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

   public void paramFreeCheck(){
      if(currentFunction != null && paramsToFree != null){
         for(ParamSymbol param : paramsToFree.params){
            currentFunction.instr.add(new StackPopOp(param.stackPtr));
         }
         paramsToFree = null;
      }
   }

   public int initializeArray(Var arrVar, ArrayExpr arrExpr, int i){
      int index = i;
      for(IRExpr elem : arrExpr.initElems){
         if(elem instanceof ArrayExpr){ //must recurse
            index = initializeArray(arrVar, (ArrayExpr)elem, index);
         }
         else{ //must be int
            if(currentFunction == null){
               GOTOprog.varDecls.add(new ArrayStore(arrVar, new GOTOLiteral(index, GOTOType.INT), elem));
            }
            else{
               currentFunction.instr.add(new ArrayStore(arrVar, new GOTOLiteral(index, GOTOType.INT), elem));
            }
            index++;
         }
      }
      return index;
   }

   @Override
   public IRExpr visitVarDecl(VarDecl node){
      paramFreeCheck();
      visit(node.type);
      IRExpr init = visit(node.init);
      Typecheck.Types.TypecheckType varType = node.type.typeAnnotation;
      GOTOType gotoVarType = typecheckTypeToGOTO(varType);
      if(init != null){
         if(gotoVarType == GOTOType.INTARRAY){
            Var arrVar = new Var(node.name, GOTOType.INT);
            ArrayExpr arrExpr = (ArrayExpr)init;
            if(currentFunction == null){
               GOTOprog.varDecls.add(new ArrayAlloc(arrVar, new GOTOLiteral(arrExpr.size, GOTOType.INT)));
            }
            else{
               currentFunction.instr.add(new ArrayAlloc(arrVar, new GOTOLiteral(arrExpr.size, GOTOType.INT)));
            }
            initializeArray(arrVar, arrExpr, 0);
         }
         else{
            Assign assign = new Assign(new Var(node.name, gotoVarType), init);
            if(currentFunction == null){
               GOTOprog.varDecls.add(assign);
            }
            else{
               currentFunction.instr.add(assign);
            }
         }
      }
      return defaultReturn;
   }

   @Override
   public IRExpr visitFunDecl(FunDecl node){
      System.out.println(node.print(0));
      paramFreeCheck();
      Scope originalscope = currentscope;
		currentscope = node.codeGenScope;

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

      node.codeGenScope = currentscope;
		currentscope = originalscope;
      return defaultReturn;
   }

   @Override
   public IRExpr visitBinOp(BinOp node){
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
      System.out.println(node.value);
      GOTOType gotoType = typecheckTypeToGOTO(node.typeAnnotation);
      return new Var(node.value, gotoType);
   }

   @Override
   public IRExpr visitReturnStmt(ReturnStmt node){
      paramFreeCheck();
      IRExpr expr = visit(node.expression);
      GOTOReturnStmt retStmt = new GOTOReturnStmt(expr);
      currentFunction.instr.add(retStmt);
      return defaultReturn;
   }

   @Override
   public IRExpr visitIfStmt(IfStmt node){
      paramFreeCheck();
      Scope originalscope = currentscope;
		currentscope = node.codeGenScope;

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
      node.codeGenScope = currentscope;
		currentscope = originalscope;
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
      paramFreeCheck();
      Scope originalscope = currentscope;
		currentscope = node.codeGenScope;

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

      node.codeGenScope = currentscope;
		currentscope = originalscope;
      return defaultReturn;
   }

   @Override
   public IRExpr visitExprStmt(ExprStmt node){
      paramFreeCheck();
      IRExpr expr = visit(node.expression);
      if(expr instanceof GOTOBinOp){
         GOTOBinOp binOpExpr = (GOTOBinOp)expr;
         if(binOpExpr.left instanceof Var){
            Var leftVar = (Var)binOpExpr.left;
            IRExpr rightExpr = (IRExpr)binOpExpr.right;
            currentFunction.instr.add(new Assign(leftVar, rightExpr));
         }
         else if(binOpExpr.left instanceof ArrayLoad){
            ArrayLoad leftArr = (ArrayLoad)binOpExpr.left;
            IRExpr rightExpr = (IRExpr)binOpExpr.right;
            currentFunction.instr.add(new ArrayStore(leftArr.array, leftArr.index, rightExpr));
         }
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
      if(tcType instanceof VOID){
         gotoType = null;
      }
      else{
         gotoType = typecheckTypeToGOTO(tcType);
      }

      ArrayExpr params = (ArrayExpr)visit(node.params);
      if(this.currentscope.hasFun(funcName.value)){
         FunSymbol fs = this.currentscope.getFun(funcName.value);
         for(int i = 0; i < fs.params.size(); i++){
            ParamSymbol ps = fs.params.get(i);
            currentFunction.instr.add(new StackPushOp(ps.name, ps.stackPtr, params.initElems.get(i)));
         }
         paramsToFree = fs;
      }

      return new Call(funcName.value, gotoType);
   }

   @Override
   public IRExpr visitStructDecl(StructDecl node){
      Scope originalscope = currentscope;
		currentscope = node.codeGenScope;

      DeclList absynFields = node.body;
      ArrayList<StructField> fields = new ArrayList<StructField>();
      StructMember sm;
      for(Decl decl : absynFields.list){
         sm = (StructMember)decl;
         fields.add(new StructField(sm.name, typecheckTypeToGOTO(sm.type.typeAnnotation)));
      }
      StructTypeDef sf = new StructTypeDef(node.name, fields);
      GOTOprog.structs.add(sf);

      node.codeGenScope = currentscope;
		currentscope = originalscope;
      return defaultReturn;
   }

   @Override
   public IRExpr visitUnionDecl(UnionDecl node){
      Scope originalscope = currentscope;
		currentscope = node.codeGenScope;

      DeclList absynFields = node.body;
      ArrayList<StructField> variants = new ArrayList<StructField>();
      UnionMember sm;
      for(Decl decl : absynFields.list){
         sm = (UnionMember)decl;
         variants.add(new StructField(sm.name, typecheckTypeToGOTO(sm.type.typeAnnotation)));
      }
      UnionTypeDef sf = new UnionTypeDef(node.name, variants);
      GOTOprog.unions.add(sf);

      node.codeGenScope = currentscope;
		currentscope = originalscope;
      return defaultReturn;
   }

   @Override
   public IRExpr visitType(Type node){
      return defaultReturn;
   }

   @Override
   public IRExpr visitExpList(ExpList node){
      if(node.list.size() == 0){
         return defaultReturn;
      }
      ArrayList<IRExpr> arrList = new ArrayList<IRExpr>();
      for(Exp exp : node.list){
         arrList.add(visit(exp));
      }
      ArrayExpr result = new ArrayExpr(arrList);
      if(arrList.size() == 0){
         result.size = 0;
      }
      else{
         if(arrList.get(0) instanceof ArrayExpr){
            ArrayExpr innerArr = (ArrayExpr)arrList.get(0);
            result.size = arrList.size() * innerArr.size;
         }
         else{
            result.size = arrList.size();
         }
      }
      return result;
   }

   @Override
   public IRExpr visitArrayExp(ArrayExp node){
      Var array = (Var)visit(node.name);
      ExpList exprList = (ExpList)node.index_list;
      IRExpr index;
      //doesn't handle multiple dimensions
      if(exprList.list.size() != 0){
         index = visit(exprList.list.get(0));
         ArrayLoad result = new ArrayLoad(array, index, GOTOType.INT);
         return result;
      }
      return defaultReturn;
   }
   
}
