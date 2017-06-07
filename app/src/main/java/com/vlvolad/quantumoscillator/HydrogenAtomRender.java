package com.vlvolad.quantumoscillator;

import android.opengl.GLES20;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Random;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Vladimir on 28.04.2015.
 */
class GLvector
{
    float fX;
    float fY;
    float fZ;
}

public class HydrogenAtomRender {
    int Width, Height;
    double scale;

    boolean motion;
    boolean animatemode;
    boolean dlistgen;

    int ncubes, ncubesanim;
    boolean pause;

    volatile int n,l,m;				//квантовые числа
    //int drawn,drawl,drawm;	//тоже самое
    volatile boolean drawsign;				//знак числа m
    volatile boolean drawrks;					//действительные или комплексные волновые функции
    volatile int fin;                   //закончилась ли генерация

    volatile boolean overflow;
    boolean candraw;

    double avt1;


    //массивы, содержащие координаты, нормали, цвета и индексы для вершин в режиме поверхностей
    int size1, size2;
    volatile FloatBuffer trivertex;
    volatile FloatBuffer trinormal;
    FloatBuffer tricolor1, tricolor2;
    volatile FloatBuffer trivertexanim;
    volatile FloatBuffer trinormalanim;
    FloatBuffer tricoloranim1, tricoloranim2;

    FloatBuffer trivertexf;
    FloatBuffer trinormalf;
    FloatBuffer tricolorf;
    FloatBuffer trivertexanimf;
    FloatBuffer trinormalanimf;
    FloatBuffer tricoloranimf;
    //сколько всего треуголников уже сгенерировано
    volatile int trcnt, trcntanim;
    boolean anim, rounderror;
    volatile boolean stopThread;

    public volatile boolean toCont;

    volatile float   fStepSize;
    float fStepSize1, fStepSizeanim;
    float   fTargetValue;
    volatile double  pct;

    float [] valtable;
    int maxinda;

    HydrogenAtomMath hAtom;

    //вращение и зум
    int ox, oy;
    public volatile float camera_trans[];
    public volatile float camera_rot[];
    public volatile float camera_trans_lag[];
    public volatile float camera_rot_lag[];
    public volatile float zoomIn;
    int buttonState;
    int memoryclass;

    public int mProgram;

    public final float[] mMVPMatrix = new float[16];
    public final float[] mProjMatrix = new float[16];
    public final float[] mVMatrix = new float[16];

    public float[] mAccumulatedRotation;// = new float[16];
    public float[] mCurrentRotation;// = new float[16];

    private FloatBuffer lightDir, lightHP, lightAC, lightDC, lightSC;
    private FloatBuffer materialAF, materialDF, materialSF;
    float materialshin;

    public volatile int progress, totalprogress;

    int index_table(int ix, int iy, int iz)
    {
        return maxinda*maxinda*ix + maxinda*iy + iz;
    }

    //Thread genThread;

    public static Random generator = new Random();

    public HydrogenAtomRender() {
        scale = 1.;
        motion = false;
        pause = false;
        n = 8;
        l = 5;
        m = 2;
        fin = 0;
        ox = 0;
        oy = 0;
        buttonState = 0;
        candraw = true;
        trcnt = 0;
        trcntanim = 0;
        anim = false;
        rounderror = false;
        fStepSize = 6.f;
        fStepSizeanim = 8.f;
        fTargetValue = 0.00172655f;
        pct = 70;
        ncubes = 0;
        ncubesanim = 0;
        overflow = false;
        animatemode = true;
        avt1 = 0.;
        dlistgen = false;
        toCont = false;

//        camera_trans[0] = camera_trans[1] = camera_trans[2] = 0;
//        camera_rot[0] = camera_rot[1] = camera_rot[2] = 0;
//        camera_trans_lag[0] = camera_trans_lag[1] = camera_trans_lag[2] = 0;
//        camera_rot_lag[0] = camera_rot_lag[1] = camera_rot_lag[2] = 0;

        hAtom = new HydrogenAtomMath();

        drawrks = false;
        hAtom.realksi = false;

        lightDir = fill3DVector(0.f, 0.f, 1.0f);
        lightHP = fill3DVector(0.f, 0.f, 1.0f);
        lightAC = fill4DVector(0.3f, 0.3f, 0.3f, 1.0f);
        lightDC = fill4DVector(1.0f, 1.0f, 1.0f, 1.0f);
        lightSC = fill4DVector(1.0f, 1.0f, 1.0f, 1.0f);
        materialAF = fill4DVector(0.2f, 0.2f, 0.2f, 1.0f);
        materialDF = fill4DVector(0.8f, 0.8f, 0.8f, 1.0f);
        materialSF = fill4DVector(1.0f, 1.0f, 1.0f, 1.0f);
        materialshin = 40.0f;

        camera_trans = new float[3];
        camera_rot = new float[3];
        camera_trans_lag = new float[3];
        camera_rot_lag = new float[3];
        camera_trans[0] = camera_trans[1] = camera_trans[2] = 0;
        camera_rot[0] = camera_rot[1] = camera_rot[2] = 0;
        camera_trans_lag[0] = camera_trans_lag[1] = camera_trans_lag[2] = 0;
        camera_rot_lag[0] = camera_rot_lag[1] = camera_rot_lag[2] = 0;

        zoomIn = 1.0f;

        pickRandomOrbital(5);

        //genThread = null;

        stopThread = false;

        mAccumulatedRotation = new float[16];
        Matrix.setIdentityM(mAccumulatedRotation, 0);
        mCurrentRotation = new float[16];
        Matrix.setIdentityM(mCurrentRotation, 0);

        //regenerate();
    }

    void pickRandomOrbital(int nmax) {
        n = generator.nextInt(nmax) + 1;
        l = generator.nextInt(n);
        m = -l + generator.nextInt(2*l+1);
    }

    void reallocateMemory() {
//        size1 = 600000;
//        size2 = 1000000;
        size1 = 300000;
        size2 = 800000;

        if (memoryclass<=64) {
            size1 /= 2;
            size2 /= 2;
        }

        if (memoryclass<=32) {
            size1 /= 2;
            size2 /= 2;
        }

        if (memoryclass<=16) {
            size1 /= 2;
            size2 /= 2;
        }

        trivertex = ByteBuffer.allocateDirect(
                // (number of coordinate values * 4 bytes per float)
                size1 * 3 * 4).
                order(ByteOrder.nativeOrder()).
                asFloatBuffer();

        trinormal = ByteBuffer.allocateDirect(
                // (number of coordinate values * 4 bytes per float)
                size1 * 3 * 4).
                order(ByteOrder.nativeOrder()).
                asFloatBuffer();

        tricolor1 = ByteBuffer.allocateDirect(
                // (number of coordinate values * 4 bytes per float)
                size1 * 3 * 4).
                order(ByteOrder.nativeOrder()).
                asFloatBuffer();

        tricolor2 = ByteBuffer.allocateDirect(
                // (number of coordinate values * 4 bytes per float)
                size1 * 3 * 4).
                order(ByteOrder.nativeOrder()).
                asFloatBuffer();

        trivertexanim = ByteBuffer.allocateDirect(
                // (number of coordinate values * 4 bytes per float)
                size1 * 3 * 4).
                order(ByteOrder.nativeOrder()).
                asFloatBuffer();

        trinormalanim = ByteBuffer.allocateDirect(
                // (number of coordinate values * 4 bytes per float)
                size1 * 3 * 4).
                order(ByteOrder.nativeOrder()).
                asFloatBuffer();

        tricoloranim1 = ByteBuffer.allocateDirect(
                // (number of coordinate values * 4 bytes per float)
                size1 * 3 * 4).
                order(ByteOrder.nativeOrder()).
                asFloatBuffer();

        tricoloranim2 = ByteBuffer.allocateDirect(
                // (number of coordinate values * 4 bytes per float)
                size1 * 3 * 4).
                order(ByteOrder.nativeOrder()).
                asFloatBuffer();
    }

    FloatBuffer trimFB(FloatBuffer in, int size) {
        FloatBuffer ret = ByteBuffer.allocateDirect(
                // (number of coordinate values * 4 bytes per float)
                size * 3 * 4).
                order(ByteOrder.nativeOrder()).
                asFloatBuffer();
        float[] arr = new float[size * 3];
        in.position(0);
        in.get(arr, 0, size * 3);
        //System.arraycopy(in.get(), 0, arr, 0, trcnt * 3 * 3);
        ret.put(arr);
        return ret;
    }

