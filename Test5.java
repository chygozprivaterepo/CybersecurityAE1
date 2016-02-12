import java.io.*;
import java.util.*;

public class Test5 {
	public static void main (String [] args){
		try {
			String input = "colours.bmp";
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(input));
			List<Integer> payloadbinary = new ArrayList<Integer>();
			
			for(;;){
				int c = bis.read();
				if (c == -1)
					break;
				else{
					payloadbinary.add(c);
				}
			}
			bis.close();
			for(Integer i: payloadbinary){
				if(i != null)
					System.out.print(i + " ");
			}
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(getBaseName(input) + "_output." + getExtension(input)));
			for(Integer i: payloadbinary){
				if(i != null){
					bos.write(i);
				}
			}
			bos.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static String getExtension(String fname){
		int i = fname.lastIndexOf('.');
		String ext = fname.substring(i+1);
		return ext;
	}
	
	private static String getBaseName(String fname){
		int i = fname.lastIndexOf('.');
		return fname.substring(0, i);
	}
}
