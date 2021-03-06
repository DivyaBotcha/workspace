package com.example.android.opengl30;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

//import javax.microedition.khronos.opengles.GL10;


public class Cube {

    private FloatBuffer vertexBuffer;  // Buffer for vertex-array
    private ShortBuffer indexBuffer;
    private int numFaces = 6;
    private int colorHandle;
    private final String vertexShaderCode =
            "uniform mat4 uMVPMatrix;" +
                    "attribute vec4 vPosition;" +
                    "void main() {" +
                    "  gl_Position = uMVPMatrix * vPosition;" +
                    "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor = vColor;" +
                    "}";
    private int MVPMatrixHandle;
    private int positionHandle;
    private final int program;

    static final int COORDS_PER_VERTEX = 3;
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    private float[][] colors = {  // Colors of the 6 faces
            {1.0f, 0.5f, 0.0f, 1.0f},  // 0. orange
            {1.0f, 0.0f, 1.0f, 1.0f},  // 1. violet
            {0.0f, 1.0f, 0.0f, 1.0f},  // 2. green
            {0.0f, 0.0f, 1.0f, 1.0f},  // 3. blue
            {1.0f, 0.0f, 0.0f, 1.0f},  // 4. red
            {1.0f, 1.0f, 0.0f, 1.0f}   // 5. yellow
    };

    private float[] vertices = {  // Vertices of the 6 faces
            // FRONT
/*            -0.3f, -0.3f, 0.3f,  // 0. left-bottom-front
            0.3f, -0.3f, 0.3f,  // 1. right-bottom-front
            -0.3f, 0.3f, 0.3f,  // 2. left-top-front
            0.3f, 0.3f, 0.3f,  // 3. right-top-front
            // BACK
            0.3f, -0.3f, -0.3f,  // 6. right-bottom-back
            -0.3f, -0.3f, -0.3f,  // 4. left-bottom-back
            0.3f, 0.3f, -0.3f,  // 7. right-top-back
            -0.3f, 0.3f, -0.3f,  // 5. left-top-back
            // LEFT
            -0.3f, -0.3f, -0.3f,  // 4. left-bottom-back
            -0.3f, -0.3f, 0.3f,  // 0. left-bottom-front
            -0.3f, 0.3f, -0.3f,  // 5. left-top-back
            -0.3f, 0.3f, 0.3f,  // 2. left-top-front
            // RIGHT
            0.3f, -0.3f, 0.3f,  // 1. right-bottom-front
            0.3f, -0.3f, -0.3f,  // 6. right-bottom-back
            0.3f, 0.3f, 0.3f,  // 3. right-top-front
            0.3f, 0.3f, -0.3f,  // 7. right-top-back
            // TOP
            -0.3f, 0.3f, 0.3f,  // 2. left-top-front
            0.3f, 0.3f, 0.3f,  // 3. right-top-front
            -0.3f, 0.3f, -0.3f,  // 5. left-top-back
            0.3f, 0.3f, -0.3f,  // 7. right-top-back
            // BOTTOM
            -0.3f, -0.3f, -0.3f,  // 4. left-bottom-back
            0.3f, -0.3f, -0.3f,  // 6. right-bottom-back
            -0.3f, -0.3f, 0.3f,  // 0. left-bottom-front
            0.3f, -0.3f, 0.3f   // 1. right-bottom-front
*/

            -0.3f, -0.3f, -0.3f,
            0.3f, -0.3f, -0.3f,
            0.3f,  0.3f, -0.3f,
            -0.3f, 0.3f, -0.3f,
            -0.3f, -0.3f,  0.3f,
            0.3f, -0.3f,  0.3f,
            0.3f,  0.3f,  0.3f,
            -0.3f,  0.3f,  0.3f
    };


    short[] indeces = {
/*            0, 1, 2, 2, 1, 3,
            6, 7, 4, 4, 5, 7,
            4, 0, 2, 2, 5, 4,
            1, 6, 7, 7, 3, 1,
            2, 3, 7, 7, 5, 2,
            4, 6, 1, 1, 0, 4*/
          0, 4, 5, 0, 5, 1,
          1, 5, 6, 1, 6, 2,
          2, 6, 7, 2, 7, 3,
          3, 7, 4, 3, 4, 0,
          4, 7, 6, 4, 6, 5,
          3, 0, 1, 3, 1, 2


  };

    // Constructor - Set up the buffers
    public Cube() {
        // Setup vertex-array buffer. Vertices in float. An float has 4 bytes
        ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);
        vbb.order(ByteOrder.nativeOrder()); // Use native byte order
        vertexBuffer = vbb.asFloatBuffer(); // Convert from byte to float
        vertexBuffer.put(vertices);         // Copy data into buffer
        vertexBuffer.position(0);           // Rewind

        indexBuffer = ByteBuffer.allocateDirect(indeces.length * 4).order(ByteOrder.nativeOrder()).asShortBuffer();
        indexBuffer.put(indeces).position(0);

        int vertexShader = MyGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = MyGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program, vertexShader);
        GLES20.glAttachShader(program, fragmentShader);
        GLES20.glLinkProgram(program);
    }

    // Draw the shape
    public void draw(float[] mvpMatrix) {
        GLES20.glUseProgram(program);

        positionHandle = GLES20.glGetAttribLocation(program, "vPosition");
        GLES20.glEnableVertexAttribArray(positionHandle);
        GLES20.glVertexAttribPointer(positionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);

        MVPMatrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix");
        GLES20.glUniformMatrix4fv(MVPMatrixHandle, 1, false, mvpMatrix, 0);
        // Render all the faces


        // can be moved outside the for loop
        colorHandle = GLES20.glGetUniformLocation(program, "vColor");

// Render all the faces
        for (int face = 0; face < numFaces; face++) {
            // Set the color for each of the faces
            GLES20.glUniform4fv(colorHandle, 0, colors[face], 0);
            // update the position of the index buffer (6 indices per drawn face)
            indexBuffer.position(face * 6);
            // draw each face by using the index buffer
            GLES20.glDrawElements(GLES20.GL_TRIANGLES, 6, GLES20.GL_UNSIGNED_SHORT, indexBuffer);
        }


/*
       for (int face = 0; face < numFaces; face++) {
            // Set the color for each of the faces
            colorHandle = GLES20.glGetUniformLocation(program, "vColor");
            GLES20.glUniform4fv(colorHandle, 1, colors[face], 0);
        }

       GLES20.glDrawElements(GLES20.GL_TRIANGLES, 36, GLES20.GL_UNSIGNED_SHORT, indexBuffer);
*/
        GLES20.glDisableVertexAttribArray(positionHandle);
    }
}

