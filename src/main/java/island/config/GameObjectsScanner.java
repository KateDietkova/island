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

    public Set<Class<? extends Organism>> getAllGameObjectClasses() {
        Reflections reflections = new Reflections("island.entity.organism");

        return reflections.getSubTypesOf(Organism.class).stream()
                // filter Interfaces and Abstract classes
                .filter(c -> !c.isInterface())
                .filter(c -> !Modifier.isAbstract(c.getModifiers()))
                // filter Annotation
                .filter(c -> !c.isSynthetic())
                .collect(Collectors.toSet());
    }

    public Class<? extends Organism> getClassBySimpleName(String simpleName) {
        return organismClasses
                .stream()
                .filter(clazz -> clazz.getSimpleName().equals(simpleName))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException("Class not found for name: " + simpleName));
    }


}