    FloatBuffer expandFB(FloatBuffer in, int size, int size2) {
        FloatBuffer ret = ByteBuffer.allocateDirect(
                // (number of coordinate values * 4 bytes per float)
                size * 3 * 4).
                order(ByteOrder.nativeOrder()).
                asFloatBuffer();
        float[] arr = new float[size * 3];
        in.position(0);
        in.get(arr, 0, size2 * 3);
        //System.arraycopy(in.get(), 0, arr, 0, trcnt * 3 * 3);
        ret.put(arr);
        return ret;
    }

    void reallocateMemoryFinal() {
        trivertex = trimFB(trivertex, trcnt*3);
        trivertex.position(0);

        trinormal = trimFB(trinormal, trcnt*3);
        trinormal.position(0);

        tricolor1 = trimFB(tricolor1, trcnt*3);
        tricolor1.position(0);

        tricolor2 = trimFB(tricolor2, trcnt*3);
        tricolor2.position(0);

        trivertexanim = trimFB(trivertexanim, trcnt * 3);
        trivertexanim.position(0);

        trinormalanim = trimFB(trinormalanim, trcnt * 3);
        trinormalanim.position(0);

        tricoloranim1 = trimFB(tricoloranim1, trcnt*3);
        tricoloranim1.position(0);

        tricoloranim2 = trimFB(tricoloranim2, trcnt*3);
        tricoloranim2.position(0);
    }

    FloatBuffer fill3DVector(float x, float y, float z)
    {
        FloatBuffer buf;
        ByteBuffer vbb = ByteBuffer.allocateDirect(3 * 4);
        vbb.order(ByteOrder.nativeOrder());
        buf = vbb.asFloatBuffer();
        buf.put(x);
        buf.put(y);
        buf.put(z);
        return buf;
    }

    FloatBuffer fill4DVector(float x, float y, float z, float w)
    {
        FloatBuffer buf;
        ByteBuffer vbb = ByteBuffer.allocateDirect(4 * 4);
        vbb.order(ByteOrder.nativeOrder());
        buf = vbb.asFloatBuffer();
        buf.put(x);
        buf.put(y);
        buf.put(z);
        buf.put(w);
        return buf;
    }

