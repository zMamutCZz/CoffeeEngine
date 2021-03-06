package com.horizon.engine;

import com.google.common.flogger.FluentLogger;
import com.horizon.engine.asset.AssetManager;
import com.horizon.engine.debug.Debugger;
import com.horizon.engine.event.EventManager;
import com.horizon.engine.graphics.hud.Canvas;
import com.horizon.engine.graphics.object.scene.Scene;
import com.horizon.engine.hud.HudManager;
import com.horizon.engine.input.InputManager;
import com.horizon.engine.input.other.MouseInput;
import com.horizon.engine.model.ModelManager;
import com.horizon.engine.tool.ToolManager;
import com.horizon.game.DummyGame;
import lombok.Getter;

public class GameEngine implements Runnable {

    public static final int TARGET_FPS = 75;
    public static final int TARGET_UPS = 30;

    @Getter private static final FluentLogger logger = FluentLogger.forEnclosingClass();
    @Getter private Debugger debugger;

    @Getter private final Window window;
    @Getter private final Timer timer;
    @Getter private final AbstractGameLogic gameLogic;

    //Managers
    @Getter private final MouseInput mouseInput;

    @Getter private InputManager inputManager;
    @Getter private ModelManager modelManager;
    @Getter private ToolManager toolManager;
    @Getter private EventManager eventManager;
    @Getter private AssetManager assetManager;
    @Getter private HudManager hudManager;

    @Getter private long lastTime = System.currentTimeMillis();
    @Getter private long currentFramesPerSecond = 0;
    @Getter private long lastFramesPerSecond = 0;

    public GameEngine(String windowTitle, int width, int height, boolean vSync, AbstractGameLogic gameLogic) throws Exception {
        window = new Window(this, windowTitle, width, height, vSync);
        mouseInput = new MouseInput();
        this.gameLogic = gameLogic;
        timer = new Timer();
        this.debugger = new Debugger(this);
    }

    @Override
    public void run() {
        try {
            initialize();
            gameLoop();
        } catch (Exception excp) {
            excp.printStackTrace();
        } finally {
            cleanup();
        }
    }

    protected void initialize() throws Exception {
        //Initializations
        window.init();
        timer.init();
        mouseInput.init(window);

        //Managers
        assetManager = new AssetManager(this);
        inputManager = new InputManager(this);
        modelManager = new ModelManager(this);
        eventManager = new EventManager(this);

        loadAssets();

        //Final initialization
        gameLogic.setGameEngine(this);
        gameLogic.onEnable();

        postInitialize();
    }

    protected void postInitialize() {
        hudManager = new HudManager(this);
        toolManager = new ToolManager(this);

        toolManager.initialize();
    }

    protected void loadAssets() {
        assetManager.initialize();

        loadFonts();
    }

    protected void loadFonts() {
        AssetManager.loadFont("Baba.otf");
        AssetManager.loadFont("ModernSans-Light.otf");
    }

    protected void gameLoop() {
        float elapsedTime;
        float accumulator = 0f;
        float interval = 1f / TARGET_UPS;

        boolean running = true;

        while (running && !window.windowShouldClose()) {
            long startDeltaTime = System.currentTimeMillis();
            elapsedTime = timer.getElapsedTime();
            accumulator += elapsedTime;

            input();

            while (accumulator >= interval) {
                update(interval);
                accumulator -= interval;
            }

            render();

            if (!window.isvSync()) {
                sync();
            }

            updateFramesPerSecond();

            startDeltaTime = System.currentTimeMillis() - startDeltaTime;
            timer.setDeltaTime(startDeltaTime / 1000.0f);
        }
    }

    private void sync() {
        float loopSlot = 1f / TARGET_FPS;
        double endTime = timer.getLastLoopTime() + loopSlot;
        while (timer.getTime() < endTime) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException ignored) {
            }
        }
    }

    protected void input() {
        mouseInput.input(window);
        gameLogic.onInput(window, mouseInput);
    }

    protected void update(float interval) {
        gameLogic.onUpdate(interval, mouseInput);
        getHudManager().getFpsCounter().update();
    }

    protected void render() {
        gameLogic.onRender(window);
        window.update();
    }

    protected void cleanup() {
        gameLogic.cleanup(getGameLogic().getRenderer());

        gameLogic.onDisable();
    }

    protected void updateFramesPerSecond() {
        if (lastTime + 1000 < getTime()) {
            lastFramesPerSecond = currentFramesPerSecond;
            currentFramesPerSecond = 0;
            lastTime = getTime();
        }

        currentFramesPerSecond++;
    }

    public long getTime() {
        return System.currentTimeMillis();
    }

    public Scene getScene() {
        return getGameLogic().getScene();
    }

    public Canvas getCanvas() {
        return getGameLogic().getCanvas();
    }
}
