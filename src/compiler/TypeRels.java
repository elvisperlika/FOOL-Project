package compiler;

import compiler.AST.BoolTypeNode;
import compiler.AST.IntTypeNode;
import compiler.AST.RefTypeNode;
import compiler.AST.EmptyTypeNode;
import compiler.AST.ArrowTypeNode;
import compiler.lib.TypeNode;

import java.util.HashMap;
import java.util.Map;

public class TypeRels {

  /**
   * Maps a class name to its superclass name
   */
  public static Map<String, String> superType = new HashMap<>();

  // valuta se il tipo "a" e' <= al tipo "b"
  public static boolean isSubtype(TypeNode a, TypeNode b) {
    // 1. Exact match
    if (a.getClass().equals(b.getClass())) {
      // If both are RefTypeNode, also check the class names
      if (a instanceof RefTypeNode) {
        String aName = ((RefTypeNode) a).className;
        String bName = ((RefTypeNode) b).className;

        // Climb the inheritance chain of a to see if it matches b
        while (aName != null) {
          if (aName.equals(bName)) return true;
          aName = superType.get(aName);
        }
        return false; // No match found in the inheritance chain
      }
      return true; // Both are the same primitive
    }

    if (a instanceof ArrowTypeNode aArrow && b instanceof ArrowTypeNode bArrow) {
      // 1. Covariant return type. a.ret <= b.ret
      if (!isSubtype(aArrow.ret, bArrow.ret)) return false;

      // 2. Controvariant parameter types. For each parameter, b.param[i] <= a.param[i]
      if (aArrow.parlist.size() != bArrow.parlist.size()) return false; // Different number of parameters
      for (int i = 0; i < aArrow.parlist.size(); i++){
        // a and b are swapped here for contravariance
        if (!isSubtype(bArrow.parlist.get(i), aArrow.parlist.get(i))) return false;
      }

      return true; // Placeholder for now
    }

    // 2. Bool is subtype of Int
    if (a instanceof BoolTypeNode && b instanceof IntTypeNode) return true;

    // 3. Null (EmptyTypeNode) is subtype of any RefTypeNode
    if (a instanceof EmptyTypeNode && b instanceof RefTypeNode) return true;

    return false; // No match
  }

  public static TypeNode lowestCommonAncestor(TypeNode a, TypeNode b) {
    if(a instanceof EmptyTypeNode)
      return b; // Null is subtype of any RefTypeNode, so the LCA is b
    else if(b instanceof EmptyTypeNode)
      return a; // Null is subtype of any RefTypeNode, so the LCA is a

    if (a instanceof RefTypeNode aRef && b instanceof RefTypeNode bRef) {
      // Climb the inheritance chain of a
      String aName = aRef.className;

      while (aName != null) {
        RefTypeNode currentClass = new RefTypeNode(aName);

        // Check if 'b' is a subtype of the current class
        if (isSubtype(b, currentClass)) {
          return currentClass; // Found the lowest common ancestor
        }
        aName = superType.get(aName);
      }

      return null; // No common ancestor found
    }

    if (isSubtype(a, new IntTypeNode()) && isSubtype(b, new IntTypeNode())) {
      if (a instanceof IntTypeNode || b instanceof IntTypeNode) {
        return new IntTypeNode(); // LCA of Int and Bool is Int
      }
      return new BoolTypeNode(); // LCA of Bool and Bool is Bool
    }

    // Typechecking failed
    return null;
  }
}
