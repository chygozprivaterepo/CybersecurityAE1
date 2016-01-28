import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

import javax.imageio.ImageIO;

public class Test2 {
	
	public static void main (String [] args){
		
		//String payload = "Now let us hide some stuff";
		String payload = "this thing gotta work now or else i'm going to be really pissed";
		hideString(payload,"colours.bmp");
		System.out.println("Extracting...");
		extractString("stegoimage.bmp");
	}
	
	public static void hideString(String payload, String cover_filename){
		String binaryPayload = stringToBinary(payload);
		ArrayList<Integer> pixels = getImagePixels(cover_filename);
		int noOfPixels = getImageWidth(cover_filename) * getImageHeight(cover_filename);
		int nrb = noOfRequiredBytes(binaryPayload);
		if(nrb > noOfPixels * 3){
			System.out.println("The chosen image does not have sufficient pixels to hide data");
			return;
		}
	
		String bitsForSize = String.format("%032d",Integer.parseInt(Integer.toBinaryString(binaryPayload.length())));
		System.out.println("Before extraction payload: "+binaryPayload);
		//System.out.println(bitsForSize);
		//String bitsForSize = String.format("%032d",Integer.parseInt(stringToBinary(binaryPayload.length()+"")));
		String dataToHide = bitsForSize + binaryPayload;
		//System.out.println(dataToHide);
		//System.out.println(binaryPayload.length());

		int i = 0, j=0;

		while(j < noOfPixels){
			
			if(i == dataToHide.length()){
				break;
			}
			
			int aa = 255;
			Integer in = pixels.get(j);
			int red = (in >> 16) & 0x000000FF;
			int green = (in >> 8) & 0x000000FF;
			int blue = in & 0x000000FF;
			
			if(i < dataToHide.length()-1){
				int bitToHide1 = Integer.parseInt(dataToHide.charAt(i)+"");
				int newRed = flipBit(bitToHide1, red);
				aa = (aa << 8) + newRed;
				i++;
			}
			else{
				int bitToHide1 = Integer.parseInt(dataToHide.charAt(i)+"");
				int newRed = flipBit(bitToHide1, red);
				aa = (((aa << 8) + newRed) << 16) + (in & 0x0000FFFF);
				
			}
		
			if(i < dataToHide.length()-1){
				int bitToHide2 = Integer.parseInt(dataToHide.charAt(i)+"");

				int newGreen = flipBit(bitToHide2, green);
				aa = (aa << 8) + newGreen;
				i++;
			}
			else{
				int bitToHide2 = Integer.parseInt(dataToHide.charAt(i)+"");
				int newGreen = flipBit(bitToHide2, green);
				aa = (((aa << 8) + newGreen ) << 8) + (in & 0x000000FF);
				
			}
				
			if(i < dataToHide.length()-1){
				int bitToHide3 = Integer.parseInt(dataToHide.charAt(i)+"");
				int newBlue = flipBit(bitToHide3, blue);
				aa = (aa << 8) + newBlue;
				i++;
			}
			
			pixels.set(j, aa);
			j++;
		}	
		
		saveStegoImage("colours.bmp",pixels);	
		
	}
	
	public static void extractString(String stego_image){
		ArrayList<Integer> pixels = getImagePixels(stego_image);
		int noOfPixels = getImageWidth(stego_image) * getImageHeight(stego_image);
		int i = 0;
		String sizeString = "";
		while(i < 11){
			if(i < 10){
				int red = (pixels.get(i) >> 16) & 0x000000FF;
				sizeString += getLSB(red);
				int green = (pixels.get(i) >> 8) & 0x000000FF;
				sizeString += getLSB(green);
				int blue = pixels.get(i) & 0x000000FF;
				sizeString += getLSB(blue);
			}
			else{
				int red = (pixels.get(i) >> 16) & 0x000000FF;
				sizeString += getLSB(red);
				int green = (pixels.get(i) >> 8) & 0x000000FF;
				sizeString += getLSB(green);
			}
			i++;
		}
	
		int payloadSize = Integer.parseInt(sizeString,2);

		int j = 10, k = 0;
		System.out.println(String.format("Pixelno: %s, PayloadSize: %s", noOfPixels, payloadSize));
		String payloadBinary = "";
		
		while(j < noOfPixels){
			
			if(k < payloadSize){
				if(j == 10){
					int blue = pixels.get(j) & 0x000000FF;
					payloadBinary += getLSB(blue);
					k++;
				}
				else{
					if(k < payloadSize){
						int red = (pixels.get(j) >> 16) & 0x000000FF;
						payloadBinary += getLSB(red);
						k++;
					}
					
					if(k < payloadSize){
						int green = (pixels.get(j) >> 8) & 0x000000FF;
						payloadBinary += getLSB(green);
						k++;
					}
					
					if(k < payloadSize){
						int blue = pixels.get(j) & 0x000000FF;
						payloadBinary += getLSB(blue);
						k++;
					}
				}
				j++;
			}
			else 
				break;
		}
		System.out.println("after extraction payload:  "+payloadBinary);
		String payload = binaryToString(payloadBinary);
		System.out.print("The payload is: " + payload);
		
		
	}
	
