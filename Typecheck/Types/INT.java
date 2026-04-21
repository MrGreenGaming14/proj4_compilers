
package Typecheck.Types;
public class INT extends TypecheckType {
   public static final INT INSTANCE = new INT();
   
   public INT() {}

   public String toString() {
      return "INT()";
   }

   public boolean canAccept(TypecheckType t) {
      for (TypecheckType syn : t.getSynonyms()) {
         if (syn instanceof INT) {
            return true;
         }
      }
      return false;
   }

}
