package com.tippingpoint;

import java.applet.Applet;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

/**
 * Example how to use unbuffered chunk-encoded POST request.
 */
public class scannerOptions extends Applet {

	/**
	 * This Applet serves 2 purposes: 1) it provides a way for a client workstation
	 * to GET ScannerOptions data from the server and 2) polls a directory for data
	 * from the handheld to POST to the server.
	 */
	private static final long serialVersionUID = -3581240315115581119L;

//	private String m_strIpAddress = "192.168.1.102";
	private static Thread m_threadPolling;
	private String m_strIpAddress = "192.168.1.69";
	private static boolean m_bKeepRunning = true;
	
	public static class PostData extends Thread {
		public PostData(String str) {
			super(str);
		}
		
		public void run() {
			int i = 1;
			while (m_bKeepRunning && i > 0) {
				try {
					Thread.sleep(3000);
				} 
				catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        	try {
					processDir();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("counter: " + Integer.valueOf(i++));
			}
			System.out.println("STOPPING thread");

		}
	}
	
	/**
	 * arg0 - ipAddress e.g. "192.168.1.108"
	 * @param args
	 */
	public static void main(String[] args){
		  scannerOptions app = new scannerOptions();


		  Frame frame = new Frame("CheckMate");
		  frame.setSize(400,200);
		  
		  frame.add(app);
		  frame.setVisible(true);
		  frame.addWindowListener(new WindowAdapter(){
			  public void windowClosing(WindowEvent e){
				  m_bKeepRunning = false;
				  
				  try {
					  Thread.sleep(1500);
				  } 
				  catch (InterruptedException e1) {
					  // TODO Auto-generated catch block
					  e1.printStackTrace();
				  }
				  
				  System.exit(0);
		      }
		   });
	
		  PostData pdThread = new PostData("asdf");
		  pdThread.start();
	}
		  
    public String getIpAddress() {
    	return m_strIpAddress;
    }

    public void paint(Graphics g) {
   		retrieveXml(getIpAddress());
    	g.drawString("GET scanner.xml   Sun 2:02 PM", 50, 25);
    	
    }    

    public void setIpAddress(String strIpAddress) {
    	m_strIpAddress = strIpAddress;
    }
    /**
     * This method copies the source file to the destination file and adds
     * a closing </objects> tag to the destination file in the process.
     * @return the name of the file containing the completed XML
     */
    private static void addClosingXmlTag(String strDestFile, String strSrcFile) 
    		throws  IOException {
    	FileInputStream from = null;
	    FileOutputStream to = null;
	    boolean bOkToDeleteSourceFile = false;
	    
	    try {
	    	from = new FileInputStream(strSrcFile);
	    	to = new FileOutputStream(strDestFile);
	    	byte[] buffer = new byte[4096];
	    	int bytesRead;
	
	    	while ((bytesRead = from.read(buffer)) != -1)
	    		to.write(buffer, 0, bytesRead); // write
	    	
	    	String strClosingTag = "</objects>";
	    	// TODO: is CharsetEncoder more robust for this conversion?
	    	buffer = strClosingTag.getBytes();
	    	to.write(buffer, 0, strClosingTag.length());
	    	
	    	// If we got this far with no exception, then the copy must have 
	    	// succeeded in which case the source file may be deleted.
	    	bOkToDeleteSourceFile = true;
	    } 
	    finally {
	    	if (from != null) {
	    		try {
	    			from.close();
	    		} 
		    	catch (IOException e) {
		    		  ;
		    	}
	    	}
	    	if (to != null) {
	    		try {
	    			to.close();
	    		} 
	    		catch (IOException e) {
	    			;
	    		}
	    	}
	    }
	    
	    if (bOkToDeleteSourceFile) {
            File f = new File(strSrcFile);
            f.delete();
	    }

     
    }

    private static void postIt(String strFilenameWPath) throws Exception {
        HttpClient httpclient = new DefaultHttpClient();
        try {
//            HttpPost httppost = new HttpPost("http://localhost:8080" + "/server/scannerlog");
            HttpPost httppost = new HttpPost("http://192.168.1.69:8080" + "/server/scannerlog");

            FileBody bin = new FileBody(new File(strFilenameWPath));
            StringBody comment = new StringBody("A binary file of some kind");

            MultipartEntity reqEntity = new MultipartEntity();
            reqEntity.addPart("bin", bin);
            reqEntity.addPart("comment", comment);

            httppost.setEntity(reqEntity);

            System.out.println("executing request " + httppost.getRequestLine());
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity resEntity = response.getEntity();

            System.out.println("----------------------------------------");
            System.out.println(response.getStatusLine());
            if (resEntity != null) {
                System.out.println("Response content length: " + resEntity.getContentLength());
            }
            EntityUtils.consume(resEntity);
        } finally {
            try { httpclient.getConnectionManager().shutdown(); } catch (Exception ignore) {}
        }
    }
 
    
    // *** Polling logic
    private static void processDir() throws Exception {
    	//String strPathToMonitor = "/Users/mgee/workspaces/wkconscan/client/xml/";
    	String strPathToMonitor = "c:\\Documents and Settings\\Owner\\My Documents\\CN3B36220927180 My Documents\\";
//    	String strPathToMonitor = args[0];
        File dir = new File(strPathToMonitor);

        String[] children = dir.list();
        if (children == null) {
            // Either dir does not exist or is not a directory
        } 
        else {
        	String strPathSeparator = "\\";  // Windows separator
            for (int i=0; i<children.length; i++) {
            	String strSrcFilename = children[i];
            	
            	// Determine if this is Windows or Apple
            	if (i == 0) {
            		File f = new File(strPathToMonitor + strPathSeparator + strSrcFilename);
            	
            		if (!f.exists())
            			strPathSeparator = "/";  // Apple OS X separator
            	}
            	
                // Get filename of file or directory
                String strSrcFilenameWPath = strPathToMonitor + strPathSeparator + strSrcFilename;
                if (strSrcFilenameWPath.indexOf("_log") > 0) {
                	String strDestFilename = strSrcFilename.substring(1);
                    String strDestFilenameWPath = strPathToMonitor + strPathSeparator + strDestFilename;
                	
                    System.out.println("processing: " + strSrcFilenameWPath);
                    addClosingXmlTag(strDestFilenameWPath, strSrcFilenameWPath);
	                postIt(strDestFilenameWPath);
	                
	                File f = new File(strDestFilenameWPath);
	                f.delete();
                }
            }
        }
    }

    private static void retrieveXml(String strIpAddress) {
        HttpClient httpclient = new DefaultHttpClient();
        FileOutputStream outstreamXml = null;
        try {
        	String strUrl = "http://" + strIpAddress + ":8080/server/scanner";
//        	String strUrl = "http://localhost:8080/conscan/scanner";
        	System.out.println("scannerOptions IPAddress: " + strUrl);
            HttpOptions httpOptions = new HttpOptions(strUrl);

            System.out.println("executing request " + httpOptions.getURI());

            // Create a response handler
            HttpResponse response = httpclient.execute(httpOptions);
            HttpEntity resEntity = response.getEntity();
            
            outstreamXml = new FileOutputStream("c:\\Documents and Settings\\Owner\\My Documents\\CN3B36220927180 My Documents\\scanner.xml");
//            outstreamXml = new FileOutputStream("C:\\Documents and Settings\\Jay\\My Documents\\CN3A00700700729 My Documents\\scanner.xml");
//            outstreamXml = new FileOutputStream("MGGscanner.xml");
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