	private static ArrayList<Integer> getImagePixels (String imageName){
		ArrayList<Integer> pixels = new ArrayList<Integer>();
	
		BufferedImage imageIn = null;
		try {
			imageIn = ImageIO.read(new File(imageName));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int width = imageIn.getWidth();
		int height = imageIn.getHeight();

		for(int x=0; x<width; x++){
			for(int y=0; y<height; y++)
				pixels.add(imageIn.getRGB(x,y));
		}
		return pixels;
	}
	
	/**
	 * method to get the width of image in pixels
	 * @param imageName the name of the image
	 * @return the image width
	 * @throws IOException
	 */
	private static int getImageWidth(String image){
		BufferedImage imageIn = null;
		try {
			imageIn = ImageIO.read(new File(image));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return imageIn.getWidth();
	}
	
	/**
	 * method to get the height of image in pixels
	 * @param imageName the name of the image
	 * @return the image height
	 * @throws IOException
	 */
	private static int getImageHeight(String image){
		BufferedImage imageIn = null;
		try {
			imageIn = ImageIO.read(new File(image));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return imageIn.getHeight();
	}
	
	/**
	 * method to get the type of image. The type could be TYPE_INT_RGB, TYPE_INT_ARGB, etc
	 * @param image to be worked on
	 * @return the image type
	 * @throws IOException
	 */
	private static int getImageType(String image) throws IOException{
		BufferedImage imageIn = ImageIO.read(new File(image));
		return imageIn.getType();
	}
	
	/**
	 * method to save the stego image
	 * @param image to be saved
	 * @param pixels the list containing the pixels with payload data embedded
	 */
	private static void saveStegoImage(String image, ArrayList<Integer> pixels){
		int index = image.lastIndexOf('.');
		String extension = image.substring(index+1);
		File output = new File("stegoimage."+extension);
		
		try{
			int width = getImageWidth(image);
			int height = getImageHeight(image);
			int type = getImageType(image);
			
			int noOfPixels = width * height;
			BufferedImage imageOut = new BufferedImage(width, height, type);
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
		}
		catch(IOException io){
			io.printStackTrace();
		}
		
	}
	
	/**
	 * method to return the LSB of a byte
	 * @param component the byte whose LSB is sought
	 * @return the LSB
	 */
	private static int getLSB(int component){
		return component & 0x00000001;
	}
	
	/**
	 * method to get the binary equivalent of a string
	 * @param s the string to be converted
	 * @return the binary equivalent
	 */
	private static String stringToBinary(String s){
		String bin = "";
		for(int i=0; i<s.length(); i++)
		{
			int bch = Integer.parseInt(Integer.toBinaryString((int)(s.charAt(i))));
			bin += String.format("%08d", bch);
		}
		return bin;
	}
	
	/**
	 * method to convert a binary string to a string of characters
	 * @param bin the binary string to be converted
	 * @return the string representation
	 */
	private static String binaryToString(String bin){
		String s = "";
		for(int i=0; i<bin.length(); i=i+8){
			String byt = bin.substring(i, i+8);
			s += (char)Integer.parseInt(byt, 2);
		}
		return s;
	}
	/**
	 * method to get the total number of bytes required to store data related to the payload.
	 * This data includes the payload binary bits and a set of bits to hold the number of bits in the payload
	 * @param bin the payload in binary
	 * @return the number of bytes required
	 */
	private static int noOfRequiredBytes(String bin){
		int noOfBitsInPayload = bin.length();
		int noOfBitsForPayloadBinarySize = stringToBinary(noOfBitsInPayload+"").length();
		return noOfBitsInPayload + noOfBitsForPayloadBinarySize;
	}
	
	/**
	 * method to get the total number of bits that make up the payload binary string
	 * @param s the total binary string extracted from stego image
	 * @return the payload size
	 */
	private static int getPayloadSizeFromStegoString(String s){
		String r = "";
		for(int i=0; i<32; i++)
			r+=s.charAt(i);
		int size = Integer.parseInt(r,2);
		return size;
	}
	
	/**
	 * method to get the payload data from the stego image
	 * @param s the total binary string extracted from stego image
	 * @return the payload binary data
	 */
	private static String getPayloadDataFromStegoString(String s){
		int payloadSize = getPayloadSizeFromStegoString(s);
		return binaryToString(s.substring(32, 32+payloadSize));
	}
	
	/**
	 * method to return a byte whose LSB has been flipped to contain a bit of the payload binary data
	 * @param b the payload binary data bit
	 * @param byt the byte to be flipped
	 * @return
	 */
	private static int flipBit(int b,int byt){
		int lsb = getLSB(byt);
		if(b == lsb)
			return byt;
		else
			if(b == 0)
				return byt & 0xFE;
			else if(b == 1)
				return byt | 0x01;
		return 0xFF;
		
	}
}
