
package Typecheck.SymbolTable;
import Typecheck.Types.*;

public class VarSymbol extends Symbol {

   public String name;
   public TypecheckType type;

   public VarSymbol(String n, TypecheckType t) {
      this.name = n;
      this.type = t;
   }


}
