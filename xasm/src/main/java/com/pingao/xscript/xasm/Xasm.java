package com.pingao.xscript.xasm;

import com.pingao.xscript.xasm.model.*;
import com.pingao.xscript.xasm.util.ParseUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

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

    public Xasm() {
        instrTable = new ArrayList<InstrLookup>();
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
    }

    private String sourceCodes;
    public String getSourceCodes() {
        return sourceCodes;
    }

    //public void loadSourceCode(String soucePath) throws IOException {
    //    sourceCodes = Files.readAllLines(Paths.get(soucePath));
    //    for (int i = 0; i < sourceCodes.size(); i++) {
    //        sourceCodes.set(i, ParseUtil.stripeComments(sourceCodes.get(i)));
    //    }
    //}

    private List<InstrLookup> instrTable;

    private int addInstrLookup(String mnemonic, int opCode, int opCount) {
        if (instrIndex >= MAX_INSTR_LOOKUP_COUNT) {
            return -1;
        }

        InstrLookup lookup = new InstrLookup(opCount);
        lookup.setMnemonic(mnemonic);
        lookup.setOpCode(opCode);
        lookup.setOpCount(opCount);
        instrTable.add(lookup);

        int returnIndex = instrIndex;
        instrIndex++;
        return returnIndex;
    }

    private void setOpType(int instrIndex, int opIndex, OpTypes opTypes) {
        instrTable.get(instrIndex).getOpTypesList().set(opIndex, opTypes);
    }



    private  void initInstrTable() {
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

        instrIndex = addInstrLookup("SetChar", INSTR_GETCHAR, 3);
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

        instrIndex = addInstrLookup("Exit", INSTR_PAUSE, 1);
        setOpType(instrIndex, 0, new OpTypes(OP_FLAG_TYPE_INT
            | OP_FLAG_TYPE_FLOAT
            | OP_FLAG_TYPE_STRING
            | OP_FLAG_TYPE_MEM_REF
            | OP_FLAG_TYPE_REG));
    }

    private List<FunNode> funTable;
    private List<LabelNode> labelTable;
    private List<SymbolNode> symbolTable;
    private List<String> StringTable;

}
