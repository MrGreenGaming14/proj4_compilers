
package Typecheck.Types;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

// This is only for unions

public class OR extends TypecheckType {
   public ArrayList<TypecheckType> options;

   public OR(ArrayList<TypecheckType> tl) {
      this.options = tl;
   }

   @Override
   public boolean canAccept(TypecheckType tt) { 
      for (TypecheckType t: tt.getSynonyms()) {
         for (TypecheckType option : this.options) {
            if (option.canAccept(t)) {
               return true;
            }
         }
      }
      return false;
   }

   public Set<TypecheckType> getSynonyms() {
      Set<TypecheckType> set = new HashSet<>();
      for (TypecheckType o : this.options) {
         set.add(o);
      }
      return set;
   }


   public String toString() {
      String ret = "";
      ret += ("OR(\n");
      for (TypecheckType t: this.options) {
         ret += ("\t" + t.toString() + "\n");
      }
      ret += (")");
      return ret;
   }

}
