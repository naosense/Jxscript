package com.pingao.xscript.xasm.enums;

/**
 * Created by pingao on 2017/2/6.
 */
public enum Token {
    TOKEN_TYPE_INT(0),
    TOKEN_TYPE_FLOAT(1),
    TOKEN_TYPE_STRING(2),
    TOKEN_TYPE_QUOTE(3),
    TOKEN_TYPE_IDENT(4),
    TOKEN_TYPE_COLON(5),
    TOKEN_TYPE_OPEN_BRACKET(6),
    TOKEN_TYPE_CLOSE_BRACKET(7),
    TOKEN_TYPE_COMMA(8),
    TOKEN_TYPE_OPEN_BRACE(9),
    TOKEN_TYPE_CLOSE_BRACE(10),
    TOKEN_TYPE_NEWLINE(11),
    TOKEN_TYPE_INSTR(12),
    TOKEN_TYPE_SETSTACKSIZE(13),
    TOKEN_TYPE_VAR(14),
    TOKEN_TYPE_FUNC(15),
    TOKEN_TYPE_PARAM(16),
    TOKEN_TYPE_REG_RETVAL(17),
    TOKEN_TYPE_INVALID(18),
    END_OF_TOKEN_STREAM(19);

    private int value;

    Token(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
