package com.pingao.xscript.xasm.model;

import com.pingao.xscript.xasm.Xasm;
import com.pingao.xscript.xasm.enums.Token;
import com.pingao.xscript.xasm.util.ParseUtil;
import lombok.AccessLevel;
import lombok.Getter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by pingao on 2017/2/7.
 */
@Getter
public class Lex2 {
    private int index0;
    private int index1;
    private List<String> codes;
    private int currentLine;
    private LexState currentState;
    private Token currentToken;
    private String currentLexeme;
    @Getter(AccessLevel.PRIVATE)
    private char[] lexeme;
    @Getter(AccessLevel.PRIVATE)
    private List<InstrLookup> instrTable;
    @Getter(AccessLevel.PRIVATE)
    private String fileName;

    private enum LexState {NO_STR, IN_STR, END_STR}

    public Lex2(List<InstrLookup> instrTable, String fileName) {
        loadSourceCode(fileName);
        this.currentState = LexState.NO_STR;
        this.instrTable = instrTable;
        this.fileName = fileName;
        this.lexeme = new char[1024];
    }

    private void loadSourceCode(String sourcePath) {
        List<String> sourceCodes;
        try {
            sourceCodes = Files.readAllLines(Paths.get(sourcePath));
            codes = new ArrayList<String>();
            for (String sourceCode : sourceCodes) {
                codes.add(ParseUtil.stripeComments(sourceCode).trim() + '\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Token getNextToken() {
        lexeme = new char[1024];
        index0 = index1;
        if (index0 >= codes.get(currentLine).length()) {
            if (!skipToNextLine()) {
                currentToken = Token.END_OF_TOKEN_STREAM;
                return currentToken;
            }
        }

        if (currentState == LexState.END_STR) {
            currentState = LexState.NO_STR;
        }

        // 非字符串
        if (currentState != LexState.IN_STR) {
            while (true) {
                // 找到一个非空字符就跳出
                if (!ParseUtil.isCharWhiteSpace(codes.get(currentLine).charAt(index0))) {
                    break;
                }
                ++index0;
            }
        }

        // 现在index0指向了非空标识的开头，移动index1和index0并齐
        index1 = index0;

        while (true) {
            if (currentState == LexState.IN_STR) {
                // 在字符串中，且到达行尾，肯定是错的，没有结束的双引号，字符串不能折行
                if (index1 >= codes.get(currentLine).length()) {
                    currentToken = Token.TOKEN_TYPE_INVALID;
                    return currentToken;
                }

                // 如果当前字符为反斜杠，往前移动两位，比如"He is a \\good boy"，移动到g
                if (codes.get(currentLine).charAt(index1) == '\\') {
                    index1 += 2;
                    continue;
                }

                // 当前字符为双引号，标识字符串结束
                if (codes.get(currentLine).charAt(index1) == '"') {
                    break;
                }
                index1++;
            } else {
                if (index1 >= codes.get(currentLine).length()) {
                    break;
                }

                if (ParseUtil.isCharDelimiter(codes.get(currentLine).charAt(index1))) {
                    break;
                }
                index1++;
            }
        }

        // 现在index0和index1的位置都已经就位，从非字符状态碰到引号时会发生这种情况
        if (index1 == index0) {
            index1++;
        }

        int count = 0;
        for (int i = index0; i < index1; i++) {
            if (currentState == LexState.IN_STR) {
                if (codes.get(currentLine).charAt(i) == '\\') {
                    i++;
                }
            }
            lexeme[count] = codes.get(currentLine).charAt(i);
            count++;
        }

        currentLexeme = new String(lexeme, 0, count);

        // 如果不是字符串，把它转为大写
        if (currentState != LexState.IN_STR) {
            currentLexeme = currentLexeme.toUpperCase();
        }

        // 设置默认值
        currentToken = Token.TOKEN_TYPE_INVALID;

        // 字符串
        if (currentLexeme.length() > 1) {
            if (currentState == LexState.IN_STR) {
                currentToken = Token.TOKEN_TYPE_STRING;
                return currentToken;
            }
        }

        // 单字符
        if (currentLexeme.length() == 1) {
            switch (currentLexeme.charAt(0)) {
                case '"':
                    switch (currentState) {
                        case NO_STR:
                            currentState = LexState.IN_STR;
                            break;
                        case IN_STR:
                            currentState = LexState.END_STR;
                            break;
                        case END_STR:
                            //currentState = LexState.NO_STR;
                            break;
                    }
                    currentToken = Token.TOKEN_TYPE_QUOTE;
                    break;
                case ',':
                    currentToken = Token.TOKEN_TYPE_COMMA;
                    break;
                case ':':
                    currentToken = Token.TOKEN_TYPE_COLON;
                    break;
                case '[':
                    currentToken = Token.TOKEN_TYPE_OPEN_BRACKET;
                    break;
                case ']':
                    currentToken = Token.TOKEN_TYPE_CLOSE_BRACKET;
                    break;
                case '{':
                    currentToken = Token.TOKEN_TYPE_OPEN_BRACE;
                    break;
                case '}':
                    currentToken = Token.TOKEN_TYPE_CLOSE_BRACE;
                    break;
                case '\n':
                    currentToken = Token.TOKEN_TYPE_NEWLINE;
                    break;
            }
        }

        // 多字符
        if (Xasm.getInstrByMnemonic(currentLexeme, instrTable) > -1) {
            currentToken = Token.TOKEN_TYPE_INSTR;
        } else if (ParseUtil.isStringInteger(currentLexeme)) {
            currentToken = Token.TOKEN_TYPE_INT;
        } else if (ParseUtil.isStringFloat(currentLexeme)) {
            currentToken = Token.TOKEN_TYPE_FLOAT;
        } else if (ParseUtil.isIdentifier(currentLexeme)) {
            currentToken = Token.TOKEN_TYPE_IDENT;
        } else if ("SETSTACKSIZE".equals(currentLexeme)) {
            currentToken = Token.TOKEN_TYPE_SETSTACKSIZE;
        } else if ("VAR".equals(currentLexeme)) {
            currentToken = Token.TOKEN_TYPE_VAR;
        } else if ("FUNC".equals(currentLexeme)) {
            currentToken = Token.TOKEN_TYPE_FUNC;
        } else if ("PARAM".equals(currentLexeme)) {
            currentToken = Token.TOKEN_TYPE_PARAM;
        } else if ("_RETVAL".equals(currentLexeme)) {
            currentToken = Token.TOKEN_TYPE_REG_RETVAL;
        }

        return currentToken;
    }

    public char lookAheadChar() {
        int currentLine = this.currentLine;
        int index = index1;

        if (currentState != LexState.IN_STR) {
            while (true) {

                // 如果到达行尾，取下一行
                if (index >= codes.get(currentLine).length()) {
                    currentLine++;

                    if (currentLine >= codes.size()) {
                        return 0;
                    }
                    index = 0;
                }

                if (!ParseUtil.isCharWhiteSpace(codes.get(currentLine).charAt(index))) {
                    break;
                }
                index++;
            }
        }
        return codes.get(currentLine).charAt(index);
    }

    void exitOnError(String msg) {
        throw new IllegalStateException(msg);
    }

    public void exitOnCodeError(String msg) {
        System.out.printf("Error: %s.\n\n", msg);
        System.out.printf("Line: %d\n", currentLine);

        // 将制表符替换为空格，更整齐
        String currentSourceLine = codes.get(currentLine);
        currentSourceLine = currentSourceLine.replace('\t', ' ');
        System.out.printf("%s", currentSourceLine);

        // 在出错的地方打印个^
        for (int i = 0; i < index0; i++) {
            System.out.printf(" ");
        }
        System.out.printf("^\n");

        System.out.printf("Could not assemble %s.\n", fileName);
        exitOnError(msg);
    }

    public void exitOnCharExpectError(char c) {
        exitOnError(c + "expected");
    }

    public void resetLexer() {
        currentLine = 0;
        index0 = index1 = 0;
        currentToken = Token.TOKEN_TYPE_INVALID;
        currentState = LexState.NO_STR;
    }

    public boolean skipToNextLine() {
        currentLine++;
        if (currentLine >= codes.size()) {
            return false;
        }
        index0 = index1 = 0;
        currentState = LexState.NO_STR;
        return true;
    }

    public static void main(String[] args) {
        List<Integer> corpIds = new ArrayList<Integer>();
        corpIds.add(1);
        Integer[] x = corpIds.toArray(new Integer[corpIds.size()]);
    }
}
