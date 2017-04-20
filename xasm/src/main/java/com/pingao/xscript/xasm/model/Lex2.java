package com.pingao.xscript.xasm.model;

import com.pingao.xscript.xasm.enums.Token;
import com.pingao.xscript.xasm.util.ParseUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pingao on 2017/2/7.
 */
public class Lex2 {
    private int index0;
    private int index1;
    private char[] lexeme;
    private List<String> codes;
    private int currentLine;
    private LexState currentState;
    private Token currentToken;

    public enum LexState {NO_STR, IN_STR, END_STR}

    public Lex2(List<String> codes) {
        lexeme = new char[256];
        this.codes = codes;
        this.currentState = LexState.NO_STR;
    }

    Token getNextToken() {
        index0 = index1;
        if (index0 >= codes.get(currentLine).length()) {
            if (!skipToNextLine()) {
                return Token.END_OF_TOKEN_STREAM;
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
    }

    boolean skipToNextLine() {
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
