package com.govos.srh.observability;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@ConditionalOnProperty(prefix = "govos.search.observation", name = "enabled", havingValue = "true", matchIfMissing = true)
@interface ConditionalOnObservationEnabled {
}
