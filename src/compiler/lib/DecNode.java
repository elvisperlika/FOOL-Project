package compiler.lib;

/**
 * Abstract class for declaration nodes.  This includes function and parameter
 * declarations.
 */
public abstract class DecNode extends Node {

  /**
   * The type of the declaration.
   * For a parameter, this is the declared type.
   * For a function, this is the return type.
   */
  protected TypeNode type;

  public TypeNode getType() {
    return type;
  }

}
