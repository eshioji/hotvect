package com.eshioji.hotvect.core.score;

import com.eshioji.hotvect.api.data.DataRecord;
import com.eshioji.hotvect.api.data.SparseVector;
import com.eshioji.hotvect.api.data.raw.RawNamespace;
import com.eshioji.hotvect.api.data.raw.RawValue;
import com.eshioji.hotvect.api.scoring.Scorer;
import com.eshioji.hotvect.core.vectorization.Vectorizer;


/**
 * A {@link Scorer} which uses the specified {@link Vectorizer} and {@link Estimator}
 * @param <R>
 */
public class ScorerImpl<R extends Enum<R> & RawNamespace> implements Scorer<R> {
    private final Vectorizer<R> vectorizer;
    private final Estimator estimator;

    /**
     * Constructs a {@link ScorerImpl}
     * @param vectorizer the {@link Vectorizer} to use
     * @param estimator the {@link Estimator} to use
     */
    public ScorerImpl(Vectorizer<R> vectorizer, Estimator estimator) {
        this.vectorizer = vectorizer;
        this.estimator = estimator;
    }

    /**
     * Given a raw {@link DataRecord}, calculate the score
     * @param input the raw {@link DataRecord} to score
     * @return the score
     */
    @Override
    public double applyAsDouble(DataRecord<R, RawValue> input) {
        SparseVector vectorized = vectorizer.apply(input);
        return estimator.applyAsDouble(vectorized);
    }
}
