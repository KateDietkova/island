package island.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import island.config.dto.AnimalConfig;
import island.config.dto.IslandConfig;
import island.config.dto.PlantConfig;
import island.config.dto.SimulationConfig;
import island.factory.OrganismPrototypeFactory;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class AppConfigurator {

    private static AppConfigurator instance;
    private final GameObjectsScanner gameObjectsScanner = GameObjectsScanner.getInstance();
    private final PrototypeLoader prototypeLoader = PrototypeLoader.getInstance();
    private final OrganismPrototypeFactory organismPrototypeFactory = OrganismPrototypeFactory.getInstance();

    private IslandConfig islandConfig;
    private SimulationConfig simulationConfig;
    private Map<String, AnimalConfig> animalsConfig;
    private Map<String, PlantConfig> plantsConfig;
    private Map<String, Map<String, Integer>> feedingProbabilities;
    private AppConfigurator() {
    }

    public static AppConfigurator getInstance() {
        if(instance == null) {
            instance = new AppConfigurator();
        }

        return instance;
    }

    public void init() {
        loadConfigs("island.json", "animals.json", "plants.json", "simulation.json");
        loadFeedingProbabilities("feeding.json");
    }

    public void loadConfigs(String islandPath, String animalsPath, String plantsPath, String simulationPath) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            islandConfig = mapper.readValue(new File(islandPath), IslandConfig.class);

            simulationConfig = mapper.readValue(new File(simulationPath), SimulationConfig.class);

            animalsConfig = mapper.readValue(new File(animalsPath), new TypeReference<>() {});
            plantsConfig = mapper.readValue(new File(plantsPath), new TypeReference<>() {});
        } catch (IOException e) {
            throw new RuntimeException("Cannot load config files", e);
        }
    }

    public void loadFeedingProbabilities(String path) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            feedingProbabilities = mapper.readValue(
                    new File(path),
                    new TypeReference<>() {
                    }
            );
        } catch (IOException e) {
            throw new RuntimeException("Cannot load feeding probabilities from: " + path, e);
        }
    }

    public int getFeedingProbability(String predator, String prey) {
        return feedingProbabilities
                .getOrDefault(predator, Map.of())
                .getOrDefault(prey, 0);
    }

    public IslandConfig getIslandConfig() {
        return islandConfig;
    }

    public SimulationConfig getSimulationConfig() {
        return simulationConfig;
    }

    public AnimalConfig getAnimalConfig(String animalName) {
        AnimalConfig config = animalsConfig.get(animalName);
        if (config == null) {
            throw new IllegalArgumentException("No config found for animal: " + animalName);
        }
        return config;
    }

    public  Map<String, PlantConfig> getPlantsConfig() {
        return plantsConfig;
    }

    public PlantConfig getPlantConfig(String plantName) {
        PlantConfig config = plantsConfig.get(plantName);
        if (config == null) {
            throw new IllegalArgumentException("No config found for plant: " + plantName);
        }
        return config;
    }

    public int getIslandWidth() {
        return islandConfig.getWidth();
    }

    public int getIslandHeight() {
        return islandConfig.getHeight();
    }

    public GameObjectsScanner getGameObjectsScanner() {
        return gameObjectsScanner;
    }


}
