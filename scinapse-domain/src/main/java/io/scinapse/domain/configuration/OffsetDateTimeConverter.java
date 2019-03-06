package io.scinapse.domain.configuration;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;

@Converter(autoApply = true)
public class OffsetDateTimeConverter implements AttributeConverter<OffsetDateTime, Date> {

    @Override
    public Date convertToDatabaseColumn(OffsetDateTime attribute) {
        return attribute == null ? null : Date.from(attribute.toInstant());
    }

    @Override
    public OffsetDateTime convertToEntityAttribute(Date dbData) {
        return dbData == null ? null : OffsetDateTime.ofInstant(dbData.toInstant(), ZoneOffset.systemDefault());
    }

}
