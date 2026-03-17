package com.interview.wealthapi;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.interview.wealthapi.domain.RiskProfile;
import com.interview.wealthapi.service.PortfolioService;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class PortfolioServiceTest {

    @Test
    public void shouldProvideAggressiveTargetAllocation() {
        PortfolioService service = new PortfolioService(null, null);
        Map<String, Integer> target = service.targetAllocation(RiskProfile.AGGRESSIVE);

        assertEquals(target.get("EQUITY").intValue(), 80);
        assertEquals(target.get("BONDS").intValue(), 15);
        assertEquals(target.get("CASH").intValue(), 5);
    }
}
