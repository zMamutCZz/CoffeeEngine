package com.horizon.engine.component.component.mesh;

import com.horizon.engine.common.UtilModel;
import com.horizon.engine.component.Component;
import com.horizon.engine.component.ComponentType;
import com.horizon.engine.graphics.data.Material;
import com.horizon.engine.graphics.texture.Texture;
import lombok.Getter;
import lombok.Setter;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.AbstractMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class DisplayMesh extends Component {

    @Getter private int vaoId;
    @Getter private List<Integer> vboIdList;

    @Getter private int vertexCount;

    @Getter @Setter private Material material;

    @Getter private float[] positions;
    @Getter private float[] textureCoordinates;
    @Getter private int[] indices;

    @Getter private FloatBuffer positionBuffer;
    @Getter private FloatBuffer textureCoordinatesBuffer;
    @Getter private IntBuffer indicesBuffer;

    protected int positionVboId;
    protected int textureVboId;
    protected int indicesVboId;

    public DisplayMesh(){
        super(ComponentType.MESH);
    }

    public DisplayMesh(float[] positions) {
        super(ComponentType.MESH);

        this.positions = positions;

        render(positions);
    }

    public DisplayMesh(float[] positions, float[] textureCoordinates, int[] indices) {
        super(ComponentType.MESH);

        this.positions = positions;
        this.textureCoordinates = textureCoordinates;
        this.indices = indices;

        render(positions, textureCoordinates, indices);
    }

    @Override
    public void update() {
        if(textureVboId == 0) {
            glBindVertexArray(getVaoId());
            glEnableVertexAttribArray(0);

            glDrawArrays(GL_TRIANGLE_STRIP, 0, getVertexCount());

            glDisableVertexAttribArray(0);
            glBindVertexArray(0);
        } else {
            Texture texture = material.getTexture();
            if (texture != null) {
                glActiveTexture(GL_TEXTURE0);
                glBindTexture(GL_TEXTURE_2D, texture.getId());
            }
            glBindVertexArray(getVaoId());

            glEnableVertexAttribArray(0);
            glEnableVertexAttribArray(1);

            glDrawElements(GL_TRIANGLES, getVertexCount(), GL_UNSIGNED_INT, 0);
            glBindVertexArray(0);
            glBindTexture(GL_TEXTURE_2D, 0);
        }
    }

    private void render(float[] positions){
        FloatBuffer positionsBuffer = null;

        try {
            vertexCount = positions.length / 2;
            vboIdList = new LinkedList<>();
            vaoId = glGenVertexArrays();

            glBindVertexArray(vaoId);

            Entry<Integer, FloatBuffer> pair = createResourceBuffers(positions, 0, 2, GL_DYNAMIC_DRAW);
            positionVboId = pair.getKey();
            positionsBuffer = pair.getValue();

            glBindVertexArray(0);
        } finally {
            if (positionsBuffer != null) {
                this.positionBuffer = positionsBuffer;
                MemoryUtil.memFree(positionsBuffer);
            }
        }
    }

    private void render(float[] positions, float[] textureCoordinates, int[] indices){
        FloatBuffer positionsBuffer = null;
        FloatBuffer textureCoordinatesBuffer = null;
        IntBuffer indicesBuffer = null;

        try {
            vertexCount = indices.length;
            vboIdList = new LinkedList<>();
            vaoId = glGenVertexArrays();

            glBindVertexArray(vaoId);

            if(positions != null) {
                Entry<Integer, FloatBuffer> pair = createResourceBuffers(positions, 0, 3, GL_DYNAMIC_DRAW);
                positionVboId = pair.getKey();
                positionsBuffer = pair.getValue();
            } else {
                return;
            }

            if(textureCoordinates != null) {
                Entry<Integer, FloatBuffer> pair = createResourceBuffers(textureCoordinates, 1, 2, GL_STATIC_DRAW);
                textureVboId = pair.getKey();
                textureCoordinatesBuffer = pair.getValue();
            }

            indicesVboId = glGenBuffers();
            vboIdList.add(indicesVboId);
            indicesBuffer = MemoryUtil.memAllocInt(indices.length);
            indicesBuffer.put(indices).flip();
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indicesVboId);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STATIC_DRAW);

            glBindBuffer(GL_ARRAY_BUFFER, 0);
            glBindVertexArray(0);
        } finally {
            if (positionsBuffer != null) {
                this.positionBuffer = positionsBuffer;
                MemoryUtil.memFree(positionsBuffer);
            }
            if (textureCoordinatesBuffer != null) {
                this.textureCoordinatesBuffer = textureCoordinatesBuffer;
                MemoryUtil.memFree(textureCoordinatesBuffer);
            }
            if (indicesBuffer != null) {
                this.indicesBuffer = indicesBuffer;
                MemoryUtil.memFree(indicesBuffer);
            }
        }
    }

    public void updatePositions(float[] positions, int size) {
        try {
            glBindBuffer(GL_ARRAY_BUFFER, positionVboId);
            glBufferSubData(GL_ARRAY_BUFFER, 0, UtilModel.updateFlippedBuffer(positionBuffer, positions, size));
            glBindBuffer(GL_ARRAY_BUFFER, 0);
        } finally {
            this.positions = positions;
        }
    }

    public void cleanUp() {
        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
        glDisableVertexAttribArray(2);
        glBindTexture(GL_TEXTURE_2D, 0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        for (int vboId : vboIdList) {
            glDeleteBuffers(vboId);
        }

        glBindVertexArray(0);
        glDeleteVertexArrays(vaoId);
    }

    public void cleanUpTexture(){
        Texture texture = material.getTexture();
        if (texture != null) {
            texture.cleanup();
        }
    }

    public Entry<Integer, FloatBuffer> createResourceBuffers(float[] bufferData, int index, int size, int drawType) {
        int vboId = glGenBuffers();

        vboIdList.add(vboId);

        FloatBuffer buffer = MemoryUtil.memAllocFloat(bufferData.length);
        buffer.put(bufferData).flip();

        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferData(GL_ARRAY_BUFFER, buffer, drawType);
        glEnableVertexAttribArray(index);
        glVertexAttribPointer(index, size, GL_FLOAT, false, 0, 0);

        return new AbstractMap.SimpleEntry<Integer, FloatBuffer>(vboId, buffer);
    }

    public boolean isTextured() {
        return this.material != null;
    }
}
