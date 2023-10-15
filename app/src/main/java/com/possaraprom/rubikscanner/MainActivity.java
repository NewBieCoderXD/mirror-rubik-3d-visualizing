package com.possaraprom.rubikscanner;

import android.opengl.GLSurfaceView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import android.os.Handler;



public class MainActivity extends AppCompatActivity {
    String currentPhotoPath;
    Uri photoURI;
    Button button;
    Button button2;
    View progressBar;

    private boolean readyToExit=false;
    private Date backPressedTime;
    private GLSurfaceView surfaceView;
    private final byte[][][] orientations = new byte[6][4][3];
    private final byte[][] rubik = new byte[6][8];
    private final int[][] directions = new int[][]{
            {1, 3, 4, 2},
            {3, 0, 2, 5},
            {1, 0, 4, 5},
            {4, 0, 1, 5},
            {2, 0, 3, 5},
            {1, 2, 4, 3}
    };

    private final float frameRate = 45;
    private final float angularVelocity = 0.5f;
    public final static float lengthPerLevel = 0.1f;
    public final static float axisWidth = 3f*lengthPerLevel;
    public PhotoCube[][] objects = new PhotoCube[3][9];
    private final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        surfaceView = findViewById(R.id.surfaceView);
        button = findViewById(R.id.button);
        button2 = findViewById(R.id.button2);
        progressBar = findViewById(R.id.progressBar);
        //surfaceView.setVisibility(View.INVISIBLE);
        onActivityResult(1,RESULT_OK,new Intent());
        progressBar.setVisibility(View.VISIBLE);
        button.setVisibility(View.INVISIBLE);
        button2.setVisibility(View.INVISIBLE);

