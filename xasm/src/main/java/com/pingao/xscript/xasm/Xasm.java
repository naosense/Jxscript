package com.pingao.xscript.xasm;

import com.pingao.xscript.xasm.enums.Token;
import com.pingao.xscript.xasm.model.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.pingao.xscript.xasm.enums.Token.*;

/**
 * Created by pingao on 2017/2/3.
 */
public class Xasm {
    public static final int MAX_FILENAME_SIZE = 2048;
    public static final String SOURCE_FILE_EXT = ".XASM";
    public static final String EXEC_FILE_EXT = ".XSE";

    public static final int MAX_SOURCE_CODE_SIZE = 65536;
    public static final int MAX_SOURCE_LINE_SIZE = 4096;

    public static final String XSE_ID_STRING = "XSE0";
    public static final int VERSION_MAJOR = 0;
    public static final int VERSION_MINOR = 4;

    public static final int MAX_LEXEME_SIZE = 256;

    public static final int MAX_IDENT_SIZE = 256;
    public static final int MAX_INSTR_LOOKUP_COUNT = 256;
    public static final int MAX_INSTR_MNEMONIC_SIZE = 16;

    public static final int INSTR_MOV = 0;
    public static final int INSTR_ADD = 1;
    public static final int INSTR_SUB = 2;
    public static final int INSTR_MUL = 3;
    public static final int INSTR_DIV = 4;
    public static final int INSTR_MOD = 5;
    public static final int INSTR_EXP = 6;
    public static final int INSTR_NEG = 7;
    public static final int INSTR_INC = 8;
    public static final int INSTR_DEC = 9;

    public static final int INSTR_AND = 10;
    public static final int INSTR_OR = 11;
    public static final int INSTR_XOR = 12;
    public static final int INSTR_NOT = 13;
    public static final int INSTR_SHL = 14;
    public static final int INSTR_SHR = 15;

    public static final int INSTR_CONCAT = 16;
    public static final int INSTR_GETCHAR = 17;
    public static final int INSTR_SETCHAR = 18;

    public static final int INSTR_JMP = 19;
    public static final int INSTR_JE = 20;
    public static final int INSTR_JNE = 21;
    public static final int INSTR_JG = 22;
    public static final int INSTR_JL = 23;
    public static final int INSTR_JGE = 24;
    public static final int INSTR_JLE = 25;

    public static final int INSTR_PUSH = 26;
    public static final int INSTR_POP = 27;

    public static final int INSTR_CALL = 28;
    public static final int INSTR_RET = 29;
    public static final int INSTR_CALLHOST = 30;

    public static final int INSTR_PAUSE = 31;
    public static final int INSTR_EXIT = 32;

    public static final int OP_FLAG_TYPE_INT = 1;
    public static final int OP_FLAG_TYPE_FLOAT = 2;
    public static final int OP_FLAG_TYPE_STRING = 4;
    public static final int OP_FLAG_TYPE_MEM_REF = 8;
    public static final int OP_FLAG_TYPE_LINE_LABEL = 16;
    public static final int OP_FLAG_TYPE_FUNC_NAME = 32;
    public static final int OP_FLAG_TYPE_HOST_API_CALL = 64;
    public static final int OP_FLAG_TYPE_REG = 128;

    public static final int OP_TYPE_INT = 0;
    public static final int OP_TYPE_FLOAT = 1;
    public static final int OP_TYPE_STRING_INDEX = 2;
    public static final int OP_TYPE_ABS_STACK_INDEX = 3;
    public static final int OP_TYPE_REL_STACK_INDEX = 4;
    public static final int OP_TYPE_INSTR_INDEX = 5;
    public static final int OP_TYPE_FUNC_INDEX = 6;
    public static final int OP_TYPE_HOST_API_CALL_INDEX = 7;
    public static final int OP_TYPE_REG = 8;

    public static final String MAIN_FUNC_NAME = "_MAIN";

