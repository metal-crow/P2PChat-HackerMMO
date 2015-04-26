package main;
import game.Player;
import host.connection_listener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import GUI.GUI;

public class p2p_user {

	private static final int PORT = 8888;
	public static String HOST = "";
	public static String BACKUP_HOST="";
	
	public static String hosting="";
	private static Object LOCK = new Object();//lock to wait on user's response
	
	public static Socket clientsocket;
	public static String name="undefined";
	
	private static boolean connecting=true;
	public static volatile boolean connected=false;
	
	public static RSA Users_RSA= new RSA(1024);
	public static ArrayList<RSA> other_users_public_keys=new ArrayList<RSA>();
	
	public static ArrayList<String> blacklist=new ArrayList<String>();
	
	private static int height=450;
	private static int width=450;
	//other classes need the gui's settext method
	public static GUI gui=new GUI(height,width);
	private static JFrame f = new JFrame("Chat Room");
	
	public static volatile Player p=new Player();
    public static ArrayList<String> connectedUsers=new ArrayList<String>();
	
	public static void main(String[] args) {
        f.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        f.setSize(height,width);
		f.add(gui);
        f.pack();
        f.setVisible(true);
        f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		
		while(connecting){
			if(!connected){
				gui.resetConnectedUsers();
				
				//starting up for the first time, dont know host
				if(HOST.equals("")){
					gui.set_text("Are you connecting to a server or hosting new one? (C:connecting,H:hosting");
					
					//block to wait for user's response
					synchronized (LOCK) {
					    while (hosting.equals("")) {
					        try { LOCK.wait(); }
					        catch (InterruptedException e) {
					            // treat interrupt as exit request
					            break;
					        }
					    }
					}
				}
				
				if(hosting.equals("h")){
					//if hosting the server, make a connection listener
					try {
						//start server
						ServerSocket server=new ServerSocket(PORT);
						//start the listener for connecting clients
					    Thread connection_listener_thread = new Thread(new connection_listener(server));
					    connection_listener_thread.start();
					} catch (IOException e) {
						System.out.println("error creating server");
					}
					
					try {
					    //connect to your own server
					    BufferedReader in = new BufferedReader(new InputStreamReader(new URL("http://checkip.amazonaws.com").openStream()));
					    String ip = in.readLine(); //you get the IP as a String
						gui.set_text(InetAddress.getLocalHost().getHostAddress()+" is your local ip, "+ip+" is your remote ip");
						clientsocket = new Socket(InetAddress.getLocalHost().getHostAddress(), PORT);
					} catch (IOException e) {
						e.printStackTrace();
						System.out.println("error connecting to server you host");
					}
					connected=true;
				}
				
				else if(hosting.equals("c")){
					//starting up for the first time, dont know host. This is bypassed for reconnect
					if(HOST.equals("")){
						gui.set_text("Please enter the host's ip");
						
						//block to wait for user's response
						synchronized (LOCK) {
						    while (HOST.equals("")) {
						        try { LOCK.wait(); }
						        catch (InterruptedException e) {
						            // treat interrupt as exit request
						            break;
						        }
						    }
						}
					}
					
					try {
	                    gui.set_text("Establishing connection...");
						clientsocket = new Socket(HOST, PORT);
						connected=true;
					} catch (IOException e) {
						gui.set_text("Error connecting to server. The address may be invalid or the remote ip may be firewalled");
						HOST="";
						hosting="";
					}
				}
				
				if(connected){
					//make sure to listen to your own socket.
					Thread reciver_thread = new Thread(new listener_receiver());
					reciver_thread.start();
					
					//tell server your local ip
					try {
						String ip=InetAddress.getLocalHost().getHostAddress();
						new PrintWriter(clientsocket.getOutputStream(), true).println("User ip="+ip);
					} catch (IOException e) {
						gui.set_text("Unable to inform host of ip. You cannot become an emergency host.");
					}
					
					gui.set_text("You are connected! Type /help for a list of commands.");
				}
			}
		}
	}
	
