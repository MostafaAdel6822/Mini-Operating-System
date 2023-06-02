import java.io.IOException;
import java.util.Queue;

public class Kernel {
	private SysCall SysCall;
	private Mutex mutex;
	private Memory currentMemory;

	public Kernel(Queue<Process> blockedQueue, Queue<Process> readyQueue, Memory currentMemory) {
		this.mutex = new Mutex(blockedQueue, readyQueue, currentMemory);
		this.currentMemory = currentMemory;
		this.SysCall = new SysCall();
	}

	
	public void printFromTo(String x, String y, int var_loc, Memory currentMemory) {
		currentMemory.print();
		int fn;
		int sn;
		if (!SysCall.contains(var_loc, x, currentMemory)) {
			fn = Integer.parseInt(x);
		}else {
			fn = Integer.parseInt(SysCall.readFromMemoryNum(var_loc, x, currentMemory));
		}
		
		if (!SysCall.contains(var_loc, y, currentMemory)) {
			sn = Integer.parseInt(y);
		}else {
			sn = Integer.parseInt(SysCall.readFromMemoryNum(var_loc, y, currentMemory));
		}
		if (fn < sn) {
			int temp = fn;
		    fn = sn;
			sn = temp;
		}		
		System.out.println("-----Values in between "+sn+" and "+fn+": --------");
		for (int i = sn+ 1; i < fn; i++) {
			System.out.print(i + " ");
		}
		
		System.out.println();
	}
	
	public void assign(String var1, String var2, int varIdx, Process p){
		if (!SysCall.contains(varIdx, var2, currentMemory)) {
			VarBlock temPair = new VarBlock(var1, var2);
			SysCall.writeToMemory(temPair, p.spot + varIdx, currentMemory);
			if ((p.spot+varIdx)!=(varIdx+2))
				currentMemory.memory[varIdx+2]="Variable name is Empty and value in Null";
			p.spot = p.spot + 1;
			System.out.println( "------"+var2 +" is assigned to variable " + var1+"-------");
			
		} else {
			var2 = SysCall.readFromMemoryStr(varIdx, var1, currentMemory);
			VarBlock temPair = new VarBlock(var1, var2);
			SysCall.writeToMemory(temPair, p.spot + varIdx, currentMemory);
			if ((p.spot+varIdx)!=(varIdx+2))
				currentMemory.memory[varIdx+2]="Variable name is Empty and value in Null";
			p.spot = p.spot + 1;
			System.out.println( "------"+var2 + "is assigned to variable " + var1+"-------");
			
		}
		
	}
	
	public String execReadFile(String var1, String var2, Process p, int varIdx) throws IOException {
		System.out.println("process:" + p.pid + " read file:" + var2);
		return SysCall.readFile(var2, varIdx, currentMemory);
	}
	
	
	public void execSecondInstruction(String arg1, String arg2, Process p, int varIdx) {
		if(arg1.equals("print")) {
			System.out.println("Process:" + p.pid + " is printing " + arg2);
			SysCall.print(arg2, varIdx, currentMemory);
		}
		else if(arg1.equals("semWait")){
			System.out.println("---------Process:" + p.pid + " is in semWait " + arg2+"----------");
			mutex.semWait(arg2, p);
		}else if(arg1.equals("semSignal")){
			System.out.println("---------process:" + p.pid + " is in semSignal " + arg2+"--------");
			mutex.semSignal(arg2, p.pid);
		}
		
	}

	public void execThirdInstruction(String arg1, String arg2, String arg3, Process p, int varIdx) throws IOException {
		
		if(arg1.equals("assign")) {
			System.out.println("------Process:" + p.pid + " is assigning--------");
			assign(arg2, arg3, varIdx, p);
		}else if(arg1.equals("writeFile")){
			System.out.println("------process:" + p.pid + " is performing a writeFile instruction ----------");
			SysCall.writeFile(arg2, arg3, varIdx, currentMemory);
		}else if(arg1.equals("printFromTo")) {
			System.out.println("------process:" + p.pid + " is printing from " + arg2 + " to " + arg3+"---------");
			this.printFromTo(arg2, arg3, varIdx, currentMemory);
		}

	}

	public String callTakeInput() {
		return SysCall.takeInput();
	}

}