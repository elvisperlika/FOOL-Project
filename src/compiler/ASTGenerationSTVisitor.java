package compiler;

import compiler.AST.*;
import compiler.FOOLParser.*;
import compiler.lib.DecNode;
import compiler.lib.Node;
import compiler.lib.TypeNode;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.ArrayList;
import java.util.List;

import static compiler.lib.FOOLlib.extractCtxName;
import static compiler.lib.FOOLlib.lowerizeFirstChar;

public class ASTGenerationSTVisitor extends FOOLBaseVisitor<Node> {

  public boolean print;
  String indent;

  ASTGenerationSTVisitor() {
  }

  ASTGenerationSTVisitor(boolean debug) {
    print = debug;
  }

  private void printVarAndProdName(ParserRuleContext ctx) {
    String prefix = "";
    Class<?> ctxClass = ctx.getClass(), parentClass = ctxClass.getSuperclass();
    if (!parentClass.equals(ParserRuleContext.class)) // parentClass is the var context (and not ctxClass itself)
      prefix =
          lowerizeFirstChar(extractCtxName(parentClass.getName())) + ": production #";
    System.out.println(
        indent + prefix + lowerizeFirstChar(extractCtxName(ctxClass.getName())));
  }

  @Override
  public Node visit(ParseTree t) {
    if (t == null) return null;
    String temp = indent;
    indent = (indent == null) ? "" : indent + "  ";
    Node result = super.visit(t);
    indent = temp;
    return result;
  }

  @Override
  public Node visitProg(ProgContext c) {
    if (print) printVarAndProdName(c);
    return visit(c.progbody());
  }

  @Override
  public Node visitLetInProg(LetInProgContext c) {
    if (print) printVarAndProdName(c);

    // Visit all classes declarations
    List<ClassNode> classList = new ArrayList<>();
    for (CldecContext cldec : c.cldec()) classList.add((ClassNode) visit(cldec));

    // Visit all declarations
    List<DecNode> decList = new ArrayList<>();
    for (DecContext dec : c.dec()) decList.add((DecNode) visit(dec));

    return new ProgLetInNode(classList, decList, visit(c.exp()));
  }

  @Override
  public Node visitNoDecProg(NoDecProgContext c) {
    if (print) printVarAndProdName(c);
    return new ProgNode(visit(c.exp()));
  }

  @Override
  public Node visitPlusMinus(PlusMinusContext c) {
    if (print) printVarAndProdName(c);
    Node n = null;
    if (c.PLUS() != null) n = new PlusNode(visit(c.exp(0)), visit(c.exp(1)));
    else if (c.MINUS() != null) n = new MinusNode(visit(c.exp(0)), visit(c.exp(1)));
    assert n != null;
    n.setLine(c.PLUS() != null ? c.PLUS().getSymbol().getLine() :
        c.MINUS().getSymbol().getLine());
    return n;
  }

  @Override
  public Node visitTimesDiv(TimesDivContext c) {
    if (print) printVarAndProdName(c);
    Node n = null;
    if (c.TIMES() != null) n = new TimesNode(visit(c.exp(0)), visit(c.exp(1)));
    else if (c.DIV() != null) n = new DivNode(visit(c.exp(0)), visit(c.exp(1)));
    assert n != null;
    n.setLine(c.TIMES() != null ? c.TIMES().getSymbol().getLine() :
        c.DIV().getSymbol().getLine());
    return n;
  }

  @Override
  public Node visitComp(CompContext c) {
    if (print) printVarAndProdName(c);
    Node n = null;
    if (c.EQ() != null) n = new EqualNode(visit(c.exp(0)), visit(c.exp(1)));
    else if (c.GE() != null) n = new GreaterEqualNode(visit(c.exp(0)), visit(c.exp(1)));
    else if (c.LE() != null) n = new LessEqualNode(visit(c.exp(0)), visit(c.exp(1)));
    assert n != null;
    n.setLine(c.EQ() != null ? c.EQ().getSymbol().getLine() :
        c.GE() != null ? c.GE().getSymbol().getLine() :
            c.LE().getSymbol().getLine());
    return n;
  }

  @Override
  public Node visitAndOr(AndOrContext c) {
    if (print) printVarAndProdName(c);
    Node n = null;
    if (c.AND() != null) n = new AndNode(visit(c.exp(0)), visit(c.exp(1)));
    else if (c.OR() != null) n = new OrNode(visit(c.exp(0)), visit(c.exp(1)));
    assert n != null;
    n.setLine(c.AND() != null ? c.AND().getSymbol().getLine() :
        c.OR().getSymbol().getLine());
    return n;
  }

  @Override
  public Node visitNot(NotContext c) {
    if (print) printVarAndProdName(c);
    Node n = new NotNode(visit(c.exp()));
    n.setLine(c.NOT().getSymbol().getLine());
    return n;
  }

  @Override
  public Node visitVardec(VardecContext c) {
    if (print) printVarAndProdName(c);
    Node n = null;
    if (c.ID() != null) { //non-incomplete ST
      n = new VarNode(c.ID().getText(), (TypeNode) visit(c.type()), visit(c.exp()));
      n.setLine(c.VAR().getSymbol().getLine());
    }
    return n;
  }

  @Override
  public Node visitFundec(FundecContext c) {
    if (print) printVarAndProdName(c);
    List<ParNode> parList = new ArrayList<>();
    for (int i = 1; i < c.ID().size(); i++) {
      ParNode p = new ParNode(c.ID(i).getText(), (TypeNode) visit(c.type(i)));
      p.setLine(c.ID(i).getSymbol().getLine());
      parList.add(p);
    }
    List<DecNode> decList = new ArrayList<>();
    for (DecContext dec : c.dec()) decList.add((DecNode) visit(dec));
    Node n = null;
    if (c.ID().size() > 0) { //non-incomplete ST
      n = new FunNode(
          c.ID(0).getText(),
          (TypeNode) visit(c.type(0)),
          parList,
          decList,
          visit(c.exp())
      );
      n.setLine(c.FUN().getSymbol().getLine());
    }
    return n;
  }

