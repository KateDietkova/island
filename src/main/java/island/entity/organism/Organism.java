package island.entity.organism;

import island.config.dto.AnimalConfig;
import island.entity.map.Cell;
import island.entity.organism.animal.Gender;

public abstract class  Organism implements Reproducible, Cloneable {
    protected String name;
    protected boolean isAlive = true;
    protected double currentWeight;

    protected Cell currentCell;

    public String getName() {
        return name;
    }

    public void setCurrentCell(Cell cell) {
        this.currentCell = cell;
    }

    public boolean isAlive() {
        return isAlive;
    }

    public void die() {
        isAlive = false;

        if (currentCell != null) {
            currentCell.removeResident(this);
        }
    }

    public double getCurrentWeight() {
        return currentWeight;
    }

    public void changeWeight(double delta) {
        currentWeight = Math.max(0.0, currentWeight + delta);
    }
    public abstract Organism reproduce();

    protected Organism() {
    }

    @Override
    public Organism clone() {
        try {
            return (Organism) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Failed to clone Organism", e);
        }
    }
}
