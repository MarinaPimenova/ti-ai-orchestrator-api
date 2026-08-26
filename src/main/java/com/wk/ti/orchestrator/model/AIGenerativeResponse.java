package com.wk.ti.orchestrator.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AIGenerativeResponse {
    private String sessionId;
    private Long questionId;
    private String content;
}
