package CodeGen;
import Typecheck.SymbolTable.*;
import Typecheck.TypeCheckException;
import Absyn.*;

public class GOTOConstructionPass extends Pass<Void> {

   public Program GOTOprog;
   public Scope globalscope;

   public GOTOConstructionPass(Program GOTOprog) {
      this.GOTOprog = GOTOprog;
      this.globalscope = new Scope();
   }

}
