package com.pingao.xscript.xasm.model;

import lombok.Data;

/**
 * Created by pingao on 2017/2/3.
 */
@Data
public class FunNode {
    private int index;
    private String name;
    private int entryPoint;
    private int paramCount;
    private int localDataSize;
}
