package CodeGen;
import Absyn.*;

public class GOTOCreateScopePass extends Pass<Void> {

   protected Scope currentscope;
   public Scope globalscope;

   public GOTOCreateScopePass() {
      this.globalscope = new Scope();
      this.currentscope = globalscope;
   }

// Hint: Functions introduce a new nested scope.
// 1. Create a new Scope whose parent is the current scope.
// 2. Temporarily switch currentscope to this new scope.
// 3. Visit the function type, parameters, and body.
// 4. Store the resulting scope in node.scope.
// 5. Restore the previous scope.
   @Override
   public Void visitFunDecl(FunDecl node) {
      Scope originalscope = currentscope;
      currentscope = new Scope(currentscope);
      visit(node.type);
      visit(node.params);
      visit(node.body);
      node.codeGenScope = currentscope;
      currentscope = originalscope;
      return null;
   }
// Hint: Struct bodies are evaluated inside their own scope.
// 1. Create a new Scope whose parent is the current scope.
// 2. Switch currentscope to this new scope.
// 3. Visit the struct body.
// 4. Store this scope in node.scope.
// 5. Restore the previous scope.
   @Override
	public Void visitStructDecl(StructDecl node) {
      Scope originalscope = currentscope;
      currentscope = new Scope(currentscope);
      visit(node.body);
      node.codeGenScope = currentscope;
      currentscope = originalscope;
      return null;
	}
// Hint: Union bodies behave like structs for scoping.
// 1. Create a new Scope whose parent is the current scope.
// 2. Switch currentscope to the new scope.
// 3. Visit the union body.
// 4. Store this scope in node.scope.
// 5. Restore the previous scope.
	@Override
	public Void visitUnionDecl(UnionDecl node) {
      Scope originalscope = currentscope;
      currentscope = new Scope(currentscope);
      visit(node.body);
      node.codeGenScope = currentscope;
      currentscope = originalscope;
      return null;
	}
// Hint: If statements execute inside a fresh scope.
// 1. Create a new Scope whose parent is the current scope.
// 2. Switch currentscope to this new scope.
// 3. Visit the condition and both branches.
// 4. Save this scope in node.scope.
// 5. Restore the previous scope.
	@Override
	public Void visitIfStmt(IfStmt node) {
      Scope originalscope = currentscope;
      currentscope = new Scope(currentscope);
      visit(node.expression);
      visit(node.if_statement);
      visit(node.else_statement);
      node.codeGenScope = currentscope;
      currentscope = originalscope;
      return null;
	}
// Hint: Loops also introduce a nested scope.
// 1. Create a new Scope whose parent is the current scope.
// 2. Switch currentscope to the new scope.
// 3. Visit the condition and loop body.
// 4. Store the scope in node.scope.
// 5. Restore the previous scope.
   @Override
	public Void visitWhileStmt(WhileStmt node) {
      Scope originalscope = currentscope;
      currentscope = new Scope(currentscope);
      visit(node.expression);
      visit(node.statement);
      node.codeGenScope = currentscope;
      currentscope = originalscope;
      return null;
	}
}
