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

    private Lex2 lex2;
    private static int instrIndex;
    private String sourceFile;
    private List<FunNode> funTable;
    private List<LabelNode> labelTable;
    private List<SymbolNode> symbolTable;
    private List<String> stringTable;
    private List<String> hostApiCallTable;

    private int stackSize;
    private boolean isMainFuncPresent;
    private int mainFuncIndex;
    private List<Instr> instrStream;
    private int instrStreamSize;
    private InstrLookup currentInstr;
    private boolean isSetStackSizeFound;
    private int globalDataSize;

    private boolean isFuncActive;
    private FunNode currentFunc;
    private int currentFuncIndex;
    private String currentFuncName;
    private int currentFuncParamCount;
    private int currentFuncLocalDataSize;

    public Xasm(String sourceFile) {
        this.sourceFile = sourceFile;
        instrTable = new ArrayList<InstrLookup>();
        stringTable = new ArrayList<String>();
        hostApiCallTable = new ArrayList<String>();
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

    public static InstrLookup getInstrByMnemonic(String mnemonic, List<InstrLookup> instrTable) {
        for (int i = 0; i < instrTable.size(); i++) {
            InstrLookup l = instrTable.get(i);
            if (mnemonic.equalsIgnoreCase(l.getMnemonic())) {
                return l;
            }
        }
        return null;
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

    public int getStackIndexByIdent(String ident, int funcIndex) {
        return getSymbolByIdent(ident, funcIndex).getStackIndex();
    }

    public int getSizeByIdent(String ident, int funcIndex) {
        return getSymbolByIdent(ident, funcIndex).getSize();
    }

    public FunNode getFunByName(String name) {
        for (FunNode f : funTable) {
            if (name.equals(f.getName())) {
                return f;
            }
        }
        return null;
    }

    private void setFuncInfo(String funcName, int paramCount, int localDataSize) {
        FunNode funNode = getFunByName(funcName);
        funNode.setParamCount(paramCount);
        funNode.setLocalDataSize(localDataSize);
    }

    public void assmblSourceFile() {
        initInstrTable();
        lex2 = new Lex2(instrTable, sourceFile);
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

                    if (lex2.getNextToken() != TOKEN_TYPE_IDENT) {
                        lex2.exitOnCodeError(ERROR_MSSG_IDENT_EXPECTED);
                    }

                    String funcName = lex2.getCurrentLexeme();
                    int iEntryPoint = instrStreamSize;

                    int iFuncIndex = addFunc(funcName, iEntryPoint);
                    if (iFuncIndex == -1) {
                        lex2.exitOnCodeError(ERROR_MSSG_FUNC_REDEFINITION);
                    }

                    if (MAIN_FUNC_NAME.equals(funcName)) {
                        isMainFuncPresent = true;
                        mainFuncIndex = iFuncIndex;
                    }

                    isFuncActive = true;
                    currentFuncName = funcName;
                    currentFuncIndex = iFuncIndex;
                    currentFuncParamCount = 0;
                    currentFuncLocalDataSize = 0;

                    while (lex2.getNextToken() == TOKEN_TYPE_NEWLINE) ;

                    if (lex2.getCurrentToken() != TOKEN_TYPE_OPEN_BRACE) {
                        lex2.exitOnCharExpectError('{');
                    }

                    instrStreamSize++;
                    break;

                case TOKEN_TYPE_CLOSE_BRACE:
                    // 必须在函数里
                    if (!isFuncActive) {
                        lex2.exitOnCharExpectError('}');
                    }

                    setFuncInfo(currentFuncName, currentFuncParamCount, currentFuncLocalDataSize);

                    isFuncActive = false;
                    break;

                case TOKEN_TYPE_PARAM:
                    // 必须在函数里
                    if (!isFuncActive) {
                        lex2.exitOnCodeError(ERROR_MSSG_GLOBAL_PARAM);
                    }

                    // 主函数没有参数
                    if (MAIN_FUNC_NAME.equals(currentFuncName)) {
                        lex2.exitOnCodeError(ERROR_MSSG_MAIN_PARAM);
                    }

                    if (lex2.getNextToken() != TOKEN_TYPE_IDENT) {
                        lex2.exitOnCodeError(ERROR_MSSG_IDENT_EXPECTED);
                    }

                    currentFuncParamCount++;
                    break;

                case TOKEN_TYPE_INSTR:
                    // 指令必须在函数中
                    if (!isFuncActive) {
                        lex2.exitOnCodeError(ERROR_MSSG_GLOBAL_INSTR);
                    }

                    instrStreamSize++;
                    break;

                // line label
                case TOKEN_TYPE_IDENT:
                    if (lex2.lookAheadChar() != ':') {
                        lex2.exitOnCodeError(ERROR_MSSG_INVALID_INSTR);
                    }

                    // label必须在函数中
                    if (!isFuncActive) {
                        lex2.exitOnCodeError(ERROR_MSSG_GLOBAL_LINE_LABEL);
                    }

                    String label = lex2.getCurrentLexeme();

                    int targetIndex = instrStreamSize - 1;
                    int funcIndex = currentFuncIndex;

                    if (addLabel(label, targetIndex, funcIndex) == -1) {
                        lex2.exitOnCodeError(ERROR_MSSG_LINE_LABEL_REDEFINITION);
                    }

                    break;

                default:
                    if (lex2.getCurrentToken() != TOKEN_TYPE_NEWLINE) {
                        lex2.exitOnCodeError(ERROR_MSSG_INVALID_INPUT);
                    }
            }

            // 这里有点不懂
            if (!lex2.skipToNextLine()) {
                break;
            }
        }

        // 第二次遍历
        instrIndex = 0;

        instrStream = new ArrayList<Instr>(instrStreamSize);

        lex2.resetLexer();

        while (true) {
            if (lex2.getNextToken() == END_OF_TOKEN_STREAM) {
                break;
            }

            switch (lex2.getNextToken()) {
                case TOKEN_TYPE_FUNC:
                    // 跳到函数名
                    lex2.getNextToken();

                    currentFunc = getFunByName(lex2.getCurrentLexeme());

                    isFuncActive = true;

                    currentFuncParamCount = 0;

                    currentFuncIndex = currentFunc.getIndex();

                    // 跳过换行
                    while (lex2.getNextToken() == TOKEN_TYPE_NEWLINE) ;

                    break;

                case TOKEN_TYPE_CLOSE_BRACE:
                    isFuncActive = false;

                    // 如果刚刚结束的是主函数，添加exit指令
                    if (currentFunc != null && MAIN_FUNC_NAME.equals(currentFunc.getName())) {
                        Instr instr = new Instr();
                        instr.setOpCode(INSTR_EXIT);
                        instr.setOpCount(1);
                        List<Op> ops = new ArrayList<Op>();
                        ops.add(Op.builder().type(OP_TYPE_INT).intLiteral(0).build());
                        instr.setOpList(ops);
                        instrStream.add(instr);
                    } else {
                        Instr instr = new Instr();
                        instr.setOpCode(INSTR_RET);
                        instr.setOpCount(0);
                        instrStream.add(instr);
                    }

                    instrIndex++;
                    break;

                case TOKEN_TYPE_PARAM:
                    if (lex2.getNextToken() != TOKEN_TYPE_IDENT) {
                        lex2.exitOnCodeError(ERROR_MSSG_IDENT_EXPECTED);
                    }

                    String ident = lex2.getCurrentLexeme();

                    // TODO 为什么这么计算，2和1都是什么
                    int stackIndex = -(currentFunc.getLocalDataSize() + 2 + (currentFuncParamCount + 1));

                    if (addSymbol(ident, 1, stackIndex, currentFuncIndex) == -1) {
                        lex2.exitOnCodeError(ERROR_MSSG_IDENT_REDEFINITION);
                    }

                    currentFuncParamCount++;

                    break;

                case TOKEN_TYPE_INSTR:
                    currentInstr = getInstrByMnemonic(lex2.getCurrentLexeme(), instrTable);
                    Instr instr = new Instr();
                    instr.setOpCode(currentInstr.getOpCode());
                    instr.setOpCount(currentInstr.getOpCount());
                    // TODO 是新建还是修改？
                    instrStream.add(instr);

                    List<Op> opList = new ArrayList<Op>(currentInstr.getOpCount());

                    for (int i = 0; i < currentInstr.getOpCount(); i++) {
                        OpTypes curType = currentInstr.getOpTypesList().get(i);

                        Token initToken = lex2.getNextToken();

                        switch (initToken) {
                            case TOKEN_TYPE_INT:
                                if ((curType.getTypes() & OP_FLAG_TYPE_INT) > 0) {
                                    opList.add(Op.builder().type(OP_TYPE_INT).intLiteral(Integer.parseInt(lex2.getCurrentLexeme())).build());
                                } else {
                                    lex2.exitOnCodeError(ERROR_MSSG_INVALID_OP);
                                }
                                break;

                            case TOKEN_TYPE_FLOAT:
                                if ((curType.getTypes() & OP_FLAG_TYPE_FLOAT) > 0) {
                                    opList.add(Op.builder().type(OP_TYPE_FLOAT).floatLiteral(Float.parseFloat(lex2.getCurrentLexeme())).build());
                                } else {
                                    lex2.exitOnCodeError(ERROR_MSSG_INVALID_OP);
                                }
                                break;

                            case TOKEN_TYPE_QUOTE:
                                if ((curType.getTypes() & OP_FLAG_TYPE_STRING) > 0) {
                                    lex2.getNextToken();

                                    switch (lex2.getCurrentToken()) {
                                        // 空串
                                        case TOKEN_TYPE_QUOTE:
                                            opList.add(Op.builder()
                                                .type(OP_TYPE_INT)
                                                .intLiteral(0)
                                                .build());
                                            break;

                                        case TOKEN_TYPE_STRING:
                                            String str = lex2.getCurrentLexeme();
                                            int strIndex = addString(stringTable, str);
                                            if (lex2.getNextToken() != TOKEN_TYPE_QUOTE) {
                                                lex2.exitOnCharExpectError('"');
                                            }

                                            opList.add(Op.builder().type(OP_TYPE_STRING_INDEX).stringTableIndex(strIndex).build());

                                            break;

                                        default:
                                            lex2.exitOnCodeError(ERROR_MSSG_INVALID_STRING);
                                    }
                                } else {
                                    lex2.exitOnCodeError(ERROR_MSSG_INVALID_OP);
                                }

                                break;

                            case TOKEN_TYPE_REG_RETVAL:
                                if ((curType.getTypes() & OP_TYPE_REG) > 0) {
                                    opList.add(Op.builder().type(OP_TYPE_REG).regCode(0).build());
                                } else {
                                    lex2.exitOnCodeError(ERROR_MSSG_INVALID_OP);
                                }
                                break;

                            case TOKEN_TYPE_IDENT:
                                if ((curType.getTypes() & OP_FLAG_TYPE_MEM_REF) > 0) {

                                    String strIdent = lex2.getCurrentLexeme();

                                    if (getSymbolByIdent(strIdent, currentFuncIndex) == null) {
                                        lex2.exitOnCodeError(ERROR_MSSG_UNDEFINED_IDENT);
                                    }

                                    int baseIndex = getStackIndexByIdent(strIdent, currentFuncIndex);

                                    // 不是数组
                                    if (lex2.lookAheadChar() != '[') {

                                        // TODO 不懂
                                        if (getSizeByIdent(strIdent, currentFuncIndex) > 1) {
                                            lex2.exitOnCodeError(ERROR_MSSG_INVALID_ARRAY_NOT_INDEXED);
                                        }

                                        opList.add(Op.builder().type(OP_TYPE_ABS_STACK_INDEX).intLiteral(baseIndex).build());
                                    } else {
                                        // 是一个数组
                                        if (getSizeByIdent(strIdent, currentFuncIndex) == 1) {
                                            lex2.exitOnCodeError(ERROR_MSSG_INVALID_ARRAY);
                                        }

                                        if (lex2.getNextToken() != TOKEN_TYPE_OPEN_BRACKET) {
                                            lex2.exitOnCharExpectError('[');
                                        }

                                        // 下一个必须是个整数或者一个指示符
                                        Token indexToken = lex2.getNextToken();

                                        if (indexToken == TOKEN_TYPE_INT) {
                                            int offsetIndex = Integer.parseInt(lex2.getCurrentLexeme());
                                            opList.add(Op.builder().type(OP_TYPE_ABS_STACK_INDEX).stackIndex(baseIndex + offsetIndex).build());
                                        } else if (indexToken == TOKEN_TYPE_IDENT) {
                                            String strIndexIdent = lex2.getCurrentLexeme();

                                            if (getSymbolByIdent(strIndexIdent, currentFuncIndex) == null) {
                                                lex2.exitOnCodeError(ERROR_MSSG_UNDEFINED_IDENT);
                                            }

                                            if (getSizeByIdent(strIndexIdent, currentFuncIndex) > 1) {

                                                lex2.exitOnCodeError(ERROR_MSSG_INVALID_ARRAY_INDEX);
                                            }

                                            int offsetIndex = getStackIndexByIdent(strIndexIdent, currentFuncIndex);

                                            opList.add(Op.builder().type(OP_TYPE_REL_STACK_INDEX).stackIndex(baseIndex).offsetIndex(offsetIndex).build());
                                        } else {
                                            lex2.exitOnCodeError(ERROR_MSSG_INVALID_ARRAY_INDEX);
                                        }

                                        if (lex2.getNextToken() != TOKEN_TYPE_CLOSE_BRACKET) {
                                            lex2.exitOnCharExpectError(']');
                                        }
                                    }

                                    // parsing a line label
                                    if ((curType.getTypes() & OP_FLAG_TYPE_LINE_LABEL) > 0) {
                                        String strLabelIdent = lex2.getCurrentLexeme();

                                        LabelNode labelNode = getLabelByIdent(strLabelIdent, currentFuncIndex);

                                        if (labelNode == null) {
                                            lex2.exitOnCodeError(ERROR_MSSG_UNDEFINED_LINE_LABEL);
                                        }

                                        opList.add(Op.builder().type(OP_TYPE_INSTR_INDEX).instrIndex(labelNode.getTargetIndex()).build());
                                    }

                                    // parsing a function
                                    if ((curType.getTypes() & OP_FLAG_TYPE_HOST_API_CALL) > 0) {
                                        String hostApiCall = lex2.getCurrentLexeme();

                                        int index = addString(hostApiCallTable, hostApiCall);

                                        opList.add(Op.builder().type(OP_TYPE_HOST_API_CALL_INDEX).hostAPICallIndex(index).build());
                                    }
                                }
                                break;

                            default:
                                lex2.exitOnCodeError(ERROR_MSSG_INVALID_OP);
                                break;
                        }

                        // 多个变量必须以逗号分隔，除非是最后一个参数
                        if (i < currentInstr.getOpCount() - 1) {
                            if (lex2.getNextToken() != TOKEN_TYPE_COMMA) {
                                lex2.exitOnCharExpectError(',');
                            }
                        }
                    }

                    // 确保后面没有其他东西
                    if (lex2.getNextToken() != TOKEN_TYPE_NEWLINE) {
                        lex2.exitOnCodeError(ERROR_MSSG_INVALID_INPUT);
                    }

                    instrStream.get(instrIndex).setOpList(opList);

                    instrIndex++;

                    break;
            }

            // to next line
            if (!lex2.skipToNextLine()) {
                break;
            }
        }
    }

    public void printAssmblStats() {
        int varCount = 0;
        int arrayCount = 0;
        int globalCount = 0;

        for (SymbolNode s : symbolTable) {
            if (s.getSize() > 1) {
                arrayCount++;
            } else {
                varCount++;
            }

            if (s.getStackIndex() >= 0) {
                globalCount++;
            }
        }

        System.out.printf("\n");
        System.out.printf("%s created successfully!\n\n", sourceFile);
        System.out.printf("Source Lines Processed: %d\n", lex2.getCodes().size());
        System.out.printf("            Stack Size: ");
        if (stackSize > 0) {
            System.out.printf("%d", stackSize);
        } else {
            System.out.printf("Default");
        }

        System.out.printf("\n");
        System.out.printf("Instructions Assembled: %d\n", instrStreamSize);
        System.out.printf("             Variables: %d\n", varCount);
        System.out.printf("                Arrays: %d\n", arrayCount);
        System.out.printf("               Globals: %d\n", globalCount);
        System.out.printf("       String Literals: %d\n", stringTable.size());
        System.out.printf("                Labels: %d\n", labelTable.size());
        System.out.printf("        Host API Calls: %d\n", hostApiCallTable.size());
        System.out.printf("             Functions: %d\n", funTable.size());

        System.out.printf("      _Main () Present: ");


        if (isMainFuncPresent) {
            System.out.printf("Yes (Index %d)\n", mainFuncIndex);
        } else {
            System.out.printf("No\n");
        }
    }

    public static void main(String[] args) throws IOException {
        Xasm xasm = new Xasm("C:\\Users\\pingao.liu\\Desktop\\Programs\\Chapter 9\\XASM 0.4\\Source\\test_1.xasm");
        xasm.assmblSourceFile();
        xasm.printAssmblStats();
    }
}
