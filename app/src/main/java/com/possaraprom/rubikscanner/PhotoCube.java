package com.possaraprom.rubikscanner;

import static android.opengl.GLES20.GL_UNSIGNED_SHORT;

import android.opengl.GLES20;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;

public class PhotoCube{
    public ArrayList<FloatBuffer> vertexBufferList = new ArrayList<>();
    public int[][] lists = {
            {0,1,0}, //Y
            {0,0,1}, //X
            {0,1,0}
    };
    public float[] coordinate;
    public double xAngle = 0;
    public double yAngle = 0;
    public double zAngle = 0;
    public float[] length;
    public float[][] coords;

    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;

    static final float[][] colorList = {
            {1f,0f,0f,1f},
            {0f,1f,0f,1f},
            {1f,1f,1f,1f},
            {1f,1f,0f,1f},
            {1f,0f,1f,1f},
            {0f,1f,1f,1f}
    };

    public int mProgram;
    private final short[] drawOrder = {0, 1, 2, 0, 2, 3};
    public ShortBuffer drawListBuffer;


    public PhotoCube(float w, float h, float d, float X, float Y, float Z) {
        vertexBufferList = new ArrayList<>();
        coordinate = new float[]{
                X,Y,Z //x,y,z
        };
        length = new float[]{
                w,h,d
        };
        coords = new float[][]{   // in counterclockwise order:
                {// X
                    w / 2, h / 2, d / 2, // top right
                    w / 2, h / 2, -d / 2, // top left
                    w / 2, -h / 2, -d / 2, // bottom left
                    w / 2, -h / 2, d / 2  // bottom right
                },
                {// Y
                    w / 2, h / 2, d / 2, // top right
                    -w / 2, h / 2, d / 2, // top left
                    -w / 2, h / 2, -d / 2, // bottom left
                    w / 2, h / 2, -d / 2  // bottom right
                },
                {// Z
                    w / 2, h / 2, d / 2, // top right
                    -w / 2, h / 2, d / 2, // top left
                    -w / 2, -h / 2, d / 2, // bottom left
                    w / 2, -h / 2, d / 2  // bottom right
                }
        };

        for(int j=0;j<3;j++){
            float[] currentCoords = coords[j].clone();
            int[] list = lists[j];
            for(int k=0;k<2;k++) {
                for (int i = 0; i < 12; i += 3) {
                    float x = currentCoords[i];
                    float y = currentCoords[i + 1];
                    float z = currentCoords[i + 2];
                    double a = Math.toRadians(k*180);
                    float[] rotated = rotate(x,y,z,a,list);
                    currentCoords[i] = rotated[0]+X*(w/2+MainActivity.axisWidth/2+MainActivity.lengthPerLevel);
                    currentCoords[i+1] = rotated[1]+Y*(h/2+MainActivity.axisWidth/2+MainActivity.lengthPerLevel);
                    currentCoords[i+2] = rotated[2]+Z*(d/2+MainActivity.axisWidth/2+MainActivity.lengthPerLevel);
                }

                ByteBuffer bb = ByteBuffer.allocateDirect(
                        // (number of coordinate values * 4 bytes per float)
                        12 * 4);
                // use the device hardware's native byte order
                bb.order(ByteOrder.nativeOrder());
                // create a floating point buffer from the ByteBuffer
                FloatBuffer vertexBuffer = bb.asFloatBuffer();
                // add the coordinates to the FloatBuffer
                vertexBuffer.put(currentCoords);
                // set the buffer to read the first coordinate
                vertexBuffer.position(0);

                ByteBuffer dlb = ByteBuffer.allocateDirect(
                        // (# of coordinate values * 2 bytes per short)
                        drawOrder.length * 2);
                vertexBufferList.add(vertexBuffer);
                dlb.order(ByteOrder.nativeOrder());
                drawListBuffer = dlb.asShortBuffer();
                drawListBuffer.put(drawOrder);
                drawListBuffer.position(0);
            }
        }
    }

    public float[] rotate(float x, float y, float z, double angle, int[] axis){
        return new float[]{
            (float) ((axis[0] == 1) ? x : (axis[1] == 1) ? x * Math.cos(angle) + z * Math.sin(angle) : x * Math.cos(angle) - y * Math.sin(angle)),
            (float) ((axis[0] == 1) ? y * Math.cos(angle) - z * Math.sin(angle) : (axis[1] == 1) ? y : x * Math.sin(angle) + y * Math.cos(angle)),
            (float) ((axis[0] == 1) ? z * Math.cos(angle) + y * Math.sin(angle) : (axis[1] == 1) ? z * Math.cos(angle) - x * Math.sin(angle) : z)
        };
    }

