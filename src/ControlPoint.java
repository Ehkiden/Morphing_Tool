/*
Authors: David Mercado and Jared Rigdon
Created: 11/11/18
Description: File the holds the control point handles
 */

import java.awt.*;

public class ControlPoint {

    private static final int IMG_WIDTH = 600;
    private int COLS, ROWS;

    //control point self information
    private double currIMGPosX, currIMGPosY;
    private int arrayX, arrayY;
    private boolean isEdge;
    private int handleCoordinatesX[];
    private int handleCoordinatesY[];

    //GUI for control point
    private Polygon CP;
    private Color color;

    //---------------------------------------------------------------------------------
    //Constructor for a Control Point
    public ControlPoint(int X, int Y, boolean isEdge, int rows, int cols){
        this.isEdge = isEdge;
        arrayX = X;
        arrayY = Y;
        ROWS = rows;
        COLS = cols;

        CP = null;

        //set the default
        currIMGPosX =(float) (arrayX * ((IMG_WIDTH - 1) / (COLS + 1)));
        currIMGPosY =(float) (arrayY * ((IMG_WIDTH - 1) / (ROWS + 1)));

        //if CP isn't on the edge frame itself
        if(!isEdge){
            frameCP();
        }
    }

    //---------------------------------------------------------------------------------
    //sets the coordinates of the cp handle
    private void frameCP(){
        handleCoordinatesX = new int[] {(int)currIMGPosX - 4, (int)currIMGPosX + 4, (int)currIMGPosX + 4, (int)currIMGPosX - 4};
        handleCoordinatesY = new int[] {(int)currIMGPosY - 4, (int)currIMGPosY - 4, (int)currIMGPosY + 4, (int)currIMGPosY + 4};
        CP = new Polygon(handleCoordinatesX, handleCoordinatesY, 4);
    }

    //---------------------------------------------------------------------------------
    //sets the color for the control point
    public void setColorRed(){color = Color.RED;}
    public void setColorBlack(){color = Color.BLACK;}

    //returns the x and y position of the control point on the img
    public double getIMGPosX(){return currIMGPosX;}
    public double getIMGPosY(){return currIMGPosY;}

    //returns the x and y position of the control point on the img
    public void setIMGPosX(double x){currIMGPosX = x; frameCP();}
    public void setIMGPosY(double y){currIMGPosY = y; frameCP();}

    //---------------------------------------------------------------------------------
    //draws the control point
    public void drawCP(Graphics g) {

        g.setColor(color);

        if (!isEdge) {
            // Draw the Control Point Itself
            g.drawPolygon(CP);
            g.fillPolygon(CP);
        }
    }

    //---------------------------------------------------------------------------------
    //Function that returns true if clicked inside of control point
    public boolean clickValid(Point click){ return CP.contains(click);}

    //---------------------------------------------------------------------------------
    //updates the current image location
    public void updateImgLoc(int ImgX, int ImgY){
        currIMGPosX = ImgX;
        currIMGPosY = ImgY;
        //call the func again to get a new polygon location
        frameCP();
    }
}
