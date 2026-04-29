package CodeGen;
import java.util.ArrayList;

public class FunSymbol extends Symbol {

   public String name;
   public ArrayList<ParamSymbol> params;

   public FunSymbol(String n1, ArrayList<ParamSymbol> n2) {
      this.name = n1;
      this.params = n2;
   }

}
