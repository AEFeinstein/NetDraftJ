package com.gelakinetic.NetDraftJ.Client;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
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
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.gelakinetic.NetDraftJ.Database.MtgCard;
import com.gelakinetic.NetDraftJ.Database.NetDraftJDatabase;
import com.gelakinetic.NetDraftJ.Server.NetDraftJServer;
import com.gelakinetic.NetDraftJ.Server.NetDraftJServer_ui;

public class NetDraftJClient_ui {

    // TODO ensure swing ops are invoked later

    private static final ExecutorService threadPool = Executors.newFixedThreadPool(1);

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

                // Show tooltips virtually forever
                ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);

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
        mFrame.setBounds(100, 100, 1024, 768);
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

    /**
     * TODO doc
     * 
     * @param string
     */
    public void appendText(String string) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                mTextArea.append(string);
            }
        });
    }

    /**
     * TODO doc
     * 
     * @return
     */
    public File getSaveFile() {
        JFileChooser fc = new JFileChooser("./");
        fc.setDialogTitle("Save Drafted Cards");
        if (JFileChooser.APPROVE_OPTION == fc.showSaveDialog(mFrame)) {
            return fc.getSelectedFile();
        }
        else {
            return null;
        }
    }

    /**
     * TODO doc
     * 
     * @param packGridLayout
     * @param textArea
     * @param pack
     */
    void loadPack(int[] pack) {

        // First clear out the grid
        mPackGridLayout.removeAll();

        for (int i = 0; i < pack.length; i++) {
            final MtgCard card = new MtgCard(pack[i]);

            ImageLabel lblCard = new ImageLabel();
            lblCard.setHorizontalAlignment(SwingConstants.CENTER);
            lblCard.setVerticalAlignment(SwingConstants.CENTER);

            lblCard.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent arg0) {

                    // Custom button text
                    Object[] options = { "Yes, please", "No, thanks" };
                    int choice = JOptionPane.showOptionDialog(mFrame, "Sure you want to draft " + card.getName() + "?",
                            "Double Checking", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options,
                            options[1]);

                    switch (choice) {
                        case JOptionPane.YES_OPTION: {
                            mPackGridLayout.remove(lblCard);
                            synchronized (mPackGridLayout.getTreeLock()) {
                                for (Component component : mPackGridLayout.getComponents()) {
                                    for (MouseListener ml : component.getMouseListeners()) {
                                        component.removeMouseListener(ml);
                                    }
                                }
                            }
                            mPackGridLayout.repaint();
                            appendText("1 " + card.getName() + '\n');
                            mClient.pickCard(card);
                            break;
                        }
                        default:
                        case JOptionPane.NO_OPTION: {
                            break;
                        }
                    }
                }
            });

            mPackGridLayout.add(lblCard);

            threadPool.submit(new Runnable() {

                @Override
                public void run() {
                    NetDraftJDatabase database = new NetDraftJDatabase();
                    try {
                        database.loadCard(card);
                        String filename = card.downloadImage();
                        lblCard.setIcon(new ImageIcon(filename));
                        lblCard.setHorizontalAlignment(SwingConstants.CENTER);
                        lblCard.setVerticalAlignment(SwingConstants.CENTER);
                        lblCard.setToolTipText(card.getToolTipText());
                        lblCard.repaint();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    database.closeConnection();

                }
            });

        }
    }
}
