package common;

import model.RouterInterface;

public record RouterInterfaceCreatedEvent(RouterInterface routerInterface) implements Event {
}
