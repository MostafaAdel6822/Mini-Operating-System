import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;
import java.util.Set;
import java.util.Stack;

public class Interpreter {
	public Queue<Process> readyQueue;
	public Queue<Process> blockedQueue;
	public Kernel kernel;
	public int counter;
	public Hashtable<Integer, String> programs; 
	public Set<String> instructions = new HashSet<String>();
	public int timeSlice;
	public int clock;
	public int totaNumOfProcesses = 3;
	public int numOfFinshed = 0;
	public Set<Integer> memory = new HashSet<Integer>();
	public Memory currentMemory;
	public SysCall tempCalls;
	public int memoryPointer = 0;
	public boolean diskEmpty = false;
	public final int id_loc = 0, pc_loc = 2, state_loc = 1, memory_start_loc = 3, memory_end_loc = 4, var_loc = 5, instruction_loc = 8;

	public Interpreter() {
		this.readyQueue = new LinkedList<>();
		this.blockedQueue = new LinkedList<>();
		this.currentMemory = new Memory();
		this.kernel = new Kernel(blockedQueue, readyQueue, currentMemory);
		this.counter = 1;
		this.programs = new Hashtable<>();
		this.timeSlice = 2;
		this.clock = 0;
		this.tempCalls = new SysCall();
		this.instructions.add("print");
		this.instructions.add("assign");
		this.instructions.add("printFromTo");
		this.instructions.add("semWait");
		this.instructions.add("semSignal");
		this.instructions.add("readFile");
		this.instructions.add("WriteFile");
	}

	public void createProcess(String program, int id) throws IOException {
		PCB pcb = new PCB(id, ProcessStatus.READY);
		Process p = new Process(id, pcb);
		int startBound = 0;
		int endBound = 0;
		
		if (this.currentMemory.memory[0] == null) { //check where to insert
			endBound = 19;
			memoryPointer = 20;
			
		} else if (this.currentMemory.memory[20] == null) {
			startBound = 20;
			endBound = 39;
			memoryPointer = Integer.MAX_VALUE;
		}
	
		else { //swap
			String unloadedFromMemory = unloadFromMemory();
			tempCalls.writeFile("Disk", unloadedFromMemory);
			diskEmpty = false;
			startBound = memoryPointer;
			endBound = memoryPointer + 19;
			memoryPointer = Integer.MAX_VALUE;

		}
		pcb.start_loc = startBound;
		pcb.end_loc = endBound;
		memory.add(pcb.id);
		this.readyQueue.add(p);
		this.writePCBToMemory(pcb);
		parseInput(programs.get(clock), pcb, currentMemory);
		System.out.println("----------"+program.substring(4, program.length() - 4) + " becomes a process------------");
		System.out.println("---------- Process " + p.pid + " is loaded to Memory --------------");
	}

	public void writePCBToMemory(PCB pcb) {
		int startBound = pcb.start_loc;
		currentMemory.memory[startBound] = pcb.id;
		currentMemory.memory[startBound + pc_loc] = pcb.pc;
		currentMemory.memory[startBound + state_loc] = ProcessStatus.READY;
		currentMemory.memory[startBound + memory_start_loc] = "Start Bound is " + pcb.start_loc;
		currentMemory.memory[startBound + memory_end_loc] = "End Bound is " + pcb.end_loc;
		currentMemory.memory[startBound + var_loc] = new VarBlock("Empty", "Null");
		currentMemory.memory[startBound + var_loc + 1] = new VarBlock("Empty", "Null");
		currentMemory.memory[startBound + var_loc + 2] = new VarBlock("Empty", "Null");
		
	}

	public void checkProgramArrival() throws IOException {
		if (programs.containsKey(clock)) {
			createProcess(programs.get(clock), counter);
			programs.remove(clock);
			counter++;
		}
	}

