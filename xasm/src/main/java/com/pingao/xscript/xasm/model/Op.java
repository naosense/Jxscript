package com.pingao.xscript.xasm.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Created by pingao on 2017/2/3.
 */
@Builder
@Getter
@Setter
@ToString
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
