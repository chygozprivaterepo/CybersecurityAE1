package AnotherWay;

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

	private List<Integer> colours; //a global variable to hold the bytes for each colour in each pixel of the cover image
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
		
		List<String> payloadList = new ArrayList<String>(); //a list to hold the payload characters as binary strings
		
		//convert each character in the payload into a binary string and save them in a list
		for(int i=0; i<payload.length(); i++){
			payloadList.add(asciiToBinary((int)payload.charAt(i)));
		}
		/*
		for(String s: payloadList){
			System.out.print(s + " ");
		}*/
		
		//open and read the cover image as strings of bytes
		try {
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(cover_filename));
			
			//read in the header bytes and store in a list. This list will not be modified
			
			//////////////////////////////////////
			FileWriter fw = new FileWriter("colours.txt");
			
			/////////////////////////////////////
			List<Integer> header = new ArrayList<Integer>();
			for(int i=0; i<54; i++){
				int c = bis.read();
				header.add(c);
				fw.write(c + "\n");
			}
			
			//read in the colour components and store in a list
			colours = new ArrayList<Integer>();
			for(;;){
				int c = bis.read();
				if(c == -1){
					break;
				}
				else {
					colours.add(c);
					fw.write(c + "\n");
				}
			}
			/////////////////////////////////
			fw.close();
			////////////////////////////////////////////
			/*FileWriter fw = new FileWriter("colours.txt");
			for(Integer i: header){
				fw.write(i + "\n");
			}
			for(Integer i: colours){
				fw.write(i + "\n");
			}
			fw.close();*/
			///////////////////////////////////////////
			
			BufferedImage imageIn = ImageIO.read(new File(cover_filename));
			int pixel = imageIn.getRGB(0,0);
			int red = (pixel >> 16) & 0xFF;
			int green = (pixel >> 8) & 0xFF;
			int blue = (pixel) & 0xFF;
			System.out.printf("Red: %s, Green: %s, Blue: %s, Pixel: %d%n", red, green, blue, pixel);
			//////////////////////////////////////////////////////
			
			//get the payloadSize as a string of binary digits
			int payloadSize = payloadList.size() * byteLength;
			String b1 = Integer.toBinaryString(payloadSize);
			String b3 = "";
			for(int i=0; i<sizeBitsLength-b1.length(); i++){
				b3 += "0";
			}
			String payloadSizeBinary = b3 + b1;
					
			//split payloadSize binary string into 4 bytes and store in its list
			List<String> psList = new ArrayList<String>();
			for(int i=0; i<payloadSizeBinary.length(); i = i+byteLength){
				String sub = payloadSizeBinary.substring(i, i+byteLength);
				psList.add(sub);
			}
			//System.out.printf("Payload list size is: %s with contents %s, %s, %s, %s", psList.size(),
			//		psList.get(0),psList.get(1),psList.get(2),psList.get(3));
			
			//check if the cover image has enough pixels to hide the payload
			if(payloadSize > colours.size()){
				return "Fail because the selected cover image does not have sufficient pixels to hide the payload string";
			}
			
			//use the first 32 entries in the colours list to hide the payload size
			int colStartIndex = 0;
			for(String p: psList){
				hideByte(p, colStartIndex);
				colStartIndex += byteLength;
			}
			
			//use 8 entries in the colours list to hide each byte of the payload
			//colStartIndex = 32 because indices 0 to 31 were used to hide the size bits
			
			for(String payloadByte: payloadList){
				hideByte(payloadByte, colStartIndex);
				colStartIndex += byteLength;
			}
			
			//produce the output stego image with the new contents of the colours list
			String extension = getExtension(cover_filename);
			String basename = getBaseName(cover_filename);
			String output = "stego_" + basename + "." + extension;
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(output));
			//write the header information first
			for(Integer i: header){
				bos.write(i);
			}
			
			//write the pixels
			for(Integer i: colours){
				bos.write(i);
			}
			////////////////////////////////////
			FileWriter fw2 = new FileWriter("colours2.txt");
			
			for(Integer i: header){
				fw2.write(i + "\n");
			}
			for(Integer i: colours){
				fw2.write(i + "\n");
			}
			fw2.close();
			
			///////////////////////////////////////
			bis.close();
			bos.close();
			return output;
			
		} catch (FileNotFoundException e) {
			return "Fail because the cover image could not be found";
		} catch (IOException e){
			return "Fail because the cover image could not be read from";
		}
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
		try{
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(stego_image)); //open an input stream for the image
			List<Integer> header = new ArrayList<Integer>(); //list to contain the header
			
			//get the header information. Will not be used though. Only serves to advance the buffered input stream pointer
			for(int i=0; i < 54; i++){
				int c = bis.read();
				if(c == -1)
					break;
				else
					header.add(c);
			}
			
			//list containing the colours
			List<Integer> colours = new ArrayList<Integer>();
			
			//get the colours
			for(;;){
				int c = bis.read();
				if(c == -1)
					break;
				else
					colours.add(c);
			}
			bis.close(); //close the input stream
			
			//extract payload size bits from the first 32 colour components
			String payloadSizeBits = "";
			for(int i=0; i<sizeBitsLength; i++){
				payloadSizeBits += getLSB(colours.get(i)) + "";
			}
			int payloadSize = Integer.parseInt(payloadSizeBits, 2);
			
			//extract the payload from the rest of the colour list
			String payloadBits = "";
			for(int i=sizeBitsLength; i<sizeBitsLength + payloadSize; i++){
				payloadBits += getLSB(colours.get(i)) + "";
			}
			String payload = binaryToString(payloadBits);
			
			return payload;
		}
		catch (FileNotFoundException e){
			return "Fail because the stego image was not found";
		}
		catch (IOException e){
			return "Fail because the stego image could not be opened";
		}
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
		try {
			List<Integer> payloadList = new ArrayList<Integer>(); //a list to hold the payload characters as binary strings
			//open and read the payload file as strings of bytes
			BufferedInputStream bisFile = new BufferedInputStream(new FileInputStream(file_payload));
			
			//read each byte from the file payload and store it in a list
			for(;;){
				int c = bisFile.read();
				if(c == -1)
					break;
				else
					payloadList.add(c);
			}
		
			//open and read the cover image as strings of bytes
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(cover_image));
			
			//read in the header bytes and store in a list. This list will not be modified
			List<Integer> header = new ArrayList<Integer>();
			for(int i=0; i<54; i++){
				int c = bis.read();
				header.add(c);
			}
			
			//read in the colour components and store in a list
			colours = new ArrayList<Integer>();
			for(;;){
				int c = bis.read();
				if(c == -1){
					break;
				}
				else {
					colours.add(c);
				}
			}			
			
			//get the payload size as a string of binary digits
			int payloadSize = getFileSize(file_payload); //size is in number of bits
			String b1 = Integer.toBinaryString(payloadSize);
			String b3 = "";
			for(int i=0; i<sizeBitsLength-b1.length(); i++){
				b3 += "0";
			}
			String payloadSizeBinary = b3 + b1;
					
			//split payloadSizeBinary string into 4 bytes and store in its list
			List<String> psList = new ArrayList<String>();
			for(int i=0; i<payloadSizeBinary.length(); i = i+byteLength){
				String sub = payloadSizeBinary.substring(i, i+byteLength);
				psList.add(sub);
			}
			
			//get the payload extension as a string of binary digits
			String ext = getExtension(file_payload);
			b1 = stringToBinary(ext);
			b3 = "";
			for(int i=0; i<64-b1.length(); i++){
				b3 += "0";
			}
			String extensionBitsBinary = b3 + b1;
			
			//split extensionBitsBinary string into 8 bytes and store in its list
			List<String> extList = new ArrayList<String>();
			for(int i=0; i<extensionBitsBinary.length(); i = i+byteLength){
				String sub = extensionBitsBinary.substring(i, i+byteLength);
				extList.add(sub);
			}
			
			//check if the cover image has enough pixels to hide the payload
			if(payloadSize > colours.size()){
				return "Fail because the selected cover image does not have sufficient pixels to hide the file";
			}
			
			//use the first 32 entries in the colours list to hide the payload size
			int colStartIndex = 0;
			for(String p: psList){
				hideByte(p, colStartIndex);
				colStartIndex += byteLength;
			}
			
			//use the next 64 entries in the colours list to hide the payload extension
			//colStartIndex = 32 because indices 0 to 31 were used to hide the size bits
			for(String p: extList){
				hideByte(p, colStartIndex);
				colStartIndex += byteLength;
			}
			
			//use 8 entries in the colours list to hide each byte of the payload
			//colStartIndex = 96 because indices 0 to 31 were used to hide the size bits and 32 to 95
			//were used to hide the extension bits
			
			for(Integer payloadByte: payloadList){
				String plbs = asciiToBinary(payloadByte);
				hideByte(plbs, colStartIndex);
				colStartIndex += byteLength;
			}
			
			//produce the output stego image with the new contents of the colours list
			String extension = getExtension(cover_image);
			String basename = getBaseName(cover_image);
			String output = "stego_" + basename + "." + extension;
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(output));
			//write the header information first
			for(Integer i: header){
				bos.write(i);
			}
			
			//write the pixels
			for(Integer i: colours){
				bos.write(i);
			}
			
			bis.close();
			bos.close();
			return output;
			
		} catch (FileNotFoundException e) {
			return "Fail because the cover image or the payload file could not be found";
		} catch (IOException e){
			return "Fail because the cover image or the payload file could not be read from";
		}
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
		try{
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(stego_image)); //open an input stream for the image
			List<Integer> header = new ArrayList<Integer>(); //list to contain the header
			
			//get the header information. Will not be used though. Only serves to advance the buffered input stream pointer
			for(int i=0; i < 54; i++){
				int c = bis.read();
				if(c == -1)
					break;
				else
					header.add(c);
			}
			
			//list containing the colours
			List<Integer> colours = new ArrayList<Integer>();
			
			//get the colours
			for(;;){
				int c = bis.read();
				if(c == -1)
					break;
				else
					colours.add(c);
			}
			bis.close(); //close the input stream
			
			//extract payload size bits from the first 32 colour components
			String payloadSizeBits = "";
			for(int i=0; i<sizeBitsLength; i++){
				payloadSizeBits += getLSB(colours.get(i)) + "";
			}
			int payloadSize = Integer.parseInt(payloadSizeBits, 2);
			
			//extract the payload extension from the next 64 colour components
			String payloadExtensionBits = "";
			for(int i=sizeBitsLength; i<sizeBitsLength + 64; i++){
				payloadExtensionBits += getLSB(colours.get(i)) + "";
			}
			String payloadExtension = binaryToString(payloadExtensionBits);
			
			//extract the payload from the rest of the colour list
			String output = "original_payload." + payloadExtension;
			//System.out.println(payloadExtension);
			//System.out.println(payloadSize);
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(output));
			String payloadBits = "";
			for(int i=96; i<96 + payloadSize; i++){
				String bit = getLSB(colours.get(i)) + "";
				payloadBits += bit;
			}
			//String payload = binaryToString(payloadBits);

			//split the payloadBits into bytes and write each byte to the output stream
			for(int i=0; i<payloadBits.length(); i = i+byteLength){
				String s = payloadBits.substring(i, i+byteLength);
				int ss = Integer.parseInt(s,2);
				bos.write(ss);	
			}
			
			bos.close(); //close the output stream
			return output; //return the recovered file payload
		}
		catch (FileNotFoundException e){
			return "Fail because the stego image was not found";
		}
		catch (IOException e){
			return "Fail because the stego image could not be opened or the payload file could not be written to";
		}
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
			else
				return byt | 0x01;
	}
	
	/**
	 * method to return the LSB of a byte
	 * @param component the byte whose LSB is sought
	 * @return the LSB
	 */
	private int getLSB(int component){
		return component & 0x01;
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
	
	/**
	 * method to convert a char from ascii to binary
	 * @param i the ascii of the char
	 * @return the binary string equivalent
	 */
	private String asciiToBinary(int i){
		String s = Integer.toBinaryString(i);
		int b2 = s.length();
		String bin = "";
		for(int j=0; j<8-b2; j++){
			bin += "0";
		}
		s = bin + s;
		return s;
	}
	
	private void hideByte(String payloadByte, int colStartIndex){
		for(int i=0; i<payloadByte.length(); i++){
			int last = Integer.parseInt(payloadByte.charAt(i) + "");
			int swapResult = swapLsb(last, colours.get(colStartIndex));
			colours.set(colStartIndex, swapResult);
			colStartIndex++;
		}
	}
	
	private String getBaseName(String fname){
		int i = fname.lastIndexOf('.');
		return fname.substring(0, i);
	}
	
}
