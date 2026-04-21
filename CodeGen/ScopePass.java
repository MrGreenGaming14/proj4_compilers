package CodeGen;
import Absyn.*;

public class ScopePass<T> extends Pass<T> {

	protected Scope currentscope;
	public Program GOTOprog;
	protected T defaultReturn = null;

    // Hint: Save scope → switch to node.scope → visit children → restore scope.

	public ScopePass(Scope s) {
		this.currentscope = s;
	}

	@Override
	public T visitFunDecl(FunDecl node) {
		Scope originalscope = currentscope;
		currentscope = node.codeGenScope;
        visit(node.type);
		visit(node.params);
		visit(node.body);
		node.codeGenScope = currentscope;
		currentscope = originalscope;
		return defaultReturn;
	}

   	@Override
	public T visitStructDecl(StructDecl node) {
		Scope originalscope = currentscope;
		currentscope = node.codeGenScope;
		visit(node.body);
		node.codeGenScope = currentscope;
		currentscope = originalscope;
		return defaultReturn;
	}

	@Override
	public T visitUnionDecl(UnionDecl node) {
		Scope originalscope = currentscope;
		currentscope = node.codeGenScope;
		visit(node.body);
		node.codeGenScope = currentscope;
		currentscope = originalscope;
		return defaultReturn;
	}

	@Override
	public T visitIfStmt(IfStmt node) {
		Scope originalscope = currentscope;
		currentscope = node.codeGenScope;
        visit(node.expression);
		visit(node.if_statement);
		visit(node.else_statement);
		node.codeGenScope = currentscope;
		currentscope = originalscope;
		return defaultReturn;
	}

   	@Override
	public T visitWhileStmt(WhileStmt node) {
		Scope originalscope = currentscope;
		currentscope = node.codeGenScope;
		visit(node.expression);
		visit(node.statement);
		node.codeGenScope = currentscope;
		currentscope = originalscope;
		return defaultReturn;
	}

}
