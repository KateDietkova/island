package island;

import island.config.AppConfigurator;
import island.entity.map.GameField;

public class Main {
    public static void main(String[] args) {
        AppConfigurator appConfigurator = AppConfigurator.getInstance();
        appConfigurator.init();

        int width = appConfigurator.getIslandWidth();
        int height = appConfigurator.getIslandHeight();
        GameField field = new GameField(width, height);
        field.startSimulation();
    }
}