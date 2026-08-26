package com.wk.ti.agents.memory;

import org.springframework.stereotype.Component;

@SuppressWarnings("unused")
@Component
public class AgentContextHolder {
    private static final ThreadLocal<AgentContext> context = new ThreadLocal<>();

    public AgentContext get() {
        AgentContext ctx = context.get();
        if (ctx == null) {
            ctx = new AgentContext();
            context.set(ctx);
        }
        return ctx;
    }

    public void remove() {
        context.remove();
    }
}
