
public class Vector3 {
	
	public double x;
	public double y;
	public double z;
	
	public Vector3( double x, double y, double z ) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public double getMagnitude() {
		return Math.sqrt( x * x + y * y + z * z );
	}
	
	public String toString() {
		return "X: " + x + " Y: " + y + " Z: " + z;
	}
}