    public static final String ERROR_MSSG_INVALID_INPUT = "Invalid input";
    public static final String ERROR_MSSG_LOCAL_SETSTACKSIZE = "SetStackSize can only appear in the global scope";
    public static final String ERROR_MSSG_INVALID_STACK_SIZE = "Invalid stack size";
    public static final String ERROR_MSSG_MULTIPLE_SETSTACKSIZES = "Multiple instances of SetStackSize illegal";
    public static final String ERROR_MSSG_IDENT_EXPECTED = "Identifier expected";
    public static final String ERROR_MSSG_INVALID_ARRAY_SIZE = "Invalid array size";
    public static final String ERROR_MSSG_IDENT_REDEFINITION = "Identifier redefinition";
    public static final String ERROR_MSSG_UNDEFINED_IDENT = "Undefined identifier";
    public static final String ERROR_MSSG_NESTED_FUNC = "Nested functions illegal";
    public static final String ERROR_MSSG_FUNC_REDEFINITION = "Function redefinition";
    public static final String ERROR_MSSG_UNDEFINED_FUNC = "Undefined function";
    public static final String ERROR_MSSG_GLOBAL_PARAM = "Parameters can only appear inside functions";
    public static final String ERROR_MSSG_MAIN_PARAM = "_Main () function cannot accept parameters";
    public static final String ERROR_MSSG_GLOBAL_LINE_LABEL = "Line labels can only appear inside functions";
    public static final String ERROR_MSSG_LINE_LABEL_REDEFINITION = "Line label redefinition";
    public static final String ERROR_MSSG_UNDEFINED_LINE_LABEL = "Undefined line label";
    public static final String ERROR_MSSG_GLOBAL_INSTR = "Instructions can only appear inside functions";
    public static final String ERROR_MSSG_INVALID_INSTR = "Invalid instruction";
    public static final String ERROR_MSSG_INVALID_OP = "Invalid operand";
    public static final String ERROR_MSSG_INVALID_STRING = "Invalid string";
    public static final String ERROR_MSSG_INVALID_ARRAY_NOT_INDEXED = "Arrays must be indexed";
    public static final String ERROR_MSSG_INVALID_ARRAY = "Invalid array";
    public static final String ERROR_MSSG_INVALID_ARRAY_INDEX = "Invalid array index";

    private static int instrIndex;
    private String sourceFile;
    private List<FunNode> funTable;
    private List<LabelNode> labelTable;
    private List<SymbolNode> symbolTable;
    private List<String> stringTable;

    private int stackSize;
    private boolean isMainFuncPresent;
    private int instrStreamSize;
    private boolean isSetStackSizeFound;
    private int globalDataSize;

    private boolean isFuncActive;
    private FunNode currentFunc;
    private int currentFuncIndex;
    private String currentFuncName;
    private int currentParamCount;
    private int currentFuncLocalDataSize;

    public Xasm() {
        instrTable = new ArrayList<InstrLookup>();
        stringTable = new ArrayList<String>();
        funTable = new ArrayList<FunNode>();
        labelTable = new ArrayList<LabelNode>();
        symbolTable = new ArrayList<SymbolNode>();
        printLogo();
        printUsage();
    }

    public void printLogo() {
        System.out.println("XASM");
        System.out.printf("XtremeScript Assembler Version %d.%d\n", Xasm.VERSION_MAJOR, Xasm.VERSION_MINOR);
        System.out.println("Written by Alex Varanese");
        System.out.println();
    }

    public void printUsage() {
        System.out.printf("Usage:\tXASM Source.XASM [Executable.XSE]\n");
        System.out.printf("\n");
        System.out.printf("\t- File extensions are not required.\n");
        System.out.printf("\t- Executable name is optional; source name is used by default.\n");
        System.out.printf("\n");
    }

    private List<InstrLookup> instrTable;

    private int addInstrLookup(String mnemonic, int opCode, int opCount) {
        if (instrTable.size() >= MAX_INSTR_LOOKUP_COUNT) {
            return -1;
        }

        InstrLookup lookup = new InstrLookup(opCount);
        lookup.setMnemonic(mnemonic);
        lookup.setOpCode(opCode);
        lookup.setOpCount(opCount);
        instrTable.add(lookup);
        instrIndex = instrTable.size() - 1;
        return instrIndex;
    }

    private void setOpType(int instrIndex, int opIndex, OpTypes opTypes) {
        instrTable.get(instrIndex).getOpTypesList().add(opTypes);
    }

