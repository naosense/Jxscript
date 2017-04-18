package com.pingao.xscript.xasm.model;

import lombok.Data;

import java.util.List;

/**
 * Created by pingao on 2017/2/3.
 */
@Data
public class Instr {
    private int opCode;
    private int opCount;
    private List<Op> opList;
}