	public static void handle_GUI_input(String users_input){
		//non chat commands, program managment user side
		if(!connected){
			if(users_input.toLowerCase().equals("h")){
				hosting="h";
			}
			else if(users_input.toLowerCase().equals("c")){
				hosting="c";
			}
			
			else if(users_input.matches("[0-9\\.]+")){
				HOST=users_input;
			}
			//unblock
			synchronized (LOCK) {
			    LOCK.notifyAll();
			}
		}
		
		else{
			//for user to exit (others can see)
			if(users_input.equals("/exit")){
				connecting=false;
				connected=false;
				try{
					new PrintWriter(clientsocket.getOutputStream(), true).println("<"+name+">"+" : "+users_input+" ["+p.curxp()+"]");
				}catch(IOException u){
					gui.set_text("ERROR: unable to alert others to exit");
				}
				try {
					clientsocket.close();
				} catch (IOException e) {
					e.printStackTrace();
					gui.set_text("ERROR: Could not exit");
				}
				gui.closeGUI();
				f.dispose();
			}
			
			//see commands (others can't see)
			else if(users_input.equals("/help")){
				gui.set_text("Type '/exit' to exit");
				gui.set_text("Type '/nick NEWNAME' to change name.");
				gui.set_text("Type '/request USERSNAME key' to be able a private message to a user.");
				gui.set_text("Type '/dm USERSNAME m:MESSAGETEXT' to send a private message to a user you have a key from.");
				gui.set_text("Type '/block USERNAME' to not see DM's and messages from this user");
				gui.set_text("Type '/unblock USERNAME' to unblock a user");
			}
			
			//for user to change name (others can see)
			else if(users_input.startsWith("/nick")){
			    String newname=users_input.substring(6);
			    if(!connectedUsers.contains(newname)){
    				try{
    					new PrintWriter(clientsocket.getOutputStream(), true).println(name+" is now called " + newname);
    				}catch(IOException u){
    					u.printStackTrace();
    					gui.set_text("ERROR: unable to alert others to name change");
    				}
    				name=users_input.substring(6);
    				gui.set_text("You are now called "+name);
			    }else{
	                 gui.set_text("A user with that name already exists.");
			    }
			}
			
			//for user to send a dm to a user using their public key (others can only see encrypted)
			else if(users_input.toLowerCase().matches("\\/dm (.*) m\\:(.*)")){
				//since a dm is supposed to be private, try to be forgiving if user fudges command
				String dm_message=users_input.substring(users_input.toLowerCase().indexOf("m:")+2);
				String username=users_input.substring(4,users_input.toLowerCase().indexOf(" m:"));
				boolean founduser=false;
				
				for(RSA user:other_users_public_keys){
					if(user.name().equals(username)){
						founduser=true;
						try{
							new PrintWriter(clientsocket.getOutputStream(), true).println("<"+name+">"+" : " +
									"DM-"+username+
									" m-"+user.Encrypt(dm_message));
							gui.set_text("Sucessfully send dm message:" +dm_message+" to "+username);
						}catch(IOException u){
							gui.set_text("ERROR: unable to send DM");
						}
					}
				}
				
				if(!founduser){
					gui.set_text("You do not have the key for user " + username + ". Request it and retry your message.");
				}
			}
			
			//NOTE: i know a user can just change their nick, but this is supposed to be an anonymous chat,
			//so i can't block a different way. Besides, if the user doesn't know they're blocked, this works.
			
			//add user to block list (not seen)
			else if(users_input.startsWith("/block")){
				blacklist.add(users_input.substring(7));
				gui.set_text("Blocked " + users_input.substring(7));
			}
			//add user to unblock list (not seen)
			else if(users_input.startsWith("/unblock")){
				String user=users_input.substring(9);
				if(blacklist.contains(user)){
					blacklist.remove(user);
					gui.set_text("Unblocked " + user);
				}
				else{
					gui.set_text("User " +user+ " not in list of blocked users");
				}
			}

            //GAME ABILTIES
            else if(users_input.startsWith("/kick") && p.hasAbility("kick")){
                checkCooldown("kick",users_input);
            }
            else if(users_input.startsWith("/disable") && p.hasAbility("disable")){
                checkCooldown("disable",users_input);
            }
            else if(users_input.startsWith("/scramble") && p.hasAbility("scramble")){
                checkCooldown("scramble",users_input);
            }
            else if(users_input.startsWith("/forceblock") && p.hasAbility("forceblock")){
                checkCooldown("forceblock",users_input);
            }
            else if(users_input.startsWith("/viewall") && p.hasAbility("viewall")){
                p.viewall=!p.viewall;
            }
            else if(users_input.startsWith("/mimic") && p.hasAbility("mimic")){
                int cooltimeleft=p.cooltimeleft("mimic");
                if(cooltimeleft<=0){
                    try{
                        new PrintWriter(clientsocket.getOutputStream(), true).println("<"+users_input.substring(7,users_input.indexOf(" ",8))+">"+" : "+users_input.substring(users_input.indexOf(" ",8)+1));
                    }catch(IOException u){
                        gui.set_text("ERROR: Unable to send.");
                    }
                    gui.set_text(users_input);
                    p.cooldown("mimic");
                }else{
                    gui.set_text("Ability on cooldown, "+cooltimeleft+" seconds remaining.");
                }
            }
			
			//anything not specifically caught by commands
			else{
				//write to the socket's output stream and the server picks it up
				try{
					new PrintWriter(clientsocket.getOutputStream(), true).println("<"+name+">"+" : "+users_input);
				}catch(IOException u){
					gui.set_text("ERROR: Unable to send.");
				}
			}
		}
	}

    private static void checkCooldown(String string,String users_input) {
        int cooltimeleft=p.cooltimeleft(string);
        if(cooltimeleft<=0){
            try {
                new PrintWriter(clientsocket.getOutputStream(), true).println("<"+name+">"+" : "+users_input);
            } catch (IOException e) {
                gui.set_text("ERROR: Unable to send.");
            }
            gui.set_text(users_input);
            p.cooldown(string);
        }else{
            gui.set_text("Ability on cooldown, "+cooltimeleft+" seconds remaining.");
        }        
    }
	
}
