package island.entity.organism.animal;

import island.config.AppConfigurator;
import island.config.dto.AnimalConfig;
import island.entity.map.Cell;
import island.entity.map.GameField;
import island.entity.organism.Movable;
import island.entity.organism.Organism;
import island.entity.organism.MoveIntent;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class Animal extends Organism implements Movable {
    protected Gender gender;
    protected final double baseWeight;
    protected final double minWeight;
    protected final int maxCountPerCell;
    protected final int speed;
    protected final double foodNeed;
    protected final int maxDaysWithoutFood;
    protected final int adultAge;

    protected double currentWeight;
    protected int age = 0;
    protected int daysWithoutFood = 0;

    public Animal(String name, AnimalConfig config, boolean randomAge) {
        this.name = name;
        this.baseWeight = config.getWeight();
        this.currentWeight = baseWeight;
        this.minWeight = config.getMinWeight();
        this.maxCountPerCell = config.getMaxCountPerCell();
        this.speed = config.getSpeed();
        this.foodNeed = config.getFoodNeed();
        this.maxDaysWithoutFood = config.getMaxDaysWithoutFood();
        this.adultAge = config.getAdultAge();
        this.gender = Gender.randomGender();
        this.age = randomAge ? adultAge / 2 : 0;
    }

    @Override
    public void move() {
        if (currentCell == null) return;

        GameField field = currentCell.getField();
        int currentX = currentCell.getX();
        int currentY = currentCell.getY();

        // Generate a random offset for x and y within the range of speed
        int deltaX = new Random().nextInt(speed * 2 + 1) - speed;
        int deltaY = new Random().nextInt(speed * 2 + 1) - speed;

        int newX = Math.max(0, Math.min(field.getWidth() - 1, currentX + deltaX));
        int newY = Math.max(0, Math.min(field.getHeight() - 1, currentY + deltaY));

        Cell newCell = field.getCell(newX, newY);

        currentCell.removeResident(this);
        newCell.addResident(this);
        currentCell = newCell;
    }


    public void eat() {}

    @Override
    public Animal reproduce() {
        return this; //TODO
    }

    public boolean isAdult() {
        return age >= adultAge;
    }
    public boolean isAlive() {
        return isAlive;
    }

    public void growOlder() {
        age++;
    }
    public double getCurrentWeight() {
        return currentWeight;
    }
    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender g) { this.gender = g; }
    public void changeWeight(double delta) {
        currentWeight = Math.max(0, currentWeight + delta);
    }


    public void actInPlaceOneDay() {
        growOlder();
        eat();
        Animal offspring = reproduceIfAdult();
        addOffspringToCell(offspring);
        checkDeath();
    }

    public MoveIntent computeMoveIntent() {
        if (!isAlive || currentCell == null || speed <= 0) return null;

        GameField field = currentCell.getField();
        int currentX = currentCell.getX();
        int currentY = currentCell.getY();

        int deltaX = ThreadLocalRandom.current().nextInt(-speed, speed + 1);
        int deltaY = ThreadLocalRandom.current().nextInt(-speed, speed + 1);

        int newX = Math.max(0, Math.min(field.getWidth() - 1, currentX + deltaX));
        int newY = Math.max(0, Math.min(field.getHeight() - 1, currentY + deltaY));

        if (newX == currentX && newY == currentY) return null; // don't move

        Cell to = field.getCell(newX, newY);
        return new MoveIntent(this, currentCell, to);
    }

    protected Animal reproduceIfAdult() {
        if (!isAdult() || currentCell == null) return null;

        // looking for a partner
        Animal partner = currentCell.findMate(getName(), gender.opposite(), this);
        if (partner == null) return null;

        Animal child = (Animal) this.clone();
        child.age = 0;
        child.currentWeight = baseWeight;
        child.setGender(Gender.randomGender());
        return child;
    }

    protected void addOffspringToCell(Animal offspring) {
        if (offspring != null && currentCell != null) {
            AppConfigurator config = AppConfigurator.getInstance();
            int maxCount = config.getAnimalConfig(this.getName()).getMaxCountPerCell();

            // get offspring count from config
            int offspringCount = config.getSimulationConfig()
                    .getOffspringCount()
                    .getOrDefault(this.getName(), 1);

            for (int i = 0; i < offspringCount; i++) {
                if (currentCell.countResidentsByName(this.getName()) < maxCount) {
                    Animal child = (Animal) offspring.clone();
                    child.setCurrentCell(currentCell);
                    currentCell.addResident(child);

                    try {
                        currentCell.getField().getStats().onBirth(this.getName(), 1);
                    } catch (Exception ignored) {}

                } else {
                    System.out.println("Cannot add offspring, cell is full");
                    break;
                }
            }
        }
    }

    protected void checkDeath() {
        if (daysWithoutFood >= maxDaysWithoutFood || currentWeight < minWeight) {
            die();
        }
    }

}