	public void scheduler() throws IOException {
	    System.out.println("----------- Execution starts! -----------");

	    while (this.numOfFinshed != totaNumOfProcesses) {
	        checkProgramArrival();
	        if (!readyQueue.isEmpty()) {
	            Process temp = this.readyQueue.poll();
	            boolean found = this.inMemory(temp);
	            if (!found)
	                swap();
	            int processIndex = this.getProcessIndex(temp);
	            temp.pcb.state = ProcessStatus.RUNNING;
	            this.currentMemory.memory[processIndex + state_loc] = ProcessStatus.RUNNING;
	            executeProcess(temp, processIndex);
	        } else {
	            checkProgramArrival();
	            clock++;
	            System.out.println("readyQueue: " + this.readyQueue);
	            System.out.println("blocedQueue: " + this.blockedQueue);
	            System.out.println("-------Clock Time is " + (clock - 1) + "-----------");
	            System.out.println("----------------------------------------------------------------------------------------------------");
	        }
	    }
	    System.out.println("----------- Execution of processes is done! -----------");
	}

	private void executeProcess(Process temp, int processIndex) throws IOException {
	    int pc = 0;
	    for (int i = 0; (i < timeSlice) && (!temp.pcb.state.equals(ProcessStatus.FINISHED))
	            && (!temp.pcb.state.equals(ProcessStatus.BLOCKED)); i++) {
	        System.out.println("process " + temp.pid + " is currently executing");
	        pc = Integer.parseInt(this.currentMemory.memory[processIndex + pc_loc] + "");
	        checkProgramArrival();
	        @SuppressWarnings("unchecked")
	        Stack<String> current = (Stack<String>) (this.currentMemory.memory[pc + instruction_loc + processIndex]);
	        @SuppressWarnings("unchecked")
	        Stack<String> currentInstruction = (Stack<String>) current.clone();
	        String temp1 = current.pop();
	        if (temp1.equals("input") && temp.checkFlag) {
	            System.out.println("Process " + temp.pid + " is taking input ");
	            this.currentMemory.memory[processIndex + var_loc + 2] = "Variable name is temperory value is " + kernel.callTakeInput();
	            temp.checkFlag = (false);
	            this.currentMemory.memory[pc + instruction_loc + processIndex] = currentInstruction;
	        } else {
	            if (this.instructions.contains(current.peek())) {
	                String temp2 = current.pop();
	                if (temp2.equals("readFile") && temp.checkFlag) {
	                    if (!current.peek().isEmpty())
	                        this.currentMemory.memory[processIndex + var_loc
	                                + 2] = "Variable name is temperory value is "
	                                            + kernel.execReadFile(temp2, temp1, temp,
	                                                    processIndex + var_loc);
	                    this.currentMemory.memory[pc + instruction_loc + processIndex] = currentInstruction;
	                    temp.checkFlag = (false);
	                } else if (!temp2.equals("readFile")) {
	                    kernel.execSecondInstruction(temp2, temp1, temp, processIndex + var_loc);
	                    if (current.isEmpty()) {
	                        this.currentMemory.memory[pc + instruction_loc + processIndex] = currentInstruction;
	                        pc++;
	                        temp.pcb.pc = pc;
	                        this.currentMemory.memory[processIndex + pc_loc] = pc;
	                        temp.checkFlag = (true);
	                    }
	                } else {
	                    temp1= this.tempCalls.readFromMemoryStr(processIndex + var_loc + 2, "temperory", currentMemory);
	                    String local = current.pop();
	                    String local2 = current.pop();
	                    kernel.execThirdInstruction(local2, local, temp1, temp, processIndex + var_loc);
	                    if (current.isEmpty()) {
	                        temp.checkFlag = (true);
	                        this.currentMemory.memory[pc + instruction_loc + processIndex] = currentInstruction;
	                        pc++;
	                        temp.pcb.pc = pc;
	                        this.currentMemory.memory[processIndex + pc_loc] = pc;
	                    }
	                }
	            } else {
	                String temp2 = current.pop();
	                String temp3 = current.pop();
	                if (temp3.equals("assign")) {
	                    temp1 = this.tempCalls.readFromMemoryStr(processIndex + var_loc + 2, "temperory", currentMemory);
	                }
	                kernel.execThirdInstruction(temp3, temp2, temp1, temp, processIndex + var_loc);
	                if (current.isEmpty()) {
	                    temp.checkFlag = true;
	                    this.currentMemory.memory[pc + instruction_loc + processIndex] = currentInstruction;
	                    pc++;
	                    temp.pcb.pc = pc;
	                    this.currentMemory.memory[processIndex + pc_loc] = pc;
	                }
	            }
	        }
	        clock++;
	        if (this.currentMemory.memory[pc + instruction_loc + processIndex] == null) {
	            pc = Integer.MAX_VALUE;
	        }
	        if (pc == Integer.MAX_VALUE) {
	            this.numOfFinshed++;
	            temp.pcb.state = ProcessStatus.FINISHED;
	            changePointer(temp);
	            swap();
	            System.out.println("$$$ process " + temp.pid + " is Finished $$$");
	        }
	        System.out.println("readyQueue > " + this.readyQueue);
	        System.out.println("blocedQueue > " + this.blockedQueue);
	        this.currentMemory.print();
	        System.out.println("*** Clock Time is " + (clock - 1) + " ***");
	        System.out.println("----------------------------------------------------------------------------------------------------");
	    }
	    if (!temp.pcb.state.equals(ProcessStatus.BLOCKED) && !(pc == Integer.MAX_VALUE)) {
	        this.readyQueue.add(temp);
	        temp.pcb.state = ProcessStatus.READY;
	        this.currentMemory.memory[processIndex + state_loc] = ProcessStatus.READY;
	        System.out.println("process " + temp.pid + " return back from running to ready queue");
	    }
	}


