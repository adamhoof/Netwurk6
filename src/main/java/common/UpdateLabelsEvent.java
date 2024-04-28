package common;

import model.PCModel;

public class UpdateLabelsEvent {
    private final PCModel pcModel;

    public UpdateLabelsEvent(PCModel pcModel) {
        this.pcModel = pcModel;
    }

    public PCModel getPcModel() {
        return pcModel;
    }
}
