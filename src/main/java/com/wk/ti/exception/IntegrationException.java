package com.wk.ti.exception;

import com.wk.ti.exception.model.ClientErrorResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class IntegrationException extends RuntimeException {
    private ClientErrorResponse errorResponse;

    public IntegrationException(ClientErrorResponse errorResponse) {
        this.errorResponse = errorResponse;
    }
}