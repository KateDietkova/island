package island.entity.organism.instance.herbivore;

import island.config.dto.AnimalConfig;
import island.entity.organism.Organism;
import island.entity.organism.animal.Animal;
import island.entity.organism.plant.Plant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class Herbivore extends Animal {

    public Herbivore(String name, AnimalConfig config, boolean randomAge) {
        super(name, config, randomAge);
    }

    @Override
    public void eat() {
        if (!isAlive || currentCell == null) return;

        double eaten = 0.0;

        ArrayList<Organism> residents = new ArrayList<>(currentCell.getAllResidents());
        Collections.shuffle(residents, new Random());

        for (Organism o : residents) {
            if (!(o instanceof Plant plant) || !plant.isAlive()) continue;

            double need = foodNeed - eaten;
            if (need <= 0) break;

            // take a bite
            double bite = plant.takeBite(need);

            // gain weight
            changeWeight(bite);
            eaten += bite;

            if (eaten >= foodNeed) break;
        }

        if (eaten >= foodNeed) {
            daysWithoutFood = 0;
        } else {
            daysWithoutFood++;
            // basic metabolism
            changeWeight(-(foodNeed - eaten));
        }
    }
//        if (!isAlive || currentCell == null) return;
//
//        double eaten = 0.0;
//
//        for (Organism o : new ArrayList<>(currentCell.getAllResidents())) {
//            if (!(o instanceof Plant plant) || !plant.isAlive()) continue;
//
//            double need = foodNeed - eaten;
//            if (need <= 0) break;
//
//            double bite = Math.min(plant.getCurrentWeight(), need);
//            changeWeight(bite);
//            plant.changeWeight(-bite);
//            eaten += bite;
//
//            if (plant.getCurrentWeight() <= 0) {
//                plant.die();
//            }
//        }
//
//        if (eaten >= foodNeed) {
//            daysWithoutFood = 0;
//        } else {
//            daysWithoutFood++;
//            //basic metabolism
//            changeWeight(-(foodNeed - eaten));
//        }
//    }
}
