package CodeGen;

import Parse.*;
import Parse.antlr_build.Parse.*;
import Typecheck.*;
import Typecheck.Pass.CreateScopePass;
import Typecheck.Pass.FunAndVarScopePass;
import Typecheck.Pass.JudgementsPass;
import Typecheck.Pass.TypeAnnotationPass;
import Typecheck.Pass.TypeScopePass;
import java.util.ArrayList;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

public class Main {
    public static void main(String[] args) throws Exception {
        CharStream input = CharStreams.fromFileName(args[0]);

        gLexer lexer = new gLexer(input);
        //gLexer lexer = new gLexer(input);

        CommonTokenStream tokens = new CommonTokenStream(lexer);      

        System.out.println("Starting Parser");
        gParser parser = new gParser(tokens);

        ParseTree tree = parser.program();

        ASTBuilder astBuilder = new ASTBuilder();
        Absyn.Absyn asttree = astBuilder.visit(tree);

        try {
            // Passes
            TypeAnnotationPass tap = new TypeAnnotationPass();
            asttree.accept(tap);
            CreateScopePass scp = new CreateScopePass();
            asttree.accept(scp);
            TypeScopePass tcp = new TypeScopePass(scp.globalscope);
            asttree.accept(tcp);
            FunAndVarScopePass fvcp = new FunAndVarScopePass(scp.globalscope);
            asttree.accept(fvcp);
            JudgementsPass jp = new JudgementsPass(scp.globalscope);
            asttree.accept(jp);
            System.out.println("Type Check Passed!");

            Program GOTOprog = new Program();
            GOTOCreateScopePass csp = new GOTOCreateScopePass();
            asttree.accept(csp);
            GOTOVariableRenamingPass vrp = new GOTOVariableRenamingPass(csp.globalscope, GOTOprog);
            asttree.accept(vrp);
            GOTOConstructionPass gcp = new GOTOConstructionPass(GOTOprog);
            asttree.accept(gcp);
            GOTOprog.funcs.add(gcp.mainFunction);
            for(Var global : GOTOprog.globals){
                System.out.println("global: name: "+global.name+" | type: "+global.type.toString());
            }
            for(Function func : GOTOprog.funcs){
                System.out.println("func: name: "+func.name+" | return type: "+func.returntype+" | instr count: "+func.instr.size());
                int i = 1;
                for(GOTO instr : func.instr){
                    System.out.print("instr "+i+": ");
                    if(instr instanceof Assign){
                        Assign assign = (Assign)instr;
                        System.out.print("Assign for var: "+assign.target.name);
                    }
                    else if(instr instanceof GOTOReturnStmt){
                        GOTOReturnStmt retStmt = (GOTOReturnStmt)instr;
                        System.out.print("Return");
                    }
                    else if(instr instanceof Call){
                        Call call = (Call)instr;
                        System.out.print("Call to function: "+call.func);
                    }
                    else if(instr instanceof GOTOIfStmt){
                        GOTOIfStmt ifStmt = (GOTOIfStmt)instr;
                        System.out.print("If Stmt: labelTrue: "+ifStmt.trueLabel+" | labelFalse: "+ifStmt.falseLabel);
                    }
                    else if(instr instanceof Goto){
                        Goto gotoStmt = (Goto)instr;
                        System.out.print("Goto label: "+gotoStmt.label);
                    }
                    else if(instr instanceof Label){
                        Label label = (Label)instr;
                        System.out.print("Label - "+label.name+":");
                    }
                    else{
                        System.out.print("unknown type");
                    }
                    System.out.println();
                    i++;
                }
            }
        } catch (TypeCheckException e) {
            System.err.println("TypeCheckError: " + e.getMessage());
        }
    }
}
