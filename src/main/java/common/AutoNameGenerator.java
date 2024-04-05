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
}
