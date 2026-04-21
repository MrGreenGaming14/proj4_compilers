package Typecheck.SymbolTable;
import Typecheck.Types.*;

public class FunSymbol extends Symbol {

   public String name;
   public LIST params;
   public TypecheckType returnType;

   public FunSymbol(String n, LIST l, TypecheckType r) {
      this.name = n;
      this.params = l;
      this.returnType = r;
   }
}
