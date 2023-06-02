import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class SysCall {

	public boolean contains(int var_Loc, String var, Memory currentMemory) {
		for (int i = var_Loc; i < var_Loc + 3; i++) {
			String n[] = (currentMemory.memory[i] + "").split(" ");
			VarBlock temp = new VarBlock(n[3], n[n.length - 1]);
			if (temp.var.equals(var)) {
				return true;
			}
		}
		return false;
	}

	public String readFromMemoryNum(int var_Loc, String var, Memory currentMemory) {
		for (int i = var_Loc; i < var_Loc + 3; i++) {
			String n[] = (currentMemory.memory[i] + "").split(" ");
			String s = "";
			for (int k = 6; k < n.length; k++) {
				s+=n[k];
			}
			VarBlock temp = new VarBlock(n[3], s);
			if (temp.var.equals(var)) {
				return temp.value;
			}
		}

		return "";
	}

	public String readFromMemoryStr(int var_Loc, String arg, Memory currentMemory) {
		for (int i = var_Loc; i < var_Loc + 3; i++) {
			String n[] = (currentMemory.memory[i] + "").split(" ");
			String s = "";

			for (int k = 6; k < n.length; k++) {
				s += n[k] + " ";
			}
			VarBlock temp = new VarBlock(n[3], s);
			if (temp.var.equals(arg)) {

				return temp.value;
			}
		}

		return "";
	}

	public void print(String var, int var_Loc, Memory memory) {
		if (this.contains(var_Loc, var, memory)) {
			System.out.println(readFromMemoryStr(var_Loc, var, memory) + "");
		} else {
			System.out.println(" Value of Variable: " + var + "");
		}

	}
	
	public String readFile(String var, int var_Loc, Memory memory) {
		String filepath;

		if (this.contains(var_Loc, var, memory))
			filepath = System.getProperty("user.dir") + "\\Src\\" + readFromMemoryStr(var_Loc, var, memory) + ".txt";
		else
			filepath = var;
		File file = new File(filepath);
		Scanner sc;
		String st = "";
		try {
			sc = new Scanner(file);
			while (sc.hasNextLine()) {
				st += sc.nextLine() + "\n";

			}
		} catch (FileNotFoundException e) {
			System.out.println(" File does not exist! ");
			st += "File not Found";
		}
		return st;
	}

	public void writeFile(String var1, String val, int var_Loc, Memory memory) throws IOException {
		String fileName;
		System.out.println("var1"+var1+"val"+val);			

		if (this.contains(var_Loc, var1, memory))
			fileName = readFromMemoryStr(var_Loc, var1, memory);
		else
			fileName = var1;
		String text;
		if (this.contains(var_Loc, val, memory))
			text = readFromMemoryStr(var_Loc, val, memory);
		else
			text = val;
		String filePath = System.getProperty("user.dir") + "\\Src\\";
		createFile(filePath, fileName);
		try(FileWriter myWriter = new FileWriter(filePath + "\\" + fileName + ".txt")){
			myWriter.write(text);
			myWriter.close();
			System.out.println("-------File created and appended successfully!---------");			
		}

	}

	public void writeFile(String var1, String val) throws IOException {
		String fileName = var1;
		String text = val;
		String filePath = System.getProperty("user.dir") + "\\Src\\";
		createFile(filePath, fileName);
		try(FileWriter myWriter = new FileWriter(filePath + "\\" + fileName + ".txt")){
			myWriter.write(text);			
			myWriter.close();
		}

	}
	public static void createFile(String var, String fileName) throws IOException {
		File file = new File(var + "\\" + fileName + ".txt");
		file.createNewFile();
	}
	
	public void writeToMemory(VarBlock var, int var_Loc, Memory currentMemory) {
		currentMemory.memory[var_Loc] = var;
	}
	
	public String takeInput() {
		Scanner sc = new Scanner(System.in);
		System.out.println("Please enter a value:");
		return sc.nextLine();	
		
	}



}