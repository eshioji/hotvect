package com.eshioji.hotvect.api.data;

/**
 * Shared interface for {@link com.eshioji.hotvect.api.data.raw.RawNamespace} and
 * {@link com.eshioji.hotvect.api.data.hashed.HashedNamespace}
 */
public interface Namespace {
    ValueType getValueType();
}
