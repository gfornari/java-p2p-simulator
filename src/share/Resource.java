package share;


public class Resource {
	String name;
	int parts;
	
	public Resource(String name, int parts) {
		this.name = name;
		this.parts = parts;
	}
	
	@Override
	public String toString() {
		return name + " " + parts;
	}
	
	public String[] toArrayStrings() {
		return new String[]{name, String.valueOf(parts)};
	}
}
