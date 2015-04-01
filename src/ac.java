import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.locks.ReentrantLock;

//自定义MyEntry类，用于存储匹配的模式串信息，
//key代表发现匹配模式串的起始位置，value代表匹配模式串的内容，这两个元素唯一确定匹配的模式串信息
class MyEntry implements Map.Entry<Integer, String>, Comparable<MyEntry>{
	public MyEntry(Integer key, String value) {
		this.key = key;
		this.value = value;
	}

	@Override
	public Integer getKey() {
		return key;
	}

	@Override
	public String setValue(String value) {
		String oldValue = value;
		this.value = value;
		return oldValue;
	}

	@Override
	public String getValue() {
		return value;
	}

	@Override
	public int hashCode() {
		int result = 17;
		Integer m = key;
		result = 31 * result + m.hashCode();
		result = 31 * result + value.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		// 如果是同一个对象返回true，反之返回false
		if (this == obj) {
			return true;
		}
		// 判断是否类型相同
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		MyEntry entry = (MyEntry) obj;
		return this.key == entry.getKey()
		        && this.value.equals(entry.getValue());
	}
	public int compareTo(MyEntry o){
		int t = o.getKey();
		if(t == key){
			return 0;
		}
		else if(t < key){
			return 1;
		}
		else{
			return -1;
		}
	}
	private int key;
	private String value;
}

// 记录下每个线程的运行性能
class ThreadRecord {
	public int textLen = 0;// 线程中的文本长度
	public int traceBack = 0;// 线程的回溯次数
	public long matchTime = 0;// 线程的匹配时间
	public int hitNum = 0;//匹配文本串数量
}

class AC {
	public AC(String patternFilename) {
		stateArray = new ArrayList<State>();
		patternSet = new PatternSet(patternFilename);
		threadRecord = new ArrayList<ThreadRecord>();
		threadRecordLock = new ReentrantLock();
		hitPatterns = new HashSet<MyEntry>();
		hitPatternsLock = new ReentrantLock();
		// 插入初始化状态
		char empty = '\0';
		addState(empty);
	}

	public void Init() {
		createGoto();
		createFail();
	}

	public void match(ArrayList<Character> charBuffer, int start, int length) {
		ThreadRecord record = new ThreadRecord();
		State state = stateArray.get(0);// 获取初始状态节点
		long startTime = System.currentTimeMillis();
		length = length + patternSet.getMaxPatternLen();
		for (int i = start; i <= start + length && i < charBuffer.size(); ++i) {
			char ch = charBuffer.get(i);
			++record.textLen;
			boolean outFlag = false;
			// 如果匹配失效
			while (state.getNextState(ch) == -1) {
				// 如果是第一个状态，则继续读入文本
				if (state == stateArray.get(0)) {
					outFlag = true;
					break;
				}
				// 否则回溯
				state = stateArray.get(state.getFailIndex());
				++record.traceBack;
			}
			if (outFlag) {
				continue;
			}
			state = stateArray.get(state.getNextState(ch));
			ArrayList<PatternNode> patterns = state.getPattern();

			// 将匹配结果添加到结果中
			if (patterns.isEmpty() == false) {
				hitPatternsLock.lock();
				record.hitNum += patterns.size();
				for (PatternNode patternNode : patterns) {
					hitPatterns.add(new MyEntry(i, patternNode.getPattern()));
				}
				hitPatternsLock.unlock();
			}
		}
		long endTime = System.currentTimeMillis();
		record.matchTime = endTime - startTime;

		// 将线程运行信息加入到结果中
		System.out.println("线程结束运行");
		threadRecordLock.lock();
		threadRecord.add(record);
		threadRecordLock.unlock();
	}

	public void setTotalMatchTime(long matchTime) {
		this.totalMatchTime = matchTime;
	}

