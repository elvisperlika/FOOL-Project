package compiler;

import compiler.lib.BaseASTVisitor;
import compiler.lib.DecNode;
import compiler.lib.Node;
import compiler.lib.TypeNode;

import java.util.Collections;
import java.util.List;

public class AST {

  public static class ProgLetInNode extends Node {
    final List<DecNode> declist;
    final Node exp;

    ProgLetInNode(List<DecNode> d, Node e) {
      declist = Collections.unmodifiableList(d);
      exp = e;
    }

    @Override
    public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor)
            throws E { return visitor.visitNode(this); }
  }

  public static class ProgNode extends Node {
    final Node exp;

    ProgNode(Node e) { exp = e; }

    @Override
    public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor)
            throws E { return visitor.visitNode(this); }
  }

  public static class FunNode extends DecNode {
    final String id;
    final TypeNode retType;
    final List<ParNode> parlist;
    final List<DecNode> declist;
    final Node exp;

    FunNode(String i, TypeNode rt, List<ParNode> pl, List<DecNode> dl, Node e) {
      id = i;
      retType = rt;
      parlist = Collections.unmodifiableList(pl);
      declist = Collections.unmodifiableList(dl);
      exp = e;
    }

    //void setType(TypeNode t) {type = t;}

    @Override
    public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor)
            throws E { return visitor.visitNode(this); }
  }

  public static class ParNode extends DecNode {
    final String id;

    ParNode(String i, TypeNode t) {
      id = i;
      type = t;
    }

    @Override
    public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor)
            throws E { return visitor.visitNode(this); }
  }

  public static class VarNode extends DecNode {
    final String id;
    final Node exp;

    VarNode(String i, TypeNode t, Node v) {
      id = i;
      type = t;
      exp = v;
    }

    @Override
    public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor)
            throws E { return visitor.visitNode(this); }
  }

  public static class PrintNode extends Node {
    final Node exp;

    PrintNode(Node e) { exp = e; }

    @Override
    public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor)
            throws E { return visitor.visitNode(this); }
  }

  public static class IfNode extends Node {
    final Node cond;
    final Node th;
    final Node el;

    IfNode(Node c, Node t, Node e) {
      cond = c;
      th = t;
      el = e;
    }

    @Override
    public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor)
            throws E { return visitor.visitNode(this); }
  }

  public static class EqualNode extends Node {
    final Node left;
    final Node right;

    EqualNode(Node l, Node r) {
      left = l;
      right = r;
    }

    @Override
    public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor)
            throws E { return visitor.visitNode(this); }
  }

  // New!
  public static class GreaterEqualNode extends Node {
    final Node left;
    final Node right;

    GreaterEqualNode(Node l, Node r) {
      left = l;
      right = r;
    }

    @Override
    public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor)
            throws E { return visitor.visitNode(this); }
  }

  // New!
  public static class LessEqualNode extends Node {
    final Node left;
    final Node right;

    LessEqualNode(Node l, Node r) {
      left = l;
      right = r;
    }

    @Override
    public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor)
            throws E { return visitor.visitNode(this); }
  }

  public static class TimesNode extends Node {
    final Node left;
    final Node right;

    TimesNode(Node l, Node r) {
      left = l;
      right = r;
    }

    @Override
    public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor)
            throws E { return visitor.visitNode(this); }
  }

  // New!
  public static class DivNode extends Node {
    final Node left;
    final Node right;

    DivNode(Node l, Node r) {
      left = l;
      right = r;
    }

    @Override
    public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor)
            throws E { return visitor.visitNode(this); }
  }

  // New!
  public static class OrNode extends Node {
    final Node left;
    final Node right;

    OrNode(Node l, Node r) {
      left = l;
      right = r;
    }

    @Override
    public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor)
            throws E { return visitor.visitNode(this); }
  }

  // New!
  public static class AndNode extends Node {
    final Node left;
    final Node right;

    AndNode(Node l, Node r) {
      left = l;
      right = r;
    }

    @Override
    public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor)
            throws E { return visitor.visitNode(this); }
  }

  public static class PlusNode extends Node {
    final Node left;
    final Node right;

    PlusNode(Node l, Node r) {
      left = l;
      right = r;
    }

    @Override
    public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor)
            throws E { return visitor.visitNode(this); }
  }

  // New!
  public static class MinusNode extends Node {
    final Node left;
    final Node right;

    MinusNode(Node l, Node r) {
      left = l;
      right = r;
    }

    @Override
    public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor)
            throws E { return visitor.visitNode(this); }
  }

  // New!
  public static class NotNode extends Node {
    final Node exp;

    NotNode(Node e) { exp = e; }

    @Override
    public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor)
            throws E { return visitor.visitNode(this); }
  }

  public static class CallNode extends Node {
    final String id;
    final List<Node> arglist;
    STentry entry;
    int nl;

    CallNode(String i, List<Node> p) {
      id = i;
      arglist = Collections.unmodifiableList(p);
    }

    @Override
    public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor)
            throws E { return visitor.visitNode(this); }
  }

  public static class IdNode extends Node {
    final String id;
    STentry entry;
    int nl;

    IdNode(String i) { id = i; }

    @Override
    public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor)
            throws E { return visitor.visitNode(this); }
  }

  public static class BoolNode extends Node {
    final Boolean val;

    BoolNode(boolean n) { val = n; }

    @Override
    public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor)
            throws E { return visitor.visitNode(this); }
  }

  public static class IntNode extends Node {
    final Integer val;

    IntNode(Integer n) { val = n; }

    @Override
    public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor)
            throws E { return visitor.visitNode(this); }
  }

  public static class ArrowTypeNode extends TypeNode {
    final List<TypeNode> parlist;
    final TypeNode ret;

    ArrowTypeNode(List<TypeNode> p, TypeNode r) {
      parlist = Collections.unmodifiableList(p);
      ret = r;
    }

    @Override
    public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor)
            throws E { return visitor.visitNode(this); }
  }

  public static class BoolTypeNode extends TypeNode {

    @Override
    public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor)
            throws E { return visitor.visitNode(this); }
  }

  public static class IntTypeNode extends TypeNode {

    @Override
    public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor)
            throws E { return visitor.visitNode(this); }
  }

  public static class ClassNode extends DecNode {

    /**
     * The name of the class.
     */
    final String ID;
    /**
     * The name of the superclass, if any.
     * If null, then the class does not extend any other class.
     */
    final String superID;
    /**
     * The list of fields and methods of the class, in the order they are declared.
     */
    final List<FieldNode> fields;
    /**
     * The list of methods of the class, in the order they are declared.
     */
    final List<MethodNode> methods;

    ClassNode(String id, List<FieldNode> fields, List<MethodNode> methods,
              String superID) {
      this.ID = id;
      this.fields = Collections.unmodifiableList(fields);
      this.methods = Collections.unmodifiableList(methods);
      this.superID = superID;
    }

    /**
     * Set the type of this class node. Used during type checking.
     * @param t the type of this class node
     */
    void setType(TypeNode t) { type = t; }

    @Override
    public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
      return visitor.visitNode(this);
    }
  }

  public static class NewNode extends Node {

    /**
     * The name of the class whose instance is being created.
     */
    final String ID;
    /**
     * The list of arguments passed to the constructor of the class,
     * in the order they are declared.
     */
    final List<Node> arglist;
    /**
     * The Symbol Table Entry of the class whose instance is being created.
     * Used during type checking.
     */
    STentry entry;

    public NewNode(String id, List<Node> arglist) {
      this.ID = id;
      this.arglist = arglist;
    }

    public void setEntry(STentry entry) { this.entry = entry; }

    @Override
    public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
      return visitor.visitNode(this);
    }
  }

  public static class FieldNode {}
  public static class MethodNode {}

}