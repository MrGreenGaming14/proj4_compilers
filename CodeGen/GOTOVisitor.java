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
    T visitGOTO(GOTONode instr);

    // Arrays
    default T visitArrayAllocInit(ArrayAllocInit instr) { return visitGOTO(instr); }

    // Structs
    default T visitStructTypeDef(StructTypeDef instr)   { return visitGOTO(instr); }
    default T visitStructInit(StructInit instr)         { return visitGOTO(instr); }
    default T visitFieldLoad(FieldLoad instr)           { return visitGOTO(instr); }
    default T visitFieldStore(FieldStore instr)         { return visitGOTO(instr); }

    // Unions
    default T visitUnionTypeDef(UnionTypeDef instr)     { return visitGOTO(instr); }
    default T visitUnionInit(UnionInit instr)           { return visitGOTO(instr); }

    default T GOTOvisit(GOTONode node) {
        return node.accept(this);
    }
}