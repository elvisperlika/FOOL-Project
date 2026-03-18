package compiler;

import compiler.AST.*;
import compiler.exc.IncomplException;
import compiler.exc.TypeException;
import compiler.lib.BaseEASTVisitor;
import compiler.lib.Node;
import compiler.lib.TypeNode;

import static compiler.TypeRels.isSubtype;
import static compiler.TypeRels.lowestCommonAncestor;

//visitNode(n) fa il type checking di un Node n e ritorna:
//- per una espressione, il suo tipo (oggetto BoolTypeNode o IntTypeNode)
//- per una dichiarazione, "null"; controlla la correttezza interna della dichiarazione
//(- per un tipo: "null"; controlla che il tipo non sia incompleto) 
//
//visitSTentry(s) ritorna, per una STentry s, il tipo contenuto al suo interno
public class TypeCheckEASTVisitor extends BaseEASTVisitor<TypeNode, TypeException> {

  TypeCheckEASTVisitor() {
    super(true);
  } // enables incomplete tree exceptions

  TypeCheckEASTVisitor(boolean debug) {
    super(true, debug);
  } // enables print for debugging

  //checks that a type object is visitable (not incomplete)
  private TypeNode ckvisit(TypeNode t) throws TypeException {
    visit(t);
    return t;
  }

  @Override
  public TypeNode visitNode(ProgLetInNode n) throws TypeException {
    if (print) printNode(n);

    // visit all class declarations
    for (ClassNode classDec : n.classlist) {
      try {
        visit(classDec);
      } catch (IncomplException e) {
      } catch (TypeException e) {
        System.out.println("Type checking error in a class declaration: " + e.text);
      }
    }

    for (Node dec : n.declist)
      try {
        visit(dec);
      } catch (IncomplException e) {
      } catch (TypeException e) {
        System.out.println("Type checking error in a declaration: " + e.text);
      }
    return visit(n.exp);
  }

  @Override
  public TypeNode visitNode(ProgNode n) throws TypeException {
    if (print) printNode(n);
    return visit(n.exp);
  }

  @Override
  public TypeNode visitNode(FunNode n) throws TypeException {
    if (print) printNode(n, n.id);
    for (Node dec : n.declist)
      try {
        visit(dec);
      } catch (IncomplException e) {
      } catch (TypeException e) {
        System.out.println("Type checking error in a declaration: " + e.text);
      }
    if (!isSubtype(visit(n.exp), ckvisit(n.retType)))
      throw new TypeException("Wrong return type for function " + n.id, n.getLine());
    return null;
  }

  @Override
  public TypeNode visitNode(VarNode n) throws TypeException {
    if (print) printNode(n, n.id);
    if (!isSubtype(visit(n.exp), ckvisit(n.getType())))
      throw new TypeException("Incompatible value for variable " + n.id, n.getLine());
    return null;
  }

  @Override
  public TypeNode visitNode(PrintNode n) throws TypeException {
    if (print) printNode(n);
    return visit(n.exp);
  }

  @Override
  public TypeNode visitNode(IfNode n) throws TypeException {
    if (print) printNode(n);
    if (!(isSubtype(visit(n.cond), new BoolTypeNode())))
      throw new TypeException("Non boolean condition in if", n.getLine());
    TypeNode t = visit(n.th);
    TypeNode e = visit(n.el);
    if (isSubtype(t, e)) return e;
    if (isSubtype(e, t)) return t;
    TypeNode lca = TypeRels.lowestCommonAncestor(t, e);
    if (lca != null) return lca;
    throw new TypeException("Incompatible types in then-else branches", n.getLine());
  }

  @Override
  public TypeNode visitNode(EqualNode n) throws TypeException {
    if (print) printNode(n);
    TypeNode l = visit(n.left);
    TypeNode r = visit(n.right);
    // equality is defined for integers and booleans, so we check that both operands
    // are subtypes of the same type
    if (!(isSubtype(l, r) || isSubtype(r, l)))
      throw new TypeException("Incompatible types in equal", n.getLine());
    return new BoolTypeNode();
  }

  // New!
  @Override
  public TypeNode visitNode(GreaterEqualNode n) throws TypeException {
    if (print) printNode(n);
    TypeNode l = visit(n.left);
    TypeNode r = visit(n.right);
    // greater-equal is only defined for integers,
    // so we check that both operands are subtypes of IntTypeNode
    if (!(isSubtype(l, new IntTypeNode()) && isSubtype(r, new IntTypeNode())))
      throw new TypeException("Non integers in greater-equal", n.getLine());
    return new BoolTypeNode();
  }

  // New!
  @Override
  public TypeNode visitNode(LessEqualNode n) throws TypeException {
    if (print) printNode(n);
    TypeNode l = visit(n.left);
    TypeNode r = visit(n.right);
    // less-equal is only defined for integers,
    // so we check that both operands are subtypes of IntTypeNode
    if (!(isSubtype(l, new IntTypeNode()) && isSubtype(r, new IntTypeNode())))
      throw new TypeException("Non integers in less-equal", n.getLine());
    return new BoolTypeNode();
  }

