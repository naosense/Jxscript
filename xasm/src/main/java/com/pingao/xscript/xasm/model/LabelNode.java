package com.pingao.xscript.xasm.model;

import lombok.Data;

/**
 * Created by pingao on 2017/2/3.
 */
@Data
public class LabelNode {
    private int index;
    private String identifier;
    private int targetIndex;
    private int funIndex;
}
