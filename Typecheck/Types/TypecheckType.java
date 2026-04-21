
package Typecheck.Types;
import java.util.Set;
import java.util.HashSet;

public abstract class TypecheckType {
   public abstract boolean canAccept(TypecheckType t);
   public Set<TypecheckType> getSynonyms() {return Set.of(this);}
}

