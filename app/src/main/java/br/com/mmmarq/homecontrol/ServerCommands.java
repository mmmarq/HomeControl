package br.com.mmmarq.homecontrol;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.keyczar.Crypter;
import org.keyczar.Encrypter;
import org.keyczar.exceptions.KeyczarException;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;

public class ServerCommands {

    private static String PUB_KEY_PATH = Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator+"Keys"+File.separator+"public";
    private static String PVT_KEY_PATH = Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator+"Keys"+File.separator+"private";
    private static String HST_SRV_PATH = Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator+"Keys"+File.separator+"host";
    private static int MSGLEN = 178;
    
   	public static String sendServerCommand(String command,Context context){
		String LOCALNETWORK = "";
		String LOCALIP = "";
		String REMOTESERVER = "";
		String SERVER = "";
		String PORT = "0";
		
		String commandResult = new String();
		char[] incoming = new char[MSGLEN];
		
		try{
   	   	 	Encrypter encrypter = new Encrypter(PUB_KEY_PATH);
   	   	 	Crypter crypter = new Crypter(PVT_KEY_PATH);
   		   	String cypheredCommand = encrypter.encrypt(command);

   		   	//Read server hostname and port from config file
   		   	File hostFile = new File(HST_SRV_PATH);
   		    BufferedReader br = new BufferedReader(new FileReader(hostFile));

   		    String line;
   		    while ((line = br.readLine()) != null){
   		    	if (line.startsWith("localnetwork:")){
   		    		LOCALNETWORK = line.split(":")[1];
   		    	}else{
   		    		if (line.startsWith("localip:")){
   		    			LOCALIP = line.split(":")[1];
   		    		}else{
   		    			if (line.startsWith("remotename:")){
   		    				REMOTESERVER = line.split(":")[1];
   		    			}else{
   		    				if (line.startsWith("port:")){
   		    					PORT = line.split(":")[1];
   		    				}
   		    			}
   		    		}
   		    	}
   		    }
   		    br.close();

   		    String networkName = ServerCommands.getNetowrkName(context).toLowerCase();
   		    if (networkName.contains(LOCALNETWORK.toLowerCase())){
   		    	SERVER = LOCALIP;
   		    }else{
   		    	SERVER = REMOTESERVER;
   		    }
   		   	//Create socket
			Socket socket = new Socket(SERVER, Integer.parseInt(PORT));
			
		   	//Send command to server
		   	PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true);
		   	out.println(cypheredCommand);

		   	//Read server response
		   	BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));

		    try{
		   		while (!socket.isClosed()){
		   			int lenght = input.read(incoming);
		   			if (lenght >= 0){
		   				commandResult = String.copyValueOf(incoming,0,lenght);
		   			}else{
		   				if (commandResult.length() >= MSGLEN){
		   				socket.close();
		   				}
		   			}
		   		}
		    }catch (SocketException e){
		    	e.printStackTrace();
		   	}
		   	commandResult = crypter.decrypt(commandResult);
		}catch (UnknownHostException e){
			return new String();
		}catch (IOException e){
			return new String();
		}catch(KeyczarException e){
			return new String();
		}

		return commandResult;
	}

    public static byte[] sendImageRequest(String command,Context context){
		String LOCALNETWORK = "";
		String LOCALIP = "";
		String REMOTESERVER = "";
		String SERVER = "";
		String PORT = "0";
		
		byte[] pic = new byte[0];
		
		try{
   	   	 	Encrypter encrypter = new Encrypter(PUB_KEY_PATH);
   	   	 	Crypter crypter = new Crypter(PVT_KEY_PATH);
   		   	String cypheredCommand = encrypter.encrypt(context.getString(R.string.image_request_command).concat(command));

   		   	//Read server hostname and port from config file
   		   	File hostFile = new File(HST_SRV_PATH);
   		    BufferedReader br = new BufferedReader(new FileReader(hostFile));

   		    String line;
   		    while ((line = br.readLine()) != null){
   		    	if (line.startsWith("localnetwork:")){
   		    		LOCALNETWORK = line.split(":")[1];
   		    	}else{
   		    		if (line.startsWith("localip:")){
   		    			LOCALIP = line.split(":")[1];
   		    		}else{
   		    			if (line.startsWith("remotename:")){
   		    				REMOTESERVER = line.split(":")[1];
   		    			}else{
   		    				if (line.startsWith("port:")){
   		    					PORT = line.split(":")[1];
   		    				}
   		    			}
   		    		}
   		    	}
   		    }
   		    br.close();

   		    String networkName = ServerCommands.getNetowrkName(context).toLowerCase(); 
   		    if (networkName.contains(LOCALNETWORK.toLowerCase())){
   		    	SERVER = LOCALIP;
   		    }else{
   		    	SERVER = REMOTESERVER;
   		    }
   		   	//Create socket
			Socket socket = new Socket(SERVER, Integer.parseInt(PORT));
						
		   	//Send command to server
		   	PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true);
		   	out.println(cypheredCommand);

		   	//Read server response
		   	DataInputStream input = new DataInputStream(socket.getInputStream());

		   	//byte[] pic = new byte[30136];
            //input.readFully(pic, 0, pic.length);
            //bitmap = BitmapFactory.decodeByteArray(pic, 0, pic.length); 

		   	int imageLen = input.readInt();
		   	pic = new byte[imageLen];
            input.readFully(pic);
		   	
            socket.close();
		}catch (UnknownHostException e){
			return pic;
		}catch (IOException e){
			return pic;
		}catch(KeyczarException e){
			return pic;
		}
		return pic;
	}
    	
    public static boolean checkConnectivity(Context context){
    	/*Check network availability*/
   		ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
   		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if ( activeNetwork != null ){
        	boolean isConnected = activeNetwork.isAvailable();
        	//boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        	//boolean isWiFi = activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
        	//if ( isConnected && isWiFi ){
            if ( isConnected ){
        		return true;
        	}else{
        		return false;
        	}
        }else{
        	return false;
        }
    }
    
    public static String getNetowrkName(Context context){
   		ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
   		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if ( activeNetwork != null ){
           if (activeNetwork.getExtraInfo() == null){
			   return activeNetwork.getTypeName();
		   }else {
               return activeNetwork.getExtraInfo();
           }
        }else{
        	return null;
        }
    }
}
