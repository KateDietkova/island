package island.config;

import island.entity.organism.Organism;

public class PrototypeLoader {

    private static PrototypeLoader instance;

    private PrototypeLoader() {}

    public static PrototypeLoader getInstance() {
        if (instance == null) {
            instance = new PrototypeLoader();
        }
        return instance;
    }

    public Organism loadPrototype(Class<? extends Organism> clazz) {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create prototype for class");
        }
    }
}
