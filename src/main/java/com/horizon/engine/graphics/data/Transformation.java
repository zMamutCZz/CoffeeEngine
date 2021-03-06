package com.horizon.engine.graphics.data;

import com.horizon.engine.graphics.object.Camera;
import com.horizon.engine.graphics.object.GameObject;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class Transformation {

    private final Matrix4f projectionMatrix;
    private final Matrix4f modelMatrix;
    private final Matrix4f modelViewMatrix;
    private final Matrix4f modelLightViewMatrix;
    private final Matrix4f orthographicProjectionMatrix;
    private final Matrix4f ortho2DMatrix;
    private final Matrix4f orthoModelMatrix;

    public Transformation() {
        projectionMatrix = new Matrix4f();
        modelMatrix = new Matrix4f();
        modelViewMatrix = new Matrix4f();
        modelLightViewMatrix = new Matrix4f();
        orthographicProjectionMatrix = new Matrix4f();
        ortho2DMatrix = new Matrix4f();
        orthoModelMatrix = new Matrix4f();
    }

    public final Matrix4f getOrthographicProjectionMatrix() {
        return orthographicProjectionMatrix;
    }

    public Matrix4f updateOrthographicProjectionMatrix(float left, float right, float bottom, float top, float zNear, float zFar) {
        return orthographicProjectionMatrix.setOrtho(left, right, bottom, top, zNear, zFar);
    }

    public static  Matrix4f updateGenericViewMatrix(Vector3f position, Vector3f rotation, Matrix4f matrix) {
        return matrix.rotationX((float) Math.toRadians(rotation.x))
                .rotateY((float) Math.toRadians(rotation.y))
                .translate(-position.x, -position.y, -position.z);
    }

    public final Matrix4f getOrtho2DProjectionMatrix(float left, float right, float bottom, float top) {
        return ortho2DMatrix.setOrtho2D(left, right, bottom, top);
    }

    public Matrix4f buildModelMatrix(GameObject gameObject) {
        Vector3f position = gameObject.getPosition();
        Vector3f scale = gameObject.getTransform().getScale();
        Quaternionf rotation = gameObject.getTransform().getRotation();
        return modelMatrix.translationRotateScale(position.x(), position.y(), position.z(), rotation.x(), rotation.y(), rotation.z(), rotation.w(), scale.x(), scale.y(), scale.z());
    }

    public Matrix4f buildModelViewMatrix(GameObject gameItem, Matrix4f viewMatrix) {
        return buildModelViewMatrix(buildModelMatrix(gameItem), viewMatrix);
    }

    public Matrix4f buildModelViewMatrix(Matrix4f modelMatrix, Matrix4f viewMatrix) {
        return viewMatrix.mulAffine(modelMatrix, modelViewMatrix);
    }

    public Matrix4f buildModelLightViewMatrix(GameObject gameItem, Matrix4f lightViewMatrix) {
        return buildModelViewMatrix(buildModelMatrix(gameItem), lightViewMatrix);
    }

    public Matrix4f buildModelLightViewMatrix(Matrix4f modelMatrix, Matrix4f lightViewMatrix) {
        return lightViewMatrix.mulAffine(modelMatrix, modelLightViewMatrix);
    }

    public Matrix4f buildOrthographicProjectionModelMatrix(GameObject gameItem, Matrix4f orthoMatrix) {
        return orthoMatrix.mulOrthoAffine(buildModelMatrix(gameItem), orthoModelMatrix);
    }
}
