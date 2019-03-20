package com.orderddos.network;

import com.orderddos.network.decisions.Decision;

import java.util.Deque;

public interface DecisionEngine {

    Decision makeDecision(Deque<LoadStatistics> loadStatistics);
}