  @Override
  public TypeNode visitNode(TimesNode n) throws TypeException {
    if (print) printNode(n);
    // multiplication is only defined for integers,
    // so we check that both operands are subtypes of IntTypeNode
    if (!(isSubtype(visit(n.left), new IntTypeNode()) &&
        isSubtype(visit(n.right), new IntTypeNode())))
      throw new TypeException("Non integers in multiplication", n.getLine());
    return new IntTypeNode();
  }

  // New!
  @Override
  public TypeNode visitNode(DivNode n) throws TypeException {
    if (print) printNode(n);
    // division is only defined for integers,
    // so we check that both operands are subtypes of IntTypeNode
    if (!(isSubtype(visit(n.left), new IntTypeNode()) &&
        isSubtype(visit(n.right), new IntTypeNode())))
      throw new TypeException("Non integers in division", n.getLine());
    return new IntTypeNode();
  }

  @Override
  public TypeNode visitNode(PlusNode n) throws TypeException {
    if (print) printNode(n);
    if (!(isSubtype(visit(n.left), new IntTypeNode()) &&
        isSubtype(visit(n.right), new IntTypeNode())))
      throw new TypeException("Non integers in sum", n.getLine());
    return new IntTypeNode();
  }

  // New!
  @Override
  public TypeNode visitNode(MinusNode n) throws TypeException {
    if (print) printNode(n);
    if (!(isSubtype(visit(n.left), new IntTypeNode()) &&
        isSubtype(visit(n.right), new IntTypeNode())))
      throw new TypeException("Non integers in difference", n.getLine());
    return new IntTypeNode();
  }

  // New!
  @Override
  public TypeNode visitNode(AndNode n) throws TypeException {
    if (print) printNode(n);
    // and is only defined for booleans,
    // so we check that both operands are subtypes of BoolTypeNode
    if (!(isSubtype(visit(n.left), new BoolTypeNode()) &&
        isSubtype(visit(n.right), new BoolTypeNode())))
      throw new TypeException("Non booleans in and", n.getLine());
    return new BoolTypeNode();
  }

  // New!
  public TypeNode visitNode(OrNode n) throws TypeException {
    if (print) printNode(n);
    // or is only defined for booleans,
    // so we check that both operands are subtypes of BoolTypeNode
    if (!(isSubtype(visit(n.left), new BoolTypeNode()) &&
        isSubtype(visit(n.right), new BoolTypeNode())))
      throw new TypeException("Non booleans in or", n.getLine());
    return new BoolTypeNode();
  }

  // New!
  @Override
  public TypeNode visitNode(NotNode n) throws TypeException {
    if (print) printNode(n);
    // not is only defined for booleans,
    // so we check that the operand is a subtype of BoolTypeNode
    if (!(isSubtype(visit(n.exp), new BoolTypeNode())))
      throw new TypeException("Non boolean in not", n.getLine());
    return new BoolTypeNode();
  }

  @Override
  public TypeNode visitNode(CallNode n) throws TypeException {
    if (print) printNode(n, n.id);
    TypeNode t = visit(n.entry);
    if (!(t instanceof ArrowTypeNode))
      throw new TypeException("Invocation of a non-function " + n.id, n.getLine());
    ArrowTypeNode at = (ArrowTypeNode) t;
    if (!(at.parlist.size() == n.arglist.size())) throw new TypeException(
        "Wrong number of parameters in the invocation of " + n.id,
        n.getLine()
    );
    for (int i = 0; i < n.arglist.size(); i++)
      if (!(isSubtype(visit(n.arglist.get(i)), at.parlist.get(i))))
        throw new TypeException(
            "Wrong type for " + (i + 1) + "-th parameter in the invocation of " +
                n.id,
            n.getLine()
        );
    return at.ret;
  }

  // L'IdNode è tipo 'x', l'uso di una var. o di un par.
  // La differenza tra un IdNode e un CallNode è che il CallNode ha le parentesi tonde dopo.
  @Override
  public TypeNode visitNode(IdNode n) throws TypeException {
    if (print) printNode(n, n.id);
    TypeNode t = visit(n.entry);
    // Mi assicuro che non sia un tipo funzionale.
    if (t instanceof ArrowTypeNode)
      throw new TypeException("Wrong usage of function identifier " + n.id, n.getLine());
    return t;
  }

  @Override
  public TypeNode visitNode(BoolNode n) {
    if (print) printNode(n, n.val.toString());
    return new BoolTypeNode();
  }

  @Override
  public TypeNode visitNode(IntNode n) {
    if (print) printNode(n, n.val.toString());
    return new IntTypeNode();
  }

  @Override
  public TypeNode visitNode(EmptyNode n) {
    if (print) printNode(n);
    return new EmptyTypeNode();
  }

// gestione tipi incompleti	(se lo sono lancia eccezione)

  @Override
  public TypeNode visitNode(ArrowTypeNode n) throws TypeException {
    if (print) printNode(n);
    for (Node par : n.parlist) visit(par);
    visit(n.ret, "->"); //marks return type
    return null;
  }

