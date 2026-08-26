package com.wk.ti.agents.tool.document.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import com.wk.ti.agents.Agents;
import lombok.*;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.wk.ti.util.ParserUtil.listListToString;


@SuppressWarnings({"unchecked", "unused"})
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SourceSet extends GeneralPart implements Serializable {
    @Serial
    private static final long serialVersionUID = 42L;

    private long rawCount;

    @Builder
    public SourceSet(List<String> headers, List<List<String>> rows, long rawCount) {
        super(headers, rows);
        this.rawCount = rawCount;
    }

    @JsonIgnore
    public static long getRawCount(List<SourceSet> sets) {
        if (sets == null || sets.isEmpty() || sets.getFirst() == null) {
            return 0L;
        }
        return sets.stream()
                .filter(Objects::nonNull)
                .mapToLong(SourceSet::getRawCount)
                .sum();
    }

    @JsonIgnore
    public static SourceSet fallback() {
        return SourceSet.builder()
                .rawCount(0)
                .headers(List.of())
                .rows(List.of())
                .build();
    }

    @JsonIgnore
    @Override
    public String toString() {
        if (rawCount == 0) {
            return new ToStringBuilder(this, ToStringStyle.NO_CLASS_NAME_STYLE).append("rawCount", rawCount).toString();
        }
        return new ToStringBuilder(this, ToStringStyle.NO_CLASS_NAME_STYLE)
                .append("rawCount", rawCount)
                .append("headers", String.join(",", headers))
                .append("rows", listListToString(rows))
                .toString();
    }

    @Override
    public <T extends GeneralPart> T of(List<String> headers, List<List<String>> rows) {
        return (T) SourceSet.builder()
                .rawCount(rows != null ? rows.size() : 0)
                .headers(headers)
                .rows(rows)
                .build();
    }

    @JsonIgnore
    public static SourceSet ofDocument(List<String> headers, List<AgentSearchResult> rows, Agents agentEnum) {
        return SourceSet.builder()
                .rawCount(rows != null ? rows.size() : 0)
                .headers(headers)
                .rows(convertToStringRows(rows, agentEnum))
                .build();
    }

    @JsonIgnore
    public static List<List<String>> convertToStringRows(List<AgentSearchResult> rows, Agents agentEnum) {
        if (rows == null) {
            return List.of();
        }
        return rows.stream()
                .map(doc -> List.of(
                        Agents.DOCUMENT_AGENT == agentEnum ? doc.getResourceName() : doc.getTitle(),
                        doc.getSource()
                ))
                .collect(Collectors.toList());
    }

}
