package com.htjc.assemble.procedure;

import com.htjc.assemble.model.Doc;

/**
 * Created by guilin on 2016/9/1.
 * 工序接口
 * (实现该接口时，若有类似数据库建连等操作时，最好使用池来管理)
 */
public interface Procedure extends NamedComponent, ConfigComponent {

    /**
     * Process a single {@link Doc}.
     *
     * @param doc Doc to be processed
     * @return Original or modified doc, or {@code null} if the Doc
     * is to be dropped (i.e. filtered out).
     */
    Doc process(Doc doc) throws Exception;

}
