package island.entity.map;

import island.config.AppConfigurator;
import island.entity.organism.Organism;
import island.entity.organism.animal.Animal;
import island.entity.organism.plant.Plant;
import island.factory.OrganismPrototypeFactory;
import island.entity.organism.MoveIntent;
import island.view.StatisticMonitor;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class GameField {
    private enum DominanceMode { DOMINANT_BIOMASS, DOMINANT_COUNT, HEAVIEST_INDIVIDUAL }

    private static final Map<String, String> SPECIES_EMOJI = Map.ofEntries(
            Map.entry("Buffalo", "🐃"),
            Map.entry("Bear", "🐻"),
            Map.entry("Horse", "🐎"),
            Map.entry("Deer", "🦌"),
            Map.entry("Boar", "🐗"),
            Map.entry("Sheep", "🐑"),
            Map.entry("Goat", "🐐"),
            Map.entry("Wolf", "🐺"),
            Map.entry("Boa", "🐍"),
            Map.entry("Fox", "🦊"),
            Map.entry("Eagle", "🦅"),
            Map.entry("Rabbit", "🐇"),
            Map.entry("Duck", "🦆"),
            Map.entry("Mouse", "🐁"),
            Map.entry("Caterpillar", "🐛"),
            Map.entry("Grass", "🌿")
    );

    private int width;
    private int height;

    private Cell[][] cells;
    private final Random random = new Random();
    private final StatisticMonitor stats = new StatisticMonitor();
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private final ExecutorService pool =
            Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    public StatisticMonitor getStats() { return stats; }

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

            // ===== Phase 1: EAT/REPRODUCE  =====
            List<Callable<Void>> phase1 = getAllCells().stream()
                    .<Callable<Void>>map(cell -> () -> { cell.processEatAndReproduce(); return null; })
                    .toList();
            try { pool.invokeAll(phase1); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

            // ===== Phase 2: PLAN MOVES  =====
            ConcurrentLinkedQueue<MoveIntent> intents = new ConcurrentLinkedQueue<>();
            List<Callable<Void>> phase2 = getAllCells().stream()
                    .<Callable<Void>>map(cell -> () -> { cell.computeMoveIntents(intents); return null; })
                    .toList();
            try { pool.invokeAll(phase2); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

            // ===== Phase 3: APPLY MOVES =====
            applyMoveIntents(intents);

            printMiniMap(DominanceMode.DOMINANT_BIOMASS);
            printStatistics();

            if (ticksLeft.decrementAndGet() == 0) {
                executor.shutdown();
                pool.shutdown(); // 🔹 shut down phase pool
                System.out.println("Simulation ended: reached maxTicks");
            }
        }, 0, tactDuration, TimeUnit.MILLISECONDS);
    }

    /** Sequential application of movements - here we perform capacity checks and transfer individuals. */
    private void applyMoveIntents(Queue<MoveIntent> intents) {
        AppConfigurator cfg = AppConfigurator.getInstance();

        for (MoveIntent intent; (intent = intents.poll()) != null; ) {
            Animal a = intent.animal;
            if (a == null || !a.isAlive()) continue;

            // if the animal is no longer in the same cage (died/moved), skip it
            if (a.getCurrentCell() != intent.from) continue;

            String name = a.getName();

            // 🔹species limit check (as with initial spawn)
            int perSpeciesCap = cfg.getAnimalConfig(name).getMaxCountPerCell();
            if (intent.to.countResidentsByName(name) >= perSpeciesCap) {
                continue; //there is no place
            }

            // move the animal
            intent.from.removeResident(a);
            intent.to.addResident(a);
            a.setCurrentCell(intent.to);
        }
    }


    private void printStatistics() {
        Map<String, Integer> population = computePopulation();
        Map<String, Integer> births = stats.getBirthsThisTick();
        Map<String, Integer> deaths = stats.getDeathsThisTick();

        List<String> species = sortedSpecies(population, births, deaths);
        int speciesColWidth  = calcSpeciesColWidth(species);

        System.out.println();
        System.out.println("New day:");
        printTableHeader(speciesColWidth);

        long totalPop = 0, totalBirths = 0, totalDeaths = 0;
        for (String s : species) {
            int pop = population.getOrDefault(s, 0);
            int b = births.getOrDefault(s, 0);
            int d = deaths.getOrDefault(s, 0);

            printTableRow(speciesColWidth, s, pop, b, d);

            totalPop += pop;
            totalBirths += b;
            totalDeaths += d;
        }

        printTableFooter(speciesColWidth, totalPop, totalBirths, totalDeaths);
        System.out.printf("%nTotals since start -> births: %d, deaths: %d%n",
                stats.getTotalBirths(), stats.getTotalDeaths());
    }



    // ---------- helpers ----------

    private Map<String, Integer> computePopulation() {
        Map<String, Integer> population = new HashMap<>();
        for (Cell cell : getAllCells()) {
            for (Organism o : cell.getAllResidents()) {
                population.merge(o.getName(), 1, Integer::sum);
            }
        }
        return population;
    }

    private List<String> sortedSpecies(Map<String, Integer> population,
                                       Map<String, Integer> births,
                                       Map<String, Integer> deaths) {
        Set<String> set = new HashSet<>();
        set.addAll(population.keySet());
        set.addAll(births.keySet());
        set.addAll(deaths.keySet());

        List<String> list = new ArrayList<>(set);
        list.sort(Comparator.comparingInt((String s) -> population.getOrDefault(s, 0)).reversed());
        return list;
    }

    private int calcSpeciesColWidth(List<String> species) {
        int longest = species.stream().mapToInt(String::length).max().orElse(7);
        return Math.max(7, longest); // min "Species"
    }

    private String separator(int speciesColWidth) {
        int len = speciesColWidth + 2 + 10 + 2 + 10 + 2 + 10; // columns + margin
        return "—".repeat(Math.max(0, len));
    }

    private void printTableHeader(int speciesColWidth) {
        String sep = separator(speciesColWidth);
        System.out.println(sep);
        System.out.printf("%-" + speciesColWidth + "s  %10s  %10s  %10s%n",
                "Species", "Population", "Births", "Deaths");
        System.out.println(sep);
    }

    private void printTableRow(int speciesColWidth, String species, int pop, int births, int deaths) {
        System.out.printf("%-" + speciesColWidth + "s  %10d  %10d  %10d%n",
                species, pop, births, deaths);
    }

    private void printTableFooter(int speciesColWidth, long totalPop, long totalBirths, long totalDeaths) {
        String sep = separator(speciesColWidth);
        System.out.println(sep);
        System.out.printf("%-" + speciesColWidth + "s  %10d  %10d  %10d%n",
                "TOTAL", totalPop, totalBirths, totalDeaths);
    }

    private void printMiniMap(DominanceMode mode) {
        System.out.println();
        System.out.println("Mini-map (mode: " + mode + "):");

        for (int y = 0; y < height; y++) {
            StringBuilder row = new StringBuilder();
            for (int x = 0; x < width; x++) {
                Cell cell = cells[x][y];
                String emoji = computeCellEmoji(cell, mode);
                row.append(emoji).append(' ');
            }
            System.out.println(row);
        }

        // Legend
        System.out.println("Legend: 🐃Buffalo 🐻Bear 🐎Horse 🦌Deer 🐗Boar 🐑Sheep 🐐Goat 🐺Wolf 🐍Boa 🦊Fox 🦅Eagle 🐇Rabbit 🦆Duck 🐁Mouse 🐛Caterpillar 🌿Grass · empty");
    }


    private String computeCellEmoji(Cell cell, DominanceMode mode) {
        // collect residents
        var residents = new ArrayList<>(cell.getAllResidents());
        if (residents.isEmpty()) return "·";

        //
        boolean hasAnimals = residents.stream().anyMatch(o -> o instanceof Animal && o.isAlive());

        if (!hasAnimals) {
            // just plant
            boolean hasPlants = residents.stream().anyMatch(o -> o instanceof Plant && o.isAlive());
            return hasPlants ? SPECIES_EMOJI.getOrDefault("Grass", "🌿") : "·";
        }

        switch (mode) {
            case DOMINANT_COUNT:
                return dominantByCount(residents);
            case HEAVIEST_INDIVIDUAL:
                return byHeaviestIndividual(residents);
            case DOMINANT_BIOMASS:
            default:
                return dominantByBiomass(residents);
        }
    }

    private String dominantByBiomass(List<Organism> residents) {
        // sum weight by species
        Map<String, Double> biomass = new HashMap<>();
        for (Organism o : residents) {
            if (o instanceof Animal && o.isAlive()) {
                biomass.merge(o.getName(), o.getCurrentWeight(), Double::sum);
            }
        }
        if (biomass.isEmpty()) {
            // just plant
            boolean hasPlants = residents.stream().anyMatch(o -> o instanceof Plant && o.isAlive());
            return hasPlants ? SPECIES_EMOJI.getOrDefault("Grass", "🌿") : "·";
        }

        /* fixed, predictable rules for resolving a tie (when the scores are equal) so that the outcome is always chosen the same, not randomly.
            In the minimap: if two animals in a cell have  the same biomass, we take the one whose species name is lexicographically larger/smaller (alphabetical order). This means that the same configuration will always give the same emoji*/
        String winner = biomass.entrySet().stream()
                .max(Comparator.<Map.Entry<String, Double>>comparingDouble(Map.Entry::getValue)
                        .thenComparing(e -> e.getKey()))
                .map(Map.Entry::getKey)
                .orElse(null);

        return SPECIES_EMOJI.getOrDefault(winner, "·");
    }

    private String dominantByCount(List<Organism> residents) {
        Map<String, Integer> counts = new HashMap<>();
        for (Organism o : residents) {
            if (o instanceof Animal && o.isAlive()) {
                counts.merge(o.getName(), 1, Integer::sum);
            }
        }
        if (counts.isEmpty()) {
            boolean hasPlants = residents.stream().anyMatch(o -> o instanceof Plant && o.isAlive());
            return hasPlants ? SPECIES_EMOJI.getOrDefault("Grass", "🌿") : "·";
        }
        String winner = counts.entrySet().stream()
                .max(Comparator.<Map.Entry<String, Integer>>comparingInt(Map.Entry::getValue)
                        .thenComparing(e -> e.getKey()))
                .map(Map.Entry::getKey)
                .orElse(null);

        return SPECIES_EMOJI.getOrDefault(winner, "·");
    }

    private String byHeaviestIndividual(List<Organism> residents) {
        Organism heaviest = residents.stream()
                .filter(o -> o instanceof Animal && o.isAlive())
                .max(Comparator.comparingDouble(Organism::getCurrentWeight)
                        .thenComparing(Organism::getName))
                .orElse(null);

        if (heaviest == null) {
            boolean hasPlants = residents.stream().anyMatch(o -> o instanceof Plant && o.isAlive());
            return hasPlants ? SPECIES_EMOJI.getOrDefault("Grass", "🌿") : "·";
        }
        return SPECIES_EMOJI.getOrDefault(heaviest.getName(), "·");
    }
}
