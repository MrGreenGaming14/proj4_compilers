
package CodeGen;

public class ParamSymbol extends Symbol {

   public String name;
   public String stackPtr;

   public ParamSymbol(String n1, String n2) {
      this.name = n1;
      this.stackPtr = n2;
   }


}
