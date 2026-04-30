package CodeGen;
import java.util.ArrayList;
import java.util.List;

public class Emitter {

    public static class ProgramEmitter {

        private ArrayList<Var> globals;
        private ArrayList<Var> stackPtrs;
        private ArrayList<IRStmt> varDecls;
        private boolean writeToFile;
        private boolean readFromFile;
        private ArrayList<Function> funcs;
        private ArrayList<StructTypeDef> structs;
        private ArrayList<UnionTypeDef> unions;

        public ProgramEmitter(Program program) {
            this.globals = program.globals;
            this.stackPtrs = program.stackPtrs;
            this.varDecls = program.varDecls;
            this.writeToFile = program.writeToFile;
            this.readFromFile = program.readFromFile;
            this.funcs = program.funcs;
            this.structs = program.structs;
            this.unions = program.unions;
        }

        private InstructionEmitter instrEmitter = new InstructionEmitter();

        public String emitProgram() {
            StringBuilder sb = new StringBuilder();

            sb.append("#include <stdlib.h>\n");
            sb.append("#include <stdio.h>\n");

            // 1. Emit global variable declarations
            for (Var v : globals) {
                sb.append(v.type.toString()).append(" ").append(v.name).append(";\n");
            }

            //2. Initialize parameter stack pointers
            for(Var v : stackPtrs){
                sb.append(v.name).append(" = 0;\n");
            }

            //3. Initialize variable declarations outside of functions
            for(IRStmt stmt : varDecls){
                sb.append(stmt.accept(instrEmitter)).append("\n");
            }

            if(writeToFile){
                sb.append("void writeToFile(const char* path, const char* content) {\n");
                sb.append("\tFILE* f = fopen(path, \"w\");\n\tif (!f) return;\n\t");
                sb.append("fputs(content, f);\n\tfclose(f);\n}\n");
            }

            if(readFromFile){
                sb.append("char* readFromFile(const char* path) {\n\t");
                sb.append("FILE* f = fopen(path, \"r\");\n\t");
                sb.append("if (!f) return NULL;\n\tfseek(f, 0, SEEK_END);\n\t");
                sb.append("long size = ftell(f);\n\trewind(f);\n\t");
                sb.append("char* buffer = malloc(size + 1);\n\t");
                sb.append("fread(buffer, 1, size, f);\n\tbuffer[size] = '\\0';\n\t");
                sb.append("fclose(f);\n\treturn buffer;\n}\n");
            }

            //3. Emit structs
            for(StructTypeDef struct : structs){
                sb.append("struct ").append(struct.name).append("{\n");
                for(StructField field : struct.fields){
                    sb.append(field.gotoType.toString()).append(" ").append(field.name).append(";\n");
                }
                sb.append("}\n");
            }

            //4. Emit unions
            for(UnionTypeDef union : unions){
                sb.append("union ").append(union.name).append("{\n");
                for(StructField field : union.variants){
                    sb.append(field.gotoType.toString()).append(" ").append(field.name).append(";\n");
                }
                sb.append("}\n");
            }

            // 5. Emit functions
            for (Function f : funcs) {
                sb.append(f.returntype + " ").append(f.name).append("() {\n");
                for (GOTONode instr : f.instr) {
                    sb.append(instr.accept(instrEmitter)).append("\n");
                }
                sb.append("}\n\n");
            }
            return sb.toString();
        }
    }

    public static class InstructionEmitter implements GOTOVisitor<String> {

        @Override
        public String visitGOTO(GOTONode instr) {
            throw new RuntimeException("Something bad happened\nEmail: blara4@lsu.edu");
        }

        @Override
        public String visitVar(Var instr) {
            return instr.name;
        }

        @Override
        public String visitLiteral(GOTOLiteral instr) {
            switch (instr.type) {
                case INT -> { return instr.value.toString(); }
                case STRING -> { return "\"" + instr.value.toString() + "\""; }
                default -> throw new RuntimeException("Unsupported literal type: " + instr.type);
            }
        }

