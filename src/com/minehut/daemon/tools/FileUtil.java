package com.minehut.daemon.tools;

import org.apache.commons.io.FileUtils;

import com.google.gson.Gson;
import com.minehut.daemon.Kingdom;
import com.minehut.daemon.protocol.addon.Addon;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

/**
 * Created by luke on 4/11/15.
 */
public class FileUtil {

	public static Gson gson = new Gson();
	
    public static void editServerProperties(Kingdom kingdom, int port) {
        try {
            //Edit server.properties
            Properties props = new Properties();
            String propsFileName = "/home/rdillender/daemon/kingdoms/" + kingdom.getOwner().playerUUID + "/kingdom" + kingdom.id + "/server.properties";

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
    
    public static void removeAddon(Kingdom kingdom, Addon addon) {
    	kingdom.removeAddon(addon);
    	saveKingdom(kingdom);
    	//TODO: write function that gets a list of all files it installed then remove them from the system
    	//This will allow us to remove any file we install but keep anything the plugin/mod create safe in the folder
    	//So if they every re-installed the data would still be there. 
    	//The Reset payload will still clear all that data however.
    }
    
    public static void installAddon(Kingdom kingdom, Addon addon) {
    	kingdom.addAddon(addon);
    	saveKingdom(kingdom); //Updates kingdom's data.json to have the addon list in it with the new addon in the list.
    	File home = new File(kingdom.getHomeDir());
    	File addonDir = new File("./addons/" + addon.systemName + "/install");
		copyFile(addonDir, home);
    }
    
    public static void resetKingdom(Kingdom kingdom) {
    	File home = new File(kingdom.getHomeDir());
    	System.out.println("Resetting Kingdom Files " + kingdom.getHomeDir());
    	File[] kingdomFiles = home.listFiles();
    	if(kingdomFiles!=null) {
    		for (File file : kingdomFiles) {
    			deleteFiles(file);
    		}
    	}
    	installKingdom(kingdom);
    }
    
    private static void deleteFiles(File file) {
    	System.out.println("Removing " + file.getPath());
    	if (file.getName().contains("screenlog")) {
    		System.out.println("SCREEN LOG FOUND, SKIPPING");
    		return;
    	}
    	if (file.isDirectory()) {
    		File[] files = file.listFiles();
    		if (files!=null) {
    			for (File f : files) {
    				deleteFiles(f);
    			}
    		}
    	}
    	file.delete();
    }
    
    public static void installKingdom(Kingdom kingdom) {
    	//if (!kingdom.isInstalled()) {
    		File home = new File(kingdom.getHomeDir());
    		if (!home.exists())
    			home.mkdir();
    		saveKingdom(kingdom);
    		File sampleDir = new File("./sample-kingdoms/" + kingdom.getSampleKingdom().getType() + "/install");
    		System.out.println(sampleDir.toString());
    		copyFile(sampleDir, home);
    		
    	//}
    }
    
    public static void renameKingdom(Kingdom kingdom) {
    	saveKingdom(kingdom);//Should just override the data.json
    }
    
    public static void saveKingdom(Kingdom kingdom) {
    	FileWriter writer = null;
    	try {
    		writer = new FileWriter(kingdom.getHomeDir() + "/data.json");
    		writer.write(gson.toJson(kingdom));
    	} catch (IOException e) {
    		e.printStackTrace();
    	} finally {
    		if (writer!=null) {
    			try {
    				writer.close();
    			} catch (IOException e) {
    				e.printStackTrace();
    			}
    		}
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

