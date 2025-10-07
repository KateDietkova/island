package island.entity.map;

import island.entity.organism.Organism;
import island.entity.organism.animal.Animal;
import island.entity.organism.animal.Gender;
import island.entity.organism.plant.Plant;
import island.entity.organism.MoveIntent;

import java.util.*;

public class Cell {

    private final Map<String, Set<Organism>> residents;
    private final int x;
    private final int y;
    private final GameField field;


    public Cell(GameField field, int x, int y) {
        this.residents = new HashMap<>();
        this.field = field;
        this.x = x;
        this.y = y;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public GameField getField() { return field; }

    /** Phase 1 without move */
    public void processEatAndReproduce() {
        List<Organism> copy = new ArrayList<>(getAllResidents());
        // random events in the cell
        Collections.shuffle(copy, new Random());

        for (Organism o : copy) {
            if (!o.isAlive()) continue;
            if (o instanceof Animal a) {
                a.actInPlaceOneDay();      // without move
            } else if (o instanceof Plant p) {
                p.liveOneDay();
            }
        }
    }

    /** Phase 2 just collect move intent */
    public void computeMoveIntents(Queue<MoveIntent> out) {
        for (Organism o : new ArrayList<>(getAllResidents())) {
            if (!(o instanceof Animal a) || !a.isAlive()) continue;
            MoveIntent intent = a.computeMoveIntent();
            if (intent != null) out.add(intent);
        }
    }


    public Set<Organism> getAllResidents() {
        Set<Organism> all = new HashSet<>();
        residents.values().forEach(all::addAll);
        return all;
    }

    public void addResident(Organism organism) {
        residents.computeIfAbsent(organism.getName(), k -> new HashSet<>()).add(organism);
    }

    public int countResidentsByName(String name) {
        return residents.getOrDefault(name, Set.of()).size();
    }

    public void removeResident(Organism organism) {
        if (residents.containsKey(organism.getName())) {
            residents.get(organism.getName()).remove(organism);
            if (residents.get(organism.getName()).isEmpty()) {
                residents.remove(organism.getName());
            }
        }
    }

    public Animal findMate(String speciesName, Gender desiredGender, Animal exclude) {
        var set = residents.getOrDefault(speciesName, Set.of());
        for (Organism o : set) {
            if (o == exclude || !o.isAlive()) continue;
            if (o instanceof Animal a) {
                if (a.getGender() == desiredGender && a.isAdult()) {
                    return a;
                }
            }
        }
        return null;
    }
}
