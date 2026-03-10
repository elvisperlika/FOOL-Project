package compiler;

import compiler.AST.*;
import compiler.exc.VoidException;
import compiler.lib.BaseASTVisitor;
import compiler.lib.Node;
import svm.ExecuteVisualVM;

import java.util.ArrayList;
import java.util.List;

import static compiler.lib.FOOLlib.*;

public class CodeGenerationASTVisitor extends BaseASTVisitor<String, VoidException> {
  private List<List<String>> dispatchTables = new ArrayList<>();

  CodeGenerationASTVisitor() {
  }

  CodeGenerationASTVisitor(boolean debug) {
    super(false, debug);
  } //enables print for debugging

  @Override
  public String visitNode(ProgLetInNode n) {
    if (print) printNode(n);
    String declCode = null;
    for (Node dec : n.declist)
      declCode = nlJoin(declCode, visit(dec));
    return nlJoin(
        "push 0", declCode, // generate code for declarations (allocation)
        visit(n.exp), "halt", getCode()
    );
  }

  @Override
  public String visitNode(ProgNode n) {
    if (print) printNode(n);
    return nlJoin(visit(n.exp), "halt");
  }

  @Override
  public String visitNode(FunNode n) {
    if (print) printNode(n, n.id);
    String declCode = null, popDecl = null, popParl = null;
    for (Node dec : n.declist) {
      declCode = nlJoin(declCode, visit(dec));
      popDecl = nlJoin(popDecl, "pop");
    }
    for (int i = 0; i < n.parlist.size(); i++)
      popParl = nlJoin(popParl, "pop");
    String funl = freshFunLabel();
    putCode(nlJoin(
        funl + ":", "cfp", // set $fp to $sp value
        "lra", // load $ra value
        declCode, // generate code for local declarations (they use the new $fp!!!)
        visit(n.exp), // generate code for function body expression
        "stm", // set $tm to popped value (function result)
        popDecl, // remove local declarations from stack
        "sra", // set $ra to popped value
        "pop", // remove Access Link from stack
        popParl, // remove parameters from stack
        "sfp", // set $fp to popped value (Control Link)
        "ltm", // load $tm value (function result)
        "lra", // load $ra value
        "js"  // jump to to popped address
    ));
    return "push " + funl;
  }

  @Override
  public String visitNode(VarNode n) {
    if (print) printNode(n, n.id);
    return visit(n.exp);
  }

  @Override
  public String visitNode(PrintNode n) {
    if (print) printNode(n);
    return nlJoin(visit(n.exp), "print");
  }

  @Override
  public String visitNode(IfNode n) {
    if (print) printNode(n);
    String l1 = freshLabel();
    String l2 = freshLabel();
    return nlJoin(
        visit(n.cond),
        "push 1",
        "beq " + l1,
        visit(n.el),
        "b " + l2,
        l1 + ":",
        visit(n.th),
        l2 + ":"
    );
  }

  @Override
  public String visitNode(EqualNode n) {
    if (print) printNode(n);
    String l1 = freshLabel();
    String l2 = freshLabel();
    return nlJoin(
        visit(n.left),
        visit(n.right),
        "beq " + l1,
        "push 0",
        "b " + l2,
        l1 + ":",
        "push 1",
        l2 + ":"
    );
  }

  @Override
  public String visitNode(TimesNode n) {
    if (print) printNode(n);
    return nlJoin(visit(n.left), visit(n.right), "mult");
  }

  @Override
  public String visitNode(DivNode n) {
    if (print) printNode(n);
    return nlJoin(visit(n.left), visit(n.right), "div");
  }

  @Override
  public String visitNode(PlusNode n) {
    if (print) printNode(n);
    return nlJoin(visit(n.left), visit(n.right), "add");
  }

  @Override
  public String visitNode(MinusNode n) {
    if (print) printNode(n);
    return nlJoin(visit(n.left), visit(n.right), "sub");
  }

