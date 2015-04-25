package GUI;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
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
	
	//TODO color coding,fix layout
	
	public GUI(int height, int width){
		this.width=width;
		this.height=height;
				
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
		JPanel abilities=new JPanel();
		abilities.setLayout(new BoxLayout(abilities, BoxLayout.Y_AXIS));

		//various abilities
		JButton kick=new JButton("Kick");
		kick.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                input.setText("/kick [target]");
            }
        });
		abilities.add(kick);
		
		abilities.setPreferredSize(new Dimension(60,height-50));
		add(abilities,BorderLayout.LINE_START);
		
		chat_text_sp.setPreferredSize(new Dimension(width-180,height-50));
		add(chat_text_sp,BorderLayout.CENTER);
		
		connected_users_sp.setPreferredSize(new Dimension(60,height-50));
		add(connected_users_sp,BorderLayout.LINE_END);
		
		JPanel user_input = new JPanel();
		user_input.add(input);
		user_input.add(send);
		add(user_input,BorderLayout.PAGE_END);
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
