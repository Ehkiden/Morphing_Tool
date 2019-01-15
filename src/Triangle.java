/*
Author: Dr. Seals
Modified By: David Mercado and Jared Rigdon
 */

import java.awt.geom.Point2D;

public class Triangle
{
    private Point2D.Double tri[];

    //constructor
    public Triangle(double x1, double y1, double x2, double y2, double x3, double y3)
    {
        tri = new Point2D.Double[3];
        tri[0] = new Point2D.Double(x1, y1);
        tri[1] = new Point2D.Double(x2, y2);
        tri[2] = new Point2D.Double(x3, y3);
    }

    //getters
    public double getX(int index)
    {
        if ((index >= 0) && (index < 6))
            return (tri[index].getX());
        System.out.println("Index out of bounds in getX()");
        return (0.0);
    }

    public double getY(int index)
    {
        if ((index >= 0) && (index < 6))
            return (tri[index].getY());
        System.out.println("Index out of bounds in getY()");
        return (0.0);
    }
}