  @Override
  public String visitNode(CallNode n) {
    if (print) printNode(n, n.id);
    String argCode = null, getAR = null;
    for (int i = n.arglist.size() - 1; i >= 0; i--)
      argCode = nlJoin(argCode, visit(n.arglist.get(i)));
    for (int i = 0; i < n.nl - n.entry.nl; i++)
      getAR = nlJoin(getAR, "lw");
    return nlJoin(
        "lfp", // load Control Link (pointer to frame of function "id" caller)
        argCode, // generate code for argument expressions in reversed order
        "lfp", getAR, // retrieve address of frame containing "id" declaration
        // by following the static chain (of Access Links)
        "stm", // set $tm to popped value (with the aim of duplicating top of stack)
        "ltm", // load Access Link (pointer to frame of function "id" declaration)
        "ltm", // duplicate top of stack
        "push " + n.entry.offset, "add", // compute address of "id" declaration
        "lw", // load address of "id" function
        "js"  // jump to popped address (saving address of subsequent instruction in $ra)
    );
  }

  @Override
  public String visitNode(IdNode n) {
    if (print) printNode(n, n.id);
    String getAR = null;
    // Risalgo (della differenza di nesting level) la catena statica per
    // raggiungere l'AR che contiene la dichiarazione di "id"
    // Se è un campo risalendo la catena l'ultimo passo finisco nell'oggetto quindi lo stesso
    // meccanismo di risalita funziona sia per variabili che per campi
    for (int i = 0; i < n.nl - n.entry.nl; i++)
      getAR = nlJoin(getAR, "lw");
    return nlJoin(
        "lfp", getAR, // retrieve address of frame containing "id" declaration
        // by following the static chain (of Access Links)
        "push " + n.entry.offset, "add", // compute address of "id" declaration
        "lw" // load value of "id" variable
    );
  }

  @Override
  public String visitNode(BoolNode n) {
    if (print) printNode(n, n.val.toString());
    return "push " + (n.val ? 1 : 0);
  }

  @Override
  public String visitNode(IntNode n) {
    if (print) printNode(n, n.val.toString());
    return "push " + n.val;
  }

  @Override
  public String visitNode(NotNode n) throws VoidException {
    if (print) printNode(n);
    String l1 = freshLabel();
    String l2 = freshLabel();
    return nlJoin(
        visit(n.exp), "push 0", "beq " + l1, // if exp is false jump to l1
        "push 0", "b " + l2, // else push 0 (false) and jump to l2
        l1 + ":", "push 1", // if exp is false push 1 (true)
        l2 + ":"
    );
  }

  @Override
  public String visitNode(GreaterEqualNode n) {
    if (print) printNode(n);
    String l1 = freshLabel();
    String l2 = freshLabel();
    return nlJoin(
        visit(n.right),   // Pushed first, becomes the "second one" popped
        visit(n.left),    // Pushed second, becomes the "first one" popped (top of stack)
        "bleq " + l1,     // Jumps if right <= left (which means left >= right)
        "push 0",         // If not, push 0 (false)
        "b " + l2,        // Jump to the end
        l1 + ":", "push 1",         // If true, push 1 (true)
        l2 + ":"          // End of the expression
    );
  }

  @Override
  public String visitNode(LessEqualNode n) {
    if (print) printNode(n);
    String l1 = freshLabel();
    String l2 = freshLabel();
    return nlJoin(
        visit(n.left),    // Pushed first, becomes the "second one" popped
        visit(n.right),   // Pushed second, becomes the "first one" popped (top of stack)
        "bleq " + l1,     // Jumps if left <= right
        "push 0",         // If not, push 0 (false)
        "b " + l2,        // Jump to the end
        l1 + ":", "push 1",         // If true, push 1 (true)
        l2 + ":"          // End of the expression
    );
  }

  public String visitNode(AndNode n) {
    if (print) printNode(n);
    String labelFalse = freshLabel();
    String labelEnd = freshLabel();
    return nlJoin(
        visit(n.left), // push the left branch
        "push 0",             // push 0 to check if the left branch is false
        "beq " + labelFalse,   // if left branch is false, jump to false label
        visit(n.right),       // push the right branch
        "push 0",             // push 0 to check if the right branch is true
        "beq " + labelFalse,       // if right branch is true, got to label true
        "push 1",             // push 1 to assert && is satisfied
        "b " + labelEnd,      // jump to the end
        labelFalse + ":",
        "push 0",             // push 0 to assert result && is false
        labelEnd + ":"
    );
  }

