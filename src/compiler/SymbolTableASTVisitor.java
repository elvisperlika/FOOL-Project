package compiler;

import compiler.AST.*;
import compiler.exc.VoidException;
import compiler.lib.BaseASTVisitor;
import compiler.lib.Node;
import compiler.lib.TypeNode;

import java.util.*;

public class SymbolTableASTVisitor extends BaseASTVisitor<Void, VoidException> {

  // counter for offset of local declarations at current nesting level
  int stErrors = 0;
  private List<Map<String, STentry>> symTable = new ArrayList<>();
  private Map<String, Map<String, STentry>> classTable = new HashMap<>();
  private int nestingLevel = 0; // current nesting level
  private int decOffset = -2;

  SymbolTableASTVisitor() {
  }

  SymbolTableASTVisitor(boolean debug) {
    super(debug);
  } // enables print for debugging

  /**
   * Looks up id in the symbol table, starting from the current nesting level and going outward.
   * @param id the identifier to look up
   * @return the STentry associated with id, or null if id is not found in the symbol table
   */
  private STentry stLookup(String id) {
    int j = nestingLevel;
    STentry entry = null;
    while (j >= 0 && entry == null) entry = symTable.get(j--).get(id);
    return entry;
  }

  @Override
  public Void visitNode(ProgLetInNode n) {
    if (print) printNode(n);
    Map<String, STentry> hm = new HashMap<>();
    symTable.add(hm);
    for (Node dec : n.declist) visit(dec);
    visit(n.exp);
    symTable.remove(0);
    return null;
  }

  @Override
  public Void visitNode(ProgNode n) {
    if (print) printNode(n);
    visit(n.exp);
    return null;
  }

  @Override
  public Void visitNode(FunNode n) {
    if (print) printNode(n);
    Map<String, STentry> hm = symTable.get(nestingLevel);
    List<TypeNode> parTypes = new ArrayList<>();
    for (ParNode par : n.parlist) parTypes.add(par.getType());
    STentry entry = new STentry(
        nestingLevel,
        new ArrowTypeNode(parTypes, n.retType),
        decOffset--
    );
    //inserimento di ID nella symtable
    if (hm.put(n.id, entry) != null) {
      System.out.println(
          "Fun id " + n.id + " at line " + n.getLine() + " already declared");
      stErrors++;
    }
    //creare una nuova hashmap per la symTable
    nestingLevel++;
    Map<String, STentry> hmn = new HashMap<>();
    symTable.add(hmn);
    int prevNLDecOffset =
        decOffset; // stores counter for offset of declarations at previous nesting level
    decOffset = -2;

    int parOffset = 1;
    for (ParNode par : n.parlist)
      if (hmn.put(par.id, new STentry(nestingLevel, par.getType(), parOffset++)) !=
          null) {
        System.out.println(
            "Par id " + par.id + " at line " + n.getLine() + " already declared");
        stErrors++;
      }
    for (Node dec : n.declist) visit(dec);
    visit(n.exp);
    //rimuovere la hashmap corrente poiche' esco dallo scope
    symTable.remove(nestingLevel--);
    decOffset =
        prevNLDecOffset; // restores counter for offset of declarations at previous nesting level
    return null;
  }

  @Override
  public Void visitNode(VarNode n) {
    if (print) printNode(n);
    visit(n.exp);
    Map<String, STentry> hm = symTable.get(nestingLevel);
    STentry entry = new STentry(nestingLevel, n.getType(), decOffset--);
    //inserimento di ID nella symtable
    if (hm.put(n.id, entry) != null) {
      System.out.println(
          "Var id " + n.id + " at line " + n.getLine() + " already declared");
      stErrors++;
    }
    return null;
  }

  @Override
  public Void visitNode(PrintNode n) {
    if (print) printNode(n);
    visit(n.exp);
    return null;
  }

  @Override
  public Void visitNode(IfNode n) {
    if (print) printNode(n);
    visit(n.cond);
    visit(n.th);
    visit(n.el);
    return null;
  }

  @Override
  public Void visitNode(EqualNode n) {
    if (print) printNode(n);
    visit(n.left);
    visit(n.right);
    return null;
  }

