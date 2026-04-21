
package Typecheck.SymbolTable;
import Typecheck.Types.*;

public class TypeSymbol extends Symbol {
   
   public String name;
   public TypecheckType type;

   public TypeSymbol(String n, TypecheckType t) {
      this.name = n;
      this.type = t;
   }


}
