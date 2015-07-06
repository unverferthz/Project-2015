package bit.hillcg2.bluetoothapp;

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

    public FTPManager(Context startContext, DBManager startDBManager){
        context = startContext;
        am = context.getAssets();
        dbManager = startDBManager;
    }

    public void sendFile(){
        AsyncSendFile aSendFile = new AsyncSendFile();
        aSendFile.execute();
    }

    private File createFileToSend(){
        ArrayList<Incident> allIncidents = dbManager.getNewIncidents();

        File outputFile = null;

        try {
            DateFormat df = new SimpleDateFormat("hhmmss");
            String currTime = df.format((Calendar.getInstance().getTime()));

            df = new SimpleDateFormat("ddMMyyyy");
            String currDate = df.format((Calendar.getInstance().getTime()));

            String fileName = "incidentData_" + currTime + currDate + ".txt";
            FileOutputStream outputStream;
            outputStream = context.openFileOutput(fileName, context.MODE_PRIVATE);
            outputFile = new File(context.getFilesDir(), fileName);

            int count = 0;

            for (Incident i : allIncidents) {
                String incidentString = "";

                if(count != 0)
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
        catch(IOException e)
        {
            e.printStackTrace();
        }

        return outputFile;
    }

    public class AsyncSendFile extends AsyncTask<Void,Void,Void>
    {
        @Override
        protected Void doInBackground(Void... voids) {

            String user = "unverzp1";
            String password = "11000712";

            //mFTP = new FTPClient();
            jsch = new JSch();

            try
            {
                /*mFTP.connect("kate.ict.op.ac.nz", 46815);

                mFTP.login(user, password);

                mFTP.setFileType(FTP.ASCII_FILE_TYPE);
                mFTP.enterLocalPassiveMode();*/

                Session session = jsch.getSession(user, "kate.ict.op.ac.nz", 46815);
                session.setConfig("PreferredAuthentications", "password");
                session.setConfig("StrictHostKeyChecking", "no");
                session.setPassword(password);
                session.connect();
                Channel channel = session.openChannel("sftp");
                ChannelSftp sftp = (ChannelSftp)channel;
                sftp.connect();


                File outputFile = createFileToSend();

                if(outputFile != null)
                {
                    // Open a FileInputStream to read your little file
                    FileInputStream inputStream = new FileInputStream(outputFile);

                    sftp.cd("/home/unverzp1/public_html/Project");

                    DateFormat df = new SimpleDateFormat("hhmmss");
                    String currTime = df.format((Calendar.getInstance().getTime()));

                    df = new SimpleDateFormat("ddMMyyyy");
                    String currDate = df.format((Calendar.getInstance().getTime()));

                    String fileName = "incidentData_" + currTime + currDate + ".txt";

                    //mFTP.storeFile("incidentData.txt", inputStream);
                    sftp.put(inputStream, fileName, sftp.OVERWRITE);
                }
                sftp.disconnect();
                session.disconnect();
                //mFTP.disconnect();
            }
            catch(IOException e)
            {
                e.printStackTrace();
            } catch (JSchException e) {
                e.printStackTrace();
            } catch (SftpException e) {
                e.printStackTrace();
            }

            return null;
        }
    }
}
