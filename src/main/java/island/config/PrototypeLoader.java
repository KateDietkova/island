package island.config;

public class PrototypeLoader {

    private static PrototypeLoader instance;

    private PrototypeLoader() {}

    public static PrototypeLoader getInstance() {
        if (instance == null) {
            instance = new PrototypeLoader();
        }
        return instance;
    }
}
