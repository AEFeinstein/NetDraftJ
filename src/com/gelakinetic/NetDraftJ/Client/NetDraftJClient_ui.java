package com.gelakinetic.NetDraftJ.Client;

import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.jar.Manifest;

import javax.swing.BoxLayout;
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
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;
import javax.swing.text.PlainDocument;

import com.gelakinetic.NetDraftJ.Client.TextInputFilter.inputType;
import com.gelakinetic.NetDraftJ.Database.MtgCard;
import com.gelakinetic.NetDraftJ.Database.NetDraftJDatabase;
import com.gelakinetic.NetDraftJ.Server.NetDraftJServer;
import com.gelakinetic.NetDraftJ.Server.NetDraftJServer_ui;

import net.miginfocom.layout.AC;
import net.miginfocom.layout.LC;
import net.miginfocom.swing.MigLayout;

public class NetDraftJClient_ui {

    private static final ExecutorService threadPool = Executors
            .newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    private NetDraftJClient mClient;

    private JFrame mFrame;
    private JPanel mPackGridLayout;
    private JPanel mTextBoxLayout;

    private JMenuItem mntmHost;
    private JMenuItem mntmConnect;

    private static boolean mHasStaticUuid;
    private static long mStaticUuid;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {

        if (args.length > 0) {
            try {
                mStaticUuid = Long.parseLong(args[0]);
                mHasStaticUuid = true;
            } catch (Exception e) {
                mHasStaticUuid = false;
            }
        }

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

        mntmConnect = new JMenuItem("Connect");
        mntmConnect.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        JTextField playerName = new JTextField();
                        ((PlainDocument) playerName.getDocument())
                                .setDocumentFilter(new TextInputFilter(inputType.USERNAME));
                        JTextField server = new JTextField();
                        ((PlainDocument) server.getDocument())
                                .setDocumentFilter(new TextInputFilter(inputType.IP_ADDRESS));
                        final JComponent[] inputs = new JComponent[] { new JLabel("Username"), playerName,
                                new JLabel("Server IP Address"), server };
                        int result = JOptionPane.showConfirmDialog(null, inputs, "Connect to a Draft",
                                JOptionPane.PLAIN_MESSAGE);
                        if (result == JOptionPane.OK_OPTION) {
                            mClient.connectToServer(playerName.getText(), server.getText());
                        }
                    }
                });
            }
        });
        mnFile.add(mntmConnect);

        mntmHost = new JMenuItem("Host");
        mntmHost.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    new NetDraftJServer_ui(NetDraftJServer.getPublicIp(), NetDraftJClient_ui.this);
                } catch (UnknownHostException e1) {
                    e1.printStackTrace();
                }
            }
        });
        mnFile.add(mntmHost);

        JMenuItem mntmSaveDraftedCards = new JMenuItem("Save Drafted Cards");
        mntmSaveDraftedCards.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                mClient.saveDraftedCards();
            }
        });
        mnFile.add(mntmSaveDraftedCards);

        JMenuItem mnDate = new JMenuItem("Built On " + getClassBuildTime(this).toString());
        mnFile.add(mnDate);

        JMenuItem mnExit = new JMenuItem("Exit");
        mnExit.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                showExitDialog(true);
            }
        });
        mnFile.add(mnExit);

        mFrame.getContentPane().setLayout(new GridLayout(0, 1, 0, 0));

        JSplitPane splitPane = new JSplitPane();
        splitPane.setResizeWeight(0.85);
        mFrame.getContentPane().add(splitPane);

        mPackGridLayout = new JPanel();
        splitPane.setLeftComponent(mPackGridLayout);
        mPackGridLayout.setLayout(new GridLayout(3, 0, 0, 0));

        JPanel textAreaGridLayout = new JPanel();
        splitPane.setRightComponent(textAreaGridLayout);
        textAreaGridLayout.setLayout(new BoxLayout(textAreaGridLayout, BoxLayout.Y_AXIS));

        JScrollPane scrollPane = new JScrollPane();
        textAreaGridLayout.add(scrollPane);

        mTextBoxLayout = new JPanel();
        mTextBoxLayout.setBorder(new EmptyBorder(0, 8, 0, 8));
        scrollPane.setViewportView(mTextBoxLayout);

        // Layout, Column and Row constraints as arguments.
        MigLayout migLayout = new MigLayout(new LC().fillX().wrapAfter(1), new AC().count(1).align("left").fill(),
                new AC());

        mTextBoxLayout.setLayout(migLayout);

        mFrame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                showExitDialog(false);
            }
        });

        mFrame.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getClassLoader().getResource("icon.png")));
        mFrame.setTitle("NetDraftJ");

        mFrame.setExtendedState(mFrame.getExtendedState() | JFrame.MAXIMIZED_BOTH);

        // Random rand = new Random(System.currentTimeMillis());
        // int testPack[] = new int[15];
        // for(int i = 0; i < testPack.length; i++) {
        // testPack[i] = rand.nextInt(1000) + 1;
        // }
        // loadPack(testPack);

        // appendText("WHITE", "", "W");
        // appendText("BLUE", "", "U");
        // appendText("BLACK", "", "B");
        // appendText("RED", "", "R");
        // appendText("GREEN", "", "G");
        // appendText("YELLOW", "", "WU");
        // appendText("NONE", "", "CAL");
    }

    /**
     * TODO doc
     */
    private void showExitDialog(boolean isMenu) {
        int confirmed = JOptionPane.showConfirmDialog(null, "Sure you want to exit?", "Leaving So Soon?",
                JOptionPane.YES_NO_OPTION);

        if (confirmed == JOptionPane.YES_OPTION) {
            mFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            if (isMenu) {
                mFrame.dispose();
            }
        }
        else {
            mFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        }
    }

    /**
     * TODO doc
     * 
     * @param string
     */
    public void appendText(String string) {
        appendText(string, null, null);
    }

    /**
     * TODO doc
     * 
     * @param string
     * @param tooltipText
     */
    public void appendText(String string, String tooltipText, String colorStr) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JLabel label = new JLabel(string);
                // Set the label's font size to the newly determined size.
                label.setFont(new Font(label.getFont().getName(), Font.PLAIN, 16));
                if (null != tooltipText) {
                    label.setToolTipText(tooltipText);
                }
                if (null != colorStr) {
                    Color bgColor = null;

                    String colorStrFilt = colorStr.replaceAll("[^WUBRGwubrg]", "").toLowerCase();
                    if (colorStrFilt.length() == 1) {
                        switch (colorStrFilt.charAt(0)) {
                            case 'w': {
                                bgColor = Color.WHITE;
                                break;
                            }
                            case 'u': {
                                bgColor = Color.BLUE;
                                break;
                            }
                            case 'b': {
                                bgColor = Color.BLACK;
                                break;
                            }
                            case 'r': {
                                bgColor = Color.RED;
                                break;
                            }
                            case 'g': {
                                bgColor = Color.GREEN.darker();
                                break;
                            }
                        }
                    }
                    else if (colorStrFilt.length() > 1) {
                        bgColor = Color.YELLOW;
                    }
                    else {
                        bgColor = null;
                    }

                    if (null != bgColor) {
                        label.setOpaque(true);
                        label.setBackground(bgColor);

                        float greyscale = (0.3f * bgColor.getRed()) + (0.59f * bgColor.getGreen())
                                + (0.11f * bgColor.getBlue());
                        if (greyscale > 128) {
                            label.setForeground(Color.BLACK);
                        }
                        else {
                            label.setForeground(Color.WHITE);
                        }
                    }
                }

                mTextBoxLayout.add(label);
                mTextBoxLayout.repaint();
                mTextBoxLayout.validate();
            }
        });
    }

    /**
     * TODO doc TODO not invoked later
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

        // First create all of the ImageLabels
        ArrayList<ImageLabel> labels = new ArrayList<ImageLabel>(pack.length);
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
                            mClient.pickCard(card);
                            appendText(mClient.getPickedCardCount() + ":  " + card.getName(), card.getToolTipText(),
                                    card.getColor());
                            break;
                        }
                        default:
                        case JOptionPane.NO_OPTION: {
                            break;
                        }
                    }
                    mPackGridLayout.repaint();
                    mPackGridLayout.validate();
                }
            });

            labels.add(lblCard);

            // Queue up a bunch of threads to download the images
            threadPool.submit(new Runnable() {

                @Override
                public void run() {
                    NetDraftJDatabase database = new NetDraftJDatabase();
                    try {
                        if (database.loadCard(card)) {
                            String filename = card.downloadImage();
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    lblCard.setIcon(new ImageIcon(filename));
                                    lblCard.setHorizontalAlignment(SwingConstants.CENTER);
                                    lblCard.setVerticalAlignment(SwingConstants.CENTER);
                                    lblCard.setToolTipText(card.getToolTipText());
                                    lblCard.repaint();
                                    lblCard.validate();
                                }
                            });
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    database.closeConnection();

                }
            });
        }

        // Then add all the labels to the UI
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                // First clear out the grid
                mPackGridLayout.removeAll();
                // Then add all the cards
                for (ImageLabel label : labels) {
                    mPackGridLayout.add(label);
                }
                // Then repaint the UI
                mPackGridLayout.repaint();
                mPackGridLayout.validate();
            }

        });
    }

    /**
     * TODO doc
     * 
     * @param enabled
     */
    public void setHostMenuItemEnabled(boolean enabled) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                mntmHost.setEnabled(enabled);
            }
        });
    }

    /**
     * TODO doc
     * 
     * @param enabled
     */
    void setConnectMenuItemEnabled(boolean enabled) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                mntmConnect.setEnabled(enabled);
            }
        });
    }

    /**
     * @param obj
     * @return The date this class file was built
     */
    public static Date getClassBuildTime(Object obj) {
        try {
            Enumeration<URL> resources = obj.getClass().getClassLoader().getResources("META-INF/MANIFEST.MF");
            while (resources.hasMoreElements()) {
                try {
                    // Open a manifest, check if it has the Built-Date
                    // If it throws an exception, the while loop will continue executing
                    Manifest manifest = new Manifest(resources.nextElement().openStream());
                    String buildDate = manifest.getMainAttributes().getValue("Built-Date");
                    return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(buildDate);
                } catch (IOException | ParseException | NullPointerException E) {
                    // Some error, keep looking through manifests
                }
            }
        } catch (IOException E) {
            // Couldn't read resources, probably a debug build
        }
        return new Date(0);
    }

    /**
     * TODO doc
     * 
     * @return
     */
    public boolean hasStaticUuid() {
        return mHasStaticUuid;
    }

    /**
     * TODO doc
     * 
     * @return
     */
    public long getUuid() {
        return mStaticUuid;
    }

    /**
     * TODO doc
     * 
     * @param message
     */
    public void showErrorDialog(String message) {
        JOptionPane.showMessageDialog(mFrame, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
