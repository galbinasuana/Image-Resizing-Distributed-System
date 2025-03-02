package eu.deic;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

import java.io.InputStream;

import jakarta.jms.BytesMessage;
import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.DeliveryMode;
import jakarta.jms.MessageProducer;
import jakarta.jms.Session;
import jakarta.jms.Topic;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

@WebServlet("/upload-image")
@MultipartConfig
public class UploadImageS extends HttpServlet{

	private static final long serialVersionUID = 1L;

	public UploadImageS() {
        super();
    } 
	
	@Resource
    private ConnectionFactory connectionFactory;

    @Resource(name = "jms/topic/image")
    private Topic topic;
    
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		processRequest(request, response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		processRequest(request, response);
	}
	
	protected void processRequest(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException{
		res.setContentType("text/html;charset=UTF-8");
		
		try {
			Part filePart = req.getPart("fileToUpload");
			String fileName = "";
			final String partHeader = filePart.getHeader("content-disposition");
		    for (String content : partHeader.split(";")) {
		        if (content.trim().startsWith("filename")) {
		            fileName = content.substring(content.indexOf('=') + 1).trim().replace("\"", "");
		        }
		        else {
		        	fileName = null; 
		        }
		    }
			
            InputStream fileContent = filePart.getInputStream();

            String zoomLevel = req.getParameter("zoomLevel");
            System.out.println("Zoom Level: " + zoomLevel);

            byte[] imageData = processInputStream(fileContent);

            JmsTopic(imageData, zoomLevel);
//            res.getWriter().append("<h1>Image Upload Successful</h1>" + "\n");
//            res.getWriter().append("<p>Uploaded File: " + fileName + "</p>" + "\n");
            
        } 
		finally {}
	}
	

	
	private byte[] processInputStream(InputStream inputStream) throws IOException {

		byte[] buffer = new byte[1024];
        int bytesRead;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        while ((bytesRead = inputStream.read(buffer)) != -1) {
            byteArrayOutputStream.write(buffer, 0, bytesRead);
        }

        return byteArrayOutputStream.toByteArray();
    }

	private void JmsTopic(byte[] imageData, String zoomLevel) {
        try {
        	Connection connection = connectionFactory.createConnection();
            connection.start();

            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            MessageProducer producer = session.createProducer(topic);
            producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

            // message
            BytesMessage message=session.createBytesMessage();
            String base64Image = Base64.getEncoder().encodeToString(imageData);
            message.setStringProperty("imageData", base64Image);
            message.setIntProperty("zoomLevel", Integer.parseInt(zoomLevel));

            producer.send(message);            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
