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
import ch.unibe.scg.nullSpy.runtimeSupporter.varAssignment.Variable;

public class NullDisplayer {

	private static ArrayList<ArrayList<String>> receiverList;
	private static HashMap<LocalVarKey, LocalVariable> localVarMap;
	private static HashMap<FieldKey, Field> fieldMap;

	public static void printLocationOnMatch(String csvPath,
			HashMap<LocalVarKey, LocalVariable> localVarMap,
			HashMap<FieldKey, Field> fieldMap, String className, int lineNr,
			String behaviorName) throws FileNotFoundException {

		// System.out.println("NullDisplayer");

		// printFieldMap();

		// System.out.println("className: " + className + ", lineNr: " + lineNr
		// + ", behaviorName: " + behaviorName);

		NullDisplayer.localVarMap = localVarMap;
		NullDisplayer.fieldMap = fieldMap;

		receiverList = new ArrayList<>();

		storeReceiverDataToMap(csvPath);
		ArrayList<Integer> npeReceiverIndexList = getNPEReceiverIndex(
				className, lineNr, behaviorName);
		// System.out.println("NpeReceiverIndexList:");
		// for (int i = 0; i < npeReceiverIndexList.size(); i++) {
		// System.out.print(npeReceiverIndexList.get(i));
		// System.out.print("\t");
		// }
		// System.out.println();

		ArrayList<ArrayList<Integer>> npeReceiverGroupList = getNPEReceiverGroupList(npeReceiverIndexList);
		// System.out.println("NpeReceiverGroupList:");
		// for (int i = 0; i < npeReceiverGroupList.size(); i++) {
		// ArrayList<Integer> arrayList = npeReceiverGroupList.get(i);
		// System.out.println("NpeReceiverGroupList " + 1 + ":");
		// int size = arrayList.size();
		// for (int j = 0; j < size; j++) {
		// System.out.print(arrayList.get(j));
		// System.out.print("\t");
		// }
		// System.out.println();
		// }
		// System.out.println();

		ArrayList<Key> keyList = getVariableKey(npeReceiverIndexList,
				npeReceiverGroupList);
		printNPELocation(keyList);

	}

	// private static void printFieldMap() {
	// System.out.println(fieldMap.size());
	// for (Entry<FieldKey, Field> k : fieldMap.entrySet()) {
	// System.out.println(k);
	// }
	//
	// }

	private static void printNPELocation(ArrayList<Key> keyList) {

		if (keyList.size() == 0) {
			System.out
					.println("Null: Collection || Value of a Collection || return value of a method");
			return;
		}

		boolean matched = false;

		for (int i = 0; i < keyList.size(); i++) {
			Key key = keyList.get(i);
			Variable var = null;
			String linkVarName = key.getVarName();
			String varID = "";

			for (FieldKey k : fieldMap.keySet()) {
				if (key.equals(k)) {
					matched = true;

					// System.out.println("contains field key");
					var = (Field) fieldMap.get(k);

					linkVarName = getFieldFullName(k);
					System.out.print("Field ");
				}

				if (matched) {
					break;
				}
			}

			if (localVarMap.containsKey(key)) {
				matched = true;
				// System.out.println("contains locVar key");
				var = localVarMap.get(key);
				varID = var.getVarID();
				if (varID.startsWith("p")) {
					System.out.print("Parameter ");
				} else {
					System.out.print("LocalVariable ");
				}
			}

			if (matched) {
				String classNameInWhichVarIsUsed = var
						.getClassNameInWhichVarIsUsed();
				int varLineNr = var.getVarLineNr();

				printNullLink(classNameInWhichVarIsUsed, varLineNr,
						linkVarName, varID);
				return;
			}
		}

		printNodInitialized(keyList.get(0));
	}

	private static String getFieldFullName(FieldKey fieldKey) {
		String memberObjectDeclaringClassName = fieldKey
				.getIndirectVarDeclaringClassName();
		// String memberObjectType = fieldKey.getIndirectVarType();

		if (memberObjectDeclaringClassName.equals("")
		// || memberObjectType.equals("")
		) {
			return "this." + fieldKey.getVarName();
		}
		return memberObjectDeclaringClassName + "."
				+ fieldKey.getIndirectVarName() + "." + fieldKey.getVarName();
	}

