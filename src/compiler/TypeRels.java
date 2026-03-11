package compiler;

import compiler.AST.BoolTypeNode;
import compiler.AST.IntTypeNode;
import compiler.AST.RefTypeNode;
import compiler.AST.EmptyTypeNode;
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

    // 2. Bool is subtype of Int
    if (a instanceof BoolTypeNode && b instanceof IntTypeNode) return true;

    // 3. Null (EmptyTypeNode) is subtype of any RefTypeNode
    if (a instanceof EmptyTypeNode && b instanceof RefTypeNode) return true;

    return false; // No match
  }
}
