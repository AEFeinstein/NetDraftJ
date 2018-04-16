package com.gelakinetic.NetDraftJ.Server;

import java.awt.EventQueue;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;

import javax.swing.*;

import com.gelakinetic.NetDraftJ.Client.NetDraftJClient_ui;

public class NetDraftJServer_ui {

    private NetDraftJServer mServer;

    private JFrame mFrame;
    private JTextPane mTextPane;
    private JButton btnStartTheGame;

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
            } catch (Exception e) {
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

        btnStartTheGame = new JButton("Start the Game");
        btnStartTheGame.addActionListener(e -> SwingUtilities.invokeLater(() -> {
            if (mServer.clickStartGameButton()) {
                btnStartTheGame.setEnabled(false);
            }
        }));
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
     * TODO doc
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
     * TODO doc
     * 
     * @param text
     */
    void appendText(String text) {
        SwingUtilities.invokeLater(() -> mTextPane.setText(mTextPane.getText() + '\n' + text));
    }

    /**
     * TODO doc TODO not invoked later
     * 
     * @return
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
     * TODO doc
     * 
     * @param enabled
     */
    void setButtonEnabled(boolean enabled) {
        SwingUtilities.invokeLater(() -> btnStartTheGame.setEnabled(enabled));
    }

    /**
     * TODO doc
     * 
     * @param enabled
     */
    void setHostMenuItemEnabled(boolean enabled) {
        mClientUi.setHostMenuItemEnabled(enabled);
    }
}
