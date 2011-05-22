package com.tippingpoint;

import java.applet.Applet;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Example how to use unbuffered chunk-encoded POST request.
 */
public class scannerOptions extends Applet {

/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

/*
    public static void main(String[] args) throws Exception {
        if (args.length != 1)  {
            System.out.println("File path not given");
            System.exit(1);
        }

        int i = 1;
        while (i > 0) {
        	processDir(args);
			Thread.sleep(2000);
			System.out.println("counter: " + Integer.valueOf(i));
			++i;
        }
    }
  */
	
	  public static void main(String[] args){
		    Frame frame = new Frame("Roseindia.net");
		    frame.setSize(400,200);
		    Applet app = new scannerOptions();
		    frame.add(app);
		    frame.setVisible(true);
		    frame.addWindowListener(new WindowAdapter(){
		      public void windowClosing(WindowEvent e){
		        System.exit(0);
		      }
		    });
		  }	
	
    public void paint(Graphics g) {
    	g.drawString("Hello world!", 50, 25);
    }    
    
    
}
