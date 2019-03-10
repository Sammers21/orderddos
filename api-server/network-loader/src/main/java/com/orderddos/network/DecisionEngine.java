package com.orderddos.network;

import com.orderddos.network.decisions.Decision;

import java.util.Queue;

public interface DecisionEngine {

    Decision makeDecision(Queue<LoadStatistics> loadStatistics);
}
