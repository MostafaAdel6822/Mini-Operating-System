
public class PCB {
	public int id ;
    public int pc;
    public ProcessStatus state;
    public int start_loc;
    public int end_loc;
    
	public PCB(int id , ProcessStatus state ) {
		this.pc=0;
		this.id=id;
		this.state=state;
 	}
}
