package svm;

public class ExecuteVM {

  public static final int CODESIZE = 10000;
  public static final int MEMSIZE = 10000;

  private int[] code;
  private int[] memory = new int[MEMSIZE];

  private int instructionPointer = 0;
  private int stackPointer = MEMSIZE;

  private int heapPointer = 0;
  private int fp = MEMSIZE;
  private int ra;
  private int tm;

  public ExecuteVM(int[] code) {
    this.code = code;
  }

  public void cpu() {
    while (true) {
      int bytecode = code[instructionPointer++]; // fetch
      int v1, v2;
      int address;
      switch (bytecode) {
        case SVMParser.PUSH:
          push(code[instructionPointer++]);
          break;
        case SVMParser.POP:
          pop();
          break;
        case SVMParser.ADD:
          v1 = pop();
          v2 = pop();
          push(v2 + v1);
          break;
        case SVMParser.MULT:
          v1 = pop();
          v2 = pop();
          push(v2 * v1);
          break;
        case SVMParser.DIV:
          v1 = pop();
          v2 = pop();
          push(v2 / v1);
          break;
        case SVMParser.SUB:
          v1 = pop();
          v2 = pop();
          push(v2 - v1);
          break;
        case SVMParser.STOREW: //
          address = pop();
          memory[address] = pop();
          break;
        case SVMParser.LOADW: //
          push(memory[pop()]);
          break;
        case SVMParser.BRANCH:
          address = code[instructionPointer];
          instructionPointer = address;
          break;
        case SVMParser.BRANCHEQ:
          address = code[instructionPointer++];
          v1 = pop();
          v2 = pop();
          if (v2 == v1) instructionPointer = address;
          break;
        case SVMParser.BRANCHLESSEQ:
          address = code[instructionPointer++];
          v1 = pop();
          v2 = pop();
          if (v2 <= v1) instructionPointer = address;
          break;
        case SVMParser.JS: //
          address = pop();
          ra = instructionPointer;
          instructionPointer = address;
          break;
        case SVMParser.STORERA: //
          ra = pop();
          break;
        case SVMParser.LOADRA: //
          push(ra);
          break;
        case SVMParser.STORETM:
          tm = pop();
          break;
        case SVMParser.LOADTM:
          push(tm);
          break;
        case SVMParser.LOADFP: //
          push(fp);
          break;
        case SVMParser.STOREFP: //
          fp = pop();
          break;
        case SVMParser.COPYFP: //
          fp = stackPointer;
          break;
        case SVMParser.STOREHP: //
          heapPointer = pop();
          break;
        case SVMParser.LOADHP: //
          push(heapPointer);
          break;
        case SVMParser.PRINT:
          System.out.println((stackPointer < MEMSIZE) ? memory[stackPointer] : "Empty stack!");
          break;
        case SVMParser.HALT:
          return;
      }
    }
  }

  private int pop() {
    return memory[stackPointer++];
  }

  private void push(int v) {
    memory[--stackPointer] = v;
  }

}