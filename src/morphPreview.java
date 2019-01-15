/*
Authors: David Mercado and Jared Rigdon
Created: 11/11/18
Description: basically holds the functions and methods to create a new window using the images and CPs in an animated fashion
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;

public class morphPreview extends JFrame {

    public MyImageObj morphinIMG, StartIMG, EndIMG;

    private int Rows, Cols, fps;

    public BufferedImage StartFrames[], EndFrames[];
    public int totalFrames;

    //---------------------------------------------------------------------------------
    public morphPreview(MyImageObj morphImg, MyImageObj startIMG, MyImageObj endIMG, int rows, int columns, int speed, int FPS){
        super("Morphing");

        Container c = getContentPane();
        c.setLayout(new FlowLayout());

        morphinIMG = morphImg;

        //Img components
        this.add(morphinIMG);
        StartIMG = startIMG;
        EndIMG = endIMG;

        //store the rows, columns, and speed
        Rows = rows;
        Cols = columns;
        fps = FPS;

        totalFrames = FPS * speed;

        morphLoop();

        setSize(700, 700);
        setResizable(false);
        setVisible(true);
        timeFunc(morphImg);
    }

    //---------------------------------------------------------------------------------
    //goes through one iteration of 2 for loops and repaints
    public void morphInTime(int frameCnt, int imageUse) {

        for (int i = 1; i < Cols+1; i++) {        //X's
            for (int j = 1; j < Rows + 1; j++) {    //Y's
                double moveX, moveY;
                //determine what we are calculating for
                if (imageUse == 0) {  //start img
                    moveX = morphinIMG.getIMGposX(i, j) + (frameCnt * ((EndIMG.getIMGposX(i, j) - morphinIMG.getIMGposX(i, j))/totalFrames));
                    moveY = morphinIMG.getIMGposY(i, j) + (frameCnt * ((EndIMG.getIMGposY(i, j) - morphinIMG.getIMGposY(i, j))/totalFrames));

                } else {   //end image
                    moveX = morphinIMG.getIMGposX(i, j) + (frameCnt * ((StartIMG.getIMGposX(i, j) - morphinIMG.getIMGposX(i, j))/totalFrames));
                    moveY = morphinIMG.getIMGposY(i, j) + (frameCnt * ((StartIMG.getIMGposY(i, j) - morphinIMG.getIMGposY(i, j))/totalFrames));

                }
                morphinIMG.updateIMGpos(i, j, moveX, moveY);    //update location
            }
        }

        //get the updated triangle
        for (int i = 0; i < Cols+1; i++) {        //X's
            for (int j = 0; j < Rows + 1; j++) {    //Y's
                //choose what img obj to pass in
                if(imageUse == 0){ getTriangle(i, j, StartIMG); }
                else{ getTriangle(i, j, EndIMG); }
            }
        }
        morphinIMG.repaint();
    }

    //-----------------------------------------------------------------------------------------------------------------------

    public void getTriangle(int x, int y, MyImageObj currImg){
        //get the original coords of the original img
        //4 pts to do the square
        double srcX1 = currImg.getIMGposX(x, y);    //curr cp
        double srcY1 = currImg.getIMGposY(x, y);
        double srcX2 = currImg.getIMGposX(x+1, y);  //right
        double srcY2 = currImg.getIMGposY(x+1, y);
        double srcX3 = currImg.getIMGposX(x+1, y+1);  //bottom-right
        double srcY3 = currImg.getIMGposY(x+1, y+1);
        double srcX4 = currImg.getIMGposX(x, y+1);    //bottom
        double srcY4 = currImg.getIMGposY(x, y+1);

        //get the new coords of the morphed img
        //4 pts to do the square
        double dstX1 = morphinIMG.getIMGposX(x, y);    //curr cp
        double dstY1 = morphinIMG.getIMGposY(x, y);
        double dstX2 = morphinIMG.getIMGposX(x+1, y);  //right
        double dstY2 = morphinIMG.getIMGposY(x+1, y);
        double dstX3 = morphinIMG.getIMGposX(x+1, y+1);    //bottom-right
        double dstY3 = morphinIMG.getIMGposY(x+1, y+1);
        double dstX4 = morphinIMG.getIMGposX(x, y+1);  //bottom
        double dstY4 = morphinIMG.getIMGposY(x, y+1);

        //check if looking at top right or bottom left
        if((x == Cols && y == 1) || (x == 1 && y ==Rows)){
            //triangle 1
            Triangle Src = new Triangle(srcX1, srcY1, srcX2, srcY2, srcX4, srcY4);
            Triangle Dst = new Triangle(dstX1, dstY1, dstX2, dstY2, dstX4, dstY4);
            //call warp
            warpTriangle(currImg.getImage(), morphinIMG.getImage(), Src, Dst, null, null);

            //triangle 2
            Src = new Triangle(srcX2, srcY2, srcX3, srcY3, srcX4, srcY4);
            Dst = new Triangle(dstX2, dstY2, dstX3, dstY3, dstX4, dstY4);
            //call warp
            warpTriangle(currImg.getImage(), morphinIMG.getImage(), Src, Dst, null, null);
        }
        else{
            //triangle 1
            Triangle Src = new Triangle(srcX1, srcY1, srcX2, srcY2, srcX3, srcY3);
            Triangle Dst = new Triangle(dstX1, dstY1, dstX2, dstY2, dstX3, dstY3);
            //call warp
            warpTriangle(currImg.getImage(), morphinIMG.getImage(), Src, Dst, null, null);

            //triangle 2
            Src = new Triangle(srcX3, srcY3, srcX4, srcY4, srcX1, srcY1);
            Dst = new Triangle(dstX3, dstY3, dstX4, dstY4, dstX1, dstY1);
            //call warp
            warpTriangle(currImg.getImage(), morphinIMG.getImage(), Src, Dst, null, null);
        }

    }

//-----------------------------------------------------------------------------------------------------------------------

    public void morphLoop() {
        //do 2 for loops

        //used to store the morphed start and end images
        StartFrames = new BufferedImage[totalFrames];
        EndFrames = new BufferedImage[totalFrames];

        for (int imgNo = 0; imgNo < 2; imgNo++) {
            //0==start      1==end

            //set image to use
            if(imgNo == 0){
                morphinIMG = new MyImageObj(StartIMG.getImage(), Rows, Cols, false);
                resetMorphCP(StartIMG);
            }
            else{
                morphinIMG = new MyImageObj(EndIMG.getImage(), Rows, Cols, false);
                resetMorphCP(EndIMG);
            }
            //loop for the total number of frames
            for (int i = 0; i < totalFrames; i++) {
                morphInTime(i, imgNo);
                if(imgNo == 0){StartFrames[i] = MyImageObj.deepCopy(morphinIMG.getImage());}
                else{EndFrames[i] = MyImageObj.deepCopy(morphinIMG.getImage());}
            }
        }
    }

    //------------------------------------------------------------------------------------------------------------------
    public void combineIMGLoop(int currFrame, MyImageObj morphinImg){
        //start counting frames from 1
        BufferedImage StartFrame = StartFrames[currFrame - 1];
        BufferedImage EndFrame = EndFrames[totalFrames - currFrame];

        //the image that will contain the combined start and end img
        BufferedImage mergeBaby = new BufferedImage(StartFrame.getWidth(), StartFrame.getHeight(), BufferedImage.TYPE_INT_RGB);

        //Graphics to actually combine them
        Graphics g = mergeBaby.getGraphics();
        Graphics2D big = (Graphics2D) g.create();

        big.setComposite(AlphaComposite.SrcOver.derive(1 - ((float) currFrame / totalFrames)));
        big.drawImage(StartFrame, 0, 0, this);

        big.setComposite(AlphaComposite.SrcOver.derive(((float) currFrame / totalFrames)));
        big.drawImage(EndFrame, 0, 0, this);

        //set the merged image to be displayed
        morphinImg.setImage(mergeBaby, Rows, Cols, false);

        morphinImg.repaint();
    }

    public void timeFunc(MyImageObj morphinImg){
        final Timer timer = new Timer((1000)/fps, null);    //(1000)/fps says to refresh n times every second
        timer.addActionListener(new ActionListener() {
            int currFrame = 1;
            @Override
            public void actionPerformed(ActionEvent e) {
                //call the func to combine the imgs
                combineIMGLoop(currFrame, morphinImg);

                //increase the current frame counter
                currFrame++;

                //end the timer when the current frame counter == the total num of frames
                if(currFrame == totalFrames){timer.stop();}
            }
        });
        timer.setRepeats(true);
        timer.start();
    }

//-----------------------------------------------------------------------------------------------------------------------
    //reset the image morph cp locations == to the desired image cps
    public void resetMorphCP(MyImageObj currIMG){
        for(int i = 0; i < Cols+1; i++){
            for(int j = 0; j < Rows+1; j++){
                morphinIMG.updateIMGpos(i, j, currIMG.getIMGposX(i, j), currIMG.getIMGposY(i, j));
            }
        }
    }
//-----------------------------------------------------------------------------------------------------------------------
    //The following code is provided by Dr. Seals
    public static void warpTriangle(BufferedImage src, BufferedImage dest, Triangle SrcTriangle,
                                    Triangle DestTriangle, Object ALIASING, Object INTERPOLATION)
    {
        if (ALIASING == null)
          ALIASING = RenderingHints.VALUE_ANTIALIAS_ON;
        if (INTERPOLATION == null)
          INTERPOLATION = RenderingHints.VALUE_INTERPOLATION_BICUBIC;
        double[][] a = new double[3][3];
        for (int i = 0; i < 3; ++i) {
            a[i][0] = SrcTriangle.getX(i);
            a[i][1] = SrcTriangle.getY(i);
            a[i][2] = 1.0;
        }

        int l[] = new int[3];
        Gauss(3, a, l);

        double[] b = new double[3];
        for (int i = 0; i < 3; ++i) {
            b[i] = DestTriangle.getX(i);
        }

        double[] x = new double[3];
        solve(3, a, l, b, x);

        double[] by = new double[3];
        for (int i = 0; i < 3; ++i) {
            by[i] = DestTriangle.getY(i);
        }

        double[] y = new double[3];
        solve(3, a, l, by, y);

        AffineTransform af = new AffineTransform(x[0], y[0], x[1], y[1], x[2], y[2]);
        GeneralPath destPath = new GeneralPath(GeneralPath.WIND_EVEN_ODD);

        destPath.moveTo((float)DestTriangle.getX(0), (float)DestTriangle.getY(0));
        destPath.lineTo((float)DestTriangle.getX(1), (float)DestTriangle.getY(1));
        destPath.lineTo((float)DestTriangle.getX(2), (float)DestTriangle.getY(2));
        destPath.lineTo((float)DestTriangle.getX(0), (float)DestTriangle.getY(0));
        Graphics2D g2 = dest.createGraphics();

        // set up an alpha value for compositing as an example
        AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float)0.5);
        g2.setComposite(ac);

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, ALIASING);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, INTERPOLATION);
        g2.clip(destPath);
        g2.setTransform(af);
        g2.drawImage(src, 0, 0, null);
        g2.dispose();
    }

    private static void Gauss(int n, double[][] a, int[] l)
    {
        /****************************************************
         a is a n x n matrix and l is an int array of length n
         l is used as an index array that will determine the order of
         elimination of coefficients
         All array indexes are assumed to start at 0
         ******************************************************/
        double[] s = new double[n];  // scaling factor
        int i, j = 0, k;
        double r, rmax, smax, xmult;
        for (i = 0; i < n; ++i) {
            l[i] = i;
            smax = 0;
            for (j = 0; j < n; ++j)
                smax = Math.max(smax, Math.abs(a[i][j]));
            s[i] = smax;
        }

        i = n - 1;
        for (k = 0; k < (n - 1); ++k) {
            --j;
            rmax = 0;
            for (i = k; i < n; ++i) {
                r = Math.abs(a[l[i]][k] / s[l[i]]);
                if (r > rmax) {
                    rmax = r;
                    j = i;
                }
            }
            int temp = l[j];
            l[j] = l[k];
            l[k] = temp;
            for (i = k + 1; i < n; ++i) {
                xmult = a[l[i]][k] / a[l[k]][k];
                a[l[i]][k] = xmult;
                for (j = k + 1; j < n; ++j) {
                    a[l[i]][j] = a[l[i]][j] - xmult * a[l[k]][j];
                }
            }
        }
    }

    private static void solve(
            int n, double[][] a, int[] l, double[] b, double[] x)
    {
         /*********************************************************
         a and l have previously been passed to Gauss() b is the product of
         a and x. x is the 1x3 matrix of coefficients to solve for
         *************************************************************/
        int i, k;
        double sum;
        for (k = 0; k < (n - 1); ++k) {
            for (i = k + 1; i < n; ++i) {
                b[l[i]] -= a[l[i]][k] * b[l[k]];
            }
        }
        x[n - 1] = b[l[n - 1]] / a[l[n - 1]][n - 1];

        for (i = n - 2; i >= 0; --i) {
            sum = b[l[i]];
            for (int j = i + 1; j < n; ++j) {
                sum = sum - a[l[i]][j] * x[j];
            }
            x[i] = sum / a[l[i]][i];
        }
    }
}
