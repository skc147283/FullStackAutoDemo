package com.interview.wealthapi;

import static org.testng.Assert.assertEquals;

import com.interview.wealthapi.domain.RiskProfile;
import com.interview.wealthapi.service.PortfolioService;
import java.util.Map;
import org.testng.annotations.Test;

public class PortfolioServiceTestNg {

    @Test
    public void shouldProvideAggressiveTargetAllocation() {
        PortfolioService service = new PortfolioService(null, null);
        Map<String, Integer> target = service.targetAllocation(RiskProfile.AGGRESSIVE);

        assertEquals(target.get("EQUITY").intValue(), 80);
        assertEquals(target.get("BONDS").intValue(), 15);
        assertEquals(target.get("CASH").intValue(), 5);
    }
}
