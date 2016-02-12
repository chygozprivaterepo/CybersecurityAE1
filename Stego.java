import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

import javax.imageio.ImageIO;

class stego
{



	/**
	 * A constant to hold the number of bits per byte
	 */
	private final int byteLength=8;

	/**
	 * A constant to hold the number of bits used to store the size of the file extracted
	 */
	protected final int sizeBitsLength=32;
	/**
	 * A constant to hold the number of bits used to store the extension of the file extracted
	 */
	protected final int extBitsLength=64;


	/**
	 *Default constructor to create a steg object, doesn't do anything - so we actually don't need to declare it explicitly. Oh well. 
	 */

	public stego()
	{

	}

	/**
	A method for hiding a string in an uncompressed image file such as a .bmp or .png
	You can assume a .bmp will be used
	@param cover_filename - the filename of the cover image as a string 
	@param payload - the string which should be hidden in the cover image.
	@return a string which either contains 'Fail' or the name of the stego image which has been 
	written out as a result of the successful hiding operation. 
	You can assume that the images are all in the same directory as the java files
	 */
	//TODO you must write this method
	public String hideString(String payload, String cover_filename)
	{
		String binaryPayload = stringToBinary(payload);
		ArrayList<Integer> pixels = getImagePixels(cover_filename);
		int noOfPixels = getImageWidth(cover_filename) * getImageHeight(cover_filename);
		int nrb = noOfRequiredBytes(binaryPayload);
		if(nrb > noOfPixels * 3){
			return "Fail because the chosen image does not have sufficient pixels to hide data";
		}
	
		//Pad it with zeros to make a total of 32 bits
		String b1 = Integer.toBinaryString(binaryPayload.length());
		int b2 = b1.length();
		String bitsForSize = "";
		for(int i=0; i<sizeBitsLength-b2; i++){
			bitsForSize += "0";
		}
		bitsForSize += b1;
		String dataToHide = bitsForSize + binaryPayload;

		int i = 0, j = 0;

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
				int newRed = swapLsb(bitToHide1, red);
				aa = (aa << 8) + newRed;
				i++;
			}
			 else if(i == dataToHide.length()-1){
				int bitToHide1 = Integer.parseInt(dataToHide.charAt(i)+"");
				int newRed = swapLsb(bitToHide1, red);
				aa = (((aa << 8) + newRed) << 16) + (in & 0x0000FFFF);
				i++;
			}
		
			if(i < dataToHide.length()-1){
				int bitToHide2 = Integer.parseInt(dataToHide.charAt(i)+"");
				int newGreen = swapLsb(bitToHide2, green);
				aa = (aa << 8) + newGreen;
				i++;
			}
			else if(i == dataToHide.length()-1){
				int bitToHide2 = Integer.parseInt(dataToHide.charAt(i)+"");
				int newGreen = swapLsb(bitToHide2, green);
				aa = (((aa << 8) + newGreen ) << 8) + (in & 0x000000FF);
				i++;
			}
				
			if(i <= dataToHide.length()-1){
				int bitToHide3 = Integer.parseInt(dataToHide.charAt(i)+"");
				int newBlue = swapLsb(bitToHide3, blue);
				aa = (aa << 8) + newBlue;
				i++;
			}
			