	private static void printNodInitialized(Key key) {
		String varName = key.getVarName();
		if (key.getVarID().equals("field")) {
			FieldKey fieldKey = (FieldKey) key;
			varName = getFieldFullName(fieldKey);
			System.out.println("Field " + varName + " is never initialized.");
		} else {
			System.out.println("Local variable  " + varName
					+ "is never initialized.");
		}
	}

	private static void printNullLink(String className, int lineNr,
			String linkVarName, String varID) {
		if (varID.startsWith("p"))
			System.out.print(linkVarName + " is null: ");
		else
			System.out.print(linkVarName + " at line " + lineNr + " is null: ");
		System.out.println(getNullLink(className, lineNr));
	}

	private static String getNullLink(String className, int lineNumber) {
		String nullLink;
		nullLink = "(" + className + ".java:" + lineNumber + ")";
		return nullLink;
	}

	private static void storeReceiverDataToMap(String csvPath)
			throws FileNotFoundException {
		String receiverEntry;
		Scanner fileScan, receiverScan;

		fileScan = new Scanner(new File(csvPath));

		while (fileScan.hasNext()) {
			receiverEntry = fileScan.nextLine();
			// System.out.println("ReceiverEntry: " + receiverEntry);

			receiverScan = new Scanner(receiverEntry);
			receiverScan.useDelimiter(",");

			ArrayList<String> receiverElement = new ArrayList<>();

			while (receiverScan.hasNext()) {
				String receiverDataPart = receiverScan.next();
				receiverElement.add(receiverDataPart);
				// System.out.println("\t" + receiverDataPart);
				// System.out.println("receiverElementSize: "
				// + receiverElement.size());
			}

			receiverList.add(receiverElement);

			// System.out.println("");
		}

		fileScan.close();
	}

	private static ArrayList<Key> getVariableKey(
			ArrayList<Integer> npeReceiverIndexList,
			ArrayList<ArrayList<Integer>> npeReceiverGroupList) {
		ArrayList<Key> keyList = new ArrayList<>();
		for (int i = 0; i < npeReceiverGroupList.size(); i++) {
			ArrayList<Integer> npeReceiverGroup = npeReceiverGroupList.get(i);
			int index_1 = npeReceiverGroup.get(0);

			String varID = getVarID(index_1);

			// System.out.println(index_1);
			// System.out.println(varID);
			// System.out.println(npeReceiverGroup.size());

			if (!varID.equals("field") && npeReceiverGroup.size() == 1) {
				keyList.add(new LocalVarKey(getVariableName(index_1),
						getClassNameWhereVariableIsUsed(index_1),
						getBehaviorName(index_1), getBehaviorSignature(index_1)));
				// System.out.println("localVar");
			} else {
				if (npeReceiverGroup.size() == 1) { // staticField
					keyList.add(new FieldKey(
							getClassNameWhereVariableIsUsed(index_1),
							getVariableName(index_1), getVariableType(index_1),
							getVariableDeclaringClassName(index_1),
							isVariableStatic(index_1), "", "", "", false,
							getBehaviorName(index_1),
							getBehaviorSignature(index_1)));
					// System.out.println("staticField");
				} else if (npeReceiverGroup.size() == 2) {
					// this.field; aload.field; staticField.field
					int index_2 = npeReceiverGroup.get(1);
					if (getVariableName(index_1).equals("this")) {
						FieldKey k = new FieldKey(
								getClassNameWhereVariableIsUsed(index_2),
								getVariableName(index_2),
								getVariableType(index_2),
								getVariableDeclaringClassName(index_2),
								isVariableStatic(index_2), "", "", "", false,
								getBehaviorName(index_2),
								getBehaviorSignature(index_2));
						keyList.add(k);
						// System.out.println("this.field");
						// System.out.println(k.toString());
						// System.out.println();
					} else {
						keyList.add(new FieldKey(
								getClassNameWhereVariableIsUsed(index_2),
								getVariableName(index_2),
								getVariableType(index_2),
								getVariableDeclaringClassName(index_2),
								isVariableStatic(index_2),
								getVariableName(index_1),
								getVariableType(index_1),
								getVariableDeclaringClassName(index_1),
								isVariableStatic(index_1),
								getBehaviorName(index_2),
								getBehaviorSignature(index_2)));
						// System.out.println("bla.field");
					}
				} else if (npeReceiverGroup.size() == 3) {
					// indirectField.field
					// FIXME: what to do..

					int index_2 = npeReceiverGroup.get(1);
					if (getVariableName(index_1).equals("this")) {
						FieldKey k = new FieldKey(
								getClassNameWhereVariableIsUsed(index_2),
								getVariableName(index_2),
								getVariableType(index_2),
								getVariableDeclaringClassName(index_2),
								isVariableStatic(index_2), "", "", "", false,
								getBehaviorName(index_2),
								getBehaviorSignature(index_2));
						keyList.add(k);
						// System.out.println("this.field");
						// System.out.println(k.toString());
						// System.out.println();
					} else {
						keyList.add(new FieldKey(
								getClassNameWhereVariableIsUsed(index_2),
								getVariableName(index_2),
								getVariableType(index_2),
								getVariableDeclaringClassName(index_2),
								isVariableStatic(index_2),
								getVariableName(index_1),
								getVariableType(index_1),
								getVariableDeclaringClassName(index_1),
								isVariableStatic(index_1),
								getBehaviorName(index_2),
								getBehaviorSignature(index_2)));
						// System.out.println("bla.field");
					}

					// System.out.println("Future Work");
				}
			}
		}

		return keyList;
	}

