package com.wk.ti.agents.tool.document.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static com.wk.ti.util.ParserUtil.listListToString;


/**
 * @noinspection unchecked
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DocumentSet extends GeneralPart implements Serializable {
    @Serial
    private static final long serialVersionUID = 42L;

    @Builder
    public DocumentSet(List<String> headers, List<List<String>> rows) {
        super(headers, rows);
    }

    @JsonIgnore
    public static DocumentSet fallback() {
        return DocumentSet.builder()
                .headers(List.of())
                .rows(List.of())
                .build();
    }

    // this method is suitable for DOCUMENT agent
    @JsonIgnore
    public static DocumentSet of(List<LinkedHashMap<String, Object>> documents) {
        List<List<String>> rows = new ArrayList<>();
        for (LinkedHashMap<String, Object> doc : documents) {
            rows.add(List.of(String.valueOf(doc.get("title")), String.valueOf(doc.get("url"))));
        }
        return DocumentSet.builder()
                .headers(List.of("title", "url"))
                .rows(rows)
                .build();
    }

    @JsonIgnore
    public static DocumentSet of(LinkedHashMap<String, Object> analyses) {
        List<String> headers = (List<String>) analyses.get("headers");

        List<List<String>> rows = (List<List<String>>) analyses.get("rows");

        return DocumentSet.builder()
                .headers(headers)
                .rows(rows)
                .build();
    }

    @JsonIgnore
    @Override
    public String toString() {
        if (headers == null || rows == null) {
            return new ToStringBuilder(this, ToStringStyle.NO_CLASS_NAME_STYLE).append("").toString();
        }
        return new ToStringBuilder(this, ToStringStyle.NO_CLASS_NAME_STYLE)
                .append("headers", String.join(",", headers))
                .append("rows", listListToString(rows))
                .toString();
    }

    @Override
    public <T extends GeneralPart> T of(List<String> headers, List<List<String>> rows) {
        return (T) new DocumentSet(headers, rows);
    }
}