  @Override
  public Node visitIntType(IntTypeContext c) {
    if (print) printVarAndProdName(c);
    return new IntTypeNode();
  }

  @Override
  public Node visitBoolType(BoolTypeContext c) {
    if (print) printVarAndProdName(c);
    return new BoolTypeNode();
  }

  @Override
  public Node visitIdType(IdTypeContext c) {
    if (print) printVarAndProdName(c);
    var n = new RefTypeNode(c.ID().getText());
    n.setLine(c.ID().getSymbol().getLine());
    return n;
  }

  @Override
  public Node visitInteger(IntegerContext c) {
    if (print) printVarAndProdName(c);
    int v = Integer.parseInt(c.NUM().getText());
    return new IntNode(c.MINUS() == null ? v : -v);
  }

  @Override
  public Node visitTrue(TrueContext c) {
    if (print) printVarAndProdName(c);
    return new BoolNode(true);
  }

  @Override
  public Node visitFalse(FalseContext c) {
    if (print) printVarAndProdName(c);
    return new BoolNode(false);
  }

  @Override
  public Node visitIf(IfContext c) {
    if (print) printVarAndProdName(c);
    Node ifNode = visit(c.exp(0));
    Node thenNode = visit(c.exp(1));
    Node elseNode = visit(c.exp(2));
    Node n = new IfNode(ifNode, thenNode, elseNode);
    n.setLine(c.IF().getSymbol().getLine());
    return n;
  }

  @Override
  public Node visitPrint(PrintContext c) {
    if (print) printVarAndProdName(c);
    return new PrintNode(visit(c.exp()));
  }

  @Override
  public Node visitPars(ParsContext c) {
    if (print) printVarAndProdName(c);
    return visit(c.exp());
  }

  @Override
  public Node visitId(IdContext c) {
    if (print) printVarAndProdName(c);
    Node n = new IdNode(c.ID().getText());
    n.setLine(c.ID().getSymbol().getLine());
    return n;
  }

  @Override
  public Node visitCall(CallContext c) {
    if (print) printVarAndProdName(c);
    List<Node> argList = new ArrayList<>();
    for (ExpContext arg : c.exp()) argList.add(visit(arg));
    Node n = new CallNode(c.ID().getText(), argList);
    n.setLine(c.ID().getSymbol().getLine());
    return n;
  }

  // Object-Oriented Programming extensions

  @Override
  public Node visitCldec(CldecContext c) {
    if (print) printVarAndProdName(c);

    String id = c.ID(0).getText();
    String superId = null;
    List<FieldNode> fieldsList = new ArrayList<>();
    int declOffset = 1;
    // Check if the class extends another class and,
    // if so, get the name of the superclass and update
    // the offset for the fields declarations
    if (c.EXTENDS() != null) {
      superId = c.ID(declOffset).getText();
      declOffset++;
    }

    // Visit all fields declarations
    for (int i = declOffset; i < c.ID().size(); i++) {
      // The field name is at index i
      String fieldId = c.ID(i).getText();

      // The field type is offset by declOffset
      TypeNode fieldType = (TypeNode) visit(c.type(i - declOffset));

      // Create the FieldNode (defined in AST.java)
      FieldNode fieldNode = new FieldNode(fieldId, fieldType);

      // Set the exact line where the field name is located
      fieldNode.setLine(c.ID(i).getSymbol().getLine());

      // Add to the list
      fieldsList.add(fieldNode);
    }

    List<MethodNode> methodList = new ArrayList<>();
    c.methdec().forEach(methdec ->
        methodList.add((MethodNode) visit(methdec)));

    ClassNode classNode = null;
    if (!c.ID().isEmpty()) { // non-incomplete ST
      classNode = new ClassNode(id, fieldsList, methodList, superId);
      classNode.setLine(c.ID(0).getSymbol().getLine());
    }
    return classNode;
  }

  @Override
  public Node visitMethdec(MethdecContext c) {
    if (print) printVarAndProdName(c);

    // Visit all parameters
    List<ParNode> parList = new ArrayList<>();
    // Start from 1 because the first ID is the method's name
    for (int i = 1; i < c.ID().size(); i++) {
      ParNode p = new ParNode(c.ID(i).getText(), (TypeNode) visit(c.type(i)));
      p.setLine(c.ID(i).getSymbol().getLine());
      parList.add(p);
    }
    // Visit all local declarations
    List<DecNode> decList = new ArrayList<>();
    for (DecContext dec : c.dec()) decList.add((DecNode) visit(dec));

    Node n = null;
    if (!c.ID().isEmpty()) { //non-incomplete ST
      n = new MethodNode(
          c.ID(0).getText(),
          (TypeNode) visit(c.type(0)),
          parList,
          decList,
          visit(c.exp())
      );
      n.setLine(c.FUN().getSymbol().getLine());
    }
    return n;
  }

  @Override
  public Node visitNew(NewContext c) {
    if (print) printVarAndProdName(c);
    String id = c.ID().getText();
    List<Node> argList = new ArrayList<>();
    for (ExpContext arg : c.exp()) argList.add(visit(arg));
    Node newNode = new NewNode(id, argList);
    newNode.setLine(c.ID().getSymbol().getLine());
    return newNode;
  }

  @Override
  public Node visitNull(NullContext c) {
    if (print) printVarAndProdName(c);
    EmptyNode n = new EmptyNode();
    n.setLine(c.NULL().getSymbol().getLine());
    return n;
  }
}
