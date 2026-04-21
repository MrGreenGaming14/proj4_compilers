package CodeGen;
import Typecheck.TypeCheckException;

import java.util.HashMap;

public class Scope {

    private Scope parent;
    private HashMap<String, SymbolBucket> bindings;

    public Scope(Scope p) {
        this.parent = p;
        this.bindings = new HashMap<>();
    }

    public Scope() {
        this.parent = null;
        this.bindings = new HashMap<>();
    }

    private class SymbolBucket {
        VarSymbol var;
    }

    private SymbolBucket locallookup(String n) {
      return this.bindings.get(n);
    }

   private SymbolBucket lookup(String n) {
      Scope current = this;
      while (current != null) {
         if (current.locallookup(n) != null) {
            if (current.locallookup(n).var != null) return current.locallookup(n);
            break;
         } 
         current = current.parent;
      }
      return null;
   }
   


    public boolean hasLocalVar(String n) {
      return (locallookup(n) != null && locallookup(n).var != null);
    }



    public boolean hasVar(String n) {
      return (lookup(n) != null );
    }

    private SymbolBucket getBucket(String n) {
      SymbolBucket symbuc = locallookup(n);
      if (symbuc == null) {
         // Make a new bucket
         symbuc = new SymbolBucket();
      }
      return symbuc;
    }

   public void addVar(String n, VarSymbol sym) {
      SymbolBucket symbuc = getBucket(n);
      if (symbuc.var != null) {
         throw new TypeCheckException("Symbol "+n+" defined twice in the same scope");
      }
      symbuc.var = sym;
      this.bindings.put(n,symbuc);
   }

   public VarSymbol getVar(String n) {
      if (lookup(n) != null) {
         return lookup(n).var;
      } else {
         throw new TypeCheckException("Looked up var "+n+" but was not found.");
      }
   }

    public Scope getParent() {
        return this.parent;
    }
}

