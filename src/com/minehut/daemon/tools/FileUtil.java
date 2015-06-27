package com.minehut.daemon.tools;

import org.apache.commons.io.FileUtils;

import com.minehut.daemon.Kingdom;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

/**
 * Created by luke on 4/11/15.
 */
public class FileUtil {

    public static void editServerProperties(int id, int port) {
        try {
            //Edit server.properties
            Properties props = new Properties();
            String propsFileName = "/home/kingdoms/kingdom" + Integer.toString(id) + "/server.properties";

            //first load old one:
            FileInputStream configStream = new FileInputStream(propsFileName);
            props.load(configStream);
            configStream.close();
            System.out.println("Detected old port: " + props.getProperty("server-port"));

            //modifies existing or adds new property
            String newPort = Integer.toString(port);
            System.out.println("Setting new port to: " + newPort);
            props.setProperty("server-port", newPort);

            //save modified property file
            FileOutputStream output = new FileOutputStream(propsFileName);
            props.store(output, "This description goes to the header of a file");
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void installKingdom(Kingdom kingdom) {
    	if (!kingdom.isInstalled()) {
    		File home = new File(kingdom.getHomeDir());
    		if (!home.exists())
    			home.mkdir();
    		
    	}
    }
    
    public static void copyFile(File source, File target) {
        try {
            ArrayList<String> ignore = new ArrayList<String>(Arrays.asList("uid.dat", "session.dat"));
            if (!ignore.contains(source.getName())) {
                if (source.isDirectory()) {
                    if (!target.exists())
                        target.mkdirs();
                    String files[] = source.list();
                    for (String file : files) {
                        File srcFile = new File(source, file);
                        File destFile = new File(target, file);
                        copyFile(srcFile, destFile);
                    }
                } else {
                    InputStream in = new FileInputStream(source);
                    OutputStream out = new FileOutputStream(target);
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = in.read(buffer)) > 0)
                        out.write(buffer, 0, length);
                    in.close();
                    out.close();
                }
            }
        } catch (IOException e) {

        }
    }

    public static void copySampleServer(int id) {

        try {
            File source = new File("/home/kingdoms/sampleKingdom");
            File target = new File("/home/kingdoms/kingdom" + Integer.toString(id));

            FileUtils.deleteDirectory(target);

//            ArrayList<String> ignore = new ArrayList<String>(Arrays.asList("uid.dat", "session.dat"));
//            if (!ignore.contains(source.getName())) {
                if (source.isDirectory()) {
                    if (!target.exists())
                        target.mkdirs();
                    String files[] = source.list();
                    for (String file : files) {
                        File srcFile = new File(source, file);
                        File destFile = new File(target, file);
                        copyFile(srcFile, destFile);
                    }
                } else {
                    InputStream in = new FileInputStream(source);
                    OutputStream out = new FileOutputStream(target);
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = in.read(buffer)) > 0)
                        out.write(buffer, 0, length);
                    in.close();
                    out.close();
                }
//            }

        } catch (IOException e) {

        }
    }

    public static void checkAndExecuteActions(String folder) {

        /* Reset World */
        File resetAction = new File(folder + "actions/resetmap.action");
        if (resetAction.exists()) {
            File world = new File(folder + "world");
            try {
                FileUtils.deleteDirectory(world);
                resetAction.delete();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

