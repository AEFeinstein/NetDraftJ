package com.gelakinetic.NetDraftJ.Client;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.net.UnknownHostException;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.gelakinetic.NetDraftJ.Server.NetDraftJServer;
import com.gelakinetic.NetDraftJ.Server.NetDraftJServer_ui;

public class NetDraftJClient_ui {

    private NetDraftJClient mClient;

    private JFrame mFrame;
    private JPanel mPackGridLayout;
    private JTextArea mTextArea;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {

        EventQueue.invokeLater(new Runnable() {
            public void run() {

                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                        | UnsupportedLookAndFeelException e) {
                    e.printStackTrace();
                }

                try {
                    NetDraftJClient_ui window = new NetDraftJClient_ui();
                    window.mFrame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Create the application.
     * 
     * @param mClient
     */
    public NetDraftJClient_ui() {
        initialize();
        this.mClient = new NetDraftJClient(this);
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        mFrame = new JFrame();
        mFrame.setBounds(100, 100, 450, 300);
        mFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JMenuBar menuBar = new JMenuBar();
        mFrame.setJMenuBar(menuBar);

        JMenu mnFile = new JMenu("File");
        menuBar.add(mnFile);

        JMenuItem mntmConnect = new JMenuItem("Connect");
        mntmConnect.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                JTextField playerName = new JTextField();
                JTextField server = new JTextField();
                final JComponent[] inputs = new JComponent[] { new JLabel("Name"), playerName, new JLabel("Server"),
                        server };
                int result = JOptionPane.showConfirmDialog(null, inputs, "Connect to a Draft",
                        JOptionPane.PLAIN_MESSAGE);
                if (result == JOptionPane.OK_OPTION) {
                    mClient.connectToServer(playerName.getText(), server.getText());
                }
            }
        });
        mnFile.add(mntmConnect);

        JMenuItem mntmNewMenuItem = new JMenuItem("Host");
        mntmNewMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    new NetDraftJServer_ui(NetDraftJServer.getPublicIp());
                } catch (UnknownHostException e1) {
                    e1.printStackTrace();
                }
            }
        });
        mnFile.add(mntmNewMenuItem);

        mFrame.getContentPane().setLayout(new GridLayout(0, 1, 0, 0));

        JSplitPane splitPane = new JSplitPane();
        splitPane.setResizeWeight(0.85);
        mFrame.getContentPane().add(splitPane);

        mPackGridLayout = new JPanel();
        splitPane.setLeftComponent(mPackGridLayout);
        mPackGridLayout.setLayout(new GridLayout(3, 0, 0, 0));

        JPanel textAreaGridLayout = new JPanel();
        splitPane.setRightComponent(textAreaGridLayout);
        textAreaGridLayout.setLayout(new GridLayout(1, 0, 0, 0));

        mTextArea = new JTextArea();
        mTextArea.setEditable(false);
        mTextArea.setLineWrap(true);
        textAreaGridLayout.add(mTextArea);
    }

    public void appendText(String string) {
        mTextArea.append(string);
    }

    public Component getFrame() {
        return mFrame;
    }

    public void removeAllCards() {
        mPackGridLayout.removeAll();
    }

    public void removeCard(ImageLabel lblCard) {
        mPackGridLayout.remove(lblCard);
    }

    public void removeCardListeners() {
        synchronized (mPackGridLayout.getTreeLock()) {
            for (Component component : mPackGridLayout.getComponents()) {
                for (MouseListener ml : component.getMouseListeners()) {
                    component.removeMouseListener(ml);
                }
            }
        }
    }

    public void addCard(ImageLabel lblCard) {
        mPackGridLayout.add(lblCard);
    }

    void redrawCards() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                mPackGridLayout.repaint();
            }
        });
    }

    public void redrawCard(ImageLabel lblCard) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                lblCard.repaint();
            }
        });
    }
}