    private void initInstrTable() {
        int instrIndex;
        instrIndex = addInstrLookup("Mov", INSTR_MOV, 2);
        setOpType(instrIndex, 0, new OpTypes(OP_FLAG_TYPE_MEM_REF | OP_FLAG_TYPE_REG));
        setOpType(instrIndex, 1, new OpTypes(OP_FLAG_TYPE_INT
            | OP_FLAG_TYPE_FLOAT
            | OP_FLAG_TYPE_STRING
            | OP_FLAG_TYPE_MEM_REF
            | OP_FLAG_TYPE_REG));

        instrIndex = addInstrLookup("Add", INSTR_ADD, 2);
        setOpType(instrIndex, 0, new OpTypes(OP_FLAG_TYPE_MEM_REF | OP_FLAG_TYPE_REG));
        setOpType(instrIndex, 1, new OpTypes(OP_FLAG_TYPE_INT
            | OP_FLAG_TYPE_FLOAT
            | OP_FLAG_TYPE_STRING
            | OP_FLAG_TYPE_MEM_REF
            | OP_FLAG_TYPE_REG));

        instrIndex = addInstrLookup("Sub", INSTR_SUB, 2);
        setOpType(instrIndex, 0, new OpTypes(OP_FLAG_TYPE_MEM_REF | OP_FLAG_TYPE_REG));
        setOpType(instrIndex, 1, new OpTypes(OP_FLAG_TYPE_INT
            | OP_FLAG_TYPE_FLOAT
            | OP_FLAG_TYPE_STRING
            | OP_FLAG_TYPE_MEM_REF
            | OP_FLAG_TYPE_REG));

        instrIndex = addInstrLookup("Mul", INSTR_MUL, 2);
        setOpType(instrIndex, 0, new OpTypes(OP_FLAG_TYPE_MEM_REF | OP_FLAG_TYPE_REG));
        setOpType(instrIndex, 1, new OpTypes(OP_FLAG_TYPE_INT
            | OP_FLAG_TYPE_FLOAT
            | OP_FLAG_TYPE_STRING
            | OP_FLAG_TYPE_MEM_REF
            | OP_FLAG_TYPE_REG));

        instrIndex = addInstrLookup("Div", INSTR_DIV, 2);
        setOpType(instrIndex, 0, new OpTypes(OP_FLAG_TYPE_MEM_REF | OP_FLAG_TYPE_REG));
        setOpType(instrIndex, 1, new OpTypes(OP_FLAG_TYPE_INT
            | OP_FLAG_TYPE_FLOAT
            | OP_FLAG_TYPE_STRING
            | OP_FLAG_TYPE_MEM_REF
            | OP_FLAG_TYPE_REG));

        instrIndex = addInstrLookup("Mod", INSTR_MOD, 2);
        setOpType(instrIndex, 0, new OpTypes(OP_FLAG_TYPE_MEM_REF | OP_FLAG_TYPE_REG));
        setOpType(instrIndex, 1, new OpTypes(OP_FLAG_TYPE_INT
            | OP_FLAG_TYPE_FLOAT
            | OP_FLAG_TYPE_STRING
            | OP_FLAG_TYPE_MEM_REF
            | OP_FLAG_TYPE_REG));

        instrIndex = addInstrLookup("Exp", INSTR_EXP, 2);
        setOpType(instrIndex, 0, new OpTypes(OP_FLAG_TYPE_MEM_REF | OP_FLAG_TYPE_REG));
        setOpType(instrIndex, 1, new OpTypes(OP_FLAG_TYPE_INT
            | OP_FLAG_TYPE_FLOAT
            | OP_FLAG_TYPE_STRING
            | OP_FLAG_TYPE_MEM_REF
            | OP_FLAG_TYPE_REG));

        instrIndex = addInstrLookup("Neg", INSTR_NEG, 1);
        setOpType(instrIndex, 0, new OpTypes(OP_FLAG_TYPE_MEM_REF | OP_FLAG_TYPE_REG));

        instrIndex = addInstrLookup("Inc", INSTR_INC, 1);
        setOpType(instrIndex, 0, new OpTypes(OP_FLAG_TYPE_MEM_REF | OP_FLAG_TYPE_REG));

        instrIndex = addInstrLookup("Dec", INSTR_DEC, 1);
        setOpType(instrIndex, 0, new OpTypes(OP_FLAG_TYPE_MEM_REF | OP_FLAG_TYPE_REG));

        instrIndex = addInstrLookup("Dec", INSTR_DEC, 1);
        setOpType(instrIndex, 0, new OpTypes(OP_FLAG_TYPE_MEM_REF | OP_FLAG_TYPE_REG));

        instrIndex = addInstrLookup("And", INSTR_AND, 2);
        setOpType(instrIndex, 0, new OpTypes(OP_FLAG_TYPE_MEM_REF | OP_FLAG_TYPE_REG));
        setOpType(instrIndex, 1, new OpTypes(OP_FLAG_TYPE_INT
            | OP_FLAG_TYPE_FLOAT
            | OP_FLAG_TYPE_STRING
            | OP_FLAG_TYPE_MEM_REF
            | OP_FLAG_TYPE_REG));

        instrIndex = addInstrLookup("Or", INSTR_OR, 2);
        setOpType(instrIndex, 0, new OpTypes(OP_FLAG_TYPE_MEM_REF | OP_FLAG_TYPE_REG));
        setOpType(instrIndex, 1, new OpTypes(OP_FLAG_TYPE_INT
            | OP_FLAG_TYPE_FLOAT
            | OP_FLAG_TYPE_STRING
            | OP_FLAG_TYPE_MEM_REF
            | OP_FLAG_TYPE_REG));

        instrIndex = addInstrLookup("Xor", INSTR_XOR, 2);
        setOpType(instrIndex, 0, new OpTypes(OP_FLAG_TYPE_MEM_REF | OP_FLAG_TYPE_REG));
        setOpType(instrIndex, 1, new OpTypes(OP_FLAG_TYPE_INT
            | OP_FLAG_TYPE_FLOAT
            | OP_FLAG_TYPE_STRING
            | OP_FLAG_TYPE_MEM_REF
            | OP_FLAG_TYPE_REG));

        instrIndex = addInstrLookup("Not", INSTR_NOT, 1);
        setOpType(instrIndex, 0, new OpTypes(OP_FLAG_TYPE_MEM_REF | OP_FLAG_TYPE_REG));

        instrIndex = addInstrLookup("Shl", INSTR_SHL, 2);
        setOpType(instrIndex, 0, new OpTypes(OP_FLAG_TYPE_MEM_REF | OP_FLAG_TYPE_REG));
        setOpType(instrIndex, 1, new OpTypes(OP_FLAG_TYPE_INT
            | OP_FLAG_TYPE_FLOAT
            | OP_FLAG_TYPE_STRING
            | OP_FLAG_TYPE_MEM_REF
            | OP_FLAG_TYPE_REG));

        instrIndex = addInstrLookup("Shr", INSTR_SHR, 2);
        setOpType(instrIndex, 0, new OpTypes(OP_FLAG_TYPE_MEM_REF | OP_FLAG_TYPE_REG));
        setOpType(instrIndex, 1, new OpTypes(OP_FLAG_TYPE_INT
            | OP_FLAG_TYPE_FLOAT
            | OP_FLAG_TYPE_STRING
            | OP_FLAG_TYPE_MEM_REF
            | OP_FLAG_TYPE_REG));

        instrIndex = addInstrLookup("Concat", INSTR_CONCAT, 2);
        setOpType(instrIndex, 0, new OpTypes(OP_FLAG_TYPE_MEM_REF | OP_FLAG_TYPE_REG));
        setOpType(instrIndex, 1, new OpTypes(OP_FLAG_TYPE_MEM_REF | OP_FLAG_TYPE_REG | OP_FLAG_TYPE_STRING));

        instrIndex = addInstrLookup("GetChar", INSTR_GETCHAR, 3);
        setOpType(instrIndex, 0, new OpTypes(OP_FLAG_TYPE_MEM_REF | OP_FLAG_TYPE_REG));
        setOpType(instrIndex, 1, new OpTypes(OP_FLAG_TYPE_MEM_REF | OP_FLAG_TYPE_REG | OP_FLAG_TYPE_STRING));
        setOpType(instrIndex, 2, new OpTypes(OP_FLAG_TYPE_MEM_REF | OP_FLAG_TYPE_REG | OP_FLAG_TYPE_INT));

        instrIndex = addInstrLookup("SetChar", INSTR_SETCHAR, 3);
        setOpType(instrIndex, 0, new OpTypes(OP_FLAG_TYPE_MEM_REF | OP_FLAG_TYPE_REG));
        setOpType(instrIndex, 1, new OpTypes(OP_FLAG_TYPE_MEM_REF | OP_FLAG_TYPE_REG | OP_FLAG_TYPE_INT));
        setOpType(instrIndex, 2, new OpTypes(OP_FLAG_TYPE_MEM_REF | OP_FLAG_TYPE_REG | OP_FLAG_TYPE_STRING));

        instrIndex = addInstrLookup("Jmp", INSTR_JMP, 1);
        setOpType(instrIndex, 0, new OpTypes(OP_FLAG_TYPE_LINE_LABEL));

        instrIndex = addInstrLookup("Je", INSTR_JE, 3);
        setOpType(instrIndex, 0, new OpTypes(OP_FLAG_TYPE_INT
            | OP_FLAG_TYPE_FLOAT
            | OP_FLAG_TYPE_STRING
            | OP_FLAG_TYPE_MEM_REF
            | OP_FLAG_TYPE_REG));
        setOpType(instrIndex, 1, new OpTypes(OP_FLAG_TYPE_INT
            | OP_FLAG_TYPE_FLOAT
            | OP_FLAG_TYPE_STRING
            | OP_FLAG_TYPE_MEM_REF
            | OP_FLAG_TYPE_REG));
        setOpType(instrIndex, 2, new OpTypes(OP_FLAG_TYPE_LINE_LABEL));

        instrIndex = addInstrLookup("Jne", INSTR_JNE, 3);
        setOpType(instrIndex, 0, new OpTypes(OP_FLAG_TYPE_INT
            | OP_FLAG_TYPE_FLOAT
            | OP_FLAG_TYPE_STRING
            | OP_FLAG_TYPE_MEM_REF
            | OP_FLAG_TYPE_REG));
        setOpType(instrIndex, 1, new OpTypes(OP_FLAG_TYPE_INT
            | OP_FLAG_TYPE_FLOAT
            | OP_FLAG_TYPE_STRING
            | OP_FLAG_TYPE_MEM_REF
            | OP_FLAG_TYPE_REG));
        setOpType(instrIndex, 2, new OpTypes(OP_FLAG_TYPE_LINE_LABEL));

        instrIndex = addInstrLookup("Jg", INSTR_JG, 3);
        setOpType(instrIndex, 0, new OpTypes(OP_FLAG_TYPE_INT
            | OP_FLAG_TYPE_FLOAT
            | OP_FLAG_TYPE_STRING
            | OP_FLAG_TYPE_MEM_REF
            | OP_FLAG_TYPE_REG));
        setOpType(instrIndex, 1, new OpTypes(OP_FLAG_TYPE_INT
            | OP_FLAG_TYPE_FLOAT
            | OP_FLAG_TYPE_STRING
            | OP_FLAG_TYPE_MEM_REF
            | OP_FLAG_TYPE_REG));
        setOpType(instrIndex, 2, new OpTypes(OP_FLAG_TYPE_LINE_LABEL));

        instrIndex = addInstrLookup("Jl", INSTR_JL, 3);
        setOpType(instrIndex, 0, new OpTypes(OP_FLAG_TYPE_INT
            | OP_FLAG_TYPE_FLOAT
            | OP_FLAG_TYPE_STRING
            | OP_FLAG_TYPE_MEM_REF
            | OP_FLAG_TYPE_REG));
        setOpType(instrIndex, 1, new OpTypes(OP_FLAG_TYPE_INT
            | OP_FLAG_TYPE_FLOAT
            | OP_FLAG_TYPE_STRING
            | OP_FLAG_TYPE_MEM_REF
            | OP_FLAG_TYPE_REG));
        setOpType(instrIndex, 2, new OpTypes(OP_FLAG_TYPE_LINE_LABEL));

        instrIndex = addInstrLookup("Jge", INSTR_JGE, 3);
        setOpType(instrIndex, 0, new OpTypes(OP_FLAG_TYPE_INT
            | OP_FLAG_TYPE_FLOAT
            | OP_FLAG_TYPE_STRING
            | OP_FLAG_TYPE_MEM_REF
            | OP_FLAG_TYPE_REG));
        setOpType(instrIndex, 1, new OpTypes(OP_FLAG_TYPE_INT
            | OP_FLAG_TYPE_FLOAT
            | OP_FLAG_TYPE_STRING
            | OP_FLAG_TYPE_MEM_REF
            | OP_FLAG_TYPE_REG));
        setOpType(instrIndex, 2, new OpTypes(OP_FLAG_TYPE_LINE_LABEL));

        instrIndex = addInstrLookup("Jle", INSTR_JLE, 3);
        setOpType(instrIndex, 0, new OpTypes(OP_FLAG_TYPE_INT
            | OP_FLAG_TYPE_FLOAT
            | OP_FLAG_TYPE_STRING
            | OP_FLAG_TYPE_MEM_REF
            | OP_FLAG_TYPE_REG));
        setOpType(instrIndex, 1, new OpTypes(OP_FLAG_TYPE_INT
            | OP_FLAG_TYPE_FLOAT
            | OP_FLAG_TYPE_STRING
            | OP_FLAG_TYPE_MEM_REF
            | OP_FLAG_TYPE_REG));
        setOpType(instrIndex, 2, new OpTypes(OP_FLAG_TYPE_LINE_LABEL));

        instrIndex = addInstrLookup("Push", INSTR_PUSH, 1);
        setOpType(instrIndex, 0, new OpTypes(OP_FLAG_TYPE_INT
            | OP_FLAG_TYPE_FLOAT
            | OP_FLAG_TYPE_STRING
            | OP_FLAG_TYPE_MEM_REF
            | OP_FLAG_TYPE_REG));

        instrIndex = addInstrLookup("Pop", INSTR_POP, 1);
        setOpType(instrIndex, 0, new OpTypes(OP_FLAG_TYPE_MEM_REF | OP_FLAG_TYPE_REG));

        instrIndex = addInstrLookup("Call", INSTR_CALL, 1);
        setOpType(instrIndex, 0, new OpTypes(OP_FLAG_TYPE_FUNC_NAME));

        instrIndex = addInstrLookup("Ret", INSTR_RET, 0);

        instrIndex = addInstrLookup("CallHost", INSTR_CALLHOST, 1);
        setOpType(instrIndex, 0, new OpTypes(OP_FLAG_TYPE_HOST_API_CALL));

        instrIndex = addInstrLookup("Pause", INSTR_PAUSE, 1);
        setOpType(instrIndex, 0, new OpTypes(OP_FLAG_TYPE_INT
            | OP_FLAG_TYPE_FLOAT
            | OP_FLAG_TYPE_STRING
            | OP_FLAG_TYPE_MEM_REF
            | OP_FLAG_TYPE_REG));

        instrIndex = addInstrLookup("Exit", INSTR_EXIT, 1);
        setOpType(instrIndex, 0, new OpTypes(OP_FLAG_TYPE_INT
            | OP_FLAG_TYPE_FLOAT
            | OP_FLAG_TYPE_STRING
            | OP_FLAG_TYPE_MEM_REF
            | OP_FLAG_TYPE_REG));
    }

