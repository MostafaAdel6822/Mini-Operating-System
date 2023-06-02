
public class Process {
	public int pid;
	public int spot;
	public boolean checkFlag;
	public PCB pcb;
	
	public Process(int pid ,PCB pcb) {
		this.spot=0;
		this.pid=pid;
		this.pcb =pcb;
		this.checkFlag=true;
	}
	public String toString() {
		return "Process "+this.pid;
	}

}
