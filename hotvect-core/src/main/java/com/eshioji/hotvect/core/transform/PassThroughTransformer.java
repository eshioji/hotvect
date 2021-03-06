package com.eshioji.hotvect.core.transform;

import com.eshioji.hotvect.api.data.DataRecord;
import com.eshioji.hotvect.api.data.DataValue;
import com.eshioji.hotvect.api.data.Namespace;
import com.eshioji.hotvect.core.util.AutoMapper;
import com.google.common.collect.Sets;

import java.lang.reflect.Array;
import java.util.EnumMap;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.stream.Collectors.toSet;


/**
 * Transforms the input into output using the functions supplied in the constructor. If both input and output has keys
 * with the same name, the value is "passed through" (copied over).
 *
 * @param <IN>
 * @param <OUT>
 * @param <V>
 */
public class PassThroughTransformer<IN extends Enum<IN> & Namespace, OUT extends Enum<OUT> & Namespace, V extends DataValue>
        implements Transformer<IN, OUT, V> {
    private final AutoMapper<IN, OUT, V> autoMapper;
    private final DataRecord<OUT, Transformation<IN, V>> transformations;
    private final OUT[] transformKeys;

    public PassThroughTransformer(Class<IN> inKey, Class<OUT> outKey) {
        this(inKey, outKey, new EnumMap<>(outKey));
    }


        /**
         * Transforms the input into output using the {@code transformations} supplied in the constructor.
         * The value for a given key in the output is the calculation result of the function that was registered for that
         * key. If no function was registered for a key in the output, but a key with the same name exists in the input,
         * then the identity function is used (the value is simply copied over from input to output).
         *
         * @param inKey
         * @param outKey
         * @param transformations
         */
    @SuppressWarnings("unchecked")
    public PassThroughTransformer(Class<IN> inKey, Class<OUT> outKey, EnumMap<OUT, Transformation<IN, V>> transformations) {
        this.autoMapper = new AutoMapper<>(inKey, outKey);
        this.transformations = new DataRecord<>(outKey);
        transformations.forEach(this.transformations::put);

        // Validations
        Set<String> automapped = autoMapper.mapped().keySet().stream().map(Enum::name).collect(toSet());
        Set<String> toTransform = this.transformations.asEnumMap().keySet().stream().map(Enum::name).collect(toSet());
        Set<String> mappedAndTransformed = Sets.intersection(automapped, toTransform);

        checkArgument(mappedAndTransformed.size() == 0,
                "transformed feature's namespace cannot share the same name with any of the input's namespaces. " +
                        " Offending namespaces" + mappedAndTransformed);

        autoMapper.mapped().forEach((k, v) -> checkArgument(k.getValueType().hasNumericValues() == v.getValueType().hasNumericValues(),
                "You are trying to map a raw value into a hashed value with incompatible value type." +
                        (k.getValueType().hasNumericValues() ?
                                k + " has numeric values and hence must be mapped to a numeric hashed value type." :
                                k + " does not have numeric values and hence must be mapped to a categorical hashed value type.")
                ));


        @SuppressWarnings("unchecked")
        OUT[] transformKeys = (OUT[]) Array.newInstance(outKey, this.transformations.asEnumMap().size());
        this.transformKeys = this.transformations.asEnumMap().keySet().toArray(transformKeys);
    }

    /**
     * Transform the specified record
     * @param toTransform the record to transform
     * @return the transformed record
     */
    @Override
    public DataRecord<OUT, V> apply(DataRecord<IN, V> toTransform) {
        DataRecord<OUT, V> mapped = autoMapper.apply(toTransform);
        for (OUT p : transformKeys) {
            Transformation<IN, V> transformation = transformations.get(p);
            V parsed = transformation.apply(toTransform);
            if (parsed != null) {
                mapped.put(p, parsed);
            }
        }
        return mapped;
    }

}
