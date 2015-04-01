import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class test {
	public static void main(String[] args) {
		AC ac = new AC("../test/pattern.txt");
		ac.Init();
		ArrayList<Character> charBuffer = new ArrayList<Character>();
		readText(charBuffer, "../test/text.txt");

		int chunkLen = charBuffer.size() / test.threadNum;

		ArrayList<Thread> threads = new ArrayList<Thread>();
		for (int i = 0; i < test.threadNum; ++i) {
			MyTask task = new MyTask(ac, charBuffer, chunkLen * i, chunkLen);
			threads.add(new Thread(task));
		}

		long startTime = System.currentTimeMillis();
		for (Thread thread : threads) {
			thread.start();
		}

		try {
			for (Thread thread : threads) {
				thread.join();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		long endTime = System.currentTimeMillis();
		long matchTime = endTime - startTime;
		ac.setTotalMatchTime(matchTime);
		ac.printResult("../test/result.txt");
		System.out.println("结束运行");
	}

	public static void readText(ArrayList<Character> charBuffer, String filename) {
		Reader reader = null;
		try {
			File file = new File(filename);
			reader = new InputStreamReader(new FileInputStream(file));
			int tempchar;
			while ((tempchar = reader.read()) != -1) {
				char ch = (char) tempchar;
				charBuffer.add(ch);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
/*
	public static String stringMD5(String input) {
		try {
			// 拿到一个MD5转换器（如果想要SHA1参数换成”SHA1”）
			MessageDigest messageDigest = MessageDigest.getInstance("MD5");
			// 输入的字符串转换成字节数组
			byte[] inputByteArray = input.getBytes();
			// inputByteArray是输入字符串转换得到的字节数组
			messageDigest.update(inputByteArray);
			// 转换并返回结果，也是字节数组，包含16个元素
			byte[] resultByteArray = messageDigest.digest();
			// 字符数组转换成字符串返回
			return byteArrayToHex(resultByteArray);
		} catch (NoSuchAlgorithmException e) {
			return null;
		}
	}

	public static String byteArrayToHex(byte[] byteArray) {
		// 首先初始化一个字符数组，用来存放每个16进制字符
		char[] hexDigits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
		        'A', 'B', 'C', 'D', 'E', 'F' };
		// new一个字符数组，这个就是用来组成结果字符串的（解释一下：一个byte是八位二进制，也就是2位十六进制字符（2的8次方等于16的2次方））
		char[] resultCharArray = new char[byteArray.length * 2];
		// 遍历字节数组，通过位运算（位运算效率高），转换成字符放到字符数组中去
		int index = 0;
		for (byte b : byteArray) {
			resultCharArray[index++] = hexDigits[b >>> 4 & 0xf];
			resultCharArray[index++] = hexDigits[b & 0xf];
		}
		// 字符数组组合成字符串返回
		return new String(resultCharArray);
	}
*/
	public static final int threadNum = 1;
}

class MyTask implements Runnable {
	private AC ac;
	private int startPos;
	private int length;
	private ArrayList<Character> charBuffer;

	public MyTask(AC ac, ArrayList<Character> charBuffer, int startPos,
	        int length) {
		this.ac = ac;
		this.charBuffer = charBuffer;
		this.startPos = startPos;
		this.length = length;
	}

	public void run() {
		ac.match(charBuffer, startPos, length);
	}
}
