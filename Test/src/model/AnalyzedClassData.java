package Modell;

import java.util.ArrayList;
import java.util.List;

public class AnalyzedClassData {

	private List<MyClass> listOfAnalyzedClassData;

	public AnalyzedClassData() {
		listOfAnalyzedClassData = new ArrayList<MyClass>();
	}

	public List<MyClass> getListOfAnalyzedClassData() {
		return listOfAnalyzedClassData;
	}

	public void setListOfAnalyzedClassData(List<MyClass> listOfAnalyzedClassData) {
		this.listOfAnalyzedClassData = listOfAnalyzedClassData;
	}

	public void addClass(MyClass myClass) {
		listOfAnalyzedClassData.add(myClass);
	}
}
