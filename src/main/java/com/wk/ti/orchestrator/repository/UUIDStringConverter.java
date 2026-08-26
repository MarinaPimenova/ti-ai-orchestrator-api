package com.wk.ti.orchestrator.repository;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.UUID;

@Converter
public class UUIDStringConverter implements AttributeConverter<String, UUID> {
    @Override
    public UUID convertToDatabaseColumn(String attribute) {
        return attribute == null ? null : UUID.fromString(attribute);
    }

    @Override
    public String convertToEntityAttribute(UUID dbData) {
        return dbData == null ? null : dbData.toString();
    }
}

