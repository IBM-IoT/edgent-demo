import java.net.InetSocketAddress;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.util.HashMap;

public class WebSocketManager {
	
	private static int nextClientID = 1;
	private static HashMap<WebSocket,WSClient> clients = new HashMap<WebSocket,WSClient>();
	private static WebSocket nodeRedClient = null;
	
	public WebSocketManager() {
		WSServer server = new WSServer( new InetSocketAddress( 31880 ) );
		server.start();
		System.out.println("WebSocket server up.");
	}
	
	public void sendData( String message ) {
		if( nodeRedClient != null ) {
			nodeRedClient.send( message );
		}
	}
	
	private class WSServer extends WebSocketServer {
		
		public WSServer( InetSocketAddress address ) {
			super( address );
		}
		
		@Override
		public void onOpen( WebSocket conn, ClientHandshake handshake ) {
			WSClient client = new WSClient( nextClientID++ );
			clients.put(conn, client);
			client.onOpen(handshake);
			
			if( nodeRedClient == null ) nodeRedClient = conn;
		}
		
		@Override
		public void onClose( WebSocket conn, int code, String reason, boolean remote ) {
			clients.get(conn).onClose(code, reason, remote);
			if( conn == nodeRedClient ) nodeRedClient = null;
		}
		
		@Override
		public void onMessage( WebSocket conn, String message ) {
			clients.get(conn).onMessage(message);
		}
		
		@Override
		public void onError( WebSocket conn, Exception ex ) {
			ex.printStackTrace();
		}
	}
	
	private class WSClient {
		
		private int id;
		
		public WSClient( int id ) {
			this.id = id;
		}
		
		public void onOpen( ClientHandshake handshake ) {
			System.out.println(id + ": Connected (" + handshake.getResourceDescriptor() + ")");
		}
		
		public void onClose( int code, String reason, boolean remote ) {
			System.out.println(id + ": Disconnected");
		}
		
		public void onMessage( String message ) {
			String[] split = message.split(",");
			if (split.length != 3 ) return;
			
			try {
				Vector3 v = new Vector3(Double.parseDouble(split[0]), Double.parseDouble(split[1]), Double.parseDouble(split[2]));
				QuarksEarthquakeDemo.addData(v);
			} catch( NumberFormatException e ) {}
		}
	}
}
