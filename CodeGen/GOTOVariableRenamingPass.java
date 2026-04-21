package CodeGen;
import Absyn.*;


public class GOTOVariableRenamingPass extends ScopePass<Void> {

   protected Void defaultReturn = null;

   public Scope globalscope;
   public Program GOTOprog;

   public GOTOVariableRenamingPass(Scope s) {
      super(s);
   }

   public GOTOVariableRenamingPass(Scope s, Program p){
      super(s);
      this.GOTOprog = p;
   }

   @Override
   public Void visitVarDecl(VarDecl node){
      if(this.currentscope.hasLocalVar(node.name)){
         System.out.println("Var "+node.name+" found to have been renamed to "+this.currentscope.getVar(node.name).new_name);
         node.name = this.currentscope.getVar(node.name).new_name;
      }
      else{
         String new_name = GOTOprog.getUniqueVarName();
         System.out.println("Renaming var "+node.name+" to "+new_name);
         VarSymbol vs = new VarSymbol(node.name, new_name);
         this.currentscope.addVar(node.name, vs);
         node.name = new_name;
      }
      return defaultReturn;
   }

}
