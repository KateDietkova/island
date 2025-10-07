package island.factory;

import island.config.AppConfigurator;
import island.config.dto.AnimalConfig;
import island.config.dto.PlantConfig;
import island.entity.organism.Organism;
import island.entity.organism.animal.Animal;
import island.entity.organism.instance.herbivore.Herbivore;
import island.entity.organism.instance.predator.Predator;
import island.entity.organism.plant.Plant;

import java.util.HashMap;
import java.util.Map;

public class OrganismPrototypeFactory {
    private static OrganismPrototypeFactory instance;

    // Here it's keeping all prototypes (one by class)
    private final Map<Class<?>, Organism> prototypes = new HashMap<>();

    private final Map<String, Organism> prototypesTest = new HashMap<>();

    private OrganismPrototypeFactory() {}

    public static OrganismPrototypeFactory getInstance() {
        if (instance == null) {
            instance = new OrganismPrototypeFactory();
        }
        return instance;
    }

    public void registerPrototype(AnimalConfig animalConfig, String name) {
        try {
            switch (animalConfig.getType()) {
                case "ANIMAL": {
                    Animal prototype = Animal.class.getDeclaredConstructor().newInstance();
                    prototypesTest.put(name, prototype);
                } case "PLANT":{
                    Plant prototype = Plant.class.getDeclaredConstructor().newInstance();
                    prototypesTest.put(name, prototype);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Cannot create prototype for " + name, e);
        }
    }

    public Organism createOrganism(AnimalConfig animalConfig, String name) {
        Organism prototype;

        if (animalConfig.getIsPredator()) {
            prototype = new Predator(name, animalConfig, true);
        } else if (animalConfig.getIsHerbivore()) {
            prototype = new Herbivore(name, animalConfig, true);
        } else {
            prototype = new Animal(name, animalConfig, true); //TODO
        }

        prototypesTest.put(name, prototype);

        return prototype;
    }

    public Organism createOrganism(PlantConfig plantConfig, String name) {
        Organism prototype = new Plant(name, plantConfig);
        prototypesTest.put(name, prototype);

        return prototype;
    }

    // Get all registered type
    public Map<Class<?>, Organism> getAllPrototypes() {
        return Map.copyOf(prototypes);
    }
}
