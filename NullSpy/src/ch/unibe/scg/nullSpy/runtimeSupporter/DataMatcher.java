package ch.unibe.scg.nullSpy.runtimeSupporter;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import ch.unibe.scg.nullSpy.runtimeSupporter.varAssignment.Field;
import ch.unibe.scg.nullSpy.runtimeSupporter.varAssignment.FieldKey;
import ch.unibe.scg.nullSpy.runtimeSupporter.varAssignment.Key;
import ch.unibe.scg.nullSpy.runtimeSupporter.varAssignment.LocalVarKey;
import ch.unibe.scg.nullSpy.runtimeSupporter.varAssignment.LocalVariable;

public class DataMatcher {

	private static ArrayList<ArrayList<String>> receiverList;
	private static HashMap<LocalVarKey, LocalVariable> localVarMap;
	private static HashMap<FieldKey, Field> fieldMap;

	public static void printLocationOnMatch(String csvPath,
			HashMap<LocalVarKey, LocalVariable> localVarMap,
			HashMap<FieldKey, Field> fieldMap, String className, int lineNr,
			String behaviorName) throws FileNotFoundException {

		DataMatcher.localVarMap = localVarMap;
		DataMatcher.fieldMap = fieldMap;

		System.out.println("It works!");
		// System.out.println("ClassName: " + className);
		// System.out.println("LineNr: " + lineNr);
		// System.out.println("MethodName: " + behaviorName);

		receiverList = new ArrayList<>();

		String receiverEntry;
		Scanner fileScan, receiverScan;

		fileScan = new Scanner(new File(csvPath));

		while (fileScan.hasNext()) {
			receiverEntry = fileScan.nextLine();
			System.out.println("ReceiverEntry: " + receiverEntry);

			receiverScan = new Scanner(receiverEntry);
			receiverScan.useDelimiter(",");

			ArrayList<String> receiverElement = new ArrayList<>();

			while (receiverScan.hasNext()) {
				String receiverDataPart = receiverScan.next();
				receiverElement.add(receiverDataPart);
				System.out.println("\t" + receiverDataPart);
			}

			receiverList.add(receiverElement);

			System.out.println("");
		}

		fileScan.close();

		// int npeReceiverIndex = getNPEReceiverIndex(className, lineNr,
		// behaviorName);
		// ArrayList<Integer> npeReceiverGroup =
		// getNPEReceiverGroup(npeReceiverIndex);
		//
		// getVariableKey(npeReceiverIndex, npeReceiverGroup);
		//
		// printNPELocation(npeReceiverIndex, npeReceiverGroup);

	}

	private static Key getVariableKey(int npeReceiverIndex,
			ArrayList<Integer> npeReceiverGroup) {

		String varID = getVarID(npeReceiverIndex);

		if (!varID.equals("field")) {
			return new LocalVarKey(getVariableName(npeReceiverIndex),
					getClassNameWhereVariableIsUsed(npeReceiverIndex),
					getBehaviorName(npeReceiverIndex),
					getBehaviorSignature(npeReceiverIndex));

		} else {
			// FIXME: field version
			return new FieldKey(
					getClassNameWhereVariableIsUsed(npeReceiverIndex),
					getVariableName(npeReceiverIndex),
					getVariableType(npeReceiverIndex),
					getVariableDeclaringClassName(npeReceiverIndex),
					isVariableStatic(npeReceiverIndex), "", "", "",
					(Boolean) null, getBehaviorName(npeReceiverIndex),
					getBehaviorSignature(npeReceiverIndex));
		}
	}

	private static boolean isVariableStatic(int npeReceiverIndex) {
		String b = receiverList.get(npeReceiverIndex).get(5);
		if (b.equals("true"))
			return true;
		else
			return false;
	}

	private static String getVariableDeclaringClassName(int npeReceiverIndex) {
		return receiverList.get(npeReceiverIndex).get(10);
	}

	private static String getVariableType(int npeReceiverIndex) {
		return receiverList.get(npeReceiverIndex).get(4);
	}

	private static String getBehaviorSignature(int npeReceiverIndex) {
		return receiverList.get(npeReceiverIndex).get(8);
	}

	private static String getBehaviorName(int npeReceiverIndex) {
		return receiverList.get(npeReceiverIndex).get(7);
	}

	private static String getClassNameWhereVariableIsUsed(int npeReceiverIndex) {
		return receiverList.get(npeReceiverIndex).get(6);
	}

	private static String getVariableName(int npeReceiverIndex) {
		return receiverList.get(npeReceiverIndex).get(3);
	}

	private static String getVarID(int npeReceiverIndex) {
		return receiverList.get(npeReceiverIndex).get(2);
	}

	private static void printNPELocation(int npeReceiverIndex,
			ArrayList<Integer> npeReceiverGroup) {

	}

	private static ArrayList<Integer> getNPEReceiverGroup(int npeReceiverIndex) {
		ArrayList<Integer> npeReceiverGroup = new ArrayList<>();
		int nr = getNrAsInteger(npeReceiverIndex);
		int checkIndex = npeReceiverIndex - 1;
		while (checkIndex >= 0) {
			int checkNr = getNrAsInteger(checkIndex);
			if (checkNr == nr) {
				npeReceiverGroup.add(checkIndex);
				checkIndex--;
				continue;
			} else
				break;
		}
		checkIndex = npeReceiverIndex + 1;
		while (checkIndex < receiverList.size()) {
			int checkNr = getNrAsInteger(checkIndex);
			if (checkNr == nr) {
				npeReceiverGroup.add(checkIndex);
				checkIndex++;
				continue;
			} else
				break;
		}
		return npeReceiverGroup;
	}

	private static int getNPEReceiverIndex(String className, int lineNr,
			String behaviorName) {
		for (int i = 0; i < receiverList.size(); i++) {
			ArrayList<String> receiverElement = receiverList.get(i);

			if (getLineNumberAsInteger(receiverElement) == lineNr
					&& receiverElement.get(6).equals(className)
					&& receiverElement.get(7).equals(behaviorName)) {
				return i;
			}
		}
		return 0;
	}

	private static int getNrAsInteger(int npeReceiverIndex) {
		return Integer.parseInt(receiverList.get(npeReceiverIndex).get(0));
	}

	private static int getLineNumberAsInteger(ArrayList<String> receiverElement) {
		return Integer.parseInt(receiverElement.get(1));
	}

	public static String getExceptionClassName(Throwable t) {
		return t.getStackTrace()[0].getClassName();
	}

	public static int getExceptionLineNumber(Throwable t) {
		return t.getStackTrace()[0].getLineNumber();
	}

	public static String getExceptionMethodName(Throwable t) {
		return t.getStackTrace()[0].getMethodName();
	}

	private class NPEKey {
		public String className;
		public int lineNr;
		public String behaviorName;

		public NPEKey(String className, int lineNr, String behaviorName) {
			this.className = className;
			this.lineNr = lineNr;
			this.behaviorName = behaviorName;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result
					+ ((behaviorName == null) ? 0 : behaviorName.hashCode());
			result = prime * result
					+ ((className == null) ? 0 : className.hashCode());
			result = prime * result + lineNr;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			NPEKey other = (NPEKey) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (behaviorName == null) {
				if (other.behaviorName != null)
					return false;
			} else if (!behaviorName.equals(other.behaviorName))
				return false;
			if (className == null) {
				if (other.className != null)
					return false;
			} else if (!className.equals(other.className))
				return false;
			if (lineNr != other.lineNr)
				return false;
			return true;
		}

		private DataMatcher getOuterType() {
			return DataMatcher.this;
		}

	}

}
