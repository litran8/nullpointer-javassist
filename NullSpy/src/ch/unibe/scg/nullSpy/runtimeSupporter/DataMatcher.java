package ch.unibe.scg.nullSpy.runtimeSupporter;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import ch.unibe.scg.nullSpy.runtimeSupporter.varAssignment.Field;
import ch.unibe.scg.nullSpy.runtimeSupporter.varAssignment.FieldKey;
import ch.unibe.scg.nullSpy.runtimeSupporter.varAssignment.IndirectFieldObject;
import ch.unibe.scg.nullSpy.runtimeSupporter.varAssignment.Key;
import ch.unibe.scg.nullSpy.runtimeSupporter.varAssignment.LocalVarKey;
import ch.unibe.scg.nullSpy.runtimeSupporter.varAssignment.LocalVariable;
import ch.unibe.scg.nullSpy.runtimeSupporter.varAssignment.Variable;

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

		receiverList = new ArrayList<>();

		storeReceiverDataToMap(csvPath);
		ArrayList<Integer> npeReceiverIndexList = getNPEReceiverIndex(
				className, lineNr, behaviorName);
		// System.out.println("npeReceiverIndexListSize: "
		// + npeReceiverIndexList.size());
		ArrayList<ArrayList<Integer>> npeReceiverGroupList = getNPEReceiverGroupList(npeReceiverIndexList);
		// System.out.println("npeReceiverGroupListSize: "
		// + npeReceiverGroupList.size());

		ArrayList<Key> keyList = getVariableKey(npeReceiverIndexList,
				npeReceiverGroupList);
		// System.out.println("keyListSize: " + keyList.size());
		printNPELocation(keyList);

	}

	private static void printNPELocation(ArrayList<Key> keyList) {
		for (int i = 0; i < keyList.size(); i++) {
			Key key = keyList.get(i);
			Variable var = null;
			String linkVarName = "";
			if (fieldMap.containsKey(key)) {
				var = fieldMap.get(key);
				Field field = (Field) var;

				String varName = field.getVarName();
				linkVarName = "this." + varName;
				IndirectFieldObject indirectVarObj = field
						.getIndirectFieldObject();

				if (indirectVarObj != null) {

					String indirectVarName = indirectVarObj
							.getIndirectVarName();
					linkVarName = indirectVarName + "." + varName;
					String indirectVarDeclaringClassName = indirectVarObj
							.getIndirectVarDeclaringClassName();
					String indirectVarType = indirectVarObj
							.getIndirectVarType();

					if (!indirectVarDeclaringClassName.equals("")
							&& !indirectVarType.equals("")) {
						linkVarName = indirectVarDeclaringClassName + "."
								+ indirectVarName + "." + varName;
					}
				}
				System.out.print("Field ");
			} else if (localVarMap.containsKey(key)) {
				var = localVarMap.get(key);
				linkVarName = var.getVarName();
				System.out.print("LocalVariable ");
			} else {
				return;
			}

			String classNameInWhichVarIsUsed = var
					.getClassNameInWhichVarIsUsed();
			int varLineNr = var.getVarLineNr();

			printNullLink(classNameInWhichVarIsUsed, varLineNr, linkVarName);
		}

	}

	private static void printNullLink(String className, int lineNr,
			String linkVarName) {
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
			System.out.println(getVariableName(index_1));
			String varID = getVarID(index_1);

			if (!varID.equals("field") && npeReceiverGroup.size() == 1) {
				keyList.add(new LocalVarKey(getVariableName(index_1),
						getClassNameWhereVariableIsUsed(index_1),
						getBehaviorName(index_1), getBehaviorSignature(index_1)));

			} else {
				if (npeReceiverGroup.size() == 1) { // staticField
					keyList.add(new FieldKey(
							getClassNameWhereVariableIsUsed(index_1),
							getVariableName(index_1), getVariableType(index_1),
							getVariableDeclaringClassName(index_1),
							isVariableStatic(index_1), "", "", "", false,
							getBehaviorName(index_1),
							getBehaviorSignature(index_1)));
				} else if (npeReceiverGroup.size() == 2) {
					// this.field; aload.field; staticField.field
					int index_2 = npeReceiverGroup.get(1);
					System.out.println(getVariableName(index_2));
					if (getVariableName(index_1).equals("this"))
						keyList.add(new FieldKey(
								getClassNameWhereVariableIsUsed(index_2),
								getVariableName(index_2),
								getVariableType(index_2),
								getVariableDeclaringClassName(index_2),
								isVariableStatic(index_2), "", "", "", false,
								getBehaviorName(index_2),
								getBehaviorSignature(index_2)));
					else
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
				} else if (npeReceiverGroup.size() == 3) {
					// indirectField.field
					System.out.println("HHHHEEEEEEEEEEELLLLLLLLLLPPPPPPPPPP");
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
		// FIXME: list empty
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
			// if (i > checkIndex
			// && getLineNumberAsInteger(receiverElement) != lineNr
			// && !receiverElement.get(6).equals(className)
			// && !receiverElement.get(7).equals(behaviorName)) {
			// break;
			// }
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
		if (b.equals("true"))
			return true;
		else
			return false;
	}

	private static String getVariableDeclaringClassName(int npeReceiverIndex) {
		ArrayList<String> list = receiverList.get(npeReceiverIndex);
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
