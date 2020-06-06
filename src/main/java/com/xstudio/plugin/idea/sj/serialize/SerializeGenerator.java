package com.xstudio.plugin.idea.sj.serialize;

import com.intellij.codeInsight.CodeInsightActionHandler;
import com.intellij.codeInsight.generation.actions.BaseGenerateAction;

/**
 * @author Beeant
 * @version 2020/6/6
 */
public class SerializeGenerator extends BaseGenerateAction {

    public SerializeGenerator() {
        super(new SerializeGeneratorHandler());
    }

    protected SerializeGenerator(CodeInsightActionHandler handler) {
        super(handler);
    }
}
