package com.wk.ti.agents.tool.routing;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClassificationResult implements Serializable {
    @Serial
    private static final long serialVersionUID = 42L;
    private String reasoning;
    private Double confidence;
    private Map<String, String> agents;

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.NO_CLASS_NAME_STYLE)
                .append("agents", agents)
                .toString();
    }
}
