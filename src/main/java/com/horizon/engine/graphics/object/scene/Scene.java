package com.horizon.engine.graphics.object.scene;

import com.horizon.engine.GameEngine;
import com.horizon.engine.common.Color;
import com.horizon.engine.component.Component;
import com.horizon.engine.component.ComponentType;
import com.horizon.engine.component.component.light.DirectionalLightComponent;
import com.horizon.engine.component.component.light.PointLightComponent;
import com.horizon.engine.component.component.light.SpotLightComponent;
import com.horizon.engine.data.ApplicationData;
import com.horizon.engine.graphics.light.DirectionalLight;
import com.horizon.engine.graphics.light.PointLight;
import com.horizon.engine.graphics.light.SpotLight;
import com.horizon.engine.graphics.object.Camera;
import com.horizon.engine.graphics.object.GameObject;
import com.horizon.engine.graphics.object.objects.ModelObject;
import com.horizon.engine.graphics.object.primitive.PrimitiveObject;
import com.horizon.engine.graphics.object.terrain.Terrain;
import com.horizon.engine.graphics.postprocessing.Fog;
import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

public @Data class Scene {

    private final GameEngine gameEngine;

    private Map<String, GameObject> sceneObjects = new LinkedHashMap<>();
    private Terrain terrain;

    private Camera sceneCamera;
    private SceneLight sceneLight;

    private boolean renderShadows = true;
    private Fog fog = Fog.NOFOG;

    public Scene(GameEngine gameEngine){
        this.gameEngine = gameEngine;
        this.sceneLight = new SceneLight();

        getSceneLight().setDirectionalLight(new DirectionalLight(gameEngine, new Color(240.0f, 230.0f, 180.0f)));

        DirectionalLightComponent directionalLightComponent = getSceneLight().getDirectionalLight().getDirectionalLight();

        directionalLightComponent.setShadowPosMulti(10);
        directionalLightComponent.setOrthographicCoordinates(-10.0f, 10.0f, -10.0f, 10.0f, -1.0f, 20.0f);
    }

    public boolean initialize(){
        sceneCamera = new Camera();

        setFog(new Fog(true, ApplicationData.getDefaultFogColor().toVector3f(), ApplicationData.getFogDensity()));
        getGameEngine().getWindow().setClearColor(sceneCamera.getBackgroundColor());

        return true;
    }

    /**
     * Instantiate function provides initialization of inserted gameobject.
     * Object is automatically inserted into asset manager if that gameobject
     * contains mesh.
     * @param gameObject - GameObject that will be initialized.
     * @return - Inserted GameObject.
     */
    public GameObject instantiate(GameObject gameObject) {
        if(gameObject.getComponents().containsKey(ComponentType.LIGHT)) {
            Component component = gameObject.getComponents().get(ComponentType.LIGHT);
            if(component instanceof DirectionalLightComponent) {
                getSceneLight().setDirectionalLight((DirectionalLight) gameObject);
            }

            if(component instanceof SpotLightComponent) {
                getSceneLight().addSpotLight((SpotLight) gameObject);
            }

            if(component instanceof PointLightComponent) {
                getSceneLight().addPointLight((PointLight) gameObject);
            }
        }

        gameObject.setGameObjectName(sceneObjects.containsKey(gameObject.getGameObjectName()) ? generateObjectName(gameObject.getGameObjectName()) : gameObject.getGameObjectName());
        sceneObjects.put(gameObject.getGameObjectName(), gameObject);

        if(gameObject instanceof Terrain) {
            terrain = (Terrain) gameObject;
            return gameObject;
        }

        getGameEngine().getAssetManager().addMesh(gameObject);

        return gameObject;
    }

    /**
     * This function is used for creation of primitive object like cube,
     * cone, ball, plane, etc.
     * @param primitiveObject - Type of primitive object.
     * @return - GameObject (Model Object)
     */
    public ModelObject instantiate(PrimitiveObject primitiveObject) {
        return (ModelObject) instantiate(getGameEngine().getAssetManager().getModel(primitiveObject.getMeshName()).instantiateObject());
    }

    public String generateObjectName(String name) {
        String generatedPrefabName;
        int index = 1;

        while(true) {
            generatedPrefabName = name + "(" + index + ")";
            if(!sceneObjects.containsKey(generatedPrefabName))
                break;
            else
                index++;
        }

        return generatedPrefabName;
    }

    public GameObject getGameObjectByName(String name){
        return sceneObjects.get(name);
    }
}
