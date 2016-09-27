/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015, 2016 
*/
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import org.bson.BSONObject;
import org.bson.BasicBSONObject;

import com.informix.jdbc.IfxBSONObject;

import quarks.connectors.jdbc.JdbcStreams;
import quarks.providers.direct.DirectProvider;
import quarks.topology.TStream;
import quarks.topology.TWindow;
import quarks.topology.Topology;

public class QuarksEarthquakeDemo {
	public static LinkedBlockingQueue<Vector3> dataQueue = new LinkedBlockingQueue<Vector3>();
	
    public static void main(String[] args) throws Exception {
    	
    	WebSocketManager wss = new WebSocketManager();
    	
        DirectProvider tp = new DirectProvider();

        Topology t = (Topology)tp.newTopology("PeriodicSource");

        JdbcStreams mydb = new JdbcStreams(t,
        		() -> InformixConnector.getIfxDataSource() ,
                (dataSource) -> dataSource.getConnection() );
        
        TStream<Vector3> accelerometer = t.generate(() -> dataQueue.poll());

        TStream<Double> magnitude = accelerometer.map(v -> v.getMagnitude());
        
        TWindow<Double,Integer> magWindow = magnitude.last(2, (Double v) -> 0);
        
        TStream<Double> magDelta = magWindow.aggregate((List<Double> values, Integer k) -> {
        	if (values.size() < 2) {
        		return 0.0;
        	} else {
        		return Math.abs( values.get(1) - values.get(0) );
        	}
        } );
        
        magDelta = magDelta.peek(v -> wss.sendData(v.toString()));
        
        // write stream tuple to database/table
        mydb.executeStatement(accelerometer, 
        		 () -> "INSERT into sensors_vti values (?,?,?)",
        		 (v,stmt) -> {
        			String ts = InformixConnector.getCurrentTS();
        			stmt.setString(1, "1");
        			stmt.setString(2, ts);
        			BSONObject bson = new BasicBSONObject();
        			bson.put("x", v.x);
        			bson.put("y", v.y);
        			bson.put("z", v.z);
        			stmt.setObject(3, new IfxBSONObject(bson));
        		 } );

        // Submit the topology
        tp.submit(t);
    }
    
    public static void addData( Vector3 value ) {
    	dataQueue.offer(value);
    } 
}
