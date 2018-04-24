package com.gelakinetic.NetDraftJ.Client;

import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
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
import javax.swing.WindowConstants;
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

    private final NetDraftJClient mClient;

    private JFrame mFrame;
    private JPanel mPackGridLayout;
    private JPanel mTextBoxLayout;

    private JMenuItem menuBtnHost;
    private JMenuItem menuBtnConnect;

    private static boolean mHasAssignedUuid;
    private static long mAssignedUuid;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {

        if (args.length > 0) {
            try {
                mAssignedUuid = Long.parseLong(args[0]);
                mHasAssignedUuid = true;
            } catch (Exception e) {
                mHasAssignedUuid = false;
            }
        }

        EventQueue.invokeLater(() -> {

            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                    | UnsupportedLookAndFeelException e1) {
                e1.printStackTrace();
            }

            // Show tooltips virtually forever
            ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);

            try {
                NetDraftJClient_ui window = new NetDraftJClient_ui();
                window.mFrame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Create the application.
     */
    private NetDraftJClient_ui() {
        initialize();
        this.mClient = new NetDraftJClient(this);
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        mFrame = new JFrame();
        mFrame.setBounds(100, 100, 1024, 768);
        mFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        JMenuBar menuBar = new JMenuBar();
        mFrame.setJMenuBar(menuBar);

        JMenu mnFile = new JMenu("File");
        menuBar.add(mnFile);

        menuBtnConnect = new JMenuItem("Connect");
        menuBtnConnect.addActionListener(e -> SwingUtilities.invokeLater(() -> {
            JTextField playerName = new JTextField();
            ((PlainDocument) playerName.getDocument()).setDocumentFilter(new TextInputFilter(inputType.USERNAME));
            JTextField server = new JTextField();
            ((PlainDocument) server.getDocument()).setDocumentFilter(new TextInputFilter(inputType.IP_ADDRESS));
            final JComponent[] inputs = new JComponent[] { new JLabel("Username"), playerName,
                    new JLabel("Server IP Address"), server };
            int result = JOptionPane.showConfirmDialog(null, inputs, "Connect to a Draft",
                    JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                mClient.connectToServer(playerName.getText(), server.getText());
            }
        }));
        mnFile.add(menuBtnConnect);

        menuBtnHost = new JMenuItem("Host");
        menuBtnHost
                .addActionListener(e -> new NetDraftJServer_ui(NetDraftJServer.getPublicIp(), NetDraftJClient_ui.this));
        mnFile.add(menuBtnHost);

        JMenuItem menuBtnSaveDraftedCards = new JMenuItem("Save Drafted Cards");
        menuBtnSaveDraftedCards.addActionListener(e -> mClient.saveDraftedCards());
        mnFile.add(menuBtnSaveDraftedCards);

        JMenuItem mnDate = new JMenuItem("Built On " + getClassBuildTime(this));
        mnFile.add(mnDate);

        JMenuItem mnExit = new JMenuItem("Exit");
        mnExit.addActionListener(e -> showExitDialog(true));
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
     * Show a dialog asking the user if they really want to exit
     *
     * @param isMenu
     *            true if the dialog is shown from the menu, false if the user presses the X button
     */
    private void showExitDialog(boolean isMenu) {
        int confirmed = JOptionPane.showConfirmDialog(null, "Sure you want to exit?", "Leaving So Soon?",
                JOptionPane.YES_NO_OPTION);

        if (confirmed == JOptionPane.YES_OPTION) {
            mFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            if (isMenu) {
                mFrame.dispose();
            }
        }
        else {
            mFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        }
    }

    /**
     * Add a String to the text display on the right
     * 
     * @param string
     *            The String to display, may be HTML
     */
    void appendText(String string) {
        appendText(string, null, null);
    }

    /**
     * Add a String to the text display on the right with a colored background and tooltip text
     * 
     * @param string
     *            The String to display, may be HTML
     * @param tooltipText
     *            The tooltip text to display, may be HTML
     * @param colorStr
     *            The color string of the card displayed consisting of the chars w, u, b, r, and g
     */
    void appendText(String string, String tooltipText, String colorStr) {
        SwingUtilities.invokeLater(() -> {
            JLabel label = new JLabel(string);
            // Set the label's font size to the newly determined size.
            label.setFont(new Font(label.getFont().getName(), Font.PLAIN, 16));
            if (null != tooltipText) {
                label.setToolTipText(tooltipText);
            }
            if (null != colorStr) {
                Color bgColor = null;

                String colorStrFiltered = colorStr.replaceAll("[^WUBRGwubrg]", "").toLowerCase();
                if (colorStrFiltered.length() == 1) {
                    switch (colorStrFiltered.charAt(0)) {
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
                else if (colorStrFiltered.length() > 1) {
                    bgColor = Color.YELLOW;
                }

                if (null != bgColor) {
                    label.setOpaque(true);
                    label.setBackground(bgColor);

                    float greyScale = (0.3f * bgColor.getRed()) + (0.59f * bgColor.getGreen())
                            + (0.11f * bgColor.getBlue());
                    if (greyScale > 128) {
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
        });
    }

    /**
     * Pop a dialog to ask the user where to save a file
     * 
     * @return The File to save data to
     */
    File getSaveFile() {
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
     * Load a pack's images when the server sends a pack to the client
     * 
     * @param pack
     *            An array of multiverse IDs for a given pack
     */
    void loadPack(int[] pack) {

        // First create all of the ImageLabels
        ArrayList<ImageLabel> labels = new ArrayList<>(pack.length);
        for (int cardMultiverseId : pack) {
            final MtgCard card = new MtgCard(cardMultiverseId);

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
            threadPool.submit(() -> {
                NetDraftJDatabase database = new NetDraftJDatabase();
                try {
                    if (database.loadCard(card)) {
                        String filename = card.downloadImage();
                        SwingUtilities.invokeLater(() -> {
                            lblCard.setIcon(new ImageIcon(filename));
                            lblCard.setHorizontalAlignment(SwingConstants.CENTER);
                            lblCard.setVerticalAlignment(SwingConstants.CENTER);
                            lblCard.setToolTipText(card.getToolTipText());
                            lblCard.repaint();
                            lblCard.validate();
                        });
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                database.closeConnection();

            });
        }

        // Then add all the labels to the UI
        SwingUtilities.invokeLater(() -> {
            // First clear out the grid
            mPackGridLayout.removeAll();
            // Then add all the cards
            for (ImageLabel label : labels) {
                mPackGridLayout.add(label);
            }
            // Then repaint the UI
            mPackGridLayout.repaint();
            mPackGridLayout.validate();
        });
    }

    /**
     * Enable or disable the "Host" button in the menu
     * 
     * @param enabled
     *            true to enable the "Host" button, false to disable it
     */
    public void setHostMenuItemEnabled(boolean enabled) {
        SwingUtilities.invokeLater(() -> menuBtnHost.setEnabled(enabled));
    }

    /**
     * Enable or disable the "Connect" button in the menu
     * 
     * @param enabled
     *            true to enable the "Connect" button, false to disable it
     */
    void setConnectMenuItemEnabled(boolean enabled) {
        SwingUtilities.invokeLater(() -> menuBtnConnect.setEnabled(enabled));
    }

    /**
     * Get the date this JAR was built from the manifest file
     *
     * @param obj
     *            An object to get resources through, can be anything
     * @return The date this JAR was built, or the epoch if this isn't running from a JAR
     */
    public static String getClassBuildTime(Object obj) {
        try {
            Enumeration<URL> resources = obj.getClass().getClassLoader().getResources("META-INF/MANIFEST.MF");
            while (resources.hasMoreElements()) {
                try {
                    // Open a manifest, check if it has the Built-Date
                    // If it throws an exception, the while loop will continue executing
                    Manifest manifest = new Manifest(resources.nextElement().openStream());
                    String time = manifest.getMainAttributes().getValue("Built-Date");
                    if(null == time) {
                        throw new NullPointerException("debug build");
                    }
                    return time;
                } catch (IOException | NullPointerException E) {
                    // Some error, keep looking through manifests
                }
            }
        } catch (IOException E) {
            // Couldn't read resources, probably a debug build
        }
        return "Today";
    }

    /**
     * @return True if this launch has a UUID assigned from the command line arguments, false otherwise
     */
    boolean hasAssignedUuid() {
        return mHasAssignedUuid;
    }

    /**
     * @return The assigned UUID from the command line arguments
     */
    long getAssignedUuid() {
        return mAssignedUuid;
    }

    /**
     * Show an error dialog with the given message
     * 
     * @param message
     *            The message to display in the error dialog
     */
    public void showErrorDialog(String message) {
        JOptionPane.showMessageDialog(mFrame, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
