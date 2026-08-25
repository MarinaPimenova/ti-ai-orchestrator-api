package com.wk.ti.agents.tool.document.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.wk.ti.agents.Agents;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@SuppressWarnings("unused")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AgentResponse {
    private String conversationId;
    private Long questionId;
    private String termList;
    private String agentType;
    private SourceSet sourceSet;
    private String summary;
    private Object documentSet;
    private Boolean isHITL;

/*    @JsonIgnore
    public Agents getAgentEnum() {
        if (this.agentType == null) {
            return null;
        }
        for (Agents e : Agents.values()) {
            if (e.name().equals(this.agentType)) {
                return e;
            }
        }
        return null;
    }*/

    public static AgentResponse of(String conversationId,
                                   Long questionId,
                                   Agents agent,
                                   DocumentSet fallbackSummary,
                                   SourceSet fallback,
                                   String summary,
                                   Boolean isHITL) {
        return AgentResponse.builder()
                .conversationId(conversationId)
                .questionId(questionId)
                .agentType(agent.name())
                .termList("")
                .sourceSet(fallback)
                .documentSet(fallbackSummary)
                .summary(summary)
                .isHITL(isHITL)
                .build();
    }

    @JsonInclude
    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.NO_CLASS_NAME_STYLE)
                .append("conversationId", conversationId)
                .append("questionId", questionId)
                .append("termList", termList)
                .append("agentType", agentType)
                .append("sourceSet", sourceSet)
                .append("documentSet", documentSet)
                .append("summary", summary)
                .append("isHITL", isHITL)
                .toString();
    }

    @JsonIgnore
    public static AgentResponse fallback(String conversationId, Long questionId, Agents agentType) {
        return AgentResponse.builder()
                .conversationId(conversationId)
                .questionId(questionId)
                .agentType(agentType.name())
                .termList("")
                .sourceSet(SourceSet.builder().build())
                .documentSet(DocumentSet.fallback())
                .summary("failed")
                .isHITL(false)
                .build();
    }

}