  @Override
  public Void visitNode(GreaterEqualNode n) {
    if (print) printNode(n);
    visit(n.left);
    visit(n.right);
    return null;
  }

  @Override
  public Void visitNode(LessEqualNode n) {
    if (print) printNode(n);
    visit(n.left);
    visit(n.right);
    return null;
  }

  // New!
  @Override
  public Void visitNode(NotNode n) {
    if (print) printNode(n);
    visit(n.exp);
    return null;
  }

  @Override
  public Void visitNode(TimesNode n) {
    if (print) printNode(n);
    visit(n.left);
    visit(n.right);
    return null;
  }

  @Override
  public Void visitNode(DivNode n) {
    if (print) printNode(n);
    visit(n.left);
    visit(n.right);
    return null;
  }

  @Override
  public Void visitNode(PlusNode n) {
    if (print) printNode(n);
    visit(n.left);
    visit(n.right);
    return null;
  }

  @Override
  public Void visitNode(MinusNode n) {
    if (print) printNode(n);
    visit(n.left);
    visit(n.right);
    return null;
  }

  @Override
  public Void visitNode(CallNode n) {
    if (print) printNode(n);
    STentry entry = stLookup(n.id);
    if (entry == null) {
      System.out.println("Fun id " + n.id + " at line " + n.getLine() + " not declared");
      stErrors++;
    } else {
      n.entry = entry;
      n.nl = nestingLevel;
    }
    for (Node arg : n.arglist) visit(arg);
    return null;
  }

  @Override
  public Void visitNode(IdNode n) {
    if (print) printNode(n);
    STentry entry = stLookup(n.id);
    if (entry == null) {
      System.out.println(
          "Var or Par id " + n.id + " at line " + n.getLine() + " not declared");
      stErrors++;
    } else {
      n.entry = entry;
      n.nl = nestingLevel;
    }
    return null;
  }

  @Override
  public Void visitNode(BoolNode n) {
    if (print) printNode(n, n.val.toString());
    return null;
  }

  @Override
  public Void visitNode(IntNode n) {
    if (print) printNode(n, n.val.toString());
    return null;
  }

  @Override
  public Void visitNode(EmptyNode n) {
    if (print) printNode(n);
    return null;
  }

  @Override
  public Void visitNode(AndNode n) {
    if (print) printNode(n);
    visit(n.left);
    visit(n.right);
    return null;
  }

  @Override
  public Void visitNode(OrNode n) {
    if (print) printNode(n);
    visit(n.left);
    visit(n.right);
    return null;
  }