  @Override
  public TypeNode visitNode(BoolTypeNode n) {
    if (print) printNode(n);
    return null;
  }

  @Override
  public TypeNode visitNode(IntTypeNode n) {
    if (print) printNode(n);
    return null;
  }

  @Override
  public TypeNode visitNode(EmptyTypeNode n) {
    if (print) printNode(n);
    return null;
  }

  @Override
  public TypeNode visitNode(RefTypeNode n) {
    if (print) printNode(n);
    return null;
  }

  public TypeNode visitNode(ClassNode n) throws TypeException {
    if (print) printNode(n, n.ID);

    // 1. Check if the class extends another class
    if (n.superID != null) {
      // 2. Add the relation to the superType map
      TypeRels.superType.put(n.ID, n.superID);
    }

    ClassTypeNode parentType = n.superID != null ? (ClassTypeNode) n.superEntry.type : null;

    // 3. Check field overrides
    for (FieldNode field : n.fields) {
      if (parentType != null) {
        // calculate the offset of the field
        int offset = -field.offset - 1;

        // if position is less then length of parent fields, then it's an override
        if (offset < parentType.allFields.size()) {
          TypeNode parentFieldType = parentType.allFields.get(offset);
          // check that the field type is subtype of the parent field type
          if (!isSubtype(field.getType(), parentFieldType))
            throw new TypeException("Wrong override of field " + field.id + " in class " + n.ID, field.getLine());
        }
      }
    }

    // 4. Type check all methods
    for (MethodNode method : n.methods) {
      visit(method);

      // Override check
      if (parentType != null) {
        // calculate the offset of the method
        int offset = method.offset;

        // if position is less then length of parent methods, then it's an override
        if (offset < parentType.allMethods.size()) {
          TypeNode parentMethodType = parentType.allMethods.get(offset);
          // check that the method type is subtype of the parent method type
          if (!isSubtype(method.getType(), parentMethodType))
            throw new TypeException("Wrong override of method " + method.id + " in class " + n.ID, method.getLine());
        }
      }
    }
    return null;
  }

  @Override
  public TypeNode visitNode(ClassCallNode n) throws TypeException {
    if (print) printNode(n, n.objectID + "." + n.methodID);

    // 1. Check that the method entry is a ArrowTypeNode
    TypeNode t = visit(n.methodEntry);
    if (!(t instanceof ArrowTypeNode))
      throw new TypeException("Invocation of a non-function " + n.methodID, n.getLine());

    ArrowTypeNode at = (ArrowTypeNode) t;

    // 2. Check argument count
    if (at.parlist.size() != n.arglist.size())
      throw new TypeException(
          "Wrong number of parameters in the invocation of " + n.methodID, n.getLine());

    // 3. Check argument types
    for (int i = 0; i < n.arglist.size(); i++) {
      if (!isSubtype(visit(n.arglist.get(i)), at.parlist.get(i)))
        throw new TypeException("Wrong type for " + (i + 1) + "-th parameter in the invocation of " + n.methodID, n.getLine());
    }
    return at.ret; // return the method's return type as in CallNode
  }

  @Override
  public TypeNode visitNode(MethodNode n) throws TypeException {
    if (print) printNode(n, n.id);

    // 1. Visit local declarations
    for (Node dec : n.declist) {
      try {
        visit(dec);
      } catch (IncomplException e) {
      } catch (TypeException e) {
        System.out.println("Type checking error in a declaration: " + e.text);
      }
    }

    // 2. Check that return type is subtype of declared return type
    if (!isSubtype(visit(n.exp), ckvisit(n.retType)))
      throw new TypeException("Wrong return type for method " + n.id, n.getLine());

    return null;
  }

  @Override
  public TypeNode visitNode(NewNode n) throws TypeException {
    if (print) printNode(n, n.ID);

    // 1. Get the ClassTypeNode from the entry
    TypeNode t = visit(n.entry);
    if (!(t instanceof ClassTypeNode))
      throw new TypeException("Instantiation of a non-class " + n.ID, n.getLine());

    ClassTypeNode ct = (ClassTypeNode) t;

    // 2. Check argument count
    if (ct.allFields.size() != n.arglist.size())
      throw new TypeException("Wrong number of parameters in the instantiation of " + n.ID, n.getLine());

    // 3. Check argument types against the class fields
    for (int i = 0; i < n.arglist.size(); i++) {
      if (!isSubtype(visit(n.arglist.get(i)), ct.allFields.get(i)))
        throw new TypeException("Wrong type for " + (i + 1) + "-th parameter in the instantiation of " + n.ID, n.getLine());
    }

    return new RefTypeNode(n.ID);
  }

  @Override
  public TypeNode visitNode(ClassTypeNode n) {
    if (print) printNode(n);
    return null;
  }

  @Override
  public TypeNode visitNode(FieldNode n) throws TypeException {
    if (print) printNode(n, n.id);
    return null; // TODO: check when implementing newnode typechecking
  }

// STentry (ritorna campo type)

  @Override
  public TypeNode visitSTentry(STentry entry) throws TypeException {
    if (print) printSTentry("type");
    return ckvisit(entry.type);
  }

}