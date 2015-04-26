package GUI;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Document;

import main.p2p_user;

@SuppressWarnings("serial")
public class GUI extends JPanel{
	private int width;
	private int height;
	
	private JTextPane chat_text = new JTextPane();
	private JTextField input = new JTextField(25);
	private JTextPane connected_users = new JTextPane();
	public  JPanel abilities=new JPanel();
	private JLabel progess=new JLabel("<html>Hacking progess 0%</html>");

	//TODO color coding,fix layout
	
	public GUI(int height, int width){
		this.width=width;
		this.height=height;
		setLayout(new BorderLayout());
				
		chat_text.setEditable(false);
		DefaultCaret caret = (DefaultCaret)chat_text.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		
		JScrollPane chat_text_sp = new JScrollPane(chat_text);
		
		//send and clear text
		JButton send = new JButton("Send");
		send.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
            	String user_input=input.getText();
            	if(user_input!=null && user_input.length()>0){
		    		p2p_user.handle_GUI_input(input.getText());
		    		input.setText("");
		    	}
            }
		});
		
		//on enter press in input, field, do same as send does
		input.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,0),"send");
		input.getActionMap().put("send",new AbstractAction() {
		    public void actionPerformed(ActionEvent e) {
		    	String user_input=input.getText();
		    	if(user_input!=null && user_input.length()>0){
		    		p2p_user.handle_GUI_input(input.getText());
		    		input.setText("");
		    	}
		    }
		});
		
		connected_users.setEditable(false);
		JScrollPane connected_users_sp = new JScrollPane(connected_users);
		
		//left sidebar, powers
		abilities.setLayout(new BoxLayout(abilities, BoxLayout.Y_AXIS));

		//various abilities
		JButton viewall=new JButton("(Un)View All");
		viewall.setMargin(new Insets(0,0,0,0));
		viewall.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                input.setText("/viewall");
            }
        });
        abilities.add(viewall);
		JButton kick=new JButton("Kick");
		kick.setMargin(new Insets(0,0,0,0));
		kick.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                input.setText("/kick [target]");
            }
        });
		abilities.add(kick);
        JButton disable=new JButton("Disable");
        disable.setMargin(new Insets(0,0,0,0));
        disable.setEnabled(false);
        disable.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                input.setText("/disable [target]");
            }
        });
        abilities.add(disable);
        JButton scramble=new JButton("(Un)Scramble");
        scramble.setMargin(new Insets(0,0,0,0));
        scramble.setEnabled(false);
        scramble.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                input.setText("/scramble [target]");
            }
        });
        abilities.add(scramble);
        JButton fblock=new JButton("Force Block");
        fblock.setMargin(new Insets(0,0,0,0));
        fblock.setEnabled(false);
        fblock.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                input.setText("/forceblock [target] [user]");
            }
        });
        abilities.add(fblock);
        JButton mimic=new JButton("Mimic user");
        mimic.setMargin(new Insets(0,0,0,0));
        mimic.setEnabled(false);
        mimic.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                input.setText("/mimic [name] [message]");
            }
        });
        abilities.add(mimic);
        
        abilities.add(progess);

		abilities.setPreferredSize(new Dimension(90,height-50));
		add(abilities,BorderLayout.LINE_START);
		
		chat_text_sp.setPreferredSize(new Dimension(width-210,height-50));
		add(chat_text_sp,BorderLayout.CENTER);
		
		connected_users_sp.setPreferredSize(new Dimension(60,height-50));
		add(connected_users_sp,BorderLayout.LINE_END);
		
		JPanel user_input = new JPanel();
		user_input.add(input);
		user_input.add(send);
		add(user_input,BorderLayout.PAGE_END);
		
		Timer timer = new Timer(1000, new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                if(p2p_user.p.cooltimeleft("kick")<=0){
                    abilities.getComponent(1).setEnabled(true);
                }else{
                    abilities.getComponent(1).setEnabled(false);
                    p2p_user.p.cooldowntick("kick");
                }
                
                if(p2p_user.p.hasAbility("disable") && p2p_user.p.cooltimeleft("disable")<=0){
                    abilities.getComponent(2).setEnabled(true);
                }else{
                    abilities.getComponent(2).setEnabled(false);
                    p2p_user.p.cooldowntick("disable");
                }
                
                if(p2p_user.p.hasAbility("scramble") && p2p_user.p.cooltimeleft("scramble")<=0){
                    abilities.getComponent(3).setEnabled(true);
                }else{
                    abilities.getComponent(3).setEnabled(false);
                    p2p_user.p.cooldowntick("scramble");
                }
                
                if(p2p_user.p.hasAbility("forceblock") && p2p_user.p.cooltimeleft("forceblock")<=0){
                    abilities.getComponent(4).setEnabled(true);
                }else{
                    abilities.getComponent(4).setEnabled(false);
                    p2p_user.p.cooldowntick("forceblock");
                }
                
                if(p2p_user.p.hasAbility("mimic") && p2p_user.p.cooltimeleft("mimic")<=0){
                    abilities.getComponent(5).setEnabled(true);
                }else{
                    abilities.getComponent(5).setEnabled(false);
                    p2p_user.p.cooldowntick("mimic");
                }
                
                //you gain an xp for every second you stay online
                if(p2p_user.connected){
                    p2p_user.p.gainxp();
                    progess.setText("<html>Hacking progess "+p2p_user.p.exp+"%</html>");
                }
            }
        });
		timer.start(); 
	}
	
	public Dimension getPreferredSize() {
        return new Dimension(width,height);
    }
	
	public void set_text(String txt){
		try {
		      Document doc = chat_text.getDocument();
		      doc.insertString(doc.getLength(), txt+"\n", null);
		} catch(BadLocationException exc) {
		      exc.printStackTrace();
		      System.out.println("Could not add to chat textbox");
		}
	}
	
	public void addUser(String user){
		try {
		      Document doc = connected_users.getDocument();
		      doc.insertString(doc.getLength(), user+"\n", null);
		} catch(BadLocationException exc) {
		      exc.printStackTrace();
		      System.out.println("Could not add user to connected user textbox");
		}
	}
	public void removeUser(String user){
		try {
		      Document doc = connected_users.getDocument();
		      int location_of_name=doc.getText(0, doc.getLength()).indexOf(user);
		      doc.remove(location_of_name, user.length()+1);
		} catch(BadLocationException exc) {
		      exc.printStackTrace();
		      System.out.println("Could not remove user from connected user textbox");
		}
	}
	public void replaceUser(String oldname,String newname){
		try {
		      Document doc = connected_users.getDocument();
		      int location_of_name=doc.getText(0, doc.getLength()).indexOf(oldname);
		      doc.remove(location_of_name, oldname.length());
		      doc.insertString(location_of_name, newname, null);
		} catch(BadLocationException exc) {
		      exc.printStackTrace();
		      System.out.println("Could not change users name in connected user textbox");
		}
	}
	//wipe list of currently connected users (used if a user takes over as host, we dont want old list sticking around)
	public void resetConnectedUsers(){
		connected_users.setText("");
	}
	//this seems wrong
	public void closeGUI(){
		System.exit(0);
	}
}
