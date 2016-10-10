package com.htjc.assemble.procedure;

import com.htjc.assemble.model.Doc;

import java.util.List;

/**
 * Created by guilin on 2016/9/27.
 */
public abstract class AbstractProcedureChain {
    protected String name;

    // list of procedures that will be traversed, in order
    protected List<Procedure> procedures;

    public abstract void buildChain();

    public Doc process(Doc doc) {
        for (Procedure procedure : procedures) {
            if (doc == null) return null;
            doc = procedure.process(doc);
        }
        return doc;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Procedure> getProcedures() {
        return procedures;
    }

    public void setProcedures(List<Procedure> procedures) {
        this.procedures = procedures;
    }
}
