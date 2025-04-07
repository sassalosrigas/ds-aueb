package main;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {


    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        System.out.println("Choose mode: (1) manager, (2) client");
        int mode = in.nextInt();
        new Master();
        if(mode == 1){
            System.out.println("Now working in manager mode");
            Customer customer = new Customer("aaa", "123", 37.9932963, 38.733413);
            try {
                int choice;
                do{
                    System.out.println("Choose action: ");
                    System.out.println("1. Add store");
                    System.out.println("2. Add product to store");
                    System.out.println("3. Remove product from store");
                    System.out.println("4. Update stock of product");
                    System.out.println("5. Sales per product");
                    System.out.println("6. Show nearby stores");
                    System.out.println("7. Filter stores");
                    System.out.println("0. Exit");
                    choice = in.nextInt();
                    System.out.println(choice);
                    in.nextLine();
                    switch (choice){
                        case 1:
                            Manager.addStore(in);
                            break;
                        case 2:
                            Manager.addProductToStore(in);
                            break;
                        case 3:
                            Manager.removeProductFromStore(in);
                            break;
                        case 4:
                            Manager.modifyAvailability(in);
                            break;
                        case 5:
                            Manager.salesPerProduct(in);
                            break;
                        case 6:
                            customer.showNearbyStores();
                            break;
                        case 7:
                            customer.filterStores(in);
                            break;
                        case 0 :
                            break;
                    }
                }while (choice != 0);
            }catch (Exception e) {
                throw new RuntimeException(e);
            }
        }else{
            System.out.println("Now working in customer mode");
            try{
                int choice;
                do {
                    System.out.println("Choose action: ");
                    System.out.println("1. Show nearby stores");
                    choice = in.nextInt();
                    System.out.println(choice);
                    in.nextLine();
                    switch (choice){
                        case 1:
                            //Customer.showNearbyStores();
                            break;
                        case 0:
                            break;
                    }
                }while(choice!=0);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }

        in.close();

    }
}
