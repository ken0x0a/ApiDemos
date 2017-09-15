/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.apis.graphics;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.os.SystemClock;
import android.util.Log;

import com.example.android.apis.R;

/**
 * Draws a textured rotating triangle using OpenGL ES 2.0
 */
@SuppressWarnings({"FieldCanBeLocal", "WeakerAccess"})
class GLES20TriangleRenderer implements GLSurfaceView.Renderer {
    /**
     * Size in bytes of a float value.
     */
    private static final int FLOAT_SIZE_BYTES = 4;
    /**
     * Stride in bytes for triangle vertex data. (3 float (x,y,z) coordinates and 2 texture coordinates).
     */
    private static final int TRIANGLE_VERTICES_DATA_STRIDE_BYTES = 5 * FLOAT_SIZE_BYTES;
    /**
     * Offset for the vertex (x,y,z) coordinates.
     */
    private static final int TRIANGLE_VERTICES_DATA_POS_OFFSET = 0;
    /**
     * Offset for the texture coordinates.
     */
    private static final int TRIANGLE_VERTICES_DATA_UV_OFFSET = 3;
    /**
     * Vertex data for the three vertices of our triangle.
     */
    private final float[] mTriangleVerticesData = {
            // X, Y, Z, U, V
            -1.0f, -0.5f, 0, -0.5f, 0.0f,
            1.0f, -0.5f, 0, 1.5f, -0.0f,
            0.0f, 1.11803399f, 0, 0.5f, 1.61803399f};

    /**
     * {@code FloatBuffer} that we load {@code mTriangleVerticesData} into then use in a call to
     * {@code glVertexAttribPointer} to define an array of generic vertex attribute data both for
     * {@code maPositionHandle} (the location of the attribute variable "aPosition" in our compiled
     * program object {@code mProgram}) and {@code maTextureHandle} (the location of the attribute
     * variable "aTextureCoord" in our compiled program object {@code mProgram})
     */
    private FloatBuffer mTriangleVertices;

