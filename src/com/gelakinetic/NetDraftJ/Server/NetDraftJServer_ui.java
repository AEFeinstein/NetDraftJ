package com.gelakinetic.NetDraftJ.Server;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;

public class NetDraftJServer_ui {

    private NetDraftJServer mServer;

    private JFrame mFrame;
    private JTextPane mTextPane;
    private JButton btnStartTheGame;

    /**
     * Create the application.
     */
    public NetDraftJServer_ui(String ipAddress) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    initialize(ipAddress);
                    mServer = new NetDraftJServer(NetDraftJServer_ui.this);
                    mServer.startServer(ipAddress);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Initialize the contents of the frame.
     * 
     * @param ipAddress
     */
    private void initialize(String ipAddress) {
        mFrame = new JFrame();
        mFrame.setBounds(100, 100, 450, 300);
        mFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        mFrame.getContentPane().setLayout(new BoxLayout(mFrame.getContentPane(), BoxLayout.Y_AXIS));

        mTextPane = new JTextPane();
        mTextPane.setEditable(false);
        mFrame.getContentPane().add(mTextPane);
        mTextPane.setText("");

        btnStartTheGame = new JButton("Start the Game");
        btnStartTheGame.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        btnStartTheGame.setEnabled(false);
                        mServer.clickStartGameButton(e);
                    }
                });
            }
        });
        mFrame.getContentPane().add(btnStartTheGame);
        mFrame.setVisible(true);

        mFrame.addWindowListener(new WindowListener() {

            @Override
            public void windowOpened(WindowEvent e) {
            }

            @Override
            public void windowIconified(WindowEvent e) {
            }

            @Override
            public void windowDeiconified(WindowEvent e) {
            }

            @Override
            public void windowDeactivated(WindowEvent e) {
            }

            @Override
            public void windowClosing(WindowEvent e) {
            }

            @Override
            public void windowClosed(WindowEvent e) {
                mServer.stopServer();
            }

            @Override
            public void windowActivated(WindowEvent e) {
            }
        });
    }

    /**
     * TODO doc
     * 
     * @param text
     */
    public void appendText(String text) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                mTextPane.setText(mTextPane.getText() + '\n' + text);
            }
        });
    }

    /**
     * TODO doc TODO not invoked later
     * 
     * @return
     */
    public File pickCubeFile() {
        // Try to load the cube file
        final JFileChooser fc = new JFileChooser("./");
        if (JFileChooser.APPROVE_OPTION == fc.showOpenDialog(mFrame)) {
            return fc.getSelectedFile();
        }
        else {
            return null;
        }
    }

    /**
     * TODO doc
     * 
     * @param enabled
     */
    public void setButtonEnabled(boolean enabled) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                btnStartTheGame.setEnabled(enabled);
            }
        });
    }
}
