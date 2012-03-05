package com.tippingpoint;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Frame;
import java.awt.TextField;

import java.awt.Label;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
public class scannerOptions extends Frame {

	/**
	 * This Application serves 2 purposes: 1) it provides a way for a client workstation
	 * to GET ScannerOptions data from the server and 2) polls a directory for data
	 * from the handheld for POSTing to the the server.
	 */
	private static final long serialVersionUID = -3581240315115581119L;

	private String m_strIpAddress = "localhost";
//	private String m_strIpAddress = "192.168.1.110";

	private String m_strAppName = "conscan";

	private String m_strClientScannerOPTIONSXmlPath = "C:\\Documents and Settings\\Jay\\My Documents\\CN3A00700700729 My Documents\\";
//	private String m_strClientScannerOPTIONSXmlPath = "z:\\Documents\\CN3B36220927180 My Documents\\";
//  private String m_strClientScannerOPTIONSXmlPath = "/Users/mgee";  // MAC

	private static boolean m_bKeepRunning = true;

    // LABEL - indicates state
    private static Panel m_panelMain = new Panel();
    private static Label m_labelCurrState = new Label("IDLE");
    private static TextField m_textDir = new TextField();

    public scannerOptions(String strIpAddress) {
    	if (strIpAddress!= null)
    		m_strIpAddress = strIpAddress;
    }
    
    private static String getDirTextfieldValue() {
    	return m_textDir.getText();
    }
    
	private static void updateCurrStateLabel(String str) {
		m_labelCurrState.setText(str);
		m_panelMain.setVisible(true);
	}

	/**
	 *
	 * @param args
	 */
	public static void main(String[] args){
		// Check for an over-ridden IP address
		String strIpAddress = null;
		if (args.length > 0) {
			strIpAddress = args[args.length - 1];
			System.out.println("JNLP <argument>: " + strIpAddress);
		}
		
		// UI - display the simple user interface
		scannerOptions objScannerOptions = new scannerOptions(strIpAddress);
 		Frame frame = objScannerOptions.setupFrame();
 		frame.setVisible(true);
 		
		// background thread - fire it up
		PostData pdThread = new PostData("PollAndPost", strIpAddress);
		pdThread.start();
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

    private String getAppName() {
    	return m_strAppName;
    }

    private String getIpAddress() {
    	return m_strIpAddress;
    }

    private String getClientScannerOPTIONSXmlPath() {
    	return m_strClientScannerOPTIONSXmlPath;
    }

    private void postIt(String strFilenameWPath) throws Exception {
        HttpClient httpclient = new DefaultHttpClient();
        try {
            HttpPost httppost = new HttpPost("http://" + getIpAddress() + ":8080" + "/" + getAppName() + "/scannerlog");

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
    private void processDir() throws Exception {
    	//String strPathToMonitor = "/Users/mgee/workspaces/wkconscan/client/xml/";
    	String strPathToMonitor = m_textDir.getText();
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

    private void retrieveXml() {
        HttpClient httpclient = new DefaultHttpClient();
        FileOutputStream outstreamXml = null;
        try {
        	String strUrl = "http://" + getIpAddress() + ":8080/" + getAppName() + "/scanner";
        	System.out.println("scannerOptions IPAddress: " + strUrl);
            HttpOptions httpOptions = new HttpOptions(strUrl);

            System.out.println("executing request " + httpOptions.getURI());

            // Create a response handler
            HttpResponse response = httpclient.execute(httpOptions);
            HttpEntity resEntity = response.getEntity();

        	String strPathToMonitor = m_textDir.getText();
           	String strPathSeparator = "\\";  // Windows separator
            
            outstreamXml = new FileOutputStream(strPathToMonitor + strPathSeparator + "scanner.xml");
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

    private Frame setupFrame() {
    	// FRAME
    	Frame frame = new Frame("ConScan(" + m_strIpAddress+ ") - Handheld");
       	frame.setLayout(new BorderLayout());
		frame.setSize(640,150);

        // BUTTON
	    Button buttonOptionScanner = new Button("GET Scanner Data from Server");
	    buttonOptionScanner.setSize(200, 50);
	    ActionListener buttonListenerOptionScanner = new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
    			scannerOptions objScannerOptions = new scannerOptions(getIpAddress());
    			updateCurrStateLabel("Handheld <- Server");
    			objScannerOptions.retrieveXml();
        		updateCurrStateLabel("IDLE");
            }
        };
        buttonOptionScanner.addActionListener(buttonListenerOptionScanner);

        // Directory for user's handhelddata
        m_textDir.setText(this.m_strClientScannerOPTIONSXmlPath);
        
	    // PANEL
        m_panelMain.setLayout(new BorderLayout());
        m_panelMain.add(m_labelCurrState, BorderLayout.NORTH);
	    m_panelMain.add(buttonOptionScanner, BorderLayout.SOUTH);
	    m_panelMain.add(m_textDir, BorderLayout.CENTER);
	    

        frame.add(m_panelMain, BorderLayout.NORTH);

        // LISTENER for quitting
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

		return frame;
    }

	private static class PostData extends Thread {
		private String m_strServerIpAddress;
		
		public PostData(String str, String strServerIpAddress) {
			super(str);
			m_strServerIpAddress = strServerIpAddress;
		}

		public void run() {
			scannerOptions objScannerOptions = new scannerOptions(m_strServerIpAddress);
			
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
	        		updateCurrStateLabel("Handheld -> Server (" + getDirTextfieldValue() + ")");
	        		objScannerOptions.processDir();
				}
				catch (Exception e) {
					// TODO Auto-generated catch block
					updateCurrStateLabel("KABOOM!!!" + e.toString());
					e.printStackTrace();
					try {
						Thread.sleep(9000);
					}
					catch (InterruptedException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					}

				}
				finally {
					try {
						Thread.sleep(2000);
					}
					catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	        		updateCurrStateLabel("IDLE");
				}
			}
			System.out.println("STOPPING thread");

		}
	}
}
