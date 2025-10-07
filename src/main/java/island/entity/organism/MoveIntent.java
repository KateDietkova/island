package island.entity.organism;

import island.entity.map.Cell;
import island.entity.organism.animal.Animal;

public class MoveIntent {
    public final Animal animal;
    public final Cell from;
    public final Cell to;

    public MoveIntent(Animal animal, Cell from, Cell to) {
        this.animal = animal;
        this.from = from;
        this.to = to;
    }
}
