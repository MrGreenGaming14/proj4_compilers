
package Typecheck.Types;
public class VOID extends TypecheckType {
   public static final VOID INSTANCE = new VOID();

   public VOID() {}

   public boolean canAccept(TypecheckType t) { return false; }

   public String toString() {
      return ("VOID()");
   }

}

