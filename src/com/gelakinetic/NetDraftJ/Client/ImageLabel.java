package com.gelakinetic.NetDraftJ.Client;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Method;

class ImageLabel extends JLabel {
    private static final long serialVersionUID = -2929247308012414202L;
    private BufferedImage mImage;

    /**
     * Creates a JLabel instance with no image and with an empty string for the title. The label is centered vertically
     * in its display area. The label's contents, once set, will be displayed on the leading edge of the label's
     * display area.
     */
    ImageLabel() {
        super();
    }

    /**
     * Defines the icon this component will display. If the value of icon is null, nothing is displayed.
     * The default value of this property is null.
     *
     * @param icon the default icon this component will display
     */
    public void setIcon(Icon icon) {
        super.setIcon(icon);
        if (icon instanceof ImageIcon) {
            mImage = toBufferedImage(((ImageIcon) icon).getImage());
        }
    }

    /**
     * Invoked by Swing to draw components. Applications should not invoke paint directly, but should instead use the
     * repaint method to schedule the component for redrawing.
     * <p>
     * This method actually delegates the work of painting to three protected methods: paintComponent, paintBorder, and
     * paintChildren. They're called in the order listed to ensure that children appear on top of component itself.
     * Generally speaking, the component and its children should not paint in the insets area allocated to the border.
     * Subclasses can just override this method, as always. A subclass that just wants to specialize the UI (look and
     * feel) delegate's paint method should just override paintComponent.
     *
     * @param graphics the Graphics context in which to paint
     */
    @Override
    public void paint(Graphics graphics) {
        int newWidth = 0;
        int newHeight = 0;
        int xOffset = 0;
        int yOffset = 0;

        if (null != mImage) {
            int viewHeight = this.getHeight();
            int viewWidth = this.getWidth();
            int imageHeight = mImage.getHeight(this);
            int imageWidth = mImage.getWidth(this);

            if (imageHeight != -1 && imageWidth != -1) {

                float screenAspectRatio = (float) viewHeight / (float) (viewWidth);
                float cardAspectRatio = (float) imageHeight / (float) imageWidth;

                float scale;
                if (screenAspectRatio > cardAspectRatio) {
                    scale = (viewWidth) / (float) imageWidth;
                }
                else {
                    scale = (viewHeight) / (float) imageHeight;
                }

                newWidth = Math.round(imageWidth * scale);
                newHeight = Math.round(imageHeight * scale);

                xOffset = (viewWidth - newWidth) / 2;
                yOffset = (viewHeight - newHeight) / 2;

                BufferedImage scaledImage = Scalr.resize(mImage, Method.ULTRA_QUALITY, newWidth, newHeight);

                graphics.drawImage(scaledImage, xOffset, yOffset, newWidth, newHeight, this);
                return;
            }
        }
        graphics.drawImage(mImage, xOffset, yOffset, newWidth, newHeight, this);
    }

    /**
     * Converts a given Image into a BufferedImage
     *
     * @param img
     *            The Image to be converted
     * @return The converted BufferedImage
     */
    private static BufferedImage toBufferedImage(Image img) {
        if (img instanceof BufferedImage) {
            return (BufferedImage) img;
        }

        // Create a buffered image with transparency
        BufferedImage bImage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

        // Draw the image on to the buffered image
        Graphics2D bGr = bImage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();

        // Return the buffered image
        return bImage;
    }
}