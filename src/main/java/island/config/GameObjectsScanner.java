package island.config;

import island.entity.organism.Organism;
import org.reflections.Reflections;

import java.lang.reflect.Modifier;
import java.util.Set;
import java.util.stream.Collectors;

public class GameObjectsScanner {
    private static GameObjectsScanner instance;

    private final Set<Class<? extends Organism>> organismClasses;

    private GameObjectsScanner() {
        Reflections reflections = new Reflections("island.entity.organism");
        this.organismClasses = reflections.getSubTypesOf(Organism.class);
    }

    public static GameObjectsScanner getInstance() {
        if (instance == null) {
            instance = new GameObjectsScanner();
        }
        return instance;
    }


}
