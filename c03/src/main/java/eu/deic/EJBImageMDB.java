package eu.deic;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Base64;

import jakarta.ejb.ActivationConfigProperty;
import jakarta.ejb.MessageDriven;
import jakarta.jms.BytesMessage;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;

/**
 * Message-Driven Bean implementation class for: EJBImageMDB
 */
@MessageDriven(
		activationConfig = { @ActivationConfigProperty(propertyName = "destination", propertyValue = "jms/topic/image"),
				@ActivationConfigProperty(propertyName = "destinationType", propertyValue = "jakarta.jms.Topic")
		}, 
		messageListenerInterface = MessageListener.class)
public class EJBImageMDB implements MessageListener {

    /**
     * Default constructor. 
     */
    public EJBImageMDB() {
        // TODO Auto-generated constructor stub
    }
	
	/**
     * @see MessageListener#onMessage(Message)
     */
    public void onMessage(Message message) {
            try {
                System.out.println("Received BytesMessage");

                BytesMessage bytesMessage = (BytesMessage) message;
                byte[] imageBytes = new byte[(int) bytesMessage.getBodyLength()];
                bytesMessage.readBytes(imageBytes);

                int zoomLevel = bytesMessage.getIntProperty("zoomLevel");

                String imageDataBase64 = bytesMessage.getStringProperty("imageData");
                byte[] imageData = Base64.getDecoder().decode(imageDataBase64);
                
                ProgMainClient rmiClient = new ProgMainClient();
                try {
                    rmiClient.processImage(imageData, zoomLevel);
                    System.out.println("S-A TRIMIS");
                } catch (RemoteException e) {
                    e.printStackTrace(); 
                } catch (IOException e) {
                    System.err.println("Eroare la procesarea imaginii: " + e.getMessage());
                }



            } catch (JMSException | NumberFormatException e) {
                e.printStackTrace();
            }
     } 	
    

}
