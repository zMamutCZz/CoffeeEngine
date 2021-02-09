package com.horizon.engine.data;

import com.horizon.engine.common.Color;
import lombok.Getter;
import lombok.Setter;
import org.joml.Vector2f;

public class ApplicationSetting {

    @Getter @Setter private static String charset = "ISO-8859-1";

    //Window settings
    @Getter @Setter private static String windowTitle = "Test Game";
    @Getter @Setter private static boolean vSync = true;
    @Getter @Setter private static Vector2f windowSize = new Vector2f(1080,720);

    //Color Pallet
    @Getter @Setter private static Color mainBackground = new Color(50.0f, 50.0f, 50.0f, 200.0f);
    @Getter @Setter private static Color subBackground = new Color(60.0f, 60.0f, 60.0f, 255.0f);
    @Getter @Setter private static Color topBar = new Color(255.0f, 210.0f, 130.0f, 255.0f);
    @Getter @Setter private static Color accept = new Color(0.0f, 160.0f, 0.0f, 255.0f);
    @Getter @Setter private static Color deny = new Color(160.0f, 0.0f, 0.0f, 255.0f);
}