    //отображение поверхностей
    public void draw(GL10 unused, int Width, int Height)
    {
//        Log.d("Draw", "Start");
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        //glMatrixMode(GL_PROJECTION);
        //glLoadIdentity();
        HAGLRenderer.perspectiveGL(mProjMatrix, 45.0f,(float)(Width)/Height,10.0f,4000.0f);
        Matrix.setIdentityM(mVMatrix, 0);
//        gluPerspective(45.0,(GLfloat)Width/Height,10.0,4000.0);
        int tHandle = GLES20.glGetUniformLocation(mProgram, "light");
        GLES20.glUniform1i(tHandle, 1);
        tHandle = GLES20.glGetUniformLocation(mProgram, "color");
        GLES20.glUniform4f(tHandle, 1.0f, 0.0f, 0.0f, 1.0f);
        tHandle = GLES20.glGetUniformLocation(mProgram, "trajectory");
        GLES20.glUniform1i(tHandle, 1);

        tHandle = GLES20.glGetAttribLocation(mProgram, "a_position");
        GLES20.glEnableVertexAttribArray(tHandle);
//        glEnable(GL_LIGHTING);

//        glMatrixMode(GL_MODELVIEW);
//        glLoadIdentity();

        tHandle = GLES20.glGetUniformLocation(mProgram, "u_directionalLight.direction");
        lightDir.position(0);
        GLES20.glUniform3fv(tHandle, 1, lightDir);
        tHandle = GLES20.glGetUniformLocation(mProgram, "u_directionalLight.halfplane");
        lightHP.position(0);
        GLES20.glUniform3fv(tHandle, 1, lightHP);
        tHandle = GLES20.glGetUniformLocation(mProgram, "u_directionalLight.ambientColor");
        lightAC.position(0);
        GLES20.glUniform4fv(tHandle, 1, lightAC);
        tHandle = GLES20.glGetUniformLocation(mProgram, "u_directionalLight.diffuseColor");
        lightDC.position(0);
        GLES20.glUniform4fv(tHandle, 1, lightDC);
        tHandle = GLES20.glGetUniformLocation(mProgram, "u_directionalLight.specularColor");
        lightSC.position(0);
        GLES20.glUniform4fv(tHandle, 1, lightSC);

        tHandle = GLES20.glGetUniformLocation(mProgram, "u_material.shininess");
        GLES20.glUniform1f(tHandle, materialshin);
        tHandle = GLES20.glGetUniformLocation(mProgram, "u_material.specularFactor");
        materialSF.position(0);
        GLES20.glUniform4fv(tHandle, 1, materialSF);

        //glTranslatef(0,0,-1000/2);
        //Matrix.translateM(mVMatrix, 0, 0, 50, 0);
        Matrix.translateM(mVMatrix, 0, 0, 0, -1000 / 2 / zoomIn);

        float dx = camera_rot[0] - camera_rot_lag[0];
        float dy = camera_rot[1] - camera_rot_lag[1];
        float dz = camera_rot[2] - camera_rot_lag[2];

        for (int k = 0; k < 3; ++k)
        {
            camera_trans_lag[k] += (camera_trans[k] - camera_trans_lag[k]);// * inertia;
            camera_rot_lag[k] += (camera_rot[k] - camera_rot_lag[k]);// * inertia;
        }
        Matrix.translateM(mVMatrix, 0, camera_trans_lag[0],
                camera_trans_lag[1],
                camera_trans_lag[2]);



        //Matrix.rotateM(mVMatrix, 0, camera_rot_lag[0], 1.0f, 0.0f, 0.0f);
        //Matrix.rotateM(mVMatrix, 0, camera_rot_lag[1], 0.0f, 0.0f, -1.0f);

        {
            Matrix.setIdentityM(mCurrentRotation, 0);
            Matrix.rotateM(mCurrentRotation, 0, dx, 1.0f, 0.0f, 0.0f);
            Matrix.rotateM(mCurrentRotation, 0, dy, 0.0f, 0.0f, -1.0f);
            //Matrix.rotateM(mCurrentRotation, 0, dz, 0.0f, 1.0f, 0.0f);
            //camera_rot[0] = 0.0f;
            //camera_rot[1] = 0.0f;

            float[] mTemporaryMatrix = new float[16];
            // Multiply the current rotation by the accumulated rotation, and then set the accumulated
            // rotation to the result.
            Matrix.multiplyMM(mTemporaryMatrix, 0, mCurrentRotation, 0, mAccumulatedRotation, 0);
            System.arraycopy(mTemporaryMatrix, 0, mAccumulatedRotation, 0, 16);

            // Rotate the cube taking the overall rotation into account.
            Matrix.multiplyMM(mTemporaryMatrix, 0, mVMatrix, 0, mAccumulatedRotation, 0);
            System.arraycopy(mTemporaryMatrix, 0, mVMatrix, 0, 16);
        }

        //
        // Matrix.rotateM(mVMatrix, 0, camera_rot_lag[2], 0.0f, 1.0f, 0.0f);
        //Matrix.rotateM(mVMatrix, 0, camera_rot_lag[1], 0.0f, -1.0f * (float)Math.sin(camera_rot_lag[0]*Math.PI/180.f), -1.0f * (float)Math.cos(camera_rot_lag[0]*Math.PI/180.f));

//        for (int k = 0; k < 3; ++k)
//        {
//            camera_trans_lag[k] += (camera_trans[k] - camera_trans_lag[k]);// * inertia;
//            camera_rot_lag[k] += (camera_rot[k] - camera_rot_lag[k]);// * inertia;
//        }
//        glTranslatef(camera_trans_lag[0],
//                camera_trans_lag[1],
//                camera_trans_lag[2]);
//        glRotatef(camera_rot_lag[0], 1.0, 0.0, 0.0);
//        glRotatef(camera_rot_lag[1], 0.0, 0.0, -1.0);

        //glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);

        trivertex.position(0);
        trinormal.position(0);
        trivertexanim.position(0);
        trinormalanim.position(0);

        dlistgen = true;

        if (fin>0 && !dlistgen)
        {
            if (!animatemode)
            {
//                Log.d("Draw", "Empty");
//                trivertexf = ByteBuffer.allocateDirect(
//                        // (number of coordinate values * 4 bytes per float)
//                        3 * 8 * trcnt * 3 * 4).
//                        order(ByteOrder.nativeOrder()).
//                        asFloatBuffer();
//
//                trinormalf = ByteBuffer.allocateDirect(
//                        // (number of coordinate values * 4 bytes per float)
//                        3 * 8 * trcnt * 3 * 4).
//                        order(ByteOrder.nativeOrder()).
//                        asFloatBuffer();
//
//                tricolorf = ByteBuffer.allocateDirect(
//                        // (number of coordinate values * 4 bytes per float)
//                        3 * 8 * trcnt * 3 * 4).
//                        order(ByteOrder.nativeOrder()).
//                        asFloatBuffer();
//
////                tricolor2f = ByteBuffer.allocateDirect(
////                        // (number of coordinate values * 4 bytes per float)
////                        8 * trcnt * 3 * 4).
////                        order(ByteOrder.nativeOrder()).
////                        asFloatBuffer();
//                //glNewList(dList, GL_COMPILE);
//                int index = 0;
//                int inv = 0;
//                for(int rotX = 0; rotX < 2; rotX++)
//                    for(int rotY = 0; rotY < 2; rotY++)
//                        for(int rotZ = 0; rotZ < 2; rotZ++)
//                        {
//                            //glPushMatrix();
//                            Matrix.scaleM(mVMatrix, 0, 1.0f-2.0f*rotX, 1.0f-2.0f*rotY, 1.0f-2.0f*rotZ);
//                            //glScalef(1.0f-2.0f*rotX, 1.0f-2.0f*rotY, 1.0f-2.0f*rotZ);  //симметрия квадрата волновой ф-ии относительно инверсии координат
//                            //glRotatef(90.0, 0.0, 1.0, 0.0);
//
//                            if (rotY>0 && ((l-m) & 1)>0) inv = 1;   //волновая ф-я меняет знак в случае инверсии по z когда l-|m| - нечетное
//                            else inv = 0;
//                            if (drawrks && m!=0)   // для действительных ф-й знак может менятся и при инверсии x и y
//                            {
//                                if (rotZ>0 && rotX==0 && (m%2==0) && !drawsign) inv++;
//                                if (rotZ>0 && rotX==0 && (m & 1)>0 && drawsign) inv++;
//                                if (rotX>0 && rotZ==0 && !drawsign) inv++;
//                                if (rotX>0 && rotZ>0 && (m & 1)>0) inv++;
//                            }
//
//                            GLES20.glFrontFace(GLES20.GL_CCW);
//                            if ((inv%2)>0)
//                            {
//                                //glBegin(GL_TRIANGLES);
//                                //rep(i,3*trcnt)
//                                if (((rotX+rotY+rotZ)%2)>0)
//                                {
//                                    for(int i=0;i<trcnt;++i)
//                                    {
//                                        tricolorf.put(3*index, tricolor2.get(9*i));
//                                        tricolorf.put(3*index + 1, tricolor2.get(9*i + 1));
//                                        tricolorf.put(3*index + 2, tricolor2.get(9*i + 2));
//                                        trinormalf.put(3*index, trinormal.get(9*i));
//                                        trinormalf.put(3*index + 1, trinormal.get(9*i + 1));
//                                        trinormalf.put(3*index + 2, trinormal.get(9*i + 2));
//                                        trivertexf.put(3*index, trivertex.get(9*i));
//                                        trivertexf.put(3*index + 1, trivertex.get(9*i + 1));
//                                        trivertexf.put(3*index + 2, trivertex.get(9*i + 2));
//                                        index++;
//
//                                        tricolorf.put(3*index, tricolor2.get(9*i + 6));
//                                        tricolorf.put(3*index + 1, tricolor2.get(9*i + 1 + 6));
//                                        tricolorf.put(3*index + 2, tricolor2.get(9*i + 2 + 6));
//                                        trinormalf.put(3*index, trinormal.get(9*i + 6));
//                                        trinormalf.put(3*index + 1, trinormal.get(9*i + 1 + 6));
//                                        trinormalf.put(3*index + 2, trinormal.get(9*i + 2 + 6));
//                                        trivertexf.put(3*index, trivertex.get(9*i + 6));
//                                        trivertexf.put(3*index + 1, trivertex.get(9*i + 1 + 6));
//                                        trivertexf.put(3*index + 2, trivertex.get(9*i + 2 + 6));
//                                        index++;
//
//                                        tricolorf.put(3*index, tricolor2.get(9*i + 3));
//                                        tricolorf.put(3*index + 1, tricolor2.get(9*i + 1 + 3));
//                                        tricolorf.put(3*index + 2, tricolor2.get(9*i + 2 + 3));
//                                        trinormalf.put(3*index, trinormal.get(9*i + 3));
//                                        trinormalf.put(3*index + 1, trinormal.get(9*i + 1 + 3));
//                                        trinormalf.put(3*index + 2, trinormal.get(9*i + 2 + 3));
//                                        trivertexf.put(3*index, trivertex.get(9*i + 3));
//                                        trivertexf.put(3*index + 1, trivertex.get(9*i + 1 + 3));
//                                        trivertexf.put(3*index + 2, trivertex.get(9*i + 2 + 3));
//                                        index++;
//                                    }
//                                }
//                                else
//                                {
//                                    for(int i=0;i<trcnt;++i)
//                                    {
//                                        tricolorf.put(3*index, tricolor2.get(9*i));
//                                        tricolorf.put(3*index + 1, tricolor2.get(9*i + 1));
//                                        tricolorf.put(3*index + 2, tricolor2.get(9*i + 2));
//                                        trinormalf.put(3*index, trinormal.get(9*i));
//                                        trinormalf.put(3*index + 1, trinormal.get(9*i + 1));
//                                        trinormalf.put(3*index + 2, trinormal.get(9*i + 2));
//                                        trivertexf.put(3*index, trivertex.get(9*i));
//                                        trivertexf.put(3*index + 1, trivertex.get(9*i + 1));
//                                        trivertexf.put(3*index + 2, trivertex.get(9*i + 2));
//                                        index++;
//
//                                        tricolorf.put(3*index, tricolor2.get(9*i + 6));
//                                        tricolorf.put(3*index + 1, tricolor2.get(9*i + 1 + 6));
//                                        tricolorf.put(3*index + 2, tricolor2.get(9*i + 2 + 6));
//                                        trinormalf.put(3*index, trinormal.get(9*i + 6));
//                                        trinormalf.put(3*index + 1, trinormal.get(9*i + 1 + 6));
//                                        trinormalf.put(3*index + 2, trinormal.get(9*i + 2 + 6));
//                                        trivertexf.put(3*index, trivertex.get(9*i + 6));
//                                        trivertexf.put(3*index + 1, trivertex.get(9*i + 1 + 6));
//                                        trivertexf.put(3*index + 2, trivertex.get(9*i + 2 + 6));
//                                        index++;
//
//                                        tricolorf.put(3*index, tricolor2.get(9*i + 3));
//                                        tricolorf.put(3*index + 1, tricolor2.get(9*i + 1 + 3));
//                                        tricolorf.put(3*index + 2, tricolor2.get(9*i + 2 + 3));
//                                        trinormalf.put(3*index, trinormal.get(9*i + 3));
//                                        trinormalf.put(3*index + 1, trinormal.get(9*i + 1 + 3));
//                                        trinormalf.put(3*index + 2, trinormal.get(9*i + 2 + 3));
//                                        trivertexf.put(3*index, trivertex.get(9*i + 3));
//                                        trivertexf.put(3*index + 1, trivertex.get(9*i + 1 + 3));
//                                        trivertexf.put(3*index + 2, trivertex.get(9*i + 2 + 3));
//                                        index++;
//                                    }
//                                }
////                                glEnd();
//                            }
//                            else
//                            {
////                                glBegin(GL_TRIANGLES);
//                                if (((rotX+rotY+rotZ)%2)>0)
//                                {
//                                    for(int i=0;i<trcnt;++i)
//                                    {
//                                        tricolorf.put(3*index, tricolor1.get(9*i));
//                                        tricolorf.put(3*index + 1, tricolor1.get(9*i + 1));
//                                        tricolorf.put(3*index + 2, tricolor1.get(9*i + 2));
//                                        trinormalf.put(3*index, trinormal.get(9*i));
//                                        trinormalf.put(3*index + 1, trinormal.get(9*i + 1));
//                                        trinormalf.put(3*index + 2, trinormal.get(9*i + 2));
//                                        trivertexf.put(3*index, trivertex.get(9*i));
//                                        trivertexf.put(3*index + 1, trivertex.get(9*i + 1));
//                                        trivertexf.put(3*index + 2, trivertex.get(9*i + 2));
//                                        index++;
//
//                                        tricolorf.put(3*index, tricolor1.get(9*i + 6));
//                                        tricolorf.put(3*index + 1, tricolor1.get(9*i + 1 + 6));
//                                        tricolorf.put(3*index + 2, tricolor1.get(9*i + 2 + 6));
//                                        trinormalf.put(3*index, trinormal.get(9*i + 6));
//                                        trinormalf.put(3*index + 1, trinormal.get(9*i + 1 + 6));
//                                        trinormalf.put(3*index + 2, trinormal.get(9*i + 2 + 6));
//                                        trivertexf.put(3*index, trivertex.get(9*i + 6));
//                                        trivertexf.put(3*index + 1, trivertex.get(9*i + 1 + 6));
//                                        trivertexf.put(3*index + 2, trivertex.get(9*i + 2 + 6));
//                                        index++;
//
//                                        tricolorf.put(3*index, tricolor1.get(9*i + 3));
//                                        tricolorf.put(3*index + 1, tricolor1.get(9*i + 1 + 3));
//                                        tricolorf.put(3*index + 2, tricolor1.get(9*i + 2 + 3));
//                                        trinormalf.put(3*index, trinormal.get(9*i + 3));
//                                        trinormalf.put(3*index + 1, trinormal.get(9*i + 1 + 3));
//                                        trinormalf.put(3*index + 2, trinormal.get(9*i + 2 + 3));
//                                        trivertexf.put(3*index, trivertex.get(9*i + 3));
//                                        trivertexf.put(3*index + 1, trivertex.get(9*i + 1 + 3));
//                                        trivertexf.put(3*index + 2, trivertex.get(9*i + 2 + 3));
//                                        index++;
//                                    }
//                                }
//                                else
//                                {
//                                    for(int i=0;i<trcnt;++i)
//                                    {
//                                        tricolorf.put(3*index, tricolor1.get(9*i));
//                                        tricolorf.put(3*index + 1, tricolor1.get(9*i + 1));
//                                        tricolorf.put(3*index + 2, tricolor1.get(9*i + 2));
//                                        trinormalf.put(3*index, trinormal.get(9*i));
//                                        trinormalf.put(3*index + 1, trinormal.get(9*i + 1));
//                                        trinormalf.put(3*index + 2, trinormal.get(9*i + 2));
//                                        trivertexf.put(3*index, trivertex.get(9*i));
//                                        trivertexf.put(3*index + 1, trivertex.get(9*i + 1));
//                                        trivertexf.put(3*index + 2, trivertex.get(9*i + 2));
//                                        index++;
//
//                                        tricolorf.put(3*index, tricolor1.get(9*i + 6));
//                                        tricolorf.put(3*index + 1, tricolor1.get(9*i + 1 + 6));
//                                        tricolorf.put(3*index + 2, tricolor1.get(9*i + 2 + 6));
//                                        trinormalf.put(3*index, trinormal.get(9*i + 6));
//                                        trinormalf.put(3*index + 1, trinormal.get(9*i + 1 + 6));
//                                        trinormalf.put(3*index + 2, trinormal.get(9*i + 2 + 6));
//                                        trivertexf.put(3*index, trivertex.get(9*i + 6));
//                                        trivertexf.put(3*index + 1, trivertex.get(9*i + 1 + 6));
//                                        trivertexf.put(3*index + 2, trivertex.get(9*i + 2 + 6));
//                                        index++;
//
//                                        tricolorf.put(3*index, tricolor1.get(9*i + 3));
//                                        tricolorf.put(3*index + 1, tricolor1.get(9*i + 1 + 3));
//                                        tricolorf.put(3*index + 2, tricolor1.get(9*i + 2 + 3));
//                                        trinormalf.put(3*index, trinormal.get(9*i + 3));
//                                        trinormalf.put(3*index + 1, trinormal.get(9*i + 1 + 3));
//                                        trinormalf.put(3*index + 2, trinormal.get(9*i + 2 + 3));
//                                        trivertexf.put(3*index, trivertex.get(9*i + 3));
//                                        trivertexf.put(3*index + 1, trivertex.get(9*i + 1 + 3));
//                                        trivertexf.put(3*index + 2, trivertex.get(9*i + 2 + 3));
//                                        index++;
//                                    }
//                                }
////                                glEnd();
//                            }
//                            //glPopMatrix();
//                            Matrix.scaleM(mVMatrix, 0, 1.0f/(1.0f-2.0f*rotX), 1.0f/(1.0f-2.0f*rotY), 1.0f/(1.0f-2.0f*rotZ));
//                        }
//                //glEndList();
//                trivertexf.position(0);
//                trinormalf.position(0);
//                tricolorf.position(0);
                dlistgen = true;
            }
            fin = 2;
        }

        if (fin>0 && motion && animatemode)
        {
//            Log.d("Draw", "Animate");
            int inv = 0;
            tHandle = GLES20.glGetAttribLocation(mProgram, "a_position");
            GLES20.glVertexAttribPointer(tHandle, 3,
                    GLES20.GL_FLOAT, false,
                    0, trivertexanim);
            tHandle = GLES20.glGetAttribLocation(mProgram, "a_normal");
            GLES20.glEnableVertexAttribArray(tHandle);
            GLES20.glVertexAttribPointer(tHandle, 3,
                    GLES20.GL_FLOAT, false,
                    0, trinormalanim);
            tHandle = GLES20.glGetAttribLocation(mProgram, "a_color");
            GLES20.glEnableVertexAttribArray(tHandle);
//            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);
//            glVertexPointer(3, GL_FLOAT, 0, &trivertexanim[0]);
//            glNormalPointer(GL_FLOAT, 0, &trinormalanim[0]);
            for(int rotX = 0; rotX < 2; rotX++)
                for(int rotY = 0; rotY < 2; rotY++)
                    for(int rotZ = 0; rotZ < 2; rotZ++)
                    {
                        //glPushMatrix();
                        Matrix.scaleM(mVMatrix, 0, 1.0f-2.0f*rotX, 1.0f-2.0f*rotY, 1.0f-2.0f*rotZ);  //симметрия квадрата волновой ф-ии относительно инверсии координат
                        //glRotatef(90.0, 0.0, 1.0, 0.0);

                        Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mVMatrix, 0);

                        tHandle = GLES20.glGetUniformLocation(mProgram, "u_mvpMatrix");
                        GLES20.glUniformMatrix4fv(tHandle, 1, false, mMVPMatrix, 0);
                        tHandle = GLES20.glGetUniformLocation(mProgram, "u_mvMatrix");
                        GLES20.glUniformMatrix4fv(tHandle, 1, false, mVMatrix, 0);

                        if (rotY>0 && (((l-m) & 1))>0) inv = 1;   //волновая ф-я меняет знак в случае инверсии по z когда l-|m| - нечетное
                        else inv = 0;
                        if (drawrks && m!=0)   // для действительных ф-й знак может менятся и при инверсии x и y
                        {
                            if (rotZ>0 && rotX==0 && (m%2==0) && !drawsign) inv++;
                            if (rotZ>0 && rotX==0 && (m & 1)>0 && drawsign) inv++;
                            if (rotX>0 && rotZ==0 && !drawsign) inv++;
                            if (rotX>0 && rotZ>0 && (m & 1)>0) inv++;
                        }

                        tHandle = GLES20.glGetAttribLocation(mProgram, "a_color");
                        if (inv%2>0) GLES20.glVertexAttribPointer(tHandle, 3,
                                GLES20.GL_FLOAT, false,
                                0, tricoloranim2);  //выбор цвета
                        else GLES20.glVertexAttribPointer(tHandle, 3,
                                GLES20.GL_FLOAT, false,
                                0, tricoloranim1);

                        if (((rotX+rotY+rotZ)%2)>0) GLES20.glFrontFace(GLES20.GL_CW);  //в случае нечетного количества инверсий
                        else GLES20.glFrontFace(GLES20.GL_CCW);                    //сменится порядок обхода верщин в полигонах

                        //glDrawElements(GL_TRIANGLES, 3*trcnt, GL_UNSIGNED_INT, indi);
                        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3*trcntanim);
//                        glPopMatrix();
                        Matrix.scaleM(mVMatrix, 0, 1.0f/(1.0f-2.0f*rotX), 1.0f/(1.0f-2.0f*rotY), 1.0f/(1.0f-2.0f*rotZ));
                    }
            tHandle = GLES20.glGetAttribLocation(mProgram, "a_normal");
            GLES20.glDisableVertexAttribArray(tHandle);
            tHandle = GLES20.glGetAttribLocation(mProgram, "a_color");
            GLES20.glDisableVertexAttribArray(tHandle);
        }
        else if (fin>0 && !animatemode && dlistgen) {
//            Log.d("Draw", "Normal1");
            int inv = 0;
            tHandle = GLES20.glGetAttribLocation(mProgram, "a_position");
            GLES20.glVertexAttribPointer(tHandle, 3,
                    GLES20.GL_FLOAT, false,
                    0, trivertex);
            tHandle = GLES20.glGetAttribLocation(mProgram, "a_normal");
            GLES20.glEnableVertexAttribArray(tHandle);
            GLES20.glVertexAttribPointer(tHandle, 3,
                    GLES20.GL_FLOAT, false,
                    0, trinormal);
            tHandle = GLES20.glGetAttribLocation(mProgram, "a_color");
            GLES20.glEnableVertexAttribArray(tHandle);
            for(int rotX = 0; rotX < 2; rotX++)
                for(int rotY = 0; rotY < 2; rotY++)
                    for(int rotZ = 0; rotZ < 2; rotZ++)
                    {
                        //glPushMatrix();
//                        if (rotX==1 && rotY==1 && rotZ==1) continue;
                        Matrix.scaleM(mVMatrix, 0, 1.0f-2.0f*rotX, 1.0f-2.0f*rotY, 1.0f-2.0f*rotZ);  //симметрия квадрата волновой ф-ии относительно инверсии координат
                        //glRotatef(90.0, 0.0, 1.0, 0.0);

                        Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mVMatrix, 0);

                        tHandle = GLES20.glGetUniformLocation(mProgram, "u_mvpMatrix");
                        GLES20.glUniformMatrix4fv(tHandle, 1, false, mMVPMatrix, 0);
                        tHandle = GLES20.glGetUniformLocation(mProgram, "u_mvMatrix");
                        GLES20.glUniformMatrix4fv(tHandle, 1, false, mVMatrix, 0);

                        if (rotY>0 && (((l-m) & 1))>0) inv = 1;   //волновая ф-я меняет знак в случае инверсии по z когда l-|m| - нечетное
                        else inv = 0;
                        if (drawrks && m!=0)   // для действительных ф-й знак может менятся и при инверсии x и y
                        {
                            if (rotZ>0 && rotX==0 && (m%2==0) && !drawsign) inv++;
                            if (rotZ>0 && rotX==0 && (m & 1)>0 && drawsign) inv++;
                            if (rotX>0 && rotZ==0 && !drawsign) inv++;
                            if (rotX>0 && rotZ>0 && (m & 1)>0) inv++;
                        }

                        tHandle = GLES20.glGetAttribLocation(mProgram, "a_color");
                        if (inv%2>0) GLES20.glVertexAttribPointer(tHandle, 3,
                                GLES20.GL_FLOAT, false,
                                0, tricolor2);  //выбор цвета
                        else GLES20.glVertexAttribPointer(tHandle, 3,
                                GLES20.GL_FLOAT, false,
                                0, tricolor1);

                        if (((rotX+rotY+rotZ)%2)>0) GLES20.glFrontFace(GLES20.GL_CW);  //в случае нечетного количества инверсий
                        else GLES20.glFrontFace(GLES20.GL_CCW);                    //сменится порядок обхода верщин в полигонах

                        //glDrawElements(GL_TRIANGLES, 3*trcnt, GL_UNSIGNED_INT, indi);
//                        Log.d("Draw", "trcnt = " + trcnt);
//                        Log.d("Draw", "x: " + trinormal.get(3) + "  y: " + trinormal.get(4) + "  z: " + trinormal.get(5));
                        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3*trcnt);
                        //GLES20.glDrawArrays(GLES20.GL_LINES, 0, 3*trcnt);
