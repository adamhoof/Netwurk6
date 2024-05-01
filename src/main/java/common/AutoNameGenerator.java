package common;

import com.google.common.eventbus.Subscribe;

public class AutoNameGenerator {
    private static final AutoNameGenerator instance = new AutoNameGenerator();

    private int routerNameCounter = 0;
    private int pcNameCounter = 0;
    private int switchNameCounter = 0;
    private int routerInterfaceNameCounter = 0;

    private AutoNameGenerator() {
    }

    public static AutoNameGenerator getInstance() {
        return instance;
    }

    public static void registerListener(){
        GlobalEventBus.register(getInstance());
    }

    public String generateRouterName() {
        return "Router" + routerNameCounter++;
    }

    public String generatePcName() {
        return "PC" + pcNameCounter++;
    }

    public String generateSwitchName() {
        return "Switch" + switchNameCounter++;
    }

    public String generateRouterInterfaceName() {
        return "RouterInterface" + routerInterfaceNameCounter++;
    }

    public void setRouterNextAvailableNumber(int value) {
        this.routerNameCounter = value;
    }

    public void setPcNextAvailableNumber(int value) {
        this.pcNameCounter = value;
    }

    public void setSwitchNextAvailableNumber(int value) {
        this.switchNameCounter = value;
    }

    public void setRouterInterfaceNextAvailableNumber(int value) {
        this.routerInterfaceNameCounter = value;
    }

    public int getRouterNameCounter() {
        return routerNameCounter;
    }

    public int getPcNameCounter() {
        return pcNameCounter;
    }

    public int getSwitchNameCounter() {
        return switchNameCounter;
    }

    public int getRouterInterfaceNameCounter() {
        return routerInterfaceNameCounter;
    }

    @Subscribe
    public void handleDecrementNameCounterRequestEvent(DecrementNameCounterRequestEvent event) {
        switch (event.type()) {
            case ROUTER -> setRouterNextAvailableNumber(getRouterNameCounter() - 1);
            case ROUTER_INTERFACE -> setRouterInterfaceNextAvailableNumber(getRouterInterfaceNameCounter() - 1);
            case SWITCH -> setSwitchNextAvailableNumber(getSwitchNameCounter() - 1);
            case PC -> setPcNextAvailableNumber(getPcNameCounter() - 1);
        }
    }
}
