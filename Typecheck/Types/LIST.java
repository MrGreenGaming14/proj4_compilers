
package Typecheck.Types;
import java.util.ArrayList;
public class LIST extends TypecheckType {
   public ArrayList<TypecheckType> typelist;

   public LIST(ArrayList<TypecheckType> tl) {
      this.typelist = tl;
   }

   public boolean canAccept(TypecheckType tt) {
      for (TypecheckType t: tt.getSynonyms()) {
         if (t instanceof ARRAY) {
            ARRAY arr = (ARRAY) t;
            return this.typelist.stream()
               .allMatch(e -> e.canAccept(arr.type));
         } else if (t instanceof LIST) {
            LIST list = (LIST) t;

            if ((list.typelist).size() != this.typelist.size()) {
               continue;
            }
            boolean ret = true;
            for (int i = 0; i < typelist.size(); i++) {
               ret &= (this.typelist.get(i).canAccept(list.typelist.get(i)));
            }
            if (ret == true) {return true;}
         }
      }
      return false;
   }

   public String toString() {
      String ret = "";
      ret += ("LIST(\n");
      for (TypecheckType t: this.typelist) {
         ret += ("\t" + t.toString() + "\n");
      }
      ret += (")");
      return ret;
   }
}
