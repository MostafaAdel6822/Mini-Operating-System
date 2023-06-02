import java.util.LinkedList;
import java.util.Queue;

public class Mutex {
	private Queue<Process> blockedOfUserInput;
	private Queue<Process> blockedOfUserOutput;
	private Queue<Process> blockedOfFile;
	private boolean userInput;
	private boolean userOutput;
	private boolean file;
	private int currentOwner[];
	private Queue<Process> blockedQueue;
	private Queue<Process> readyQueue;
	private Memory currentMemory;

	public Mutex(Queue<Process> blockedQueue, Queue<Process> readyQueue, Memory currentMemory) {
		this.blockedOfFile = new LinkedList<>();
		this.blockedOfUserInput = new LinkedList<>();
		this.blockedOfUserOutput = new LinkedList<>();
		this.userInput = true;
		this.userOutput = true;
		this.file = true;
		this.currentOwner = new int[3];
		this.readyQueue = readyQueue;
		this.blockedQueue = blockedQueue;
		this.currentMemory = currentMemory;

	}

	public void semWait(String instruction, Process p) {
		if(instruction.equals("userInput")) {
			if (userInput) {
				currentOwner[0] = p.pid;
				userInput = false;
			} else {
				blockedOfUserInput.add(p);
				this.blockedQueue.add(p);
				System.out.println("Process " + p.pid + " is now blocked and waiting for User UserInput");
				System.out.println("Current blocked processes waiting for UserInput: " + this.blockedOfUserOutput);
				p.pcb.state = ProcessStatus.BLOCKED;
				currentMemory.memory[p.pcb.start_loc + 1] = ProcessStatus.BLOCKED;
			}
		}else if(instruction.equals("userOutput")) {
			if (userOutput) {
				currentOwner[1] = p.pid;
				userOutput = false;
			}else{
				blockedOfUserOutput.add(p);
				this.blockedQueue.add(p);
				System.out.println("Process " + p.pid + " is now blocked and waiting for UserOutput");
				System.out.println("Current blocked processes waiting for UserOutput: " + this.blockedOfUserOutput);
				p.pcb.state = ProcessStatus.BLOCKED;
				currentMemory.memory[p.pcb.start_loc + 1] = ProcessStatus.BLOCKED;

			}
		}else if(instruction.equals("file")) {
			if (file) {
				currentOwner[2] = p.pid;
				file = false;
			} else {
				blockedOfFile.add(p);
				this.blockedQueue.add(p);
				System.out.println("Process " + p.pid + " is now blocked and waiting for File");
				System.out.println("Current blocked processes waiting for File: " + this.blockedOfUserOutput);
				p.pcb.state = ProcessStatus.BLOCKED;
				currentMemory.memory[p.pcb.start_loc + 1] = ProcessStatus.BLOCKED;

			}
		}
		
	}

	public void semSignal(String arg, int pid) {
		if (pid == currentOwner[0] && arg.equals("userInput")) {
			if (!blockedOfUserInput.isEmpty()) {
				currentOwner[0] = blockedOfUserInput.peek().pid;
				readyQueue.add(blockedOfUserInput.peek());

				System.out.println("Process removed from blocked queue:" + blockedOfUserInput.peek().pid + "");
				deleteFromQueue(blockedQueue, blockedOfUserInput.peek().pid);
				this.blockedOfUserInput.poll();

			} else
				userInput = true;
		} else if (pid == currentOwner[1] && arg.equals("userOutput")) {
			if (!blockedOfUserOutput.isEmpty()) {
				currentOwner[1] = blockedOfUserOutput.peek().pid;
				readyQueue.add(blockedOfUserOutput.peek());
				System.out.println("Process removed from blocked queue:" + blockedOfUserOutput.peek().pid + "");
				deleteFromQueue(blockedQueue, blockedOfUserOutput.peek().pid);
				this.blockedOfUserOutput.poll();
			}

			else
				userOutput = true;
		} else if (pid == currentOwner[2] && arg.equals("file")) {
			if (!blockedOfFile.isEmpty()) {
				currentOwner[2] = blockedOfFile.peek().pid;
				readyQueue.add(blockedOfFile.peek());
				System.out.println("Process removed from blocked queue:" + blockedOfFile.peek().pid + "");
				deleteFromQueue(blockedQueue, blockedOfFile.peek().pid);
				this.blockedOfFile.poll();
			} else
				file = true;
		}

	}

	public static void deleteFromQueue(Queue<Process> x, int pid) {
		Queue<Process> temp = new LinkedList<>();
		while (!x.isEmpty()) {
			Process y = x.poll();
			if (!(y.pid == pid))
				temp.add(y);
		}
		while (!temp.isEmpty()) {
			x.add(temp.poll());
		}
	}
}