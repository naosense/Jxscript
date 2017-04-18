package com.pingao.xscript.xasm.model;

import lombok.Data;

/**
 * Created by pingao on 2017/2/3.
 */
@Data
public class ScriptHeader {
    private int stackSize;
    private int globalDataSize;
    private boolean isMainFunPresent;
    private int mainFunIndex;
}
