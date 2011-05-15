
import java.io.File;

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
            for (int i=0; i<children.length; i++) {
                // Get filename of file or directory
                String filename = strPathToMonitor + "\\" + children[i];
                if (filename.indexOf("log") > 0) {
                    System.out.println("processing: " + filename);
	                postIt(filename);
	                
	                File f = new File(filename);
	                f.delete();
                }
            }
        }
    }

    private static void postIt(String strFilename) throws Exception {
        HttpClient httpclient = new DefaultHttpClient();
        try {
            HttpPost httppost = new HttpPost("http://localhost:8080" +
                    "/server/scannerlog");

            FileBody bin = new FileBody(new File(strFilename));
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
