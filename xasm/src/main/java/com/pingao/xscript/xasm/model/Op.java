package com.pingao.xscript.xasm.model;

import lombok.Data;

/**
 * Created by pingao on 2017/2/3.
 */
@Data
public class Op {
    private int type;
    private int intLiteral;
    private float floatLiteral;
    private int stringTableIndex;
    private int stackIndex;
    private int instrIndex;
    private int funIndex;
    private int hostAPICallIndex;
    private int regCode;
    private int offsetIndex;
}