    public void update(){
        vertexBufferList = new ArrayList<>();
        float w = length[0];
        float h = length[1];
        float d = length[2];
        float X = coordinate[0];
        float Y = coordinate[1];
        float Z = coordinate[2];
        for(int j=0;j<3;j++){
            float[] currentCoords = coords[j].clone();
            int[] list = lists[j];
            for(int k=0;k<2;k++) {
                for (int i = 0; i < 12; i += 3) {
                    float x = coords[j][i];
                    float y = coords[j][i + 1];
                    float z = coords[j][i + 2];
                    double a = Math.toRadians(k*180);
                    float[] rotated = rotate(x,y,z,a,list);
                    x = rotated[0]+X*(w/2+MainActivity.axisWidth/2+MainActivity.lengthPerLevel);
                    y = rotated[1]+Y*(h/2+MainActivity.axisWidth/2+MainActivity.lengthPerLevel);
                    z = rotated[2]+Z*(d/2+MainActivity.axisWidth/2+MainActivity.lengthPerLevel);
                    rotated = rotate(x,y,z,xAngle,new int[]{1,0,0});
                    x = rotated[0];y = rotated[1];z = rotated[2];
                    rotated = rotate(x,y,z,yAngle,new int[]{0,1,0});
                    x = rotated[0];y = rotated[1];z = rotated[2];
                    rotated = rotate(x,y,z,zAngle,new int[]{0,0,1});
                    x = rotated[0];y = rotated[1];z = rotated[2];
                    currentCoords[i] = x;
                    currentCoords[i + 1] = y;
                    currentCoords[i + 2] = z;
                }

                ByteBuffer bb = ByteBuffer.allocateDirect(
                        // (number of coordinate values * 4 bytes per float)
                        12 * 4);
                // use the device hardware's native byte order
                bb.order(ByteOrder.nativeOrder());
                // create a floating point buffer from the ByteBuffer
                FloatBuffer vertexBuffer = bb.asFloatBuffer();
                // add the coordinates to the FloatBuffer
                vertexBuffer.put(currentCoords);
                // set the buffer to read the first coordinate
                vertexBuffer.position(0);

                vertexBufferList.add(vertexBuffer);
                ByteBuffer dlb = ByteBuffer.allocateDirect(
                        // (# of coordinate values * 2 bytes per short)
                        drawOrder.length * 2);
                dlb.order(ByteOrder.nativeOrder());
                drawListBuffer = dlb.asShortBuffer();
                drawListBuffer.put(drawOrder);
                drawListBuffer.position(0);

            }
        }
        for (int j = 0; j < 6; j++) {
            draw(j);
        }
    }

    public void updateAngleX(double angle){
        xAngle = xAngle+angle;
        if(xAngle>2*Math.PI){
            xAngle=xAngle-2*Math.PI;
        }
    }
    public void updateAngleY(double angle){
        yAngle = yAngle+angle;
        if(yAngle>2*Math.PI){
            yAngle=yAngle-2*Math.PI;
        }
    }
    public void updateAngleZ(double angle){
        zAngle = zAngle+angle;
        if(zAngle>2*Math.PI){
            zAngle=zAngle-2*Math.PI;
        }
    }

    public void draw(int order) {
        mProgram=MyGLRenderer.program;
        // get handle to vertex shader's vPosition member
        int positionHandle = MyGLRenderer.positionHandle;
        // get handle to fragment shader's vColor member
        int colorHandle = MyGLRenderer.colorHandle;

        // Add program to OpenGL ES environment
        GLES20.glUseProgram(mProgram);
        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(positionHandle);
        // Prepare the triangle coordinate data
        // 4 bytes per vertex
        int vertexStride = COORDS_PER_VERTEX * 4;
        try {
            GLES20.glVertexAttribPointer(positionHandle, COORDS_PER_VERTEX,
                    GLES20.GL_FLOAT, true,
                    vertexStride, vertexBufferList.get(order));

            // Set color for drawing the triangle
            GLES20.glUniform4fv(colorHandle, 1, colorList[order], 0);

            GLES20.glDrawElements(
                    GLES20.GL_TRIANGLES,
                    drawOrder.length,
                    GL_UNSIGNED_SHORT,
                    drawListBuffer);

            // Disable vertex array
            GLES20.glDisableVertexAttribArray(positionHandle);
        }
        catch(java.lang.IndexOutOfBoundsException|java.lang.NullPointerException e){

        }
    }
}
