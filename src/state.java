import java.util.ArrayList;
import java.util.HashMap;

//状态类，每个状态包含了失效跳转节点的索引信息，当前状态的goto表信息, 当前状态的failure表信息
class State{
	public State(char stateChar){
		this.stateChar = stateChar;
		gotoTable = new HashMap<Character, Integer>();
		outputList = new ArrayList<PatternNode>();
	}

	public void addPattern(PatternNode pattern){
		outputList.add(pattern);
	}
	public ArrayList<PatternNode> getPattern(){
		return outputList;
	}

	public char getStateChar(){
		return stateChar;
	}

	public int getFailIndex(){
		return failIndex;
	}
	public void setFailIndex(int index){
		failIndex = index;
	}

	public int getNextState(char ch){
		int nextState;
		try{
			nextState = gotoTable.get(ch);
		}
		catch(Exception e){
			nextState = -1;
		}
		return nextState;
	}
	//往goto表中添加元素
	public void addGotoState(char nextChar, int nextState){
		gotoTable.put(nextChar, nextState);
	}

	public HashMap<Character, Integer> getGotoTable(){
		return gotoTable;
	}

	private int failIndex;//失效后跳转的状态
	private char stateChar;//表示状态代表的字符
	private HashMap<Character, Integer> gotoTable;//goto表(key 为字符，value为下一个状态的索引)
	private ArrayList<PatternNode> outputList;//匹配模式串输出列表
}