			pixels.set(j, aa);
			j++;
		}	
		
		String output = "stego_"+cover_filename;
		saveStegoImage(output, cover_filename, pixels);
		return output;
	} 
	
	//TODO you must write this method
	/**
	The extractString method should extract a string which has been hidden in the stegoimage
	@param the name of the stego image 
	@return a string which contains either the message which has been extracted or 'Fail' which indicates the extraction
	was unsuccessful
	 */
	public String extractString(String stego_image)
	{
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
		
		String payload = binaryToString(payloadBinary);
		return payload;
	}

	//TODO you must write this method
	/**
	The hideFile method hides any file (so long as there's enough capacity in the image file) in a cover image
	@param file_payload - the name of the file to be hidden, you can assume it is in the same directory as the program
	@param cover_image - the name of the cover image file, you can assume it is in the same directory as the program
	@return String - either 'Fail' to indicate an error in the hiding process, or the name of the stego image written out as a
	result of the successful hiding process
	 */
	public String hideFile(String file_payload, String cover_image)
	{
		ArrayList<Integer> pixels = getImagePixels(cover_image);
		String extensionBits = getFileExtensionBits(file_payload);
		String sizeBits = getFileSizeBits(file_payload);
		List<Integer> payloadBinary = getFileBytes(file_payload);
		
		int totalSize = sizeBitsLength + extBitsLength + getFileSize(file_payload);
		int noOfPixels = getImageWidth(cover_image) * getImageHeight(cover_image);
		
		if(totalSize > noOfPixels * 3){
			return "Fail because the chosen image does not have sufficient pixels to hide data";
		}
		
		//hide the size bits in the first 32 colour components of the pixels in the cover image
		int i = 0, j = 0;
		while(i < noOfPixels){
			if(j == sizeBitsLength){
				break;
			}
			
			int in = pixels.get(i);
			
			int aa = 255;
			int red = (in >> 16) & 0x000000FF;
			int green = (in >> 8) & 0x000000FF;
			int blue = in & 0x000000FF;
			
			if(j < sizeBitsLength){
				int bitToHide = Integer.parseInt(sizeBits.charAt(j) + "");
				int newRed = swapLsb(red, bitToHide);
				aa = (aa << 8) + newRed;
				j++;
			}
			
			if(j < sizeBitsLength){
				int bitToHide = Integer.parseInt(sizeBits.charAt(j) + "");
				int newGreen = swapLsb(green, bitToHide);
				aa = (aa << 8) + newGreen;
				j++;
			}
			
			if(j < sizeBitsLength){
				int bitToHide = Integer.parseInt(sizeBits.charAt(j) + "");
				int newBlue = swapLsb(blue, bitToHide);
				aa = (aa << 8) + newBlue;
				j++;
			}
			pixels.set(i, aa);
			i++;
		}
		
		//hide the extension bits in the next 64 colour components of the pixels in the cover image
		int k = 0;
		while(i < noOfPixels){
			if(k == extBitsLength){
				break;
			}
			
			int in = pixels.get(i);
			
			int aa = 255;
			int red = (in >> 16) & 0x000000FF;
			int green = (in >> 8) & 0x000000FF;
			int blue = in & 0x000000FF;
			
			if(i == 10){
				//the red and green components of the pixel at index 10 of the pixels list already contain data for the sizeBits
				//and should not be changed. Change the blue component only
				if(k < extBitsLength){
					int bitToHide = Integer.parseInt(extensionBits.charAt(k) + "");
					int newBlue = swapLsb(blue, bitToHide);
					aa = (aa & 0xFFFFFF00) | newBlue;
					k++;
				}
			}
			else {
				if(k < extBitsLength){
					int bitToHide = Integer.parseInt(extensionBits.charAt(k) + "");
					int newRed = swapLsb(red, bitToHide);
					aa = (aa << 8) + newRed;
					k++;
				}
				
				if(k < extBitsLength){
					int bitToHide = Integer.parseInt(extensionBits.charAt(k) + "");
					int newGreen = swapLsb(green, bitToHide);
					aa = (aa << 8) + newGreen;
					k++;
				}
				
				if(k < extBitsLength){
					int bitToHide = Integer.parseInt(extensionBits.charAt(k) + "");
					int newBlue = swapLsb(blue, bitToHide);
					aa = (aa << 8) + newBlue;
					k++;
				}
			}
			pixels.set(j, aa);
			i++;
		}
		
		//hide the payload bits in the rest of the pixels
		System.out.println("i is " + i);
		System.out.println("j is " + j);
		System.out.println("k is " + k);
		
		/*for(Integer inb: payloadBinary){
			if(inb != null){
				String bin = String.format("%08s", Integer.toBinaryString(inb));
				int curr = 0;
				while(i < noOfPixels){
					int in = pixels.get(i);
					int aa = 255;
					int red = (in >> 16) & 0x000000FF;
					int green = (in >> 8) & 0x000000FF;
					int blue = in & 0x000000FF;
					
					while(curr < bin.length()){
						int bitToHide = Integer.parseInt(bin.charAt(curr)+"");
						
						int newRed = swapLsb(red, bitToHide);
						aa = (aa << 8) + newRed;
						curr++;
					}
				}
			}
			*/
		
			String dataToHide = "";
			for(Integer inb: payloadBinary){
				if(inb != null){
					dataToHide += integerToBinary(inb);
				}
			}
			
			i = 0;

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
					int newRed = swapLsb(bitToHide1, red);
					aa = (aa << 8) + newRed;
					i++;
				}
				 else if(i == dataToHide.length()-1){
					int bitToHide1 = Integer.parseInt(dataToHide.charAt(i)+"");
					int newRed = swapLsb(bitToHide1, red);
					aa = (((aa << 8) + newRed) << 16) + (in & 0x0000FFFF);
					i++;
				}
			
				if(i < dataToHide.length()-1){
					int bitToHide2 = Integer.parseInt(dataToHide.charAt(i)+"");
					int newGreen = swapLsb(bitToHide2, green);
					aa = (aa << 8) + newGreen;
					i++;
				}
				else if(i == dataToHide.length()-1){
					int bitToHide2 = Integer.parseInt(dataToHide.charAt(i)+"");
					int newGreen = swapLsb(bitToHide2, green);
					aa = (((aa << 8) + newGreen ) << 8) + (in & 0x000000FF);
					i++;
				}
					
				if(i <= dataToHide.length()-1){
					int bitToHide3 = Integer.parseInt(dataToHide.charAt(i)+"");
					int newBlue = swapLsb(bitToHide3, blue);
					aa = (aa << 8) + newBlue;
					i++;
				}
				
				pixels.set(j, aa);
				j++;
			}	
		
			String output = "stego_"+cover_image;
			saveStegoImage(output, cover_image, pixels);
			return output;
	}

	//TODO you must write this method
	/**
	The extractFile method hides any file (so long as there's enough capacity in the image file) in a cover image
	@param stego_image - the name of the file to be hidden, you can assume it is in the same directory as the program
	@return String - either 'Fail' to indicate an error in the extraction process, or the name of the file written out as a
	result of the successful extraction process
	 */
	public String extractFile(String stego_image)
	{
		ArrayList<Integer> pixels = getImagePixels(stego_image);
		int noOfPixels = getImageWidth(stego_image) * getImageHeight(stego_image);
		
		//get the no of bits of the payload
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
		
		Integer payloadSize = Integer.parseInt(sizeString,2);
		
		//get the file extension
		i = 10;
		String extString = "";
		while(i < 31){
			if(i == 10){
				int blue = pixels.get(i) & 0x000000FF;
				extString += getLSB(blue);
			}
			else{
				int red = (pixels.get(i) >> 16) & 0x000000FF;
				extString += getLSB(red);
				int green = (pixels.get(i) >> 8) & 0x000000FF;
				extString += getLSB(green);
				int blue = pixels.get(i) & 0x000000FF;
				extString += getLSB(blue);
			}
			i++;
		}
		String extension = binaryToString(extString);
		System.out.println("The stego extension is " + extension);
		
		/*
		//get the payload
		int j = 32, k = 0;
		String payloadBinary = "";
		
		while(j < noOfPixels){
			if(k < payloadSize){
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
				j++;
			}
			else 
				break;
		}
		
		String payload = binaryToString(payloadBinary);
		return payload;*/
		return null;
	}

	//TODO you must write this method
	/**
	 * This method swaps the least significant bit with a bit from the filereader
	 * @param bitToHide - the bit which is to replace the lsb of the byte of the image
	 * @param byt - the current byte
	 * @return the altered byte
	 */
	public int swapLsb(int bitToHide,int byt)
	{		
		int lsb = getLSB(byt);
		if(bitToHide == lsb)
			return byt;
		else
			if(bitToHide == 0)
				return byt & 0xFE;
			else if(bitToHide == 1)
				return byt | 0x01;
		return 0xFF;
		
	}
	
	/**
	 * method to return the LSB of a byte
	 * @param component the byte whose LSB is sought
	 * @return the LSB
	 */
	private int getLSB(int component){
		return component & 0x00000001;
	}
	
	/**
	 * method to get the binary equivalent of a string
	 * @param s the string to be converted
	 * @return the binary equivalent
	 */
	
	private String stringToBinary(String s){
		String bin = "";
		for(int i=0; i<s.length(); i++)
		{
			int bch = Integer.parseInt(Integer.toBinaryString((int)s.charAt(i)));
			bin += String.format("%08d", bch);
		}
		return bin;
	}
	/*
	private String stringToBinary(String s){
		String bin = "";
		for(int i=0; i<s.length(); i++)
		{
			String b1 = Integer.toBinaryString((int)s.charAt(i));
			int b2 = b1.length();
			for(int j=0; j<8-b2; j++){
				bin += "0";
			}
			bin += b1;
		}
		return bin;
	}*/
	
	/**
	 * method to convert a binary string to a string of characters
	 * @param bin the binary string to be converted
	 * @return the string representation
	 */
	public String binaryToString(String bin){
		String s = "";
		for(int i=0; i<bin.length(); i=i+8){
			String byt = bin.substring(i, i+8); //get 8 bits from the binary string
			if(!byt.equals("00000000"))
				s += (char)Integer.parseInt(byt, 2);  //convert it to a character and add to character string
		}
		return s;
	}
	
	/**
	 * method to get the total number of bytes required to store data related to the payload.
	 * This data includes the payload binary bits and a set of bits to hold the number of bits in the payload
	 * @param bin the payload in binary
	 * @return the number of bytes required
	 */
	private int noOfRequiredBytes(String bin){
		int noOfBitsInPayload = bin.length();
		//int noOfBitsForPayloadBinarySize = stringToBinary(noOfBitsInPayload+"").length();
		return noOfBitsInPayload + sizeBitsLength;
	}

	private ArrayList<Integer> getImagePixels (String imageName){
		ArrayList<Integer> pixels = new ArrayList<Integer>();
	
		BufferedImage imageIn = null;
		try {
			imageIn = ImageIO.read(new File(imageName));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("The image specified does not exist or could not be read");
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
	private int getImageWidth(String image){
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
	private int getImageHeight(String image){
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
	private int getImageType(String image) throws IOException{
		BufferedImage imageIn = ImageIO.read(new File(image));
		return imageIn.getType();
	}

	/**
	 * method to save the stego image
	 * @param image to be saved
	 * @param pixels the list containing the pixels with payload data embedded
	 */
	private void saveStegoImage(String image, String image2, ArrayList<Integer> pixels){
		int index = image2.lastIndexOf('.');
		String extension = image2.substring(index+1);
		File output = new File(image);
		
		try{
			int width = getImageWidth(image2);
			int height = getImageHeight(image2);
			int type = getImageType(image2);
			
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
	 * method to get the extension of a file
	 * @param fname the name of the file
	 * @return the extension
	 */
	public String getExtension(String fname){
		int i = fname.lastIndexOf('.');
		String ext = fname.substring(i+1);
		return ext;
	}
	
	/**
	 * get the bytes that make up the file and put them in a list
	 * @param fname the name of the file
	 * @return the list containing the bytes
	 */
	private List<Integer> getFileBytes(String fname){
		/*List<Integer> list = null;
		try {
			FileInputStream fis = new FileInputStream(new File(fname));
			list = new ArrayList<Integer>();
			while(fis.read() != -1){
				list.add(fis.read());
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e){
			e.printStackTrace();
		}*/
		List<Integer> payloadBinary = null;
		try{
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(fname));
			payloadBinary = new ArrayList<Integer>();
			
			for(;;){
				int c = bis.read();
				if (c == -1)
					break;
				else{
					payloadBinary.add(c);
				}
			}
			bis.close();	
		} catch (FileNotFoundException e){
			e.printStackTrace();
		} catch (IOException e){
			e.printStackTrace();
		}
		
		return payloadBinary;
	}
	
	/**
	 * method to get the binary representation of the file extension
	 * @param fname the name of the file
	 * @return the binary representation of the extension
	 */
	public String getFileExtensionBits(String fname){
		String ext = getExtension(fname);
		String bi = stringToBinary(ext);
		int b1 = bi.length();
		
		String bits = "";
		for(int i=0; i<extBitsLength - b1; i++){
			bits += "0";
		}
		bits += bi;
		return bits;
	}
	
	/**
	 * method to get the binary representation of the size of the file
	 * @param fname the name of the file
	 * @return the binary representation of the size
	 */
	public String getFileSizeBits(String fname){
		int size = getFileSize(fname);
		String b = Integer.toBinaryString(size);
		String bb = "";
		for(int i=0; i < sizeBitsLength - b.length(); i++){
			bb += "0";
		}
		String bits = bb + b;
		return bits;
	}
	
	/**
	 * a method to return the size of the file in terms of how many bits are in the file
	 * @return the size of the file in bits
	 */
	public int getFileSize(String fname)
	{
		File file = new File(fname);
		return (int)file.length()* byteLength;
	}
	
	private String integerToBinary(int i){
		String s = Integer.toBinaryString(i);
		int b2 = s.length();
		String bin = "";
		for(int j=0; j<8-b2; j++){
			bin += "0";
		}
		s = bin + s;
		return s;
	}
}