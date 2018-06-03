package com.stangassinger.mysync;


import android.os.Environment;
import android.util.Log;

import com.jcraft.jsch.*;
import java.io.*;
import java.net.InetAddress;
import java.util.List;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


public class Scp_to{
    private static FileInputStream fis = null;
    private final static String TAG = "Scp_to";




    static int checkAck(InputStream in) throws IOException{
        int b=in.read();
        // b may be 0 for success,
        //          1 for error,
        //          2 for fatal error,
        //          -1
        if(b==0) return b;
        if(b==-1) return b;

        if(b==1 || b==2){
            StringBuffer sb=new StringBuffer();
            int c;
            do {
                c=in.read();
                sb.append((char)c);
            }
            while(c!='\n');
            if(b==1){ // error
                System.out.print(sb.toString());
            }
            if(b==2){ // fatal error
                System.out.print(sb.toString());
            }
        }
        return b;
    }




    public static void executeRemoteSCP(String username,String hostname,int port,
                                           String lfile, String rfile)
            throws Exception {
        try {

            JSch jsch=new JSch();
            Session session=jsch.getSession(username, hostname, 22);
            jsch.addIdentity("my key", Conf.PRIVATEKEY.getBytes(), Conf.PUBLIC_KEY.getBytes(), Conf.PASSPHRASE.getBytes() );


            // Avoid asking for key confirmation
            Properties prop = new Properties();
            prop.put("StrictHostKeyChecking", "no");
            session.setConfig(prop);

            session.connect();

            boolean ptimestamp = false;

            // exec 'scp -t rfile' remotely
            String command="scp " + (ptimestamp ? "-p" :"") +" -t "+rfile;

            Channel channel=session.openChannel("exec");
            ((ChannelExec)channel).setCommand(command);

            // get I/O streams for remote scp
            OutputStream out=channel.getOutputStream();
            InputStream in=channel.getInputStream();

            channel.connect();

            if(checkAck(in)!=0){
                System.exit(0);
            }

            File _lfile = new File(lfile);

            if(ptimestamp){
                command="T "+(_lfile.lastModified()/1000)+" 0";
                // The access time should be sent here,
                // but it is not accessible with JavaAPI ;-<
                command+=(" "+(_lfile.lastModified()/1000)+" 0\n");
                out.write(command.getBytes()); out.flush();
                if(checkAck(in)!=0){
                    System.exit(0);
                }
            }

            // send "C0644 filesize filename", where filename should not include '/'
            long filesize=_lfile.length();
            command="C0644 "+filesize+" ";
            if(lfile.lastIndexOf('/')>0){
                command+=lfile.substring(lfile.lastIndexOf('/')+1);
            }
            else{
                command+=lfile;
            }
            command+="\n";
            out.write(command.getBytes()); out.flush();
            if(checkAck(in)!=0){
                System.exit(0);
            }

            // send a content of lfile
            fis=new FileInputStream(lfile);
            byte[] buf=new byte[1024];
            while(true){
                int len=fis.read(buf, 0, buf.length);
                if(len<=0) break;
                out.write(buf, 0, len); //out.flush();
            }
            fis.close();
            fis=null;
            // send '\0'
            buf[0]=0; out.write(buf, 0, 1); out.flush();
            if(checkAck(in)!=0){
                System.exit(0);
            }
            out.close();

            channel.disconnect();
            session.disconnect();

            System.exit(0);
        }
        catch(Exception e){
            System.out.println(e);
            try{if(fis!=null)fis.close();}catch(Exception ee){}
        }

    }







    public static String executeRemoteCommand(String username,String hostname,int port)
            throws Exception {
        JSch jsch = new JSch();
        Session session = jsch.getSession(username, hostname, port);
        jsch.addIdentity("my key", Conf.PRIVATEKEY.getBytes(), Conf.PUBLIC_KEY.getBytes(), Conf.PASSPHRASE.getBytes() );

        Properties prop = new Properties();
        prop.put("StrictHostKeyChecking", "no");
        session.setConfig(prop);
        session.connect();


        // SSH Channel
        ChannelExec channelssh = (ChannelExec)
                session.openChannel("exec");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        channelssh.setOutputStream(baos);

        // Execute command
        channelssh.setCommand("ls");
        channelssh.connect();
        channelssh.disconnect();
        return baos.toString();
    }



    public static String checkHosts(String subnet)
            throws Exception {
        int timeout=1000;
        String out = "SSH_ERROR";
        for (int i=100;i<120;i++){
            String host=subnet + "." + i;
            if (InetAddress.getByName(host).isReachable(timeout)){
                Log.i(TAG, host + " is reachable" );

                try{
                    executeRemoteCommand( Conf.USERNAME, host , 22);
                    return host;
                } catch (Exception e) {
                    //e.printStackTrace();
                }
            }
        }
        return "";
    }




    public static void zipPics (List<File> all_pic_files, File zipFile) throws Exception {
        final int BUFFER_SIZE = 2048;


        for (File strArr : all_pic_files) {
            Log.i(TAG, "------------------>" + strArr.getAbsolutePath() );
        }


        ZipOutputStream zipOut = new ZipOutputStream(new BufferedOutputStream( new FileOutputStream(zipFile) ));
        for (File fileToZip : all_pic_files) {
            FileInputStream fis = new FileInputStream(fileToZip);
            ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
            zipOut.putNextEntry(zipEntry);

            byte[] bytes = new byte[1024];
            int length;
            while((length = fis.read(bytes)) >= 0) {
                zipOut.write(bytes, 0, length);
            }
            fis.close();
        }
        zipOut.close();
    }


}
