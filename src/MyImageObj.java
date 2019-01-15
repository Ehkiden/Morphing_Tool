/*
Authors: David Mercado and Jared Rigdon
Created: 11/11/18
Description: File that does everything with the image
 */

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.RescaleOp;
import java.awt.image.WritableRaster;


/*****************************************************************

 This is a helper object, which could reside in its own file, that
 extends JLabel so that it can hold a BufferedImage

 I've added the ability to apply image processing operators to the
 image and display the result
 ***************************************************************************/

public class MyImageObj extends JLabel {

    // instance variable to hold the buffered image
    private BufferedImage bufferedIMG = null;
    private BufferedImage originalIMG = null;

    //bool to tell if we should draw the control points
    private boolean drawCP = true;

    //holds control points
    //private ControlPoint ControlPoints[][] = new ControlPoint[ROWS+2][COLUMNS+2];
    private ControlPoint ControlPoints[][];

    //grid display componets
    private int gridRows, gridColumns;

    //holds the value of the currently selected CP
    private int currCPx, currCPy;

    private BufferedImage filteredbim;
    private boolean showfiltered=false;



    //---------------------------------------------------------------------------------
    // This constructor stores a buffered image passed in as a parameter
    public MyImageObj(BufferedImage img, int rows, int columns, boolean showCPs) {
        drawCP = showCPs;

        bufferedIMG = deepCopy(img);
        originalIMG = deepCopy(img);
        filteredbim = new BufferedImage
                (bufferedIMG.getWidth(), bufferedIMG.getHeight(), BufferedImage.TYPE_INT_RGB);

        setPreferredSize(new Dimension(bufferedIMG.getWidth(), bufferedIMG.getHeight()));

        gridRows = rows;
        gridColumns = columns;
        ControlPoints = new ControlPoint[gridRows+2][gridColumns+2];

        initGrid();
        this.repaint();
    }

    //---------------------------------------------------------------------------------
    // This mutator changes the image by resetting what is stored
    // The input parameter img is the new image;  it gets stored as an
    //     instance variable
    public void setImage(BufferedImage img, int rows, int columns, boolean showCps) {
        drawCP = showCps;

        if (img == null) return;
        bufferedIMG = deepCopy(img);
        originalIMG = deepCopy(img);
        filteredbim = new BufferedImage
                (bufferedIMG.getWidth(), bufferedIMG.getHeight(), BufferedImage.TYPE_INT_RGB);

        setPreferredSize(new Dimension(bufferedIMG.getWidth(), bufferedIMG.getHeight()));

        gridRows = rows;
        gridColumns = columns;
        ControlPoints = new ControlPoint[gridRows+2][gridColumns+2];

        initGrid();

        this.repaint();
    }

    //---------------------------------------------------------------------------------
    // accessor to get a handle to the buffered image object stored here
    public BufferedImage getImage() {
        if (showfiltered){
            filteredbim = deepCopy(filteredbim);
            showfiltered = false;
            return filteredbim;
        }
        else{
            bufferedIMG = deepCopy(bufferedIMG);

            return bufferedIMG;
        }
    }

    //---------------------------------------------------------------------------------
    //  show current image by a scheduled call to paint()
    public void showImage() {
        if (bufferedIMG == null) return;
        this.repaint();
    }

    //---------------------------------------------------------------------------------
    //  get a graphics context and show either filtered image or
    //  regular image
    public void paintComponent(Graphics g) {
        Graphics2D big = (Graphics2D) g;

        //use the filtered image for when the brightness is changed
        if (showfiltered){
            big.drawImage(filteredbim, 0, 0, this);
        }
        else{
            big.drawImage(bufferedIMG, 0, 0, this);
        }

        //determine if we need to draw the CPs
        if(drawCP){
            //draws the actual control points
            for (int x = 1; x < (gridColumns + 1); x++){
                for (int y = 1; y < (gridRows + 1); y++){
                    ControlPoints[x][y].drawCP(big);
                }
            }

            //calls functions to draw all lines
            drawVerticalLines(big);
            drawHorizontalLines(big);
        }
    }

    //---------------------------------------------------------------------------------
    //Function that sets up grid
    public void initGrid(){
        for (int x = 0; x < (gridColumns + 2); x++) {
            for (int y = 0; y < (gridRows + 2); y++) {

                boolean isedge = false;
                if (x == 0 || x == (gridColumns + 1) || y == 0 || y == (gridRows + 1)) {
                    isedge = true;
                }

                // Create Control Points in 2D array
                ControlPoint c = new ControlPoint(x, y, isedge, gridRows, gridColumns);
                c.setColorBlack();
                ControlPoints[x][y] = c;
                System.out.println(c.getIMGPosX() + " " + c.getIMGPosY());
            }
        }
    }

    //---------------------------------------------------------------------------------
    //function that checks each control point
    //clarifications so I don't get confused again: we have an array of 12*12 control
    // points but we only care about the 10*10 points that are not on the edge
    public boolean checkCPs(Point click){
        for(int i = 1; i < (gridColumns + 1); i++){
            for(int j = 1; j < (gridRows + 1); j++){
                if(ControlPoints[i][j].clickValid(click)){
                    currCPx = i;  //columns
                    currCPy = j;  //rows

                    //set valid cp to red
                    ControlPoints[i][j].setColorRed();
                    return true;
                }
            }
        }
        return false;
    }

    //---------------------------------------------------------------------------------
    //returns the current position
    public int getCurrCPx(){return currCPx;}
    public int getCurrCPy(){return currCPy;}

