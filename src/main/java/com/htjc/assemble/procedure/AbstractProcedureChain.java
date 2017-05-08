package com.htjc.assemble.procedure;

import com.alibaba.fastjson.JSON;
import com.htjc.assemble.model.Doc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by guilin on 2016/9/27.
 */
public abstract class AbstractProcedureChain {

    private static final Logger logger = LoggerFactory.getLogger(AbstractProcedureChain.class);

    private static final Logger errorDocLogger = LoggerFactory.getLogger("errorDoc");

    protected String name;

    // list of procedures that will be traversed, in order
    protected List<Procedure> procedures;

    public abstract void buildChain();

    public Doc process(Doc doc) {
        for (Procedure procedure : procedures) {
            if (doc == null) return null;
            try {
                doc = procedure.process(doc);
            } catch (Exception e) {
                logger.error("name:%s, config:", procedure.getName(), procedure.getConfig().toString(), e);
                errorDocLogger.error(JSON.toJSONString(doc));
                return null;
            }
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
