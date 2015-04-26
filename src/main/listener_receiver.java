package main;
import game.Player;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

//this listener thread listens to this socket's input stream
//print any text it receives, as well as handling commands
public class listener_receiver implements Runnable{
	
	public void run() {
		BufferedReader get = null;
		try{
			get = new BufferedReader(new InputStreamReader(p2p_user.clientsocket.getInputStream()));
		}catch(IOException e){
			e.printStackTrace();
			System.out.println("Could not open input stream");
		}
		
		while(p2p_user.connected){
			String inputstring = null;
			try {
				inputstring = get.readLine();
			} catch (IOException e1) {
				//user has properly closed the socket, exit thread
				if(!p2p_user.connected){
					return;
				}
				//the server side has been improperly closed
				else{
					p2p_user.gui.set_text("Server has been destroyed.");
					//reset main loop to beginning (try to create server)
					p2p_user.connected=false;
					
					//new host is the emergency host
					if(!p2p_user.BACKUP_HOST.equals("")){
						String thisip="";
						try {
							thisip=InetAddress.getLocalHost().getHostAddress();
						} catch (UnknownHostException e) {
							p2p_user.gui.set_text("Unable to verify you are the emergency host.");
						}
						//if you are the emergency host
						if((p2p_user.BACKUP_HOST).equals(thisip)){
							p2p_user.hosting="h";
							p2p_user.gui.set_text("Recreating server.");
						}
						
						else{
							//connect, dont host
							p2p_user.hosting="c";
							p2p_user.gui.set_text("Connecting to backup server.");
						}
						
						p2p_user.HOST=p2p_user.BACKUP_HOST;
						p2p_user.BACKUP_HOST="";
					}
					
					//emergency catch if no one is assigned to be emergency host, this user hosts
					else{
						p2p_user.hosting="h";
						p2p_user.gui.set_text("No backup server found. Starting a new server.");
					}
					//close this thread (main will reopen it)
					return;
				}
			}
			
			//this prevents null reading, and blocks until such time
			if(inputstring!=null && inputstring.length()>0){
			    String userviewstxt="";
				
				//check to see if its the server assiging name
				if(inputstring.startsWith("server-assigned-nick: ")){
					p2p_user.name=inputstring.substring(22);
					userviewstxt=("you are called "+p2p_user.name);
				}
				
				else if(inputstring.matches("[0-9\\.]+ is the emergency host")){
				    if(p2p_user.p.viewall){
				        userviewstxt=inputstring;
				    }
					p2p_user.BACKUP_HOST=inputstring.substring(0,inputstring.indexOf(" is the emergency host"));
				}
				
				//if someone wants user's public key, broadcast it
				else if(inputstring.matches("\\[[0-9]{2}\\:[0-9]{2}\\:[0-9]{2}\\] \\<(.*)\\> \\: \\/request " +p2p_user.name+ " key")){
					BigInteger[] publickey=p2p_user.Users_RSA.publickey();
					
					try{
						new PrintWriter(p2p_user.clientsocket.getOutputStream(), true).println(
								"Public Key for " + p2p_user.name + ":" +
								"n-"+publickey[0] +
								"e-"+publickey[1]);
						
						userviewstxt=(inputstring.substring(0,inputstring.indexOf("> :")+1) +" requested your public key. It has been broadcast.");
					}catch(IOException u){
						u.printStackTrace();
						userviewstxt=("ERROR: unable to broadcast public key");
					}
				}
				
				//if someone else is broadcasting their public key, store it
				else if(inputstring.matches("\\[[0-9]{2}\\:[0-9]{2}\\:[0-9]{2}\\] Public Key for (.*)\\:n-[0-9]+e-[0-9]+")){
					BigInteger n=new BigInteger(inputstring.substring(inputstring.indexOf("n-")+2, inputstring.indexOf("e-")));
					BigInteger e=new BigInteger(inputstring.substring(inputstring.indexOf("e-")+2));
					String name=inputstring.substring(inputstring.indexOf("Public Key for ")+15,inputstring.indexOf(":n-"));
					String date=inputstring.substring(0,10);
					
					//if its not your public key and you dont already have it
					if(!name.equals(p2p_user.name) && !p2p_user.other_users_public_keys.contains(name)){
						p2p_user.other_users_public_keys.add(new RSA(n,e,name));
						userviewstxt=(date+" Got "+name+" public key");
					}
				}
				
				//if someone is sending a dm
				else if(inputstring.matches("\\[[0-9]{2}\\:[0-9]{2}\\:[0-9]{2}\\] \\<(.*)\\> \\: DM-(.*) m-[0-9]+") && !p2p_user.blacklist.contains(inputstring.substring(inputstring.indexOf("<")+1,inputstring.indexOf(">")))){
				    if(p2p_user.p.viewall){
                        userviewstxt=inputstring;
                    }
				    //check if its directed to this user, and decrypt it
				    if(inputstring.matches("\\[[0-9]{2}\\:[0-9]{2}\\:[0-9]{2}\\] \\<(.*)\\> \\: DM-"+p2p_user.name+" m-[0-9]+")){
				        BigInteger encryptedmss= new BigInteger(inputstring.substring(inputstring.indexOf("m-")+2));
				        userviewstxt=(inputstring.substring(0,inputstring.indexOf("m-")+2)
										+p2p_user.Users_RSA.Decrypt(encryptedmss));
				    }
				}
				
				//if someone exits
				else if(inputstring.matches("\\[[0-9]{2}\\:[0-9]{2}\\:[0-9]{2}\\] \\<(.*)\\> \\: \\/exit (.*)")){
					String name=inputstring.substring(inputstring.indexOf("<")+1,inputstring.indexOf(">"));
					userviewstxt=(name + " left the chat");
					//remove from connected users
					p2p_user.gui.removeUser(name);
	                //check their high score
					int highscorerecivedval=Integer.valueOf(inputstring.substring(inputstring.lastIndexOf("[")+1,inputstring.lastIndexOf("]")));
                    if(p2p_user.p.highScoreVal()<highscorerecivedval){
                        p2p_user.p.storeHighScore(name, highscorerecivedval);
                        userviewstxt+=" with a new high score of "+highscorerecivedval+"!";
                    }

				}
				
				//if someone connects
				else if(inputstring.matches("\\[[0-9]{2}\\:[0-9]{2}\\:[0-9]{2}\\] User \\<(.*)\\> connected to chat")){
					String name=inputstring.substring(inputstring.indexOf("<")+1,inputstring.indexOf(">"));
					userviewstxt=(inputstring);
					//add connected user
					p2p_user.gui.addUser(name);
				}
				
				//if user connects and receives the list of people already connected
				else if(inputstring.matches("User \\<(.*)\\> is connected to chat")){
					String name=inputstring.substring(inputstring.indexOf("<")+1,inputstring.indexOf(">"));
					//add connected user
					p2p_user.gui.addUser(name);
				}
				
				//if someone changes their name
				else if(inputstring.matches("\\[[0-9]{2}\\:[0-9]{2}\\:[0-9]{2}\\] (.*) is now called (.*)")){
					String oldname=inputstring.substring(inputstring.indexOf("]")+2,inputstring.indexOf(" is"));
					String newname=inputstring.substring(inputstring.indexOf("called ")+7);
					userviewstxt=(inputstring);
					//replace connected users name
					p2p_user.gui.replaceUser(oldname,newname);
				}
				
				//Game Abilities
				//kick ability is prevented by a block
				else if(inputstring.matches("\\[[0-9]{2}\\:[0-9]{2}\\:[0-9]{2}\\] \\<(.*)\\> \\: \\/kick (.*)") && !p2p_user.blacklist.contains(inputstring.substring(inputstring.indexOf("<")+1,inputstring.indexOf(">")))){
				    String directeduser=inputstring.substring(inputstring.indexOf("/kick")+6);
				    if(p2p_user.p.viewall){
                        userviewstxt=inputstring;
                    }
				    //if directed at us
				    if(directeduser.equals(p2p_user.name)){
				        //disconnect
	                    p2p_user.handle_GUI_input("/exit");
				    }
				}
				//disable disables a random ability
				else if(inputstring.matches("\\[[0-9]{2}\\:[0-9]{2}\\:[0-9]{2}\\] \\<(.*)\\> \\: \\/disable (.*)")){
                    String directeduser=inputstring.substring(inputstring.indexOf("/disable")+9);
                    if(p2p_user.p.viewall){
                        userviewstxt=inputstring;
                    }
                    //if directed at us
                    if(directeduser.equals(p2p_user.name)){
                        //choose random ability, make it cooldown
                        p2p_user.p.cooldown(Player.abilities[new Random().nextInt(Player.abilities.length)]);
                    }
				}
				//scramble converts all text you see into 
				else if(inputstring.matches("\\[[0-9]{2}\\:[0-9]{2}\\:[0-9]{2}\\] \\<(.*)\\> \\: \\/scramble (.*)")){
				    if(p2p_user.p.viewall){
                        userviewstxt=inputstring;
                    }
                    String directeduser=inputstring.substring(inputstring.indexOf("/scramble")+10);
                    //if directed at us
                    if(directeduser.equals(p2p_user.name)){
                        p2p_user.p.scrambled=!p2p_user.p.scrambled;
                    }
				}
				//forced to block another user
                else if(inputstring.matches("\\[[0-9]{2}\\:[0-9]{2}\\:[0-9]{2}\\] \\<(.*)\\> \\: \\/forceblock (.*)")){
                    if(p2p_user.p.viewall){
                        userviewstxt=inputstring;
                    }
                    String directeduser=inputstring.substring(inputstring.indexOf("/forceblock")+12,inputstring.indexOf(" ", inputstring.indexOf("/forceblock")+13));
                    String usertoblock=inputstring.substring(inputstring.indexOf(" ", inputstring.indexOf("/forceblock")+13));
                    //if directed at us
                    if(directeduser.equals(p2p_user.name)){
                        p2p_user.blacklist.add(usertoblock);
                    }
                }
				
				else{
					//This works, but feels wrong
					//check is this text is a user text message (not a user action alert), and then check against blacklist
					if(inputstring.matches("(.*)<(.*)>(.*)") && !p2p_user.blacklist.contains(inputstring.substring(inputstring.indexOf("<")+1,inputstring.indexOf(">")))){
					    userviewstxt=(inputstring);
					}
					//text only doesnt have <Name> if its an action alert. Allow it
					else if(!inputstring.matches("(.*)<(.*)>(.*)")){
					    userviewstxt=(inputstring);
					}
				}
				
				if(!userviewstxt.isEmpty()){
				    //if user is scrambled, alter text b4 its seen
				    if(p2p_user.p.scrambled){
				        userviewstxt=scrambleText(userviewstxt);
				    }
				    p2p_user.gui.set_text(userviewstxt);
				}
				
				//have to flush string
				inputstring=null;
			}
		}
	}

