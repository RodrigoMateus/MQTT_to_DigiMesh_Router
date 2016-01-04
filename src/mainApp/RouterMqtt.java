package mainApp;

import java.io.File;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import org.apache.commons.lang3.SerializationUtils;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.digi.xbee.api.exceptions.TimeoutException;
import com.digi.xbee.api.exceptions.TransmitException;
import com.digi.xbee.api.exceptions.XBeeException;
import com.digi.xbee.api.utils.LogRecord;
import com.maykot.radiolibrary.ErrorCode;
import com.maykot.radiolibrary.ErrorMessage;
import com.maykot.radiolibrary.ProxyRequest;
import com.maykot.radiolibrary.ProxyResponse;

public class RouterMqtt implements MqttCallback {

	public RouterMqtt() {
		super();
		try {
			Thread.sleep(1000);
			testeSendMessage();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

//	public void testeSendMessage() {
//		try {
//			byte[] dataToSend = null;
//			dataToSend = new String("This is a message boot test!").getBytes();
//			SendTextMessage.send(dataToSend, MainApp.ENDPOINT_TXT);
//			System.out.println("Boot test SUCCESS!");
//		} catch (Exception e) {
//			System.out.println("Boot test FAILED!");
//			e.printStackTrace();
//		}
//	}

	public void testeSendMessage() {
		try {

			ProxyRequest proxyRequest = new ProxyRequest();
			proxyRequest.setVerb("POST");
			proxyRequest.setUrl("http://localhost:8000");

			HashMap<String, String> header = new HashMap<String, String>();
			header.put("content-type", "image/png");
			header.put("proxy-response", "0");
			proxyRequest.setHeader(header);
			proxyRequest.setIdMessage("1");

			String clientId = "clientId_Test";
			byte[] mqttClientId = clientId.getBytes();
			byte[] noMessage = new String("noMessage").getBytes();
			byte[] dataToSend = Files.readAllBytes(new File("image50KB.png").toPath());

			proxyRequest.setBody(dataToSend);

			SendHttpPost.send(MainApp.myDevice, mqttClientId, MainApp.ENDPOINT_HTTP_POST_INIT, MainApp.remoteDevice);

			System.out.println("POST INIT SUCCESS!");

			SendHttpPost.send(MainApp.myDevice,
					new String(
							"As diversas finalidades do trabalho acadêmico podem se resumir em apresentar, demonstrar, difundir, recuperar ou contestar o conhecimento produzido, acumulado ou transmitido. Ao apresentar resultados, o texto acadêmico atende à necessidade de publicidade relativa ao processo de conhecimento. A pesquisa realizada, a ideia concebida ou a dedução feita perecem se não vierem a público; por esse motivo existem diversos canais de publicidade adequados aos diferentes trabalhos: as defesas públicas, os periódicos, as comunicações e a multimídia virtual são alguns desses. A demonstração do conhecimento é necessidade na comunidade acadêmica, onde esse conhecimento é o critério de mérito e acesso. Assim, existem as provas, concursos e diversos outros processos de avaliação pelos quais se constata a construção ou transmissão do saber. Difundir o conhecimento às esferas externas à comunidade acadêmica é atividade cada vez mais presente nas instituições de ensino, pesquisa e extensão, e o texto correspondente a essa prática tem característica própria sem abandonar a maior parte dos critérios de cientificidade. A recuperação do conhecimento é outra finalidade do texto acadêmico. Com bastante freqüência, parcelas significativas do conhecimento caem no esquecimento das comunidades e das pessoas; a recuperação e manutenção ativa da maior diversidade de saberes é finalidade importante de atividades científicas objeto da produção de texto. Quase todo conhecimento produzido é contestado. Essa contestação, em que não constitua conhecimento diferenciado, certamente é etapa contribuinte no processo da construção do saber que contesta, quer por validá-lo, quer por refutá-lo. As finalidades do texto acadêmico certamente não se esgotam nessas, mas ficam aqui exemplificadas. Para atender à diversidade dessas finalidades, existe a multiplicidade de formas, entre as quais se encontram alguns conhecidos tipos, sobre os quais se estabelece conceito difuso.")
									.getBytes(),
					MainApp.ENDPOINT_HTTP_POST_DATA, MainApp.remoteDevice);

			System.out.println("POST DATA SUCCESS!");
			
			SendHttpPost.send(MainApp.myDevice, noMessage, MainApp.ENDPOINT_HTTP_POST_SEND, MainApp.remoteDevice);
			
			System.out.println("POST SEND SUCCESS!");

		} catch (Exception e) {
			System.out.println("Boot test FAILED!");
			e.printStackTrace();
		}
		System.exit(0);
	}

	@Override
	public void connectionLost(Throwable arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {

		// Se a mensagem contém um HTTP POST
		if (topic.toLowerCase().contains("http_post")) {

			String[] topicWords = topic.split("/");
			String clientId = topicWords[2];
			byte[] mqttClientId = clientId.getBytes();
			String messageId = topicWords[3];
			String body = null;

			ProxyRequest proxyRequest = (ProxyRequest) SerializationUtils.deserialize(message.getPayload());

			if (proxyRequest.getBody() == null)
				body = "no body";
			else
				body = new String(proxyRequest.getBody());

			LogRecord.insertLog("ProxyRequestLog", clientId + ";" + new String(proxyRequest.getIdMessage()) + ";"
					+ new String(new SimpleDateFormat("yyyy-MM-dd;HH:mm:ss:SSS").format(new Date())) + ";" + body);

			byte[] noMessage = new String("noMessage").getBytes();

			try {
				SendHttpPost.send(MainApp.myDevice, mqttClientId, MainApp.ENDPOINT_HTTP_POST_INIT,
						MainApp.remoteDevice);
				SendHttpPost.send(MainApp.myDevice, message.getPayload(), MainApp.ENDPOINT_HTTP_POST_DATA,
						MainApp.remoteDevice);
				SendHttpPost.send(MainApp.myDevice, noMessage, MainApp.ENDPOINT_HTTP_POST_SEND, MainApp.remoteDevice);
			} catch (TransmitException e) {
				LogRecord.insertLog("ErrorLog",
						clientId + ";" + new String(proxyRequest.getIdMessage()) + ";"
								+ new String(new SimpleDateFormat("yyyy-MM-dd;HH:mm:ss:SSS").format(new Date())) + ";"
								+ ErrorMessage.TRANSMIT_EXCEPTION.getDescription());
				System.out.println(ErrorMessage.TRANSMIT_EXCEPTION.getValue() + ": "
						+ ErrorMessage.TRANSMIT_EXCEPTION.getDescription());
				sendErrorMessage(ErrorMessage.TRANSMIT_EXCEPTION.getValue(), clientId, messageId,
						ErrorMessage.TRANSMIT_EXCEPTION.getDescription());

			} catch (TimeoutException e) {
				LogRecord.insertLog("ErrorLog",
						clientId + ";" + new String(proxyRequest.getIdMessage()) + ";"
								+ new String(new SimpleDateFormat("yyyy-MM-dd;HH:mm:ss:SSS").format(new Date())) + ";"
								+ ErrorMessage.TIMEOUT_ERROR.getDescription());
				System.out.println("Erro " + ErrorMessage.TIMEOUT_ERROR.getValue() + ": "
						+ ErrorMessage.TIMEOUT_ERROR.getDescription());
				sendErrorMessage(ErrorMessage.TIMEOUT_ERROR.getValue(), clientId, messageId,
						ErrorMessage.TIMEOUT_ERROR.getDescription());

			} catch (XBeeException e) {
				LogRecord.insertLog("ErrorLog",
						clientId + ";" + new String(proxyRequest.getIdMessage()) + ";"
								+ new String(new SimpleDateFormat("yyyy-MM-dd;HH:mm:ss:SSS").format(new Date())) + ";"
								+ "XBeeException ERROR");
				e.printStackTrace();
				sendErrorMessage(606, clientId, messageId, ErrorCode.e606);

			} catch (Exception e) {
				LogRecord.insertLog("ErrorLog",
						clientId + ";" + new String(proxyRequest.getIdMessage()) + ";"
								+ new String(new SimpleDateFormat("yyyy-MM-dd;HH:mm:ss:SSS").format(new Date())) + ";"
								+ "Exception ERROR");
				sendErrorMessage(607, clientId, messageId, ErrorCode.e607);
			}
		}

		// Se a mensagem contém um comando JAVA
		if (topic.toLowerCase().contains("command")) {
			String command = new String(message.getPayload());

			switch (command) {

			case "reset":
				String[] topicWords = topic.split("/");
				String clientId = topicWords[1];
				MainApp.modemStatusReceiveListener.modemReset(clientId);
				break;

			default:
				break;
			}
		}

		// Se a mensagem contém apenas texto
		if (topic.toLowerCase().contains("text")) {
			SendTextMessage.send(message.getPayload(), MainApp.ENDPOINT_TXT);
		}
	}

	public void sendErrorMessage(int statusCode, String clientId, String messageId, String errorCode) {
		ProxyResponse errorResponse = new ProxyResponse(statusCode, "application/json", errorCode.getBytes());
		errorResponse.setMqttClientId(clientId);
		errorResponse.setIdMessage(messageId);

		byte[] payload = SerializationUtils.serialize(errorResponse);

		new MQTTMonitor().sendMQTT(errorResponse, payload);
	}
}
