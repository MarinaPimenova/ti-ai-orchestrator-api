package com.wk.ti.orchestrator.model;

import java.util.List;

@SuppressWarnings("unused")
public record CancellationRequest(List<SubscriptionIdentifier> subscriptions) {}
