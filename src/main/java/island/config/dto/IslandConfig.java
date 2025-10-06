package island.config.dto;

import java.util.Map;

public class IslandConfig {
    private int width;
    private int height;
    private Map<String, Integer> initialPlants;
    private Map<String, Integer> initialAnimals;
    private int maxAnimalsPerCell;
    private int maxPlantsPerCell;
    private int tactDurationMillis;

    public int getTactDurationMillis() {
        return tactDurationMillis;
    }

    public void setTactDurationMillis(int tactDurationMillis) {
        this.tactDurationMillis = tactDurationMillis;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public Map<String, Integer> getInitialPlants() {
        return initialPlants;
    }

    public void setInitialPlants(Map<String, Integer> initialPlants) {
        this.initialPlants = initialPlants;
    }

    public Map<String, Integer> getInitialAnimals() {
        return initialAnimals;
    }

    public void setInitialAnimals(Map<String, Integer> initialAnimals) {
        this.initialAnimals = initialAnimals;
    }

    public int getMaxAnimalsPerCell() {
        return maxAnimalsPerCell;
    }

    public void setMaxAnimalsPerCell(int maxAnimalsPerCell) {
        this.maxAnimalsPerCell = maxAnimalsPerCell;
    }

    public int getMaxPlantsPerCell() {
        return maxPlantsPerCell;
    }

    public void setMaxPlantsPerCell(int maxPlantsPerCell) {
        this.maxPlantsPerCell = maxPlantsPerCell;
    }
}
