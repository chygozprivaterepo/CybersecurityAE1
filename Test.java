import java.awt.image.BufferedImage;
import java.io.*;
import java.math.BigInteger;
import java.util.*;

import javax.imageio.ImageIO;

public class Test {
	public static void main (String [] args){
		try {
			String inputName = "colours.bmp";
			String extension = inputName.substring(inputName.lastIndexOf('.')+1);
			FileInputStream fis = new FileInputStream(new File(inputName));
			BufferedInputStream bis = new BufferedInputStream(fis);
			
			FileWriter writer2 = new FileWriter("output2.txt");
			int what;
			ArrayList<Integer> byteArray = new ArrayList<Integer>();
			
			//read binary from file
			while((what = bis.read()) != -1){
				byteArray.add(what);
			}
			
			//write binary to file
			for(Integer i: byteArray){
				//writer.write(Integer.toBinaryString(i));
				//bw.write(Integer.toBinaryString(i));
				writer2.write(String.format("%08d%n",Integer.parseInt(Integer.toBinaryString(i))));
			}
			
			System.out.println("Image width");
			for(int i=18; i<22; i++)
				System.out.println(byteArray.get(i));
			
			
			System.out.println("Image height");
			for(int i=22; i<26; i++)
				System.out.println(byteArray.get(i));
				
			
			fis.close();
			bis.close();
			//bw.close();
			writer2.close();
			
			
			BufferedImage imageIn = ImageIO.read(new File("colours.bmp"));
			int type = imageIn.getType();
			int width = imageIn.getWidth();
			int height = imageIn.getHeight();
			int noOfPixels = width * height;
			System.out.println("Width: " + width + " , Height: " + height);
			ArrayList<Integer> pixels = new ArrayList<Integer>();
			for(int x=0; x<width; x++){
				for(int y=0; y<height; y++)
					pixels.add(imageIn.getRGB(x,y));
			}
			
			FileWriter writer = new FileWriter("output.txt");
			for(Integer p: pixels){
				writer.write(p+"\n");
			}
			
			File output = new File("outputfile."+extension);
			BufferedImage imageOut = new BufferedImage(width, height, type );
			int j = 0;
			for(int x=0; x<width; x++){
				for(int y=0; y<height; y++){
					if(j < noOfPixels){
						imageOut.setRGB(x, y, pixels.get(j).intValue());
						j++;
					}
				}
			}
			ImageIO.write(imageOut, extension, output);
			writer.close();
			
			
			FileWriter writer3 = new FileWriter("output3.txt");
			for(Integer p: pixels){
				writer3.write(Integer.toBinaryString(p)+"\n");
			}
			writer3.close();
			
			FileWriter writer4 = new FileWriter("pixellsbs.txt");
			for(Integer p: pixels){
				writer4.write((p & 0x00000001)+"\n");
			}
			writer3.close();
			/*
			byte b1 = 3;
			byte b2 = 2;
			//byte b4 = (byte)4;
			byte b3 = (byte) (b1 | b2);
			int bitmask = 0x000F & 0x0000;
			System.out.println("The byte is " + b3);
			System.out.println("The bit is " + bitmask);
			*/
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e){
			e.printStackTrace();
		}
	}
}
