package compiler.lib;

public abstract class Node implements Visitable {

  int line = -1;  // line -1 means unset

  public int getLine() {
    return line;
  }

  public void setLine(int l) {
    line = l;
  }

}

	  