	public boolean inMemory(Process p) {
		return memory.contains(p.pid);
	}

	public void swap() throws IOException {
		if (this.currentMemory.isFull()) {
			this.reload();
		} else if (this.currentMemory.isNotFull()) {
			this.load(memoryPointer);
		}

	}

	public void reload() throws IOException {
		String unloadedFromMemory = unloadFromMemory();
		load(memoryPointer);
		this.tempCalls.writeFile("Disk", unloadedFromMemory);
		this.diskEmpty = false;
		System.out.println("************ Process " + unloadedFromMemory.charAt(0) + " is written on Disk ************ ");

	}

	public void load(int i) throws IOException {
		int endBound = i + 19;
		if (!diskEmpty) {
			parseInputString("src/Disk.txt", i, currentMemory);
			this.currentMemory.memory[i + 3] = "Start Bound is " + i;
			this.currentMemory.memory[i + 4] = " End Bound is " + endBound;
			memory.add(Integer.parseInt(this.currentMemory.memory[i] + ""));
			System.out.println("************ Process " + Integer.parseInt(this.currentMemory.memory[i] + "") + " is loaded to Memory ************ ");
			this.memoryPointer = Integer.MAX_VALUE;
			this.tempCalls.writeFile("Disk", "EMPTY");
			this.diskEmpty = true;
		}
	}

	public String unloadFromMemory() throws IOException {
		String unloaded = "";
		int startBound = 0;
		if (!this.currentMemory.memory[state_loc].equals(ProcessStatus.RUNNING))
			startBound = 0;
		else
			startBound = 20;
		memory.remove(Integer.parseInt(this.currentMemory.memory[startBound] + ""));
		System.out.println("************ Process " + this.currentMemory.memory[startBound + id_loc]
				+ " is swaped from  Memory ************");
		for (int i = startBound; i <= startBound + 19 && this.currentMemory.memory[i] != null; i++) {
			if (i >= startBound + instruction_loc) {
				@SuppressWarnings("unchecked")
				Stack<String> y = (Stack<String>) this.currentMemory.memory[i];
				unloaded += printStack(y);
			} else {
				if (i == startBound + state_loc) {
					if (this.currentMemory.memory[i].equals(ProcessStatus.READY))
						unloaded += "STATUS IS READY" + "\n";
					else
						unloaded += "STATUS IS BLOCKED" + "\n";
				} else
					unloaded += this.currentMemory.memory[i] + "\n";
			}

			this.currentMemory.memory[i] = null;
		}
		this.memoryPointer = startBound;
		return unloaded;

	}

