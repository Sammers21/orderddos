package com.orderddos.network;


import io.vertx.core.Handler;

/**
 * Implementations allowed to do network loading.
 */
public interface NetworkLoader {

    /**
     * Signalize to start load
     *
     * @param address address to load
     * @param decisionEngine the engine to decide which load strategy to use
     */
    default void loadAddress(String address, DecisionEngine decisionEngine) throws Exception {
        loadAddress(address, decisionEngine,
                event -> {
                }, event -> {
                }, event -> {
                }
        );
    }

    void loadAddress(String address, DecisionEngine decisionEngine,
                     Handler<Void> requestSucceed,
                     Handler<Void> requestFailed,
                     Handler<Long> elapsedHandler) throws Exception;

}
