package alpv_ws1415.ub1.webradio.webradio.client;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.InetSocketAddress;

import javax.swing.*;
import javax.swing.event.*;

import alpv_ws1415.ub1.webradio.ui.ClientUI;
import alpv_ws1415.ub1.webradio.webradio.server.TextMessage;

/**
 * Zeigt eine grafische Oberfläche für den Client an
 */

public class ClientGUI extends JFrame implements ClientUI
{
	private Container cp;
	private JButton playButton = new JButton();
	private JButton stopButton = new JButton();
	private JButton connectButton = new JButton();
	private JLabel nowPlaying = new JLabel();
	private JTextArea chatArea = new JTextArea("");
	private JTextField chatInput = new JTextField();
	private JButton chatSendButton = new JButton();
	
	RadioClient context;
	String username;
	InetSocketAddress address;
	
	public ClientGUI(RadioClient context)
	{
		// Frame-Initialisierung
		super("Webradio Client");
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent evt)
			{
				close();
			}
		});
		
		this.context = context;
	}
	
	public void run()
	{
		int frameWidth = 561;
		int frameHeight = 368;
		setSize(frameWidth, frameHeight);
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		int x = (d.width - getSize().width) / 2;
		int y = (d.height - getSize().height) / 2;
		setLocation(x, y);
		cp = getContentPane();
		cp.setLayout(null);
		
		playButton.setBounds(8, 8, 41, 33);
		playButton.setText("play");
		playButton.setMargin(new Insets(2, 2, 2, 2));
		playButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				playButton_ActionPerformed(evt);
			}
		});
		cp.add(playButton);
		
		stopButton.setBounds(56, 8, 41, 33);
		stopButton.setText("stop");
		stopButton.setMargin(new Insets(2, 2, 2, 2));
		stopButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				stopButton_ActionPerformed(evt);
			}
		});
		cp.add(stopButton);
		
		connectButton.setBounds(104, 8, 65, 33);
		connectButton.setText("connect...");
		connectButton.setMargin(new Insets(2, 2, 2, 2));
		connectButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				connectButton_ActionPerformed(evt);
			}
		});
		cp.add(connectButton);
		
		nowPlaying.setBounds(176, 16, 357, 16);
		nowPlaying.setText("nowPlaying");
		nowPlaying.setFont(new Font("MS Sans Serif", Font.PLAIN, 13));
		cp.add(nowPlaying);
		
		chatArea.setBounds(8, 56, 529, 233);
		chatArea.setText("(Strings)");
		chatArea.setLineWrap(true);
		cp.add(chatArea);
		
		chatInput.setBounds(8, 296, 481, 24);
		chatInput.setText("");
		cp.add(chatInput);
		
		chatSendButton.setBounds(496, 296, 41, 25);
		chatSendButton.setText("send");
		chatSendButton.setMargin(new Insets(2, 2, 2, 2));
		chatSendButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				chatSendButton_ActionPerformed(evt);
			}
		});
		cp.add(chatSendButton);
		
		setResizable(false);
		setVisible(true);
		
		// Username erfragen
		do
		{
			username = JOptionPane.showInputDialog(cp, "Bitte gib deinen Username ein", "Username", JOptionPane.QUESTION_MESSAGE);
		}
		while(username != null && !username.isEmpty());
	}
	
	public void playButton_ActionPerformed(ActionEvent evt)
	{
		if(address == null)
		{
			makeConnection();
		}
		
		if(context.isClosed())
		{
			makeConnection();
		}
	}

	public void stopButton_ActionPerformed(ActionEvent evt)
	{
		context.closeSocket();
	}

	public void connectButton_ActionPerformed(ActionEvent evt)
	{
		makeConnection();
	}

	public void chatSendButton_ActionPerformed(ActionEvent evt)
	{
		TextMessage message = new TextMessage(username, chatInput.getText());
		try
		{
			context.sendChatMessage(message);
		}
		catch(IOException e)
		{
			addTextToChatArea("Nachricht nicht gesendet: Netzwerkfehler!");
		}
	}
	
	// ------------------
	
	public void pushChatMessage(String message)
	{
		addTextToChatArea(message);
	}
	
	public String getUserName()
	{
		return username;
	}
	
	public void close()
	{
		context.close();
	}
	
	// ------------------
	
	private void addTextToChatArea(String text)
	{
		chatArea.append(text+"\n");
	}
	
	private void makeConnection()
	{
		String url = JOptionPane.showInputDialog(cp, "Bitte Server-Addresse eingeben (ohne Port)", "Server-Addresse", JOptionPane.QUESTION_MESSAGE);
		
		int port = 0;
		do
		{
			try
			{
				port = Integer.parseInt(JOptionPane.showInputDialog(cp, "Bitte Server-Port eingeben", "Server-Port", JOptionPane.QUESTION_MESSAGE));
			}
			catch(NumberFormatException e)
			{
				port = 0;
			}
		}
		while(port > 0);
		
		this.address = new InetSocketAddress(url, port);
	}
}
