package Absyn;

public class Absyn {
  public int pos;
  public Typecheck.SymbolTable.Scope scope;
  public CodeGen.Scope codeGenScope;
  public Typecheck.Types.TypecheckType typeAnnotation;

	public <T> T accept(Typecheck.Pass.Visitor<T> v) {
		return v.visitAbsyn(this);
	}

  	public <T> T accept(CodeGen.Visitor<T> v) {
		return v.visitAbsyn(this);
	}
}
