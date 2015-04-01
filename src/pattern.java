import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.LinkedList;
import java.util.TreeMap;

class PatternNode {

	public PatternNode(String pattern) {
		this.pattern = pattern;
		index = 0;
	}

    //返回模式串当前的字符
	public char getChar() {
		if (index >= pattern.length()) {
			index = 0;
			return '\0';
		}
		return pattern.charAt(index++);
	}

	public String getPattern() {
		return pattern;
	}

	public String toString() {
		return pattern;
	}

	private String pattern;
	private int index;
}

class PatternSet {
	public PatternSet(String filename) {
		patternList = new LinkedList<PatternNode>();
        //统计模式串的长度分布信息
		sdPatternLen = new TreeMap<Integer, Integer>();

		this.filename = filename;
		maxPatternLen = 0;
		minPatternLen = 0;
		totalPatternLen = 0;
		totalPatternNum = 0;
		readPatternFile();
	}

	public int getMaxPatternLen() {
		return maxPatternLen;
	}
/*
	public String getPatternMd5(){
		StringBuilder buffer = new StringBuilder();
		for(PatternNode node : patternList){
			buffer.append(node.getPattern());
		}
		String str = buffer.toString();
		return test.stringMD5(str);
	}
*/

	public boolean readPatternFile() {
		Reader reader = null;
		try {
			File file = new File(filename);
			reader = new InputStreamReader(new FileInputStream(file));
			int tempchar;
			StringBuilder buffer = new StringBuilder();
			while ((tempchar = reader.read()) != -1) {
				char ch = (char) tempchar;
				buffer.append(ch);
				if (ch == '\n') {
					String str = buffer.toString();
					if(str.equals("\r\n")){
						continue;
					}
					PatternNode patternNode = new PatternNode(buffer.toString());
					patternList.add(patternNode);
					int len = patternNode.getPattern().length();
					++totalPatternNum;
					totalPatternLen += len;
					if (minPatternLen == 0) {
						minPatternLen = len;
					}
					minPatternLen = len < minPatternLen ? len : minPatternLen;
					maxPatternLen = len > maxPatternLen ? len : maxPatternLen;

					if (sdPatternLen.containsKey(len)) {
						int num = sdPatternLen.get(len);
						sdPatternLen.put(len, num + 1);
					} else {
						sdPatternLen.put(len, 1);
					}
					buffer.delete(0, buffer.length());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}

	public LinkedList<PatternNode> getPatternList() {
		return patternList;
	}

	private LinkedList<PatternNode> patternList;
	private String filename;
	private int maxPatternLen;
	private int minPatternLen;
	private int totalPatternLen;
	private int totalPatternNum;
	private TreeMap<Integer, Integer> sdPatternLen;// 统计串长度分布（采用平衡查找树实现，key为串长度，value为串数量）
}
