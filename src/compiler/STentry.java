package compiler;

import compiler.lib.BaseASTVisitor;
import compiler.lib.BaseEASTVisitor;
import compiler.lib.TypeNode;
import compiler.lib.Visitable;

public class STentry implements Visitable {
  final int nl;
  final TypeNode type;
  final int offset;

  /**
   * Constructor for STentry.
   * @param n nesting level of the declaration
   * @param t type of the declaration
   * @param o offset of the declaration
   */
  public STentry(int n, TypeNode t, int o) {
    nl = n;
    type = t;
    offset = o;
  }

  @Override
  public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
    return ((BaseEASTVisitor<S, E>) visitor).visitSTentry(this);
  }
}
