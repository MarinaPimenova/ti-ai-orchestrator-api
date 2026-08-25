package com.wk.ti.agents.memory;

import lombok.Data;

@Data
public class AgentContext {
    protected String conversationId;
    protected Long questionId;
    protected String question;

}