  public String visitNode(OrNode n) {
    if (print) printNode(n);
    String labelTrue = freshLabel();
    String labelEnd = freshLabel();
    return nlJoin(
        visit(n.left), // push the left branch
        "push 1",             // push 0 to check if the left branch is false
        "beq " + labelTrue,   // if left branch is false, jump to false label
        visit(n.right),       // push the right branch
        "push 1",             // push 0 to check if the right branch is true
        "beq " + labelTrue,       // if right branch is true, got to label true
        "push 0",             // push 1 to assert && is satisfied
        "b " + labelEnd,      // jump to the end
        labelTrue + ":",
        "push 1",             // push 0 to assert result && is false
        labelEnd + ":"
    );
  }

  @Override
  public String visitNode(FieldNode n) {
    if (print) printNode(n, n.id);
    return ""; // Does not generate code
  }


  @Override
  public String visitNode(EmptyNode n) {
    if (print) printNode(n);
    return "push -1";
  }

  @Override
  public String visitNode(NewNode n) {
    if (print) printNode(n);
    // Before: Save fields on the stack
    String argCode = null;
    for (Node arg : n.arglist)
      argCode = nlJoin(argCode, visit(arg));

    // Next (1): get fields from the stack and put them in the heap
    String storeArgsCode = null;
    for (Node _ : n.arglist)
      storeArgsCode = nlJoin(
          storeArgsCode,
          "lhp",      // load heap pointer on the stack
          "sw",       // pop the hp and the arg below it and store the arg in the heap at the hp address
          increaseHeapPointer()
      ); // store field value in heap

    // Next (2): store class dispatch pointer in the heap
    int classOffset = n.entry.offset; // get class offset in vtable
    String storeDispatchPointerCode = nlJoin(
        "push " + ExecuteVisualVM.MEMSIZE,
        "push " + classOffset,
        "add",        // compute address of class dispatch pointer
        "lw",         // load class dispatch pointer replacing the address on the stack with its value
        "lhp",        // load heap pointer on the stack
        "sw"          // pop the hp and the dispatch pointer below it and store the dispatch pointer in the heap at the hp address
    );

    // Next (3): return the address of the new object on the stack
    String returnObjectPointer = nlJoin(
        "lhp",
        increaseHeapPointer()
    );

    return nlJoin(
        argCode,
        storeArgsCode,
        storeDispatchPointerCode,
        returnObjectPointer
    );
  }

  /**
   * Generates code to increment the heap pointer by 1.
   * This is used to allocate space for a new object in the heap.
   *
   * @return the code to increment the heap pointer
   */
  private String increaseHeapPointer() {
    return nlJoin(
        "lhp",      // load heap pointer on the stack
        "push 1",   // push 1 on the stack
        "add",      // increment heap pointer by 1
        "shp"       // store the incremented heap pointer
    );
  }

  @Override
  public String visitNode(ClassNode n) {
    var dispatchTable = this.generateDispatchTable(n);
    this.dispatchTables.add(dispatchTable); // Copy the row in the global dispatch table to make it available to this class' subclasses
    var generatedCode = new StringBuilder();
    generatedCode.append("lhp"); // Load heap pointer on the stack
    dispatchTable.forEach(label -> generatedCode.append(nlJoin(
        "push " + label,
        "lhp",
        "sw", // store method address in the heap
        this.increaseHeapPointer()
    )));
    return generatedCode.toString();
  }

  private ArrayList<String> generateDispatchTable(ClassNode n) {
    var dispatchTable = new ArrayList<String>();
    var classOffset = -(this.dispatchTables.size() + 2);
    if (n.superEntry != null) {
      var superOffset = n.superEntry.offset;
      /* The first declared class has offset -2, the second -3 and so on.
       * Since the first offset is -2, we can get the first index as -(-2)-2
       */
      var superDispatchTable = this.dispatchTables.get(-superOffset - 2);
      dispatchTable.addAll(superDispatchTable);
    }
    for (var method : n.methods) {
      if (method.offset < dispatchTable.size()) {
        dispatchTable.set(method.offset, method.label);
      } else {
        dispatchTable.add(method.label);
      }
    }
    return dispatchTable;
  }
}
