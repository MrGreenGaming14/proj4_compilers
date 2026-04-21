package Typecheck.Pass;
import Typecheck.Types.*;
import Typecheck.SymbolTable.*;
import java.util.ArrayList;

public class TypeScopePass extends ScopePass<Void> {

   public TypeScopePass(Scope s) {
      super(s);
   }
// Hint: Structs define a new type from their member types.
// 1. Visit the body so member types are fully resolved.
// 2. Collect each member's typeAnnotation.
// 3. Build a LIST type from them.
// 4. Register the struct name in the current scope.
   @Override
	public Void visitStructDecl(Absyn.StructDecl node) {
      if(node.body != null){
         visitDeclList(node.body);
      }
      ArrayList<TypecheckType> arrlist = new ArrayList<>();
      Absyn.StructMember sm;
      for(int i = 0; i < node.body.list.size(); i++){
         sm = (Absyn.StructMember)node.body.list.get(i);
         arrlist.add(sm.type.typeAnnotation);
      }
      node.typeAnnotation = new LIST(arrlist);
      /*
      System.out.println("visitStructDecl:");
      System.out.println(node.typeAnnotation.toString());
      //*/
      currentscope.addType(node.name, new TypeSymbol(node.name, node.typeAnnotation));
      /*
      System.out.println("type taken from symbol table:");
      System.out.println("name: "+node.name);
      System.out.println(currentscope.getType(node.name).type.toString());
      System.out.println("--------------------------");
      //*/
		return null;
   }
// Hint: Unions define a type that can be any of their member types.
// 1. Visit the body so member types are resolved.
// 2. Collect each member's typeAnnotation.
// 3. Build an OR type from them.
// 4. Register the union name in the current scope.
   @Override
	public Void visitUnionDecl(Absyn.UnionDecl node) {
      if(node.body != null){
         visitDeclList(node.body);
      }
      ArrayList<TypecheckType> arrlist = new ArrayList<>();
      Absyn.UnionMember um;
      for(int i = 0; i < node.body.list.size(); i++){
         um = (Absyn.UnionMember)node.body.list.get(i);
         arrlist.add(um.type.typeAnnotation);
      }
      node.typeAnnotation = new OR(arrlist);
      /*
      System.out.println("visitUnionDecl:");
      System.out.println(node.typeAnnotation.toString());
      */
      currentscope.addType(node.name, new TypeSymbol(node.name, node.typeAnnotation));
      /* 
      System.out.println("type taken from symbol table:");
      System.out.println("name: "+node.name);
      System.out.println(currentscope.getType(node.name).type.toString());
      System.out.println("--------------------------");
      */
		return null;
   }
// Hint: Typedef introduces a new name for an existing type.
// Visit the type first, then register the alias in the current scope.
   @Override
	public Void visitTypedef(Absyn.Typedef node) {
      visitType(node.type);
      /*
      System.out.println("visitTypedef:\nresolved type:");
      System.out.println(node.type.typeAnnotation.toString());
      */
      currentscope.addType(node.name, new TypeSymbol(node.name, node.type.typeAnnotation));
      /*
      System.out.println("type taken from symbol table:");
      System.out.println("name: "+node.name);
      System.out.println(currentscope.getType(node.name).type.toString());
      System.out.println("--------------------------");
      */
		return null;
	}
// Hint: Replace ALIAS types with their real definition.
// Remember that Types can be nested (IE ARRAY(ARRAY(ARRAY(...))) )
// Traverse the whole type to search for Aliases. Once an alias is found,
// look up the type of the alias in the symbol table.
    // This is a function I found helpful to implement. If you have a solution
    // in mind that does not include a helper function, then feel free to ignore
   private TypecheckType resolveAlias(TypecheckType type) {
      if(type instanceof ARRAY){
         ARRAY arr = (ARRAY)type;
         arr.type = resolveAlias(arr.type);
         return arr.type;
      }
      else if(type instanceof LIST){
         LIST list = (LIST)type;
         for(int i = 0; i < list.typelist.size(); i++){
            list.typelist.set(i, resolveAlias(list.typelist.get(i)));
         }
         return list;
      }
      else if(type instanceof ALIAS){
         ALIAS alias = (ALIAS)type;
         return currentscope.getType(alias.name).type;
      }
      else{
         return type;
      }
   }
   


// Hint: Visit the brackets and resolve the alias to a type (if the typeAnnotation contains ALIAS)

   @Override
   public Void visitType(Absyn.Type node) {
      visitDeclList(node.brackets);
      if(node.typeAnnotation instanceof ALIAS){
         node.typeAnnotation = resolveAlias(node.typeAnnotation);
      }
		return null;
   }
}
