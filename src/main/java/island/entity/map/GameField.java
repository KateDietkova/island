package island.entity.map;

import island.config.AppConfigurator;
import island.entity.organism.Organism;
import island.entity.organism.animal.Animal;
import island.entity.organism.plant.Plant;
import island.factory.OrganismPrototypeFactory;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class GameField {

    private int width;
    private int height;

    private Cell[][] cells;
    private final Random random = new Random();
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    public GameField(int width, int height) {
        this.width = width;
        this.height = height;
        this.cells = new Cell[width][height];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                cells[x][y] = new Cell(this, x, y);
            }
        }

        populateAnimals();
        populatePlants();
    }


    private void populateAnimals() {
        AppConfigurator config = AppConfigurator.getInstance();

        Map<String, Integer> initialAnimals = config.getIslandConfig().getInitialAnimals();

        initialAnimals.forEach((animalName, count) -> {
            try {
                for (int i = 0; i < count; i++) {
                    Organism organism = OrganismPrototypeFactory.getInstance().createOrganism(config.getAnimalConfig(animalName), animalName);

                    // random cell
                    int x = random.nextInt(width);
                    int y = random.nextInt(height);
                    Cell cell = cells[x][y];

                    //Check max count animal per cell and add them to it if it's possible
                    if (organism instanceof Animal animal) {
                        int maxCount = AppConfigurator
                                .getInstance()
                                .getAnimalConfig(animalName)
                                .getMaxCountPerCell();

                        if (cell.countResidentsByName(animalName) < maxCount) {
                            cell.addResident(organism);
                            animal.setCurrentCell(cell);
                        } else {
                            System.out.println("Cell is full for " + animalName);
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Couldn't add " + animalName + ": " + e.getMessage());
            }
        });
    }

    private void populatePlants() {
        AppConfigurator config = AppConfigurator.getInstance();
        Map<String, Integer> initialPlants = config.getIslandConfig().getInitialPlants();

        initialPlants.forEach((plantName, count) -> {
            try {
                for (int i = 0; i < count; i++) {
                    Organism plant = OrganismPrototypeFactory.getInstance().createOrganism(config.getPlantConfig(plantName), plantName);

                    int x = random.nextInt(width);
                    int y = random.nextInt(height);
                    Cell cell = cells[x][y];

                    cell.addResident(plant);
                    plant.setCurrentCell(cell);
                }
            } catch (Exception e) {
                System.err.println("Couldn't add " + plantName + ": " + e.getMessage());
            }
        });
    }

    private void growPlants() {
        for (Cell cell : getAllCells()) {
            // Copying the list to avoid ConcurrentModificationException
            List<Organism> residents = new ArrayList<>(cell.getAllResidents());
            for (Organism o : residents) {
                if (o instanceof Plant plant) {
                    // We call the method, which itself checks maxPerCell and adds offspring to the cell
                    plant.reproduceIfPossible();
                }
            }
        }
    }

    public void startSimulation() {
        AppConfigurator config = AppConfigurator.getInstance();
        int tactDuration = config.getSimulationConfig().getTactDurationMillis();
        AtomicInteger ticksLeft = new AtomicInteger(config.getSimulationConfig().getMaxTicks());

        executor.scheduleAtFixedRate(() -> {
            growPlants();
            for (Cell cell : getAllCells()) {
                //iteration by list copy
                for (Organism o : new ArrayList<>(cell.getAllResidents())) {
                    if (o instanceof Animal a && a.isAlive()) a.liveOneDay();
                    else if (o instanceof Plant p && p.isAlive()) p.liveOneDay();
                    else o.die();
                }
            }
            printStatistics();

            if (ticksLeft.decrementAndGet() == 0) {
                executor.shutdown();
                System.out.println("Simulation ended: reached maxTicks");
            }
        }, 0, tactDuration, TimeUnit.MILLISECONDS);
    }


    private void printStatistics() {
        Map<String, Integer> counts = new HashMap<>();
        for (Cell cell : getAllCells()) {
            for (Organism o : cell.getAllResidents()) {
                counts.merge(o.getName(), 1, Integer::sum);
            }
        }
        System.out.println("Statistic per day: " + counts);
    }

    public Cell getCell(int x, int y) {
        return cells[x][y];
    }

    public List<Cell> getAllCells() {
        return Arrays.stream(cells)
                .flatMap(Arrays::stream)
                .toList();
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