    public static int getInstrByMnemonic(String mnemonic, List<InstrLookup> instrTable) {
        for (int i = 0; i < instrTable.size(); i++) {
            InstrLookup l = instrTable.get(i);
            if (mnemonic.equalsIgnoreCase(l.getMnemonic())) {
                return i;
            }
        }
        return -1;
    }

    public int addString(List<String> stringTable, String string) {
        int index = stringTable.indexOf(string);
        if (index >= 0) {
            return index;
        }
        stringTable.add(string);
        return stringTable.size() - 1;
    }

    public int addFunc(String funcName, int entryPoint) {
        if (getFunByName(funcName) != null) {
            return -1;
        }
        FunNode funNode = new FunNode();
        funNode.setName(funcName);
        funNode.setEntryPoint(entryPoint);
        funTable.add(funNode);
        funNode.setIndex(funTable.size() - 1);
        return funNode.getIndex();
    }

    public int addSymbol(String symbol, int size, int stackIndex, int funcIndex) {
        if (getSymbolByIdent(symbol, funcIndex) != null) {
            return -1;
        }
        SymbolNode symbolNode = new SymbolNode();
        symbolNode.setIdentifier(symbol);
        symbolNode.setSize(size);
        symbolNode.setStackIndex(stackIndex);
        symbolNode.setFunIndex(funcIndex);
        symbolTable.add(symbolNode);
        return symbolTable.size() - 1;
    }