	private static ArrayList<ArrayList<Integer>> getNPEReceiverGroupList(
			ArrayList<Integer> npeReceiverIndexList) {
		ArrayList<ArrayList<Integer>> npeReceiverGroupList = new ArrayList<>();
		for (int i = 0; i < npeReceiverIndexList.size(); i++) {

			ArrayList<Integer> npeReceiverGroup = new ArrayList<>();
			int npeReceiverIndex = npeReceiverIndexList.get(i);
			npeReceiverGroup.add(npeReceiverIndex);
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
					i++;
					npeReceiverGroup.add(checkIndex);
					checkIndex++;
					continue;
				} else
					break;
			}
			npeReceiverGroupList.add(npeReceiverGroup);

		}

		removeDuplicatedEntry(npeReceiverGroupList);
		return npeReceiverGroupList;
	}

	private static void removeDuplicatedEntry(
			ArrayList<ArrayList<Integer>> npeReceiverGroupList) {
		for (int i = 0; i + 1 < npeReceiverGroupList.size(); i++) {
			ArrayList<Integer> npeReceiverGroup_1 = npeReceiverGroupList.get(i);
			ArrayList<Integer> npeReceiverGroup_2 = npeReceiverGroupList
					.get(i + 1);
			int j = 0;
			while (npeReceiverGroup_1.size() == npeReceiverGroup_2.size()
					&& j < npeReceiverGroup_1.size()
					&& npeReceiverGroup_1.get(j) == npeReceiverGroup_2.get(j)) {
				if (j == npeReceiverGroup_1.size() - 1) {
					npeReceiverGroupList.remove(i);
					removeDuplicatedEntry(npeReceiverGroupList);
				}
				j++;
			}
		}
	}

	private static ArrayList<Integer> getNPEReceiverIndex(String className,
			int lineNr, String behaviorName) {
		// System.out.println(className);
		// System.out.println(lineNr);
		// System.out.println(behaviorName);
		receiverList.remove(0);
		ArrayList<Integer> receiverElementIndexList = new ArrayList<>();
		for (int i = 0; i < receiverList.size(); i++) {
			ArrayList<String> receiverElement = receiverList.get(i);

			// int checkIndex = 0;
			if (getLineNumberAsInteger(receiverElement) == lineNr
					&& receiverElement.get(6).equals(className)
					&& receiverElement.get(7).equals(behaviorName)) {
				receiverElementIndexList.add(i);
			}
		}
		return receiverElementIndexList;
	}

	private static int getNrAsInteger(int npeReceiverIndex) {
		return Integer.parseInt(receiverList.get(npeReceiverIndex).get(0));
	}

	private static int getLineNumberAsInteger(ArrayList<String> receiverElement) {
		return Integer.parseInt(receiverElement.get(1));
	}

	private static boolean isVariableStatic(int npeReceiverIndex) {
		String b = receiverList.get(npeReceiverIndex).get(5);
		if (b.equals("true")) {
			return true;
		}

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

}
