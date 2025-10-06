package island.entity.organism.instance.predator;

import island.config.AppConfigurator;
import island.config.dto.AnimalConfig;
import island.entity.organism.Organism;
import island.entity.organism.animal.Animal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class Predator extends Animal {

    public Predator(String name, AnimalConfig config, boolean randomAge) {
        super(name, config, randomAge);
    }

    @Override
    public void eat() {
        if (!isAlive || currentCell == null) return;

        var residents = new ArrayList<>(currentCell.getAllResidents());
        Collections.shuffle(residents, new Random());

        double eaten = 0.0;

        for (Organism o : residents) {
            if (!(o instanceof Animal prey) || !prey.isAlive()) continue;
            if (!canEat(prey)) continue;

            double need = foodNeed - eaten;
            if (need <= 0) break;

            double bite = Math.min(prey.getCurrentWeight(), need);

            this.changeWeight(bite);
            prey.changeWeight(-bite);

            if (prey.getCurrentWeight() <= 0.0) {
                prey.die();
            }

            eaten += bite;
            if (eaten >= foodNeed) break;
        }

        if (eaten >= foodNeed) {
            daysWithoutFood = 0;
        } else {
            daysWithoutFood++;
            this.changeWeight(-(foodNeed - eaten)); // basic metabolism
        }
    }

    protected boolean canEat(Organism prey) {
        if (!prey.isAlive()) return false;
        String predator = this.getName();
        String preyName = prey.getName();

        int probability = AppConfigurator.getInstance()
                .getFeedingProbability(predator, preyName);

        if (probability <= 0) return false;
        return new Random().nextInt(100) < probability;
    }
}
