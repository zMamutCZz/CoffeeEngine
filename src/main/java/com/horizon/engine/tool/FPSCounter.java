package com.horizon.engine.tool;

import com.horizon.engine.GameEngine;
import com.horizon.engine.component.component.hud.text.TextFont;
import com.horizon.engine.graphics.hud.orientations.AlignmentDistance;
import com.horizon.engine.graphics.hud.orientations.AlignmentPercentage;
import com.horizon.engine.graphics.hud.other.DisplayAnchor;
import com.horizon.engine.graphics.hud.text.TextView;
import lombok.Getter;

public class FPSCounter {

    @Getter private final GameEngine gameEngine;
    @Getter private final TextView fpsCounterText;

    public FPSCounter(GameEngine gameEngine){
        this.gameEngine = gameEngine;

        fpsCounterText = getGameEngine().getCanvas().instantiateText("FPS Counter", " FPS", TextFont.IMPACT, DisplayAnchor.TOP);
        fpsCounterText.setSize(16);
        fpsCounterText.addAlignmentData(new AlignmentPercentage(fpsCounterText, -95, 0));
        fpsCounterText.addAlignmentData(new AlignmentDistance(fpsCounterText, 0, 25));
    }

    public void update(){
        String currentText = getGameEngine().getLastFramesPerSecond() + " FPS";

        if(getFpsCounterText().getTextComponent().getText().equalsIgnoreCase(currentText))
            return;

        getFpsCounterText().setText(currentText);
    }
}
