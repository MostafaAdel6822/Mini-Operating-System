
public class Memory {
	private final int memorySize = 40;
	public Object[] memory;
	
	public Memory() {
		this.memory = new Object[memorySize];
	}

	public void print() {
		System.out.println("------------- MEMORY CONTENT ---------");
		System.out.println("--------------Part1------------");
		for(int i=0 ; i<20 ; i++) {
			System.out.print(this.memory[i]+"----");
		}
		System.out.println("");
		System.out.println("--------------Part2------------");
		for(int i=20 ; i<memorySize ; i++) {
			System.out.print(this.memory[i]+"----");
		}
		System.out.println("");

		System.out.println("--------------------------");

	}

	public boolean isFull() {
		return this.memory[0] != null && this.memory[20] != null;
	}

	public boolean isNotFull() {
		return this.memory[0] != null || this.memory[20] != null;
	}
}