    //---------------------------------------------------------------------------------
    //Function that draws the Vertical lines between the control points
    public void drawVerticalLines(Graphics g){

        g.setColor(Color.BLACK);
        int startX = 0;
        int startY = 0;
        int startDX, startDY;
        int endX, endY, endDX, endDY;

        for(int i = 0; i < (gridColumns + 1); i++){
            for(int j = 0; j < (gridRows + 1); j++){
                if((j+1) <= (gridRows + 1)){
                    //position of end coordinates for vertical line
                    endX = (int)ControlPoints[i][j+1].getIMGPosX();
                    endY = (int)ControlPoints[i][j+1].getIMGPosY();

                    //position of start and end coordinates for diagonal line
                    startDX = (int)ControlPoints[i][j].getIMGPosX();
                    startDY = (int)ControlPoints[i][j].getIMGPosY();
                    endDX = (int)ControlPoints[i+1][j+1].getIMGPosX();
                    endDY = (int)ControlPoints[i+1][j+1].getIMGPosY();

                    //draw the vertical and diagonal lines
                    g.drawLine(startX, startY, endX, endY);

                    if(i==0 && j==(gridRows)){
                        int tempSx = (int)ControlPoints[1][gridRows].getIMGPosX();
                        int tempSy = (int)ControlPoints[1][gridRows].getIMGPosY();
                        int tempEx = (int)ControlPoints[0][gridRows+1].getIMGPosX();
                        int tempEy = (int)ControlPoints[0][gridRows+1].getIMGPosY();
                        g.drawLine(tempSx, tempSy, tempEx, tempEy);
                    }
                    else if(i==(gridColumns) && j==0){
                        int tempSx = (int)ControlPoints[gridColumns+1][0].getIMGPosX();
                        int tempSy = (int)ControlPoints[gridColumns+1][0].getIMGPosY();
                        int tempEx = (int)ControlPoints[gridColumns][1].getIMGPosX();
                        int tempEy = (int)ControlPoints[gridColumns][1].getIMGPosY();
                        g.drawLine(tempSx, tempSy, tempEx, tempEy);
                    }
                    else{
                        g.drawLine(startDX, startDY, endDX, endDY);

                    }

                    //store end position of vertical line as the next starting position
                    startX = endX;
                    startY = endY;
                }
            }
            startX = (int)ControlPoints[i+1][0].getIMGPosX();
            startY = (int)ControlPoints[i+1][0].getIMGPosY();
        }
    }

    //---------------------------------------------------------------------------------
    //Function that draws the Horizontal lines between the control points
    public void drawHorizontalLines(Graphics g){

        g.setColor(Color.BLACK);
        int startX = 0;
        int startY = 0;
        int endX;
        int endY;

        for(int j = 0; j < (gridRows + 1); j++){
            for(int i = 0; i < (gridColumns + 1); i++){

                if((i+1) <= (gridColumns + 1)){
                    //position of end coordinates for horizontal lines
                    endX = (int)ControlPoints[i+1][j].getIMGPosX();
                    endY = (int)ControlPoints[i+1][j].getIMGPosY();

                    //draw horizontal line
                    g.drawLine(startX, startY, endX, endY);

                    //store end position of horizontal line as the next starting position
                    startX = endX;
                    startY = endY;
                }
            }
            startX = (int)ControlPoints[0][j+1].getIMGPosX();
            startY = (int)ControlPoints[0][j+1].getIMGPosY();
        }
    }

    //---------------------------------------------------------------------------------
    //updates the currCP with the passed in img location
    public void updateImgLoc(int x, int y){
        ControlPoints[currCPx][currCPy].updateImgLoc(x,y);
        repaint();
    }

    //---------------------------------------------------------------------------------
    //updates the morph IMG position
    public void updateIMGpos(int x, int y, double newX, double newY){
        ControlPoints[x][y].setIMGPosX(newX);
        ControlPoints[x][y].setIMGPosY(newY);
        repaint();
    }


    //---------------------------------------------------------------------------------
    //Mainly used to change the opposite img CP
    public void changeCP_Color(boolean choose, int ImgX, int ImgY){
        if(choose){ControlPoints[ImgX][ImgY].setColorRed();}
        else {ControlPoints[ImgX][ImgY].setColorBlack();}
        repaint();
    }

    //---------------------------------------------------------------------------------
    //Function that returns the corredinates of the array position
    public double getIMGposX(int x, int y){ return ControlPoints[x][y].getIMGPosX();}
    public double getIMGposY(int x, int y){ return ControlPoints[x][y].getIMGPosY();}

    //---------------------------------------------------------------------------------
    //function to change the brightness
    public void changeBrightness(int value){
        if (bufferedIMG == null) { return; }    //error checking

        //change the value into a percentage since the slider goes from 0 to 200 (0% -- 200%
        float percentage = (float) (value/100.0);
        float scale = 0.0f + percentage;    //just in case

        //set offset to 15f to keep it normal
        RescaleOp op = new RescaleOp(scale, 15f, null);

        //use the original source image and then apply it to the filtered image with the new scale
        op.filter(bufferedIMG, filteredbim);

        //set value to true to be able to repaint the new image
        showfiltered=true;
        this.repaint();
    }

    static BufferedImage deepCopy(BufferedImage bi) {
        ColorModel cm = bi.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = bi.copyData(null);
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null)
                .getSubimage(0, 0, bi.getWidth(), bi.getHeight());

    }
}