package com.pingao.xscript.xasm.model;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pingao on 2017/2/3.
 */
@Data
@RequiredArgsConstructor
public class InstrLookup {
    private String mnemonic;
    private int opCode;
    @NonNull private int opCount;
    private List<OpTypes> opTypesList = new ArrayList<OpTypes>();
}