    /**
     * Source code for the GL_VERTEX_SHADER part of our shader program {@code mProgram} (shader that
     * is intended to run on the programmable vertex processor). The statements meanings:
     * <ul>
     *     <li>
     *         uniform mat4 uMVPMatrix; # A {@code uniform} is a global openGL Shading Language variable
     *         declared with the "uniform" storage qualifier. These act as parameters that the user of a
     *         shader program can pass to that program. They are stored in a program object. Uniforms
     *         are so named because they do not change from one execution of a shader program to the next
     *         within a particular rendering call. This makes them unlike shader stage inputs and outputs,
     *         which are often different for each invocation of a program stage. A {@code mat4} is a
     *         4x4 matrix, and {@code uMVPMatrix} is the name of the uniform variable which is located
     *         using the method {@code glGetUniformLocation}, its location assigned to the field
     *         {@code muMVPMatrixHandle} and changed using the method {@code glUniformMatrix4fv} in our
     *         {@code onDrawFrame} method. It is used to feed the transformation matrix {@code mMVPMatrix}
     *         which rotates the triangle a little bit every frame.
     *     </li>
     *     <li>
     *         attribute vec4 aPosition; # An {@code attribute} is used to feed data from the vertex
     *         array object, with the index into that object for the vertices being fed it set by the
     *         method {@code glVertexAttribPointer}, a {@code vec4} is a 4-component float vector,
     *         {@code aPosition} is the name of the attribute, and its location is located and assigned
     *         to the field {@code maPositionHandle} using the method {@code glGetAttribLocation}. It
     *         is used to feed the (x,y,z) coordinates to the shader program.
     *     </li>
     *     <li>
     *         attribute vec2 aTextureCoord; # Like {@code aPosition} but a 2-component float vector,
     *         with the location assigned to the field {@code maTextureHandle}. It is used to feed
     *         the (u,v) texture coordinates to the shader program.
     *     </li>
     *     <li>
     *         varying vec2 vTextureCoord; # A {@code varying} variable provides an interface between
     *         Vertex and Fragment Shader. Vertex Shaders compute values per vertex and fragment shaders
     *         compute values per fragment. If you define a varying variable in a vertex shader, its
     *         value will be interpolated (perspective-correct) over the primitive being rendered and
     *         you can access the interpolated value in the fragment shader. We use it simply to pass
     *         the value of {@code aTextureCoord} for this vertex to the fragment shader.
     *     </li>
     *     <li>
     *         void main() { # Each shader's entry point is at its {@code main} function where we
     *         process any input variables and output the results in its output variables.
     *     </li>
     *     <li>
     *         gl_Position = uMVPMatrix * aPosition; # {@code gl_Position} is a built-in variable for
     *         the clip-space output position of the current vertex, and is intended for writing the
     *         homogeneous vertex position. It can be written at any time during vertex shader execution.
     *         This value will be used by primitive assembly, clipping, culling, and other fixed
     *         functionality operations, if present, that operate on primitives after vertex processing
     *         has occurred. Its value is undefined after the vertex processing stage if the vertex
     *         shader executable does not write gl_Position. We calculate it by multiplying the (x,y,z)
     *         coordinates of the vertex fed us in {@code aPosition} by the transformation matrix
     *         {@code uMVPMatrix} which rotates the vertex to the current position.
     *     </li>
     *     <li>
     *         vTextureCoord = aTextureCoord; # We merely pass on the (u,v) texture coordinates of this
     *         vertex fed us in {@code aTextureCoord} to the fragment shader using {@code vTextureCoord}.
     *     </li>
     *     <li>
     *         } # That's all folks!
     *     </li>
     * </ul>
     */
    private final String mVertexShader =
            "uniform mat4 uMVPMatrix;\n" +
                    "attribute vec4 aPosition;\n" +
                    "attribute vec2 aTextureCoord;\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "void main() {\n" +
                    "  gl_Position = uMVPMatrix * aPosition;\n" +
                    "  vTextureCoord = aTextureCoord;\n" +
                    "}\n";

    /**
     * Source code for the GL_FRAGMENT_SHADER part of our shader program {@code mProgram} (shader that
     * is intended to run on the programmable fragment processor). The statements meanings:
     * <ul>
     *     <li>
     *         precision mediump float; # Specifies the use of medium precision for {@code float} calculations.
     *     </li>
     *     <li>
     *         varying vec2 vTextureCoord; # Storage for the vertex shader to use to pass us the (u,v) texture
     *         coordinates for the vertex.
     *     </li>
     *     <li>
     *         uniform sampler2D sTexture; # A {@code uniform} is a global openGL Shading Language variable
     *         declared with the "uniform" storage qualifier. These act as parameters that the user of a
     *         shader program can pass to that program. They are stored in a program object. Uniforms
     *         are so named because they do not change from one execution of a shader program to the next
     *         within a particular rendering call. This makes them unlike shader stage inputs and outputs,
     *         which are often different for each invocation of a program stage. A {@code sampler2D} is
     *         a floating point sampler for a 2 dimensional texture. The only place where you can use a
     *         sampler is in one of the openGL Shader Language standard library's texture lookup functions.
     *         These functions access the texture referred to by the sampler. They take a texture coordinate
     *         as parameters. The name {@code sTexture} is never used by the java program, so the sampler2D
     *         simply accesses the texture bound to the GL_TEXTURE_2D of the default texture unit GL_TEXTURE0.
     *     </li>
     *     <li>
     *         void main() { # As in the vertex shader program
     *     </li>
     *     <li>
     *         gl_FragColor = texture2D(sTexture, vTextureCoord); # {@code gl_FragColor} is a built-in output
     *         variable for setting the {@code vec4} fragment color. {@code texture2D} looks up the color
     *         for the coordinates given by {@code vTextureCoord} using the sampler {@code sTexture} (which
     *         just our texture we bound to GL_TEXTURE_2D for the default texture unit GL_TEXTURE0.
     *     </li>
     *     <li>
     *         } # That's all folks!
     *     </li>
     * </ul>
     */
    private final String mFragmentShader =
            "precision mediump float;\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "uniform sampler2D sTexture;\n" +
                    "void main() {\n" +
                    "  gl_FragColor = texture2D(sTexture, vTextureCoord);\n" +
                    "}\n";

    /**
     *
     */
    private float[] mMVPMatrix = new float[16];
    private float[] mProjMatrix = new float[16];
    private float[] mMMatrix = new float[16];
    private float[] mVMatrix = new float[16];

    private int mProgram;
    private int mTextureID;
    private int muMVPMatrixHandle;
    private int maPositionHandle;
    private int maTextureHandle;

    private Context mContext;
    private static String TAG = "GLES20TriangleRenderer";

    public GLES20TriangleRenderer(Context context) {
        mContext = context;
        mTriangleVertices = ByteBuffer.allocateDirect(mTriangleVerticesData.length * FLOAT_SIZE_BYTES)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mTriangleVertices.put(mTriangleVerticesData).position(0);
    }

    public void onDrawFrame(GL10 glUnused) {
        // Ignore the passed-in GL10 interface, and use the GLES20
        // class's static methods instead.
        GLES20.glClearColor(0.0f, 0.0f, 1.0f, 1.0f);
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glUseProgram(mProgram);
        checkGlError("glUseProgram");

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureID);

        mTriangleVertices.position(TRIANGLE_VERTICES_DATA_POS_OFFSET);
        GLES20.glVertexAttribPointer(maPositionHandle, 3, GLES20.GL_FLOAT, false,
                TRIANGLE_VERTICES_DATA_STRIDE_BYTES, mTriangleVertices);
        checkGlError("glVertexAttribPointer maPosition");
        mTriangleVertices.position(TRIANGLE_VERTICES_DATA_UV_OFFSET);
        GLES20.glEnableVertexAttribArray(maPositionHandle);
        checkGlError("glEnableVertexAttribArray maPositionHandle");
        GLES20.glVertexAttribPointer(maTextureHandle, 2, GLES20.GL_FLOAT, false,
                TRIANGLE_VERTICES_DATA_STRIDE_BYTES, mTriangleVertices);
        checkGlError("glVertexAttribPointer maTextureHandle");
        GLES20.glEnableVertexAttribArray(maTextureHandle);
        checkGlError("glEnableVertexAttribArray maTextureHandle");

        long time = SystemClock.uptimeMillis() % 4000L;
        float angle = 0.090f * ((int) time);
        Matrix.setRotateM(mMMatrix, 0, angle, 0, 0, 1.0f);
        Matrix.multiplyMM(mMVPMatrix, 0, mVMatrix, 0, mMMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mMVPMatrix, 0);

        GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, mMVPMatrix, 0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);
        checkGlError("glDrawArrays");
    }

    public void onSurfaceChanged(GL10 glUnused, int width, int height) {
        // Ignore the passed-in GL10 interface, and use the GLES20
        // class's static methods instead.
        GLES20.glViewport(0, 0, width, height);
        float ratio = (float) width / height;
        Matrix.frustumM(mProjMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
    }

    public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
        // Ignore the passed-in GL10 interface, and use the GLES20
        // class's static methods instead.
        mProgram = createProgram(mVertexShader, mFragmentShader);
        if (mProgram == 0) {
            return;
        }
        maPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
        checkGlError("glGetAttribLocation aPosition");
        if (maPositionHandle == -1) {
            throw new RuntimeException("Could not get attrib location for aPosition");
        }
        maTextureHandle = GLES20.glGetAttribLocation(mProgram, "aTextureCoord");
        checkGlError("glGetAttribLocation aTextureCoord");
        if (maTextureHandle == -1) {
            throw new RuntimeException("Could not get attrib location for aTextureCoord");
        }

        muMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        checkGlError("glGetUniformLocation uMVPMatrix");
        if (muMVPMatrixHandle == -1) {
            throw new RuntimeException("Could not get attrib location for uMVPMatrix");
        }

        /*
         * Create our texture. This has to be done each time the
         * surface is created.
         */

        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);

        mTextureID = textures[0];
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureID);

        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);

        InputStream is = mContext.getResources().openRawResource(R.raw.robot);
        Bitmap bitmap;
        try {
            bitmap = BitmapFactory.decodeStream(is);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                // Ignore.
            }
        }

        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        bitmap.recycle();

        Matrix.setLookAtM(mVMatrix, 0, 0, 0, -5, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
    }

    private int loadShader(int shaderType, String source) {
        int shader = GLES20.glCreateShader(shaderType);
        if (shader != 0) {
            GLES20.glShaderSource(shader, source);
            GLES20.glCompileShader(shader);
            int[] compiled = new int[1];
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
            if (compiled[0] == 0) {
                Log.e(TAG, "Could not compile shader " + shaderType + ":");
                Log.e(TAG, GLES20.glGetShaderInfoLog(shader));
                GLES20.glDeleteShader(shader);
                shader = 0;
            }
        }
        return shader;
    }

    private int createProgram(String vertexSource, String fragmentSource) {
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
        if (vertexShader == 0) {
            return 0;
        }

        int pixelShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
        if (pixelShader == 0) {
            return 0;
        }

        int program = GLES20.glCreateProgram();
        if (program != 0) {
            GLES20.glAttachShader(program, vertexShader);
            checkGlError("glAttachShader");
            GLES20.glAttachShader(program, pixelShader);
            checkGlError("glAttachShader");
            GLES20.glLinkProgram(program);
            int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
            if (linkStatus[0] != GLES20.GL_TRUE) {
                Log.e(TAG, "Could not link program: ");
                Log.e(TAG, GLES20.glGetProgramInfoLog(program));
                GLES20.glDeleteProgram(program);
                program = 0;
            }
        }
        return program;
    }

    private void checkGlError(String op) {
        int error;
        //noinspection LoopStatementThatDoesntLoop
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, op + ": glError " + error);
            throw new RuntimeException(op + ": glError " + error);
        }
    }

}