        button2.setOnClickListener((View v) -> {
            Log.v("BUTTON", "Button Triggered");
            pickImageFromGallery(Intent.ACTION_GET_CONTENT, 2);
            progressBar.setVisibility(View.VISIBLE);
            button.setVisibility(View.INVISIBLE);
            button2.setVisibility(View.INVISIBLE);
        });
        button.setOnClickListener((View v) -> {
            Log.v("BUTTON", "Button Triggered");
            dispatchTakePictureIntent(MediaStore.ACTION_IMAGE_CAPTURE);
            progressBar.setVisibility(View.VISIBLE);
            button.setVisibility(View.INVISIBLE);
            button2.setVisibility(View.INVISIBLE);
        });
    }

    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            rotateU(angularVelocity/frameRate);
            addToRender();
            surfaceView.requestRender();
            handler.postDelayed(runnable,(long)(1000/frameRate));
        }
    };


    @Override
    public void onBackPressed(){
        if(button.isShown()){
            if(readyToExit&&(new Date()).getTime()-backPressedTime.getTime()<3000){
                this.finishAffinity();
                return;
            }
            backPressedTime = new Date();
            Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show();
            readyToExit = true;
            return;
        }
        surfaceView.setVisibility(View.GONE);
        progressBar.setVisibility(View.INVISIBLE);
        button.setVisibility(View.VISIBLE);
        button2.setVisibility(View.VISIBLE);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //try {
            if(resultCode!=RESULT_OK){
                onBackPressed();
                return;
            }

            //ImageView imgView = findViewById(R.id.img);
            /*Bitmap bitmap = MediaStore.Images.Media.getBitmap(MainActivity.this.getContentResolver(),
                    (photoURI != null) ? photoURI : data.getData());*/

            //After fetching image processing
            /*if(bitmap.getWidth()>10 || bitmap.getHeight()>10){
                scale=10;
            }
            int sumX = 0;
            int sumY = 0;
            int numberOfPixels = 0;
            w = bitmap.getWidth()/scale;
            h = bitmap.getHeight()/scale;
            bitmap = Bitmap.createScaledBitmap(bitmap, w, h, false);
            Log.i("dimension: ",w+","+h);
            Bitmap.Config conf = Bitmap.Config.ARGB_8888;
            Bitmap render = Bitmap.createBitmap(w,h,conf);
            render.setHasAlpha(true);
            for(int i=0;i<w;i++){
                for(int j=0;j<h;j++) {
                    String colorCode = String.format("#%06x", (0xffffffff & bitmap.getPixel(i,j)));
                    //Log.i("distance",Double.toString(colorDistance(colorCode,"#ffb7c4d7")));
                    Double.toString(colorDistance(colorCode,"#ffb7c4d7"));
                    if(colorDistance(colorCode,"#ffb7c4d7") < 10){
                        numberOfPixels++;
                        sumX+=i;
                        sumY+=j;
                        render.setPixel(i, j, 0xffffffff);
                        continue;
                    }
                    render.setPixel(i, j, 0xff000000);
                }
            }
            final int avgX = sumX/numberOfPixels;
            final int avgY = sumY/numberOfPixels;
            Log.i("average: ",sumX/numberOfPixels+" "+sumY/numberOfPixels);
            render = createLine(render,0,avgY,w,avgY);
            render = createLine(render,avgX,0,avgX,h);
            float m;*/
            {

                int side = 3;
                byte[][] orientaion = {
                        {4, 4, 4},
                        {0, 0, 0},
                        {1, 1, 1},
                        {5, 5, 5}
                };
                orientations[side] = orientaion;
                side = 0;
                orientaion = new byte[][]{
                        {1, 1, 1},
                        {3, 3, 3},
                        {4, 4, 4},
                        {2, 2, 2}
                };
                orientations[side] = orientaion;
                side = 4;
                orientaion = new byte[][]{
                        {2, 2, 2},
                        {0, 0, 0},
                        {3, 3, 3},
                        {5, 5, 5}
                };
                orientations[side] = orientaion;
                side = 2;
                orientaion = new byte[][]{
                        {1, 1, 1},
                        {0, 0, 0},
                        {4, 4, 4},
                        {5, 5, 5}
                };
                orientations[side] = orientaion;
                side = 5;
                orientaion = new byte[][]{
                        {1, 1, 1},
                        {2, 2, 2},
                        {4, 4, 4},
                        {3, 3, 3}
                };
                orientations[side] = orientaion;
                side = 1;
                orientaion = new byte[][]{
                        {3, 3, 3},
                        {0, 0, 0},
                        {2, 2, 2},
                        {5, 5, 5}
                };
                orientations[side] = orientaion;
            }
            int[] sides = new int[]{0, 1, 2, 3, 4, 5};
            for (int side : sides) {
                for (int i = 0; i < 4; i++) {
                    for (int j = 0; j < 2; j++) {
                        int n = findIndex(directions[directions[side][i]], side);
                        rubik[side][2 * i + j] = orientations[directions[side][i]][n][j];
                    }
                }
                Log.i(side+"", Arrays.toString(rubik[side]));
            }

            draw(1);
            draw(0);
            draw(-1);

            /*RubikSolving RubikSolving = new RubikSolving();
            String string = "";
            RubikSolving.saveRubik(rubik);
            for(int time=0;time<10;time++){
                String random = (new String[]{"U","R","L","F","D","B","U_","R_","L_","F_","D_","B_"})[(int) (Math.random()*5)];
                RubikSolving.call(random);
                string+=random+" ";
            }
            RubikSolving.startSolving();
            Log.i("Solution",String.join(", ",RubikSolving.solution));*/

            /*rotateU(0.5f);
            rotateU(0.5f);
            rotateU(0.5f);
            rotateU(0.5f);
            rotateU(0.5f);

            surfaceView.requestRender();*/

            handler.post(runnable);

            /*for(String scramble: "B U U B F R U U R R U U L B U U L L F U R B R B U L B F B B R U B U B U L B R L U R U F F R F L U U U B R U R B R F U U F F U U F B B U U B F R B F R F U B U U B L F B B B U F L F L R R U B B U F B F F L L B F B R U B U R L F B L F R U R U B F U F R U F R B F B L U L R L L B B F B U U U B R R R U U U F L B B U U L B B B L F L L L U U U B U U L R L U U U L B F L R U U R B B R F B F F B B R L L L R R B B F L L L F L R U B L L B U L F B U L U U U F U B L U U B B L F R F L U B U B R R B F F L L L R F L R B B L F F U B B B U R R B U L R L F R F R R L L B L B L U U R B L L L U R L R R L F L F U L B B U L U B R U R L F F F B L L B R R U B R F U F L F U F F F L B U F L F U U U L B F L L L L U B L U U L B B U U B U R U F R L F B B B B F B R R U B F L B B L U L L B F B L L R R F L L L L F L U B R F R L U L F L U F L U L U L B F F F F F L L L L R B F B U R R F U F L R B R B R B U L L L F U U L F F F U B B U L R B F F U L F B B F F F F U U F L U B L B F B U U R B F R U F U U B R B B L R F U F L R".split(" ")){
              call(scramble);
            }*/

            
            //rotate("L",false);
        /*} catch (IOException e) {
            Log.e("bitmap", "error" + e);
        }*/
    }

    private void rotateU(float angle) {
        for (PhotoCube cube : objects[0]) {
            cube.updateAngleY(angle);
            cube.update();
        }
    }
    private void rotateR() {
        //6 7 0
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                PhotoCube cube = objects[i][(j + 6) % 8];
                cube.updateAngleZ(-Math.PI / 2);
                cube.update();
            }
        }
    }
    private void rotateL() {
        //2 3 4
        /*PhotoCube cube = objects[0][2];
        cube.updateAngleZ(Math.PI/2);
        cube.update();*/
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                PhotoCube cube = objects[i][(j + 2) % 8];
                cube.updateAngleZ(Math.PI / 2);
                cube.update();
            }
        }
    }
    private void rotateF() {
        //0 1 2
        /*PhotoCube cube = objects[0][6];
        cube.updateAngleZ(Math.PI/2);
        cube.update();*/
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                PhotoCube cube = objects[i][(j + 0) % 8];
                cube.updateAngleX(Math.PI / 2);
                cube.update();
            }
        }
    }
    private void rotateB() {
        //02 03 04 12 13 14 22 23 24
                /*PhotoCube cube = objects[0][6];
                cube.updateAngleZ(Math.PI/2);
                cube.update();*/
                /*for(int i=0;i<3;i++){
                    for(int j=0;j<3;j++){
                        PhotoCube cube = objects[i][(j+0)%8];
                        cube.updateAngleX(-Math.PI/2*((IsPrime)?-1:1));
                        cube.update();
                    }
                }*/
    }

    private void addToRender(){
        MyGLRenderer.clear();
        for(PhotoCube[] PhotoCubes : objects){
            for(PhotoCube cube : PhotoCubes) {
                MyGLRenderer.add(cube);
            }
        }
    }

    private void draw(int y){
        final int[][] lists={
                {0,1,0}, //Y
                {1,0,0}, //X
                {0,0,-1}, //-Z
                {0,0,1}, //Z
                {-1,0,0}, //-X
                {0,-1,0} //-Y
        };
        int ySide = Math.round(2.5f-y*2.5f);
        for(int i=0;i<4;i++) {
            int currentSide = directions[0][i];
            int lateralSide = directions[0][(i+1+4)%4];
            int nextSide = directions[0][(i-1+4)%4];
            int n = findIndex(directions[currentSide], 0);
            for (int j = 0; j < 2; j++) {
                /*Log.i("current side",currentSide+" "+Arrays.toString(lists[currentSide]));
                Log.i("lateral side",lateralSide+" "+Arrays.toString(lists[lateralSide]));
                Log.i("w",(j*currentSide*lists[currentSide][0])+" "+((1-j)*(rubik[currentSide][1]*lists[currentSide][0]+rubik[nextSide][5]*lists[nextSide][0])));
                Log.i("d",(j*currentSide*lists[currentSide][2])+" "+((1-j)*(rubik[currentSide][1]*lists[currentSide][2]+rubik[nextSide][5]*lists[nextSide][2])));
                Log.i("x",(lists[currentSide][0]+" "+(j-1)*lists[lateralSide][0]));
                Log.i("z",(lists[currentSide][2]+" "+(j-1)*lists[lateralSide][2]));*/
                if(y==0){
                    currentSide = directions[0][i];
                    lateralSide = directions[0][(i+1+4)%4];
                    nextSide = directions[0][(i-1+4)%4];
                    ySide = 2;
                    PhotoCube mPhotoCube = new PhotoCube(
                            Math.abs(j*currentSide*lists[currentSide][0]+(1-j)*(rubik[currentSide][1]*lists[currentSide][0]+rubik[nextSide][5]*lists[nextSide][0]))*lengthPerLevel+((j==1&&lists[lateralSide][0]!=0)?axisWidth:lengthPerLevel),
                            axisWidth,
                            Math.abs(j*currentSide*lists[currentSide][2]+(1-j)*(rubik[currentSide][1]*lists[currentSide][2]+rubik[nextSide][5]*lists[nextSide][2]))*lengthPerLevel+((j==1&&lists[lateralSide][2]!=0)?axisWidth:lengthPerLevel),
                            (float) (lists[currentSide][0]+(j-1)*lists[lateralSide][0]),
                            (float) y,
                            (float) (lists[currentSide][2]+(j-1)*lists[lateralSide][2])
                    );
                    objects[1-y][2*i+j]=mPhotoCube;
                    continue;
                }
                PhotoCube mPhotoCube = new PhotoCube(
                        Math.abs((rubik[currentSide][(2*n)%8]*lists[currentSide][0]+rubik[0][2*i+j]*lists[0][0]+(1-j)*lists[lateralSide][0]*rubik[nextSide][(2*n)%8]))*lengthPerLevel+((j==1&&lists[lateralSide][0]!=0)?axisWidth:lengthPerLevel),
                        (rubik[ySide][2*i+j]+1)*lengthPerLevel,
                        Math.abs((rubik[currentSide][(2*n)%8]*lists[currentSide][2]+rubik[0][2*i+j]*lists[0][2]+(1-j)*lists[lateralSide][2]*rubik[nextSide][(2*n)%8]))*lengthPerLevel+((j==1&&lists[lateralSide][2]!=0)?axisWidth:lengthPerLevel),
                        (float) (lists[currentSide][0]+(j-1)*lists[lateralSide][0]),
                        (float) y,
                        (float) (lists[currentSide][2]+(j-1)*lists[lateralSide][2])
                );
                objects[1-y][2*i+j]=mPhotoCube;
            }
        }
        PhotoCube cube = new PhotoCube(
            axisWidth,
            (ySide+1)*lengthPerLevel,
            axisWidth,
            0,
            y,
            0
        );
        objects[1-y][8]=cube;
    }

    private int findIndex(int[] list, int value) {
        for (int i = 0; i < list.length; i++) {
            if (list[i] == value) {
                return i;
            }
        }
        return 5;
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void pickImageFromGallery(String intent, Integer activityCode) {
        Intent takePictureIntent = new Intent();
        takePictureIntent.setType("image/*");
        takePictureIntent.setAction(intent);
        takePictureIntent = Intent.createChooser(takePictureIntent, "Select Picture");
        startActivityForResult(takePictureIntent, activityCode);
    }

    private void dispatchTakePictureIntent(String intent) {
        Intent takePictureIntent = new Intent(intent);
        // Ensure that there's a camera activity to handle the intent
        Log.v("PICTURE", "Dispatch Ran");

        //if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
        //Log.v("PICTURE Package", "Package's Available");
        File photoFile = null;
        // Create the File where the photo should go
        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            Log.e("PICTURE Error", "Error" + ex);
        }
        // Continue only if the File was successfully created
        if (photoFile != null) {
            photoURI = FileProvider.getUriForFile(this,
                    BuildConfig.APPLICATION_ID + ".provider",
                    photoFile);
            Log.i("PICTURE URI", photoURI.toString());
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            startActivityForResult(takePictureIntent, 1); //activity code = 1
        }
        //}
    }
}