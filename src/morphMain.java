/*
Authors: David Mercado and Jared Rigdon
Created: 11/11/18
Description: Main file Hold the Jframe and Builds the components
 */

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/***************************************************************************

 Loads an image (JPEG or GIF), displays it, selects from
 a small set of image processing routines, and shows the results

 ***************************************************************************/

public class morphMain extends JFrame {

    // Instance variables
    private BufferedImage image;                        // the image
    private MyImageObj startImg, endImg, morphImg;      // a component in which to display an image
    private JButton preview;                            //Button to preview
    private JLabel secondsLabel;                          //speed label
    private JSlider secondsSlider;                        //speed slider
    private int Seconds = 1;                              //initial speed duration
    private int currCPx, currCPy;                       // Store x, y mouse position for paint
    private boolean isDragging = false;                 //flag to check if user is dragging a valid point
    private int fps = 60;

    private JLabel startBrightLabel, endBrightLabel, fpsLabel, gridLabel;   // Label for threshold slider
    private JSlider startBrightSlider, endBrightSlider, fpsSlider, gridSlider;

    //grid variables
    private int Rows = 10;                              //rows
    private int Columns = 10;                           //columns

    //user componets
    private Point userClick;                            //user click
    private final String defaultIMG = "src/largeBoat.gif";    //default starting image

    //---------------------------------------------------------------------------------
    // Constructor for the frame
    public morphMain() {

        super();                                        // call JFrame constructor
        this.buildMenus();                              // helper method to build menus
        this.buildComponents();                         // helper method to set up components
        this.buildDisplay();                            // Lay out the components on the display
    }