//                        glPopMatrix();
                        Matrix.scaleM(mVMatrix, 0, 1.0f/(1.0f-2.0f*rotX), 1.0f/(1.0f-2.0f*rotY), 1.0f/(1.0f-2.0f*rotZ));
                    }
            tHandle = GLES20.glGetAttribLocation(mProgram, "a_normal");
            GLES20.glDisableVertexAttribArray(tHandle);
            tHandle = GLES20.glGetAttribLocation(mProgram, "a_color");
            GLES20.glDisableVertexAttribArray(tHandle);
//            Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mVMatrix, 0);
//
//            tHandle = GLES20.glGetUniformLocation(mProgram, "u_mvpMatrix");
//            GLES20.glUniformMatrix4fv(tHandle, 1, false, mMVPMatrix, 0);
//            tHandle = GLES20.glGetUniformLocation(mProgram, "u_mvMatrix");
//            GLES20.glUniformMatrix4fv(tHandle, 1, false, mVMatrix, 0);
//
//            tHandle = GLES20.glGetAttribLocation(mProgram, "a_position");
//            GLES20.glVertexAttribPointer(tHandle, 3,
//                    GLES20.GL_FLOAT, false,
//                    0, trivertexf);
//            tHandle = GLES20.glGetAttribLocation(mProgram, "a_normal");
//            GLES20.glEnableVertexAttribArray(tHandle);
//            GLES20.glVertexAttribPointer(tHandle, 3,
//                    GLES20.GL_FLOAT, false,
//                    0, trinormalf);
//
//            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3*8*trcnt);
//
//            tHandle = GLES20.glGetAttribLocation(mProgram, "a_normal");
//            GLES20.glDisableVertexAttribArray(tHandle);
        }
        else
        {
//            Log.d("Draw", "Normal");
            int inv = 0;
            tHandle = GLES20.glGetAttribLocation(mProgram, "a_position");
            GLES20.glVertexAttribPointer(tHandle, 3,
                    GLES20.GL_FLOAT, false,
                    0, trivertex);
            tHandle = GLES20.glGetAttribLocation(mProgram, "a_normal");
            GLES20.glEnableVertexAttribArray(tHandle);
            GLES20.glVertexAttribPointer(tHandle, 3,
                    GLES20.GL_FLOAT, false,
                    0, trinormal);
            tHandle = GLES20.glGetAttribLocation(mProgram, "a_color");
            GLES20.glEnableVertexAttribArray(tHandle);
            for(int rotX = 0; rotX < 2; rotX++)
                for(int rotY = 0; rotY < 2; rotY++)
                    for(int rotZ = 0; rotZ < 2; rotZ++)
                    {
//                        if (rotX==1 && rotY==1 && rotZ==1) continue;
                        //glPushMatrix();
                        Matrix.scaleM(mVMatrix, 0, 1.0f-2.0f*rotX, 1.0f-2.0f*rotY, 1.0f-2.0f*rotZ);  //симметрия квадрата волновой ф-ии относительно инверсии координат
                        //glRotatef(90.0, 0.0, 1.0, 0.0);

                        Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mVMatrix, 0);

                        tHandle = GLES20.glGetUniformLocation(mProgram, "u_mvpMatrix");
                        GLES20.glUniformMatrix4fv(tHandle, 1, false, mMVPMatrix, 0);
                        tHandle = GLES20.glGetUniformLocation(mProgram, "u_mvMatrix");
                        GLES20.glUniformMatrix4fv(tHandle, 1, false, mVMatrix, 0);

                        if (rotY>0 && (((l-m) & 1))>0) inv = 1;   //волновая ф-я меняет знак в случае инверсии по z когда l-|m| - нечетное
                        else inv = 0;
                        if (drawrks && m!=0)   // для действительных ф-й знак может менятся и при инверсии x и y
                        {
                            if (rotZ>0 && rotX==0 && (m%2==0) && !drawsign) inv++;
                            if (rotZ>0 && rotX==0 && (m & 1)>0 && drawsign) inv++;
                            if (rotX>0 && rotZ==0 && !drawsign) inv++;
                            if (rotX>0 && rotZ>0 && (m & 1)>0) inv++;
                        }

                        tHandle = GLES20.glGetAttribLocation(mProgram, "a_color");
                        if (inv%2>0) GLES20.glVertexAttribPointer(tHandle, 3,
                                GLES20.GL_FLOAT, false,
                                0, tricolor2);  //выбор цвета
                        else GLES20.glVertexAttribPointer(tHandle, 3,
                                GLES20.GL_FLOAT, false,
                                0, tricolor1);

                        if (((rotX+rotY+rotZ)%2)>0) GLES20.glFrontFace(GLES20.GL_CW);  //в случае нечетного количества инверсий
                        else GLES20.glFrontFace(GLES20.GL_CCW);                    //сменится порядок обхода верщин в полигонах

                        //glDrawElements(GL_TRIANGLES, 3*trcnt, GL_UNSIGNED_INT, indi);
//                        Log.d("Draw", "trcnt = " + trcnt);
                        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3*trcnt);
