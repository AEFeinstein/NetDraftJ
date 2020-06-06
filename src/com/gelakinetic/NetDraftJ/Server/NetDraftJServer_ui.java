package com.gelakinetic.NetDraftJ.Server;

import java.awt.EventQueue;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import com.gelakinetic.NetDraftJ.Client.NetDraftJClient_ui;

public class NetDraftJServer_ui {

    private NetDraftJServer          mServer;

    private JFrame                   mFrame;
    private JTextPane                mTextPane;
    private JButton                  mBtnStartTheGame;

    private final NetDraftJClient_ui mClientUi;

    /**
     * Create the application.
     *
     */
    public NetDraftJServer_ui(String ipAddress, NetDraftJClient_ui clientUi) {
        mClientUi = clientUi;
        EventQueue.invokeLater(() -> {
            try {
                initialize();
                mServer = new NetDraftJServer(NetDraftJServer_ui.this);
                mServer.startServer(ipAddress);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Initialize the contents of the frame.
     *
     */
    private void initialize() {
        mFrame = new JFrame();
        mFrame.setBounds(100, 100, 450, 300);
        mFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        mFrame.getContentPane().setLayout(new BoxLayout(mFrame.getContentPane(), BoxLayout.Y_AXIS));

        JScrollPane scrollPane = new JScrollPane();
        mFrame.getContentPane().add(scrollPane);

        mTextPane = new JTextPane();
        scrollPane.setViewportView(mTextPane);
        mTextPane.setEditable(false);
        mTextPane.setText("");

        mBtnStartTheGame = new JButton("Start the Game");
        mBtnStartTheGame.addActionListener(e -> SwingUtilities.invokeLater(() -> {
            if (mServer.clickStartGameButton()) {
                mBtnStartTheGame.setEnabled(false);
            }
        }));
        mFrame.getContentPane().add(mBtnStartTheGame);
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
                showExitDialog();
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
     * Pop a dialog asking the users if they really want to close the server
     */
    private void showExitDialog() {
        int confirmed = JOptionPane.showConfirmDialog(null, "Sure you want to close the server?", "Close the Server",
                JOptionPane.YES_NO_OPTION);

        if (confirmed == JOptionPane.YES_OPTION) {
            mFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        }
        else {
            mFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        }
    }

    /**
     * Add text to the server text output window
     * 
     * @param text The text to display
     */
    void appendText(String text) {
        SwingUtilities.invokeLater(() -> mTextPane.setText(mTextPane.getText() + '\n' + text));
    }

    /**
     * Pop a dialog to ask the user what cube file to load
     * 
     * @return The File to load the cube from
     */
    File pickCubeFile() {
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
     * Enable or disable the "Start the draft" button
     * 
     * @param enabled true to enable the button, false to disable it
     */
    void setStartButtonEnabled(boolean enabled) {
        SwingUtilities.invokeLater(() -> mBtnStartTheGame.setEnabled(enabled));
    }

    /**
     * Enable or disable the "Host" button in the client UI
     * 
     * @param enabled true to enable the button, false to disable it
     */
    void setHostMenuItemEnabled(boolean enabled) {
        mClientUi.setHostMenuItemEnabled(enabled);
    }
}
