package common;

import model.PCModel;

public record UpdateLabelsEvent(PCModel pcModel) implements Event{
}