    //---------------------------------------------------------------------------------
    //  Builds the menus to be attached to this JFrame object
    //  Primary side effect:  menus are added via the setJMenuBar call
    //  Action listeners for the menu items are anonymous inner classes here
    //  This helper method is called once by the constructor
    private void buildMenus() {

        final JFileChooser fc = new JFileChooser(".");
        JMenuBar bar = new JMenuBar();
        this.setJMenuBar(bar);
        JMenu fileMenu = new JMenu("File");
        JMenuItem fileopen1 = new JMenuItem("Open Starting Image");
        JMenuItem fileopen2 = new JMenuItem("Open End Image");
        JMenuItem fileexit = new JMenuItem("Exit");

        //open image for the start (left) panel, Action listener
        fileopen1.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        int returnVal = fc.showOpenDialog(morphMain.this);
                        if (returnVal == JFileChooser.APPROVE_OPTION) {
                            File file = fc.getSelectedFile();
                            try {
                                image = ImageIO.read(file);
                            } catch (IOException e1) {
                                System.out.println("Error: " + e1);
                            }

                            startImg.setImage(image, Rows, Columns, true);
                            startImg.showImage();
                        }
                    }
                }
        );

        //open image for the end (right) panel, Action listener
        fileopen2.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        int returnVal = fc.showOpenDialog(morphMain.this);
                        if (returnVal == JFileChooser.APPROVE_OPTION) {
                            File file = fc.getSelectedFile();
                            try {
                                image = ImageIO.read(file);
                            } catch (IOException e1) {
                                System.out.println("Error: " + e1);
                            }

                            endImg.setImage(image, Rows, Columns, true);
                            endImg.showImage();
                        }
                    }
                }
        );

        //exit Action listener
        fileexit.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        System.exit(0);
                    }
                }
        );

        fileMenu.add(fileopen1);
        fileMenu.add(fileopen2);
        fileMenu.add(fileexit);
        bar.add(fileMenu);
    }

    //---------------------------------------------------------------------------------
    //  Allocate components (these are all instance vars of this frame object)
    //  and set up action listeners for each of them
    //  This is called once by the constructor
    private void buildComponents() {

        // Create components to in which to display image and GUI control reads a default image
        startImg = new MyImageObj(readImage(defaultIMG), Rows, Columns, true);
        endImg = new MyImageObj(readImage(defaultIMG), Rows, Columns, true);

        //motion listener to simulate rubber banding Action listener for left image
        startImg.addMouseMotionListener(
                new MouseMotionAdapter() {
                    public void mouseDragged(MouseEvent event) {
                        //check if dragging is true
                        if(isDragging){
                            //get the currently selected CP(changes on press)
                            currCPx=startImg.getCurrCPx();
                            currCPy=startImg.getCurrCPy();

                            //update the CP location (for now, just draggable anywhere)
                            //call func to check event loc before updating
                            if(checkBoundry(event, startImg)){
                                //use this func to create a new poly each time
                                startImg.updateImgLoc(event.getX(), event.getY());
                                startImg.repaint();
                            }
                        }
                    }
                }
        );

        //motion listener to simulate rubber banding Action listener for right image
        endImg.addMouseMotionListener(
                new MouseMotionAdapter() {
                    public void mouseDragged(MouseEvent event) {
                        if(isDragging){
                            //get the currently selected CP(changes on press)
                            currCPx=endImg.getCurrCPx();
                            currCPy=endImg.getCurrCPy();

                            //update the CP location (for now, just draggable anywhere)
                            if(checkBoundry(event, endImg)){
                                //use this func to create a new poly each time
                                endImg.updateImgLoc(event.getX(), event.getY());
                                endImg.repaint();
                            }
                        }
                    }
                }
        );

        // Listen for mouse release to detect when we've stopped painting
        // this will draw the final position of the location
        startImg.addMouseListener(
                new MouseAdapter() {
                    public void mouseReleased(MouseEvent event) {
                        //set release means the user is no longer dragging
                        isDragging = false;
                        //update the currCP
                        startImg.repaint();
                    }
                }
        );

        // Listen for mouse release to detect when we've stopped painting
        endImg.addMouseListener(
                new MouseAdapter() {
                    public void mouseReleased(MouseEvent event) {
                        //set release means the user is no longer dragging
                        isDragging = false;
                        //update the currCP
                        endImg.repaint();
                    }
                }
        );

        //enables dragging and highlights corresponding CPs
        startImg.addMouseListener(
                new MouseAdapter() {
                    public void mousePressed(MouseEvent e) {
                        userClick = e.getPoint();
                        if(startImg.checkCPs(userClick)){
                            isDragging =true;

                            //change the previous CP color back unless its the same point
                            if(currCPx != startImg.getCurrCPx() || currCPy != startImg.getCurrCPy()){
                                startImg.changeCP_Color(false, currCPx, currCPy);
                                endImg.changeCP_Color(false, currCPx, currCPy);
                            }

                            //update the currCP since this will be updated at each valid press
                            currCPx=startImg.getCurrCPx();
                            currCPy=startImg.getCurrCPy();

                            //change opposite cp color
                            endImg.changeCP_Color(true, currCPx, currCPy);
                        }
                    }
                }
        );

        endImg.addMouseListener(
                new MouseAdapter() {
                    public void mousePressed(MouseEvent e) {
                        userClick = e.getPoint();
                        if(endImg.checkCPs(userClick)){
                            isDragging =true;

                            //change the previous CP color back unless its the same point
                            if(currCPx != endImg.getCurrCPx() || currCPy != endImg.getCurrCPy()){
                                startImg.changeCP_Color(false, currCPx, currCPy);
                                endImg.changeCP_Color(false, currCPx, currCPy);
                            }

                            //update the currCP since this will be updated at each valid press
                            currCPx=endImg.getCurrCPx();
                            currCPy=endImg.getCurrCPy();

                            //change opposite cp color
                            startImg.changeCP_Color(true, currCPx, currCPy);
                        }
                    }
                }
        );
    }

    //---------------------------------------------------------------------------------
    //displays the imgs and the button/slider panel below
    private void buildDisplay() {

        // Build bottom JPanel
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new GridLayout(2, 5));

        //button for preview and Action listener
        preview = new JButton("Preview");
        preview.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("You clicked on Preview");
                preview.setEnabled(false);

                morphImg = new MyImageObj(startImg.getImage(), Rows, Columns, false);
                morphPreview Morphing = new morphPreview(morphImg, startImg, endImg, Rows, Columns, Seconds, fps);


                //action listener
                Morphing.addWindowListener(
                        new WindowAdapter() {
                            public void windowClosing(WindowEvent e) {
                                preview.setEnabled(true);
                            }
                        }
                );
            }
        });

        //===================================================================================//
        //--------------------------------------Sliders--------------------------------------//
        //===================================================================================//

        //--------------------------------------Seconds--------------------------------------//
        //speed label, speed slider and Action listener
        secondsLabel = new JLabel("Seconds: " + Seconds);
        secondsSlider = new JSlider(SwingConstants.HORIZONTAL, 1, 10, 1);
        secondsSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                Seconds = secondsSlider.getValue();
                secondsLabel.setText("Seconds: " + Seconds);
            }
        });

        //--------------------------------------FPS--------------------------------------//
        //fps label, speed slider and Action listener
        fpsLabel = new JLabel("Frames per Second: " + fps);
        fpsSlider = new JSlider(SwingConstants.HORIZONTAL, 1, 60, 60);
        fpsSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                fps = fpsSlider.getValue();
                fpsLabel.setText("Frames per Second: " + fps);
            }
        });

        //--------------------------------------Start Brightness--------------------------------------//
        startBrightLabel = new JLabel("Brightness Value: 100%");
        startBrightSlider = new JSlider( SwingConstants.HORIZONTAL, 0, 200, 100);
        startBrightSlider.setMajorTickSpacing(10);
        startBrightSlider.setPaintTicks(true);

        startBrightSlider.addChangeListener(
                new ChangeListener() {
                    public void stateChanged (ChangeEvent e) {
                        startImg.changeBrightness(startBrightSlider.getValue());
                        startBrightLabel.setText("Brightness Value: " +
                                Integer.toString(startBrightSlider.getValue())+"%");
                    }
                }
        );

        //--------------------------------------End Brightness--------------------------------------//
        endBrightLabel = new JLabel("Brightness Value: 100%");
        endBrightSlider = new JSlider( SwingConstants.HORIZONTAL, 0, 200, 100);
        endBrightSlider.setMajorTickSpacing(10);
        endBrightSlider.setPaintTicks(true);

        endBrightSlider.addChangeListener(
                new ChangeListener() {
                    public void stateChanged (ChangeEvent e) {
                        endImg.changeBrightness(endBrightSlider.getValue());
                        endBrightLabel.setText("Brightness Value: " +
                                Integer.toString(endBrightSlider.getValue())+"%");
                    }
                }
        );

        //--------------------------------------Grid Size--------------------------------------//
        gridLabel = new JLabel("Grid Size: 10 X 10");
        gridSlider = new JSlider( SwingConstants.HORIZONTAL, 3, 20, 10);
        gridSlider.setMajorTickSpacing(1);
        gridSlider.setPaintTicks(true);

        gridSlider.addChangeListener(
                new ChangeListener() {
                    public void stateChanged (ChangeEvent e) {
                        Rows = gridSlider.getValue();
                        Columns = gridSlider.getValue();
                        startImg.setImage(startImg.getImage(), Rows, Columns, true);
                        endImg.setImage(endImg.getImage(), Rows, Columns, true);
                        gridLabel.setText("Grid Size: " +
                                Integer.toString(gridSlider.getValue())+" X "+ Integer.toString(gridSlider.getValue()));
                    }
                }
        );

        controlPanel.add(startBrightLabel);
        controlPanel.add(startBrightSlider);
        controlPanel.add(preview);
        controlPanel.add(endBrightLabel);
        controlPanel.add(endBrightSlider);
        controlPanel.add(secondsLabel);
        controlPanel.add(secondsSlider);
        controlPanel.add(fpsLabel);
        controlPanel.add(fpsSlider);
        controlPanel.add(gridLabel);
        controlPanel.add(gridSlider);


        // Add panels and image data component to the JFrame
        Container c = this.getContentPane();
        c.setLayout(new FlowLayout());
        c.add(startImg);
        c.add(endImg);
        c.add(controlPanel, BorderLayout.SOUTH);
        setSize(new Dimension(1250, 750));
        setResizable(false);
        setVisible(true);
    }

    //---------------------------------------------------------------------------------
    // This method reads an Image object from a file indicated by the string provided
    // as the parameter.  The image is converted here to a BufferedImage object, and
    // that new object is the returned value of this method. The mediatracker in this
    // method can throw an exception
    public BufferedImage readImage(String file) {

        Image image = Toolkit.getDefaultToolkit().getImage(file);

        MediaTracker tracker = new MediaTracker(new Component() {});
        tracker.addImage(image, 0);
        try {
            tracker.waitForID(0);
        } catch (InterruptedException e) {
            System.out.println("Error:" + e);
        }
        int imgWidth = image.getWidth(this);
        int imgHeight = image.getWidth(this);

        if(imgHeight > 594) {
            imgHeight = 594;
        }
        if(imgWidth > 594){
            imgWidth = 594;
        }
        BufferedImage bim = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D big = bim.createGraphics();
        big.drawImage(image, 0, 0, this);
        return bim;
    }

    public boolean checkBoundry(MouseEvent event, MyImageObj currImg) {
        //temp arrays
        int[] X = {-1,0,1,1,0,-1};
        int[] Y = {-1,-1,0,1,1,0};

        //get the points based on the array below as this will work for all CPs
        //loop through array for all pairs
        for (int i = 0; i < 6; i++){
            //wrap around
            if(i == 5){
                if(Line2D.linesIntersect(currImg.getIMGposX(currCPx+X[i], currCPy+Y[i]), (int)currImg.getIMGposY(currCPx+X[i], currCPy+Y[i]),
                        currImg.getIMGposX(currCPx+X[0], currCPy+Y[0]), currImg.getIMGposY(currCPx+X[0], currCPy+Y[0]),
                        event.getX(), event.getY(), currImg.getIMGposX(currCPx, currCPy), currImg.getIMGposY(currCPx, currCPy))){
                    return false;
                }

            }
            else {
                if(Line2D.linesIntersect(currImg.getIMGposX(currCPx+X[i], currCPy+Y[i]), (int)currImg.getIMGposY(currCPx+X[i], currCPy+Y[i]),
                        currImg.getIMGposX(currCPx+X[i+1], currCPy+Y[i+1]), currImg.getIMGposY(currCPx+X[0], currCPy+Y[0]),
                        event.getX(), event.getY(), currImg.getIMGposX(currCPx, currCPy), currImg.getIMGposY(currCPx, currCPy))){
                    return false;
                }
            }
        }
        return true;
    }
    //---------------------------------------------------------------------------------
    // The main method allocates the "window" and makes it visible. The windowclosing
    // event is handled by an anonymous inner (adapter) class. Command line arguments are ignored.
    public static void main(String[] argv) {

        JFrame frame = new morphMain();
        frame.addWindowListener(
                new WindowAdapter() {
                    public void windowClosing(WindowEvent e) {
                        System.exit(0);
                    }
                }
        );
    }
}
