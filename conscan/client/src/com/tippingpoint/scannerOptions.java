package com.tippingpoint;

import java.applet.Applet;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * Example how to use unbuffered chunk-encoded POST request.
 */
public class scannerOptions extends Applet {

/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String m_strIpAddress = "192.168.1.69";
	
	/**
	 * arg0 - ipAddress e.g. "192.168.1.108"
	 * @param args
	 */
	public static void main(String[] args){
		  
		  Frame frame = new Frame("CheckMate");
		  frame.setSize(400,200);
		  scannerOptions app = new scannerOptions();
		  
		  if (args[0] != null)
			  app.setIpAddress(args[0]);
		  
		  frame.add(app);
		  frame.setVisible(true);
		  frame.addWindowListener(new WindowAdapter(){
			  public void windowClosing(WindowEvent e){
				  System.exit(0);
		      }
		   });
	}	
	
    public String getIpAddress() {
    	return m_strIpAddress;
    }

    public void paint(Graphics g) {
    	retrieveXml(getIpAddress());
    	g.drawString("generating scanner.xml", 50, 25);
    	
    }    

    public void setIpAddress(String strIpAddress) {
    	m_strIpAddress = strIpAddress;
    }

    private static void retrieveXml(String strIpAddress) {
        HttpClient httpclient = new DefaultHttpClient();
        FileOutputStream outstreamXml = null;
        try {
            HttpOptions httpOptions = new HttpOptions("http://" + strIpAddress + ":8080/server/scanner");

            System.out.println("executing request " + httpOptions.getURI());

            // Create a response handler
            HttpResponse response = httpclient.execute(httpOptions);
            HttpEntity resEntity = response.getEntity();
            
            outstreamXml = new FileOutputStream("c:/Documents and Settings/Owner/My Documents/CN3B36220927180 My Documents/scanner.xml");
            resEntity.writeTo(outstreamXml);
            
        } catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
        finally {
        	if (outstreamXml != null) {
        		try {
        			outstreamXml.close();
        		}
        		catch (IOException e) {
        			; // eat
        		}
        	}
            try { httpclient.getConnectionManager().shutdown(); } catch (Exception ignore) {}
        }
    }
    

}
