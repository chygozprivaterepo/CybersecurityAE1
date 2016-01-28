import java.io.InputStreamReader;
import java.util.Scanner;

public class StegoMain {
	public static void main(String [] args){
		Steg s = new Steg();
		
		System.out.println("Do you want to hide some text? Enter 'yes' or 'no'");
		Scanner scan = new Scanner(new InputStreamReader(System.in));
		String in = scan.nextLine();
		if(in.equals("yes")){
			System.out.println("What text do you want to hide?");
			String payload = scan.nextLine();
			System.out.println("What cover image do you want to use?");
			String image = scan.nextLine();
			System.out.println(s.hideString(payload, image));
			
		}else{
			System.out.println("Do you want to retrieve some text from image?");
			String in2 = scan.nextLine();
			if(in2.equals("yes")){
				System.out.println("What is the name of the image?");
				String image = scan.nextLine();
				System.out.println(s.extractString(image));
			}
			
		}
			
	}
}