    public int addLabel(String ident, int targetIndex, int funcIndex) {
        if (getLabelByIdent(ident, funcIndex) != null) {
            return -1;
        }
        LabelNode labelNode = new LabelNode();
        labelNode.setIdentifier(ident);
        labelNode.setTargetIndex(targetIndex);
        labelNode.setFunIndex(funcIndex);
        labelTable.add(labelNode);
        labelNode.setIndex(labelTable.size() - 1);
        return labelNode.getIndex();
    }

    public LabelNode getLabelByIdent(String ident, int funcIndex) {
        for (LabelNode l : labelTable) {
            if (ident.equals(l.getIdentifier()) && funcIndex == l.getFunIndex()) {
                return l;
            }
        }
        return null;
    }

    public SymbolNode getSymbolByIdent(String ident, int funcIndex) {
        for (SymbolNode s : symbolTable) {
            if (ident.equals(s.getIdentifier()) && funcIndex == s.getFunIndex()) {
                return s;
            }
        }
        return null;
    }

    public FunNode getFunByName(String name) {
        for (FunNode f : funTable) {
            if (name.equals(f.getName())) {
                return f;
            }
        }
        return null;
    }

    public void assmblSourceFile() {
        initInstrTable();
        Lex2 lex2 = new Lex2(instrTable, sourceFile);
        lex2.resetLexer();

        while (true) {
            if (lex2.getNextToken() == Token.END_OF_TOKEN_STREAM) {
                break;
            }

            switch (lex2.getCurrentToken()) {
                case TOKEN_TYPE_SETSTACKSIZE:
                    // 只能是全局的
                    if (isFuncActive) {
                        lex2.exitOnCodeError(ERROR_MSSG_LOCAL_SETSTACKSIZE);
                    }

                    // 只能出现一次
                    if (isSetStackSizeFound) {
                        lex2.exitOnCodeError(ERROR_MSSG_MULTIPLE_SETSTACKSIZES);
                    }

                    if (lex2.getNextToken() != Token.TOKEN_TYPE_INT) {
                        lex2.exitOnCodeError(ERROR_MSSG_INVALID_STACK_SIZE);
                    }

                    stackSize = Integer.parseInt(lex2.getCurrentLexeme());
                    isSetStackSizeFound = true;
                    break;
                case TOKEN_TYPE_VAR:
                    if (lex2.getNextToken() != Token.TOKEN_TYPE_IDENT) {
                        lex2.exitOnCodeError(ERROR_MSSG_IDENT_EXPECTED);
                    }

                    String ident = lex2.getCurrentLexeme();
                    // 默认非数组
                    int iSize = 1;

                    // 偷看一眼是不是数组
                    if (lex2.lookAheadChar() == '[') {
                        if (lex2.getNextToken() != TOKEN_TYPE_OPEN_BRACKET) {
                            lex2.exitOnCharExpectError('[');
                        }

                        if (lex2.getNextToken() != TOKEN_TYPE_INT) {
                            lex2.exitOnCodeError(ERROR_MSSG_INVALID_ARRAY_SIZE);
                        }

                        iSize = Integer.parseInt(lex2.getCurrentLexeme());

                        if (iSize <= 0) {
                            lex2.exitOnCodeError(ERROR_MSSG_INVALID_ARRAY_SIZE);
                        }

                        if (lex2.getNextToken() != TOKEN_TYPE_CLOSE_BRACKET) {
                            lex2.exitOnCharExpectError(']');
                        }
                    }

                    int iStackIndex;
                    // 局部变量
                    if (isFuncActive) {
                        iStackIndex = -(currentFuncLocalDataSize + 2);
                    } else {
                        iStackIndex = globalDataSize;
                    }

                    if (addSymbol(ident, iSize, iStackIndex, currentFuncIndex) == -1) {
                        lex2.exitOnCodeError(ERROR_MSSG_IDENT_REDEFINITION);
                    }

                    if (isFuncActive) {
                        currentFuncLocalDataSize += iSize;
                    } else {
                        globalDataSize += iSize;
                    }

                    break;
                case TOKEN_TYPE_FUNC:
                    // 函数不能嵌套
                    if (isFuncActive) {
                        lex2.exitOnCodeError(ERROR_MSSG_NESTED_FUNC);
                    }
            }
        }
    }

    public static void main(String[] args) throws IOException {

        //xasm.initInstrTable();
        //for (InstrLookup l : xasm.instrTable) {
        //    System.out.println(l);
        //}
        //System.out.println(Xasm.instrIndex);
    }
}
