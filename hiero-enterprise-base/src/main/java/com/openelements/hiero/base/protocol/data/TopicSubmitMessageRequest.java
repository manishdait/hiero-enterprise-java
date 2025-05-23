package com.openelements.hiero.base.protocol.data;

import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.TopicId;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Objects;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public record TopicSubmitMessageRequest(Hbar maxTransactionFee,
                                        Duration transactionValidDuration,
                                        @NonNull TopicId topicId,
                                        @Nullable PrivateKey submitKey,
                                        @NonNull byte[] message) implements TransactionRequest {

    static final int MAX_MESSAGE_LENGTH = 1024;

    public TopicSubmitMessageRequest {
        Objects.requireNonNull(topicId, "TopicId cannot be null");
        Objects.requireNonNull(message, "Message cannot be null");
        if (message.length > MAX_MESSAGE_LENGTH) {
            throw new IllegalArgumentException("Message cannot be longer than " + MAX_MESSAGE_LENGTH + " bytes");
        }
    }

    public static TopicSubmitMessageRequest of(@NonNull final TopicId topicId,  @NonNull final String message) {
        Objects.requireNonNull(message, "Message cannot be null");
        return of(topicId, null, message);
    }

    public static TopicSubmitMessageRequest of(@NonNull final TopicId topicId,  @NonNull final byte[] message) {
        Objects.requireNonNull(message, "Message cannot be null");
        return of(topicId, null, message);
    }

    public static TopicSubmitMessageRequest of(@NonNull final TopicId topicId, @Nullable PrivateKey submitKey, @NonNull final String message) {
        Objects.requireNonNull(message, "Message cannot be null");
        return new TopicSubmitMessageRequest(DEFAULT_MAX_TRANSACTION_FEE, DEFAULT_TRANSACTION_VALID_DURATION, topicId,
                submitKey,
                message.getBytes(StandardCharsets.UTF_8));
    }

    public static TopicSubmitMessageRequest of(@NonNull final TopicId topicId, @Nullable PrivateKey submitKey, @NonNull final byte[] message) {
        return new TopicSubmitMessageRequest(DEFAULT_MAX_TRANSACTION_FEE, DEFAULT_TRANSACTION_VALID_DURATION, topicId,
                submitKey,
                message);
    }
}
