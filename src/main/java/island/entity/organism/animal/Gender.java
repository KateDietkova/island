package island.entity.organism.animal;

public enum Gender {
    MALE,
    FEMALE;

    public static Gender randomGender() {
        return Math.random() < 0.5 ? MALE : FEMALE;
    }
    public Gender opposite() {
        return this == MALE ? FEMALE : MALE;
    }

}
