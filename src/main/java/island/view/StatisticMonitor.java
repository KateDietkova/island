package island.view;

import java.util.HashMap;
import java.util.Map;

public class StatisticMonitor {
    private final Map<String, Integer> birthsThisTick = new HashMap<>();
    private final Map<String, Integer> deathsThisTick = new HashMap<>();
    private long totalBirths = 0;
    private long totalDeaths = 0;

    public void onBirth(String species, int count) {
        if (count <= 0) return;
        birthsThisTick.merge(species, count, Integer::sum);
        totalBirths += count;
    }

    public void onDeath(String species) {
        deathsThisTick.merge(species, 1, Integer::sum);
        totalDeaths += 1;
    }

    public Map<String, Integer> getBirthsThisTick() { return birthsThisTick; }
    public Map<String, Integer> getDeathsThisTick() { return deathsThisTick; }
    public long getTotalBirths() { return totalBirths; }
    public long getTotalDeaths() { return totalDeaths; }
}