	public void changePointer(Process p) throws IOException {
		int startBound = 0;
		if (this.currentMemory.memory[id_loc] != null && p.pid == Integer.parseInt(this.currentMemory.memory[id_loc] + ""))
			startBound = 0;
		else
			startBound = 20;
		memory.remove(Integer.parseInt(this.currentMemory.memory[startBound] + ""));
		for (int i = startBound; i <= startBound + 19; i++) {
			this.currentMemory.memory[i] = null;
		}
		this.memoryPointer = startBound;
	}

	public int getProcessIndex(Process p) {
		if (this.currentMemory.memory[id_loc] != null && p.pid == Integer.parseInt(this.currentMemory.memory[id_loc] + ""))
			return 0;
		else
			return 20;
	}

	public static String printStack(Stack<String> s) {
		String temp = "";
		while (!s.isEmpty()) {
			temp = s.pop() + " " + temp;
		}
		temp += "\n";
		return temp;
	}

	public void parseInput(String fileName, PCB pcb, Memory m) throws FileNotFoundException {
		File file = new File(fileName);
		Scanner sc = new Scanner(file);
		String line;
		int counter = pcb.start_loc + 8;
		
		while (sc.hasNext()) {
			Stack<String> s = new Stack<>();
			line = sc.nextLine();
			String[] currLine = line.split(" ");
			for (int i = 0; i < currLine.length; i++) {
				s.push(currLine[i]);
			}
			m.memory[counter] = s;
			counter++;

		}
		sc.close();

	}

	public void parseInputString(String fileName, int loadPos, Memory m) throws FileNotFoundException {
		File file = new File(fileName);
		Scanner sc = new Scanner(file);
		String line;
		int counter = loadPos;
		while (sc.hasNext()) {
			if (counter > loadPos + 7) {
				Stack<String> temp = new Stack<>();
				line = sc.nextLine();
				String stringBuilder[] = line.split(" ");
				for (int i = 0; i < stringBuilder.length; i++) {
					temp.push(stringBuilder[i]);
				}
				m.memory[counter] = temp;
			}

			else if (counter == loadPos + 1) {
				line = sc.nextLine();
				if (line.equals("STATUS IS READY")) {
					m.memory[counter] = ProcessStatus.READY;
				} else if (line.equals("STATUS IS BLOCKED"))
					m.memory[counter] = ProcessStatus.BLOCKED;
			} else if (counter == loadPos || counter == loadPos + 2) {
				line = sc.nextLine();
				if (line.equals(null)) {
					checkEmpty(loadPos, m);
					break;
				}
				m.memory[counter] = Integer.parseInt(line);
			} else {
				m.memory[counter] = sc.nextLine();
			}

			counter++;

		}
		sc.close();

	}

	public void checkEmpty(int start, Memory m) {
		for (int i = start; i < start + 20; i++) {
			m.memory[i] = null;
		}
	}

	public static void main(String[] args) throws IOException {
		Scanner sc = new Scanner(System.in);
		Interpreter interpreter = new Interpreter();
		String program1 = "src/Program_1.txt";
		String program2 = "src/Program_2.txt";
		String program3 = "src/Program_3.txt";
		System.out.println("Time Slice Value?");
		interpreter.timeSlice = sc.nextInt();
		System.out.println("Arrival of first program?");
		interpreter.programs.put(sc.nextInt(), program1);
		System.out.println("Arrival of second program?");
		interpreter.programs.put(sc.nextInt(), program2);
		System.out.println("Arrival of third program?");
		interpreter.programs.put(sc.nextInt(), program3);
		interpreter.scheduler();
		sc.close();
	}
}
