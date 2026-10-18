package lando.systems.ld60;

import com.zendogames.zenlib.Flag;

import static com.zendogames.zenlib.Flag.Kind.DEBUG;
import static com.zendogames.zenlib.Flag.Kind.FEATURE;

public enum GameFlags implements Flag {
    CRT_SHADER           (true,  DEBUG) // master switch
    ;

    private final Kind kind;
    private final State state;

    GameFlags(boolean enabledByDefault, Kind kind) {
        this.kind = kind;
        this.state = new State(this, enabledByDefault);
    }

    @Override public Kind  kind()  { return kind; }
    @Override public State state() { return state; }
}

