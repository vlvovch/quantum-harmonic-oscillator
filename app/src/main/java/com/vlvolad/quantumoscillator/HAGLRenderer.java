package com.vlvolad.quantumoscillator;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Vladimir on 28.04.2015.
 */
public class HAGLRenderer implements GLSurfaceView.Renderer{
    private static final String TAG = "HAGLRenderer";
    public static HydrogenAtomRender mAtom = new HydrogenAtomRender();
    //    public DoubleSphericalPendulum mPendulum;
    public int Width, Height;

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {

//        mPendulum = new DoubleSphericalPendulum(75., 75., 1., 1., 45. * Math.PI / 180., 90. * Math.PI / 180.,
//        		5. * Math.PI / 8., 90. * Math.PI / 180., 0. * Math.PI / 180.,
//        		0.* Math.PI / 180., 45. * Math.PI / 180., 45. * Math.PI / 180., 9.81 * 100., 0. /1.e6, true, 100, true);
        // Set the background frame color
        GLES20.glClearColor(0.f, 0.f, 0.f, 1.0f);

        //GLES10.glDisable(GLES10.GL_DITHER);
        //GLES10.glHint(GLES10.GL_PERSPECTIVE_CORRECTION_HINT, GLES10.GL_FASTEST);

        GLES20.glClearDepthf(1.0f);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
//        GLES10.glClearDepthf(1.0f);
//        GLES10.glEnable(GLES10.GL_DEPTH_TEST);
//        GLES10.glEnable(GLES10.GL_COLOR_MATERIAL);
        //GLES10.glFrontFace(GLES10.GL_CW);
        //GLES10.glEnable(GLES10.GL_CULL_FACE);

//        float ambient[] = {0.f, 0.f, 0.f, 1.f};
//        GLES10.glLightModelfv(GLES10.GL_LIGHT_MODEL_AMBIENT, (FloatBuffer) ((ByteBuffer.allocateDirect(ambient.length * 4)
//                .order(ByteOrder.nativeOrder()).asFloatBuffer()).put(ambient)).position(0));
//
//        float pos[] = {0,0,1,0};
//        //float dir[3] = {1,1,-1};
//        float color[] = {1,1,1,1};
//        float mat_specular[] = {1,1,1,1};
//
//       //Position of light
//        float[] lightpos = {0.0f,0.0f,1f,0};
//        //direction of light
//        float[] lightdir = { 0.0f, 0.0f, -1.0f };
//
//        GLES10.glEnable(GLES10.GL_LIGHT0);
//
//        GLES10.glLightfv(GLES10.GL_LIGHT0, GLES10.GL_DIFFUSE, (FloatBuffer) ((ByteBuffer.allocateDirect(color.length * 4)
//                .order(ByteOrder.nativeOrder()).asFloatBuffer()).put(color)).position(0));
//        GLES10.glLightfv(GLES10.GL_LIGHT0, GLES10.GL_POSITION, (FloatBuffer) ((ByteBuffer.allocateDirect(pos.length * 4)
//                .order(ByteOrder.nativeOrder()).asFloatBuffer()).put(pos)).position(0));
//
//        float material_ambient[] = {1.f, 0.f, 0.f, 1.f};
//        float material_diffuse[] = {1.f, 0.f, 0.f, 1.f};
//
//        /*GLES10.glMaterialfv(GLES10.GL_FRONT_AND_BACK, GLES10.GL_AMBIENT, (FloatBuffer) ((ByteBuffer.allocateDirect(material_ambient.length * 4)
//                .order(ByteOrder.nativeOrder()).asFloatBuffer()).put(material_ambient)).position(0));
//
//        GLES10.glMaterialfv(GLES10.GL_FRONT_AND_BACK, GLES10.GL_DIFFUSE, (FloatBuffer) ((ByteBuffer.allocateDirect(material_diffuse.length * 4)
//                .order(ByteOrder.nativeOrder()).asFloatBuffer()).put(material_diffuse)).position(0));
//        */
//
//        //glLightfv(GL_LIGHT0, GL_SPOT_DIRECTION, dir);
//        GLES10.glMaterialfv(GLES10.GL_FRONT_AND_BACK, GLES10.GL_SPECULAR, (FloatBuffer) ((ByteBuffer.allocateDirect(mat_specular.length * 4)
//                .order(ByteOrder.nativeOrder()).asFloatBuffer()).put(mat_specular)).position(0));
//        GLES10.glMaterialf(GLES10.GL_FRONT_AND_BACK, GLES10.GL_SHININESS, 40.0f);
//
//        GLES10.glLightModelx(GLES10.GL_LIGHT_MODEL_TWO_SIDE, GLES10.GL_TRUE);
//

        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc( GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA );
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
//        GLES10.glShadeModel( GLES10.GL_SMOOTH );
//        GLES10.glEnable(GLES10.GL_LINE_SMOOTH);
//        GLES10.glHint( GLES10.GL_LINE_SMOOTH_HINT, GLES10.GL_NICEST );
//        GLES10.glEnable(GLES10.GL_BLEND);
//        GLES10.glBlendFunc( GLES10.GL_SRC_ALPHA, GLES10.GL_ONE_MINUS_SRC_ALPHA );
//        //GLES10.glClearColor(0, 0, 0, 1);
//        //GLES10.glClearAccum(0.0, 0.0, 0.0, 0.0);
//        GLES10.glClear(GLES10.GL_COLOR_BUFFER_BIT | GLES10.GL_DEPTH_BUFFER_BIT);// | GL_ACCUM_BUFFER_BIT);

        //GLES10.glEnableClientState(GLES10.GL_VERTEX_ARRAY);

//        GLES10.glLineWidth(2.0f);
        GLES20.glLineWidth(2.0f);

        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        int mProgram = GLES20.glCreateProgram();             // create empty OpenGL ES Program
        GLES20.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
        GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
        GLES20.glLinkProgram(mProgram);                  // creates OpenGL ES program executables

        mAtom.mProgram = mProgram;

//        int vertexShaderTraj = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
//        int fragmentShaderTraj = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);
//
//        int mProgram2 = GLES20.glCreateProgram();             // create empty OpenGL ES Program
//        GLES20.glAttachShader(mProgram2, vertexShaderTraj);   // add the vertex shader to program
//        GLES20.glAttachShader(mProgram2, fragmentShaderTraj); // add the fragment shader to program
//        GLES20.glLinkProgram(mProgram2);                  // creates OpenGL ES program executables
//
//        mPendulum.mTrajectory.mProgram = mProgram;
//        mPendulum.mTrajectory2.mProgram = mProgram2;

        /*SimulationParameters.simParams.l = (float)mPendulum.l;
        SimulationParameters.simParams.m = (float)mPendulum.m;
        SimulationParameters.simParams.g = (float)mPendulum.g;
        SimulationParameters.simParams.k = (float)mPendulum.k;

        SimulationParameters.simParams.th0 = (float)mPendulum.q[0];
        SimulationParameters.simParams.thv0 = (float)mPendulum.qv[0];
        SimulationParameters.simParams.ph0 = (float)mPendulum.q[1];
        SimulationParameters.simParams.phv0 = (float)mPendulum.qv[1];

        SimulationParameters.simParams.showTrajectory = mPendulum.trmd;*/

        //GLES10.glDisable(GLES10.GL_DITHER);
        /*mPendulum = new SphericalPendulum(100., 1., 22.5 * Math.PI / 180., 45. * Math.PI / 180.,
        		0. * Math.PI / 180., 0.* Math.PI / 180., 9.81 * 100., 0. /1.e6, 0);*/
    }

