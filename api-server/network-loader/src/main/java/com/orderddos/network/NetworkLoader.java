package com.orderddos.network;

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
    void loadAddress(String address, DecisionEngine decisionEngine) throws Exception;

}
