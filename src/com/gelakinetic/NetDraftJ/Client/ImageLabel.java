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
	private BufferedImage _myimage;

	public ImageLabel(String text) {
		super(text);
	}

	public ImageLabel() {
		super();
	}

	public void setIcon(Icon icon) {
		super.setIcon(icon);
		if (icon instanceof ImageIcon) {
			_myimage = toBufferedImage(((ImageIcon) icon).getImage());
		}
	}

	@Override
	public void paint(Graphics graphics) {

		int newWidth = 0;
		int newHeight = 0;
		int xOffset = 0;
		int yOffset = 0;

		if (null != _myimage) {
			int viewHeight = this.getHeight();
			int viewWidth = this.getWidth();
			int imageHeight = _myimage.getHeight(this);
			int imageWidth = _myimage.getWidth(this);
			
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
				
				BufferedImage scaledImage = Scalr.resize(_myimage, Method.ULTRA_QUALITY, newWidth, newHeight);
				
				graphics.drawImage(scaledImage, xOffset, yOffset, newWidth, newHeight, this);
				return;
			}
		}
		graphics.drawImage(_myimage, xOffset, yOffset, newWidth, newHeight, this);
	}
	
	/**
	 * Converts a given Image into a BufferedImage
	 *
	 * @param img The Image to be converted
	 * @return The converted BufferedImage
	 */
	public static BufferedImage toBufferedImage(Image img)
	{
	    if (img instanceof BufferedImage)
	    {
	        return (BufferedImage) img;
	    }

	    // Create a buffered image with transparency
	    BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

	    // Draw the image on to the buffered image
	    Graphics2D bGr = bimage.createGraphics();
	    bGr.drawImage(img, 0, 0, null);
	    bGr.dispose();

	    // Return the buffered image
	    return bimage;
	}
}