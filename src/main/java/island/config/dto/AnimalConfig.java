package island.config.dto;

public class AnimalConfig {
    private String type;
    private boolean isHerbivore = false;

    private boolean isPredator = false;

    private double weight;
    private double minWeight;
    private int maxCountPerCell;
    private int speed;
    private double foodNeed;
    private int maxDaysWithoutFood;
    private int adultAge;

    public String getType() { return type; }

    public boolean getIsHerbivore() { return isHerbivore; }

    public boolean getIsPredator() { return isPredator; }

    public double getWeight() { return weight; }
    public double getMinWeight() { return minWeight; }
    public int getMaxCountPerCell() { return maxCountPerCell; }
    public int getSpeed() { return speed; }
    public double getFoodNeed() { return foodNeed; }
    public int getMaxDaysWithoutFood() { return maxDaysWithoutFood; }
    public int getAdultAge() { return adultAge; }
}
