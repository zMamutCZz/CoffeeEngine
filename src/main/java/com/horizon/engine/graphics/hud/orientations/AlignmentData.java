package com.horizon.engine.graphics.hud.orientations;

import com.horizon.engine.graphics.hud.HudObject;
import lombok.Getter;

public abstract class AlignmentData {

    @Getter private final HudObject hudObject;

    public abstract float getX();
    public abstract float getY();

    public AlignmentData(HudObject hudObject){
        this.hudObject = hudObject;
    }
}