//                        glPopMatrix();
                        Matrix.scaleM(mVMatrix, 0, 1.0f/(1.0f-2.0f*rotX), 1.0f/(1.0f-2.0f*rotY), 1.0f/(1.0f-2.0f*rotZ));
                    }
            tHandle = GLES20.glGetAttribLocation(mProgram, "a_normal");
            GLES20.glDisableVertexAttribArray(tHandle);
            tHandle = GLES20.glGetAttribLocation(mProgram, "a_color");
            GLES20.glDisableVertexAttribArray(tHandle);
        }

        tHandle = GLES20.glGetUniformLocation(mProgram, "light");
        GLES20.glUniform1i(tHandle, 0);

//        glMatrixMode(GL_PROJECTION);
//        glLoadIdentity();
//        glOrtho(0, Width, 0, Height, -1.0, 1.0);
//
//        glMatrixMode(GL_MODELVIEW);
//        glLoadIdentity();
//
//        glColor3ub(255,255,255);
//        Log.d("Draw", "Finish");
    }

    float fSample(float fX, float fY, float fZ)
    {
        //GLdouble fResult = dblprec::disfiel(distR,distTh,m,fZ*avt/100,fX*avt/100,fY*avt/100);
//        Log.d("fSample", "Start");
//        Log.d("fSample", Float.toString(fX) + " " + Float.toString(fY) + " " + Float.toString(fZ));
        double fResult = hAtom.ksi(fZ*avt1/100,fX*avt1/100,fY*avt1/100,n,l,m);
//        Log.d("fSample", Double.toString(fResult));
//        Log.d("fSample", "Finish");
        //fout << fResult << "\n";
        return (float) (fResult*fResult);
    }

    //fGetOffset finds the approximate point of intersection of the surface
