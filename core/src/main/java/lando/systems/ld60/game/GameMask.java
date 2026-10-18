package lando.systems.ld60.game;

import com.zendogames.zenlib.collision.CollisionMask;

public enum GameMask implements CollisionMask {
    SOLID, PLAYER, ENEMY, TRIGGER, HAZARD, PICKUP;

    @Override public int bit() { return 1 << ordinal(); }
}
