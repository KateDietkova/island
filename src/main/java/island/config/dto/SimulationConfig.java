package island.config.dto;

import java.util.Map;

public class SimulationConfig {
    private int tactDurationMillis;
    private String stopCondition;
    private int maxTicks;
    private Map<String, Integer> offspringCount;

    public int getTactDurationMillis() {
        return tactDurationMillis;
    }

    public void setTactDurationMillis(int tactDurationMillis) {
        this.tactDurationMillis = tactDurationMillis;
    }

    public String getStopCondition() {
        return stopCondition;
    }

    public void setStopCondition(String stopCondition) {
        this.stopCondition = stopCondition;
    }

    public int getMaxTicks() {
        return maxTicks;
    }

    public void setMaxTicks(int maxTicks) {
        this.maxTicks = maxTicks;
    }

    public Map<String, Integer> getOffspringCount() {
        return offspringCount;
    }

    public void setOffspringCount(Map<String, Integer> offspringCount) {
        this.offspringCount = offspringCount;
    }
}
