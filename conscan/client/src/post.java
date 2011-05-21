
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

/**
 * Example how to use unbuffered chunk-encoded POST request.
 */
public class post {

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
    
    private static void processDir(String[] args) throws Exception {
    	//String strPathToMonitor = "/Users/mgee/workspaces/wkconscan/client/xml/";
    	//String strPathToMonitor = "c:\\Documents and Settings\\Owner\\My Documents\\CN3B36220927180 My Documents\\";
    	String strPathToMonitor = args[0];
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
            HttpPost httppost = new HttpPost("http://localhost:8080" +
                    "/server/scannerlog");

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
    
}
