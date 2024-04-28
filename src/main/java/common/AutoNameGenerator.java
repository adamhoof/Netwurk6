package common;

public class AutoNameGenerator {
    private AutoNameGenerator(){}

    private static int routerNameCounter = 0;
    private static int pcNameCounter = 0;

    private static int switchNameCounter = 0;
    private static int routerInterfaceNameCounter = 0;

    public static String generateRouterName() {
        return "Router" + routerNameCounter++;
    }

    public static String generatePcName() {
        return "PC" + pcNameCounter++;
    }

    public static String generateSwitchName() {
        return "Switch" + switchNameCounter++;
    }

    public static String generateRouterInterfaceName() {
        return "RouterInterface" + routerInterfaceNameCounter++;
    }

    public static void setRouterNameCounter(int routerNameCounter){
        AutoNameGenerator.routerNameCounter=  routerNameCounter;
    }

    public static void setPcNameCounter(int pcNameCounter) {
        AutoNameGenerator.pcNameCounter = pcNameCounter;
    }

    public static void setSwitchNameCounter(int switchNameCounter) {
        AutoNameGenerator.switchNameCounter = switchNameCounter;
    }

    public static void setRouterInterfaceNameCounter(int routerInterfaceNameCounter) {
        AutoNameGenerator.routerInterfaceNameCounter = routerInterfaceNameCounter;
    }

    public static int getRouterNameCounter() {
        return routerNameCounter;
    }

    public static int getPcNameCounter() {
        return pcNameCounter;
    }

    public static int getSwitchNameCounter() {
        return switchNameCounter;
    }

    public static int getRouterInterfaceNameCounter() {
        return routerInterfaceNameCounter;
    }
}
