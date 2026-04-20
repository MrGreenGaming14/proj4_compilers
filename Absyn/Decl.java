package Absyn;
public class Decl extends Absyn {
   public String print(int depth) {return "";}

	public <T> T accept(Typecheck.Pass.Visitor<T> v) {
		return v.visitDecl(this);
	}
	public <T> T accept(CodeGen.Visitor<T> v) {
		return v.visitDecl(this);
	}
}
