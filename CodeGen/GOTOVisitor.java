package CodeGen;

public interface GOTOVisitor<T> {
    T visitVar(Var instr);
    T visitLiteral(GOTOLiteral instr);
    T visitGOTOBinOp(GOTOBinOp instr);
    T visitUnaryOp(UnaryOp instr);
    T visitCall(Call instr);
    T visitArrayLoad(ArrayLoad instr);
    T visitAssign(Assign instr);
    T visitArrayStore(ArrayStore instr);
    T visitArrayAlloc(ArrayAlloc instr);
    T visitGOTOIfStmt(GOTOIfStmt instr);
    T visitGoto(Goto instr);
    T visitLabel(Label instr);
    T visitGOTOReturnStmt(GOTOReturnStmt instr);
    T visitPrintf(Printf instr);
    T visitGOTO(GOTO instr);


	default T GOTOvisit(GOTO node) {
		return node.accept(this);
	}

}
