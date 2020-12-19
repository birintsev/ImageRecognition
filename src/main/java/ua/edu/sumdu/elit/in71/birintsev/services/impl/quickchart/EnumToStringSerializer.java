package ua.edu.sumdu.elit.in71.birintsev.services.impl.quickchart;

import com.fasterxml.jackson.databind.util.StdConverter;

public class EnumToStringSerializer<T extends Enum<?>>
    extends StdConverter<T, String> {

    @Override
    public String convert(T value) {
        return value.toString();
    }
}