    @Override
    public void onDrawFrame(GL10 unused) {

        // Draw background color
        //GLES10.glMatrixMode(GLES10.GL_MODELVIEW);
//    	GLES10.glClear(GLES10.GL_COLOR_BUFFER_BIT | GLES10.GL_DEPTH_BUFFER_BIT);
//    	/*GLES10.glLoadIdentity();
//    	GLES10.glTranslatef(0.0f, 0.0f, -10.0f);*/
//
//
//        // Draw pendulum
//        mPendulum.draw(unused, Width, Height);
        GLES20.glUseProgram(mAtom.mProgram);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
    	/*GLES10.glLoadIdentity();
    	GLES10.glTranslatef(0.0f, 0.0f, -10.0f);*/
        Matrix.setIdentityM(mAtom.mVMatrix, 0);
        //Matrix.translateM(mAtom.mVMatrix, 0, 0.0f, 0.0f, -10.0f);


        // Draw pendulum
        mAtom.draw(unused, Width, Height);
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        // Adjust the viewport based on geometry changes,
        // such as screen rotation
        Width = width;
        Height = height;
        GLES20.glViewport(0, 0, width, height);

        float ratio = (float) width / height;

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        //perspectiveGL(45.f, ratio, 0.1f, 500.f);
        perspectiveGL(mAtom.mProjMatrix, 45.0f,(float)(Width)/Height,10.0f,4000.0f);
        //GLU.gluPerspective(unused, 45.f, ratio, 0.1f, 1200.f);

    }

    public static void orthoGL(float[] ProjectionMatrix, float Width, float Height)
    {
        Matrix.orthoM(ProjectionMatrix, 0, 0, Width, 0, Height, -1000.f, 1000.f);
    }

