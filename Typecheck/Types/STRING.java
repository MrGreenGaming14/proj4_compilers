
package Typecheck.Types;
public class STRING extends TypecheckType {
   public static final STRING INSTANCE = new STRING();

   public STRING() {}
   
   public boolean canAccept(TypecheckType t) { return t instanceof STRING; }

   public String toString() {
      return ("STRING()");
   }

}
