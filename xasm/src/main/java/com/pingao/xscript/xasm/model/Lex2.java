package com.pingao.xscript.xasm.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pingao on 2017/2/7.
 */
public class Lex2 {
    private int index0;
    private int index1;
    private char[] lexeme;
    public enum LexState {START, INT, FLOAT, END}

    public Lex2() {
        lexeme = new char[256];
    }

    public void print() {
        lexeme[0] = 'a';
        String s = "a";
        System.out.println(s.charAt(0) == '\0');
        System.out.println(lexeme[1] == '\0');
        System.out.println(lexeme.length);
        System.out.println(lexeme);
        System.out.println(lexeme.toString().length());
        System.out.println(new String(lexeme, 0, 2).length());
    }

    public static void main(String[] args) {
        List<Integer> corpIds = new ArrayList<Integer>();
        corpIds.add(1);
        Integer [] x =  corpIds.toArray(new Integer[corpIds.size()]);


    }
}
