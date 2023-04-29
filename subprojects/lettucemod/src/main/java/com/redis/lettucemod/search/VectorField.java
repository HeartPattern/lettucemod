package com.redis.lettucemod.search;

import com.redis.lettucemod.protocol.SearchCommandKeyword;

public class VectorField<K> extends Field<K> {
    private VectorField(Builder<K> builder) {
        super(Type.VECTOR, builder);
    }

    @Override
    protected void buildField(SearchCommandArgs<K, Object> args) {
        args.add(SearchCommandKeyword.VECTOR);
    }

    @Override
    public String toString() {
        return "VectorField [type=" + type + ", name=" + name + ", as=" + as + ", sortable=" + sortable
                + ", unNormalizedForm=" + unNormalizedForm + ", noIndex=" + noIndex + "]";
    }

    public static <K> Builder<K> name(K name) {
        return new Builder<>(name);
    }

    public static class Builder<K> extends Field.Builder<K, Builder<K>> {
        public Builder(K name) {
            super(name);
        }

        public VectorField<K> build() {
            return new VectorField<>(this);
        }
    }
}
