package main;

import java.util.Scanner;

import static java.lang.Thread.sleep;

public class Main {


    public static void main(String[] args) throws InterruptedException {
        Scanner in = new Scanner(System.in);
        new Master();  //Arxikopoihsh kai run Master server
        sleep(2500);  //kane sleep gia na exei xrono na anoiksei kai na arxikopoihthei o master allies ta addStore ton vriskoun kleisto kai dinoun error
        //ama bgalei error kamia fora thelei restart
        Manager.addStore(new Scanner("src/stores/store.json"));  //Arxikopoihsh stores me thn ekkinhsh
        Manager.addStore(new Scanner("src/stores/store2.json"));
        Manager.addStore(new Scanner("src/stores/store3.json"));
        Manager.addStore(new Scanner("src/stores/store4.json"));
        Manager.addStore(new Scanner("src/stores/store5.json"));
        Manager.addStore(new Scanner("src/stores/store6.json"));
        Manager.addStore(new Scanner("src/stores/store7.json"));
        int mode;
        do {
            System.out.println("Choose mode: (1) manager, (2) client, (3) Exit");
            mode = in.nextInt();
            if(mode == 3){
                break;
            }
            if (mode == 1) {
                //Console menu leitourgias manager
                System.out.println("Now working in manager mode");
                try {
                    int choice;
                    do {
                        System.out.println("Choose action: ");
                        System.out.println("1. Add store");
                        System.out.println("2. Add product to store");
                        System.out.println("3. Remove product from store");
                        System.out.println("4. Update stock of product");
                        System.out.println("5. Statistics");
                        System.out.println("0. Exit");
                        choice = in.nextInt();
                        System.out.println(choice);
                        in.nextLine();
                        switch (choice) {
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
                            case 0:
                                break;
                        }
                    } while (choice != 0);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else if (mode == 2) {
                //Console menu leitourgias customer
                System.out.println("Now working in customer mode");
                Customer customer = new Customer("rigas", "123", 37.986633, 23.734900);
                try {
                    int choice;
                    do {
                        System.out.println("Choose action: ");
                        System.out.println("1. Show nearby stores");
                        System.out.println("2. Filter stores");
                        System.out.println("3. Buy products");
                        System.out.println("4. Rate store");
                        System.out.println("0. Exit");
                        choice = in.nextInt();
                        System.out.println(choice);
                        in.nextLine();
                        switch (choice) {
                            case 1:
                                customer.showNearbyStores();
                                break;
                            case 2:
                                customer.filterStores(in);
                                break;
                            case 3:
                                customer.buyProducts(in);
                                break;
                            case 4:
                                customer.rateStore(in);
                                break;
                            case 0:
                                break;
                        }
                    } while (choice != 0);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            }
        }while(true);
        in.close();
    }
}
