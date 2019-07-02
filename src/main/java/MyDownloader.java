import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;

public class MyDownloader {
    private boolean needToRestart;

    MyDownloader(File myDir){
        this.needToRestart = true;
        //this.myDir = myDir;
        if(!myDir.exists()){
            //doesn't exist, try to create file
            boolean result = myDir.mkdir();
            checkToDownload(result);
        }
        else {
            System.out.println("Directory exists, so file must exist. Ready to run!");
            this.needToRestart = false;
        }

    }

    public boolean isNeedToRestart() {
        return needToRestart;
    }

    private void checkToDownload(boolean directoryMade){
        if(directoryMade){
            System.out.println("\nFile directory was not found, creating directory and installing file now...\n");
            FileOutputStream fileOS;
            //Download the .dll file
            try {
                //Attempting to download a file uploaded to DropBox
                //The file uploaded was from sqljdbc_6.0->enu->auth->x64->sqljdbc_auth.dll
                //This file is important for Windows Authentication methods
                ReadableByteChannel readChannel = Channels.newChannel(new URL("https://dl.dropboxusercontent.com/s/8y91d3c9014j0j5/sqljdbc_auth.dll?dl=0").openStream());
                //Store the downloaded file into the newly created sqlauth folder in C:
                fileOS = new FileOutputStream("C:\\\\sqlauth\\sqljdbc_auth.dll");
                FileChannel writeChannel = fileOS.getChannel();
                //Transfer bytes from downloaded file to the new FileOutputStream file
                writeChannel.transferFrom(readChannel, 0, Long.MAX_VALUE);
                System.out.println("Downloaded file to path! Check C://sqlauth");
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(0);
            }
        }
        else{
            System.out.println("Dir could not be created for some reason");
        }
    }


}
