package Typecheck.Pass;
import Absyn.*;
import Typecheck.SymbolTable.*;

public class ScopePass<T> extends Pass<T> {

	protected Scope currentscope;
	protected T defaultReturn = null;

    // Hint: Save scope → switch to node.scope → visit children → restore scope.

	public ScopePass(Scope s) {
		this.currentscope = s;
	}

	@Override
	public T visitFunDecl(FunDecl node) {
		Scope originalscope = currentscope;
		currentscope = node.scope;
        visit(node.type);
		visit(node.params);
		visit(node.body);
		node.scope = currentscope;
		currentscope = originalscope;
		return defaultReturn;
	}

   	@Override
	public T visitStructDecl(StructDecl node) {
		Scope originalscope = currentscope;
		currentscope = node.scope;
		visit(node.body);
		node.scope = currentscope;
		currentscope = originalscope;
		return defaultReturn;
	}

	@Override
	public T visitUnionDecl(UnionDecl node) {
		Scope originalscope = currentscope;
		currentscope = node.scope;
		visit(node.body);
		node.scope = currentscope;
		currentscope = originalscope;
		return defaultReturn;
	}

	@Override
	public T visitIfStmt(IfStmt node) {
		Scope originalscope = currentscope;
		currentscope = node.scope;
        visit(node.expression);
		visit(node.if_statement);
		visit(node.else_statement);
		node.scope = currentscope;
		currentscope = originalscope;
		return defaultReturn;
	}

   	@Override
	public T visitWhileStmt(WhileStmt node) {
		Scope originalscope = currentscope;
		currentscope = node.scope;
		visit(node.expression);
		visit(node.statement);
		node.scope = currentscope;
		currentscope = originalscope;
		return defaultReturn;
	}

}
