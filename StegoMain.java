import java.io.InputStreamReader;
import java.util.Scanner;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class StegoMain {
	public static void main(String [] args){
		stego s = new stego();
		
		System.out.println("Do you want to hide some text? Enter 'yes' or 'no'");
		Scanner scan = new Scanner(new InputStreamReader(System.in));
		String in = scan.nextLine();
		
		if(in.equals("yes")){
			System.out.println("Source? text or file? ");
			String source = scan.nextLine();
			if(source.equals("text")){
				System.out.println("What text do you want to hide?");
				String payload = scan.nextLine();
				System.out.println("What cover image do you want to use?");
				String image = scan.nextLine();
				System.out.println("Hiding...");
				System.out.println(s.hideString(payload, image));
			}
			else {
				System.out.println("What is the name of the file? ");
				String source2 = scan.nextLine();
				System.out.println("What cover image do you want to use?");
				String image = scan.nextLine();
				System.out.println("Hiding...");
				System.out.println(s.hideFile(source2, image));
			}
			
		}else{
			System.out.println("Do you want to retrieve some text from image?");
			String in2 = scan.nextLine();
			if(in2.equals("yes")){
				System.out.println("What is the name of the image?");
				String image = scan.nextLine();
				//System.out.println(s.extractString(image));
				System.out.println("What is the storage type: String or file?");
				String type = scan.nextLine();
				if(type.equals("String")){
					try {
						System.out.println("Extracting...");
						FileWriter fw = new FileWriter("payload.txt");
						fw.write(s.extractString(image));
						System.out.println("Done!");
						fw.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				else{
					s.extractFile(image);
				}
				
			}
		}
		
		/*System.out.println(s.getExtension("music.mp3"));
		String f = s.getFileExtensionBits("payload.txt");
		System.out.println(f);
		System.out.println(s.getFileSizeBits("payload.txt"));
		System.out.println(s.binaryToString(f));
		s.hideFile("payload.txt", "colours.bmp");
		*/
	}
}
