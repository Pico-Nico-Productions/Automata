package pro.piconico.automata.client.input;

public enum MouseButton {
    LEFT(0), RIGHT(1), MIDDLE(2), BACK(3), FORWARD(4);

    public final int button;

    private MouseButton(int button) {
        this.button = button;
    }
}
