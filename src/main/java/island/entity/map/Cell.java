package island.entity.map;

import island.entity.organism.Organism;
import island.entity.organism.animal.Animal;
import island.entity.organism.animal.Gender;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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

    public Map<String, Set<Organism>> getResidents() {
        return residents;
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
            //System.out.println("Removed. Current cell size - " + getAllResidents().size()) ;
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
