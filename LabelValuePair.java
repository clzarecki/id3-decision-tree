import java.util.Comparator;


public class LabelValuePair {

	public String label;
	public double value;
	
	public LabelValuePair(String label, double value) {
		super();
		this.label = label;
		this.value = value;
	}
	
	public static Comparator<LabelValuePair> valueComp = new Comparator<LabelValuePair>() {

		@Override
		public int compare(LabelValuePair lvp1, LabelValuePair lvp2) {
			return Double.compare(lvp1.value, lvp2.value);
		}
		
	};
	
	public static Comparator<LabelValuePair> labelComp = new Comparator<LabelValuePair>() {

		@Override
		public int compare(LabelValuePair lvp1, LabelValuePair lvp2) {
			return lvp1.label.compareTo(lvp2.label);
		}
		
	};
	
}