// between two points with the values fValue1 and fValue2
    float fGetOffset(float fValue1, float fValue2, float fValueDesired)
    {
        double fDelta = fValue2 - fValue1;

        if(fDelta == 0.0)
        {
            return 0.5f;
        }
        return (float)((fValueDesired - fValue1)/fDelta);
    }

    //vGetColor generates a color from a given position and normal of a point
    void vGetColor(GLvector rfColor, GLvector rfPosition, GLvector rfNormal)
    {
        float fX = rfNormal.fX;
        float fY = rfNormal.fY;
        float fZ = rfNormal.fZ;
        rfColor.fX = (fX > 0.0f ? fX : 0.0f) + (fY < 0.0f ? -0.5f*fY : 0.0f) + (fZ < 0.0f ? -0.5f*fZ : 0.0f);
        rfColor.fY = (fY > 0.0f ? fY : 0.0f) + (fZ < 0.0f ? -0.5f*fZ : 0.0f) + (fX < 0.0f ? -0.5f*fX : 0.0f);
        rfColor.fZ = (fZ > 0.0f ? fZ : 0.0f) + (fX < 0.0f ? -0.5f*fX : 0.0f) + (fY < 0.0f ? -0.5f*fY : 0.0f);
    }

    void vNormalizeVector(GLvector rfVectorResult, GLvector rfVectorSource)
    {
        float fOldLength;
        float fScale;

        fOldLength = (float)Math.sqrt( (rfVectorSource.fX * rfVectorSource.fX) +
                (rfVectorSource.fY * rfVectorSource.fY) +
                (rfVectorSource.fZ * rfVectorSource.fZ) );

        if(fOldLength == 0.0)
        {
            rfVectorResult.fX = rfVectorSource.fX;
            rfVectorResult.fY = rfVectorSource.fY;
            rfVectorResult.fZ = rfVectorSource.fZ;
        }
        else
        {
            fScale = 1.0f/fOldLength;
            rfVectorResult.fX = rfVectorSource.fX*fScale;
            rfVectorResult.fY = rfVectorSource.fY*fScale;
            rfVectorResult.fZ = rfVectorSource.fZ*fScale;
        }
    }

    void vGetNormal(GLvector rfNormal, float fX, float fY, float fZ)
    {
        rfNormal.fX = fSample(fX-0.01f, fY, fZ) - fSample(fX+0.01f, fY, fZ);
        rfNormal.fY = fSample(fX, fY-0.01f, fZ) - fSample(fX, fY+0.01f, fZ);
        rfNormal.fZ = fSample(fX, fY, fZ-0.01f) - fSample(fX, fY, fZ+0.01f);
        vNormalizeVector(rfNormal, rfNormal);
    }

    void vMarchCube(int ix, int iy, int iz, float fScale)
    {
        //extern GLint aiCubeEdgeFlags[256];
        //extern GLint a2iTriangleConnectionTable[256][16];

        int iCorner, iVertex, iVertexTest, iEdge, iTriangle, iFlagIndex, iEdgeFlags;
        float fOffset;
        GLvector sColor;
        float afCubeValue[] = new float[8];
        GLvector asEdgeVertex[] = new GLvector[12];
        GLvector asEdgeNorm[] = new GLvector[12];
        for(int i=0;i<12;++i)
        {
            asEdgeVertex[i] = new GLvector();
            asEdgeNorm[i] = new GLvector();
        }
        float fX, fY, fZ;
        fX = -250.0f + (ix)*fScale;
        fY = -250.0f + (iy)*fScale;
        fZ = -250.0f + (iz)*fScale;

        //Make a local copy of the values at the cube's corners
        for(iVertex = 0; iVertex < 8; iVertex++)
        {
            afCubeValue[iVertex] = valtable[index_table(ix+(int)MarchingCubes.a2fVertexOffset[iVertex][0],iy+(int)MarchingCubes.a2fVertexOffset[iVertex][1],iz+(int)MarchingCubes.a2fVertexOffset[iVertex][2])];
            //valtable[ix+(int)a2fVertexOffset[iVertex][0]][iy+(int)a2fVertexOffset[iVertex][1]][iz+(int)a2fVertexOffset[iVertex][2]];/*fSample(fX + a2fVertexOffset[iVertex][0]*fScale,
            //                                   fY + a2fVertexOffset[iVertex][1]*fScale,
            //                                   fZ + a2fVertexOffset[iVertex][2]*fScale);*/
            //afCubeValue[iVertex] *= afCubeValue[iVertex];
        }

        //Find which vertices are inside of the surface and which are outside
        iFlagIndex = 0;
        for(iVertexTest = 0; iVertexTest < 8; iVertexTest++)
        {
            if(afCubeValue[iVertexTest] <= fTargetValue)
                iFlagIndex |= 1<<iVertexTest;
        }

        //Find which edges are intersected by the surface
        iEdgeFlags = MarchingCubes.aiCubeEdgeFlags[iFlagIndex];

        //If the cube is entirely inside or outside of the surface, then there will be no intersections
        if(iEdgeFlags == 0)
        {
            return;
        }

        //Find the point of intersection of the surface with each edge
        //Then find the normal to the surface at those points
        for(iEdge = 0; iEdge < 12; iEdge++)
        {
            //if there is an intersection on this edge
            if ((iEdgeFlags & (1<<iEdge))!=0)
            {
                fOffset = fGetOffset(afCubeValue[ MarchingCubes.a2iEdgeConnection[iEdge][0] ],
                        afCubeValue[ MarchingCubes.a2iEdgeConnection[iEdge][1] ], fTargetValue);

                asEdgeVertex[iEdge].fX = fX + (MarchingCubes.a2fVertexOffset[ MarchingCubes.a2iEdgeConnection[iEdge][0] ][0]  +  fOffset * MarchingCubes.a2fEdgeDirection[iEdge][0]) * fScale;
                asEdgeVertex[iEdge].fY = fY + (MarchingCubes.a2fVertexOffset[ MarchingCubes.a2iEdgeConnection[iEdge][0] ][1]  +  fOffset * MarchingCubes.a2fEdgeDirection[iEdge][1]) * fScale;
                asEdgeVertex[iEdge].fZ = fZ + (MarchingCubes.a2fVertexOffset[ MarchingCubes.a2iEdgeConnection[iEdge][0] ][2]  +  fOffset * MarchingCubes.a2fEdgeDirection[iEdge][2]) * fScale;

                vGetNormal(asEdgeNorm[iEdge], asEdgeVertex[iEdge].fX, asEdgeVertex[iEdge].fY, asEdgeVertex[iEdge].fZ);
            }
        }


        //Draw the triangles that were found.  There can be up to five per cube
        boolean fl = true;
        if (!anim)
        {
            for(iTriangle = 0; iTriangle < 5 && fl; iTriangle++)
            {
                if(MarchingCubes.a2iTriangleConnectionTable[iFlagIndex][3*iTriangle] < 0)
                    break;

                if ((9*trcnt + 9)>=size1) {
                    fl = false;
                    overflow = true;
                    break;
                }

//                if ((9*trcnt + 9)>=size1) {
//                    overflow = true;
//                    trivertex = expandFB(trivertex, size2, size1);
//                    trivertex.position(0);
//
//                    trinormal = expandFB(trinormal, size2, size1);
//                    trinormal.position(0);
//
//                    tricolor1 = expandFB(tricolor1, size2, size1);
//                    tricolor1.position(0);
//
//                    tricolor2 = expandFB(tricolor2, size2, size1);
//                    tricolor2.position(0);
//                }


                //for(int invZ=0;invZ<1 && fl;invZ++)
                //for(int invX=0;invX<1 && fl;invX++)
                //for(int invY=0;invY<1 && fl;invY++)
                //{
//                if ((9*trcnt + 9)>=size1 && !overflow)
//                {
//                    return;
//                    //candraw = 0;
//                    overflow = true;
//                    trivertex = ByteBuffer.allocateDirect(
//                            // (number of coordinate values * 4 bytes per float)
//                            size2 * 3 * 4).
//                            order(ByteOrder.nativeOrder()).
//                            asFloatBuffer();
//                    trinormal = ByteBuffer.allocateDirect(
//                            // (number of coordinate values * 4 bytes per float)
//                            size2 * 3 * 4).
//                            order(ByteOrder.nativeOrder()).
//                            asFloatBuffer();
//                    tricolor1 = ByteBuffer.allocateDirect(
//                            // (number of coordinate values * 4 bytes per float)
//                            size2 * 3 * 4).
//                            order(ByteOrder.nativeOrder()).
//                            asFloatBuffer();
//                    tricolor2 = ByteBuffer.allocateDirect(
//                            // (number of coordinate values * 4 bytes per float)
//                            size2 * 3 * 4).
//                            order(ByteOrder.nativeOrder()).
//                            asFloatBuffer();
//
//                }
                for(iCorner = 0; iCorner < 3; iCorner++)
                {
                    iVertex = MarchingCubes.a2iTriangleConnectionTable[iFlagIndex][3*iTriangle+iCorner];


                    //TrVertex tmp;
							/*fdbg = fopen("debug.txt", "w");
							fprintf(fdbg, "%d\t%d\n", 9*trcnt + 3*iCorner, trivertex.size());
							fclose(fdbg);*/
                    trivertex.put(9*trcnt + 3*iCorner, asEdgeVertex[iVertex].fX);
                    trivertex.put(9*trcnt + 3*iCorner + 1, asEdgeVertex[iVertex].fY);
                    trivertex.put(9*trcnt + 3*iCorner + 2, asEdgeVertex[iVertex].fZ);
                    trinormal.put(9*trcnt + 3*iCorner, asEdgeNorm[iVertex].fX);
                    trinormal.put(9*trcnt + 3*iCorner + 1, asEdgeNorm[iVertex].fY);
                    trinormal.put(9*trcnt + 3*iCorner + 2, asEdgeNorm[iVertex].fZ);
                    //массив цветов для вершин, второй массив нужен в случае перемены знака при инверсии координат
                    //if (dblprec::ksi(tx,ty,tz,n,l,m)>0)
                    if (hAtom.ksi(trivertex.get(9*trcnt + 3*iCorner + 2)*avt1/100,
                            trivertex.get(9*trcnt + 3*iCorner)*avt1/100,
                            trivertex.get(9*trcnt + 3*iCorner + 1)*avt1/100,n,l,m)>0)
                    {
                        tricolor1.put(9*trcnt + 3*iCorner, 1.0f);
                        tricolor1.put(9*trcnt + 3*iCorner + 1, 0.0f);
                        tricolor1.put(9*trcnt + 3*iCorner + 2, 0.0f);
                        tricolor2.put(9*trcnt + 3*iCorner, 0.0f);
                        tricolor2.put(9*trcnt + 3*iCorner + 1, 0.0f);
                        tricolor2.put(9*trcnt + 3*iCorner + 2, 1.0f);
                    }
                    else
                    {
                        tricolor1.put(9*trcnt + 3*iCorner, 0.0f);
                        tricolor1.put(9*trcnt + 3*iCorner + 1, 0.0f);
                        tricolor1.put(9*trcnt + 3*iCorner + 2, 1.0f);
                        tricolor2.put(9*trcnt + 3*iCorner, 1.0f);
                        tricolor2.put(9*trcnt + 3*iCorner + 1, 0.0f);
                        tricolor2.put(9*trcnt + 3*iCorner + 2, 0.0f);
                    }
                }
                //если в одном полигоне вершины не имеют одинаковый цвет
                //то дискретизация недостаточна, нужно разбивать поверхность
                //на более мелкие куски
                //if (tricolor1[9*trcnt + 3*0]==tricolor1[9*trcnt + 3*1] && tricolor1[9*trcnt + 3*0]==tricolor1[9*trcnt + 3*2])
                {
						/*if ((invX+invZ+invY)%2>0)
							rep(jk,3)
							{
								swap(trivertex[9*trcnt + 3*1 + jk],trivertex[9*trcnt + 3*2 + jk]);
								swap(trinormal[9*trcnt + 3*1 + jk],trinormal[9*trcnt + 3*2 + jk]);
							}*/
                    trcnt++;
                }
					/*else
					{
						trcnt -= iTriangle;
						fl = 0;
						break;
					}*/
                //}
            }
        }
        else
        {
            for(iTriangle = 0; iTriangle < 5; iTriangle++)
            {
                if(MarchingCubes.a2iTriangleConnectionTable[iFlagIndex][3*iTriangle] < 0)
                    break;

                //for(int invZ=0;invZ<1 && fl;invZ++)
                //for(int invX=0;invX<1 && fl;invX++)
                //for(int invY=0;invY<1 && fl;invY++)
                //{
                if ((9*trcntanim + 9)>=size1) {
                    fl = false;
                    overflow = true;
                    break;
                }

//                if ((9*trcntanim + 9)>=size1) {
//                    overflow = true;
//
//                    trivertexanim = expandFB(trivertexanim, size2, size1);
//                    trivertexanim.position(0);
//
//                    trinormalanim = expandFB(trinormalanim, size2, size1);
//                    trinormalanim.position(0);
//
//                    tricoloranim1 = expandFB(tricoloranim1, size2, size1);
//                    tricoloranim1.position(0);
//
//                    tricoloranim2 = expandFB(tricoloranim2, size2, size1);
//                    tricoloranim2.position(0);
//                }



                for(iCorner = 0; iCorner < 3; iCorner++)
                {
                    iVertex = MarchingCubes.a2iTriangleConnectionTable[iFlagIndex][3*iTriangle+iCorner];


                    trivertexanim.put(9*trcntanim + 3*iCorner, asEdgeVertex[iVertex].fX);
                    trivertexanim.put(9*trcntanim + 3*iCorner + 1, asEdgeVertex[iVertex].fY);
                    trivertexanim.put(9*trcntanim + 3*iCorner + 2, asEdgeVertex[iVertex].fZ);
                    trinormalanim.put(9*trcntanim + 3*iCorner, asEdgeNorm[iVertex].fX);
                    trinormalanim.put(9*trcntanim + 3*iCorner + 1, asEdgeNorm[iVertex].fY);
                    trinormalanim.put(9*trcntanim + 3*iCorner + 2, asEdgeNorm[iVertex].fZ);
                    if (hAtom.ksi(trivertexanim.get(9*trcntanim + 3*iCorner + 2)*avt1/100,
                            trivertexanim.get(9*trcntanim + 3*iCorner)*avt1/100,
                            trivertexanim.get(9*trcntanim + 3*iCorner + 1)*avt1/100,n,l,m)>0)
                    {
                        tricoloranim1.put(9*trcntanim + 3*iCorner, 1.0f);
                        tricoloranim1.put(9*trcntanim + 3*iCorner + 1, 0.0f);
                        tricoloranim1.put(9*trcntanim + 3*iCorner + 2, 0.0f);
                        tricoloranim2.put(9*trcntanim + 3*iCorner, 0.0f);
                        tricoloranim2.put(9*trcntanim + 3*iCorner + 1, 0.0f);
                        tricoloranim2.put(9*trcntanim + 3*iCorner + 2, 1.0f);
                    }
                    else
                    {
                        tricoloranim1.put(9*trcntanim + 3*iCorner, 0.0f);
                        tricoloranim1.put(9*trcntanim + 3*iCorner + 1, 0.0f);
                        tricoloranim1.put(9*trcntanim + 3*iCorner + 2, 1.0f);
                        tricoloranim2.put(9*trcntanim + 3*iCorner, 1.0f);
                        tricoloranim2.put(9*trcntanim + 3*iCorner + 1, 0.0f);
                        tricoloranim2.put(9*trcntanim + 3*iCorner + 2, 0.0f);
                    }
                }
                trcntanim++;
                //}
            }
        }
        //разбитие на еще более мелкие куски в случае необходимости
		/*if (fl == 0 && !anim)
		{
			for(double ftX=fX;ftX<fX+fScale-1e-5;ftX+=fScale/2)
			for(double ftY=fY;ftY<fY+fScale-1e-5;ftY+=fScale/2)
			for(double ftZ=fZ;ftZ<fZ+fScale-1e-5;ftZ+=fScale/2)
			{
				vMarchCube2(ftX, ftY, ftZ, fScale/2);
			}
		}*/
    }

    //генерация всей повехности алгоритмом Marching Cubes
    public void vMarchingCubes()
    {
        //GLfloat iX, iY, iZ;
        //Tris.resize(0);
        //candraw = 0;
//        Log.d("MarchingCubes", "Start");
        trcntanim = trcnt = 0;
        totalprogress = progress = 0;
        //vector<double> sortvaltable(0);
        double [] sortvaltable;
        /*for(iX = 0; iX < iDataSetSize; iX++)
        for(iY = 0; iY < iDataSetSize; iY++)
        for(iZ = 0; iZ < iDataSetSize; iZ++)*/
		/*for(iY = -250; iY < 0; iY+=fStepSize1)
		for(iX = -250; iX < 0; iX+=fStepSize1)
        for(iZ = -250; iZ < 0; iZ+=fStepSize1)
        {
                vMarchCube(iX, iY, iZ, fStepSize1);
        }*/
        anim = true;
        fStepSize1 = Math.max(fStepSizeanim, fStepSize);
        int ncbs = (int)(250.0f/fStepSize1+1);
        ncubesanim = ncbs;
        int tncbs = (int)(250.0f/fStepSize+1);
        totalprogress = (ncbs)*(ncbs)*(ncbs) + (tncbs)*(tncbs)*(tncbs);
        //valtable.resize((ncbs+1)*(ncbs+1)*(ncbs+1));
        valtable = new float[(ncbs+1)*(ncbs+1)*(ncbs+1)];
        int nraz = ncbs/101 + 1;
        maxinda = ncbs+1;
        int cbx,cby,cbz;
		/*rep(iY,nraz)
			rep(iX,nraz)
				rep(iZ,nraz)*/
//        Log.d("MarchingCubes", "Anim start");
        {
					/*cby = min(100, ncbs - iY*100);
					cbx = min(100, ncbs - iX*100);
					cbz = min(100, ncbs - iZ*100);*/
            for(int iy=0;iy<ncbs+1 && !stopThread;iy++)
                for(int ix=0;ix<ncbs+1 && !stopThread;ix++)
                    for(int iz=0;iz<ncbs+1 && !stopThread;iz++)
                        valtable[index_table(ix,iy,iz)] = fSample(-250.0f + fStepSize1*(ix),
                                -250.0f + fStepSize1*(iy),
                                -250.0f + fStepSize1*(iz));


            for(int iy=0;iy<ncbs && !overflow && !stopThread;iy++)
                for(int ix=0;ix<ncbs && !overflow && !stopThread;ix++)
                    for(int iz=0;iz<ncbs && !overflow && !stopThread;iz++) {
                        vMarchCube(ix, iy, iz, fStepSize1);
                        progress++;
                    }
        }
//        Log.d("MarchingCubes", "Anim finish");
        anim = false;
        fStepSize1 = fStepSize;

        ncbs = (int)(250.0f/fStepSize1+1);
        ncubes = ncbs;
        //valtable.resize((ncbs+1)*(ncbs+1)*(ncbs+1));
        valtable = new float[(ncbs+1)*(ncbs+1)*(ncbs+1)];
        nraz = ncbs/101 + 1;
        maxinda = ncbs+1;
//        Log.d("MarchingCubes", "Normal start");
        {
            for(int iy=0;iy<ncbs+1 && !stopThread;iy++)
                for(int ix=0;ix<ncbs+1 && !stopThread;ix++)
                    for(int iz=0;iz<ncbs+1 && !stopThread;iz++)
                        valtable[index_table(ix,iy,iz)] = fSample(-250.0f + fStepSize1*(ix),
                                    -250.0f + fStepSize1*(iy),
                                    -250.0f + fStepSize1*(iz));

            for(int iy=0;iy<ncbs && !overflow && !stopThread;iy++)
                for(int ix=0;ix<ncbs && !overflow && !stopThread;ix++)
                    for(int iz=0;iz<ncbs && !overflow && !stopThread;iz++) {
                        vMarchCube(ix, iy, iz, fStepSize1);
                        progress++;
                    }
        }
//        Log.d("MarchingCubes", "Normal finish");


        //valtable.clear();
       // vector<float>(valtable).swap(valtable);
        valtable = null;
//        Log.d("MarchingCubes", "trcntanim = " + trcntanim);
//        Log.d("MarchingCubes", "trcnt = " + trcnt);
//        Log.d("MarchingCubes", "Finish");
        fin = 1; //построение поверхности завершено
        //_endthread();
        //candraw = 1;
    }

    public void setRealKsi(boolean real) {
        drawrks = real;
        hAtom.realksi = real;
    }

    void regenerate()
    {
//        Log.d("Render", "Regenerate start");
        //InterruptThread();
        fin = 0;
        trcnt = 0;
        overflow = false;
        //reallocateMemory();
        //ptcnt = 0;
//        drawrks = false;
//        hAtom.realksi = false;
        if (m<0)
        {
            hAtom.setsign(true);
            drawsign = true;
            m = -m;
        }
        else
        {
            hAtom.setsign(false);
            drawsign = false;
        }
        if (overflow)
        {
            overflow = false;
            trivertex.clear();
            //vector<float>(trivertex).swap(trivertex);
            trivertex = ByteBuffer.allocateDirect(
                    // (number of coordinate values * 4 bytes per float)
                    size1 * 3 * 4).
                    order(ByteOrder.nativeOrder()).
                    asFloatBuffer();
            trinormal.clear();
            //vector<float>(trinormal).swap(trinormal);
            trinormal = ByteBuffer.allocateDirect(
                    // (number of coordinate values * 4 bytes per float)
                    size1 * 3 * 4).
                    order(ByteOrder.nativeOrder()).
                    asFloatBuffer();
            tricolor1.clear();
            //vector<unsigned char>(tricolor1).swap(tricolor1);
            tricolor1 = ByteBuffer.allocateDirect(
                    // (number of coordinate values * 4 bytes per float)
                    size1 * 3 * 4).
                    order(ByteOrder.nativeOrder()).
                    asFloatBuffer();
            tricolor2.clear();
            //vector<unsigned char>(tricolor2).swap(tricolor2);
            tricolor2 = ByteBuffer.allocateDirect(
                    // (number of coordinate values * 4 bytes per float)
                    size1 * 3 * 4).
                    order(ByteOrder.nativeOrder()).
                    asFloatBuffer();
            //fprintf(fdbg, "%d\t%d\n", trivertex.size(), trivertex.capacity());
        }

//        glEnableClientState(GL_NORMAL_ARRAY);
//        glEnable(GL_LIGHTING);
//        glDisable(GL_BLEND);

        rounderror = false;

//        Log.d("Render", "Prepareallpov start");
        hAtom.prepareallpov(n, l, m);
//        Log.d("Render", "Prepareallpov finish");

        double xt,yt,zt;
        double [] kss = new double[1001];

        //генерация тестовых точек для определения значения квадрата волновой ф-ии,
        //отвечающего заданой вероятности
        double tr,tph,tth,mr=0,avcmp=0;

        //if (n==1) avt1 = 6.;
        //else avt1 = 2.5 * n;
        avt1 = Math.sqrt((double)n);

        //qDebug() << fTargetValue;

        //fTargetValue = hAtom.getEquiValue(pct/100., fStepSize*avt1/100, (int)(250.0f/fStepSize+1), n, l, m);
        //if (!drawrks)
//        Log.d("Render", "Get target value start");
//        Log.d("Render", "Size: " + 10*(int)(250.0f/fStepSize+1)*(int)(100.f*Math.sqrt(l+1.f)));
          fTargetValue = (float)hAtom.getEquiValue(pct/100., 10*(int)(250.0f/fStepSize+1), 2.5*avt1, (int)(100.f*Math.sqrt(l+1.f)), n, l, m);
//        fTargetValue = (float)hAtom.getEquiValue(pct/100., 2*(int)(250.0f/fStepSize+1), 2.5*avt1, (int)(20.f*Math.sqrt(l+1.f)), n, l, m);
//        Log.d("Render", "fTargetValue: " + fTargetValue);
//        Log.d("Render", "Get target value finish");

        //qDebug() << fTargetValue;

        //dlistgen = false;
        //MarchCubesThread((void*)this);
        //QtConcurrent::run(vMarchingCubes);
        //QtConcurrent::run(MarchCubesThread, (void*)this);
//        Log.d("Render", "Marching cubes start");
        //vMarchingCubes();
        stopThread = false;
        Thread t = new Thread(new Runnable() {
            public void run() {
                vMarchingCubes();
            }
        });
        t.start();

//        Log.d("Render", "Marching cubes finish");
        //_beginthread( MarchCubesThread, 0, (void*)this);  //сама генерация эквиповерхностей алгоритмом Marching Cubes
//        Log.d("Render", "Regenerate finish");
    }

    public void InterruptThread() {
        if (fin==0) {
//            Log.d("Render", "Suspending thread");
            stopThread = true;
            //genThread.interrupt();
            //genThread.interrupt();
            //genThread = null;
        }
    }
}