    public static void perspectiveGL(float[] ProjectionMatrix, float fovY, float aspect, float zNear, float zFar)
    {
        final float pi = (float) 3.1415926535897932384626433832795;
        float fW, fH;
        fH = (float) (Math.tan( fovY / 360 * pi ) * zNear);
        fW = (float) (fH * aspect);
        Matrix.frustumM(ProjectionMatrix, 0, -fW, fW, -fH-0.25f*fH, fH-0.25f*fH, zNear, zFar);
        //GLES20.glFrustumf( -fW, fW, -fH, fH, zNear, zFar );
    }

    private final String vertexShaderCode =
            "struct DirectionalLight { \n" +
                    "vec3 direction; \n" +
                    "vec3 halfplane; \n" +
                    "vec4 ambientColor; \n" +
                    "vec4 diffuseColor; \n" +
                    "vec4 specularColor; \n" +
                    "}; \n" +
                    "struct Material { \n" +
                    "    vec4 ambientFactor; \n" +
                    "    vec4 diffuseFactor; \n" +
                    "    vec4 specularFactor; \n" +
                    "    float shininess; \n" +
                    "}; \n" +
                    "// Light \n" +
                    "uniform DirectionalLight u_directionalLight; \n" +
                    "// Material \n" +
                    "uniform Material u_material; \n" +
                    "// Matrices \n" +
                    "uniform mat4 u_mvMatrix; \n" +
                    "uniform mat4 u_mvpMatrix; \n" +
                    "uniform vec4 color \n;" +
                    "// Attributes \n" +
                    "attribute vec4 a_position; \n" +
                    "attribute vec4 a_color; \n" +
                    "attribute vec3 a_normal; \n" +
                    "// Varyings \n" +
                    "varying vec4 v_light; \n" +
                    "varying vec4 v_color; \n" +
                    "void main() { \n" +
                    "    // Define position and normal in model coordinates \n" +
                    "    vec4 mcPosition = a_position; \n" +
                    "    vec3 mcNormal = a_normal; \n" +
                    "    // Calculate and normalize eye space normal \n" +
                    "    vec3 ecNormal = vec3(u_mvMatrix * vec4(mcNormal, 0.0)); \n" +
                    "    ecNormal = ecNormal / length(ecNormal); \n" +
                    "    // Do light calculations \n" +
                    "    float ecNormalDotLightDirection = max(0.0, dot(ecNormal, u_directionalLight.direction)); \n" +
                    "    float ecNormalDotLightHalfplane = max(0.0, dot(ecNormal, u_directionalLight.halfplane)); \n" +
                    "    // Ambient light \n" +
                    "    vec4 ambientLight = u_directionalLight.ambientColor * a_color;//u_material.ambientFactor; \n" +
                    "    // Diffuse light \n" +
                    "    vec4 diffuseLight = ecNormalDotLightDirection * u_directionalLight.diffuseColor * a_color;//u_material.diffuseFactor; \n" +
                    "    // Specular light \n" +
                    "    vec4 specularLight = vec4(0.0); \n" +
                    "    if (ecNormalDotLightHalfplane > 0.0) { \n" +
                    "        specularLight = pow(ecNormalDotLightHalfplane, u_material.shininess) * u_directionalLight.specularColor * u_material.specularFactor; \n" +
                    "    } \n" +
                    "    v_light = ambientLight + diffuseLight + specularLight; \n" +
                    "    v_color = a_color; \n" +
                    "    gl_Position = u_mvpMatrix * mcPosition; \n" +
                    "}";


    private final String fragmentShaderCode =
            "precision highp float; \n" +
                    "uniform vec4 color; \n" +
                    "uniform int light; \n" +
                    "uniform int trajectory; \n" +
                    "varying vec4 v_light; \n" +
                    "varying vec4 v_color; \n" +
                    "void main() { \n" +
                    "    if (light>0) gl_FragColor = v_light; \n" +
                    "    else if (trajectory>0) gl_FragColor = v_color; \n" +
                    "    else gl_FragColor = color; \n" +
                    "}";


    private final String vertexShaderCodeTraj =
            "uniform mat4 u_mvpMatrix; \n" +
                    "// Attributes \n" +
                    "attribute vec4 a_position; \n" +
                    "attribute vec4 a_color; \n" +
                    "// Varyings \n" +
                    "varying vec4 v_color; \n" +
                    "void main() { \n" +
                    "    // Define position and normal in model coordinates \n" +
                    "    vec4 mcPosition = a_position; \n" +
                    "    gl_Position = u_mvpMatrix * mcPosition; \n" +
                    "    v_color = a_color; \n" +
                    "}";


    private final String fragmentShaderCodeTraj =
            "varying vec4 v_color; \n" +
                    "void main() { \n" +
                    "    gl_FragColor = vec4(1.0, 0., 0., 1.); \n" + //v_color; \n" +
                    "}";

    public static int loadShader(int type, String shaderCode){

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }
}
