import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

import javax.imageio.ImageIO;

class Steg
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

	public Steg()
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
			System.out.println("The chosen image does not have sufficient pixels to hide data");
			return "Fail";
		}
	
		String bitsForSize = String.format("%032d",Integer.parseInt(Integer.toBinaryString(binaryPayload.length())));
		String dataToHide = bitsForSize + binaryPayload;

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
				int newRed = swapLsb(bitToHide1, red);
				aa = (aa << 8) + newRed;
				i++;
			}
			else{
				int bitToHide1 = Integer.parseInt(dataToHide.charAt(i)+"");
				int newRed = swapLsb(bitToHide1, red);
				aa = (((aa << 8) + newRed) << 16) + (in & 0x0000FFFF);
				
			}
		
			if(i < dataToHide.length()-1){
				int bitToHide2 = Integer.parseInt(dataToHide.charAt(i)+"");

				int newGreen = swapLsb(bitToHide2, green);
				aa = (aa << 8) + newGreen;
				i++;
			}
			else{
				int bitToHide2 = Integer.parseInt(dataToHide.charAt(i)+"");
				int newGreen = swapLsb(bitToHide2, green);
				aa = (((aa << 8) + newGreen ) << 8) + (in & 0x000000FF);
				
			}
				
			if(i < dataToHide.length()-1){
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
					int red = (pixels.get(j) >> 16) & 0x000000FF;
					payloadBinary += getLSB(red);
					k++;
					int green = (pixels.get(j) >> 8) & 0x000000FF;
					payloadBinary += getLSB(green);
					k++;
					int blue = pixels.get(j) & 0x000000FF;
					payloadBinary += getLSB(blue);
					k++;
				}
			}
			j++;
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
		return null;
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
	
	/**
	 * method to convert a binary string to a string of characters
	 * @param bin the binary string to be converted
	 * @return the string representation
	 */
	private String binaryToString(String bin){
		String s = "";
		for(int i=0; i<bin.length()-8; i=i+8){
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
	private int noOfRequiredBytes(String bin){
		int noOfBitsInPayload = bin.length();
		int noOfBitsForPayloadBinarySize = stringToBinary(noOfBitsInPayload+"").length();
		return noOfBitsInPayload + noOfBitsForPayloadBinarySize;
	}

	private ArrayList<Integer> getImagePixels (String imageName){
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
}