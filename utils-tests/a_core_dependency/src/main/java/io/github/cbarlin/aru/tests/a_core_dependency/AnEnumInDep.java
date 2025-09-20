package io.github.cbarlin.aru.tests.a_core_dependency;

import io.github.cbarlin.aru.annotations.TypeConverter;

public enum AnEnumInDep {
    MONDAY("Monday"),
    TUESDAY("Tuesday");

    private final String label;

    AnEnumInDep(final String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    @TypeConverter
    public static AnEnumInDep fromLabel(final String label) {
        if (MONDAY.label().equals(label)) {
            return MONDAY;
        }
        return TUESDAY;
    }
}
