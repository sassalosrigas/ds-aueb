import java.util.Scanner;

public class Manager{

    
    public void addStore(){
        Scanner in = new Scanner(System.in);
        System.out.println("Give name:");
        String storeName = in.nextLine();
        System.out.println("Give lattitude:");
        double lattitude = in.nextDouble();
        System.out.println("Give longitude:");
        double longitude = in.nextDouble();
        System.out.println("Give food category: ");
        String foodCategory = in.nextLine();
        System.out.println("Give stars: ");
        double stars = in.nextDouble();
        System.out.println("Give number of votes: ");
        int numOfVotes = in.nextInt();
        System.out.println("Give store logo: ");
        String storeLogo = in.nextLine();
        Store newStore = new Store(storeName, lattitude, longitude, foodCategory, stars, numOfVotes, storeLogo);
        in.close();
    }

    public void addProduct(){
        
    }
}