	public void printResult(String filename) {
		ArrayList<MyEntry> array = new ArrayList<MyEntry>(hitPatterns);
		Collections.sort(array);

		OutputStreamWriter writer = null;
		try {
			writer = new OutputStreamWriter(new FileOutputStream(new File(
			        filename)));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		BufferedWriter wr = new BufferedWriter(writer);

		try {
			int threadNum = threadRecord.size();
			wr.append("自动机包含的状态总数为：" + count + "\r\n");
			wr.append("创建fail表花费时间: " + createFailTime + "ms" + "\r\n");
			wr.append("创建goto表花费时间: " + createGotoTime + "ms" + "\r\n");
			wr.append("线程数量为" + threadNum + "的情况下的匹配的时间为:" + totalMatchTime
			        + "ms" + "\r\n");
			wr.append("\r\n");
			for (int i = 0; i < threadRecord.size(); ++i) {
				wr.append("在线程" + i + "中，匹配的信息为：" + "\r\n");
				wr.append("线程匹配的文本长度为:" + threadRecord.get(i).textLen + "\r\n");
				wr.append("线程回溯的次数为：" + threadRecord.get(i).traceBack + "\r\n");
				wr.append("线程匹配到的模式串数量为:" + threadRecord.get(i).hitNum + "\r\n");
				wr.append("线程匹配的时间为:" + threadRecord.get(i).matchTime + "ms"
				        + "\r\n");
				wr.append("\r\n");
			}
			wr.append("匹配的模式串为：" + "\r\n");
			for(MyEntry entry : array){
				wr.append(entry.getValue());
			}
			wr.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				wr.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void createGoto() {
		long startTime = System.currentTimeMillis();
		LinkedList<PatternNode> patterns = patternSet.getPatternList();
		for (PatternNode node : patterns) {
			enter(node);
		}
		long endTime = System.currentTimeMillis();
		createGotoTime = endTime - startTime;
	}

	private int addState(char stateChar) {
		State state = new State(stateChar);
		stateArray.add(state);
		++count;
		return stateArray.indexOf(state);
	}

	private void enter(PatternNode node) {
		char stateChar = node.getChar();
		int index = 0;
		State state = stateArray.get(index);

		while (stateChar != '\0') {
			// 查看字符哈希表中是否存在当前字符，如果不存在则构造
			int nextIndex = 0;
			nextIndex = state.getNextState(stateChar);
			if (nextIndex == -1) {
				nextIndex = addState(stateChar);
				state.addGotoState(stateChar, nextIndex);

			}
			index = nextIndex;
			state = stateArray.get(index);
			stateChar = node.getChar();
		}
		state.addPattern(node);
	}

	private void createFail() {
		long startTime = System.currentTimeMillis();
		// 广度搜索队列
		Queue<Integer> stateQueue = new LinkedList<Integer>();

		// 处理深度为1的状态
		State state = stateArray.get(0);
		HashMap<Character, Integer> nextStates = state.getGotoTable();
		Iterator<Entry<Character, Integer>> iter = nextStates.entrySet()
		        .iterator();
		while (iter.hasNext()) {
			Entry<Character, Integer> entry = iter.next();
			stateQueue.offer(entry.getValue());
			stateArray.get(entry.getValue()).setFailIndex(0);
		}

		// 广度优先遍历状态机
		while (stateQueue.isEmpty() == false) {
			int stateIndex = stateQueue.poll();
			state = stateArray.get(stateIndex);
			nextStates = state.getGotoTable();
			iter = nextStates.entrySet().iterator();
			while (iter.hasNext()) {
				Entry<Character, Integer> entry = iter.next();
				State nextState = stateArray.get(entry.getValue());
				char ch = entry.getKey();
				// 先将下一个状态入队
				stateQueue.offer(entry.getValue());

				// 先获取当前结点的失败索引
				int curFail = state.getFailIndex();
				State failState = stateArray.get(curFail);

				// 失败节点能够走得通就走，否则回退
				int gotoIndex = 0;
				while ((gotoIndex = failState.getNextState(ch)) == -1) {
					// 对于状态0而言，对所有字符的失败节点都指向自己，所以循环肯定会终止
					if (failState == stateArray.get(0)) {
						gotoIndex = 0;
						break;
					}
					// 回退
					failState = stateArray.get(failState.getFailIndex());
				}

				// 设置下一个状态节点的失败节点
				nextState.setFailIndex(gotoIndex);
				ArrayList<PatternNode> patternList = stateArray.get(gotoIndex)
				        .getPattern();

				// 合并output pattern
				for (Iterator<PatternNode> patternIter = patternList.iterator(); patternIter
				        .hasNext();) {
					nextState.addPattern(patternIter.next());
				}
			}
		}
		long endTime = System.currentTimeMillis();
		createFailTime = endTime - startTime;
	}

	private ArrayList<State> stateArray;// 存储状态的数组
	private PatternSet patternSet;// 模式串集合
	private int count = 1;// AC状态机包含的状态总数
	private long createGotoTime;// 创建goto表时间
	private long createFailTime;// 创建fail表时间
	private long totalMatchTime;// 整体的匹配时间
	private ArrayList<ThreadRecord> threadRecord;// 线程运行信息
	private ReentrantLock threadRecordLock;// 线程记录锁
	private HashSet<MyEntry> hitPatterns;// 匹配的模式串，其中key为模式串的起始位置，value为模式串的内容，从而避免重复匹配
	private ReentrantLock hitPatternsLock;// 多个线程会并发访问hitPatterns
}