        @Override
        public String visitGOTOBinOp(GOTOBinOp instr) {
            return String.format("(%s %s %s)",
                                 GOTOvisit(instr.left),
                                 instr.op,
                                 GOTOvisit(instr.right)
                                 );
        }

        @Override
        public String visitUnaryOp(UnaryOp instr) {
            return String.format("(%s(%s))",
                                 instr.op,
                                 GOTOvisit(instr.expr));
        }

        @Override
        public String visitCall(Call instr) {
            String ret = instr.func + "()";
            return ret;
        }

        @Override
        public String visitArrayLoad(ArrayLoad instr) {
            return String.format("(*%s+%s)",
                                 GOTOvisit(instr.array),
                                 GOTOvisit(instr.index));
        }
        
        @Override
        public String visitAssign(Assign instr) {
                    return String.format("%s = %s;",
                                         GOTOvisit(instr.target),
                                         GOTOvisit(instr.value)
                                         );
        }

        @Override
        public String visitArrayStore(ArrayStore instr) {
            String ret = "";
            String idx = GOTOvisit(instr.index);
            String val = GOTOvisit(instr.value);
            ret += "(*" + GOTOvisit(instr.array) + " + " + idx + ") = " + val + ";";
            return ret;
        }

        @Override
        public String visitArrayAlloc(ArrayAlloc instr) {
            String arrayName = GOTOvisit(instr.array);
            String size = GOTOvisit(instr.size);
            String type = instr.array.type.toString();
                return String.format("%s = realloc(%s, sizeof(%s) * %s);",
                                     arrayName,
                                     arrayName,
                                     type,
                                     size);
        }

        @Override
        public String visitGOTOIfStmt(GOTOIfStmt instr) {
            return String.format("if (%s) goto %s;\ngoto %s;",
                                 GOTOvisit(instr.cond),
                                 instr.trueLabel,
                                 instr.falseLabel);
        }

        @Override
        public String visitGoto(Goto instr) {
            return String.format("goto %s;",
                                 instr.label);
        }

        @Override
        public String visitLabel(Label instr) {
            return String.format("%s:",
                                 instr.name);
        }

        @Override
        public String visitGOTOReturnStmt(GOTOReturnStmt instr) {
            return String.format("return %s;",
                                 GOTOvisit(instr.value));
        }

        @Override
        public String visitPrintf(Printf instr) {
            StringBuilder sb = new StringBuilder();
            sb.append("printf(\"").append(instr.format).append("\"");
            for (IRExpr arg : instr.args) {
                sb.append(", ").append(GOTOvisit(arg));
            }
            sb.append(");");
            return sb.toString();
        }

        @Override
        public String visitInput(Input instr){
            StringBuilder sb = new StringBuilder();
            sb.append("scan(\"%d\", &").append( GOTOvisit(instr.arg)).append(");");;
            return sb.toString();
        }

        @Override
        public String visitReadFromFile(ReadFromFile instr){
            StringBuilder sb = new StringBuilder();
            sb.append("readFromFile(").append(GOTOvisit(instr.path)).append(")");
            return sb.toString();
        }

        @Override
        public String visitWriteToFile(WriteToFile instr){
            StringBuilder sb = new StringBuilder();
            sb.append("writeToFile(").append(GOTOvisit(instr.path)).append(", ");
            sb.append(GOTOvisit(instr.content)).append(");");
            return sb.toString();
        }

        @Override
        public String visitStackPushOp(StackPushOp instr){
            StringBuilder sb = new StringBuilder();
            sb.append(instr.stack).append(" = realloc(&").append(instr.stack);
            sb.append(", (").append(instr.stackPtr).append("+1) * sizeof(int);\n");
            sb.append(instr.stack).append("[").append(instr.stackPtr).append("]");
            sb.append(" = ").append(GOTOvisit(instr.value)).append(";\n");
            sb.append(instr.stackPtr).append("++;");
            return sb.toString();
        }

        @Override
        public String visitStackPopOp(StackPopOp instr){
            StringBuilder sb = new StringBuilder();
            sb.append(instr.stackPtr).append("--;");
            return sb.toString();
        }

    }


}
