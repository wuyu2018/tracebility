package com.foodtraceability.domain;

import java.util.Objects;

public abstract class ValueObject {

    protected abstract Object[] getAttributes();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ValueObject that = (ValueObject) o;
        return Objects.deepEquals(getAttributes(), that.getAttributes());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAttributes());
    }
}