    private String scrambleText(String userviewstxt) {
        StringBuilder out=new StringBuilder();
        //for each word
        String[] sentence=userviewstxt.split(" ");
        for(String word:sentence){
            //trim punctuation off word
            String endpunc = "";
            boolean endpunccheck=false;
            for(char c:word.toCharArray()){
                if(!Character.isAlphabetic(c)){
                    if(!endpunccheck){
                        out.append(c);
                    }else{
                        endpunc+=c;
                    }
                }else{
                    endpunccheck=true;
                }
            }
            word = word.replaceAll("[^a-zA-Z]", "");

            if(word.length()>2){
                //scramble the word,keeping 1st and last letter
                out.append(word.charAt(0));
                char end=word.charAt(word.length()-1);
                //take letters in middle and shuffle
                char[] middle=word.substring(1, word.length()-1).toCharArray();
                //if the word is 4 letters long, force shuffle
                if(middle.length==2){
                    out.append(middle[1]);
                    out.append(middle[0]);
                }else{
                    List<Character> l = new ArrayList<Character>();
                    for(char c : middle)
                        l.add(c); 
                    Collections.shuffle(l);
                    for(char c : l)
                        out.append(c);
                }
                out.append(end+endpunc+" ");
            }else{
                out.append(word+endpunc+" ");
            }
        }
        return out.toString();
    }
}
