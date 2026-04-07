package Typecheck.Pass;
import Typecheck.Types.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.*;
import Typecheck.TypeCheckException;

public class TypeAnnotationPass extends Pass<Void> {

    // Hint: Build the base type from the name, then wrap it for pointers and any [] modifiers.
    // 1. Construct the base type ("string" -> STRING)
    // 2. Wrap the base type in a POINTER Type if stars count > 0
    // 3. If the Array has concrete values ([10][3][9]), Then construct a LIST
    //         a. Loop over the List of brackets ([x][i][j]...)
    //            For the first bracket ([x]) construct a List of "basetype"
    //                  LIST(basetype, basetype, basetype,... x times)
    //            For the next bracket ([i]) construct a List of the PREVIOUS List
    //                  LIST(
    //                     LIST(basetype, basetype, basetype,... x times),
    //                     LIST(basetype, basetype, basetype,... x times),
    //                     LIST(basetype, basetype, basetype,... x times),
    //                     LIST(basetype, basetype, basetype,... x times),
    //                     ... i times
    //                  )
    //            Keep repeating until no more brackets
    // 4. If the Array does not have expressions ([][][]...), then construct an ARRAY
    //         a. Pull the first bracket ([]) and construct an ARRAY(basetype)
    //            Pull the next bracket and construct an ARRAY(ARRAY(basetype))
    //            Keep repeating.

   public Type copy(Type input){
      //do we need to handle pointers?
      if(input instanceof POINTER){
         POINTER ptr = (POINTER)input;
         return new POINTER(copy(ptr.type));
      }
      else if(input instanceof STRING){
         return new STRING();
      }
      else if(input instanceof INT){
         return new INT();
      }
      else if(input instanceof VOID){
         return new VOID();
      }
      else if(input instanceof ALIAS){
         ALIAS alias = (ALIAS)input;
         return new ALIAS(alias.name);
      }
      else if(input instanceof POINTER){
         POINTER ptr = (POINTER)input;
         return new POINTER(copy(ptr.type));
      }
      else if(input instanceof LIST){
         LIST list = (LIST)input;
         ArrayList<Type> copy = new ArrayList<>();
         for(Type type : list.typelist){
            copy.add(copy(type));
         }
         return new LIST(copy);
      }
      else{
         throw new TypeCheckException("Attempted to copy unsupported type");
      }
   }

   @Override
   public Void visitType(Absyn.Type node) {
       // Here is how I checked if the type needed ARRAY or a LIST:
       // Feel free to use it or change it. 
      boolean isARRAY = node.brackets.list.stream()
         .allMatch(e -> ((Absyn.ArrayType)e).size instanceof Absyn.EmptyExp);
      boolean isLIST = node.brackets.list.stream()
         .allMatch(e -> ((Absyn.ArrayType)e).size instanceof Absyn.DecLit);
      if (!isARRAY && !isLIST && node.brackets.list.size() != 0) 
         throw new TypeCheckException("Array has invalid parameters in []");

      // 1. Construct the base type ("string" -> STRING)
      Type basetype = node.name.equals("int") ? new INT() :
                  node.name.equals("string") ? new STRING() :
                  node.name.equals("void") ? new VOID() :
                  new ALIAS(node.name);
      
      // 2. Wrap the base type in a POINTER type if stars count > 0
      int curPtr = node.pointerCount;
      while(curPtr > 0){
         basetype = new POINTER(basetype);
         curPtr--;
      }

      // 3. If the Array has concrete values ([10][3][9]), construct a LIST
      //         a. Loop over the List of brackets ([x][i][j]...)
      //            For the first bracket ([x]) construct a List of "basetype"
      //                  LIST(basetype, basetype, basetype,... x times)
      //            For the next bracket ([i]) construct a List of the PREVIOUS List
      //                  LIST(
      //                     LIST(basetype, basetype, basetype,... x times),
      //                     LIST(basetype, basetype, basetype,... x times),
      //                     LIST(basetype, basetype, basetype,... x times),
      //                     LIST(basetype, basetype, basetype,... x times),
      //                     ... i times
      //                  )
      //            Keep repeating until no more brackets
      if (isLIST) {
         List<Absyn.Decl> dims = node.brackets.list;
         for (int d = dims.size() - 1; d >= 0; d--) {
            Absyn.Decl decl = dims.get(d);
            if (decl instanceof Absyn.ArrayType arr && arr.size instanceof Absyn.DecLit lit) {
               ArrayList<Type> types = new ArrayList<>();
               for (int i = 0; i < lit.value; i++) {
                     types.add(copy(basetype));
               }
               basetype = new LIST(types);
            }
         }
      }

      // 4. If the Array does not have expressions ([][][]...), construct an ARRAY
      if(isARRAY){
         for(int i = 0; i < node.brackets.list.size(); i++){
            basetype = new ARRAY(basetype);
         }
      }

      node.typeAnnotation = basetype;

      return null;

   }
} 
