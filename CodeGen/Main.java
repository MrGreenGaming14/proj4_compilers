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
        } catch (TypeCheckException e) {
            System.err.println("TypeCheckError: " + e.getMessage());
        }

        Program GOTOprog = new Program();
        GOTOCreateScopePass csp = new GOTOCreateScopePass();
        asttree.accept(csp);
        GOTOVariableRenamingPass vrp = new GOTOVariableRenamingPass(csp.globalscope, GOTOprog);
        asttree.accept(vrp);
        GOTOConstructionPass gcp = new GOTOConstructionPass(GOTOprog);
        asttree.accept(gcp);
        for(Var global : GOTOprog.globals){
            System.out.println("global: name: "+global.name+" type: "+global.type.toString());
        }
    }
}
