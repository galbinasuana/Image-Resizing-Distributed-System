package eu.deic;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.sql.Blob;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.imageio.ImageIO;
import javax.sql.rowset.serial.SerialBlob;

import jakarta.jms.Connection;
import jakarta.jms.JMSException;

public class ProgMainClient {

	public ProgMainClient() {

	}

	@SuppressWarnings("deprecation")
	public void processImage(byte[] imageData, int zoomPercentage) throws IOException, RemoteException, JMSException {
        try {
        	BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(imageData));
        	saveImageToFile(imageData, "originalImage.bmp");
        	
        	int width = originalImage.getWidth();
            int height = originalImage.getHeight();
            BufferedImage firstHalf = originalImage.getSubimage(0, 0, width / 2, height);
            BufferedImage secondHalf = originalImage.getSubimage(width / 2, 0, width / 2, height);
            
            byte[] firstHalfBytes = convertImageToByteArray(firstHalf);
            byte[] secondHalfBytes = convertImageToByteArray(secondHalf);
            
            
            saveImageToFile(firstHalfBytes, "firstHalf.bmp");
            saveImageToFile(secondHalfBytes, "secondHalf.bmp");
            
            // client -> server
            String url1 = "rmi://172.17.0.5:1087/ZOOM-IMAGE-S1";
            String url2 = "rmi://172.17.0.6:1088/ZOOM-IMAGE-S2";
            ZoomInterface server1 = (ZoomInterface) Naming.lookup(url1);
            ZoomInterface server2 = (ZoomInterface) Naming.lookup(url2);

            byte[] processedFirstHalf = server1.zoomImage(firstHalfBytes, zoomPercentage);
            byte[] processedSecondHalf = server2.zoomImage(secondHalfBytes, zoomPercentage);

            
            // server -> client
            BufferedImage firstHalfImage = convertByteArrayToImage(processedFirstHalf);
            BufferedImage secondHalfImage = convertByteArrayToImage(processedSecondHalf);

            byte[] combinedImage = combineImageHalves(processedFirstHalf, processedSecondHalf);
            
            saveImageToFile(combinedImage, "combinedResized.bmp");
            
            Blob blobImage = null;
            
            try {
                blobImage = new SerialBlob(combinedImage);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            
            int insert = insertInDB(blobImage);
            if (insert != 0) {
                throw new RuntimeException("EROARE.");
            }
            
            
        } catch (RemoteException exc) {
	          System.out.println("Error in lookup: " + exc.toString());
	    } catch (java.net.MalformedURLException exc) {
	          System.out.println("Malformed URL: " + exc.toString());
	    } catch (java.rmi.NotBoundException exc) {
	          System.out.println("NotBound: " + exc.toString());
	    }
    }

	private byte[] convertImageToByteArray(BufferedImage image) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "bmp", baos);
        return baos.toByteArray();
    }
	
	private void saveImageToFile(byte[] imageData, String fileName) {
        try (FileOutputStream fos = new FileOutputStream("/home/media/" + fileName)) {
            fos.write(imageData);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            System.err.println("Eroare la salvarea imaginii: " + e.getMessage());
        }
        
    }
	
	private BufferedImage convertByteArrayToImage(byte[] imageBytes) throws IOException {
	    ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes);
	    return ImageIO.read(bais);
	}

	
	private byte[] combineImageHalves(byte[] firstHalf, byte[] secondHalf) throws IOException {
	    BufferedImage firstHalfImage = ImageIO.read(new ByteArrayInputStream(firstHalf));
	    BufferedImage secondHalfImage = ImageIO.read(new ByteArrayInputStream(secondHalf));

	    int width = firstHalfImage.getWidth() + secondHalfImage.getWidth();
	    int height = Math.max(firstHalfImage.getHeight(), secondHalfImage.getHeight());

	    BufferedImage combinedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	    Graphics2D g2d = combinedImage.createGraphics();
	    g2d.drawImage(firstHalfImage, 0, 0, null);
	    g2d.drawImage(secondHalfImage, firstHalfImage.getWidth(), 0, null);
	    g2d.dispose();

	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    ImageIO.write(combinedImage, "bmp", baos);
	    return baos.toByteArray();
	}
	
	private int insertInDB(Blob image) throws JMSException {
        int result = 0;
        String mySQLUrl = "jdbc:mysql://172.17.0.2:3306/resizedImage?" + "user=mysqluser&password=mysqluserpwd";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException exc) {
            exc.printStackTrace();
            return 1;
        }

        try (Connection db_con = (Connection) DriverManager.getConnection(mySQLUrl);
             PreparedStatement stmt = ((java.sql.Connection) db_con).prepareStatement("INSERT INTO resizedImage.Imgs(photo) VALUES (?)")) {

            stmt.setBlob(1, image);
            stmt.executeUpdate();

        } catch (SQLException e1) {
            e1.printStackTrace();
            return 2;
        }
        return result;
    }
	
}