  @Override
  public Void visitNode(ClassNode n) {
    if (print) printNode(n);

    // Phase 1

    // Get the global symbol table (nesting level 0)
    Map<String, STentry> globalSymTable = symTable.getFirst();
    List<TypeNode> allFields = new ArrayList<>();
    List<ArrowTypeNode> allMethods = new ArrayList<>();

    // If the class extends another class, look up the superclass in the global symbol table and,
    // if found, add its fields and methods to the lists of all fields and methods of the current class
    if (n.superID != null) {
      n.superEntry = stLookup(n.superID);
      ClassTypeNode classTypeNode = (ClassTypeNode) n.superEntry.type;
      allFields.addAll(classTypeNode.allFields);
      allMethods.addAll(classTypeNode.allMethods);
    }
    // Add the fields and methods declared in the current class to the lists of all fields and methods of the current class.
    STentry sTentry = new STentry(0, new ClassTypeNode(allFields, allMethods), decOffset--);
    n.setType(sTentry.type);

    // Store the STentry of the current class in the global symbol table, checking for duplicate declarations
    if (globalSymTable.put(n.ID, sTentry) != null) {
      System.out.println("Class id " + n.ID + " at line " + n.getLine() + " already declared");
      stErrors++;
    }

    // Phase 2
    // New level for the Symbol Table
    nestingLevel++;
    Map<String, STentry> virtualTable = new HashMap<>();
    if (n.superID != null) {
      Map<String, STentry> superVirtualTable = classTable.get(n.superID);
      virtualTable.putAll(superVirtualTable);
    }
    // ???
    symTable.add(virtualTable);
    // Add Class name mapped with its virtual table
    classTable.put(n.ID, virtualTable);

    int fieldOffset = -n.fields.size() - 1;
    Set<String> newFields = new HashSet<>();
    for (FieldNode field : n.fields) {
      if (print) printNode(field);
      STentry oldFieldEntry = virtualTable.get(field.id);
      STentry fieldEntry;

      if (!newFields.add(field.id)) {
        System.out.println("Field id " +  field.id + " at line " + field.getLine() + " already declared");
        stErrors++;
      }
      // If there is already an entry for the field name in the virtual table,
      // check if it is a method or a field and print the appropriate error message.
      // If it is a method, print an error message indicating that the field cannot override a method.
      if (oldFieldEntry != null) {
        // Avoid overriding a method with a field
        if (oldFieldEntry.type instanceof ArrowTypeNode) {
          System.out.println("Field id " + field.id + " at line " + field.getLine()
              + " already declared as method in class " + n.ID);
          stErrors++;
        } else {
          System.out.println("Field id " + field.id + " at line " + field.getLine()
              + " already declared in class " + n.ID + " -> Overriding.");
        }
        // Dummy Entry to avoid NullPointerException in the code generation phase
        fieldEntry = new STentry(nestingLevel, field.getType(), oldFieldEntry.offset);
      }
      // If there is no entry for the field name in the virtual table, add it to the virtual table with a new offset.
      else {
        fieldEntry = new STentry(nestingLevel, field.getType(), fieldOffset--);
      }

      // ???
      field.offset = fieldEntry.offset;
      virtualTable.put(field.id, fieldEntry);
      allFields.add(-fieldEntry.offset - 1, field.getType());
    };

    int previousDecOffset = decOffset;
    decOffset = allMethods.size();
    List<String> newMethods = new ArrayList<>();

    for (MethodNode method : n.methods) {
      System.out.println("Method " + " in class " + n.ID);
    }

    return null;
  }

  @Override
  public Void visitNode(FieldNode n) {
    if (print) printNode(n, n.id);
    return null;
  }

  public Void visitNode(MethodNode n) {
    if (print) printNode(n);

    // 1. Get the current Symbol Table level
    Map<String, STentry> hm = symTable.get(nestingLevel);

    // 2. Build the functional type of the method
    List<TypeNode> parTypes = new ArrayList<>();
    for (ParNode par : n.parlist) parTypes.add(par.getType());
    TypeNode methodType = new ArrowTypeNode(parTypes, n.retType);

    // TODO: per type checking può servire un n.setType?

    // 3. Insert method in Virtual Table and handle Overriding
    STentry oldEntry = hm.get(n.id);
    STentry newEntry;

    if (oldEntry != null) {
      // Check that we are not overriding a non-method
      if (!(oldEntry.type instanceof ArrowTypeNode)) {
        System.out.println(
            "Method id " + n.id + " at line " + n.getLine() + " cannot override field");
        stErrors++;
      }
      // Overriding. We replace the entry but keep the old offset
      newEntry = new STentry(nestingLevel, methodType, oldEntry.offset);
    } else {
      // New method. We assign a new offset
      newEntry = new STentry(nestingLevel, methodType, decOffset++);
    }

    n.offset = newEntry.offset;
    hm.put(n.id, newEntry);

    // 4. Open a new scope for the mothod's parameters and local declarations
    nestingLevel++;
    Map<String, STentry> hmn = new HashMap<>();
    symTable.add(hmn);

    int prevNLDecOffset = decOffset; // Save the offset counter for methods
    decOffset = -2; // Reset the offset counter for parameters and local variables

    // 5. Insert parameters in the method scope
    int parOffset = 1; // Parameters start at offset 1
    for (ParNode par : n.parlist) {
      if (hmn.put(par.id, new STentry(nestingLevel, par.getType(), parOffset++)) != null) {
        System.out.println("Par id " + par.id + " at line " + n.getLine() + " already declared");
        stErrors++;
      }
    }

    // 6. Visit local declarations and method body
    for (Node dec : n.declist) visit(dec);
    visit(n.exp);

    // 7. Close the method scope and restore the offset counter
    symTable.remove(nestingLevel--);
    decOffset = prevNLDecOffset; // Restore the offset counter for methods

    return null;
  }
}
