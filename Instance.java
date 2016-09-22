import java.util.List;

/**
 * Holds data for particular instance.
 */
public class Instance {
	
	public String label;
	public List<String> attributes;
	
	public Instance(String label, List<String> attributes) {
		super();
		this.label = label;
		this.attributes = attributes;
	}

}