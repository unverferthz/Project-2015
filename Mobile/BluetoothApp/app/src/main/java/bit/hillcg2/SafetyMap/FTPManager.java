package bit.hillcg2.SafetyMap;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
//import org.apache.commons.net.ftp.FTP;
//import org.apache.commons.net.ftp.FTPClient;
//import org.w3c.dom.Text;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class FTPManager {
    //FTPClient mFTP;
    private JSch jsch;
    private AssetManager am;
    private DBManager dbManager;
    private Context context;
    private ViewIncidents incidentActivity;

    //Constructor
    public FTPManager(Context startContext, DBManager startDBManager, ViewIncidents startIncidentActivity){
        context = startContext;
        am = context.getAssets();
        dbManager = startDBManager;
        incidentActivity = startIncidentActivity;
    }

    //Starts the asynchronous method that uploads file to server
    public void sendFile(){
        AsyncSendFile aSendFile = new AsyncSendFile();
        aSendFile.execute();
    }

    //Creates a text file from the sqlite database incidents and returns it as a text file
    private File createFileToSend(){
        ArrayList<Incident> allIncidents = dbManager.getNewIncidents();

        File outputFile = null;

        //Make a JSON formatted text file if there any any incidents
        if(allIncidents.size() > 0)
        {
            try
            {
                //TODO may need to change this in event of multiple users
                //Get time and date to add into file name for unique filename
                DateFormat df = new SimpleDateFormat("hhmmss");
                String currTime = df.format((Calendar.getInstance().getTime()));

                df = new SimpleDateFormat("ddMMyyyy");
                String currDate = df.format((Calendar.getInstance().getTime()));

                String fileName = "incidentData_" + currTime + currDate + ".txt";

                FileOutputStream outputStream;
                outputStream = context.openFileOutput(fileName, context.MODE_PRIVATE);
                outputFile = new File(context.getFilesDir(), fileName);

                int count = 0;

                //Loop over all incidents and add into the text file
                for (Incident i : allIncidents)
                {
                    String incidentString = "";

                    if (count != 0)
                        incidentString += ",{";
                    else
                        incidentString += "{";

                    String distance = String.valueOf(i.getDistance());
                    incidentString += "distance:" + distance + ",";

                    String time = i.getTime();
                    incidentString += "time:" + time + ",";

                    String date = i.getDate();
                    incidentString += "date:" + date + ",";

                    String lat = i.getLat();
                    incidentString += "latitude:" + lat + ",";

                    String lng = i.getLng();
                    incidentString += "longitude:" + lng + "}";

                    outputStream.write(incidentString.getBytes());
                    count++;
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        //Returns null file if no new data
        return outputFile;
    }

    //Class for handling asynchronous sending of file
    public class AsyncSendFile extends AsyncTask<Void,Void,Boolean>
    {
        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);

            //Inform the activity that the upload has finished, return the result
            incidentActivity.finishedUpload(aBoolean);
        }

        //Asynchronous call
        @Override
        protected Boolean doInBackground(Void... voids) {
            //Get the text file that needs to be sent
            File outputFile = createFileToSend();

            boolean success = false;

            //Check that there was actually a file
            if(outputFile != null)
            {
                String user = "unverzp1";
                String password = "11000712";

                //mFTP = new FTPClient();
                jsch = new JSch();

                try
                {
                    //Normal FTP didn't work need to use SFTP to upload to kate
                    /*mFTP.connect("kate.ict.op.ac.nz", 46815);

                    mFTP.login(user, password);

                    mFTP.setFileType(FTP.ASCII_FILE_TYPE);
                    mFTP.enterLocalPassiveMode();*/

                    //Setup to SFTP file to server
                    Session session = jsch.getSession(user, "kate.ict.op.ac.nz", 46815);
                    session.setConfig("PreferredAuthentications", "password");
                    session.setConfig("StrictHostKeyChecking", "no");
                    session.setPassword(password);
                    session.connect();
                    Channel channel = session.openChannel("sftp");
                    ChannelSftp sftp = (ChannelSftp) channel;
                    sftp.connect();

                    //Open a FileInputStream to read file
                    FileInputStream inputStream = new FileInputStream(outputFile);

                    //Directory to put file into
                    sftp.cd("/home/unverzp1/public_html/Project");

                    //TODO may need to change if multiple users
                    DateFormat df = new SimpleDateFormat("hhmmss");
                    String currTime = df.format((Calendar.getInstance().getTime()));

                    df = new SimpleDateFormat("ddMMyyyy");
                    String currDate = df.format((Calendar.getInstance().getTime()));

                    //File name, made unique by time and date
                    String fileName = "incidentData_" + currTime + currDate + ".txt";

                    //Put the file into the directory
                    //mFTP.storeFile("incidentData.txt", inputStream);
                    sftp.put(inputStream, fileName, sftp.OVERWRITE);

                    //Close connections
                    sftp.disconnect();
                    session.disconnect();
                    success = true;

                    //mFTP.disconnect();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
                catch (JSchException e)
                {
                    e.printStackTrace();
                }
                catch (SftpException e)
                {
                    e.printStackTrace();
                }
            }
            //Return whether it was successful or not
            return success;
        }
    }
}
