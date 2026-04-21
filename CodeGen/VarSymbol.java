
package CodeGen;

public class VarSymbol extends Symbol {

   public String name;
   public String new_name;

   public VarSymbol(String n1, String n2) {
      this.name = n1;
      this.new_name = n2;
   }


}
