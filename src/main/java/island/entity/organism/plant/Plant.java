package island.entity.organism.plant;

import island.config.dto.PlantConfig;
import island.entity.map.Cell;
import island.entity.organism.Organism;

public class Plant extends Organism {
    private final double baseWeight;
    private final int maxPerCell;
    private static final double GROWTH_PER_DAY = 0.2;
    private static final double REPRODUCTION_THRESHOLD_MULTIPLIER = 2.0;

    public Plant(String name, PlantConfig config) {
        this.name = name;
        this.baseWeight = config.getWeight();
        this.currentWeight = baseWeight;
        this.maxPerCell = config.getMaxCountPerCell();
    }
    @Override
    public Organism reproduce() {
        try {
            return this.getClass().getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Cannot reproduce plant: " + this.getClass().getSimpleName(), e);
        }
    }

    public int getMaxPerCell() {
        return maxPerCell;
    }

    public double takeBite(double requested) {
        if (!isAlive || requested <= 0) return 0.0;

        double bite = Math.min(currentWeight, requested);
        changeWeight(-bite);

        if (currentWeight <= 0.0) {
            die();
        }
        return bite;
    }

    public Plant reproduceIfPossible() {
        if (currentCell == null) return null;

        if (currentCell.countResidentsByName(this.getName()) >= maxPerCell) {
            return null;
        }

        Plant offspring = (Plant) this.clone();
        offspring.setCurrentCell(currentCell);
        currentCell.addResident(offspring);

        try {
            currentCell.getField().getStats().onBirth(this.getName(), 1);
        } catch (Exception ignored) {}

        return offspring;
    }


    public void grow() {
        if (!isAlive) return;

        this.changeWeight(GROWTH_PER_DAY);

        double threshold = baseWeight * REPRODUCTION_THRESHOLD_MULTIPLIER;
        if (this.currentWeight >= threshold) {
            reproduceIfPossible();
            this.currentWeight = baseWeight;
        }
    }

    public void liveOneDay() {
        grow();
    }

}
