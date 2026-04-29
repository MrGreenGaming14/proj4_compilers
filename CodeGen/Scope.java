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
        FunSymbol fun;
    }

    private SymbolBucket locallookup(String n) {
      return this.bindings.get(n);
    }

    private enum SYMBOL{
      VAR,
      FUN
    }

   private SymbolBucket lookup(String n, SYMBOL s) {
      Scope current = this;
      while (current != null) {
         if (current.locallookup(n) != null) {
            switch(s){
               case VAR:
                  if (current.locallookup(n).var != null) return current.locallookup(n);
                  break;
               case FUN:
                  if (current.locallookup(n).fun != null) return current.locallookup(n);
                  break;
            }
         } 
         current = current.parent;
      }
      return null;
   }
   


    public boolean hasLocalVar(String n) {
      return (locallookup(n) != null && locallookup(n).var != null);
    }

    public boolean hasLocalFun(String n){
      return (locallookup(n) != null && locallookup(n).fun != null);
    }



    public boolean hasVar(String n) {
      return (lookup(n, SYMBOL.VAR) != null );
    }

    public boolean hasFun(String n){
      return (lookup(n, SYMBOL.FUN) != null);
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

   public void addFun(String n, FunSymbol sym){
      SymbolBucket symbuc = getBucket(n);
      if (symbuc.fun != null) {
         throw new TypeCheckException("Symbol "+n+" defined twice in the same scope");
      }
      symbuc.fun = sym;
      this.bindings.put(n, symbuc);
   }

   public VarSymbol getVar(String n) {
      if (lookup(n, SYMBOL.VAR) != null) {
         return lookup(n, SYMBOL.VAR).var;
      } else {
         throw new TypeCheckException("Looked up var "+n+" but was not found.");
      }
   }

   public FunSymbol getFun(String n){
      if(lookup(n, SYMBOL.FUN) != null){
         return lookup(n, SYMBOL.FUN).fun;
      } else {
         throw new TypeCheckException("Looked up fun "+n+" but was not found.");
      }
   }

    public Scope getParent() {
        return this.parent;
    }
}

