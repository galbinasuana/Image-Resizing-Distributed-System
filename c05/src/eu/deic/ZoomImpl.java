package eu.deic;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import javax.imageio.ImageIO;

public class ZoomImpl extends UnicastRemoteObject implements ZoomInterface {

	private static final long serialVersionUID = 1L;

	public ZoomImpl() throws RemoteException {
		super();
	}
	
	@Override
	public byte[] zoomImage(byte[] imageData, int zoomPercentage) throws RemoteException {

		try {
			// byte[] -> BufferedImage
			ByteArrayInputStream bais = new ByteArrayInputStream(imageData);
	        BufferedImage image = ImageIO.read(bais);
	        
	        int width = image.getWidth();
	        int height = image.getHeight();
	        
	        int newWidth = width * zoomPercentage / 100;
	        int newHeight = height * zoomPercentage / 100;
	        
	        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
	        Graphics2D g2d = resizedImage.createGraphics();
	        
	        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
	        g2d.drawImage(image, 0, 0, newWidth, newHeight, null);
	        g2d.dispose();
	        
	        File outputFile = new File("resizedImageSecondHalf.bmp");
            ImageIO.write(resizedImage, "bmp", outputFile);

	        
	        // byte[]
	        ByteArrayOutputStream baos = new ByteArrayOutputStream();
	        ImageIO.write(resizedImage, "bmp", baos);
	        
	        saveImageToFile(baos.toByteArray(), outputFile.getName());
	        
	        return baos.toByteArray();
		} catch (Exception e) {
            throw new RemoteException("Eroare la procesarea imaginii: " + e.getMessage(), e);
        }
	}
	
	private void saveImageToFile(byte[] imageData, String fileName) {
        try (FileOutputStream fos = new FileOutputStream("D:\\DAD\\" + fileName)) {
            fos.write(imageData);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            System.err.println("Error saving image: " + e.getMessage());
        }
    }

}
