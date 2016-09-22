import java.util.ArrayList;
import java.util.List;


public class Attribute {
	
	public String name;
	public boolean isReal;
	public List<String> values;
	
	public Attribute(String name, boolean isReal) {
		super();
		this.name = name;
		this.isReal = isReal;
		this.values = new ArrayList<String>();
	}
	
	public boolean equals(Attribute attr) {
		return name.equals(attr.name);
	}

}
